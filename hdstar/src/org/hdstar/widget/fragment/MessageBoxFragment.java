package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.model.Message;
import org.hdstar.task.MyAsyncTask.TaskCallback;
import org.hdstar.task.ViewMessagesTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.MessageAdapter;
import org.hdstar.widget.PullToRefreshListView;

import android.annotation.SuppressLint;
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

public class MessageBoxFragment extends StackFragment<List<Message>> {
	private int boxType;
	private List<Message> list;
	private PullToRefreshListView listView;
	private View view;
	private MessageAdapter adapter;

	public static MessageBoxFragment newInstance(int boxType) {
		Bundle bundle = new Bundle();
		bundle.putInt("boxType", boxType);
		MessageBoxFragment fragment = new MessageBoxFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boxType = getArguments().getInt("boxType");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.message_box, null);
		return view;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		if (adapter == null) {
			list = new ArrayList<Message>();
			adapter = new MessageAdapter(getActivity(), list, boxType);
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
		menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.delete)
				.setIcon(R.drawable.delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	public void onActionBarClick(int MenuItemId) {
		delete();
	}

	private void init() {
		listView = (PullToRefreshListView) view.findViewById(R.id.messageList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				getStackAdapter().forward(
						ViewMessageFragment.newInstance(1, ""));
				getViewPager().setCurrentItem(
						getViewPager().getCurrentItem() + 1, true);
			}

		});
	}

	private void fetch() {
		if (task != null) {
			return;
		}
		listView.setSelection(0);
		listView.prepareForRefresh();
		task = new ViewMessagesTask(HDStarApp.cookies);
		task.attach(mCallback);
		task.execute("");
	}

	private void delete() {
		if (adapter.getSelectedCount() == 0) {
			Toast.makeText(getActivity(), "没有选择任何消息记录", Toast.LENGTH_SHORT)
					.show();
			return;
		}

	}

	TaskCallback<List<Message>> mCallback = new TaskCallback<List<Message>>() {
		@Override
		public void onComplete(List<Message> result) {
			task.detach();
			listView.onRefreshComplete();
			adapter.setList(result);
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
