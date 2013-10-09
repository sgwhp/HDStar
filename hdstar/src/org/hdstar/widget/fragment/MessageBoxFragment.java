package org.hdstar.widget.fragment;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.model.Message;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.DelegateTask;
import org.hdstar.task.MyAsyncTask.TaskCallback;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.MessageAdapter;
import org.hdstar.widget.PullToRefreshListView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
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

public class MessageBoxFragment extends StackFragment<List<Message>> {
	private int boxType;
	private List<Message> list;
	private PullToRefreshListView listView;
	private Parcelable listViewState;
	private View view;
	private MessageAdapter adapter;

	public static MessageBoxFragment newInstance(int boxType) {
		Bundle bundle = new Bundle();
		bundle.putInt("boxType", boxType);
		bundle.putString("url", Const.Urls.SERVER_VIEW_MESSAGES_URL);
		MessageBoxFragment fragment = new MessageBoxFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		boxType = bundle.getInt("boxType");
		url = bundle.getString("url");
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
		}
	}
	
	@Override
	public void onDestroyView() {
		//StackPagerAdapter forward和ViewPager setCurrentItem时各会触发一次onDestroyView
		//但两次之间并未使得listView的onRestoreInstanceState立即生效，故只有第一次的状态是有效的
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
	public void initActionBar(Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.delete)
				.setIcon(R.drawable.delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public void onActionBarClick(int MenuItemId) {
		delete();
	}
	
	@Override
	public void onSelected() {
		listViewState = null;
	}

	private void init() {
		listView = (PullToRefreshListView) view.findViewById(R.id.messageList);
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
		}
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Message msg = list.get(position-1);
				push(ViewMessageFragment.newInstance(msg.id, msg.subject,
						msg.sender, msg.time));
			}

		});
	}

	private void fetch() {
		if (task != null) {
			return;
		}
		listView.setSelection(0);
		listView.prepareForRefresh();
		task = new DelegateTask<List<Message>>(HDStarApp.cookies);
		task.attach(mCallback);
		task.execGet(url, new TypeToken<ResponseWrapper<List<Message>>>() {
		}.getType());
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
			list.clear();
			list.addAll(result);
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
