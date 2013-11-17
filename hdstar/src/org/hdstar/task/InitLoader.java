package org.hdstar.task;

import java.util.ArrayList;

import org.hdstar.model.Topic;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.SoundPoolManager;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class InitLoader extends AsyncTaskLoader<ArrayList<Topic>> {

	public InitLoader(Context context) {
		super(context);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		stopLoading();
	}

	@Override
	public ArrayList<Topic> loadInBackground() {
		// HDStarApp.init(this.getContext());
		SoundPoolManager.load(this.getContext());
		HttpClientManager.getHttpClient();
		return null;
	}

}
