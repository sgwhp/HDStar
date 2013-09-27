package org.hdstar.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;

import org.hdstar.R;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ViewMessageTask extends MyAsyncTask<String> {

	public ViewMessageTask(String cookie) {
		super(cookie);
	}

	/**
	 * @param params
	 *            params[0] url
	 */
	@Override
	protected String doInBackground(String... params) {
		request = new HttpGet(params[0]);
		request.setHeader("Cookie", cookie);
		BufferedInputStream in = null;
		HttpClient client = CustomHttpClient.getHttpClient();
		try {
			HttpResponse response = client.execute(request);
			in = new BufferedInputStream(response.getEntity().getContent(),
					1024);

			Gson gson = new Gson();
			ResponseWrapper<String> wrapper = gson.fromJson(
					new InputStreamReader(in),
					new TypeToken<ResponseWrapper<String>>() {
					}.getType());
			return wrapper.body;
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			request.abort();
			e.printStackTrace();
		} catch (ConnectException e) {
			request.abort();
			setMessageId(R.string.connection_refused);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			request.abort();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (IOException e) {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.abort();
			e.printStackTrace();
		} catch (Exception e) {
			request.abort();
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.releaseConnection();
		}
		return null;
	}

}
