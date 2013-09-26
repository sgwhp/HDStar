package org.hdstar.widget.fragment;

import org.hdstar.task.MyAsyncTask;
import org.hdstar.widget.StackHook;
import org.hdstar.widget.StackPagerAdapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class StackFragment<T> extends Fragment {
	protected String url;
	protected MyAsyncTask<T> task;

	// protected StackPagerAdapter adapter;
	// protected ViewPager vp;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// adapter = ((StackHook)activity).getStackAdapter();
		// vp = ((StackHook)activity).getViewPager();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.v("whp", "detach");
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

	protected StackPagerAdapter getStackAdapter() {
		return ((StackHook) getActivity()).getStackAdapter();
		// return adapter;
	}

	protected ViewPager getViewPager() {
		return ((StackHook) getActivity()).getViewPager();
		// return vp;
	}

	public void abort() {
		if (task != null) {
			task.abort();
			task = null;
		}
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View v = inflater.inflate(R.layout.test, null);
	// Button btn = (Button)v.findViewById(R.id.button);
	// btn.setOnClickListener(new OnClickListener(){
	//
	// @Override
	// public void onClick(View v) {
	// mAdapter.forward(new StackFragment(mAdapter, vp));
	// mAdapter.notifyDataSetChanged();
	// vp.setCurrentItem(vp.getCurrentItem()+1, true);
	// }
	//
	// });
	// return v;//return super.onCreateView(inflater, container,
	// savedInstanceState);
	// }

}
