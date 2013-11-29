package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.CommonSettingFragment;

import android.os.Bundle;

public class SettingActivity extends BaseStackActivity {
	// private ToggleButton fetchImage;
	// private ToggleButton sound;
	// private EditText deviceName;
	// private ToggleButton autoRefresh;
	// private EditText serverAddr;
	// private DelegateTask<NewApkInfo> task;

	public SettingActivity() {
		super(R.string.setting);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			stackAdapter.add(new CommonSettingFragment());
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.setting);
	// fetchImage = (ToggleButton) findViewById(R.id.fetchImage);
	// sound = (ToggleButton) findViewById(R.id.sound);
	// autoRefresh = (ToggleButton) findViewById(R.id.auto_refresh);
	// deviceName = (EditText) findViewById(R.id.deviceName);
	// serverAddr = (EditText) findViewById(R.id.server_addr);
	// fetchImage.setChecked(CustomSetting.loadImage);
	// sound.setChecked(CustomSetting.soundOn);
	// deviceName.setText(CustomSetting.device);
	// autoRefresh.setChecked(CustomSetting.autoRefresh);
	// serverAddr.setText(CustomSetting.serverAddress);
	// }
	//
	// @Override
	// protected void onResume() {
	// super.onResume();
	// SharedPreferences shared = this.getSharedPreferences(
	// Const.DOWNLOAD_SHARED_PREFS, MODE_PRIVATE);
	// if (shared.getInt("status", -1) != -1) {
	// findViewById(R.id.download_btn).setVisibility(View.VISIBLE);
	// } else {
	// findViewById(R.id.download_btn).setVisibility(View.GONE);
	// }
	// }
	//
	// @Override
	// protected void onStop() {
	// if (task != null) {
	// task.detach();
	// }
	// super.onStop();
	// Editor edit = getSharedPreferences(Const.SETTING_SHARED_PREFS,
	// MODE_PRIVATE).edit();
	// CustomSetting.loadImage = fetchImage.isChecked();
	// edit.putBoolean("loadImage", CustomSetting.loadImage);
	// CustomSetting.soundOn = sound.isChecked();
	// if (!CustomSetting.soundOn) {
	// SoundPoolManager.clear();
	// }
	// edit.putBoolean("sound", CustomSetting.soundOn);
	// CustomSetting.device = deviceName.getText().toString();
	// edit.putString("device", CustomSetting.device);
	// CustomSetting.autoRefresh = autoRefresh.isChecked();
	// edit.putBoolean("autoRefresh", CustomSetting.autoRefresh);
	// CustomSetting.serverAddress = serverAddr.getText().toString();
	// edit.putString("serverAddr", CustomSetting.serverAddress);
	// edit.commit();
	// Const.Urls.initServerAdd(CustomSetting.serverAddress);
	// }
	//
	// public void onClick(View v) {
	// switch (v.getId()) {
	// case R.id.logOut:
	// HttpClientManager.restClient();
	// Editor edit = getSharedPreferences(Const.SETTING_SHARED_PREFS,
	// MODE_PRIVATE).edit();
	// edit.remove("cookies");
	// edit.commit();
	// HDStarApp.cookies = null;
	// Intent intent = new Intent(this, LoginActivity.class);
	// startActivity(intent);
	// finish();
	// break;
	// case R.id.clearCache:
	// ImageLoader.getInstance().clearDiscCache();
	// Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.checkUpdate:
	// Toast.makeText(this, R.string.searching_for_update,
	// Toast.LENGTH_LONG).show();
	// task = DelegateTask.newInstance("");
	// task.attach(mCallback);
	// // task.execGet(
	// // "http://10.10.28.113:8084/HDStarService/checkVersion?appCode="
	// // + Const.APP_CODE + "&packageName="
	// // + this.getPackageName() + "&versionCode="
	// // + Util.getVersionCode(this),
	// // new TypeToken<ResponseWrapper<NewApkInfo>>() {
	// // }.getType());
	//
	// task.execGet(Const.Urls.SERVER_CHECK_UPDATE_URL + "?appCode="
	// + Const.APP_CODE + "&packageName=" + this.getPackageName()
	// + "&versionCode=" + Util.getVersionCode(this),
	// new TypeToken<ResponseWrapper<NewApkInfo>>() {
	// }.getType());
	// break;
	// case R.id.download_btn:
	// Intent dIntent = new Intent(this, DownloadActivity.class);
	// startActivity(dIntent);
	// break;
	// }
	// }
	//
	// private TaskCallback<NewApkInfo> mCallback = new
	// TaskCallback<NewApkInfo>() {
	//
	// @Override
	// public void onComplete(final NewApkInfo result) {
	// if (result == null) {
	// Toast.makeText(SettingActivity.this, R.string.latest_version,
	// Toast.LENGTH_SHORT).show();
	// return;
	// }
	// CharSequence updateInfo = getString(R.string.update_info);
	// CharSequence fullSize = Util.formatFileSize(result.size);
	// updateInfo = String.format(updateInfo.toString(),
	// result.versionName, fullSize);
	// if (result.patchSize != 0) {
	// // ÔöÁ¿Éý¼¶
	// SpannableString ss = new SpannableString(updateInfo.toString()
	// + "  " + Util.formatFileSize(result.patchSize));
	// ss.setSpan(new StrikethroughSpan(), updateInfo.length()
	// - fullSize.length() - 1, updateInfo.length(),
	// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	// updateInfo = ss;
	// }
	// new AlertDialog.Builder(SettingActivity.this)
	// .setTitle(R.string.update)
	// .setIcon(R.drawable.ic_launcher)
	// .setMessage(updateInfo)
	// .setPositiveButton(R.string.update,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// SharedPreferences shared = getSharedPreferences(
	// Const.DOWNLOAD_SHARED_PREFS,
	// MODE_PRIVATE);
	// Editor editor = shared.edit();
	// editor.putString("packageName",
	// result.packageName);
	// editor.putString("desc", result.desc);
	// String pics = "";
	// for (String pic : result.pics) {
	// pics += pic;
	// }
	// editor.putString("pics", pics);
	// editor.putString("updateDate",
	// result.updateDate);
	// editor.putString("versionName",
	// result.versionName);
	// editor.putInt("versionCode",
	// result.versionCode);
	// editor.putLong("size", result.size);
	// editor.putLong("patchSize",
	// result.patchSize);
	// editor.commit();
	// Intent dlIntent = new Intent(
	// SettingActivity.this,
	// DownloadActivity.class);
	// startActivity(dlIntent);
	// dlIntent = new Intent(SettingActivity.this,
	// DownloadService.class);
	// dlIntent.putExtra(
	// "command",
	// DownloadService.COMMAND_DOWNLOAD_ADD);
	// dlIntent.putExtra("isPatch",
	// result.patchSize != 0);
	// startService(dlIntent);
	// }
	// })
	// .setNegativeButton(R.string.cancel,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// }
	// }).create().show();
	// }
	//
	// @Override
	// public void onCancel() {
	// }
	//
	// @Override
	// public void onFail(Integer msgId) {
	// Toast.makeText(SettingActivity.this, msgId, Toast.LENGTH_SHORT)
	// .show();
	// }
	//
	// };
}
