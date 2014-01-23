package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Topic;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.adapter.PageAdapter;
import org.hdstar.widget.adapter.TopicsAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnCancelListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

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
	private int page = 0;
	private int curPage = 0;

	public static ForumFragment newInstance(String url) {
		Pattern pattern = Pattern.compile("forumid=([0-9]+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group(1));
			ForumFragment fragment = new ForumFragment();
			Bundle args = new Bundle();
			args.putString("url", url);
			args.putInt("id", id);
			pattern = Pattern.compile("&page=([0-9]+)");
			matcher = pattern.matcher(url);
			if (matcher.find()) {
				args.putInt("page", Integer.parseInt(matcher.group(1)));
			}
			fragment.setArguments(args);
			return fragment;
		} else {
			throw new IllegalArgumentException("url must contains forumid");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		url = bundle.getString("url");
		forumId = bundle.getInt("id", 1);
		page = bundle.getInt("page", 0);
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
			push(NewTopicFragment.newInstance(forumId));
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
		refreshView = (PullToRefreshListView) view
				.findViewById(R.id.pull_refresh_list);
		listView = refreshView.getRefreshableView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Topic t = (Topic) listView.getItemAtPosition(position);
				if (t.pageList == null) {
					viewTopic(t.topicId, 0, t.title);
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
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
			// listView.setSelectionFromTop(index, top);
		}
		refreshView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				doRefresh();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				loadNextPage();
			}

		});

		refreshView.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				detachTask();
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

	public void loadNextPage() {
		DelegateTask<List<Topic>> task = new DelegateTask<List<Topic>>(
				HDStarApp.cookies);
		task.attach(addCallback);
		attachTask(task);
		task.execGet(Const.Urls.SERVER_VIEW_FORUM_URL + "?forumId=" + forumId
				+ "&page=" + ++curPage,
				new TypeToken<ResponseWrapper<List<Topic>>>() {
				}.getType());
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	TaskCallback<List<Topic>> refreshCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			curPage = page;
			refreshView.onRefreshComplete();
			adapter.clearItems();
			adapter.itemsAddAll(list);
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			refreshView.onRefreshComplete();
			// listView.setSelection(1);
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			refreshView.onRefreshComplete();
		}
	};

	TaskCallback<List<Topic>> addCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			refreshView.onRefreshComplete();
			if (list.size() > 0) {
				adapter.itemsAddAll((ArrayList<Topic>) list);
				adapter.notifyDataSetChanged();
			} else {
				Crouton.makeText(getActivity(), R.string.no_more_data,
						Style.CONFIRM).show();
			}
		}

		@Override
		public void onFail(Integer msgId) {
			--curPage;
			refreshView.onRefreshComplete();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			--curPage;
			refreshView.onRefreshComplete();
		}

	};

}
