package org.hdstar.task;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.util.CustomHttpClient;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class ReplyTask extends MyAsyncTask<Void> {

	public ReplyTask(String cookie) {
		super(cookie);
	}

	@Override
	protected Void doInBackground(String... params) {
		setMessageId(R.string.reply_failed);
		HttpClient client = CustomHttpClient.getHttpClient();
		request = new HttpPost(Const.Urls.REPLY_URL);
		request.setHeader("Cookie", cookie);
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("id", params[0]));
		nvp.add(new BasicNameValuePair("type", "reply"));
		nvp.add(new BasicNameValuePair("body", params[1]));
		try {
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(nvp,
					"UTF-8"));
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 302) {
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
			e.printStackTrace();
		} finally {
			request.releaseConnection();
		}
		return null;
	}
}
