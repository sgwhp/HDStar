package org.hdstar.widget.fragment;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.RssSettingManager;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.RssSetting;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RssListFragment extends StackFragment implements OnClickListener {
	private LinearLayout mContainer;
	private ArrayList<RssSetting> settings;
	private static final int ID = 10000;
	private LayoutInflater mInflater;

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

	private void init() {
		settings = RssSettingManager.getAll(getActivity());
		if (settings.size() == 0) {
			return;
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
		RssSetting setting;
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
	public void initActionBar(Menu menu) {
		MenuItem item = menu.add(0, Menu.FIRST, 0, R.string.add);
		item.setIcon(R.drawable.add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public void onActionBarClick(int MenuItemId) {
		push(RssSettingFragment.newInstance(RssSettingFragment.MODE_ADD, null));
	}

	private View createChild(Context context, RssSetting setting, int id) {
		View v = mInflater.inflate(R.layout.rss_setting_item, null);
		TextView text = (TextView) v.findViewById(R.id.rss_label);
		text.setText(setting.label);
		text = (TextView) v.findViewById(R.id.rss_link);
		text.setText("http://" + setting.link);
		ImageView icon = (ImageView) v.findViewById(R.id.rss_icon);
		ImageLoader.getInstance().displayImage(
				String.format(Const.Urls.GETFVO_URL, setting.link), icon,
				HDStarApp.displayOptions);
		v.setId(id);
		v.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		push(RssSettingFragment.newInstance(RssSettingFragment.MODE_EDIT, settings.get(v.getId() - ID)));
	}
}
