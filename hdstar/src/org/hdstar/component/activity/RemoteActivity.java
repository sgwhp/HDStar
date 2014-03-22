package org.hdstar.component.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.RemoteSettingManager;
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
import org.hdstar.task.TaskStatus;
import org.hdstar.task.parser.ResponseParser;
import org.hdstar.util.RssHandler;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.TextProgressBar;
import org.hdstar.widget.navigation.FilterListDropDownAdapter;
import org.hdstar.widget.navigation.Label;
import org.hdstar.widget.navigation.NavigationFilter;
import org.hdstar.widget.navigation.SimpleListItem;
import org.hdstar.widget.navigation.StatusType;
import org.xml.sax.SAXException;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.CancelableHeaderTransformer.OnCancelListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Mode;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
import android.widget.Spinner;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpResponse;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.slidingmenu.lib.SlidingMenu;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 远程控制. <br/>
 * 
 * @author robust
 */
public class RemoteActivity extends BaseActivity implements
		OnNavigationListener, OnClickListener {
	/** 下拉导航 */
	protected FilterListDropDownAdapter navigationSpinnerAdapter = null;
	// private int skipNextOnNavigationItemSelectedCalls = 2;
	/** 当前过滤模式 */
	protected NavigationFilter currentFilter = null;
	private PullToRefreshLayout mPullToRefreshLayout;
	private View root;
	/** 占位，防止操作按钮窗口遮挡任务列表，无特殊意义 */
	private View empty;
	private View label, start, pause, stop, delete;
	private ListView listView;
	private ExpandableListView rssListView;
	private View noData;
	private RemoteTaskAdapter adapter;
	private RssAdapter rssAdapter;
	private ArrayList<RemoteTaskInfo> taskList = new ArrayList<RemoteTaskInfo>();
	private ArrayList<RemoteTaskInfo> filterList = new ArrayList<RemoteTaskInfo>();
	private boolean[] selected;
	private RssItem selectedRssItem;
	/** 选中的下载任务数 */
	private int selectedCount;
	private PopupWindow window = null;
	/** 下载确认窗口 */
	private PopupWindow addTorrentWindow;
	/** 下载目录 */
	private EditText dirEt;
	/** 设置标签窗口 */
	private PopupWindow setLabelWindow;
	/** 标签文本框 */
	private EditText labelEt;
	/** 标签选择 */
	private Spinner labelSp;
	private LinearLayout ctrlBox;
	private CustomDialog dialog = null;
	private BaseAsyncTask<?> mTask;
	private BaseAsyncTask<?> rssTask;
	private LayoutInflater inflater;
	private TextProgressBar disk;
	private Button refreshDiskInfoBtn;
	private BaseAsyncTask<long[]> diskTask;
	private RemoteBase remote;
	private ArrayList<RemoteSetting> settings;
	private RemoteSetting setting;
	private ArrayList<RssSetting> rssSettings;
	private SparseArray<RssChannel> rssChannels = new SparseArray<RssChannel>();
	private TaskStatus[] rssStatus;

	// /** 是否已登录 */
	// private boolean login;

	public RemoteActivity() {
		super(R.string.remote);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		setContentView(R.layout.remote_layout);
		getSlidingMenu().setSecondaryMenu(R.layout.rss_list);

		root = findViewById(R.id.task_list_content);
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
		empty = findViewById(R.id.empty);

		noData = findViewById(R.id.no_data);
		rssListView = (ExpandableListView) findViewById(R.id.rss_list);

		inflater = LayoutInflater.from(this);
		settings = RemoteSettingManager.getAll(this);
		init();
		// Context context = getSupportActionBar().getThemedContext();
		if (settings.size() > 0) {
			String[] servers = new String[settings.size()];
			for (int i = 0; i < settings.size(); i++) {
				servers[i] = settings.get(i).name;
			}
			// ArrayAdapter<CharSequence> list = new ArrayAdapter<CharSequence>(
			// context, R.layout.sherlock_spinner_item, servers);
			// list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			navigationSpinnerAdapter = new FilterListDropDownAdapter(this);
			navigationSpinnerAdapter.updateServers(settings);
			// Add status types directly to the action bar spinner
			navigationSpinnerAdapter.updateStatusTypes(StatusType
					.getAllStatusTypes(this));
			// Add an empty labels list (which will be updated later, but the
			// adapter needs to be created now)
			navigationSpinnerAdapter.updateLabels(new ArrayList<Label>());
			currentFilter = StatusType.getShowAllType(this);
			ActionBar actionbar = getSupportActionBar();
			actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			navigationSpinnerAdapter.updateCurrentFilter(currentFilter);
			actionbar
					.setListNavigationCallbacks(navigationSpinnerAdapter, this);
			actionbar.setSelectedNavigationItem(RemoteSettingManager
					.getDefault(this) + 1);
		} else {
			new AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(R.string.no_remote_setting)
					.setPositiveButton(R.string.add,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RemoteActivity.this,
											SettingActivity.class);
									intent.putExtra("action", SettingActivity.ACTION_ADD_REMOTE_SETTING);
									startActivity(intent);
									finish();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
			return;
		}
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
		case R.id.label:
			showSetLabelWindow();
			break;
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
					.setNegativeButton(android.R.string.cancel,
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
		case R.id.close_label_dialog:
			setLabelWindow.dismiss();
			break;
		case R.id.set_label_btn:
			setLabel();
			break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// if (skipNextOnNavigationItemSelectedCalls > 0) {
		// skipNextOnNavigationItemSelectedCalls--;
		// return false;
		// }
		Object item = navigationSpinnerAdapter.getItem(itemPosition);
		if (item instanceof SimpleListItem) {
			// A filter item was selected form the navigation spinner
			filterSelected((SimpleListItem) item, false);
			return true;
		}
		// A header was selected; no action
		return false;
	}

	/**
	 * A new filter was selected; update the view over the current data
	 * 
	 * @param item
	 *            The touched filter item
	 * @param forceNewConnection
	 *            Whether a new connection should be initialised regardless of
	 *            the old server selection
	 */
	protected void filterSelected(SimpleListItem item,
			boolean forceNewConnection) {

		// Server selection
		if (item instanceof RemoteSetting) {
			setting = (RemoteSetting) item;
			navigationSpinnerAdapter.updateCurrentServer(setting);

			detachTask();
			mPullToRefreshLayout.setRefreshComplete();
			// login = false;
			taskList.clear();
			filterList.clear();
			adapter.notifyDataSetChanged();
			// 切换服务器时，取消状态过滤
			currentFilter = StatusType.getShowAllType(this);
			navigationSpinnerAdapter.updateCurrentFilter(currentFilter);
			remote = RemoteFactory.newInstanceByName(setting.type);
			remote.setSetting(setting);
			dirEt.setText(setting.downloadDir);

			// 刷新打标签按钮
			if (remote.setLabelEnabled()) {
				label.setVisibility(View.VISIBLE);
			} else {
				label.setVisibility(View.GONE);
			}

			// 刷新磁盘信息界面
			if (remote.diskEnable()) {
				findViewById(R.id.disk_info).setVisibility(View.VISIBLE);
				findViewById(R.id.refresh_disk_info).setOnClickListener(this);
				refreshDiskInfoBtn = (Button) findViewById(R.id.refresh_disk_info);
				disk = (TextProgressBar) findViewById(R.id.disk_size);
			} else {
				findViewById(R.id.disk_info).setVisibility(View.GONE);
			}

			mPullToRefreshLayout.setRefreshing(true);
			doRefresh();
			refreshDiskInfo();

			// Update connection to the newly selected server and refresh
			return;

		}
		// Status type or label selection - both of which are navigation filters
		if (item instanceof NavigationFilter) {
			currentFilter = (NavigationFilter) item;
			navigationSpinnerAdapter.updateCurrentFilter(currentFilter);
			applyFilter();
		}

	}

	protected void init() {
		rssSettings = RssSettingManager.getAll(this);
		if(rssSettings.size() == 0){
			noData.setVisibility(View.VISIBLE);
		} else {
			noData.setVisibility(View.GONE);
		}
		rssStatus = new TaskStatus[rssSettings.size()];
		for (int i = rssSettings.size() - 1; i >= 0; i--) {
			rssStatus[i] = TaskStatus.Normal;
		}
		listView = (ListView) findViewById(R.id.task_list);
		adapter = new RemoteTaskAdapter();
		listView.setAdapter(adapter);
		CancelableHeaderTransformer transformer = new CancelableHeaderTransformer();
		transformer.setFromEndLabel(
				getString(R.string.pull_up_to_refresh_pull_label),
				getString(R.string.pull_to_refresh_release_label));
		ActionBarPullToRefresh
				.from(this)
				.options(
						Options.create().refreshOnUp(true).mode(Mode.BOTH)
								.headerLayout(R.layout.cancelable_header)
								.headerTransformer(transformer).build())
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(R.id.task_list)
				.listener(new OnRefreshListener() {

					@Override
					public void onRefreshStarted(View view) {
						doRefresh();
					}
				}).setup(mPullToRefreshLayout);

		transformer.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {
				mPullToRefreshLayout.setRefreshComplete();
				detachTask();
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
		// 初始化下载任务控制弹出窗口
		ctrlBox = (LinearLayout) inflater.inflate(
				R.layout.remote_task_ctrl_layout, null);
		label = ctrlBox.findViewById(R.id.label);
		label.setOnClickListener(this);
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
				R.drawable.pop_up_window_bottom_bg));
		window.setAnimationStyle(R.style.task_ctrl_box_anim_style);
		// 初始化rss下载弹出窗口
		View addTorrentLayout = inflater.inflate(R.layout.add_torrent_dialog,
				null);
		addTorrentLayout.findViewById(R.id.add_torrent)
				.setOnClickListener(this);
		addTorrentLayout.findViewById(R.id.close).setOnClickListener(this);
		dirEt = (EditText) addTorrentLayout.findViewById(R.id.download_dir);
		addTorrentWindow = new PopupWindow(addTorrentLayout,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		addTorrentWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_up_window_bg));
		addTorrentWindow.setAnimationStyle(R.style.normalPopWindow_anim_style);
		// 初始化设置标签窗口
		View setLabelLayout = inflater.inflate(R.layout.set_label_dialog, null);
		setLabelLayout.findViewById(R.id.set_label_btn)
				.setOnClickListener(this);
		setLabelLayout.findViewById(R.id.close_label_dialog)
				.setOnClickListener(this);
		labelEt = (EditText) setLabelLayout.findViewById(R.id.label);
		labelSp = (Spinner) setLabelLayout.findViewById(R.id.labels_for_choose);
		setLabelWindow = new PopupWindow(setLabelLayout,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		setLabelWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_up_window_bg));
		setLabelWindow.setAnimationStyle(R.style.normalPopWindow_anim_style);
	}

	/**
	 * 
	 * TODO 刷新某个rss订阅. <br/>
	 * 
	 * @param refreshingLabel
	 *            该订阅的位置
	 */
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
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = remote.fetchList();
		task.attach(mCallback);
		attachTask(task);
		BaseAsyncTask.commit(task);
	}

	private String[] selectedHashes() {
		String[] hashes = new String[selectedCount];
		for (int i = 0, j = 0; i < filterList.size(); i++) {
			if (selected[i]) {
				hashes[j++] = filterList.get(i).hash;
			}
		}
		return hashes;
	}

	/**
	 * 
	 * 显示打标签窗口. <br/>
	 */
	private void showSetLabelWindow() {
		List<org.hdstar.model.Label> labels = remote.getLabels();
		final String[] labelsStr = new String[labels.size()];
		for (int i = labels.size() - 1; i >= 0; i--) {
			labelsStr[i] = labels.get(i).getName();
		}
		labelSp.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, labelsStr));
		labelSp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				labelEt.setText(labelsStr[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		setLabelWindow.update();
		setLabelWindow.showAtLocation(root, Gravity.CENTER, 0, 0);
	}

	/**
	 * 打标签
	 */
	private void setLabel() {
		setLabelWindow.dismiss();
		final BaseAsyncTask<Boolean> task = remote.setLabel(labelEt.getText()
				.toString(), selectedHashes());
		if (task == null) {
			return;
		}
		task.attach(processCallback);
		attachTask(task);
		BaseAsyncTask.commit(task);
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
	 * 
	 * 开始任务. <br/>
	 */
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
		BaseAsyncTask.commit(task);
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
	 * 
	 * 暂停任务. <br/>
	 */
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
		BaseAsyncTask.commit(task);
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
	 * 
	 * 停止任务. <br/>
	 */
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
		BaseAsyncTask.commit(task);
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
	 * 
	 * 删除任务. <br/>
	 */
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
		BaseAsyncTask.commit(task);
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
		final BaseAsyncTask<Boolean> task = remote.addByUrl(dir,
				selectedRssItem.getTheLink());
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
		BaseAsyncTask.commit(task);
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
		// if (login) {
		// fetch();
		// } else {
		// BaseAsyncTask<Boolean> task = remote.login(setting.username,
		// setting.password);
		// task.attach(loginCallback);
		// BaseAsyncTask.commit(task);
		// attachTask(task);
		// }
	}

	/**
	 * 刷新硬盘信息
	 */
	private void refreshDiskInfo() {
		if (!remote.diskEnable()) {
			return;
		}
		refreshDiskInfoBtn.setEnabled(false);
		if (diskTask != null) {
			diskTask.detach();
		}
		diskTask = remote.getDiskInfo();
		diskTask.attach(diskCallback);
		BaseAsyncTask.commit(diskTask);
	}

	/**
	 * 
	 * 应用当前过滤模式 <br/>
	 */
	private void applyFilter() {
		filterList.clear();
		for (RemoteTaskInfo task : taskList) {
			if (currentFilter.matches(task, true)) {
				filterList.add(task);
			}
		}
		adapter.notifyDataSetChanged();
	}

	// private TaskCallback<Boolean> loginCallback = new TaskCallback<Boolean>()
	// {
	//
	// @Override
	// public void onComplete(Boolean result) {
	// login = true;
	// detachTask();
	// fetch();
	// }
	//
	// @Override
	// public void onCancel() {
	// mPullToRefreshLayout.setRefreshComplete();
	// }
	//
	// @Override
	// public void onFail(Integer msgId) {
	// mPullToRefreshLayout.setRefreshComplete();
	// Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT).show();
	// }
	// };

	/**
	 * 获取任务列表请求回调
	 */
	private TaskCallback<ArrayList<RemoteTaskInfo>> mCallback = new TaskCallback<ArrayList<RemoteTaskInfo>>() {

		@Override
		public void onComplete(ArrayList<RemoteTaskInfo> result) {
			mPullToRefreshLayout.setRefreshComplete();
			taskList.clear();
			SoundPoolManager.play(RemoteActivity.this);
			taskList.addAll(result);
			applyFilter();
			selected = new boolean[taskList.size()];
			selectedCount = 0;
			window.dismiss();
			empty.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			// update navigation label
			navigationSpinnerAdapter.updateLabels(Label
					.convertToNavigationLabels(remote.getLabels(),
							getString(R.string.unlabled)));
		}

		@Override
		public void onCancel() {
			mPullToRefreshLayout.setRefreshComplete();
		}

		@Override
		public void onFail(Integer msgId) {
			mPullToRefreshLayout.setRefreshComplete();
			Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT).show();
		}
	};

	/**
	 * 操作（开始、暂停等）任务请求回调
	 */
	private TaskCallback<Boolean> processCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			mPullToRefreshLayout.setRefreshing(true);
			doRefresh();
			dialog.dismiss();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

		@Override
		public void onFail(Integer msgId) {
			Crouton.makeText(RemoteActivity.this, msgId, Style.ALERT).show();
			dialog.dismiss();
		}

	};

	/**
	 * 
	 * 获取rss信息请求回调 <br/>
	 * 
	 * @author wuhongping
	 */
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

	/**
	 * 获取磁盘信息请求回调
	 */
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

	/**
	 * 
	 * 上下载任务列表适配器 <br/>
	 * 
	 * @author robust
	 */
	public class RemoteTaskAdapter extends BaseAdapter {

		private String taskInfo;

		public RemoteTaskAdapter() {
			selectedCount = 0;
			selected = new boolean[filterList.size()];
			taskInfo = getString(R.string.task_info);
		}

		@Override
		public int getCount() {
			return filterList == null ? 0 : filterList.size();
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
			RemoteTaskInfo item = filterList.get(position);
			holder.title.setText(item.title);
			if (item.progress == -1) {
				holder.progress
						.setProgress((int) (item.downloaded * 100.0 / item.size));
			} else {
				holder.progress.setProgress(item.progress);
			}
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
			switch (item.status) {
			case Seeding:
				holder.state.setImageResource(R.drawable.state_seeding);
				break;
			case Downloading:
				holder.state.setImageResource(R.drawable.state_leaching);
				break;
			case Paused:
				holder.state.setImageResource(R.drawable.state_pause);
				break;
			default:
				holder.state.setImageResource(R.drawable.state_stop);
			}
			return convertView;
		}

		public ArrayList<RemoteTaskInfo> getList() {
			return filterList;
		}

	}

	/**
	 * rss频道holder <br/>
	 * 
	 * @author robust
	 */
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

	/**
	 * 
	 * rss订阅列表适配器 <br/>
	 * 
	 * @author robust
	 */
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
					String.format(CommonUrls.GETFVO_URL,
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

	}
}
