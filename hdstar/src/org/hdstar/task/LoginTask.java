package org.hdstar.task;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;

public class LoginTask extends BaseAsyncTask<String> {

	// @Override
	// protected String doInBackground(String... params) {
	// setMessageId(R.string.login_error);
	// HttpClient client = CustomHttpClient.getHttpClient();
	// request = new HttpPost(TAKE_LOGIN_URL);
	// HttpResponse response;
	// List<NameValuePair> nvp = new ArrayList<NameValuePair>();
	// nvp.add(new BasicNameValuePair("username", params[0]));
	// nvp.add(new BasicNameValuePair("password", params[1]));
	// nvp.add(new BasicNameValuePair("imagestring", params[2]));
	// nvp.add(new BasicNameValuePair("imagehash", params[3]));
	// try {
	// ((HttpPost) request).setEntity(new UrlEncodedFormEntity(nvp,
	// "utf-8"));
	// response = client.execute(request);
	// String location = response.getFirstHeader("Location").getValue();
	// if (!location.equals(HOME_PAGE)) {
	// return null;
	// }
	// String cookieStr = "";
	// Header[] cookies = response.getHeaders("set-cookie");
	// for (Header h : cookies) {
	// String str = h.getValue();
	// cookieStr += str.substring(0, str.indexOf(";") + 1);
	// }
	// // setMessageId(TaskCallback.SUCCESS_MSG_ID);
	// return cookieStr;
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// } catch (ConnectTimeoutException e) {
	// setMessageId(R.string.time_out);
	// CustomHttpClient.restClient();
	// e.printStackTrace();
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (ConnectException e) {
	// request.abort();
	// setMessageId(R.string.connection_refused);
	// e.printStackTrace();
	// } catch (SocketException e) {
	// e.printStackTrace();
	// request.abort();
	// if ("Connection reset by peer".equals(e.getMessage())) {
	// CustomHttpClient.restClient();
	// }
	// } catch (IOException e) {
	// request.abort();
	// e.printStackTrace();
	// } catch (NullPointerException e) {
	// e.printStackTrace();
	// } finally {
	// request.releaseConnection();
	// }
	// return null;
	// }

	@Override
	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		super.execPost(url, nvp, new ResponseParser<String>(
				R.string.login_error) {
			@Override
			public String parse(HttpResponse res, InputStream in) {
				String location = res.getFirstHeader("Location").getValue();
				if (!location.equals(Const.Urls.HOME_PAGE)) {
					return null;
				}
				String cookieStr = "";
				Header[] cookies = res.getHeaders("set-cookie");
				for (Header h : cookies) {
					String str = h.getValue();
					cookieStr += str.substring(0, str.indexOf(";") + 1);
				}
				msgId = ResponseParser.SUCCESS_MSG_ID;
				return cookieStr;
			}
		});
	}

}
