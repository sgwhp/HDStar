package org.hdstar.widget;

import java.util.ArrayList;

import org.hdstar.widget.fragment.StackFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;

public class StackPagerAdapter extends FragmentPagerAdapter {
	private FragmentManager fm;
	public ArrayList<StackFragment<?>> fragments = new ArrayList<StackFragment<?>>();
	// private FragmentTransaction mCurTransaction = null;
	// private Fragment mCurrentPrimaryItem = null;
	// private static final String TAG = "FragmentPagerAdapter";
	// private static final boolean DEBUG = false;
	private int curPosition = 0;

	public StackPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments == null ? 0 : fragments.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return PagerAdapter.POSITION_NONE;
	}

	// @Override
	// public Object instantiateItem(ViewGroup container, int position) {
	// if(mCurTransaction == null){
	// mCurTransaction = fm.beginTransaction();
	// }
	// final long itemId = getItemId(position);
	// String name = makeFragmentName(container.getId(), itemId);
	// Fragment fragment = fm.findFragmentByTag(name);
	// if (fragment != null) {
	// if (DEBUG) Log.v(TAG, "Attaching item #" + itemId + ": f=" + fragment);
	// mCurTransaction.attach(fragment);
	// } else {
	// fragment = getItem(position);
	// if (DEBUG) Log.v(TAG, "Adding item #" + itemId + ": f=" + fragment);
	// mCurTransaction.add(container.getId(), fragment,
	// makeFragmentName(container.getId(), itemId));
	// }
	// if (fragment != mCurrentPrimaryItem) {
	// fragment.setMenuVisibility(false);
	// fragment.setUserVisibleHint(false);
	// }
	// return fragment;
	// }
	//
	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// if (mCurTransaction == null) {
	// mCurTransaction = fm.beginTransaction();
	// }
	// if (DEBUG) Log.v(TAG, "Detaching item #" + getItemId(position) + ": f=" +
	// object
	// + " v=" + ((Fragment)object).getView());
	// mCurTransaction.detach((Fragment)object);
	// }
	//
	// @Override
	// public void setPrimaryItem(ViewGroup container, int position, Object
	// object) {
	// Fragment fragment = (Fragment)object;
	// if (fragment != mCurrentPrimaryItem) {
	// if (mCurrentPrimaryItem != null) {
	// mCurrentPrimaryItem.setMenuVisibility(false);
	// mCurrentPrimaryItem.setUserVisibleHint(false);
	// }
	// if (fragment != null) {
	// fragment.setMenuVisibility(true);
	// fragment.setUserVisibleHint(true);
	// }
	// mCurrentPrimaryItem = fragment;
	// }
	// }
	//
	// @Override
	// public void finishUpdate(ViewGroup container) {
	// if (mCurTransaction != null) {
	// mCurTransaction.commitAllowingStateLoss();
	// mCurTransaction = null;
	// fm.executePendingTransactions();
	// }
	// }
	//
	// @Override
	// public boolean isViewFromObject(View view, Object object) {
	// return ((Fragment)object).getView() == view;
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return position;
	// }
	//
	// private static String makeFragmentName(int viewId, long id) {
	// return "android:switcher:" + viewId + ":" + id;
	// }

	public void forceBack(int pages) {
		fragments.get(curPosition).abort();
		curPosition -= pages;
	}

	public void back(int pages) {
		curPosition -= pages;
	}

	public void up(int pages) {
		curPosition += pages;
	}

	public void forward(StackFragment<?> f) {
		FragmentTransaction t = fm.beginTransaction();
		for (int i = fragments.size() - 1; i > curPosition; i--) {
			t.remove(fragments.remove(i));
		}
		fragments.add(f);
		t.commit();
		notifyDataSetChanged();
	}

	public void add(StackFragment<?> f) {
		fragments.add(f);
	}

	public void clear() {
		FragmentTransaction t = fm.beginTransaction();
		for (StackFragment<?> f : fragments) {
			// f.abort();
			t.remove(f);
		}
		curPosition = 0;
		fragments.clear();
		t.commit();
		notifyDataSetChanged();
	}

}
