package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.common.PTSiteType;
import org.hdstar.component.HDStarApp;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PTSiteSettingFragment extends StackFragment implements
		OnClickListener {
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;

	private PTAdapter ptAdapter;
	private PTSiteSetting setting;
	private int mMode;

	private ImageView icon;
	private TextView label;
	private EditText username;
	private EditText password;
	private FrameLayout security;
	private ImageView securityImg;
	private EditText securityCode;
	private Button refreshBtn;
	private Button initBtn;
	private Button removeBtn;
	private Button logoutBtn;
	private BaseAsyncTask<Bitmap> imgTask;
	private BaseAsyncTask<Boolean> logoutTask;
	private CustomDialog dialog = null;

	public static PTSiteSettingFragment newInstance(PTSiteSetting setting) {
		PTSiteSettingFragment fragment = new PTSiteSettingFragment();
		Bundle args = new Bundle();
		args.putInt("mode", MODE_EDIT);
		args.putParcelable("setting", setting);
		fragment.setArguments(args);
		return fragment;
	}

	public static PTSiteSettingFragment newInstance(String type) {
		PTSiteSettingFragment fragment = new PTSiteSettingFragment();
		Bundle args = new Bundle();
		args.putInt("mode", MODE_ADD);
		args.putString("type", type);
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
			setting.type = b.getString("type");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		detachImgTask();
		detachLogoutTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pt_site_setting, null);
		icon = (ImageView) v.findViewById(R.id.site_icon);
		label = (TextView) v.findViewById(R.id.site_label);
		username = (EditText) v.findViewById(R.id.username);
		password = (EditText) v.findViewById(R.id.password);
		security = (FrameLayout) v.findViewById(R.id.security);
		securityImg = (ImageView) v.findViewById(R.id.security_image);
		securityCode = (EditText) v.findViewById(R.id.security_code);
		refreshBtn = (Button) v.findViewById(R.id.refresh);
		initBtn = (Button) v.findViewById(R.id.init);
		removeBtn = (Button) v.findViewById(R.id.remove);
		logoutBtn = (Button) v.findViewById(R.id.logout);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ptAdapter = PTFactory.newInstanceByName(setting.type);
		ImageLoader.getInstance().displayImage(
				String.format(CommonUrls.GETFVO_URL,
						PTSiteType.getByName(setting.type).getUrl()), icon,
				HDStarApp.displayOptions);
		label.setText(setting.type);
		if (mMode == MODE_EDIT) {
			username.setText(setting.username);
			password.setText(setting.password);
			removeBtn.setVisibility(View.VISIBLE);
			removeBtn.setOnClickListener(this);
			if (setting.cookie != null && !"".equals(setting.cookie)) {
				logoutBtn.setVisibility(View.VISIBLE);
				logoutBtn.setOnClickListener(this);
				initBtn.setVisibility(View.GONE);
			} else {
				initBtn.setOnClickListener(this);
			}
		} else {
			initBtn.setOnClickListener(this);
		}
		if (ptAdapter.needSecurityCode()) {
			if (setting.cookie == null || "".equals(setting.cookie)) {
				security.setVisibility(View.VISIBLE);
				refreshBtn.setVisibility(View.VISIBLE);
				// 获取验证码
				getSecurityCode();
			}
		} else {
			security.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.GONE);
		}
		refreshBtn.setOnClickListener(this);
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
			getSecurityCode();
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
			BaseAsyncTask.commit(task);
			break;
		case R.id.logout:
			dialog = new CustomDialog(getActivity(), R.string.connecting);
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					detachLogoutTask();
				}
			});
			dialog.show();
			logoutTask = ptAdapter.logout();
			logoutTask.attach(logoutCallback);
			BaseAsyncTask.commit(logoutTask);
			break;
		}
	}

	@Override
	public void onCreateActionBar(Menu menu) {
		MenuItem item = menu.add(0, R.id.ab_save_pt_site_setting, 0,
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

	private void detachLogoutTask() {
		if (logoutTask != null) {
			logoutTask.detach();
		}
	}

	private void getSecurityCode() {
		detachImgTask();
		Toast.makeText(getActivity(), R.string.get_security_code,
				Toast.LENGTH_LONG).show();
		imgTask = ptAdapter.getSecurityImage();
		imgTask.attach(fetchSecurityImgCallback);
		BaseAsyncTask.commit(imgTask);
	}

	private boolean save() {
		setting.username = username.getText().toString();
		setting.password = password.getText().toString();
		if ("".equals(setting.username) || "".equals(setting.password)) {
			Crouton.makeText(getActivity(), R.string.fill_in_the_blanks,
					Style.CONFIRM).show();
			return false;
		}
		if (mMode == MODE_EDIT) {
			PTSiteSettingManager.save(getActivity(), setting);
		} else {
			mMode = MODE_EDIT;
			PTSiteSettingManager.add(getActivity(), setting);
		}
		return true;
	}

	private TaskCallback<Bitmap> fetchSecurityImgCallback = new TaskCallback<Bitmap>() {

		@Override
		public void onComplete(Bitmap result) {
			if (result != null) {
				result.setDensity(160);
				securityImg.setImageBitmap(result);
			}
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
			initBtn.setVisibility(View.GONE);
			logoutBtn.setVisibility(View.VISIBLE);
			logoutBtn.setOnClickListener(PTSiteSettingFragment.this);
			security.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.GONE);
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Crouton.showText(getActivity(), msgId, Style.ALERT);
		}
	};

	private TaskCallback<Boolean> logoutCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			dialog.dismiss();
			setting.cookie = "";
			PTSiteSettingManager.save(getActivity(), setting);
			initBtn.setVisibility(View.VISIBLE);
			initBtn.setOnClickListener(PTSiteSettingFragment.this);
			logoutBtn.setVisibility(View.GONE);
			if (ptAdapter.needSecurityCode()) {
				security.setVisibility(View.VISIBLE);
				refreshBtn.setVisibility(View.VISIBLE);
				refreshBtn.setOnClickListener(PTSiteSettingFragment.this);
			}
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Crouton.showText(getActivity(), msgId, Style.ALERT);
		}
	};
}
