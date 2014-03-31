package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.task.parser.ResponseParser;
import org.hdstar.util.HttpClientManager;
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

import com.google.zxing.client.android.common.executor.AsyncTaskExecInterface;
import com.google.zxing.client.android.common.executor.AsyncTaskExecManager;

/**
 * 基础请求任务. <br/>
 * 
 * @author robust
 */
public class BaseAsyncTask<T> extends AsyncTask<String, Integer, T> {
	// 保证Android 3.0以上系统的AsyncTask也能多线程同时执行的工具
	protected static final AsyncTaskExecInterface taskExec = new AsyncTaskExecManager()
			.build();
	protected String cookie = "";
	protected HttpRequestBase request = null;
	protected TaskCallback<T> mCallback;
	protected ResponseParser<T> parser;
	/** 是否被取消 */
	protected boolean interrupted = false;
	protected boolean needContent = true;

	public BaseAsyncTask() {
	}

	public BaseAsyncTask(String cookie) {
		this.cookie = cookie;
	}

	public BaseAsyncTask(String cookie, ResponseParser<T> parser) {
		this.cookie = cookie;
		this.parser = parser;
	}

	public BaseAsyncTask(HttpRequestBase request, ResponseParser<T> parser) {
		this.request = request;
		this.parser = parser;
	}

	public BaseAsyncTask(String cookie, HttpRequestBase request,
			ResponseParser<T> parser) {
		this.request = request;
		this.parser = parser;
		this.cookie = cookie;
	}

	public static <T> BaseAsyncTask<T> newInstance() {
		return new BaseAsyncTask<T>();
	}

	public static <T> BaseAsyncTask<T> newInstance(String cookie) {
		return new BaseAsyncTask<T>(cookie);
	}

	public static <T> BaseAsyncTask<T> newInstance(HttpRequestBase request,
			ResponseParser<T> parser) {
		return new BaseAsyncTask<T>(request, parser);
	}

	public static <T> BaseAsyncTask<T> newInstance(String cookie,
			HttpRequestBase request, ResponseParser<T> parser) {
		return new BaseAsyncTask<T>(cookie, request, parser);
	}

	/** 设置callback */
	public void attach(TaskCallback<T> callback) {
		mCallback = callback;
	}

	/**
	 * 
	 * 删除callback引用，停止请求. <br/>
	 */
	public void detach() {
		interrupted = true;
		mCallback = null;
		if (request != null) {
			request.abort();
		}
	}

	public void abort() {
		interrupted = true;
		parser = null;
		if (request != null) {
			request.abort();
		}
	}

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = HttpClientManager.getHttpClient();
		InputStream in = null;
		request.setHeader("Cookie", cookie);
		try {
			HttpResponse response = client.execute(request);
			if (needContent) {
				in = response.getEntity().getContent();
			}
			if (parser != null) {
				return parser.parse(response, in);
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			setMessageId(R.string.time_out);
			HttpClientManager.getHttpClient().getConnectionManager()
					.closeExpiredConnections();
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			HttpClientManager.getHttpClient().getConnectionManager()
					.closeExpiredConnections();
			// HttpClientManager.restClient();
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
				// TODO HttpClientManager.restClient();
			}
		} catch (IOException e) {
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
			mCallback.onFail(parser.getMessageId());
		}
	}

	/**
	 * 
	 * 设置执行完毕后要显示的消息资源id. <br/>
	 * 
	 * @param msgId
	 *            要显示的消息资源id
	 */
	public void setMessageId(int msgId) {
		parser.setMessageId(msgId);
	}

	/**
	 * 
	 * 是否需要打开Response的InputStream. <br/>
	 * 
	 * @return
	 */
	public boolean isNeedContent() {
		return needContent;
	}

	/**
	 * 设置是否需要打开Response的InputStream
	 * 
	 * @param needContent
	 */
	public void setNeedContent(boolean needContent) {
		this.needContent = needContent;
	}

	public void execGet(String url, final Type resultType) {
		request = new HttpGet(url);
		taskExec.execute(this);
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
		taskExec.execute(this);
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

	/**
	 * 
	 * 在线程池中执行，Android 3.0以上系统的AsyncTask是单线程的. <br/>
	 * 
	 * @param task
	 * @param args
	 */
	public static <T> void commit(AsyncTask<T, ?, ?> task, T... args) {
		taskExec.execute(task, args);
	}

	/**
	 * 
	 * 任务回调 <br/>
	 * 
	 * @author robust
	 */
	public static interface TaskCallback<T> {

		/**
		 * 任务执行完成 <br/>
		 * onComplete还是onFail被调用取决于ResponseParser的isSucceeded方法
		 * 
		 * @see org.hdstar.task.parser.ResponseParser
		 * 
		 * @param result
		 */
		public void onComplete(T result);

		/**
		 * 任务被取消 <br/>
		 */
		public void onCancel();

		/**
		 * 任务执行失败 <br/>
		 * onComplete还是onFail被调用取决于ResponseParser的isSucceeded方法
		 * 
		 * @see org.hdstar.task.parser.ResponseParser
		 * 
		 * @param msgId
		 *            失败提示的资源文件id
		 */
		public void onFail(Integer msgId);
	}
}
