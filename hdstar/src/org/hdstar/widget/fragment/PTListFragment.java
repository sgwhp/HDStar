package org.hdstar.widget.fragment;

import java.util.ArrayList;

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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PTListFragment extends StackFragment implements OnClickListener {
	private LinearLayout mContainer;
	private static final int ID = 10000;
	private LayoutInflater mInflater;
	private ArrayList<PTSiteSetting> settings;

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
		init();
	}

	@Override
	public void refresh() {
		mContainer.removeAllViews();
		init();
	}

	@Override
	public void onCreateActionBar(Menu menu) {
		MenuItem item = menu.add(0, R.id.ab_add_pt_site_setting, 0,
				R.string.add);
		item.setIcon(R.drawable.add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public boolean onActionBarSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ab_add_pt_site_setting:
			push(PTSiteSettingFragment.newInstance(
					PTSiteSettingFragment.MODE_ADD, null));
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		push(PTSiteSettingFragment.newInstance(PTSiteSettingFragment.MODE_EDIT,
				settings.get(v.getId() - ID)));
	}

	private void init() {
		Context context = getActivity();
		settings = PTSiteSettingManager.getAll(context);
		if (settings.size() == 0) {
			return;
		}
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
		PTSiteSetting setting;
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

	private View createChild(Context context, PTSiteSetting setting, int id) {
		View v = mInflater.inflate(R.layout.pt_site_setting_item, null);
		TextView text = (TextView) v.findViewById(R.id.site_label);
		text.setText(setting.type);
		text = (TextView) v.findViewById(R.id.username);
		text.setText(setting.username);
		ImageView icon = (ImageView) v.findViewById(R.id.site_icon);
		// »ñÈ¡Í¼±ê
		ImageLoader.getInstance().displayImage(
				String.format(CommonUrls.GETFVO_URL, "http://"
						+ PTSiteType.getByName(setting.type).getUrl()), icon,
				HDStarApp.displayOptions);
		v.setId(id);
		v.setOnClickListener(this);
		return v;
	}
}
