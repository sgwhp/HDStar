package org.hdstar.component.activity;

import org.hdstar.R;

import android.os.Bundle;

public class RemoteActivity extends BaseActivity {
	public RemoteActivity() {
		super(R.string.remote);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
	}
}
