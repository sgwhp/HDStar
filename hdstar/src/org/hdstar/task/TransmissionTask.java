package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.hdstar.R;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

public class TransmissionTask<T> extends BaseAsyncTask<T> {
	private String sessionId;
	private String url;
	private final String SESSION_HEADER = "X-Transmission-Session-Id";

	public TransmissionTask(String url, ResponseParser<T> parser) {
		this.url = url;
		this.parser = parser;
	}

	public static <T> TransmissionTask<T> newInstance(String url,
			ResponseParser<T> parser) {
		return new TransmissionTask<T>(url, parser);
	}

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = HttpClientManager.getHttpClient();
		InputStream in = null;
		try {
			request = new HttpGet(String.format(url, sessionId));
			if (sessionId != null) {
				request.addHeader(SESSION_HEADER, sessionId);
			}
			request.setHeader("Cookie", cookie);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 409) {
				// session¹ýÆÚ
				request.abort();
				sessionId = response.getFirstHeader(SESSION_HEADER).getValue();
				request.addHeader(SESSION_HEADER, sessionId);
				response = client.execute(request);
			}
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
			if ("Connection reset by peer".equals(e.getMessage())) {
				HttpClientManager.restClient();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.releaseConnection();
			request.abort();
		}
		return null;
	}
}
