package org.hdstar.task;

import java.io.InputStream;

import org.hdstar.R;
import org.hdstar.util.HttpClientManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

public class RemoteLoginTask extends BaseAsyncTask<Boolean> {

	public void auth(String ip, int port, String url, String username,
			String password) {
		HttpHost targetHost = new HttpHost(ip, port, "http");
		DefaultHttpClient client = (DefaultHttpClient) HttpClientManager
				.getHttpClient();
		client.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(username, password));
		request = new HttpGet(url);
		parser = new ResponseParser<Boolean>(R.string.login_error) {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 200) {
					return true;
				}
				return false;
			}
		};
		this.execute("");
	}
}
