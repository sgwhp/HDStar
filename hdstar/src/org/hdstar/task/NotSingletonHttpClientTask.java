package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.hdstar.R;
import org.hdstar.task.parser.ResponseParser;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

/**
 * 
 * 使用非单例HttpClient的BaseAsyncTask. <br/>
 * 
 * @see org.hdstar.task.BaseAsyncTask
 * @author robust
 */
public class NotSingletonHttpClientTask<T> extends BaseAsyncTask<T> {

	public NotSingletonHttpClientTask() {
	}

	public NotSingletonHttpClientTask(String cookie) {
		this.cookie = cookie;
	}

	public NotSingletonHttpClientTask(String cookie, ResponseParser<T> parser) {
		this.cookie = cookie;
		this.parser = parser;
	}

	public NotSingletonHttpClientTask(HttpRequestBase request,
			ResponseParser<T> parser) {
		this.request = request;
		this.parser = parser;
	}

	public NotSingletonHttpClientTask(String cookie, HttpRequestBase request,
			ResponseParser<T> parser) {
		this.request = request;
		this.parser = parser;
		this.cookie = cookie;
	}

	public static <T> BaseAsyncTask<T> newInstance() {
		return new NotSingletonHttpClientTask<T>();
	}

	public static <T> BaseAsyncTask<T> newInstance(String cookie) {
		return new NotSingletonHttpClientTask<T>(cookie);
	}

	public static <T> BaseAsyncTask<T> newInstance(HttpRequestBase request,
			ResponseParser<T> parser) {
		return new NotSingletonHttpClientTask<T>(request, parser);
	}

	public static <T> BaseAsyncTask<T> newInstance(String cookie,
			HttpRequestBase request, ResponseParser<T> parser) {
		return new NotSingletonHttpClientTask<T>(cookie, request, parser);
	}

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = HttpClientManager.newHttpClient();
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
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ConnectException e) {
			setMessageId(R.string.connection_refused);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.abort();
			client.getConnectionManager().shutdown();
		}
		return null;
	}

}
