package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.RemoteLoginTask;
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

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.remoteClient, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
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
		String acc = accET.getText().toString();
		String pwd = pwdET.getText().toString();

		dialog = new CustomDialog(this, R.string.try_to_login);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (task.getStatus() != AsyncTask.Status.FINISHED)
					task.abort();
			}

		});
		dialog.show();
		task = new RemoteLoginTask();
		task.attach(mCallback);
		task.auth(ip, "http://" + ip + "/rutorrent", acc, pwd);
	}

	private TaskCallback<Boolean> mCallback = new TaskCallback<Boolean>() {

		@Override
		public void onComplete(Boolean result) {
			dialog.dismiss();
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
