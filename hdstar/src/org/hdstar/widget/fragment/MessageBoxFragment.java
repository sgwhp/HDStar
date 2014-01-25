package org.hdstar.widget.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.BaseStackActivity;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.model.Message;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.task.OriginTask;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.adapter.MessageAdapter;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener2;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.reflect.TypeToken;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageBoxFragment extends StackFragment {
	private int boxType;
	private List<Message> list;
	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView listView;
	private Parcelable listViewState;
	private View view;
	private MessageAdapter adapter;
	private CustomDialog dialog = null;
	private int page = 0;

	public static MessageBoxFragment newInstance(int boxType) {
		Bundle bundle = new Bundle();
		bundle.putInt("boxType", boxType);
		bundle.putString("url", Const.Urls.SERVER_VIEW_MESSAGES_URL
				+ "?boxtype=" + boxType);
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
		setHasOptionsMenu(true);
		if (adapter == null) {
			list = new ArrayList<Message>();
			adapter = new MessageAdapter(getActivity(), list, boxType);
		}
		init();
		if (adapter.getList() == null || adapter.getList().size() == 0) {
			mPullToRefreshLayout.post(new Runnable() {

				@Override
				public void run() {
					refresh();
				}
			});
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDestroyView() {
		// StackPagerAdapter forward和ViewPager
		// setCurrentItem时各会触发一次onDestroyView
		// 但两次之间并未使得listView的onRestoreInstanceState立即生效，故只有第一次的状态是有效的
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
				.setSubtitle(null);
		menu.add(0, MessageActivity.DELETE_MENU_ITEM_ID, 0, R.string.delete)
				.setIcon(R.drawable.delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MessageActivity.DELETE_MENU_ITEM_ID:
			delete();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		mPullToRefreshLayout.setRefreshing(true);
		fetch();
	}

	private void init() {
		mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		listView = (ListView) view.findViewById(R.id.messageList);
		listView.setAdapter(adapter);
		if (listViewState != null) {
			listView.onRestoreInstanceState(listViewState);
		}
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Message msg = list.get(position - 1);
				push(ViewMessageFragment.newInstance(msg.id, msg.subject,
						msg.sender, msg.time));
			}

		});
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		transformer.setFromEndLabel(getString(R.string.pull_to_add_next_page),
				getString(R.string.release_to_add_next_page));
		ActionBarPullToRefresh
				.from(getActivity())
				.options(
						Options.create().refreshOnUp(true).mode(Mode.BOTH)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(R.id.messageList)
				.listener(new OnRefreshListener2() {

					@Override
					public void onRefreshStartedFromStart(View view) {
						doRefresh();
					}

					@Override
					public void onRefreshStartedFromEnd(View view) {
						loadNextPage();
					}
				}).setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				mPullToRefreshLayout.setRefreshComplete();
				detachTask();
			}
		});
	}

	private void fetch() {
		if (mTask != null) {
			return;
		}
		DelegateTask<List<Message>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(fetchCallback);
		attachTask(task);
		task.execGet(url, new TypeToken<ResponseWrapper<List<Message>>>() {
		}.getType());
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	private void delete() {
		if (adapter.getSelectedCount() == 0) {
			Crouton.makeText(getActivity(), R.string.no_msg_selected,
					Style.CONFIRM).show();
			return;
		}
		dialog = new CustomDialog(getActivity(), R.string.deleting);
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					detachTask();
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
		dialog.show();
		detachTask();
		OriginTask<Void> task = OriginTask.newInstance(HDStarApp.cookies);
		task.attach(delCallback);
		attachTask(task);
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("action", "moveordel"));
		nvp.add(new BasicNameValuePair("box", "1"));
		nvp.add(new BasicNameValuePair("delete", "删除"));
		int[] ids = adapter.getSelectedIds();
		for (int i = 0; i < ids.length; i++) {
			nvp.add(new BasicNameValuePair("messages[]", ids[i] + ""));
		}
		try {
			task.execPost(Const.Urls.COMMON_MESSAGE_BOX_URL, nvp);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void loadNextPage() {
		DelegateTask<List<Message>> task = DelegateTask
				.newInstance(HDStarApp.cookies);
		task.attach(addCallback);
		attachTask(task);
		task.execGet(url + "&page=" + ++page,
				new TypeToken<ResponseWrapper<List<Message>>>() {
				}.getType());
	}

	TaskCallback<List<Message>> fetchCallback = new TaskCallback<List<Message>>() {
		@Override
		public void onComplete(List<Message> result) {
			mPullToRefreshLayout.setRefreshComplete();
			list.clear();
			list.addAll(result);
			adapter.clear();
			adapter.addAll(result);
			adapter.notifyDataSetChanged();
			SoundPoolManager.play(getActivity());
			HDStarApp.hasNewMessage = false;
			((BaseStackActivity) getActivity()).refreshMenu();
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

	TaskCallback<List<Message>> addCallback = new TaskCallback<List<Message>>() {

		@Override
		public void onComplete(List<Message> result) {
			mPullToRefreshLayout.setRefreshComplete();
			if (result.size() > 0) {
				list.addAll(result);
				adapter.addAll(result);
				adapter.notifyDataSetChanged();
			} else {
				Crouton.makeText(getActivity(), R.string.no_more_data,
						Style.CONFIRM).show();
			}
			SoundPoolManager.play(getActivity());
		}

		@Override
		public void onCancel() {
			--page;
			mPullToRefreshLayout.setRefreshComplete();
		}

		@Override
		public void onFail(Integer msgId) {
			--page;
			mPullToRefreshLayout.setRefreshComplete();
		}
	};

	TaskCallback<Void> delCallback = new TaskCallback<Void>() {

		@Override
		public void onComplete(Void result) {
			dialog.dismiss();
			doRefresh();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Crouton.makeText(getActivity(), msgId, Style.ALERT).show();
		}
	};
}
