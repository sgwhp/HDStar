package org.hdstar.component.activity;

import org.hdstar.R;

import android.os.Bundle;

public class TorrentActivity extends BaseActivity {

	public TorrentActivity() {
		super(R.string.torrent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
	}

}
