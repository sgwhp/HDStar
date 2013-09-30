package org.hdstar.component.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.task.DownloadImageTask;
import org.hdstar.task.LoginTask;
import org.hdstar.task.MyAsyncTask.TaskCallback;
import org.hdstar.util.EncodeDecode;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {
	private DownloadImageTask imageTask = null;
	private final String LOGIN_URL = Const.Urls.BASE_URL + "/login.php";
	private LoginTask task = null;
	private CustomDialog dialog = null;

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
		Button login = (Button) findViewById(R.id.login);
		SharedPreferences user = getSharedPreferences(Const.SHARED_PREFS,
				MODE_PRIVATE);
		String username = user.getString("username", null);
		String password = user.getString("password", null);
		if (username != null) {
			((EditText) findViewById(R.id.username)).setText(username);
		}
		if (password != null) {
			password = EncodeDecode.decode(password);
			((EditText) findViewById(R.id.password)).setText(password);
		}
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (imageTask.getStatus() == AsyncTask.Status.FINISHED) {
					String imageHash = imageTask.getHash();
					String imageString = ((EditText) findViewById(R.id.security_code))
							.getText().toString();
					String id = ((EditText) findViewById(R.id.username))
							.getText().toString();
					String password = ((EditText) findViewById(R.id.password))
							.getText().toString();
					if (imageString.equals("")) {
						Toast.makeText(LoginActivity.this,
								R.string.input_security_code,
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (id.equals("")) {
						Toast.makeText(LoginActivity.this,
								R.string.input_username, Toast.LENGTH_SHORT)
								.show();
						return;
					}
					if (password.equals("")) {
						Toast.makeText(LoginActivity.this,
								R.string.input_password, Toast.LENGTH_SHORT)
								.show();
						return;
					}
					Editor user = LoginActivity.this.getSharedPreferences(
							Const.SHARED_PREFS, MODE_PRIVATE).edit();
					user.putString("username", id);
					user.putString("password", EncodeDecode.encode(password));
					user.commit();
					dialog = new CustomDialog(LoginActivity.this,
							R.string.try_to_login);
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

		});

		Button refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (imageTask != null) {
					imageTask.interrupt(true);
				}
				getSecurityCode();
			}

		});
	}

	/**
	 * 获取验证码
	 * */
	void getSecurityCode() {
		imageTask = new DownloadImageTask(this);
		imageTask.execute(LOGIN_URL);
	}

	void onBack(String cookieStr) {
		Intent intent = new Intent(this, ForumsActivity.class);
		Bundle bundle = new Bundle();
		if (cookieStr != null) {
			bundle.putString("cookies", cookieStr);
			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_OK + 1, intent);
		}
		intent.putExtras(bundle);
		if (imageTask != null)
			imageTask.interrupt(true);
		finish();
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
			Editor edit = getSharedPreferences(Const.SHARED_PREFS, MODE_PRIVATE)
					.edit();
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
			Toast.makeText(LoginActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

	};
}
