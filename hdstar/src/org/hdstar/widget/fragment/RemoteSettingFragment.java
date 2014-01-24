package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.RemoteSettingManager;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteSetting;
import org.hdstar.util.Util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RemoteSettingFragment extends StackFragment {
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;

	private int mMode;
	private RemoteSetting setting;
	private String[] typeStr;
	private Spinner typeSpn;
	private EditText name;
	private EditText ip;
	private EditText username;
	private EditText password;
	private EditText downloadDir;
	private Button remove;
	private ToggleButton setDefault;

	public static RemoteSettingFragment newInstance(int mode,
			RemoteSetting setting) {
		RemoteSettingFragment f = new RemoteSettingFragment();
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.remote_setting, null);
		typeSpn = (Spinner) v.findViewById(R.id.remote_type);
		name = (EditText) v.findViewById(R.id.remote_name);
		ip = (EditText) v.findViewById(R.id.ip);
		username = (EditText) v.findViewById(R.id.username);
		password = (EditText) v.findViewById(R.id.password);
		downloadDir = (EditText) v.findViewById(R.id.downloadDir);
		remove = (Button) v.findViewById(R.id.remove);
		setDefault = (ToggleButton) v.findViewById(R.id.set_as_defalut);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		Bundle b = getArguments();
		mMode = b.getInt("mode");
		if (mMode == MODE_EDIT) {
			setting = b.getParcelable("setting");
		} else {
			setting = new RemoteSetting();
			setting.order = RemoteSettingManager
					.getMaxRemote(RemoteSettingManager.getPrefs(getActivity()));
		}
		typeStr = getResources().getStringArray(R.array.remoteClient);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, typeStr);
		typeSpn.setAdapter(adapter);
		if (mMode == MODE_EDIT) {
			name.setText(setting.name);
			ip.setText(setting.ip);
			username.setText(setting.username);
			password.setText(setting.password);
			downloadDir.setText(setting.downloadDir);
			remove.setVisibility(View.VISIBLE);
			remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					RemoteSettingManager.removeRemoteSettings(getActivity(),
							setting.order);
					getViewPager().setCurrentItem(
							getViewPager().getCurrentItem() - 1, true);
				}
			});
			for (RemoteType type : RemoteType.values()) {
				if (type.name().equals(setting.type)) {
					typeSpn.setSelection(type.ordinal());
					break;
				}
			}
		}
		typeSpn.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String typeName = typeStr[position];
				if (name.getText().toString().equals("")) {
					name.setText(typeName);
				}
				for (RemoteType type : RemoteType.values()) {
					if (type.getName().equals(typeName)) {
						setting.type = type.name();
						return;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		setDefault.setChecked(setting.order == RemoteSettingManager
				.getDefault(getActivity()));
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
		String nameStr = name.getText().toString();
		String ipStr = ip.getText().toString();
		if (!Util.isIp(ipStr)) {
			Crouton.makeText(getActivity(), R.string.invalidate_ip,
					Style.CONFIRM).show();
			return;
		}
		String usernameStr = username.getText().toString();
		String passwordStr = password.getText().toString();
		String dir = downloadDir.getText().toString();
		if ("".equals(nameStr) || "".equals(usernameStr)
				|| "".equals(passwordStr)) {
			Crouton.makeText(getActivity(), R.string.fill_in_the_blanks,
					Style.CONFIRM).show();
			return;
		}
		setting.name = nameStr;
		setting.ip = ipStr;
		setting.username = usernameStr;
		setting.password = passwordStr;
		setting.downloadDir = dir;
		if (mMode == MODE_ADD) {
			RemoteSettingManager.add(getActivity(), setting,
					setDefault.isChecked());
		} else {
			RemoteSettingManager.save(getActivity(), setting,
					setDefault.isChecked());
		}
		StackFragment f = getStackAdapter().preItem();
		getViewPager()
				.setCurrentItem(getViewPager().getCurrentItem() - 1, true);
		if (f != null) {
			f.refresh();
		}
		Crouton.makeText(getActivity(), R.string.saved, Style.INFO).show();
	}
}
