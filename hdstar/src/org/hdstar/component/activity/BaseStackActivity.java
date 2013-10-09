package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.StackPagerAdapter;
import org.hdstar.widget.fragment.MenuListFragment;
import org.hdstar.widget.fragment.StackFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class BaseStackActivity extends SlidingFragmentActivity implements
		StackHook {
	private int mTitleRes;
	protected ListFragment mFrag;
	public static int newMessageNum = 0;
	protected StackPagerAdapter stackAdapter;
	protected int curPage = 0;
	protected ViewPager viewPager;

	protected BaseStackActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(mTitleRes);
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
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		int selectedPage = 0;
		stackAdapter = new StackPagerAdapter(getSupportFragmentManager());
		if (savedInstanceState != null) {
			int count = savedInstanceState.getInt("pageCount");
			selectedPage = savedInstanceState.getInt("selectedPage");
			Fragment f;
			for (int i = 0; i < count; i++) {
				if ((f = getFragment(i)) == null) {
					break;
				}
				stackAdapter.add((StackFragment) f);
			}
		}
		viewPager.setAdapter(stackAdapter);
		viewPager.setCurrentItem(selectedPage);

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
				invalidateOptionsMenu();
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
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
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
		stackAdapter.fragments.get(curPage).onActionBarClick(item.getItemId());
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (curPage < stackAdapter.fragments.size()) {
			stackAdapter.fragments.get(curPage).initActionBar(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		if (curPage != 0) {
			viewPager.setCurrentItem(curPage - 1);
		} else {
			checkExit();
		}
	}

	private void checkExit() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.confirm)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}

						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}

						}).create().show();
	}

	@Override
	public StackPagerAdapter getStackAdapter() {
		return stackAdapter;
	}

	@Override
	public ViewPager getViewPager() {
		return viewPager;
	}

	private Fragment getFragment(int position) {
		return getSupportFragmentManager().findFragmentByTag(
				getFragmentTag(position));
	}

	private String getFragmentTag(int position) {
		return "android:switcher:" + R.id.viewPager + ":" + position;
	}

}
