package org.hdstar.widget.fragment;

import java.io.InputStream;
import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;
import org.hdstar.widget.RemoteTaskAdapter;

import ch.boye.httpclientandroidlib.HttpResponse;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RemoteFragment extends StackFragment {
	private PullToRefreshListView refreshView;
	private ListView listView;
	private Parcelable listViewState;
	private RemoteTaskAdapter adapter;
	private ArrayList<RemoteTaskInfo> list = new ArrayList<RemoteTaskInfo>();

	public static RemoteFragment newInstance() {
		RemoteFragment f = new RemoteFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.forum_view, null);
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		if (adapter == null) {
			adapter = new RemoteTaskAdapter(getActivity(), list);
		}
		init();
		if (adapter.getList() == null || adapter.getList().size() == 0) {
			refreshView.setRefreshing(false);
		} else {
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onDestroyView() {
		// StackPagerAdapter forward和ViewPager
		// setCurrentItem时各会触发一次onDestroyView
		// 但两次之间并未使得listView的onRestoreInstanceState立即生效，故只有第一次的状态是有效的
		if (listViewState == null) {
			// 缓存listView的状态，以便在fragment attach时恢复
			listViewState = listView.onSaveInstanceState();
		}
		// index = listView.getFirstVisiblePosition();
		// View v = listView.getChildAt(0);
		// top = v == null ? 0 : v.getTop();
		super.onDestroyView();
	}
	
	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		refreshView.setRefreshing(false);
	}
	
	protected void init() {
		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				doRefresh();
			}
		});
	}
	
	void fetch() {
		if (mTask != null) {
			return;
		}
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = new BaseAsyncTask<ArrayList<RemoteTaskInfo>>();
		task.attach(mCallback);
		attachTask(task);
		task.execGet("", new ResponseParser<ArrayList<RemoteTaskInfo>>(){

			@Override
			public ArrayList<RemoteTaskInfo> parse(HttpResponse res,
					InputStream in) {
				//
				return null;
			}});
	}
	
	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}
	
	private TaskCallback<ArrayList<RemoteTaskInfo>> mCallback = new TaskCallback<ArrayList<RemoteTaskInfo>>(){

		@Override
		public void onComplete(ArrayList<RemoteTaskInfo> result) {
			
		}

		@Override
		public void onCancel() {
			
		}

		@Override
		public void onFail(Integer msgId) {
			
		}
	};

}
