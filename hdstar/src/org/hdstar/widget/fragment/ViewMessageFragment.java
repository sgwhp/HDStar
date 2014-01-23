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

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ViewMessageFragment extends StackFragment {
	// private int boxType;
	private int messageId;
	private String subject;
	private MessageContent content;
	private TextView fromTV;
	private TextView contentTV;
	private TextView timeTV;
	private PullToRefreshLayout mPullToRefreshLayout;

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
		mPullToRefreshLayout = (PullToRefreshLayout) v
				.findViewById(R.id.ptr_layout);
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
		if (content == null) {
			mPullToRefreshLayout.post(new Runnable(){

				@Override
				public void run() {
					refresh();
				}});
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
		mPullToRefreshLayout.setRefreshing(true);
		doRefresh();
	}

	private void init() {
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
//		transformer.setFromEndLabel(getString(R.string.pull_to_add_next_page), getString(R.string.release_to_add_next_page));
		ActionBarPullToRefresh
		.from(getActivity())
		.options(
				Options.create().refreshOnUp(true).mode(Mode.BOTH)
						.headerLayout(R.layout.cancelable_header)
						.headerTransformer(transformer).build())
		// Here we mark just the ListView and it's Empty View as
		// pullable
		.theseChildrenArePullable(R.id.message_scroll_view).listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						doRefresh();
					}
				})
		.setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				detachTask();
			}
		});
		if (content != null) {
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
			mPullToRefreshLayout.setRefreshComplete();
			content = result;
			((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
			init();
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
