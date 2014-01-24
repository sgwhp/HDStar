package org.hdstar.widget.fragment;

import org.hdstar.task.BaseAsyncTask;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.adapter.StackPagerAdapter;

import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragment;

public class StackFragment extends SherlockFragment {
	protected String url;
	protected BaseAsyncTask<?> mTask;

	@Override
	public void onDetach() {
		super.onDetach();
		detachTask();
	}

	protected void attachTask(BaseAsyncTask<?> task) {
		if (mTask != null) {
			mTask.detach();
		}
		mTask = task;
	}

	protected void detachTask() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
	}

	/**
	 * viewpager 选中当前fragment
	 */
	public void onSelected() {
	}

	public void refresh() {
	}

	public void push(StackFragment f) {
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
