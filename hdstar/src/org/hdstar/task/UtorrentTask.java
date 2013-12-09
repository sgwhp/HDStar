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
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

public class UtorrentTask<T> extends BaseAsyncTask<T> {
	private String token;
	private String url;
	private String ip;

	public UtorrentTask(String ip) {
		this.ip = ip;
	}

	public static <T> UtorrentTask<T> newInstance(String ip) {
		return new UtorrentTask<T>(ip);
	}

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = HttpClientManager.getHttpClient();
		InputStream in = null;
		try {
			if (token == null) {
				getToken();
			}
			request = new HttpGet(String.format(url, token));
			request.setHeader("Cookie", cookie);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 300) {
				// 令牌过期
				request.abort();
				getToken();
				request = new HttpGet(String.format(url, token));
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

	@Override
	public void execGet(String url, final Type resultType) {
		this.url = url;
		this.execute("");
	}

	@Override
	public void execPost(String url, List<NameValuePair> nvp)
			throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取令牌
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String getToken() throws ClientProtocolException, IOException {
		String tokenUrl = String.format(Const.Urls.UTORRENT_TOKEN_URL, ip);
		request = new HttpGet(tokenUrl);
		HttpClient client = HttpClientManager.getHttpClient();
		HttpResponse response = client.execute(request);
		String result = IOUtils.inputStream2String(response.getEntity()
				.getContent());
		request.abort();
		token = result.substring(result.indexOf(">") + 1,
				result.lastIndexOf("<"));
		return token;
	}

}
