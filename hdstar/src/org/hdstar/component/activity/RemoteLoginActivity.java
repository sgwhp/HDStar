package org.hdstar.component.activity;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.RemoteSettingManager;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.RemoteSetting;
import org.hdstar.remote.RemoteBase;
import org.hdstar.remote.RemoteFactory;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.util.Util;
import org.hdstar.widget.CustomDialog;

import android.app.AlertDialog;
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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RemoteLoginActivity extends BaseActivity implements
		OnNavigationListener, OnClickListener {
	private EditText ipET, accET, pwdET;
	private CustomDialog dialog = null;
	private BaseAsyncTask<Boolean> task;
	private RemoteSetting setting;
	private ArrayList<RemoteSetting> settings;
	int order;

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
		settings = RemoteSettingManager.getAll(context);
		if (settings.size() > 0) {
			String[] servers = new String[settings.size()];
			for (int i = 0; i < settings.size(); i++) {
				servers[i] = settings.get(i).name;
			}
			ArrayAdapter<CharSequence> list = new ArrayAdapter<CharSequence>(
					context, R.layout.sherlock_spinner_item, servers);
			// ArrayAdapter<CharSequence> list =
			// ArrayAdapter.createFromResource(
			// context, R.array.remoteClient, R.layout.sherlock_spinner_item);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			getSupportActionBar().setListNavigationCallbacks(list, this);
		} else {
			findViewById(R.id.login).setEnabled(false);
			new AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setIcon(R.drawable.ic_launcher)
					.setMessage(R.string.no_remote_setting)
					.setPositiveButton(R.string.add,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RemoteLoginActivity.this,
											SettingActivity.class);
									startActivity(intent);
									finish();
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
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		order = itemPosition;
		setting = settings.get(itemPosition);
		initInputField();
		return false;
	}

	@Override
	public void onClick(View v) {
		String ip = ipET.getText().toString();
		if (!Util.isIp(ip)) {
			Crouton.makeText(this, R.string.invalidate_ip, Style.ALERT).show();
			return;
		}
		setting.ip = ip;
		setting.username = accET.getText().toString();
		setting.password = pwdET.getText().toString();

		setting.saveIp(this);
		setting.saveUsername(this);
		setting.savePassword(this);

		dialog = new CustomDialog(this, R.string.try_to_login);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (task.getStatus() != AsyncTask.Status.FINISHED)
					task.detach();
			}

		});
		dialog.show();
		RemoteBase remote = RemoteFactory.newInstanceByName(setting.type);
		remote.setIpNPort(setting.ip);
		task = remote.login(setting.username, setting.password);
		task.attach(mCallback);
		task.execute("");
	}

	private void initInputField() {
		ipET.setText(setting.ip);
		accET.setText(setting.username);
		pwdET.setText(setting.password);
	}

	private TaskCallback<Boolean> mCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			dialog.dismiss();
			HDStarApp.remote = setting;
			Intent intent = new Intent(RemoteLoginActivity.this,
					RemoteActivity.class);
			intent.putExtra("remote", setting);
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
			Crouton.makeText(RemoteLoginActivity.this, msgId, Style.ALERT)
					.show();
		}

	};
}
