package org.hdstar.task;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.util.CustomHttpClient;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class NewTopicTask extends MyAsyncTask<Void> {
	public NewTopicTask(String cookie) {
		super(cookie);
	}

	private final String url = "http://hdsky.me/forums.php?action=post";

	@Override
	protected Void doInBackground(String... params) {
		setMessageId(R.string.add_topic_failed);
		request = new HttpPost(url);
		request.setHeader("Cookie", cookie);
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("id", params[2]));
		nvp.add(new BasicNameValuePair("type", "new"));
		nvp.add(new BasicNameValuePair("subject", params[0]));
		nvp.add(new BasicNameValuePair("color", "0"));
		nvp.add(new BasicNameValuePair("font", "0"));
		nvp.add(new BasicNameValuePair("size", "0"));
		nvp.add(new BasicNameValuePair("body", params[1]));
		try {
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(nvp,
					"UTF-8"));
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 302
					&& response.getFirstHeader("Location").getValue()
							.contains("action=viewtopic")) {
				setMessageId(TaskCallback.SUCCESS_MSG_ID);
			}
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
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
			request.abort();
			e.printStackTrace();
		} finally {
			request.releaseConnection();
		}
		return null;
	}

}
