package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.model.Post;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.DelegateTask;
import org.hdstar.task.MyAsyncTask.TaskCallback;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.PostsAdapter;
import org.hdstar.widget.PullToRefreshListView;
import org.hdstar.widget.PullToRefreshListView.OnRefreshListener;
import org.jsoup.Jsoup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class TopicFragment extends StackFragment {

	private PullToRefreshListView listView;
	private int topicId;
	private String title = "";
	// private int page = 0;
	private PostsAdapter adapter = null;
	private boolean pauseOnScroll = false;
	private boolean pauseOnFling = true;

	// private ArrayList<Post> posts;

	public static TopicFragment newInstance(int topicId, int page, String title) {
		TopicFragment fragment = new TopicFragment();
		fragment.url = Const.Urls.SERVER_VIEW_TOPIC_URL + "?topicId=" + topicId
				+ "&page=" + page;
		Bundle args = new Bundle();
		args.putInt("topicId", topicId);
		args.putInt("page", page);
		args.putString("title", title);
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
	}

	@Override
	public void onDestroy() {
		adapter.clearAnimListener();
		super.onDestroy();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (adapter == null) {
			adapter = new PostsAdapter(getActivity(), new ArrayList<Post>());
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
		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refresh();
			}

		});
		if (adapter.getCount() != 0) {
			adapter.notifyDataSetChanged();
			listView.setSelection(1);
		} else {
			fetch();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.topic_view, null);
		listView = (PullToRefreshListView) view.findViewById(android.R.id.list);
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

	void fetch() {
		if (mTask != null) {
			return;
		}
		listView.prepareForRefresh();
		DelegateTask<List<Post>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(mCallback);
		attachTask(task);
		task.execGet(url, new TypeToken<ResponseWrapper<List<Post>>>() {
		}.getType());
	}

	void refresh() {
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
			listView.onRefreshComplete();
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onFail(Integer msgId) {
			listView.onRefreshComplete();
			listView.setSelection(1);
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			listView.onRefreshComplete();
			detachTask();
		}
	};
}
