package org.hdstar.component.activity;

import org.hdstar.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

@SuppressLint("ValidFragment")
public class HelpActivity extends BaseActivity {
	ViewPager vp;

	public HelpActivity() {
		super(R.string.help);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
	}
}
