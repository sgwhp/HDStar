package org.hdstar.component.activity;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.Topic;
import org.hdstar.task.InitLoader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class InitActivity extends FragmentActivity implements
		LoaderCallbacks<ArrayList<Topic>> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra("exit", false)) {
			finish();
			return;
		}
		if (intent.getBooleanExtra("message", false)) {
			intent.setClass(this, MessageActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public Loader<ArrayList<Topic>> onCreateLoader(int arg0, Bundle arg1) {
		return new InitLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Topic>> arg0,
			ArrayList<Topic> arg1) {
		Intent intent;
		if (HDStarApp.cookies != null) {
			intent = new Intent(this, ForumsActivity.class);
		} else {
			intent = new Intent(this, LoginActivity.class);
		}
		startActivity(intent);
		((HDStarApp) getApplicationContext()).checkMessage();
		// finish();
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Topic>> arg0) {
	}

}
