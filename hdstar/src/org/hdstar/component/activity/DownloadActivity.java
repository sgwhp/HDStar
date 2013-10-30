package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.DownloadService;
import org.hdstar.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class DownloadActivity extends SherlockActivity implements OnClickListener {
	private ProgressBar progress;
	private ImageButton ctrlBtn;
	private ImageButton cancelBtn;
	private TextView sizeTV;
	private long size;
	private int status;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		progress = (ProgressBar) findViewById(R.id.download_progress);
		ctrlBtn = (ImageButton) findViewById(R.id.download_ctrl_btn);
		cancelBtn = (ImageButton) findViewById(R.id.download_cancel_btn);
		sizeTV = (TextView) findViewById(R.id.app_size);
		SharedPreferences shared = this.getSharedPreferences(Const.DOWNLOAD_SHARED_PREFS, MODE_PRIVATE);
		size = shared.getLong("size", 0);
		long completeSize = shared.getLong("completeSize", 0);
		if(size != 0){
			setProgress(completeSize);
		}
		status = shared.getInt("status", -1);
		refreshCtrlBtn();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadService.ACTION_DOWNLOAD_STATUS_CHANGED);
		filter.addAction(DownloadService.ACTION_DOWNLOAD_UPDATE_PROGRESS);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	private void stopDownload(){
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_STOP);
		startService(intent);
	}
	
	private void pauseDownload(){
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_PAUSE);
		startService(intent);
	}
	
	private void resumeDownload(){
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("command", DownloadService.COMMAND_DOWNLOAD_RESUME);
		startService(intent);
	}
	
	private void refreshCtrlBtn(){
		switch(status){
		case DownloadService.DOWNLOAD_STATUS_PAUSED:
		case DownloadService.DOWNLOAD_STATUS_FAILED:
			ctrlBtn.setEnabled(true);
			ctrlBtn.setImageResource(R.drawable.sw_start_n);
			break;
		case DownloadService.DOWNLOAD_STATUS_RUNNING:
			ctrlBtn.setEnabled(true);
			ctrlBtn.setImageResource(R.drawable.sw_pause_n);
			break;
		default:
			ctrlBtn.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.download_cancel_btn:
			stopDownload();
			break;
		case R.id.download_ctrl_btn:
			ctrlBtn.setEnabled(false);
			if(status == DownloadService.DOWNLOAD_STATUS_RUNNING){
				pauseDownload();
			} else if(status == DownloadService.DOWNLOAD_STATUS_PAUSED){
				resumeDownload();
			}
			break;
		}
	}
	
	private void setProgress(long completeSize){
		progress.setProgress((int) (completeSize * 100.0 / size));
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
				switch (status) {
				case DownloadService.DOWNLOAD_STATUS_START:
					size = intent.getLongExtra("size", 0);
					sizeTV.setText(Util.formatFileSize(size));
					break;
				}
			}
		}
	};

}
