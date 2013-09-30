package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.DelegateTask;
import org.hdstar.task.MyAsyncTask.TaskCallback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;

public class ViewMessageFragment extends StackFragment<String> {
	private int boxType;
	private int messageId;
	private String subject;
	private String content;
	private TextView fromTV;
	private TextView contentTV;
	private TextView timeTV;
	private LinearLayout loading;
	private ProgressBar progress;
	private TextView message;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_message, null);
		fromTV = (TextView) v.findViewById(R.id.from);
		contentTV = (TextView) v.findViewById(R.id.messageContent);
		timeTV = (TextView) v.findViewById(R.id.time);
		fromTV.setText(getArguments().getString("from"));
		timeTV.setText(getArguments().getString("time"));
		loading = (LinearLayout) v.findViewById(R.id.loading);
		progress = (ProgressBar) v.findViewById(R.id.progressBar);
		message = (TextView) v.findViewById(R.id.message);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		init();
		if (content == null) {
			fetch();
		}
	}

	@Override
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(subject);
		menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.delete)
				.setIcon(R.drawable.delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	public void onActionBarClick(int MenuItemId) {
		delete();
	}

	private void init() {
		if (content != null) {
			contentTV.setText(content);
		}
	}

	private void fetch() {
		if (task != null) {
			task.detach();
		}
		task = new DelegateTask<String>(HDStarApp.cookies);
		task.attach(fetchCallback);
		if (boxType == Const.boxTypes[0]) {
			task.execGet(Const.Urls.SERVER_VIEW_MESSAGE_URL + messageId,
					new TypeToken<ResponseWrapper<String>>() {
					}.getType());
		}
	}

	void delete() {

	}

	TaskCallback<String> fetchCallback = new TaskCallback<String>() {
		@Override
		public void onComplete(String result) {
			loading.setVisibility(View.GONE);
			content = result;
			init();
		}

		@Override
		public void onFail(Integer msgId) {
			progress.setVisibility(View.GONE);
			message.setText(msgId);
		}

		@Override
		public void onCancel() {
			progress.setVisibility(View.GONE);
			message.setText("");
		}
	};

}
