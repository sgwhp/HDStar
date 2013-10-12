package org.hdstar.component.activity;

import org.hdstar.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.slidingmenu.lib.SlidingMenu;

@SuppressLint("ValidFragment")
public class HelpActivity extends BaseActivity {
	ViewPager vp;

	public HelpActivity() {
		super(R.string.help);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		setContentView(R.layout.test);
	}
}
