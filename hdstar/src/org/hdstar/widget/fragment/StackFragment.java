package org.hdstar.widget.fragment;

import org.hdstar.task.BaseAsyncTask;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.adapter.StackPagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * 
 * @author robust
 */
public class StackFragment extends Fragment {
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
	 * viewpager ѡ�е�ǰfragment
	 */
	public void onSelected() {
	}

	public void refresh() {
	}

	/**
	 * ��ʼ��actionbar <br/>
	 * ����onCreateOptionsMenu
	 * 
	 * @see https://github.com/jakewharton/actionbarsherlock/issues/272
	 */
	public void onCreateActionBar(Menu menu) {
	}

	/**
	 * actionbar��ѡ�� <br/>
	 * ����onOptionsItemSelected
	 * 
	 * @see https://github.com/jakewharton/actionbarsherlock/issues/272
	 */
	public boolean onActionBarSelected(MenuItem item) {
		return false;
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
