package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.util.Util;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.adapter.StackPagerAdapter;
import org.hdstar.widget.fragment.MenuListFragment;
import org.hdstar.widget.fragment.StackFragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.view.MenuItem;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.slidingmenu.lib.SlidingMenu;

public class BaseStackActivity extends SlidingFragmentActivity implements
		StackHook {
	private int mTitleRes;
	private String mTitle;
	protected ListFragment mFrag;
	public static int newMessageNum = 0;
	protected StackPagerAdapter stackAdapter;
	protected int curPage = 0;
	protected JazzyViewPager viewPager;
	private final int PAGER_MARGIN = 10;

	protected BaseStackActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	protected BaseStackActivity(String title) {
		mTitle = title;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mTitleRes != 0) {
			setTitle(mTitleRes);
		}
		if (mTitle != null) {
			setTitle(mTitle);
		}
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		FragmentTransaction t = this.getSupportFragmentManager()
				.beginTransaction();
		mFrag = new MenuListFragment();
		t.replace(R.id.menu_frame, mFrag);
		t.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		setSlidingActionBarEnabled(false);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_main);
		// This is a workaround for http://b.android.com/15340 from
		// http://stackoverflow.com/a/5852198/132047
		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		// BitmapDrawable bg =
		// (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped);
		// bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		// getSupportActionBar().setBackgroundDrawable(bg);
		//
		// BitmapDrawable bgSplit =
		// (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped_split_img);
		// bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		// getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
		// }
		viewPager = (JazzyViewPager) findViewById(R.id.viewPager);
		viewPager.setPageMargin(Util.dip2px(this, PAGER_MARGIN));
		viewPager.setFadeEnabled(CustomSetting.fade);
		viewPager.setTransitionEffect(CustomSetting.anim);
		stackAdapter = new StackPagerAdapter(getSupportFragmentManager(),
				viewPager);
		if (savedInstanceState != null) {
			int count = savedInstanceState.getInt("pageCount");
			curPage = savedInstanceState.getInt("selectedPage");
			Fragment f;
			for (int i = 0; i < count; i++) {
				if ((f = getFragment(i)) == null) {
					break;
				}
				stackAdapter.add((StackFragment) f);
			}
			stackAdapter.setCurPosition(curPage);
		}
		viewPager.setAdapter(stackAdapter);
		viewPager.setCurrentItem(curPage);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@SuppressLint("NewApi")
			@Override
			public void onPageSelected(int position) {
				stackAdapter.fragments.get(position).onSelected();
				if (position < curPage) {
					stackAdapter.forceBack(curPage - position);
				} else if (position > curPage) {
					stackAdapter.up(position - curPage);
				}
				curPage = position;
				// invalidateOptionsMenu();
				switch (position) {
				case 0:
					getSlidingMenu().setTouchModeAbove(
							SlidingMenu.TOUCHMODE_FULLSCREEN);
					break;
				default:
					getSlidingMenu().setTouchModeAbove(
							SlidingMenu.TOUCHMODE_MARGIN);
					break;
				}
			}

		});
		// getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(newMessageReceiver, new IntentFilter(
				Const.NEW_MESSAGE_ACTION));
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(newMessageReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("pageCount", stackAdapter.getCount());
		outState.putInt("selectedPage", viewPager.getCurrentItem());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		// stackAdapter.fragments.get(curPage).onActionBarClick(item.getItemId());
		return false;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// if (curPage < stackAdapter.fragments.size()) {
	// stackAdapter.fragments.get(curPage).initActionBar(menu);
	// }
	// return super.onCreateOptionsMenu(menu);
	// }

	@Override
	public void onBackPressed() {
		if (curPage != 0) {
			viewPager.setCurrentItem(curPage - 1);
		} else {
			Util.showExitDialog(this);
		}
	}

	@Override
	public StackPagerAdapter getStackAdapter() {
		return stackAdapter;
	}

	@Override
	public ViewPager getViewPager() {
		return viewPager;
	}

	public void refreshMenu() {
		((ArrayAdapter<?>) mFrag.getListAdapter()).notifyDataSetChanged();
	}

	private Fragment getFragment(int position) {
		return getSupportFragmentManager().findFragmentByTag(
				getFragmentTag(position));
	}

	private String getFragmentTag(int position) {
		return "android:switcher:" + R.id.viewPager + ":" + position;
	}

	private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Const.NEW_MESSAGE_ACTION.equals(intent.getAction())) {
				if (intent.getIntExtra("req", 0) == Const.NEW_MESSAGE_REQ_REFRESH) {
					refreshMenu();
				} else if (intent.getIntExtra("req", 0) == Const.NEW_MESSAGE_REQ_VIEW) {
					Intent nIntent = new Intent(BaseStackActivity.this,
							MessageActivity.class);
					startActivity(nIntent);
					finish();
				}
			}
		}

	};

}
