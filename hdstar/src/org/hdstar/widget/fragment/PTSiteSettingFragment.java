package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.PTSiteSetting;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.FetchSecurityImgTask;

import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PTSiteSettingFragment extends StackFragment implements
		OnClickListener {
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;

	private PTSiteSetting setting;
	private Spinner typeSpn;
	private String[] typeStr;
	private int mMode;

	private EditText label;
	private EditText addr;
	private EditText username;
	private EditText password;
	private ImageView securityImg;
	private EditText securityCode;
	private Button refreshBtn;
	private Button initBtn;
	private Button removeBtn;
	private FetchSecurityImgTask imgTask;

	public static PTSiteSettingFragment newInstance(int mode,
			PTSiteSetting setting) {
		PTSiteSettingFragment fragment = new PTSiteSettingFragment();
		Bundle args = new Bundle();
		args.putInt("mode", mode);
		if (setting != null) {
			args.putParcelable("setting", setting);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		mMode = b.getInt("mode");
		if (mMode == MODE_EDIT) {
			setting = b.getParcelable("setting");
		} else {
			setting = new PTSiteSetting();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		detachImgTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pt_site_setting, null);
		typeSpn = (Spinner) v.findViewById(R.id.pt_type);
		label = (EditText) v.findViewById(R.id.site_label);
		addr = (EditText) v.findViewById(R.id.site_addr);
		username = (EditText) v.findViewById(R.id.username);
		password = (EditText) v.findViewById(R.id.password);
		securityImg = (ImageView) v.findViewById(R.id.security_image);
		securityCode = (EditText) v.findViewById(R.id.security_code);
		refreshBtn = (Button) v.findViewById(R.id.refresh);
		initBtn = (Button) v.findViewById(R.id.init);
		removeBtn = (Button) v.findViewById(R.id.remove);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		typeStr = PTSiteType.getAllNames();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, typeStr);
		typeSpn.setAdapter(adapter);
		if (mMode == MODE_EDIT) {
			label.setText(setting.label);
			addr.setText(setting.address);
			username.setText(setting.username);
			password.setText(setting.password);
			removeBtn.setVisibility(View.VISIBLE);
			removeBtn.setOnClickListener(this);
		}
		typeSpn.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String typeName = typeStr[position];
				for (PTSiteType type : PTSiteType.values()) {
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
		for (PTSiteType type : PTSiteType.values()) {
			if (type.name().equals(setting.type)) {
				typeSpn.setSelection(type.ordinal());
				break;
			}
		}
		refreshBtn.setOnClickListener(this);
		initBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.remove:
			PTSiteSettingManager.remove(getActivity(), setting.order);
			getViewPager().setCurrentItem(getViewPager().getCurrentItem() - 1,
					true);
			break;
		case R.id.refresh:
			detachImgTask();
			Toast.makeText(getActivity(), R.string.get_security_code,
					Toast.LENGTH_LONG).show();
			imgTask = new FetchSecurityImgTask();
			imgTask.attach(fetchSecurityImgCallback);
			imgTask.execGet(addr.getText().toString(), Bitmap.class);
			break;
		case R.id.init:
			break;
		}
	}

	@Override
	public void onCreateActionBar(Menu menu) {
		MenuItem item = menu.add(0, R.id.ab_save_remote_setting, 0,
				R.string.save);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	private void detachImgTask() {
		if (imgTask != null) {
			imgTask.detach();
		}
	}

	private TaskCallback<Bitmap> fetchSecurityImgCallback = new TaskCallback<Bitmap>() {

		@Override
		public void onComplete(Bitmap result) {
			securityImg.setImageBitmap(result);
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			Crouton.showText(getActivity(), msgId, Style.ALERT);
		}
	};
}
