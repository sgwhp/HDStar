package org.hdstar.widget;

import org.hdstar.widget.adapter.StackPagerAdapter;

import android.support.v4.view.ViewPager;

public interface StackHook {

	public StackPagerAdapter getStackAdapter();
	public ViewPager getViewPager();
}
