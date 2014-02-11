package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.PTSiteSetting;
import org.hdstar.ptadapter.PTAdapter;
import org.hdstar.ptadapter.PTFactory;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.widget.CustomDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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

	private PTAdapter ptAdapter;
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
	private BaseAsyncTask<Bitmap> imgTask;
	private CustomDialog dialog = null;

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
				ptAdapter = PTFactory.newInstanceByName(typeName);
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
			if (!save()) {
				return;
			}
			ptAdapter.setmUrl(setting.address);
			detachImgTask();
			Toast.makeText(getActivity(), R.string.get_security_code,
					Toast.LENGTH_LONG).show();
			imgTask = ptAdapter.getSecurityImage();
			imgTask.attach(fetchSecurityImgCallback);
			BaseAsyncTask.taskExec.execute(imgTask);
			break;
		case R.id.init:
			if (!save()) {
				return;
			}
			if ("".equals(securityCode.getText())) {
				Crouton.makeText(getActivity(), R.string.input_security_code,
						Style.CONFIRM).show();
				return;
			}
			ptAdapter.setmUrl(setting.address);
			dialog = new CustomDialog(getActivity(), R.string.connecting);
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					detachTask();
				}
			});
			dialog.show();
			BaseAsyncTask<String> task = ptAdapter.login(setting.username,
					setting.password, securityCode.getText().toString());
			task.attach(initCallback);
			attachTask(task);
			BaseAsyncTask.taskExec.execute(task);
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

	@Override
	public boolean onActionBarSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ab_save_pt_site_setting:
			if (save()) {
				Crouton.makeText(getActivity(), R.string.saved, Style.INFO)
						.show();
			}
			break;
		}
		return super.onActionBarSelected(item);
	}

	private void detachImgTask() {
		if (imgTask != null) {
			imgTask.detach();
		}
	}

	private boolean save() {
		setting.address = addr.getText().toString();
		setting.label = label.getText().toString();
		setting.username = username.getText().toString();
		setting.password = password.getText().toString();
		if ("".equals(setting.address) || "".equals(setting.username)
				|| "".equals(setting.password) || "".equals(setting.label)) {
			Crouton.makeText(getActivity(), R.string.fill_in_the_blanks,
					Style.CONFIRM).show();
			return false;
		}
		if (mMode == MODE_EDIT) {
			PTSiteSettingManager.save(getActivity(), setting);
		} else {
			PTSiteSettingManager.add(getActivity(), setting);
		}
		return true;
	}

	private TaskCallback<Bitmap> fetchSecurityImgCallback = new TaskCallback<Bitmap>() {

		@Override
		public void onComplete(Bitmap result) {
			result.setDensity(160);
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

	private TaskCallback<String> initCallback = new TaskCallback<String>() {

		@Override
		public void onComplete(String result) {
			dialog.dismiss();
			Crouton.showText(getActivity(), R.string.initialize_completed,
					Style.INFO);
			setting.cookie = result;
			PTSiteSettingManager.save(getActivity(), setting);
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
