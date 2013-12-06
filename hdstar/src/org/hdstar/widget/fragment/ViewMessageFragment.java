package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.model.MessageContent;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.CustomLinkMovementMethod;
import org.hdstar.util.URLImageParser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnCancelListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

public class ViewMessageFragment extends StackFragment {
	// private int boxType;
	private int messageId;
	private String subject;
	private MessageContent content;
	private TextView fromTV;
	private TextView contentTV;
	private TextView timeTV;
	private PullToRefreshScrollView refreshView;

	// private LinearLayout loading;
	// private ProgressBar progress;
	// private TextView message;

	public static ViewMessageFragment newInstance(int messageId,
			String subject, String from, String time) {
		ViewMessageFragment fragment = new ViewMessageFragment();
		Bundle args = new Bundle();
		args.putInt("messageId", messageId);
		args.putString("subject", subject);
		args.putString("from", from);
		args.putString("time", time);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		messageId = getArguments().getInt("messageId");
		subject = getArguments().getString("subject");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		CustomLinkMovementMethod.detach();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_message, null);
		fromTV = (TextView) v.findViewById(R.id.from);
		contentTV = (TextView) v.findViewById(R.id.messageContent);
		timeTV = (TextView) v.findViewById(R.id.time);
		fromTV.setText(getArguments().getString("from"));
		timeTV.setText(getArguments().getString("time"));
		refreshView = (PullToRefreshScrollView) v
				.findViewById(R.id.pull_refresh_scrollview);
		refreshView.setScrollingWhileRefreshingEnabled(true);
		refreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				doRefresh();
			}
		});
		// loading = (LinearLayout) v.findViewById(R.id.loading);
		// progress = (ProgressBar) v.findViewById(R.id.progressBar);
		// message = (TextView) v.findViewById(R.id.message);
		return v;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CustomLinkMovementMethod.attach(this);
		contentTV.setMovementMethod(CustomLinkMovementMethod.getInstance());
		// ((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		init();

		refreshView.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				abort();
			}
		});
		if (content == null) {
			refreshView.setRefreshingInit();
		}
	}

	@Override
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(subject);
		if (content != null && content.receiverId != 0) {
			menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.reply)
					.setIcon(R.drawable.reply)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
	}

	@Override
	public void onActionBarClick(int MenuItemId) {
		reply();
	}

	@Override
	public void refresh() {
		refreshView.setRefreshing(false);
	}

	private void init() {
		if (content != null) {
			// loading.setVisibility(View.GONE);
			contentTV.setText(Html.fromHtml(content.content,
					new URLImageParser(contentTV, getActivity()), null));
		}
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	private void fetch() {
		DelegateTask<MessageContent> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(fetchCallback);
		attachTask(task);
		task.execGet(Const.Urls.SERVER_VIEW_MESSAGE_URL + messageId,
				new TypeToken<ResponseWrapper<MessageContent>>() {
				}.getType());
	}

	void reply() {
		push(PMFragment.newInstance(messageId, subject, content));
	}

	TaskCallback<MessageContent> fetchCallback = new TaskCallback<MessageContent>() {
		@SuppressLint("NewApi")
		@Override
		public void onComplete(MessageContent result) {
			// loading.setVisibility(View.GONE);
			refreshView.onRefreshComplete();
			content = result;
			((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
			init();
		}

		@Override
		public void onFail(Integer msgId) {
			// progress.setVisibility(View.GONE);
			refreshView.onRefreshComplete();
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
			// progress.setVisibility(View.GONE);
			refreshView.onRefreshComplete();
		}
	};

}
