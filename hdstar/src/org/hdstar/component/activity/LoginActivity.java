package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.ptadapter.HDSky;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.util.DES;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;
import org.hdstar.widget.fragment.CommonSettingFragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 登录
 * 
 * @author robust
 * 
 */
public class LoginActivity extends SherlockFragmentActivity implements
		OnClickListener {
	private BaseAsyncTask<Bitmap> imageTask = null;
	private BaseAsyncTask<String> task = null;
	private CustomDialog dialog = null;
	private ImageView securityImage;
	private HDSky hdsky = new HDSky();
	private boolean settingViewIsShown;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.login);
		setContentView(R.layout.login);
		init();
		getSecurityCode();
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
				Util.showExitDialog(this);
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
			try {
				password = DES.decryptDES(password, Const.TAG);
				((EditText) findViewById(R.id.password)).setText(password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		securityImage = (ImageView) findViewById(R.id.security_image);
	}

	/**
	 * 获取验证码
	 * */
	void getSecurityCode() {
		imageTask = hdsky.getSecurityImage();
		imageTask.attach(imageCallback);
		BaseAsyncTask.commit(imageTask);
	}

	private void login() {
		if (imageTask.getStatus() == AsyncTask.Status.FINISHED) {
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
			Editor user = getSharedPreferences(Const.SETTING_SHARED_PREFS,
					MODE_PRIVATE).edit();
			user.putString("username", id);
			try {
				user.putString("password", DES.encryptDES(password, Const.TAG));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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
			task = hdsky.login(id, password, imageString);
			task.attach(mListener);
			BaseAsyncTask.commit(task);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			login();
			break;
		case R.id.refresh:
			if (imageTask != null) {
				imageTask.detach();
			}
			getSecurityCode();
			break;
		case R.id.setting:
			if (!settingViewIsShown) {
				settingViewIsShown = true;
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.common_setting,
						CommonSettingFragment.getInstance());
				transaction.commit();
			}
			break;
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

	private TaskCallback<Bitmap> imageCallback = new TaskCallback<Bitmap>() {

		@Override
		public void onComplete(Bitmap result) {
			result.setDensity(160);
			securityImage.setImageBitmap(result);
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onFail(Integer msgId) {
			Crouton.makeText(LoginActivity.this, msgId, Style.ALERT).show();
		}
	};
}
