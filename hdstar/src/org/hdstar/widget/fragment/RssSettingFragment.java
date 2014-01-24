package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.RemoteSettingManager;
import org.hdstar.common.RssSettingManager;
import org.hdstar.model.RssSetting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RssSettingFragment extends StackFragment implements
		OnClickListener {
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;

	private int mMode;
	private RssSetting setting;
	private EditText label;
	private EditText link;
	private Button remove;
	private Button scan;

	public static RssSettingFragment newInstance(int mode, RssSetting setting) {
		RssSettingFragment f = new RssSettingFragment();
		Bundle args = new Bundle();
		args.putInt("mode", mode);
		if (setting != null) {
			args.putParcelable("setting", setting);
		}
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		mMode = b.getInt("mode");
		if (mMode == MODE_EDIT) {
			setting = b.getParcelable("setting");
		} else {
			setting = new RssSetting();
			setting.order = RssSettingManager.getMaxRss(RssSettingManager
					.getPrefs(getActivity()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.rss_setting, null);
		label = (EditText) v.findViewById(R.id.rss_label);
		link = (EditText) v.findViewById(R.id.rss_link);
		remove = (Button) v.findViewById(R.id.remove);
		scan = (Button) v.findViewById(R.id.scan);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		if (mMode == MODE_EDIT) {
			label.setText(setting.label);
			link.setText(setting.link);
			remove.setVisibility(View.VISIBLE);
			remove.setOnClickListener(this);
		}
		scan.setOnClickListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case Activity.RESULT_OK:
			link.setText(data.getStringExtra(Intents.Scan.RESULT));
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(0, Menu.FIRST, 0, R.string.save);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			save();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void save() {
		String labelStr = label.getText().toString();
		String linkStr = link.getText().toString();
		if ("".equals(labelStr) || "".equals(linkStr)) {
			Crouton.makeText(getActivity(), R.string.fill_in_the_blanks,
					Style.CONFIRM).show();
			return;
		}
		setting.label = labelStr;
		setting.link = linkStr;
		if (mMode == MODE_ADD) {
			RssSettingManager.add(getActivity(), setting);
		} else {
			RssSettingManager.save(getActivity(), setting);
		}
		StackFragment f = getStackAdapter().preItem();
		getViewPager()
				.setCurrentItem(getViewPager().getCurrentItem() - 1, true);
		if (f != null) {
			f.refresh();
		}
		Crouton.makeText(getActivity(), R.string.saved, Style.INFO).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.remove:
			RemoteSettingManager.removeRemoteSettings(getActivity(),
					setting.order);
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() - 1,
					true);
			break;
		case R.id.scan:
			Intent intent = new Intent(getActivity(), CaptureActivity.class);
			startActivityForResult(intent, 1);
			break;
		}
	}

}
