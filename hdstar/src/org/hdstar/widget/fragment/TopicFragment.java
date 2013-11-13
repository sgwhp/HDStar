package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.model.Post;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.CustomLinkMovementMethod;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.PostAdapter;
import org.jsoup.Jsoup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class TopicFragment extends StackFragment {

	private PullToRefreshListView refreshView;
	private ListView listView;
	private int topicId;
	private String title = "";
	// private int page = 0;
	private PostAdapter adapter = null;

	private boolean pauseOnScroll = false;
	private boolean pauseOnFling = true;

	// private ArrayList<Post> posts;

	public static TopicFragment newInstance(String url) {
		TopicFragment fragment = new TopicFragment();
		Bundle args = new Bundle();
		Pattern pattern = Pattern.compile("topicid=([0-9]+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group(1));
			args.putInt("topicId", id);
			int page = 0;
			pattern = Pattern.compile("&page=([0-9]+)");
			matcher = pattern.matcher(url);
			if (matcher.find()) {
				page = Integer.parseInt(matcher.group(1));
			}
			args.putString("url", Const.Urls.SERVER_VIEW_TOPIC_URL
					+ "?topicId=" + id + "&page=" + page);
			fragment.setArguments(args);
			return fragment;
		}
		return null;
	}

	public static TopicFragment newInstance(int topicId, int page, String title) {
		TopicFragment fragment = new TopicFragment();
		Bundle args = new Bundle();
		args.putInt("topicId", topicId);
		// args.putInt("page", page);
		args.putString("title", title);
		args.putString("url", Const.Urls.SERVER_VIEW_TOPIC_URL + "?topicId="
				+ topicId + "&page=" + page);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		topicId = bundle.getInt("topicId");
		// page = getArguments().getInt("page");
		title = bundle.getString("title");
		if (title == null) {
			title = "";
		}
		url = bundle.getString("url");
	}

	@Override
	public void onDestroy() {
		adapter.clearAnimListener();
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		CustomLinkMovementMethod.detach();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CustomLinkMovementMethod.attach(this);
		if (adapter == null) {
			adapter = new PostAdapter(getActivity(), new ArrayList<Post>());
		}
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Post post = (Post) listView.getItemAtPosition(position);
				String username = Jsoup.parseBodyFragment(post.userName).text();
				username = username.substring(0, username.indexOf("("));
				// listView.setFocusable(false);
				reply(post.body, username);
			}

		});
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), pauseOnScroll, pauseOnFling));
		// 注册下拉列表刷新的监听器
		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				doRefresh();
			}
		});
		if (adapter.getCount() != 0) {
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
		} else {
			refreshView.setRefreshing(false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.topic_view, null);
		refreshView = (PullToRefreshListView) view
				.findViewById(R.id.pull_refresh_list);
		listView = refreshView.getRefreshableView();
		return view;
	}

	@Override
	public void onPrepareOptionsMenu(android.view.Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(title);
		MenuItem item = menu.add(0, ForumsActivity.COMMIT_ACTION_BAR_ID, 0,
				R.string.reply);
		item.setIcon(R.drawable.reply);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public void onActionBarClick(int menuItemId) {
		reply(null, null);
	}

	@Override
	public void refresh() {
		refreshView.setRefreshing(false);
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		// listView.prepareForRefresh();
		DelegateTask<List<Post>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(mCallback);
		attachTask(task);
		task.execGet(url, new TypeToken<ResponseWrapper<List<Post>>>() {
		}.getType());
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	void reply(String text, String username) {
		if (getActivity().findViewById(android.R.id.list) != null
				&& adapter.getList() != null) {
			push(ReplyFragment.newInstance(topicId + "", text, username));
		}
	}

	private TaskCallback<List<Post>> mCallback = new TaskCallback<List<Post>>() {

		@Override
		public void onComplete(List<Post> list) {
			adapter.clearItems();
			adapter.addAll(list);
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
			// listView.setSelection(1);
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			// listView.onRefreshComplete();
			refreshView.onRefreshComplete();
			detachTask();
		}
	};
}
