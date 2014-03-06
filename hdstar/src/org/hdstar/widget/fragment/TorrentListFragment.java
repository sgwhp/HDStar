package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.model.PTSiteSetting;
import org.hdstar.model.Torrent;
import org.hdstar.ptadapter.PTAdapter;
import org.hdstar.ptadapter.PTFactory;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.adapter.TorrentAdapter;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener2;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TorrentListFragment extends StackFragment {
	private PTSiteSetting setting;
	private PTAdapter ptAdapter;
	private View view;
	private PullToRefreshLayout mPullToRefreshLayout;
	private ExpandableListView listView;
	private Parcelable listViewState;
	private TorrentAdapter adapter;
	private List<Torrent> torrents = new ArrayList<Torrent>();
	private int page = 0;
	private int curPage = 0;
	private String keywords = "";

	public static TorrentListFragment newInstance(PTSiteSetting setting) {
		TorrentListFragment f = new TorrentListFragment();
		Bundle args = new Bundle();
		args.putParcelable("setting", setting);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setting = getArguments().getParcelable("setting");
		ptAdapter = PTFactory.newInstanceByName(setting.type);
		ptAdapter.setCookie(setting.cookie);
		ptAdapter.setTorrentsUrl(setting.torrentUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.torrent_list_layout, null);
		mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		listView = (ExpandableListView) view.findViewById(R.id.torrent_list);
		return view;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (adapter == null) {
			adapter = new TorrentAdapter(getActivity(), ptAdapter, torrents);
		}
		init();
		if (torrents.size() == 0) {
			mPullToRefreshLayout.post(new Runnable() {

				@Override
				public void run() {
					refresh();
				}
			});
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
	public void onCreateActionBar(Menu menu) {
		final SearchView search = new SearchView(
				((SherlockFragmentActivity) getActivity())
						.getSupportActionBar().getThemedContext());
		search.setSubmitButtonEnabled(true);
		search.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				curPage = 0;
				keywords = query;
				if (!"".equals(keywords)) {
					refresh();
					return true;
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				keywords = newText;
				return false;
			}
		});
		menu.add(0, Menu.FIRST, 0, android.R.string.search_go)
				.setIcon(android.R.drawable.ic_search_category_default)
				.setActionView(search)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}

	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		mPullToRefreshLayout.setRefreshing(true);
		fetch();
	}

	void init() {
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
		}

		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		// transformer.setFromEndLabel(getString(R.string.pull_to_add_next_page),
		// getString(R.string.release_to_add_next_page));
		ActionBarPullToRefresh
				.from(getActivity())
				.options(
						Options.create().refreshOnUp(true).mode(Mode.BOTH)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(R.id.torrent_list)
				.listener(new OnRefreshListener2() {

					@Override
					public void onRefreshStartedFromStart(View view) {
						doRefresh();
					}

					@Override
					public void onRefreshStartedFromEnd(View view) {
						loadNextPage();
					}
				}).setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				mPullToRefreshLayout.setRefreshComplete();
				detachTask();
			}
		});
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		BaseAsyncTask<ArrayList<Torrent>> task = ptAdapter.getTorrents(page,
				keywords.trim());
		task.attach(refreshCallback);
		attachTask(task);
		BaseAsyncTask.commit(task);
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	public void loadNextPage() {
		BaseAsyncTask<ArrayList<Torrent>> task = ptAdapter.getTorrents(
				curPage + 1, keywords.trim());
		task.attach(addCallback);
		attachTask(task);
		BaseAsyncTask.commit(task);
	}

	TaskCallback<ArrayList<Torrent>> refreshCallback = new TaskCallback<ArrayList<Torrent>>() {

		@Override
		public void onComplete(ArrayList<Torrent> list) {
			curPage = page;
			mPullToRefreshLayout.setRefreshComplete();
			torrents.clear();
			torrents.addAll(list);
			adapter.notifyDataSetChanged();
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			mPullToRefreshLayout.setRefreshComplete();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			mPullToRefreshLayout.setRefreshComplete();
		}
	};

	TaskCallback<ArrayList<Torrent>> addCallback = new TaskCallback<ArrayList<Torrent>>() {

		@Override
		public void onComplete(ArrayList<Torrent> list) {
			curPage++;
			mPullToRefreshLayout.setRefreshComplete();
			if (list.size() > 0) {
				torrents.addAll((ArrayList<Torrent>) list);
				adapter.notifyDataSetChanged();
			} else {
				Crouton.makeText(getActivity(), R.string.no_more_data,
						Style.CONFIRM).show();
			}
		}

		@Override
		public void onFail(Integer msgId) {
			mPullToRefreshLayout.setRefreshComplete();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			mPullToRefreshLayout.setRefreshComplete();
		}

	};
}
