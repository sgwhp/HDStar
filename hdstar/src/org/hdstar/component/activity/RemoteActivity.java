package org.hdstar.component.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.RssSettingManager;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.RemoteSetting;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssChannel;
import org.hdstar.model.RssItem;
import org.hdstar.model.RssSetting;
import org.hdstar.remote.RemoteBase;
import org.hdstar.remote.RemoteFactory;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;
import org.hdstar.task.TaskStatus;
import org.hdstar.util.RssHandler;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.TextProgressBar;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpResponse;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnCancelListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.slidingmenu.lib.SlidingMenu;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RemoteActivity extends BaseActivity implements OnClickListener {
	private PullToRefreshListView refreshView;
	private View root;
	private View empty;
	private View start, pause, stop, delete;
	private ListView listView;
	private ExpandableListView rssListView;
	private RemoteTaskAdapter adapter;
	private RssAdapter rssAdapter;
	private ArrayList<RemoteTaskInfo> list = new ArrayList<RemoteTaskInfo>();
	// private ArrayList<RssLabel> rssList = new ArrayList<RssLabel>();
	private boolean[] selected;
	private RssItem selectedRssItem;
	private int selectedCount;// 选中的下载任务数
	private PopupWindow window = null;
	private PopupWindow addTorrentWindow;
	private EditText dirEt;
	private LinearLayout ctrlBox;
	private CustomDialog dialog = null;
	private BaseAsyncTask<?> mTask;
	private BaseAsyncTask<?> rssTask;
	private LayoutInflater inflater;
	private TextProgressBar disk;
	private Button refreshDiskInfoBtn;
	private BaseAsyncTask<long[]> diskTask;
	private RemoteBase remote;
	private RemoteSetting setting;
	private ArrayList<RssSetting> rssSettings;
	private SparseArray<RssChannel> rssChannels = new SparseArray<RssChannel>();
	private TaskStatus[] rssStatus;

	public RemoteActivity() {
		super(R.string.rutorrent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		setContentView(R.layout.remote_layout);
		getSlidingMenu().setSecondaryMenu(R.layout.rss_list);

		root = findViewById(R.id.task_list_content);
		refreshView = (PullToRefreshListView) findViewById(R.id.task_list);
		empty = findViewById(R.id.empty);

		rssListView = (ExpandableListView) findViewById(R.id.rss_list);

		inflater = LayoutInflater.from(this);
		init();
		refreshView.setRefreshing(false);
		refreshDiskInfo();
	}

	@Override
	protected void onStop() {
		detachTask();
		super.onStop();
	}

	protected void attachTask(BaseAsyncTask<?> task) {
		if (mTask != null) {
			mTask.detach();
		}
		mTask = task;
	}

	private void attachRssTask(BaseAsyncTask<?> task) {
		if (rssTask != null) {
			rssTask.detach();
		}
		rssTask = task;
	}

	protected void detachTask() {
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		if (rssTask != null) {
			rssTask.detach();
			rssTask = null;
		}
		if (diskTask != null) {
			diskTask.detach();
			diskTask = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, Menu.FIRST, 0, R.string.rss);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			showSecondaryMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
			start();
			break;
		case R.id.pause:
			pause();
			break;
		case R.id.stop:
			stop();
			break;
		case R.id.del:
			new AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setIcon(R.drawable.ic_launcher)
					.setMultiChoiceItems(
							new String[] { getString(R.string.remove_file) },
							new boolean[] { setting.rmFile },
							new OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									setting.rmFile = isChecked;
								}
							})
					.setPositiveButton(R.string.delete,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									delete();
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
		case R.id.add_torrent:
			addTorrentWindow.dismiss();
			download();
			break;
		case R.id.close:
			addTorrentWindow.dismiss();
			break;
		case R.id.refresh_disk_info:
			refreshDiskInfo();
			break;
		}
	}

	protected void init() {
		rssSettings = RssSettingManager.getAll(this);
		rssStatus = new TaskStatus[rssSettings.size()];
		for (int i = rssSettings.size() - 1; i >= 0; i--) {
			rssStatus[i] = TaskStatus.Normal;
		}
		// SharedPreferences share = getSharedPreferences(
		// Const.RUTORRENT_SHARED_PREFS, Activity.MODE_PRIVATE);
		setting = getIntent().getParcelableExtra("remote");
		remote = RemoteFactory.newInstanceByName(setting.type);
		remote.setIpNPort(setting.ip);
		listView = refreshView.getRefreshableView();
		adapter = new RemoteTaskAdapter();
		listView.setAdapter(adapter);
		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(RemoteActivity.this,
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				doRefresh();
			}
		});

		refreshView.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				if (mTask != null) {
					mTask.detach();
				}
			}
		});

		rssAdapter = new RssAdapter();
		rssListView.setAdapter(rssAdapter);
		rssListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				TaskStatus status = rssStatus[groupPosition];
				if (status == TaskStatus.Normal || status == TaskStatus.Failed) {
					rssStatus[groupPosition] = TaskStatus.Refreshing;
					refreshRss(groupPosition);
					rssAdapter.notifyDataSetChanged();
				}
			}
		});

		rssListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				selectedRssItem = rssChannels.get(groupPosition).items
						.get(childPosition);
				showDownloadWindow();
				return false;
			}
		});

		if (remote.diskEnable()) {
			((ViewStub) findViewById(R.id.disk_info)).inflate();
			findViewById(R.id.refresh_disk_info).setOnClickListener(this);
			refreshDiskInfoBtn = (Button) findViewById(R.id.refresh_disk_info);
			disk = (TextProgressBar) findViewById(R.id.disk_size);
		}
		// 初始化下载任务控制弹出窗口
		ctrlBox = (LinearLayout) inflater.inflate(
				R.layout.remote_task_ctrl_layout, null);
		start = ctrlBox.findViewById(R.id.start);
		start.setOnClickListener(this);
		pause = ctrlBox.findViewById(R.id.pause);
		pause.setOnClickListener(this);
		stop = ctrlBox.findViewById(R.id.stop);
		stop.setOnClickListener(this);
		delete = ctrlBox.findViewById(R.id.del);
		delete.setOnClickListener(this);
		window = new PopupWindow(ctrlBox, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, false);
		window.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bottom_pop_up_window_bg));
		window.setAnimationStyle(R.style.task_ctrl_box_anim_style);
		// 初始化rss下载弹出窗口
		View addTorrentLayout = inflater.inflate(R.layout.add_torrent_dialog,
				null);
		addTorrentLayout.findViewById(R.id.add_torrent)
				.setOnClickListener(this);
		addTorrentLayout.findViewById(R.id.close).setOnClickListener(this);
		dirEt = (EditText) addTorrentLayout.findViewById(R.id.download_dir);
		dirEt.setText(setting.downloadDir);
		addTorrentWindow = new PopupWindow(addTorrentLayout,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		addTorrentWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bottom_pop_up_window_bg));
		addTorrentWindow.setAnimationStyle(R.style.normalPopWindow_anim_style);
	}

	private void refreshRss(int refreshingLabel) {
		BaseAsyncTask<RssChannel> task = new BaseAsyncTask<RssChannel>();
		task.attach(new RssCallBack(refreshingLabel));
		attachRssTask(task);
		task.execGet(rssSettings.get(refreshingLabel).link,
				new ResponseParser<RssChannel>() {

					@Override
					public RssChannel parse(HttpResponse res, InputStream in) {
						SAXParserFactory spf = SAXParserFactory.newInstance();
						if (spf != null) {
							SAXParser sp;
							try {
								sp = spf.newSAXParser();
								RssHandler handler = new RssHandler();
								sp.parse(in, handler);
								msgId = SUCCESS_MSG_ID;
								return handler.getChannel();
							} catch (ParserConfigurationException e) {
								e.printStackTrace();
							} catch (SAXException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						return null;
					}
				});
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = remote.fetchList(this);
		task.attach(mCallback);
		attachTask(task);
		task.execute("");
	}

	private String[] selectedHashes() {
		String[] hashes = new String[selectedCount];
		for (int i = 0, j = 0; i < list.size(); i++) {
			if (selected[i]) {
				hashes[j++] = list.get(i).hash;
			}
		}
		return hashes;
	}

	private void start() {
		if (selectedCount == 0) {
			Crouton.makeText(this, R.string.no_task_selected, Style.CONFIRM)
					.show();
			return;
		}
		final BaseAsyncTask<Boolean> task = remote.start(selectedHashes());
		if (task == null) {
			return;
		}
		task.attach(processCallback);
		attachTask(task);
		task.execute("");
		dialog = new CustomDialog(this, R.string.connecting);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				task.detach();
			}
		});
		dialog.show();
	}

	private void pause() {
		if (selectedCount == 0) {
			Crouton.makeText(this, R.string.no_task_selected, Style.CONFIRM)
					.show();
			return;
		}
		final BaseAsyncTask<Boolean> task = remote.pause(selectedHashes());
		if (task == null) {
			return;
		}
		task.attach(processCallback);
		attachTask(task);
		task.execute("");
		dialog = new CustomDialog(this, R.string.connecting);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				task.detach();
			}
		});
		dialog.show();
	}

	private void stop() {
		if (selectedCount == 0) {
			Crouton.makeText(this, R.string.no_task_selected, Style.CONFIRM)
					.show();
			return;
		}
		final BaseAsyncTask<Boolean> task = remote.stop(selectedHashes());
		if (task == null) {
			return;
		}
		task.attach(processCallback);
		attachTask(task);
		task.execute("");
		dialog = new CustomDialog(this, R.string.connecting);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				task.detach();
			}
		});
		dialog.show();
	}

	private void delete() {
		if (selectedCount == 0) {
			Crouton.makeText(this, R.string.no_task_selected, Style.CONFIRM)
					.show();
			return;
		}
		setting.saveRemoveFile(this);
		final BaseAsyncTask<Boolean> task = remote.remove(setting.rmFile,
				selectedHashes());
		if (task == null) {
			return;
		}
		task.attach(processCallback);
		attachTask(task);
		task.execute("");
		dialog = new CustomDialog(this, R.string.connecting);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				task.detach();
			}
		});
		dialog.show();
	}

	/**
	 * 展示下载确认窗口
	 */
	private void showDownloadWindow() {
		addTorrentWindow.update();
		addTorrentWindow.showAtLocation(root, Gravity.CENTER, 0, 0);
	}

	/**
	 * 从rss中添加下载任务
	 */
	private void download() {
		dialog = new CustomDialog(this, R.string.connecting);
		String dir = dirEt.getText().toString();
		final BaseAsyncTask<Boolean> task = remote.addByUrl(dir
				, selectedRssItem.getTheLink());
		if (task == null) {
			return;
		}
		task.attach(new TaskCallback<Boolean>() {

			@Override
			public void onComplete(Boolean result) {
				dialog.dismiss();
				refreshDiskInfo();
			}

			@Override
			public void onCancel() {
				dialog.dismiss();
			}

			@Override
			public void onFail(Integer msgId) {
				dialog.dismiss();
				Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT,
						(ViewGroup) rssListView.getParent()).show();
			}
		});
		attachRssTask(task);
		task.execute("");
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				task.detach();
			}
		});
		dialog.show();
	}

	private void doRefresh() {
		if (window != null) {
			window.dismiss();
			empty.setVisibility(View.GONE);
		}
		if (mTask != null) {
			mTask.detach();
			mTask = null;
		}
		fetch();
	}

	/**
	 * 刷新硬盘信息
	 */
	private void refreshDiskInfo() {
		if(!remote.diskEnable()){
			return;
		}
		refreshDiskInfoBtn.setEnabled(false);
		if (diskTask != null) {
			diskTask.detach();
		}
		diskTask = remote.getDiskInfo();
		diskTask.attach(diskCallback);
		diskTask.execute("");
	}

	private TaskCallback<ArrayList<RemoteTaskInfo>> mCallback = new TaskCallback<ArrayList<RemoteTaskInfo>>() {

		@Override
		public void onComplete(ArrayList<RemoteTaskInfo> result) {
			refreshView.onRefreshComplete();
			list.clear();
			SoundPoolManager.play(RemoteActivity.this);
			list.addAll(result);
			selected = new boolean[list.size()];
			selectedCount = 0;
			window.dismiss();
			empty.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onCancel() {
			refreshView.onRefreshComplete();
		}

		@Override
		public void onFail(Integer msgId) {
			refreshView.onRefreshComplete();
			Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT).show();
		}
	};

	private TaskCallback<Boolean> processCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			refreshView.setRefreshing(true);
			dialog.dismiss();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
		}

	};

	private class RssCallBack implements TaskCallback<RssChannel> {
		private int labelPosition;

		private RssCallBack(int labelPosition) {
			this.labelPosition = labelPosition;
		}

		@Override
		public void onComplete(RssChannel result) {
			rssChannels.put(labelPosition, result);
			rssStatus[labelPosition] = TaskStatus.Finished;
			rssAdapter.notifyDataSetChanged();
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			rssStatus[labelPosition] = TaskStatus.Failed;
			rssAdapter.notifyDataSetChanged();
			Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT,
					(ViewGroup) rssListView.getParent()).show();
		}
	}

	private TaskCallback<long[]> diskCallback = new TaskCallback<long[]>() {

		@Override
		public void onComplete(long[] result) {
			disk.setProgress((int) ((result[0] - result[1]) * 100.0 / result[0]));
			refreshDiskInfoBtn.setEnabled(true);
		}

		@Override
		public void onCancel() {
			refreshDiskInfoBtn.setEnabled(true);
		}

		@Override
		public void onFail(Integer msgId) {
			refreshDiskInfoBtn.setEnabled(true);
			Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT,
					(ViewGroup) rssListView.getParent()).show();
		}
	};

	private static class ViewHolder {
		TextView title, info;
		ProgressBar progress;
		CheckBox check;
		ImageView state;

		ViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.title);
			info = (TextView) v.findViewById(R.id.task_info);
			progress = (ProgressBar) v.findViewById(R.id.progress);
			check = (CheckBox) v.findViewById(R.id.check);
			state = (ImageView) v.findViewById(R.id.state);
		}
	}

	public class RemoteTaskAdapter extends BaseAdapter {

		private String taskInfo;

		public RemoteTaskAdapter() {
			selectedCount = 0;
			selected = new boolean[list.size()];
			taskInfo = getString(R.string.task_info);
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
					Util.formatFileSize(item.size),
					Util.formatFileSize(item.uploaded), item.ratio,
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
			if (item.open == 0) {
				holder.state.setImageResource(R.drawable.state_stop);
			} else if (item.state == 0) {
				holder.state.setImageResource(R.drawable.state_pause);
			} else if (item.state == 1) {
				if (item.completeSize == item.size) {
					holder.state.setImageResource(R.drawable.state_seeding);
				} else {
					holder.state.setImageResource(R.drawable.state_leaching);
				}
			}
			return convertView;
		}

		public ArrayList<RemoteTaskInfo> getList() {
			return list;
		}

	}

	private static class RssChannelViewHolder {
		private ImageView expand, icon;
		private TextView label;
		private ProgressBar progress;
		private Button refresh;

		RssChannelViewHolder(View v) {
			expand = (ImageView) v.findViewById(R.id.expand);
			icon = (ImageView) v.findViewById(R.id.rss_icon);
			label = (TextView) v.findViewById(R.id.label);
			progress = (ProgressBar) v.findViewById(R.id.progress);
			refresh = (Button) v.findViewById(R.id.refresh_rss_label);
		}
	}

	private class RssAdapter extends BaseExpandableListAdapter {

		RssAdapter() {
		}

		@Override
		public int getGroupCount() {
			return rssSettings == null ? 0 : rssSettings.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			RssChannel channel = rssChannels.get(groupPosition);
			return channel == null ? 0 : channel.items.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			RssChannelViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rss_channel, null);
				holder = new RssChannelViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (RssChannelViewHolder) convertView.getTag();
			}
			RssChannel channel = rssChannels.get(groupPosition);
			if (channel != null) {
				holder.label.setText(rssSettings.get(groupPosition).label + "("
						+ rssChannels.get(groupPosition).items.size() + ")");
			} else {
				holder.label.setText(rssSettings.get(groupPosition).label);
			}
			ImageLoader.getInstance().displayImage(
					String.format(Const.Urls.GETFVO_URL,
							rssSettings.get(groupPosition).link), holder.icon,
					HDStarApp.displayOptions);
			TaskStatus status = rssStatus[groupPosition];
			if (status == TaskStatus.Refreshing) {
				holder.progress.setVisibility(View.VISIBLE);
				holder.refresh.setEnabled(false);
			} else {
				holder.progress.setVisibility(View.INVISIBLE);
				holder.refresh.setEnabled(true);
			}
			if (isExpanded) {
				holder.expand.setImageResource(R.drawable.arrow_expand);
			} else {
				holder.expand.setImageResource(R.drawable.arrow_collapse);
			}
			holder.refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					rssStatus[groupPosition] = TaskStatus.Refreshing;
					notifyDataSetChanged();
					refreshRss(groupPosition);
				}
			});
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new TextView(RemoteActivity.this);
			}
			((TextView) convertView)
					.setText(rssChannels.get(groupPosition).items
							.get(childPosition).title);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	};
}
