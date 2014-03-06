package org.hdstar.widget.fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.ForumPostType;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.Post;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.TopicDetails;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.task.OriginTask;
import org.hdstar.util.CustomLinkMovementMethod;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.adapter.PostAdapter;
import org.hdstar.widget.adapter.PostAdapter.OnPostClickListener;
import org.jsoup.Jsoup;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ch.boye.httpclientandroidlib.HttpResponse;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TopicFragment extends StackFragment {

	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView listView;
	private int topicId;
	private String title = "";
	private int page = 0;
	private PostAdapter adapter = null;

	private boolean pauseOnScroll = false;
	private boolean pauseOnFling = true;
	private CustomDialog loadingDialog = null;

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
			args.putString("url", CommonUrls.HDStar.SERVER_VIEW_TOPIC_URL
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
		args.putInt("page", page);
		args.putString("title", title);
		args.putString("url", CommonUrls.HDStar.SERVER_VIEW_TOPIC_URL
				+ "?topicId=" + topicId + "&page=" + page);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setHasOptionsMenu(true);
		Bundle bundle = getArguments();
		topicId = bundle.getInt("topicId");
		page = getArguments().getInt("page");
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
		if (mPullToRefreshLayout != null)
			mPullToRefreshLayout.setRefreshComplete();
		CustomLinkMovementMethod.detach();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CustomLinkMovementMethod.attach(this);
		// 首次打开，需要获取数据
		boolean init = false;
		if (adapter == null) {
			init = true;
			adapter = new PostAdapter(getActivity(), new ArrayList<Post>(),
					new OnPostClickListener() {

						@Override
						public void quote(Post p) {
							String username = Jsoup.parseBodyFragment(
									p.userName).text();
							username = username.substring(0,
									username.indexOf("("));
							reply(p.body, username);
						}

						@Override
						public void pm(int receiver) {
							push(PMFragment.newInstance(receiver));
						}

						@Override
						public void edit(Post p, boolean top) {
							TopicFragment.this.edit(p.body, p.id, page == 0
									&& top);
						}

						@Override
						public void delete(int id, boolean isFirst) {
							TopicFragment.this.delete(id, isFirst && page == 0);
						}
					});
		}
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), pauseOnScroll, pauseOnFling));
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		// transformer.setFromEndLabel(getString(R.string.pull_to_add_next_page),
		// getString(R.string.release_to_add_next_page));
		ActionBarPullToRefresh
				.from(getActivity())
				.options(
						Options.create().refreshOnUp(true)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				.theseChildrenArePullable(R.id.post_list)
				.listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						doRefresh();
					}
				}).setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				mPullToRefreshLayout.setRefreshComplete();
				detachTask();
			}
		});
		if (!init) {
			adapter.notifyDataSetChanged();
		} else {
			mPullToRefreshLayout.post(new Runnable() {

				@Override
				public void run() {
					refresh();
				}
			});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.topic_view, null);
		mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		listView = (ListView) view.findViewById(R.id.post_list);
		return view;
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
	// .setSubtitle(title);
	// MenuItem item = menu.add(0, R.id.ab_reply, 0, R.string.reply);
	// item.setIcon(R.drawable.reply);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
	// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.ab_reply:
	// reply(null, null);
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onCreateActionBar(Menu menu) {
		// 尚未初始化或主题已锁定
		if (adapter == null || adapter.getList().size() == 0
				|| adapter.getList().get(0).id == 0) {
			return;
		}
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(title);
		MenuItem item = menu.add(0, R.id.ab_reply, 0, R.string.reply);
		item.setIcon(R.drawable.reply);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public boolean onActionBarSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ab_reply:
			reply(null, null);
			return true;
		}
		return false;
	}

	@Override
	public void refresh() {
		mPullToRefreshLayout.setRefreshing(true);
		doRefresh();
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		DelegateTask<TopicDetails> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(mCallback);
		attachTask(task);
		task.execGet(url, new TypeToken<ResponseWrapper<TopicDetails>>() {
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
			push(ReplyFragment.newInstance(topicId + "", text, username,
					ForumPostType.Reply));
		}
	}

	void edit(String text, int id, boolean mainFloor) {
		if (getActivity().findViewById(android.R.id.list) != null
				&& adapter.getList() != null) {
			if (mainFloor) {
				push(NewTopicFragment.newInstance(id, title, text));
			} else {
				push(ReplyFragment.newInstance(id + "", text, null,
						ForumPostType.Edit));
			}
		}
	}

	@SuppressLint("DefaultLocale")
	private void delete(int id, boolean isTopic) {
		final String url;
		int msg;
		if (isTopic) {
			msg = R.string.delete_topic_msg;
			url = String.format(CommonUrls.HDStar.DELETE_TOPIC_URL, topicId);
		} else {
			msg = R.string.delete_post_msg;
			url = String.format(CommonUrls.HDStar.DELETE_POST_URL, id);
		}
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm)
				.setIcon(R.drawable.ic_launcher)
				.setMessage(msg)
				.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								loadingDialog = new CustomDialog(getActivity(),
										R.string.deleting);
								loadingDialog
										.setOnDismissListener(new OnDismissListener() {

											@Override
											public void onDismiss(
													DialogInterface arg0) {
												detachTask();
											}
										});
								loadingDialog.show();
								OriginTask<Void> task = OriginTask
										.newInstance(HDStarApp.cookies);
								task.attach(delCallback);
								attachTask(task);
								task.execGet(
										url,
										new org.hdstar.task.parser.ResponseParser<Void>() {

											@Override
											public Void parse(HttpResponse res,
													InputStream in) {
												if (res.getStatusLine()
														.getStatusCode() == 302) {
													msgId = SUCCESS_MSG_ID;
												}
												return null;
											}
										});
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create().show();
	}

	private TaskCallback<TopicDetails> mCallback = new TaskCallback<TopicDetails>() {

		@SuppressLint("NewApi")
		@Override
		public void onComplete(TopicDetails result) {
			if (result.title != null) {
				title = result.title;
				((SherlockFragmentActivity) getActivity())
						.getSupportActionBar().setSubtitle(title);
			}
			adapter.clearItems();
			adapter.addAll(result.posts);
			adapter.notifyDataSetChanged();
			((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
			mPullToRefreshLayout.setRefreshComplete();
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
			detachTask();
		}
	};

	private TaskCallback<Void> delCallback = new TaskCallback<Void>() {

		@Override
		public void onComplete(Void result) {
			loadingDialog.dismiss();
			mPullToRefreshLayout.setRefreshing(true);
		}

		@Override
		public void onCancel() {
			loadingDialog.dismiss();
		}

		@Override
		public void onFail(Integer msgId) {
			loadingDialog.dismiss();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}
	};
}
