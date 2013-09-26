package org.hdstar.task;

import org.hdstar.util.CustomHttpClient;

import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;

public abstract class MyAsyncTask<T> extends AsyncTask<String, Integer, T> {
	protected String cookie;
	protected static HttpClient client = CustomHttpClient.getHttpClient();
	protected HttpRequestBase request = null;
	protected TaskCallback<T> mCallback;
	protected boolean interrupted = false;

	MyAsyncTask(String cookie) {
		this.cookie = cookie;
	}

	public void attach(TaskCallback<T> callbacks) {
		mCallback = callbacks;
	}

	public void detach() {
		interrupted = true;
		mCallback = null;
		if (request != null) {
			request.abort();
		}
	}

	public void abort() {
		interrupted = true;
		if (request != null) {
			request.abort();
		}
	}

	protected void onPostExecute(T result) {
		if (mCallback == null) {
			return;
		}
		if (interrupted) {
			mCallback.onComplete(null);
		}
		if (mCallback.isSucceeded()) {
			mCallback.onComplete(result);
		} else {
			mCallback.onFail(mCallback.msgId);
		}
	}

	protected void setMessageId(int msgId) {
		if (mCallback != null) {
			mCallback.msgId = msgId;
		}
	}

	public static abstract class TaskCallback<T> {
		public static final int SUCCESS_MSG_ID = 0;
		protected int msgId = SUCCESS_MSG_ID;

		public int getMessageId() {
			return msgId;
		}

		public boolean isSucceeded() {
			return msgId == 0;
		}

		public abstract void onComplete(T result);

		public abstract void onCancel();

		public abstract void onFail(Integer msgId);
	}
}