package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Topic;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.PageAdapter;
import org.hdstar.widget.TopicsAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ForumFragment extends StackFragment {
	private View view;
	private PullToRefreshListView refreshView;
	private ListView listView;
	private Parcelable listViewState;
	// private int index;
	// private int top;
	private PopupWindow window = null;
	private TopicsAdapter adapter = null;
	private int forumId = 1;
	private int page = 1;
	private boolean refresh = false;

	public static ForumFragment newInstance(String url, int id) {
		ForumFragment fragment = new ForumFragment();
		Bundle args = new Bundle();
		args.putString("url", url);
		args.putInt("id", id);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		url = bundle.getString("url");
		forumId = bundle.getInt("id", 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.forum_view, null);
		return view;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		if (adapter == null) {
			adapter = new TopicsAdapter(getActivity(), new ArrayList<Topic>());
		}
		init();
		if (adapter.getList() == null || adapter.getList().size() == 0) {
			refreshView.setRefreshing(false);
			// fetch();
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
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		MenuItem item = menu.add(0, ForumsActivity.COMMIT_ACTION_BAR_ID, 0,
				R.string.new_topic);
		item.setIcon(R.drawable.reply);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public void onActionBarClick(int MenuItemId) {
		if (getActivity().findViewById(android.R.id.list) != null
				&& adapter.getList() != null && !url.equals("")) {
			getStackAdapter().forward(NewTopicFragment.newInstance(forumId));
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() + 1,
					true);
		}
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
		final float dip = this.getResources().getDisplayMetrics().density;
		final Activity act = getActivity();
		final View footerView = LayoutInflater.from(act).inflate(
				R.layout.footer_view, null);
		refreshView = (PullToRefreshListView) view
				.findViewById(R.id.pull_refresh_list);
		listView = refreshView.getRefreshableView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Topic t = (Topic) listView.getItemAtPosition(position);
				if (t.pageList == null) {
					viewTopic(t.topicId, page, t.title);
				} else {
					View v = LayoutInflater.from(act).inflate(
							R.layout.popupwindow, null);
					ListView lv = (ListView) v.findViewById(R.id.lv);
					lv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							viewTopic(t.topicId, t.pageList.get(arg2), t.title);
							window.dismiss();
						}

					});
					lv.setAdapter(new PageAdapter(act, t.pageIndex));
					window = new PopupWindow(v, (int) (80 * dip + 0.5),
							(int) (150 * dip + 0.5));
					window.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.rounded_corners_pop));
					window.setFocusable(true);
					window.setAnimationStyle(R.style.normalPopWindow_anim_style);
					window.update();
					window.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
				}
			}

		});
		if (adapter.getCount() != 0) {
			listView.addFooterView(footerView);
			footerView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doNextPageClick(footerView);
				}
			});
		}
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
			// listView.setSelectionFromTop(index, top);
		}
		// listView.setOnRefreshListener(new OnRefreshListener() {
		//
		// @Override
		// public void onRefresh() {
		// refresh();
		// }
		// });
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

	void viewTopic(int topicId, int page, String title) {
		push(TopicFragment.newInstance(topicId, page, title));
	}

	/**
	 * 通过AsyncTask获取数据
	 * */
	void fetch() {
		if (mTask != null) {
			return;
		}
		listView.setSelection(0);
		// listView.prepareForRefresh();
		DelegateTask<List<Topic>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(refreshCallback);
		attachTask(task);
		task.execGet(Const.Urls.SERVER_VIEW_FORUM_URL + "?forumId=" + forumId,
				new TypeToken<ResponseWrapper<List<Topic>>>() {
				}.getType());
	}

	/**
	 * 更新ListView
	 * 
	 * @param list
	 *            新的Topic列表
	 * */
	public void updateView(List<Topic> list) {
		adapter.setList(list);
		adapter.notifyDataSetChanged();
		// listView.onRefreshComplete();
		final Activity act = getActivity();
		final View footerView = LayoutInflater.from(act).inflate(
				R.layout.footer_view, null);
		if (adapter.getCount() != 0) {
			listView.addFooterView(footerView);
			footerView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doNextPageClick(footerView);
				}
			});
		}
		listView.setSelection(1);
	}

	/** 加载下一页 */
	public void addNextPage(List<Topic> list) {
		adapter.itemsAddAll(list);
		adapter.notifyDataSetChanged();
		// listView.setSelection(listView.mLastItem - listView.mVisibleItemCount
		// + 1);
		// findViewById(R.id.next_page).setVisibility(View.VISIBLE);
		((TextView) view.findViewById(R.id.loading_next_page_text))
				.setText(R.string.next_page);
		view.findViewById(R.id.loading_next_page_progressBar).setVisibility(
				View.GONE);
	}

	public void doNextPageClick(final View view) {
		refresh = false;
		((TextView) view.findViewById(R.id.loading_next_page_text))
				.setText(R.string.loading);
		view.findViewById(R.id.loading_next_page_progressBar).setVisibility(
				View.VISIBLE);
		// mTask.detach();
		DelegateTask<List<Topic>> task = new DelegateTask<List<Topic>>(
				HDStarApp.cookies);
		task.attach(addCallback);
		attachTask(task);
		task.execGet(Const.Urls.SERVER_VIEW_FORUM_URL + "?forumId=" + forumId
				+ "&page=" + page,
				new TypeToken<ResponseWrapper<List<Topic>>>() {
				}.getType());
		page++;
	}

	private void doRefresh() {
		refresh = true;
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	TaskCallback<List<Topic>> refreshCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			// mTask.detach();
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
			if (refresh) {
				adapter.clearItems();
			}
			adapter.itemsAddAll(list);
			if (adapter.getCount() != 0) {
				final Activity act = getActivity();
				final View footerView = LayoutInflater.from(act).inflate(
						R.layout.footer_view, null);
				if (listView.getFooterViewsCount() == 0) {
					listView.addFooterView(footerView);
					footerView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							doNextPageClick(footerView);
						}
					});
				}
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			// mTask.detach();
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
			// listView.setSelection(1);
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			// mTask.detach();
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
		}
	};

	TaskCallback<List<Topic>> addCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			// mTask.detach();
			refreshView.onRefreshComplete();
			adapter.itemsAddAll((ArrayList<Topic>) list);
			adapter.notifyDataSetChanged();
			((TextView) view.findViewById(R.id.loading_next_page_text))
					.setText(R.string.next_page);
			view.findViewById(R.id.loading_next_page_progressBar)
					.setVisibility(View.GONE);
		}

		@Override
		public void onFail(Integer msgId) {
			// listView.onRefreshComplete();
			// mTask.detach();
			refreshView.onRefreshComplete();
			((TextView) view.findViewById(R.id.loading_next_page_text))
					.setText(R.string.next_page);
			view.findViewById(R.id.loading_next_page_progressBar)
					.setVisibility(View.GONE);
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			// mTask.detach();
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
		}

	};

}
