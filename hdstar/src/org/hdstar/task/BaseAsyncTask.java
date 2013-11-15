package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

public class BaseAsyncTask<T> extends AsyncTask<String, Integer, T> {
	protected String cookie;
	protected HttpRequestBase request = null;
	protected TaskCallback<T> mCallback;
	protected ResponseParser<T> parser;
	protected boolean interrupted = false;
	protected boolean needContent = true;

	public BaseAsyncTask() {
		this.cookie = "";
	}

	public BaseAsyncTask(String cookie) {
		this.cookie = cookie;
	}

	public static <T> BaseAsyncTask<T> newInstance(String cookie) {
		return new BaseAsyncTask<T>(cookie);
	}

	public BaseAsyncTask(String cookie, ResponseParser<T> parser) {
		this.cookie = cookie;
		this.parser = parser;
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

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = CustomHttpClient.getHttpClient();
		InputStream in = null;
		request.setHeader("Cookie", cookie);
		try {
			HttpResponse response = client.execute(request);
			if (needContent) {
				in = response.getEntity().getContent();
			}
			return parser.parse(response, in);
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// request.abort();
			e.printStackTrace();
		} catch (ConnectException e) {
			// request.abort();
			setMessageId(R.string.connection_refused);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			// request.abort();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (IOException e) {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			// request.abort();
			e.printStackTrace();
		} catch (Exception e) {
			// request.abort();
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.releaseConnection();
			request.abort();
		}
		return null;
	}

	@Override
	protected void onPostExecute(T result) {
		if (mCallback == null) {
			return;
		}
		if (interrupted) {
			mCallback.onCancel();
			return;
		}
		if (parser.isSucceeded()) {
			mCallback.onComplete(result);
		} else {
			mCallback.onFail(parser.msgId);
		}
	}

	public void setMessageId(int msgId) {
		parser.msgId = msgId;
	}

	public boolean isNeedContent() {
		return needContent;
	}

	public void setNeedContent(boolean needContent) {
		this.needContent = needContent;
	}

	public void execGet(String url, final Type resultType) {
		request = new HttpGet(url);
		this.execute("");
	}

	public void execGet(String url, ResponseParser<T> parser) {
		if (parser == null) {
			throw new IllegalStateException("parser can not be null");
		}
		this.parser = parser;
		execGet(url, parser.getType());
	}

	public void execPost(String url, List<NameValuePair> nvp)
			throws UnsupportedEncodingException {
		if (parser == null) {
			throw new IllegalStateException("parser can not be null");
		}
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(nvp, Const.CHARSET));
		request = post;
		this.execute("");
	}

	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		execPost(url, nvp);
	}

	public void execPost(String url, List<NameValuePair> nvp,
			ResponseParser<T> parser) throws UnsupportedEncodingException {
		this.parser = parser;
		execPost(url, nvp);
	}

	public static interface TaskCallback<T> {

		public abstract void onComplete(T result);

		public abstract void onCancel();

		public abstract void onFail(Integer msgId);
	}
}