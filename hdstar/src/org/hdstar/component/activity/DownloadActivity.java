package org.hdstar.component.activity;

import java.io.File;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.DownloadService;
import org.hdstar.util.Util;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

/**
 * 下载管理
 * 
 * @author robust
 * 
 */
public class DownloadActivity extends SherlockActivity implements
		OnClickListener {
	private static final int THUMBNAIL_MARGIN = 4;// dp
	private ProgressBar progress;
	private ImageButton ctrlBtn;
	private ImageButton cancelBtn;
	private TextView sizeTV;
	private TextView versionTV;
	private TextView updateDate;
	private TextView descTV;
	private LinearLayout thumbnails;
	private boolean isPatch;
	private long downloadSize;
	private int status;
	private String[] pics;
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.download);
		progress = (ProgressBar) findViewById(R.id.download_progress);
		ctrlBtn = (ImageButton) findViewById(R.id.download_ctrl_btn);
		cancelBtn = (ImageButton) findViewById(R.id.download_cancel_btn);
		sizeTV = (TextView) findViewById(R.id.app_size);
		versionTV = (TextView) findViewById(R.id.app_version);
		updateDate = (TextView) findViewById(R.id.update_date);
		descTV = (TextView) findViewById(R.id.desc);
		thumbnails = (LinearLayout) findViewById(R.id.thumbnails);
		prefs = this.getSharedPreferences(Const.DOWNLOAD_SHARED_PREFS,
				MODE_PRIVATE);
		long size = prefs.getLong("size", 0);
		long patchSize = prefs.getLong("patchSize", 0);
		if (patchSize == 0) {
			// 普通升级
			downloadSize = size;
			sizeTV.setText(Util.formatFileSize(size));
		} else {
			// 增量升级
			isPatch = true;
			downloadSize = patchSize;
			String oriSize = Util.formatFileSize(size).toString();
			String patSize = Util.formatFileSize(patchSize).toString();
			SpannableString ss = new SpannableString(oriSize + "  " + patSize);
			ss.setSpan(new StrikethroughSpan(), 0, oriSize.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sizeTV.setText(ss);
		}
		long completeSize = prefs.getLong("completeSize", 0);
		if (downloadSize != 0) {
			setProgress(completeSize);
		}
		status = prefs.getInt("status", -1);
		refreshCtrlBtn();
		versionTV.setText(prefs.getString("versionName", ""));
		updateDate.setText(prefs.getString("updateDate", ""));
		descTV.setText(prefs.getString("desc", ""));
		pics = prefs.getString("pics", "").split(" ");
		for (String pic : pics) {
			ImageView thumbnail = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = Util.dip2px(this, THUMBNAIL_MARGIN);
			params.rightMargin = Util.dip2px(this, THUMBNAIL_MARGIN);
			thumbnail.setLayoutParams(params);
			ImageLoader.getInstance().displayImage(pic, thumbnail);
			thumbnails.addView(thumbnail);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadService.ACTION_DOWNLOAD_STATUS_CHANGED);
		filter.addAction(DownloadService.ACTION_DOWNLOAD_UPDATE_PROGRESS);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		unregisterReceiver(receiver);
	}

	private void stopDownload() {
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_STOP);
		startService(intent);
		// 删除apk
		String apk = prefs.getString("apk", null);
		if (apk != null) {
			File file = new File(Const.DOWNLOAD_DIR + File.separator + apk);
			file.delete();
		}
		Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}

	private void pauseDownload() {
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_PAUSE);
		startService(intent);
	}

	private void resumeDownload() {
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_RESUME);
		intent.putExtra("isPatch", isPatch);
		startService(intent);
	}

	private void openApk() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(
				Uri.fromFile(new File(Const.DOWNLOAD_DIR + File.separator
						+ prefs.getString("apk", ""))),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	private void refreshCtrlBtn() {
		switch (status) {
		case -1:
			ctrlBtn.setEnabled(false);
			cancelBtn.setEnabled(false);
			break;
		case DownloadService.DOWNLOAD_STATUS_PAUSED:
		case DownloadService.DOWNLOAD_STATUS_FAILED:
			ctrlBtn.setEnabled(true);
			ctrlBtn.setImageResource(R.drawable.sw_start_n);
			break;
		case DownloadService.DOWNLOAD_STATUS_START:
		case DownloadService.DOWNLOAD_STATUS_RUNNING:
			ctrlBtn.setEnabled(true);
			ctrlBtn.setImageResource(R.drawable.sw_pause_n);
			break;
		case DownloadService.DOWNLOAD_STATUS_FINISHED:
			ctrlBtn.setEnabled(true);
			ctrlBtn.setImageResource(R.drawable.sw_open_n);
			break;
		default:
			ctrlBtn.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.download_cancel_btn:
			new AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(R.string.delete_message)
					.setPositiveButton(R.string.delete,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									stopDownload();
									finish();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
			break;
		case R.id.download_ctrl_btn:
			ctrlBtn.setEnabled(false);
			if (status == DownloadService.DOWNLOAD_STATUS_RUNNING
					|| status == DownloadService.DOWNLOAD_STATUS_START) {
				pauseDownload();
			} else if (status == DownloadService.DOWNLOAD_STATUS_PAUSED) {
				resumeDownload();
			} else if (status == DownloadService.DOWNLOAD_STATUS_FINISHED) {
				openApk();
			}
			break;
		}
	}

	private void setProgress(long completeSize) {
		progress.setProgress((int) (completeSize * 100.0 / downloadSize));
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DownloadService.ACTION_DOWNLOAD_UPDATE_PROGRESS.equals(action)) {
				long completeSize = intent.getLongExtra("completeSize", 0);
				setProgress(completeSize);
			} else if (DownloadService.ACTION_DOWNLOAD_STATUS_CHANGED
					.equals(action)) {
				status = intent.getIntExtra("status", -1);
				refreshCtrlBtn();
				// switch (status) {
				// case DownloadService.DOWNLOAD_STATUS_START:
				// downloadSize = intent.getLongExtra("size", 0);
				// sizeTV.setText(Util.formatFileSize(downloadSize));
				// break;
				// }
			}
		}
	};

}
