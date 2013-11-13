package org.hdstar.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.activity.DownloadActivity;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import cn.sgwhp.patchdroid.PatchClient;

/**
 * 
 * 控制下载服务时，intent必须带的参数：("command", DownloadService.COMMAND_DOWNLOAD_XXX),
 * ("appCode", appCode) ("isPatch", isPatch)
 */
public class DownloadService extends Service {
	public static final int DOWNLOAD_STATUS_PAUSED = 0;
	public static final int DOWNLOAD_STATUS_WAITING = 1;
	public static final int DOWNLOAD_STATUS_RUNNING = 2;
	public static final int DOWNLOAD_STATUS_FINISHED = 3;
	public static final int DOWNLOAD_STATUS_PAUSING = 4;
	public static final int DOWNLOAD_STATUS_FAILED = 5;
	public static final int DOWNLOAD_STATUS_START = 6;
	public static final int DOWNLOAD_STATUS_PATCH_FAILED = 7;
	// 下载任务状态更改
	public static final String ACTION_DOWNLOAD_STATUS_CHANGED = "org.hdstar.action.download.status.changed";
	// 下载任务失败
	// public static final String ACTION_DOWNLOAD_FAILED =
	// "org.hdstar.action.download.failed";
	// 下载进度更新
	public static final String ACTION_DOWNLOAD_UPDATE_PROGRESS = "org.hdstar.action.download.update.progress";
	// 添加一个下载任务
	public static final int COMMAND_DOWNLOAD_ADD = 0;
	// 移除一个下载任务以及本地文件
	public static final int COMMAND_DOWNLOAD_PAUSE = 1;
	// 停止一个下载任务
	public static final int COMMAND_DOWNLOAD_STOP = 2;
	// 停止所有下载任务并结束Service
	public static final int COMMAND_DOWNLOAD_EXIT = 3;
	// 继续下载历史记录
	public static final int COMMAND_DOWNLOAD_RESUME = 4;
	private NotificationManager nm;
	private Notification nf;
	private DownloadTask task;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		int command = intent.getIntExtra("command", -1);
		switch (command) {
		case COMMAND_DOWNLOAD_ADD:
			add(intent.getBooleanExtra("isPatch", false), true);
			break;
		case COMMAND_DOWNLOAD_PAUSE:
			pause();
			break;
		case COMMAND_DOWNLOAD_STOP:
			stop();
			break;
		case COMMAND_DOWNLOAD_EXIT:
			stopSelf();
			break;
		case COMMAND_DOWNLOAD_RESUME:
			add(intent.getBooleanExtra("isPatch", false), false);
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	void add(boolean isPatch, boolean isNew) {
		task = new DownloadTask(isPatch, isNew);
		task.start();
	}

	void pause() {
		if (task != null) {
			task.pause(true);
			updateStatus(DOWNLOAD_STATUS_PAUSED);
		}
	}

	/**
	 * 停止一个下载任务
	 */
	void stop() {
		if (task != null) {
			task.pauseNDelete();
			updateStatus(DOWNLOAD_STATUS_PAUSED);
		}
	}

	private void updateProgress(long progress) {
		Intent intent = new Intent(ACTION_DOWNLOAD_UPDATE_PROGRESS);
		intent.putExtra("completeSize", progress);
		sendBroadcast(intent);
	}

	private void updateStatus(int status) {
		Intent intent = new Intent(ACTION_DOWNLOAD_STATUS_CHANGED);
		intent.putExtra("status", status);
		sendBroadcast(intent);
	}

	private void updateStatus(Intent intent) {
		sendBroadcast(intent);
	}

	/**
	 * 一个下载任务执行完毕后的后续工作
	 * 
	 * @param task
	 *            执行完毕的task
	 * @param isSuccessfully
	 *            true 成功执行 false 执行失败
	 */
	private synchronized void finishDownload(DownloadTask task,
			boolean isSuccessfully) {
		Intent nfIntent = null;
		PendingIntent pIntent = null;
		String text;
		int status;
		if (isSuccessfully) {
			status = DOWNLOAD_STATUS_FINISHED;
			text = this.getResources().getString(R.string.download_complete);
			nfIntent = new Intent(Intent.ACTION_VIEW);
			nfIntent.setDataAndType(
					Uri.fromFile(new File(Const.DOWNLOAD_DIR + File.separator
							+ task.fileName)),
					"application/vnd.android.package-archive");
		} else {
			status = DOWNLOAD_STATUS_FAILED;
			text = "下载失败，点击查看";
			nfIntent = new Intent(this, DownloadActivity.class);
		}
		updateStatus(status);
		pIntent = PendingIntent.getActivity(this, 0, nfIntent, 0);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nf = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher).setTicker(text)
				.setContentTitle(getText(R.string.notification_title))
				.setContentText(text).setContentIntent(pIntent).build();
		nf.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(0, nf);
	}

	class DownloadTask extends Thread {
		private boolean isPatch;
		private boolean isNew;
		private boolean paused = false;
		private long size = -1;
		private long startPos = 0;
		private long lastUpdate;
		private final float UPDATE_PERCENT = 0.05f;// 5%更新一次进度
		private long updateOffset;
		private String fileName;
		private HttpGet get;
		private InputStream in = null;

		DownloadTask(boolean isPatch, boolean isNew) {
			this.isPatch = isPatch;
			this.isNew = isNew;
		}

		@Override
		public void run() {
			SharedPreferences shared = DownloadService.this
					.getSharedPreferences(Const.DOWNLOAD_SHARED_PREFS,
							MODE_PRIVATE);
			Editor editor = shared.edit();
			RandomAccessFile file = null;
			editor.putInt("status", DOWNLOAD_STATUS_RUNNING);
			editor.commit();
			updateStatus(DOWNLOAD_STATUS_RUNNING);
			HttpClient client = null;
			try {
				File dir = new File(Const.DOWNLOAD_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				client = CustomHttpClient.getHttpClient();
				 get = new HttpGet(Const.Urls.SERVER_DOWNLOAD_URL + "appCode="
				 + Const.APP_CODE + "patch=" + isPatch);
//				get = new HttpGet(
//						"http://10.10.28.113:8084/HDStarService/download?appCode="
//								+ Const.APP_CODE + "&appVersion=164");
				if (!isNew) {
					fileName = shared.getString("downloadFile", null);
					if (fileName != null) {
						file = new RandomAccessFile(dir.getAbsolutePath()
								+ File.separator + fileName, "rwd");
						startPos = file.length();
						lastUpdate = startPos;
					}
				}
				get.setHeader("Range", "bytes=" + startPos + "-");
				HttpResponse response = client.execute(get);
				size = Long.parseLong(response.getFirstHeader("Content-Length")
						.getValue());
				editor.putLong("size", size);
				editor.commit();
				Intent intent = new Intent(ACTION_DOWNLOAD_STATUS_CHANGED);
				intent.putExtra("status", DOWNLOAD_STATUS_START);
				intent.putExtra("size", size);
				updateStatus(intent);
				updateOffset = (long) (size * UPDATE_PERCENT);
				if (fileName == null) {
					fileName = response.getFirstHeader("Content-Disposition")
							.getValue();
					fileName = fileName
							.substring(fileName.indexOf("filename=") + 10);
					editor.putString("downloadFile", fileName);
					editor.commit();
				}
				file = new RandomAccessFile(dir.getAbsolutePath()
						+ File.separator + fileName, "rwd");
				file.seek(startPos);
				int offset = 0;
				in = response.getEntity().getContent();
				byte[] data = new byte[16 * 1024];
				while (!paused && (offset = in.read(data)) != -1) {
					file.write(data, 0, offset);
					startPos += offset;
					if (startPos - lastUpdate >= updateOffset) {
						lastUpdate = startPos;
						updateProgress(startPos);
					}
				}
				updateProgress(startPos);
				try {
					if (isPatch) {
						PatchClient.applyPatchToOwn(DownloadService.this,
								Const.DOWNLOAD_DIR + File.separator + fileName,
								Const.DOWNLOAD_DIR + File.separator
										+ "patch.patch");
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					updateStatus(DOWNLOAD_STATUS_PATCH_FAILED);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (!paused) {
					finishDownload(this, false);
				}
			} finally {
				IOUtils.closeInputStreamIgnoreExceptions(in);
				IOUtils.closeFileIgnoreExceptions(file);
				editor.putLong("completeSize", startPos);
				if (startPos == size) {
					editor.putInt("status", DOWNLOAD_STATUS_FINISHED);
					editor.commit();
					finishDownload(this, true);
				} else if (paused) {
					editor.putInt("status", DOWNLOAD_STATUS_PAUSED);
					editor.commit();
					DownloadService.this.pause();
				} else {
					editor.putInt("status", DOWNLOAD_STATUS_FAILED);
					editor.commit();
					finishDownload(this, false);
				}
			}
		}

		protected void pause(boolean isPause) {
			paused = isPause;
			if (paused && get != null) {
				get.abort();
			}
		}

		void pauseNDelete() {
			pause(true);
			File file = new File(Const.DOWNLOAD_DIR + File.separator + fileName);
			file.delete();
		}
	}
}
