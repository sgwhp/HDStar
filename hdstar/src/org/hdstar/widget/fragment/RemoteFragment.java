package org.hdstar.widget.fragment;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hdstar.R;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class RemoteFragment extends StackFragment implements OnClickListener {
	private PullToRefreshListView refreshView;
	private View root;
	private View empty;
	private View start, pause, stop, delete;
	private ListView listView;
	private Parcelable listViewState;
	private RemoteTaskAdapter adapter;
	private ArrayList<RemoteTaskInfo> list;
	private boolean[] selected;
	private int selectedCount;
	private PopupWindow window = null;
	private LinearLayout ctrlBox;

	public static RemoteFragment newInstance() {
		RemoteFragment f = new RemoteFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.remote_layout, null);
		refreshView = (PullToRefreshListView) root
				.findViewById(R.id.taskList);
		empty = root.findViewById(R.id.empty);
		start = root.findViewById(R.id.start);
		start.setOnClickListener(this);
		pause = root.findViewById(R.id.pause);
		pause.setOnClickListener(this);
		stop = root.findViewById(R.id.stop);
		stop.setOnClickListener(this);
		delete = root.findViewById(R.id.del);
		delete.setOnClickListener(this);
		return root;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SherlockFragmentActivity) getActivity()).invalidateOptionsMenu();
		if (adapter == null) {
			list = new ArrayList<RemoteTaskInfo>();
			adapter = new RemoteTaskAdapter();
		}
		init();
		if (list == null || list.size() == 0) {
			refreshView.setRefreshing(false);
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
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.start:
			break;
		case R.id.pause:
			break;
		case R.id.stop:
			break;
		case R.id.del:
			new AlertDialog.Builder(getActivity())
			.setTitle(R.string.confirm)
			.setIcon(R.drawable.ic_launcher)
			.setMessage(R.string.exit_message)
			.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}

					})
			.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}

					}).create().show();
			break;
		}
	}

	@Override
	public void onSelected() {
		listViewState = null;
	}

	@Override
	public void refresh() {
		refreshView.setRefreshing(false);
	}

	protected void init() {
		listView = refreshView.getRefreshableView();
		listView.setAdapter(adapter);
		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				doRefresh();
			}
		});
		window = new PopupWindow(ctrlBox, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, false);
		window.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bottom_pop_up_window_bg));
		window.setAnimationStyle(R.style.task_ctrl_box_anim_style);
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = new BaseAsyncTask<ArrayList<RemoteTaskInfo>>();
		task.attach(mCallback);
		attachTask(task);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cmd", "d.get_throttle_name="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=sch_ignore"));
		params.add(new BasicNameValuePair("cmd", "cat=$d.views="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=seedingtime"));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=addtime"));
		params.add(new BasicNameValuePair("mode", "list"));
		try {
			task.execPost(
					"http://91.121.104.122/rutorrent/plugins/httprpc/action.php",
					params, new ResponseParser<ArrayList<RemoteTaskInfo>>() {

						@Override
						public ArrayList<RemoteTaskInfo> parse(
								HttpResponse res, InputStream in) {
							JsonParser parser = new JsonParser();
							JsonElement element = parser
									.parse(new InputStreamReader(in));
							JsonObject obj = element.getAsJsonObject()
									.getAsJsonObject("t");
							Set<Entry<String, JsonElement>> set = obj
									.entrySet();
							ArrayList<RemoteTaskInfo> result = new ArrayList<RemoteTaskInfo>();
							RemoteTaskInfo info;
							JsonArray arr;
							for (Entry<String, JsonElement> entry : set) {
								info = new RemoteTaskInfo();
								arr = entry.getValue().getAsJsonArray();
								info.hash = entry.getKey();
								info.state = arr.get(3).getAsInt();
								info.title = arr.get(4).toString();
								info.size = arr.get(5).getAsLong();
								info.completeSize = arr.get(8).getAsLong();
								info.ratio = arr.get(10).getAsFloat() / 1000;
								info.upSpeed = arr.get(11).getAsLong();
								info.dlSpeed = arr.get(12).getAsLong();
								result.add(info);
							}
							msgId = SUCCESS_MSG_ID;
							return result;
						}
					});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void doRefresh() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	private TaskCallback<ArrayList<RemoteTaskInfo>> mCallback = new TaskCallback<ArrayList<RemoteTaskInfo>>() {

		@Override
		public void onComplete(ArrayList<RemoteTaskInfo> result) {
			refreshView.onRefreshComplete();
			list.clear();
			SoundPoolManager.play(getActivity());
			list.addAll(result);
			selected = new boolean[list.size()];
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onCancel() {
			refreshView.onRefreshComplete();
		}

		@Override
		public void onFail(Integer msgId) {
			refreshView.onRefreshComplete();
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}
	};

	private static class ViewHolder {
		TextView title, info;
		ProgressBar progress;
		CheckBox check;

		ViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.title);
			info = (TextView) v.findViewById(R.id.task_info);
			progress = (ProgressBar) v.findViewById(R.id.progress);
			check = (CheckBox) v.findViewById(R.id.check);
		}
	}

	public class RemoteTaskAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private String taskInfo;

		public RemoteTaskAdapter() {
			inflater = LayoutInflater.from(getActivity());
			selectedCount = 0;
			selected = new boolean[list.size()];
			// for (int i = 0; i < list.size(); i++) {
			// selected[i] = false;
			// }
			taskInfo = getActivity().getString(R.string.task_info);
			ctrlBox = (LinearLayout) inflater.inflate(
					R.layout.remote_task_ctrl_layout, null);
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.remote_task_row, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			RemoteTaskInfo item = list.get(position);
			holder.title.setText(item.title);
			holder.progress
					.setProgress((int) (item.completeSize * 100.0 / item.size));
			holder.info.setText(String.format(taskInfo,
					Util.formatFileSize(item.size), item.ratio,
					Util.formatFileSize(item.dlSpeed),
					Util.formatFileSize(item.upSpeed)));
			holder.check.setOnCheckedChangeListener(null);
			holder.check.setChecked(selected[position]);
			holder.check
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							selected[position] = isChecked;
							if (isChecked) {
								selectedCount++;
							} else {
								selectedCount--;
							}
							if (isChecked && selectedCount == 1) {
								empty.setVisibility(View.INVISIBLE);
								window.update();
								window.showAtLocation(root, Gravity.CENTER
										| Gravity.BOTTOM, 0, 0);
							} else if (selectedCount == 0) {
								empty.setVisibility(View.GONE);
								window.dismiss();
							}
							notifyDataSetChanged();
						}
					});
			return convertView;
		}

		public ArrayList<RemoteTaskInfo> getList() {
			return list;
		}

	}

}
