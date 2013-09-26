package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.task.MyAsyncTask.TaskCallback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ViewMessageFragment extends StackFragment<String> {
	private int messageId;
	private String subject;
	private TextView from;
	private TextView content;
	private TextView time;

	public static ViewMessageFragment newInstance(int messageId, String subject) {
		ViewMessageFragment fragment = new ViewMessageFragment();
		Bundle args = new Bundle();
		args.putInt("messageId", messageId);
		args.putString("subject", subject);
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
		from = (TextView) v.findViewById(R.id.from);
		content = (TextView) v.findViewById(R.id.messageContent);
		time = (TextView) v.findViewById(R.id.time);
		return v;
	}

	@Override
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.delete)
				.setIcon(R.drawable.delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	public void onActionBarClick(int MenuItemId) {
		delete();
	}

	void delete() {

	}

	TaskCallback<String> mCallback = new TaskCallback<String>() {
		@Override
		public void onComplete(String result) {
		}

		@Override
		public void onFail(Integer msgId) {
		}

		@Override
		public void onCancel() {
		}
	};

}
