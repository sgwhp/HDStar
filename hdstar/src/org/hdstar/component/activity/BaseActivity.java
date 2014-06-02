package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.util.Util;
import org.hdstar.widget.fragment.HexagonMenuFragment;
import org.hdstar.widget.fragment.MenuListFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;

import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	private String mTitle;
	protected Fragment mFrag;

	public BaseActivity() {
	}

	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	public BaseActivity(String title) {
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
//		mFrag = new MenuListFragment();
        mFrag = new HexagonMenuFragment();
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Util.showExitDialog(this);
	}
}
