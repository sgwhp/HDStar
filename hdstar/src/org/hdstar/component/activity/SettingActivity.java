package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.DownloadService;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.NewApkInfo;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingActivity extends BaseActivity {
	private CheckBox fetchImage;
	private CheckBox sound;
	private EditText deviceName;
	private CheckBox autoRefresh;
	private DelegateTask<NewApkInfo> task;

	public SettingActivity() {
		super(R.string.setting);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		fetchImage = (CheckBox) findViewById(R.id.fetchImage);
		sound = (CheckBox) findViewById(R.id.sound);
		autoRefresh = (CheckBox) findViewById(R.id.auto_refresh);
		deviceName = (EditText) findViewById(R.id.deviceName);
		fetchImage.setChecked(CustomSetting.loadImage);
		sound.setChecked(CustomSetting.soundOn);
		deviceName.setText(CustomSetting.device);
		autoRefresh.setChecked(CustomSetting.autoRefresh);
	}

	@Override
	protected void onStop() {
		if (task != null) {
			task.detach();
		}
		super.onStop();
		Editor edit = getSharedPreferences(Const.SETTING_SHARED_PREFS,
				MODE_PRIVATE).edit();
		CustomSetting.loadImage = fetchImage.isChecked();
		edit.putBoolean("loadImage", CustomSetting.loadImage);
		CustomSetting.soundOn = sound.isChecked();
		if (!CustomSetting.soundOn) {
			SoundPoolManager.clear();
		}
		edit.putBoolean("sound", CustomSetting.soundOn);
		CustomSetting.device = deviceName.getText().toString();
		edit.putString("device", CustomSetting.device);
		CustomSetting.autoRefresh = autoRefresh.isChecked();
		edit.putBoolean("autoRefresh", CustomSetting.autoRefresh);
		edit.commit();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logOut:
			CustomHttpClient.restClient();
			Editor edit = getSharedPreferences(Const.SETTING_SHARED_PREFS,
					MODE_PRIVATE).edit();
			edit.remove("cookies");
			edit.commit();
			HDStarApp.cookies = null;
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.clearCache:
			ImageLoader.getInstance().clearDiscCache();
			break;
		case R.id.checkUpdate:
			Toast.makeText(this, R.string.searching_for_update,
					Toast.LENGTH_LONG).show();
			task = DelegateTask.newInstance("");
			task.attach(mCallback);
			task.execGet(
					"http://10.10.28.113:8084/HDStarService/checkVersion?appCode="
							+ Const.APP_CODE + "&packageName="
							+ this.getPackageName() + "&versionCode="
							+ Util.getVersionCode(this),
					new TypeToken<ResponseWrapper<NewApkInfo>>() {
					}.getType());
			break;
		}
	}

	private TaskCallback<NewApkInfo> mCallback = new TaskCallback<NewApkInfo>() {

		@Override
		public void onComplete(final NewApkInfo result) {
			if (result == null) {
				Toast.makeText(SettingActivity.this, R.string.latest_version,
						Toast.LENGTH_SHORT).show();
				return;
			}
			String updateInfo = getString(R.string.update_info);
			updateInfo = String.format(updateInfo, result.versionName,
					Util.formatFileSize(result.size));
			new AlertDialog.Builder(SettingActivity.this)
					.setTitle(R.string.update)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(updateInfo)
					.setPositiveButton(R.string.update,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SharedPreferences shared = getSharedPreferences(
											Const.DOWNLOAD_SHARED_PREFS,
											MODE_PRIVATE);
									Editor editor = shared.edit();
									editor.putString("packageName",
											result.packageName);
									editor.putString("desc", result.desc);
									editor.putStringSet("pics",
											result.getPicsSet());
									editor.putString("updateDate",
											result.updateDate);
									editor.putString("versionName",
											result.versionName);
									editor.putInt("versionCode",
											result.versionCode);
									editor.putLong("size", result.size);
									editor.putLong("patchSize",
											result.patchSize);
									editor.commit();
									Intent dlIntent = new Intent(
											SettingActivity.this,
											DownloadActivity.class);
									startActivity(dlIntent);
									dlIntent = new Intent(SettingActivity.this,
											DownloadService.class);
									dlIntent.putExtra(
											"command",
											DownloadService.COMMAND_DOWNLOAD_ADD);
									dlIntent.putExtra("isPatch",
											result.patchSize != 0);
									startService(dlIntent);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			Toast.makeText(SettingActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
		}

	};
}
