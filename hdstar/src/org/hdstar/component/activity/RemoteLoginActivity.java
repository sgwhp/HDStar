package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.RemoteSetting;
import org.hdstar.common.RemoteType;
import org.hdstar.component.HDStarApp;
import org.hdstar.remote.RemoteBase;
import org.hdstar.remote.RemoteFactory;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public class RemoteLoginActivity extends BaseActivity implements
		OnNavigationListener, OnClickListener {
	private EditText ipET, accET, pwdET;
	private CustomDialog dialog = null;
	private BaseAsyncTask<Boolean> task;
	private RemoteType type;
	private RemoteSetting setting;

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

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.remoteClient, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		// SharedPreferences share = getSharedPreferences(
		// Const.RUTORRENT_SHARED_PREFS, MODE_PRIVATE);
		// ipET.setText(share.getString("ip", ""));
		// accET.setText(share.getString("username", ""));
		// pwdET.setText(EncodeDecode.decode(share.getString("password", "")));
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
		switch (itemPosition) {
		case 0:
			type = RemoteType.RuTorrentRemote;
			break;
		case 1:
			type = RemoteType.UTorrentRemote;
			break;
		}
		initInputField();
		return false;
	}

	@Override
	public void onClick(View v) {
		String ip = ipET.getText().toString();
		if (!Util.isIp(ip)) {
			Toast.makeText(this, R.string.invalidate_ip, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String acc = accET.getText().toString();
		String pwd = pwdET.getText().toString();

		// SharedPreferences share = getSharedPreferences(
		// Const.RUTORRENT_SHARED_PREFS, MODE_PRIVATE);
		// Editor editor = share.edit();
		// editor.putString("ip", ip);
		// editor.putString("username", acc);
		// editor.putString("password", EncodeDecode.encode(pwd));
		// editor.commit();
		setting.saveIp(ip);
		setting.saveUsername(acc);
		setting.savePassword(pwd);

		dialog = new CustomDialog(this, R.string.try_to_login);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (task.getStatus() != AsyncTask.Status.FINISHED)
					task.detach();
			}

		});
		dialog.show();
		RemoteBase remote = RemoteFactory.newInstanceByName(type.name());
		remote.setIpNPort(ip);
		task = remote.login(acc, pwd);
		task.attach(mCallback);
		task.execute("");
		// if (ip.contains(":")) {
		// int port = Integer.parseInt(ip.substring(ip.indexOf(':') + 1));
		// task.auth(ip, port, acc, pwd);
		// } else {
		// task.auth(ip, acc, pwd);
		// }
	}

	private void initInputField() {
		setting = new RemoteSetting(this, type);
		ipET.setText(setting.getIp(""));
		accET.setText(setting.getUsername(""));
		pwdET.setText(setting.getPassword(""));
	}

	private TaskCallback<Boolean> mCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			dialog.dismiss();
			HDStarApp.loginRemote = true;
			Intent intent = new Intent(RemoteLoginActivity.this,
					RemoteActivity.class);
			intent.putExtra("remote", type.name());
			intent.putExtra("ip", ipET.getText().toString());
			intent.putExtra("downloadDir", setting.getDownloadDir(""));
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
