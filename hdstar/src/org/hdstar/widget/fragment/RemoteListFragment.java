package org.hdstar.widget.fragment;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.RemoteSettingManager;
import org.hdstar.model.RemoteSetting;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * 远程控制设置列表
 * @author robust
 *
 */
public class RemoteListFragment extends StackFragment implements
		OnClickListener {
	private LinearLayout mContainer;
	private ArrayList<RemoteSetting> settings;
	private static final int ID = 10000;
	private LayoutInflater mInflater;
	private View noData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		View v = inflater.inflate(R.layout.scroll_list, null);
		mContainer = (LinearLayout) v.findViewById(R.id.container);
		noData = v.findViewById(R.id.no_data);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	@Override
	public void refresh() {
		mContainer.removeAllViews();
		init();
	}

	private void init() {
		settings = RemoteSettingManager.getAll(getActivity());
		if (settings.size() == 0) {
			noData.setVisibility(View.VISIBLE);
			return;
		} else {
			noData.setVisibility(View.GONE);
		}
		Context context = getActivity();
		View v;
		if (settings.size() == 1) {
			v = createChild(context, settings.get(0), ID);
			v.setBackgroundResource(R.drawable.setting_strip_bg_sel);
			mContainer.addView(v);
			return;
		}
		v = createChild(context, settings.get(0), ID);
		v.setBackgroundResource(R.drawable.setting_strip_top_sel);
		mContainer.addView(v);
		RemoteSetting setting;
		for (int i = 1; i < settings.size() - 1; i++) {
			setting = settings.get(i);
			v = createChild(context, setting, ID + i);
			v.setBackgroundResource(R.drawable.setting_strip_middle_sel);
			mContainer.addView(v);
		}
		v = createChild(context, settings.get(settings.size() - 1), ID
				+ settings.size() - 1);
		v.setBackgroundResource(R.drawable.setting_strip_bottom_sel);
		mContainer.addView(v);
	}

	@Override
	public void onCreateActionBar(Menu menu) {
		MenuItem item = menu
				.add(0, R.id.ab_add_remote_setting, 0, R.string.add);
		item.setIcon(R.drawable.add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar()
		.setSubtitle(R.string.remote);
	}

	@Override
	public boolean onActionBarSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ab_add_remote_setting:
			push(RemoteSettingFragment.newInstance(
					RemoteSettingFragment.MODE_ADD, null));
			return true;
		}
		return false;
	}

	private View createChild(Context context, RemoteSetting setting, int id) {
		View v = mInflater.inflate(R.layout.remote_setting_item, null);
		TextView text = (TextView) v.findViewById(R.id.name);
		text.setText(setting.name);
		text = (TextView) v.findViewById(R.id.ip);
		text.setText("http://" + setting.ip);
		ImageView icon = (ImageView) v.findViewById(R.id.remote_icon);
		int iconId = -1;
		switch (RemoteSetting.getRemoteType(setting.type)) {
		case RuTorrent:
			iconId = R.drawable.rtorrent_icon;
			break;
		case UTorrent:
			iconId = R.drawable.utorrent_icon;
			break;
		case Transmission:
			iconId = R.drawable.transmission_icon;
			break;
		}
		if (iconId != -1) {
			icon.setImageResource(iconId);
		}
		v.setId(id);
		v.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		push(RemoteSettingFragment.newInstance(RemoteSettingFragment.MODE_EDIT,
				settings.get(v.getId() - ID)));
	}

}
