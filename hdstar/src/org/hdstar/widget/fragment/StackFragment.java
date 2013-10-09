package org.hdstar.widget.fragment;

import org.hdstar.task.MyAsyncTask;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.StackPagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class StackFragment extends Fragment {
	protected String url;
	protected MyAsyncTask<?> mTask;

	@Override
	public void onDetach() {
		super.onDetach();
		detachTask();
	}

	protected void attachTask(MyAsyncTask<?> task) {
		if (mTask != null) {
			Log.v("whp", "stackfragment attachtask");
			mTask.detach();
		}
		mTask = task;
	}

	protected void detachTask() {
		if (mTask != null) {
			Log.v("whp", "stackfragment detachtask");
			mTask.detach();
			mTask = null;
		}
	}

	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
	}

	public void onActionBarClick(int menuItemId) {
	}

	/**
	 * viewpager 选中当前fragment
	 */
	public void onSelected() {
	}

	protected void push(StackFragment f) {
		getStackAdapter().forward(f);
		ViewPager vp = getViewPager();
		vp.setCurrentItem(vp.getCurrentItem() + 1, true);
	}

	protected StackPagerAdapter getStackAdapter() {
		return ((StackHook) getActivity()).getStackAdapter();
	}

	protected ViewPager getViewPager() {
		return ((StackHook) getActivity()).getViewPager();
	}

	public void abort() {
		if (mTask != null) {
			mTask.abort();
			mTask = null;
		}
	}
}
