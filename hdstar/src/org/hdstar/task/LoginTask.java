package org.hdstar.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.util.CustomHttpClient;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class LoginTask extends MyAsyncTask<String> {
	public LoginTask() {
		super("");
	}

	private final String TAKE_LOGIN_URL = Const.Urls.BASE_URL
			+ "/takelogin.php";
	private final String HOME_PAGE = Const.Urls.BASE_URL + "/index.php";

	@Override
	protected String doInBackground(String... params) {
		setMessageId(R.string.login_error);
		HttpClient client = CustomHttpClient.getHttpClient();
		request = new HttpPost(TAKE_LOGIN_URL);
		HttpResponse response;
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("username", params[0]));
		nvp.add(new BasicNameValuePair("password", params[1]));
		nvp.add(new BasicNameValuePair("imagestring", params[2]));
		nvp.add(new BasicNameValuePair("imagehash", params[3]));
		try {
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(nvp,
					"utf-8"));
			response = client.execute(request);
			String location = response.getFirstHeader("Location").getValue();
			if (!location.equals(HOME_PAGE)) {
				return null;
			}
			String cookieStr = "";
			Header[] cookies = response.getHeaders("set-cookie");
			for (Header h : cookies) {
				String str = h.getValue();
				cookieStr += str.substring(0, str.indexOf(";") + 1);
			}
			setMessageId(TaskCallback.SUCCESS_MSG_ID);
			return cookieStr;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (ClientProtocolException e) {
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
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			request.releaseConnection();
		}
		return null;
	}

	public void abort() {
		request.abort();
	}

}
