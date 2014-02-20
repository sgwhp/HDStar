package org.hdstar.widget.fragment;

import java.util.HashMap;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.common.PTSiteType;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.PTSiteSetting;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class PTListFragment extends StackFragment implements OnClickListener {
	private LinearLayout mContainer;
	private static final int ID = 10000;
	private LayoutInflater mInflater;
	private HashMap<String, PTSiteSetting> settings;
	private PTSiteType[] mTypes;
	{
		// ��ʼ������վ�㣬�ų���HDSky
		PTSiteType[] types = PTSiteType.values();
		for (int i = 0, j = 0; i < types.length; i++) {
			if (types[i] != PTSiteType.HDSky) {
				mTypes[j++] = types[i];
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		View v = inflater.inflate(R.layout.scroll_list, null);
		mContainer = (LinearLayout) v.findViewById(R.id.container);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// init();
	}

	@Override
	public void refresh() {
		mContainer.removeAllViews();
		init();
	}

	@Override
	public void onSelected() {
		init();
	}

	// @Override
	// public void onCreateActionBar(Menu menu) {
	// MenuItem item = menu.add(0, R.id.ab_add_pt_site_setting, 0,
	// R.string.add);
	// item.setIcon(R.drawable.add);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
	// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }
	//
	// @Override
	// public boolean onActionBarSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.ab_add_pt_site_setting:
	// push(PTSiteSettingFragment.newInstance(
	// PTSiteSettingFragment.MODE_ADD, null));
	// return true;
	// }
	// return false;
	// }

	@Override
	public void onClick(View v) {
		PTSiteSetting setting = settings.get(mTypes[v.getId() - ID].name());
		if (setting != null) {
			push(PTSiteSettingFragment.newInstance(setting));
		} else {
			push(PTSiteSettingFragment.newInstance(mTypes[v.getId() - ID]
					.name()));
		}
	}

	private void init() {
		if (mTypes.length == 0) {
			return;
		}
		Context context = getActivity();
		settings = PTSiteSettingManager.getAsMap(context);
		View v;
		String username;
		String uninitialized = getString(R.string.unintialized);
		if (mTypes.length == 1) {
			username = settings.containsKey(mTypes[0].name()) ? settings
					.get(mTypes[0].name()).username : uninitialized;
			v = createChild(context, mTypes[0], username, ID);
			v.setBackgroundResource(R.drawable.setting_strip_bg_sel);
			mContainer.addView(v);
			return;
		}
		username = settings.containsKey(mTypes[0].name()) ? settings
				.get(mTypes[0].name()).username : uninitialized;
		v = createChild(context, mTypes[0], username, ID);
		v.setBackgroundResource(R.drawable.setting_strip_top_sel);
		mContainer.addView(v);
		for (int i = 1; i < mTypes.length - 1; i++) {
			username = settings.containsKey(mTypes[i].name()) ? settings
					.get(mTypes[i].name()).username : uninitialized;
			v = createChild(context, mTypes[i], username, ID + i);
			v.setBackgroundResource(R.drawable.setting_strip_middle_sel);
			mContainer.addView(v);
		}
		username = settings.containsKey(mTypes[mTypes.length - 1].name()) ? settings
				.get(mTypes[mTypes.length - 1].name()).username : uninitialized;
		v = createChild(context, mTypes[mTypes.length - 1], username, ID
				+ mTypes.length - 1);
		v.setBackgroundResource(R.drawable.setting_strip_bottom_sel);
		mContainer.addView(v);
	}

	private View createChild(Context context, PTSiteType type, String username,
			int id) {
		View v = mInflater.inflate(R.layout.pt_site_setting_item, null);
		TextView text = (TextView) v.findViewById(R.id.site_label);
		text.setText(type.getName());
		text = (TextView) v.findViewById(R.id.username);
		text.setText(username);
		ImageView icon = (ImageView) v.findViewById(R.id.site_icon);
		// ��ȡͼ��
		ImageLoader.getInstance().displayImage(
				String.format(CommonUrls.GETFVO_URL, type.getUrl()), icon,
				HDStarApp.displayOptions);
		v.setId(id);
		v.setOnClickListener(this);
		return v;
	}
}
