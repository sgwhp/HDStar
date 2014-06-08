package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Topic;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;
import org.hdstar.widget.adapter.PageAdapter;
import org.hdstar.widget.adapter.TopicsAdapter;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener2;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 论坛<br/>
 * @author  robust
 */
public class ForumFragment extends StackFragment {
    private static final int POP_UP_WINDOW_WIDTH = 100;//页码窗口宽度(dp)
    private static final int POP_UP_WINDOW_HEIGHT = 180;//页码窗口高度(dp)
	private View view;
	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView listView;
	private Parcelable listViewState;
	// private int index;
	// private int top;
	private PopupWindow window = null;
	private TopicsAdapter adapter = null;
	private int forumId = 1;
	private int page = 0;
	private int curPage = 0;
    private int windowWidth;//页码窗口宽度(px)
    private int windowHeight;//页码窗口高度(px)

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
		// setHasOptionsMenu(true);
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
		// 首次打开，需要获取数据
		boolean init = false;
		if (adapter == null) {
			init = true;
			adapter = new TopicsAdapter(getActivity(), new ArrayList<Topic>());
		}
        windowWidth = Util.dip2px(getActivity(), POP_UP_WINDOW_WIDTH);
        windowHeight = Util.dip2px(getActivity(), POP_UP_WINDOW_HEIGHT);
		init();
		if (init) {
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
	public void onDetach() {
		super.onDetach();
		if (mPullToRefreshLayout != null)
			mPullToRefreshLayout.setRefreshComplete();
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
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		MenuItem item = menu.add(0, R.id.ab_new_topic, 0, R.string.new_topic);
		item.setIcon(R.drawable.reply);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public boolean onActionBarSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ab_new_topic:
			if (getActivity().findViewById(android.R.id.list) != null
					&& adapter.getList() != null && !url.equals("")) {
				push(NewTopicFragment.newInstance(forumId));
			}
			return true;
		}
		return false;
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
	// .setSubtitle(null);
	// MenuItem item = menu.add(0, R.id.ab_new_topic, 0, R.string.new_topic);
	// item.setIcon(R.drawable.reply);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
	// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.ab_new_topic:
	// if (getActivity().findViewById(android.R.id.list) != null
	// && adapter.getList() != null && !url.equals("")) {
	// push(NewTopicFragment.newInstance(forumId));
	// }
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		mPullToRefreshLayout.setRefreshing(true);
		doRefresh();
	}

	protected void init() {
		final Activity act = getActivity();
		mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		transformer.setFromEndLabel(getString(R.string.pull_to_add_next_page),
				getString(R.string.release_to_add_next_page));
		ActionBarPullToRefresh
				.from(act)
				.options(
						Options.create().refreshOnUp(true).mode(Mode.BOTH)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(R.id.topic_list)
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
		listView = (ListView) view.findViewById(R.id.topic_list);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Topic t = (Topic) listView.getItemAtPosition(position);
				if (t.pageList == null) {
					viewTopic(t.topicId, 0, t.title);
				} else {
                    //弹出页码列表
					View v = LayoutInflater.from(act).inflate(
							R.layout.popupwindow, null);
					ListView lv = (ListView) v.findViewById(R.id.lv);
					lv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							viewTopic(t.topicId, position, t.title);
							window.dismiss();
						}

					});
					lv.setAdapter(new PageAdapter(act, t.pageList
							.get(t.pageList.size() - 1) + 1));

                    window = new PopupWindow(v, windowWidth, windowHeight, true);
					window.setAnimationStyle(R.style.normalPopWindow_anim_style);
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    Rect anchorRect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1]
                            + view.getHeight());

                    Point screen = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getSize(screen);

                    int xPos = (screen.x - windowWidth) / 2;
                    int yPos	= anchorRect.top - windowHeight;

                    // display on bottom
                    if (windowHeight <= screen.y - location[1]) {
                        yPos = anchorRect.bottom;
                        window.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.popover_background_down));
                    } else {
                        window.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.popover_background_up));
                    }
                    window.update();
					window.showAtLocation(parent, Gravity.NO_GRAVITY, xPos, yPos);
				}
			}

		});
        AnimationAdapter animAdapter = new SwingLeftInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
		listView.setAdapter(animAdapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
			// listView.setSelectionFromTop(index, top);
		}
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
		// listView.prepareForRefresh();
		DelegateTask<List<Topic>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(refreshCallback);
		attachTask(task);
		task.execGet(CommonUrls.HDStar.SERVER_VIEW_FORUM_URL + "?forumId="
				+ forumId, new TypeToken<ResponseWrapper<List<Topic>>>() {
		}.getType());
	}

    /**
     * 加载下一页
     */
	public void loadNextPage() {
		DelegateTask<List<Topic>> task = new DelegateTask<List<Topic>>(
				HDStarApp.cookies);
		task.attach(addCallback);
		attachTask(task);
		task.execGet(CommonUrls.HDStar.SERVER_VIEW_FORUM_URL + "?forumId="
				+ forumId + "&page=" + (curPage + 1),
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

    /**
     * 刷新回调
     */
	TaskCallback<List<Topic>> refreshCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			curPage = page;
			mPullToRefreshLayout.setRefreshComplete();
			adapter.clearItems();
			adapter.itemsAddAll(list);
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

    /**
     * 获取下一页回调
     */
	TaskCallback<List<Topic>> addCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			curPage++;
			mPullToRefreshLayout.setRefreshComplete();
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
			mPullToRefreshLayout.setRefreshComplete();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			mPullToRefreshLayout.setRefreshComplete();
		}

	};

}
