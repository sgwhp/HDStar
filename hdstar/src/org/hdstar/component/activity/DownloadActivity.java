package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.component.DownloadService;
import org.hdstar.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class DownloadActivity extends SherlockActivity {
	private ProgressBar progress;
	private ImageButton ctrlBtn;
	private ImageButton cancelBtn;
	private TextView sizeTV;
	private long size;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		progress = (ProgressBar) findViewById(R.id.download_progress);
		ctrlBtn = (ImageButton) findViewById(R.id.download_ctrl_btn);
		cancelBtn = (ImageButton) findViewById(R.id.download_cancel_btn);
		sizeTV = (TextView) findViewById(R.id.app_size);
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

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DownloadService.ACTION_DOWNLOAD_UPDATE_PROGRESS.equals(action)) {
				long completeSize = intent.getLongExtra("completeSize", 0);
				progress.setProgress((int) (completeSize * 100.0 / size));
			} else if (DownloadService.ACTION_DOWNLOAD_STATUS_CHANGED
					.equals(action)) {
				int status = intent.getIntExtra("status", -1);
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
