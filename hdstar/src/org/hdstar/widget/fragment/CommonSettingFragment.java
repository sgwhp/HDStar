package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.DownloadService;
import org.hdstar.component.HDStarApp;
import org.hdstar.component.activity.DownloadActivity;
import org.hdstar.component.activity.LoginActivity;
import org.hdstar.model.NewApkInfo;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommonSettingFragment extends StackFragment implements
		OnClickListener {
	private ToggleButton fetchImage;
	private ToggleButton sound;
	private EditText deviceName;
	private ToggleButton autoRefresh;
	private EditText serverAddr;
	private Button downloadBtn;

	// private DelegateTask<NewApkInfo> task;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.setting, null);
		fetchImage = (ToggleButton) v.findViewById(R.id.fetchImage);
		sound = (ToggleButton) v.findViewById(R.id.sound);
		autoRefresh = (ToggleButton) v.findViewById(R.id.auto_refresh);
		deviceName = (EditText) v.findViewById(R.id.deviceName);
		serverAddr = (EditText) v.findViewById(R.id.server_addr);
		downloadBtn = (Button) v.findViewById(R.id.download_btn);
		fetchImage.setChecked(CustomSetting.loadImage);
		sound.setChecked(CustomSetting.soundOn);
		deviceName.setText(CustomSetting.device);
		autoRefresh.setChecked(CustomSetting.autoRefresh);
		serverAddr.setText(CustomSetting.serverAddress);
		downloadBtn.setOnClickListener(this);
		v.findViewById(R.id.logOut).setOnClickListener(this);
		v.findViewById(R.id.clearCache).setOnClickListener(this);
		v.findViewById(R.id.checkUpdate).setOnClickListener(this);
		v.findViewById(R.id.animation).setOnClickListener(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences shared = getActivity().getSharedPreferences(
				Const.DOWNLOAD_SHARED_PREFS, Activity.MODE_PRIVATE);
		if (shared.getInt("status", -1) != -1) {
			downloadBtn.setVisibility(View.VISIBLE);
		} else {
			downloadBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Editor edit = getActivity().getSharedPreferences(
				Const.SETTING_SHARED_PREFS, Activity.MODE_PRIVATE).edit();
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
		CustomSetting.serverAddress = serverAddr.getText().toString();
		edit.putString("serverAddr", CustomSetting.serverAddress);
		edit.commit();
		Const.Urls.initServerAddr(CustomSetting.serverAddress);
	}

	@Override
	public void onClick(View v) {
		Activity act = getActivity();
		switch (v.getId()) {
		case R.id.logOut:
			HttpClientManager.restClient();
			Editor edit = act.getSharedPreferences(Const.SETTING_SHARED_PREFS,
					Activity.MODE_PRIVATE).edit();
			edit.remove("cookies");
			edit.commit();
			HDStarApp.cookies = null;
			Intent intent = new Intent(act, LoginActivity.class);
			startActivity(intent);
			act.finish();
			break;
		case R.id.clearCache:
			ImageLoader.getInstance().clearDiscCache();
			Toast.makeText(act, R.string.cache_cleared, Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.checkUpdate:
			Toast.makeText(act, R.string.searching_for_update,
					Toast.LENGTH_LONG).show();
			DelegateTask<NewApkInfo> task = DelegateTask.newInstance("");
			task.attach(mCallback);
			attachTask(task);
			// task.execGet(
			// "http://10.10.28.113:8084/HDStarService/checkVersion?appCode="
			// + Const.APP_CODE + "&packageName="
			// + this.getPackageName() + "&versionCode="
			// + Util.getVersionCode(this),
			// new TypeToken<ResponseWrapper<NewApkInfo>>() {
			// }.getType());

			task.execGet(Const.Urls.SERVER_CHECK_UPDATE_URL + "?appCode="
					+ Const.APP_CODE + "&packageName=" + act.getPackageName()
					+ "&versionCode=" + Util.getVersionCode(act),
					new TypeToken<ResponseWrapper<NewApkInfo>>() {
					}.getType());
			break;
		case R.id.download_btn:
			Intent dIntent = new Intent(act, DownloadActivity.class);
			startActivity(dIntent);
			break;
		case R.id.animation:
			push(new AnimSettingFragment());
			break;
		}
	}

	private TaskCallback<NewApkInfo> mCallback = new TaskCallback<NewApkInfo>() {

		@Override
		public void onComplete(final NewApkInfo result) {
			if (result == null) {
				Toast.makeText(getActivity(), R.string.latest_version,
						Toast.LENGTH_SHORT).show();
				return;
			}
			CharSequence updateInfo = getString(R.string.update_info);
			CharSequence fullSize = Util.formatFileSize(result.size);
			updateInfo = String.format(updateInfo.toString(),
					result.versionName, fullSize);
			if (result.patchSize != 0) {
				// ÔöÁ¿Éý¼¶
				SpannableString ss = new SpannableString(updateInfo.toString()
						+ "  " + Util.formatFileSize(result.patchSize));
				ss.setSpan(new StrikethroughSpan(), updateInfo.length()
						- fullSize.length() - 1, updateInfo.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				updateInfo = ss;
			}
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.update)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(updateInfo)
					.setPositiveButton(R.string.update,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SharedPreferences shared = getActivity()
											.getSharedPreferences(
													Const.DOWNLOAD_SHARED_PREFS,
													Activity.MODE_PRIVATE);
									Editor editor = shared.edit();
									editor.putString("packageName",
											result.packageName);
									editor.putString("desc", result.desc);
									String pics = "";
									for (String pic : result.pics) {
										pics += pic;
									}
									editor.putString("pics", pics);
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
									Intent dlIntent = new Intent(getActivity(),
											DownloadActivity.class);
									startActivity(dlIntent);
									dlIntent = new Intent(getActivity(),
											DownloadService.class);
									dlIntent.putExtra(
											"command",
											DownloadService.COMMAND_DOWNLOAD_ADD);
									dlIntent.putExtra("isPatch",
											result.patchSize != 0);
									getActivity().startService(dlIntent);
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
			Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
		}

	};

}
