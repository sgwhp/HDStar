package org.hdstar.component.activity;

import org.hdstar.R;

import android.annotation.SuppressLint;
import android.os.Bundle;

@SuppressLint("ValidFragment")
public class HelpActivity extends BaseActivity {

	public HelpActivity() {
		super(R.string.help);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
	}
}
