package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.RemoteLoginTask;
import org.hdstar.util.EncodeDecode;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public class RemoteLoginActivity extends BaseActivity implements
		OnNavigationListener, OnClickListener {
	private EditText ipET, accET, pwdET;
	private CustomDialog dialog = null;
	private RemoteLoginTask task;

	public RemoteLoginActivity() {
		super(R.string.remote);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_remote);
		ipET = (EditText) findViewById(R.id.ip);
		accET = (EditText) findViewById(R.id.username);
		pwdET = (EditText) findViewById(R.id.password);

//		Context context = getSupportActionBar().getThemedContext();
//		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
//				context, R.array.remoteClient, R.layout.sherlock_spinner_item);
//		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
//		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//		getSupportActionBar().setListNavigationCallbacks(list, this);
		SharedPreferences share = getSharedPreferences(
				Const.RUTORRENT_SHARED_PREFS, MODE_PRIVATE);
		ipET.setText(share.getString("ip", ""));
		accET.setText(share.getString("username", ""));
		pwdET.setText(EncodeDecode.decode(share.getString("password", "")));
	}

	@Override
	protected void onDestroy() {
		if (task != null) {
			task.detach();
		}
		super.onDestroy();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}

	@Override
	public void onClick(View v) {
		String ip = ipET.getText().toString();
		if(!Util.isIp(ip)){
			Toast.makeText(this, R.string.invalidate_ip, Toast.LENGTH_SHORT).show();
			return;
		}
		String acc = accET.getText().toString();
		String pwd = pwdET.getText().toString();

		SharedPreferences share = getSharedPreferences(
				Const.RUTORRENT_SHARED_PREFS, MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putString("ip", ip);
		editor.putString("username", acc);
		editor.putString("password", EncodeDecode.encode(pwd));
		editor.commit();

		dialog = new CustomDialog(this, R.string.try_to_login);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (task.getStatus() != AsyncTask.Status.FINISHED)
					task.detach();
			}

		});
		dialog.show();
		task = new RemoteLoginTask();
		task.attach(mCallback);
		if(ip.contains(":")){
			int port = Integer.parseInt(ip.substring(ip.indexOf(':') + 1));
			task.auth(ip, port, acc, pwd);
		} else {
			task.auth(ip, acc, pwd);
		}
	}

	private TaskCallback<Boolean> mCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			dialog.dismiss();
			HDStarApp.loginRemote = true;
			Intent intent = new Intent(RemoteLoginActivity.this,
					RemoteActivity.class);
			startActivity(intent);
			finish();
		}

		@Override
		public void onCancel() {
			dialog.dismiss();
		}

		@Override
		public void onFail(Integer msgId) {
			dialog.dismiss();
			Toast.makeText(RemoteLoginActivity.this, msgId, Toast.LENGTH_SHORT)
					.show();
		}

	};
}
