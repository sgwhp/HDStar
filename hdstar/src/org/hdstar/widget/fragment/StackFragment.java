package org.hdstar.widget.fragment;

import org.hdstar.task.MyAsyncTask;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.StackPagerAdapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class StackFragment<T> extends Fragment {
	protected String url;
	protected MyAsyncTask<T> task;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		detachTask();
	}

	protected void detachTask() {
		if (task != null) {
			task.detach();
			task = null;
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
	public void onSelected(){
		
	}
	
	protected void push(StackFragment<?> f){
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
		if (task != null) {
			task.abort();
			task = null;
		}
	}
}
