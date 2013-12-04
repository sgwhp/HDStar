package org.hdstar.component.activity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RutorrentRssItem;
import org.hdstar.model.RutorrentRssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.TextProgressBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnCancelListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.slidingmenu.lib.SlidingMenu;

public class RemoteActivity extends BaseActivity implements OnClickListener {
	private String ip;
	private PullToRefreshListView refreshView;
	private PullToRefreshExpandableListView refreshExpandableView;
	private View root;
	private View empty;
	private View start, pause, stop, delete;
	private ListView listView;
	private ExpandableListView rssListView;
	private RemoteTaskAdapter adapter;
	private RssAdapter rssAdapter;
	private ArrayList<RemoteTaskInfo> list = new ArrayList<RemoteTaskInfo>();
	private ArrayList<RutorrentRssLabel> rssList = new ArrayList<RutorrentRssLabel>();
	private boolean[] selected;
	private boolean[] selectedRss;
	private int refreshingLabel = -1;// 正在刷新的标签位置
	private int selectedCount;// 选中的下载任务数
	private PopupWindow window = null;
	private PopupWindow addRssWindow;
	private EditText dir;
	private LinearLayout ctrlBox;
	private CustomDialog dialog = null;
	private BaseAsyncTask<?> mTask;
	private BaseAsyncTask<?> rssTask;
	private LayoutInflater inflater;
	private int group = -1;// 选中的rss标签位置
	private TextProgressBar disk;
	private Button refreshDiskInfoBtn;
	private BaseAsyncTask<long[]> diskTask;

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

		refreshExpandableView = (PullToRefreshExpandableListView) findViewById(R.id.rss_list);
		rssListView = refreshExpandableView.getRefreshableView();

		inflater = LayoutInflater.from(this);
		init();
		refreshView.setRefreshing(false);
		refreshDiskInfo();
		refreshExpandableView.setRefreshing(false);
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
					.setMessage(R.string.exit_message)
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
		case R.id.download:
			showDownloadWindow();
			break;
		case R.id.add_rss:
			addRssWindow.dismiss();
			download();
			break;
		case R.id.close:
			addRssWindow.dismiss();
			break;
		case R.id.refresh_disk_info:
			refreshDiskInfo();
			break;
		}
	}

	protected void init() {
		SharedPreferences share = getSharedPreferences(
				Const.RUTORRENT_SHARED_PREFS, Activity.MODE_PRIVATE);
		ip = share.getString("ip", null);
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
					mTask.abort();
				}
			}
		});

		rssAdapter = new RssAdapter();
		rssListView.setAdapter(rssAdapter);
		// 只能同时展开一项
		rssListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				for (int i = 0; i < rssAdapter.getGroupCount(); i++) {
					if (groupPosition != i) {
						rssListView.collapseGroup(i);
					}
				}
				group = groupPosition;
				selectedRss = new boolean[rssList.get(groupPosition).items
						.size()];
			}
		});

		rssListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				group = -1;
				selectedRss = null;
			}
		});
		refreshExpandableView
				.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
						String label = DateUtils.formatDateTime(
								RemoteActivity.this,
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);
						refreshExpandableView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						if (refreshingLabel == -1) {
							refreshRss();
						} else {
							refreshRssLabel();
						}
					}
				});

		refreshExpandableView.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				if (rssTask != null) {
					rssTask.abort();
				}
			}
		});

		findViewById(R.id.refresh_disk_info).setOnClickListener(this);
		refreshDiskInfoBtn = (Button) findViewById(R.id.refresh_disk_info);
		disk = (TextProgressBar) findViewById(R.id.disk_info);
		findViewById(R.id.download).setOnClickListener(this);
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
		View addRssLayout = inflater.inflate(R.layout.add_rss_dialog, null);
		addRssLayout.findViewById(R.id.add_rss).setOnClickListener(this);
		addRssLayout.findViewById(R.id.close).setOnClickListener(this);
		dir = (EditText) addRssLayout.findViewById(R.id.dir);
		dir.setText(share.getString("downloadDir", ""));
		addRssWindow = new PopupWindow(addRssLayout, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		addRssWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bottom_pop_up_window_bg));
		addRssWindow.setAnimationStyle(R.style.normalPopWindow_anim_style);
	}

	// public void refresh() {
	// refreshView.setRefreshing(false);
	// }

	private void refreshRss() {
		BaseAsyncTask<ArrayList<RutorrentRssLabel>> task = BaseAsyncTask
				.newInstance();
		attachRssTask(task);
		task.attach(rssCallback);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "get"));
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RSS_ACTION_URL, ip),
					params, new ResponseParser<ArrayList<RutorrentRssLabel>>() {

						@Override
						public ArrayList<RutorrentRssLabel> parse(
								HttpResponse res, InputStream in) {
							JsonParser parser = new JsonParser();
							JsonElement element = parser
									.parse(new InputStreamReader(in));
							JsonArray arr = element.getAsJsonObject()
									.getAsJsonArray("list");
							Gson gson = new Gson();
							ArrayList<RutorrentRssLabel> result = gson
									.fromJson(
											arr,
											new TypeToken<ArrayList<RutorrentRssLabel>>() {
											}.getType());
							msgId = SUCCESS_MSG_ID;
							return result;
						}
					});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	void fetch() {
		if (mTask != null) {
			return;
		}
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = BaseAsyncTask
				.newInstance();
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
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					params, new ResponseParser<ArrayList<RemoteTaskInfo>>() {

						@Override
						public ArrayList<RemoteTaskInfo> parse(
								HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 401) {
								Intent intent = new Intent(RemoteActivity.this,
										RemoteLoginActivity.class);
								startActivity(intent);
								finish();
								return null;
							}
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
								info.open = arr.get(0).getAsInt();
								info.state = arr.get(3).getAsInt();
								info.title = arr.get(4).toString();
								info.size = arr.get(5).getAsLong();
								info.completeSize = arr.get(8).getAsLong();
								info.uploaded = arr.get(9).getAsLong();
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

	private List<NameValuePair> buildParams(String mode) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", mode));
		for (int i = 0; i < list.size(); i++) {
			if (selected[i]) {
				params.add(new BasicNameValuePair("hash", list.get(i).hash));
			}
		}
		return params;
	}

	/**
	 * 开始下载任务
	 */
	private void start() {
		if (selectedCount == 0) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		dialog = new CustomDialog(this, R.string.connecting);
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(processCallback);
		attachTask(task);
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					buildParams("start"), new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					task.detach();
				}
			});
			dialog.show();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停下载任务
	 */
	private void pause() {
		if (selectedCount == 0) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		dialog = new CustomDialog(this, R.string.connecting);
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(processCallback);
		attachTask(task);
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					buildParams("pause"), new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					task.detach();
				}
			});
			dialog.show();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 停止下载任务
	 */
	private void stop() {
		if (selectedCount == 0) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		dialog = new CustomDialog(this, R.string.connecting);
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(processCallback);
		attachTask(task);
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					buildParams("stop"), new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					task.detach();
				}
			});
			dialog.show();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除下载任务和文件
	 */
	private void delete() {
		if (selectedCount == 0) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		dialog = new CustomDialog(this, R.string.connecting);
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(processCallback);
		attachTask(task);
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					buildParams("remove"), new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					task.detach();
				}
			});
			dialog.show();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 展示下载确认窗口
	 */
	private void showDownloadWindow() {
		if (selectedRss == null) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		int i = selectedRss.length - 1;
		for (; i >= 0; i--) {
			if (selectedRss[i]) {
				break;
			}
		}
		if (i < 0) {
			Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		addRssWindow.update();
		addRssWindow.showAtLocation(root, Gravity.CENTER, 0, 0);
	}

	/**
	 * 从rss中添加下载任务
	 */
	private void download() {
		dialog = new CustomDialog(this, R.string.connecting);
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(new TaskCallback<Boolean>() {

			@Override
			public void onComplete(Boolean result) {
				dialog.dismiss();
				refreshExpandableView.setRefreshing(false);
				refreshDiskInfo();
			}

			@Override
			public void onCancel() {
				dialog.dismiss();
			}

			@Override
			public void onFail(Integer msgId) {
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), msgId,
						Toast.LENGTH_SHORT).show();
			}
		});
		attachRssTask(task);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "loadtorrents"));
		Editor editor = getSharedPreferences(Const.RUTORRENT_SHARED_PREFS,
				MODE_PRIVATE).edit();
		editor.putString("downloadDir", dir.getText().toString());
		editor.commit();
		params.add(new BasicNameValuePair("dir_edit", dir.getText().toString()));
		RutorrentRssLabel label = rssList.get(group);
		params.add(new BasicNameValuePair("rss", label.hash));
		for (int i = 0; i < label.items.size(); i++) {
			if (selectedRss[i]) {
				params.add(new BasicNameValuePair("url",
						label.items.get(i).href));
			}
		}
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RSS_ACTION_URL, ip),
					params, new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					task.detach();
				}
			});
			dialog.show();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 刷新选中订阅
	 */
	private void refreshRssLabel() {
		final BaseAsyncTask<ArrayList<RutorrentRssLabel>> task = new BaseAsyncTask<ArrayList<RutorrentRssLabel>>();
		task.attach(rssCallback);
		attachRssTask(task);
		task.execGet(
				String.format(Const.Urls.RUTORRENT_RSS_REFRESH_URL, ip,
						rssList.get(refreshingLabel).hash),
				new ResponseParser<ArrayList<RutorrentRssLabel>>() {

					@Override
					public ArrayList<RutorrentRssLabel> parse(HttpResponse res,
							InputStream in) {
						JsonParser parser = new JsonParser();
						JsonElement element = parser
								.parse(new InputStreamReader(in));
						JsonArray arr = element.getAsJsonObject()
								.getAsJsonArray("list");
						Gson gson = new Gson();
						ArrayList<RutorrentRssLabel> result = gson.fromJson(
								arr,
								new TypeToken<ArrayList<RutorrentRssLabel>>() {
								}.getType());
						msgId = SUCCESS_MSG_ID;
						return result;
					}
				});
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
		refreshDiskInfoBtn.setEnabled(false);
		if (diskTask != null) {
			diskTask.detach();
		}
		diskTask = new BaseAsyncTask<long[]>();
		diskTask.attach(diskCallback);
		diskTask.execGet(String.format(Const.Urls.RUTORRENT_DISK_SPACE_URL, ip)
				+ System.currentTimeMillis(), new ResponseParser<long[]>(
				R.string.get_disk_space_failed) {

			@Override
			public long[] parse(HttpResponse res, InputStream in) {
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(new InputStreamReader(in))
						.getAsJsonObject();
				long[] space = new long[2];
				space[0] = obj.get("total").getAsLong();
				space[1] = obj.get("free").getAsLong();
				msgId = SUCCESS_MSG_ID;
				return space;
			}
		});
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
			Toast.makeText(RemoteActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
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

	private TaskCallback<ArrayList<RutorrentRssLabel>> rssCallback = new TaskCallback<ArrayList<RutorrentRssLabel>>() {

		@Override
		public void onComplete(ArrayList<RutorrentRssLabel> result) {
			refreshExpandableView.onRefreshComplete();
			rssList.clear();
			rssList.addAll(result);
			refreshingLabel = -1;
			rssAdapter.notifyDataSetChanged();
		}

		@Override
		public void onCancel() {
			refreshExpandableView.onRefreshComplete();
		}

		@Override
		public void onFail(Integer msgId) {
			refreshExpandableView.onRefreshComplete();
			Toast.makeText(RemoteActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
		}
	};

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
			Toast.makeText(RemoteActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
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

	private static class RssItemViewHolder {
		private CheckBox check;
		private TextView title;
		private RssOnCheckedChangeListener listener;

		RssItemViewHolder(View v) {
			check = (CheckBox) v.findViewById(R.id.rss_check);
			title = (TextView) v.findViewById(R.id.rss_title);
		}
	}

	private static class RssLabelViewHolder {
		private ImageView expand;
		private TextView label;
		private ProgressBar progress;
		private Button refresh;

		RssLabelViewHolder(View v) {
			expand = (ImageView) v.findViewById(R.id.expand);
			label = (TextView) v.findViewById(R.id.label);
			progress = (ProgressBar) v.findViewById(R.id.progress);
			refresh = (Button) v.findViewById(R.id.refresh_rss_label);
		}
	}

	private class RssOnCheckedChangeListener implements OnCheckedChangeListener {
		int child;

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			selectedRss[child] = isChecked;
		}
	}

	private class RssAdapter extends BaseExpandableListAdapter {

		RssAdapter() {
		}

		@Override
		public int getGroupCount() {
			return rssList == null ? 0 : rssList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return rssList.get(groupPosition).items.size();
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
			RssLabelViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rss_label, null);
				holder = new RssLabelViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (RssLabelViewHolder) convertView.getTag();
			}
			RutorrentRssLabel label = rssList.get(groupPosition);
			holder.label.setText(label.label + "(" + label.items.size() + ")");
			if (refreshingLabel == -1) {
				holder.progress.setVisibility(View.INVISIBLE);
				holder.refresh.setEnabled(true);
			} else if (refreshingLabel == groupPosition) {
				holder.progress.setVisibility(View.VISIBLE);
				holder.refresh.setEnabled(false);
			} else {
				holder.progress.setVisibility(View.INVISIBLE);
				holder.refresh.setEnabled(false);
			}
			if (isExpanded) {
				holder.expand.setImageResource(R.drawable.arrow_expand);
			} else {
				holder.expand.setImageResource(R.drawable.arrow_collapse);
			}
			holder.refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					refreshingLabel = groupPosition;
					notifyDataSetChanged();
					refreshExpandableView.setRefreshing(true);
				}
			});
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			RssItemViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rss_item, null);
				holder = new RssItemViewHolder(convertView);
				holder.listener = new RssOnCheckedChangeListener();
				convertView.setTag(holder);
			} else {
				holder = (RssItemViewHolder) convertView.getTag();
			}
			RutorrentRssItem item = rssList.get(groupPosition).items
					.get(childPosition);
			holder.title.setText(item.title);
			holder.check.setOnCheckedChangeListener(null);
			holder.check.setChecked(selectedRss[childPosition]);
			holder.listener.child = childPosition;
			holder.check.setOnCheckedChangeListener(holder.listener);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	};
}
