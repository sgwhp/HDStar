package org.hdstar.component.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DownloadImageTask;
import org.hdstar.task.LoginTask;
import org.hdstar.util.EncodeDecode;
import org.hdstar.util.SoundPoolManager;
import org.hdstar.widget.CustomDialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ToggleButton;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class LoginActivity extends SherlockActivity implements OnClickListener {
	private DownloadImageTask imageTask = null;
	private final String LOGIN_URL = Const.Urls.BASE_URL + "/login.php";
	private LoginTask task = null;
	private CustomDialog dialog = null;
	private ToggleButton fetchImage;
	private ToggleButton sound;
	private EditText deviceName;
	private ToggleButton autoRefresh;
	private EditText serverAddr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.login);
		setContentView(R.layout.login);
		getSecurityCode();
		init();
	}

	@Override
	protected void onDestroy() {
		if (task != null) {
			task.detach();
		}
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if (keyCode == KeyEvent.KEYCODE_BACK && e.getRepeatCount() == 0) {
			if (dialog != null && dialog.isShowing()) {
				task.abort();
				dialog.dismiss();
			} else {
				new AlertDialog.Builder(this)
						.setTitle(R.string.confirm)
						.setIcon(R.drawable.ic_launcher)
						.setMessage(R.string.exit_message)
						.setPositiveButton(R.string.exit,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		}

		return false;
	}

	void init() {
		SharedPreferences user = getSharedPreferences(
				Const.SETTING_SHARED_PREFS, MODE_PRIVATE);
		String username = user.getString("username", null);
		String password = user.getString("password", null);
		if (username != null) {
			((EditText) findViewById(R.id.username)).setText(username);
		}
		if (password != null) {
			password = EncodeDecode.decode(password);
			((EditText) findViewById(R.id.password)).setText(password);
		}
	}

	/**
	 * 获取验证码
	 * */
	void getSecurityCode() {
		imageTask = new DownloadImageTask(this);
		imageTask.execute(LOGIN_URL);
	}

	private void login() {
		if (fetchImage != null) {
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
			CustomSetting.serverAddress = serverAddr.getText().toString();
			edit.putString("serverAddr", CustomSetting.serverAddress);
			edit.commit();
			Const.Urls.initServerAddr(CustomSetting.serverAddress);
		}
		if (imageTask.getStatus() == AsyncTask.Status.FINISHED) {
			String imageHash = imageTask.getHash();
			String imageString = ((EditText) findViewById(R.id.security_code))
					.getText().toString();
			String id = ((EditText) findViewById(R.id.username)).getText()
					.toString();
			String password = ((EditText) findViewById(R.id.password))
					.getText().toString();
			if (imageString.equals("")) {
				Crouton.makeText(LoginActivity.this,
						R.string.input_security_code, Style.CONFIRM).show();
				return;
			}
			if (id.equals("")) {
				Crouton.makeText(LoginActivity.this, R.string.input_username,
						Style.CONFIRM).show();
				return;
			}
			if (password.equals("")) {
				Crouton.makeText(LoginActivity.this, R.string.input_password,
						Style.CONFIRM).show();
				return;
			}
			Editor user = LoginActivity.this.getSharedPreferences(
					Const.SETTING_SHARED_PREFS, MODE_PRIVATE).edit();
			user.putString("username", id);
			user.putString("password", EncodeDecode.encode(password));
			user.commit();
			dialog = new CustomDialog(LoginActivity.this, R.string.try_to_login);
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (task.getStatus() != AsyncTask.Status.FINISHED)
						task.abort();
				}

			});
			dialog.show();
			task = new LoginTask();
			task.attach(mListener);
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("username", id));
			nvp.add(new BasicNameValuePair("password", password));
			nvp.add(new BasicNameValuePair("imagestring", imageString));
			nvp.add(new BasicNameValuePair("imagehash", imageHash));
			try {
				task.execPost(Const.Urls.TAKE_LOGIN_URL, nvp, "");
			} catch (UnsupportedEncodingException e) {
				dialog.dismiss();
				e.printStackTrace();
			}
		}
	}

	private TaskCallback<String> mListener = new TaskCallback<String>() {

		@Override
		public void onComplete(String result) {
			String cookieStr = result;
			// if (lang.equals("简体中文")) {
			// cookieStr += "c_lang_folder=chs";
			// } else if (lang.equals("繁體中文")) {
			// cookieStr += "c_lang_folder=cht";
			// }
			HDStarApp.cookies = cookieStr;
			Editor edit = getSharedPreferences(Const.SETTING_SHARED_PREFS,
					MODE_PRIVATE).edit();
			edit.putString("cookies", cookieStr);
			edit.commit();
			dialog.dismiss();
			if (cookieStr != null) {
				Intent intent = new Intent(LoginActivity.this,
						ForumsActivity.class);
				startActivity(intent);
				finish();
			}
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Crouton.makeText(LoginActivity.this, msgId, Style.ALERT).show();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			login();
			break;
		case R.id.refresh:
			if (imageTask != null) {
				imageTask.interrupt(true);
			}
			getSecurityCode();
			break;
		case R.id.setting:
			if (fetchImage == null) {
				ViewStub stub = (ViewStub) findViewById(R.id.setting_stub);
				stub.inflate();
				fetchImage = (ToggleButton) findViewById(R.id.fetchImage);
				sound = (ToggleButton) findViewById(R.id.sound);
				autoRefresh = (ToggleButton) findViewById(R.id.auto_refresh);
				deviceName = (EditText) findViewById(R.id.deviceName);
				serverAddr = (EditText) findViewById(R.id.server_addr);
				fetchImage.setChecked(CustomSetting.loadImage);
				sound.setChecked(CustomSetting.soundOn);
				deviceName.setText(CustomSetting.device);
				autoRefresh.setChecked(CustomSetting.autoRefresh);
				serverAddr.setText(CustomSetting.serverAddress);
			}
			break;
		}
	}
}
