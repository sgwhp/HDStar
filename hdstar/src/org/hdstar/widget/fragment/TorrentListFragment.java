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
import org.hdstar.widget.TorrentAdapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnCancelListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TorrentListFragment extends StackFragment {
	private View view;
	private PullToRefreshExpandableListView refreshView;
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
		refreshView = (PullToRefreshExpandableListView) view
				.findViewById(R.id.pull_refresh_expandable_list);
		listView = refreshView.getRefreshableView();
		return view;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		if (adapter == null) {
			adapter = new TorrentAdapter(getActivity(), torrents);
		}
		init();
		if (torrents.size() == 0) {
			refreshView.setRefreshing(false);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDestroyView() {
		// StackPagerAdapter forward��ViewPager
		// setCurrentItemʱ���ᴥ��һ��onDestroyView
		// ������֮�䲢δʹ��listView��onRestoreInstanceState������Ч����ֻ�е�һ�ε�״̬����Ч��
		if (listViewState == null) {
			// ����listView��״̬���Ա���fragment attachʱ�ָ�
			listViewState = listView.onSaveInstanceState();
		}
		// index = listView.getFirstVisiblePosition();
		// View v = listView.getChildAt(0);
		// top = v == null ? 0 : v.getTop();
		super.onDestroyView();
	}

	@Override
	public void initActionBar(Menu menu) {
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
					refreshView.setRefreshing(false);
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
	public void onActionBarClick(int menuItemId) {
	}

	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		refreshView.setRefreshing(false);
	}

	void init() {
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
		}

		refreshView
				.setOnRefreshListener(new OnRefreshListener2<ExpandableListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
						String label = DateUtils.formatDateTime(getActivity(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						doRefresh();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
						doNextPageClick(refreshView);
					}

				});
		refreshView.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
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

	public void doNextPageClick(final View view) {
		// ((TextView) view.findViewById(R.id.loading_next_page_text))
		// .setText(R.string.loading);
		// view.findViewById(R.id.loading_next_page_progressBar).setVisibility(
		// View.VISIBLE);
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
			refreshView.onRefreshComplete();
			torrents.clear();
			torrents.addAll(list);
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			refreshView.onRefreshComplete();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			refreshView.onRefreshComplete();
		}
	};

	TaskCallback<List<Torrent>> addCallback = new TaskCallback<List<Torrent>>() {

		@Override
		public void onComplete(List<Torrent> list) {
			page++;
			refreshView.onRefreshComplete();
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
			refreshView.onRefreshComplete();
			// ((TextView) view.findViewById(R.id.loading_next_page_text))
			// .setText(R.string.next_page);
			// view.findViewById(R.id.loading_next_page_progressBar)
			// .setVisibility(View.GONE);
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			refreshView.onRefreshComplete();
		}

	};
}
