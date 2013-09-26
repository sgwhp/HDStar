package org.hdstar.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.model.Topic;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//org.apache.http.conn.ConnectTimeoutException
//java.net.SocketTimeoutException
public class ViewForumsTask extends MyAsyncTask<List<Topic>> {
	String forumId = "1";

	public ViewForumsTask(String cookie) {
		super(cookie);
	}

	@Override
	protected List<Topic> doInBackground(String... params) {
		setMessageId(R.string.refresh_failed);
		HttpClient client = CustomHttpClient.getHttpClient();
		request = new HttpGet(Const.Urls.SERVER_VIEW_FORUM_URL + "?forumId="
				+ params[0]);
		forumId = params[0];
		BufferedInputStream in = null;
		request.setHeader("Cookie", cookie);
		try {
			HttpResponse response = client.execute(request);
			in = new BufferedInputStream(response.getEntity().getContent(),
					8192);

			Gson gson = new Gson();
			ResponseWrapper<List<Topic>> wrapper = gson.fromJson(
					new InputStreamReader(in),
					new TypeToken<ResponseWrapper<List<Topic>>>() {
					}.getType());
			setMessageId(TaskCallback.SUCCESS_MSG_ID);
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