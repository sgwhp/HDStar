package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.model.Topic;
import org.hdstar.task.MyAsyncTask.TaskCallback;
import org.hdstar.task.ViewForumsTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.PageAdapter;
import org.hdstar.widget.PullToRefreshListView;
import org.hdstar.widget.PullToRefreshListView.OnRefreshListener;
import org.hdstar.widget.TopicsAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
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

public class ForumFragment extends StackFragment<List<Topic>> {
	private View view;
	private PullToRefreshListView listView;
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
		url = getArguments() != null ? getArguments().getString("url") : null;
		forumId = getArguments() != null ? getArguments().getInt("id") : 1;
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
			fetch();
		} else {
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
		}
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

	protected void init() {
		final float dip = this.getResources().getDisplayMetrics().density;
		final Activity act = getActivity();
		final View footerView = LayoutInflater.from(act).inflate(
				R.layout.footer_view, null);
		listView = (PullToRefreshListView) view.findViewById(android.R.id.list);
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
		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	void viewTopic(int topicId, int page, String title) {
		getStackAdapter().forward(
				TopicFragment.newInstance(topicId, page, title));
		getViewPager()
				.setCurrentItem(getViewPager().getCurrentItem() + 1, true);
	}

	/**
	 * ͨ��AsyncTask��ȡ����
	 * */
	void fetch() {
		if (task != null) {
			return;
		}
		listView.setSelection(0);
		listView.prepareForRefresh();
		task = new ViewForumsTask(HDStarApp.cookies);
		task.attach(mCallback);
		task.execute(forumId + "");
	}

	/**
	 * ����ListView
	 * 
	 * @param list
	 *            �µ�Topic�б�
	 * */
	public void updateView(List<Topic> list) {
		adapter.setList(list);
		adapter.notifyDataSetChanged();
		listView.onRefreshComplete();
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

	/** ������һҳ */
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
		task.detach();
		task = new ViewForumsTask(HDStarApp.cookies);
		task.attach(new TaskCallback<List<Topic>>() {

			@Override
			public void onComplete(List<Topic> list) {
				task.detach();
				// topics.addAll((ArrayList<Topic>) list);
				adapter.itemsAddAll((ArrayList<Topic>) list);
				adapter.notifyDataSetChanged();
				((TextView) view.findViewById(R.id.loading_next_page_text))
						.setText(R.string.next_page);
				view.findViewById(R.id.loading_next_page_progressBar)
						.setVisibility(View.GONE);
			}

			@Override
			public void onFail(Integer msgId) {
				listView.onRefreshComplete();
				task.detach();
				((TextView) view.findViewById(R.id.loading_next_page_text))
						.setText(R.string.next_page);
				view.findViewById(R.id.loading_next_page_progressBar)
						.setVisibility(View.GONE);
				Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				task.detach();
				listView.onRefreshComplete();
			}

		});
		task.execute(Const.Urls.VIEW_FORUM_BASE_URL + forumId + "&page=" + page);
		page++;
	}

	void refresh() {
		refresh = true;
		if (task != null) {
			task.detach();
			task = null;
		}
		fetch();
	}

	TaskCallback<List<Topic>> mCallback = new TaskCallback<List<Topic>>() {

		@Override
		public void onComplete(List<Topic> list) {
			task.detach();
			listView.onRefreshComplete();
			if (refresh) {
				adapter.clearItems();
			}
			adapter.itemsAddAll(list);
			if (adapter.getCount() != 0) {
				final Activity act = getActivity();
				final View footerView = LayoutInflater.from(act).inflate(
						R.layout.footer_view, null);
				listView.addFooterView(footerView);
				footerView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						doNextPageClick(footerView);
					}
				});
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			task.detach();
			listView.onRefreshComplete();
			listView.setSelection(1);
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			task.detach();
			listView.onRefreshComplete();
		}
	};

}