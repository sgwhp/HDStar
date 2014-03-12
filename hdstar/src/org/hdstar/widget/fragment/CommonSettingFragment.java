package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;
import org.hdstar.util.SoundPoolManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * 
 * 常用设置，包括是否获取网络图片、服务器地址等 <br/>
 * 
 * @author robust
 */
public class CommonSettingFragment extends Fragment implements OnClickListener {
	private ToggleButton fetchImage;
	private ToggleButton sound;
	private EditText deviceName;
	private ToggleButton autoRefresh;
	private EditText serverAddr;
	private ToggleButton enableProxy;

	public static CommonSettingFragment getInstance() {
		return new CommonSettingFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.common_setting, null);
		fetchImage = (ToggleButton) v.findViewById(R.id.fetchImage);
		sound = (ToggleButton) v.findViewById(R.id.sound);
		autoRefresh = (ToggleButton) v.findViewById(R.id.auto_refresh);
		deviceName = (EditText) v.findViewById(R.id.deviceName);
		serverAddr = (EditText) v.findViewById(R.id.server_addr);
		enableProxy = (ToggleButton) v.findViewById(R.id.enable_proxy);
		bindData();
		return v;
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
		CustomSetting.setServerAddress(serverAddr.getText().toString());
		edit.putString("serverAddr", CustomSetting.serverAddress);
		CustomSetting.enableProxy = enableProxy.isChecked();
		edit.putBoolean("enableProxy", CustomSetting.enableProxy);
		edit.commit();
		CommonUrls.HDStar.initServerAddr(CustomSetting.getCurServerAddr());
	}

	@Override
	public void onClick(View v) {
		final Activity act = getActivity();
		switch (v.getId()) {
		case R.id.reset:
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.reset)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(R.string.reset_message)
					.setPositiveButton(R.string.reset,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Editor editor = act.getSharedPreferences(
											"", Context.MODE_PRIVATE).edit();
									editor.remove("loadImage");
									editor.remove("sound");
									editor.remove("device");
									editor.remove("autoRefresh");
									editor.remove("serverAddr");
									editor.remove("enableProxy");
									editor.remove("fade");
									editor.remove("anim");
									editor.commit();
									HDStarApp.init(act);
									bindData();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();
			break;
		}
	}

	/**
	 * 绑定数据 <br/>
	 */
	private void bindData() {
		fetchImage.setChecked(CustomSetting.loadImage);
		sound.setChecked(CustomSetting.soundOn);
		deviceName.setText(CustomSetting.device);
		autoRefresh.setChecked(CustomSetting.autoRefresh);
		serverAddr.setText(CustomSetting.serverAddress);
		enableProxy.setChecked(CustomSetting.enableProxy);
	}

}
