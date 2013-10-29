package org.hdstar.component;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.Const;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * 控制下载服务时，intent必须带的参数：("command", DownloadService.COMMAND_DOWNLOAD_XXX),
 * ("downloadInfo", DownloadInfo)，downloadInfo.size为-1时，使用http的返回头content-length
 * downloadInfo.iconUrl使用以下两种格式：http://xxx或者package:xxx，如果以package开头，则通过package获取应用图标
 */
public class DownloadService extends Service {
	public static final int DOWNLOAD_STATUS_PAUSED = 0;
	public static final int DOWNLOAD_STATUS_WAITING = 1;
	public static final int DOWNLOAD_STATUS_RUNNING = 2;
	public static final int DOWNLOAD_STATUS_FINISHED = 3;
	public static final int DOWNLOAD_STATUS_PAUSING = 4;
	public static final int DOWNLOAD_STATUS_FAILED = 5;
	//下载任务状态更改
	public static final String ACTION_DOWNLOAD_STATUS_CHANGED = "org.hdstar.action.download.status.changed";
	//下载任务失败
	//public static final String ACTION_DOWNLOAD_FAILED = "org.hdstar.action.download.failed";
	//下载进度更新
	public static final String ACTION_DOWNLOAD_UPDATE_PROGRESS = "org.hdstar.action.download.update.progress";
	//添加一个下载任务
	public static final int COMMAND_DOWNLOAD_ADD = 0;
	//移除一个下载任务以及本地文件
	public static final int COMMAND_DOWNLOAD_PAUSE = 1;
	//停止一个下载任务
	public static final int COMMAND_DOWNLOAD_STOP = 2;
	//停止所有下载任务并结束Service
	public static final int COMMAND_DOWNLOAD_EXIT = 3;
	//静默下载，不发送进度更新广播
	public static final int COMMAND_DOWNLOAD_SLIENT = 4;
	//默认模式下载，自动发送进度更新广播
	public static final int COMMAND_DOWNLOAD_DEFAULT = 5;
	public static int runningTaskCount = 0;
	private NotificationManager nm;
	private Notification nf;
	private ControlThread controlThread = null;
	//private boolean pause = false;

	@Override
	public void onCreate() {
		runningTaskCount = 0;
		super.onCreate();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nf = new Notification();
//		nf.icon = R.drawable.app_manager_ic;
		//nf.defaults |= Notification.DEFAULT_SOUND;
//        nf.defaults |= Notification.DEFAULT_VIBRATE;
        nf.defaults |= Notification.DEFAULT_LIGHTS;
        nf.flags |= Notification.FLAG_AUTO_CANCEL;
        controlThread = new ControlThread();
        controlThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null){
			return super.onStartCommand(intent, flags, startId);
		}
		int command = intent.getIntExtra("command", -1);
		switch(command){
		case COMMAND_DOWNLOAD_ADD:
			add();
			break;
		case COMMAND_DOWNLOAD_PAUSE:
			pause();
			break;
		case COMMAND_DOWNLOAD_STOP:
			pause();
			break;
		case COMMAND_DOWNLOAD_EXIT:
			stopSelf();
			break;
		case COMMAND_DOWNLOAD_SLIENT:
			switchMode(command);
			break;
		case COMMAND_DOWNLOAD_DEFAULT:
			switchMode(command);
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
		if(controlThread != null){
			controlThread.pause();
		}
		super.onDestroy();
	}
	
	public void switchMode(int downloadMode){
		switch(downloadMode){
		case COMMAND_DOWNLOAD_SLIENT:
			if(controlThread != null){
				controlThread.pause();
			}
			break;
		case COMMAND_DOWNLOAD_DEFAULT:
			if (controlThread == null || controlThread.paused) {
				controlThread = new ControlThread();
				controlThread.start();
			}
			break;
		}
	}

	void add(DownloadInfo info){
		DownloadTask task = new DownloadTask(info);
	}
	
	void pause(){
		
	}

	/**
	 * 停止一个下载任务
	 */
	void stop(int id){
//		DownloadTask task = findTaskById(id);
//		if(task != null){
//			task.pauseNDelete();
//		}
	}
	
	private void updateProgress(){
		Intent intent = new Intent(ACTION_DOWNLOAD_UPDATE_PROGRESS);
		if(runningTaskCount < 1){
			return;
		}
		int[] ids = new int[runningTaskCount];
		int[] completeSizeArr = new int[runningTaskCount];
		intent.putExtra("ids", ids);
		intent.putExtra("completeSizeArr", completeSizeArr);
		sendBroadcast(intent);
	}
	
	private void updateStatus(int status){
		Intent intent = new Intent(ACTION_DOWNLOAD_STATUS_CHANGED);
		intent.putExtra("status", status);
		sendBroadcast(intent);
	}
	
	/**
	 * 一个下载任务执行完毕后的后续工作
	 * @param task 执行完毕的task
	 * @param isSuccessfully true 成功执行 false 执行失败
	 */
	private synchronized void finishOneTask(DownloadTask task, boolean isSuccessfully
			, DownloadDAO dao){
		Intent nfIntent = null;
		PendingIntent pIntent = null;
		String title = task.appCode;
		String text;
		int status;
		if(isSuccessfully){
			status = DOWNLOAD_STATUS_FINISHED;
			title = task.appCode;
			text = "下载完毕，点击查看";
//			nfIntent = new Intent(Intent.ACTION_VIEW);
//			nfIntent.setDataAndType(Uri.fromFile(new File(Const.DOWNLOAD_PATH 
//					+ File.separator + task.appCode + task.appVersion + Const.FILE_SUFFIX))
//					, "application/vnd.android.package-archive");
		} else{
			status = DOWNLOAD_STATUS_FAILED;
			title = task.appCode;
			text = "下载失败，点击查看";
			//nfIntent = new Intent(this, ActivityAppDownloadManager.class);
		}
		updateStatus(task.id, status, dao);
		nfIntent = new Intent(this, ActivityAppDownloadManager.class);
		nf.tickerText = title + text;
		pIntent = PendingIntent.getActivity(this, 0, nfIntent, 0);
		nf.setLatestEventInfo(this, title, text, pIntent);
		nm.notify(0, nf);
	}
	
	class ControlThread extends Thread{
		private boolean paused = false;
		
		public void pause(){
			paused = true;
		}
		
		@Override
		public void run(){
			while(!paused){
    			try {
					Thread.sleep(1000);
					updateProgress();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
		}
	}
	
	class DownloadTask extends Thread{
		private int appCode;
		private boolean isPatch;
		private boolean isNew;
		private boolean paused = false;
		private int size = -1;
		private long startPos = 0;
		private HttpGet get;
		private InputStream in = null;
		private Boolean isRunning = false;
		
		DownloadTask(int appCode, boolean isPatch, boolean isNew){
			this.appCode = appCode;
			this.isPatch = isPatch;
			this.isNew = isNew;
		}
		
		@Override
		public void run(){
			synchronized(isRunning){
				isRunning = true;
			}
			RandomAccessFile file = null;
			updateStatus(DOWNLOAD_STATUS_RUNNING);
			HttpClient client = null;
			try {
				File dir = new File(Const.DOWNLOAD_DIR);
				if(!dir.exists()){
					dir.mkdirs();
				}
				client = CustomHttpClient.getHttpClient();
				get = new HttpGet(Const.Urls.SERVER_DOWNLOAD_URL + "appCode=" + appCode + "patch=" + isPatch);
				if(!isNew){
					SharedPreferences shared = DownloadService.this.getSharedPreferences(Const.SHARED_PREFS, MODE_PRIVATE);
					String fileName = shared.getString("downloadFile", null);
					if(fileName != null){
						file = new RandomAccessFile(dir.getAbsolutePath() + File.separator + fileName, "rwd");
						startPos = file.length();
					}
				}
				HttpResponse response = client.execute(get);
				
			    file = new RandomAccessFile(dir.getAbsolutePath() + File.separator 
			    		+ "HDSky.tmp", "rwd");
			    file.seek(startPos);
			    int offset = 0;
			    in = con.getInputStream();
					byte[] data = new byte[16 * 1024];
					while (!paused && (offset = in.read(data)) != -1) {
						file.write(data, 0, offset);
						startPos += offset;
					}
//			    }
				
				/*if(startPos == size){
					finishOneTask(this, true, dao);
				}*/
			} catch (Exception e) {
				e.printStackTrace();
				if(!paused){
					finishOneTask(this, false, dao);
				}
			} finally{
//				if(fetcher != null){
//					fetcher.disconnect();
//				}
				IOUtils.closeInputStreamIgnoreExceptions(in);
				IOUtils.closeFileIgnoreExceptions(file);
				if(startPos == size){
					finishOneTask(this, true, dao);
				} else if(paused){
					pauseOneTask(id, dao);
				} else{
					finishOneTask(this, false, dao);
				}
				dao.updateProgress(id, startPos);
				long lastModified = new File(Const.DEFAULT_DOWNLOAD_PATH + File.separator 
			    		+ Const.FILE_SUFFIX).lastModified();
				dao.updateLastModified(id, lastModified);
				synchronized (downloadTaskList) {
					runningTaskCount--;
					downloadTaskList.remove(this);
				}
				executeOneTask(-1);
			}
		}
		
		protected void pause(boolean isPause){
			paused = isPause;
			if(paused && con != null){
				con.disconnect();
			}
			if(paused && in != null){
				IOUtils.closeInputStreamIgnoreExceptions(in);
			}
		}
		
		void pauseNDelete(){
			pause(true);
			File file = new File(TerminalToolsApp.CustomSetting.DOWNLOAD_DIR + File.separator 
			    		+ appCode + appVersion);
			file.delete();
		}
	}
}
