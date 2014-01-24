package org.hdstar.widget.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
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
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.google.gson.reflect.TypeToken;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TorrentListFragment extends StackFragment {
	private View view;
	private PullToRefreshLayout mPullToRefreshLayout;
	private ExpandableListView listView;
	private Parcelable listViewState;
	private TorrentAdapter adapter;
	private List<Torrent> torrents = new ArrayList<Torrent>();
	private int page = 1;
	private String keyWords = null;

	public static TorrentListFragment newInstance() {
		TorrentListFragment f = new TorrentListFragment();
		return f;
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
		setHasOptionsMenu(true);
		if (adapter == null) {
			adapter = new TorrentAdapter(getActivity(), torrents);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final SearchView search = new SearchView(
				((SherlockFragmentActivity) getActivity())
						.getSupportActionBar().getThemedContext());
		search.setSubmitButtonEnabled(true);
		search.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				keyWords = query;
				keyWords = keyWords.trim();
				if (!"".equals(keyWords)) {
					mPullToRefreshLayout.setRefreshing(false);
					return true;
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
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
		listView.setSelection(0);
		DelegateTask<List<Torrent>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(refreshCallback);
		attachTask(task);
		String url = Const.Urls.SERVER_TORRENTS_URL;
		if (keyWords != null) {
			try {
				url += "?search=" + URLEncoder.encode(keyWords, Const.CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			keyWords = null;
		}
		task.execGet(url, new TypeToken<ResponseWrapper<List<Torrent>>>() {
		}.getType());
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	public void loadNextPage() {
		DelegateTask<List<Torrent>> task = new DelegateTask<List<Torrent>>(
				HDStarApp.cookies);
		task.attach(addCallback);
		attachTask(task);
		task.execGet(Const.Urls.SERVER_TORRENTS_URL + "?page=" + page,
				new TypeToken<ResponseWrapper<List<Torrent>>>() {
				}.getType());
	}

	TaskCallback<List<Torrent>> refreshCallback = new TaskCallback<List<Torrent>>() {

		@Override
		public void onComplete(List<Torrent> list) {
			page = 1;
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

	TaskCallback<List<Torrent>> addCallback = new TaskCallback<List<Torrent>>() {

		@Override
		public void onComplete(List<Torrent> list) {
			page++;
			mPullToRefreshLayout.setRefreshComplete();
			if (list.size() > 0) {
				torrents.addAll((ArrayList<Torrent>) list);
				adapter.notifyDataSetChanged();
			} else {
				Crouton.makeText(getActivity(), R.string.no_more_data,
						Style.CONFIRM).show();
			}
			// ((TextView) view.findViewById(R.id.loading_next_page_text))
			// .setText(R.string.next_page);
			// view.findViewById(R.id.loading_next_page_progressBar)
			// .setVisibility(View.GONE);
		}

		@Override
		public void onFail(Integer msgId) {
			mPullToRefreshLayout.setRefreshComplete();
			// ((TextView) view.findViewById(R.id.loading_next_page_text))
			// .setText(R.string.next_page);
			// view.findViewById(R.id.loading_next_page_progressBar)
			// .setVisibility(View.GONE);
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			mPullToRefreshLayout.setRefreshComplete();
		}

	};
}
