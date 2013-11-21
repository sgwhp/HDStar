package org.hdstar.task;

import java.io.InputStream;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.util.HttpClientManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

public class RemoteLoginTask extends BaseAsyncTask<Boolean> {

	public void auth(String ip, int port, String username,
			String password) {
		HttpHost targetHost = new HttpHost(ip, port, "http");
		DefaultHttpClient client = (DefaultHttpClient) HttpClientManager
				.getHttpClient();
		client.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(username, password));
		request = new HttpGet(String.format(Const.Urls.RUTORRENT_HOME_PAGE, ip));
		parser = new ResponseParser<Boolean>(R.string.login_error) {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 301) {
					msgId = SUCCESS_MSG_ID;
					return true;
				}

				return false;
			}
		};
		this.execute("");
	}
	
	public void auth(String ip, String username,
			String password) {
		auth(ip, 80, username, password);
	}
}
