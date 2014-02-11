package org.hdstar.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class FetchSecurityImgTask extends BaseAsyncTask<Bitmap> {
	private String imageHash = null;
	private String url;

	public FetchSecurityImgTask(String url) {
		this.url = url;
	}

	public String getImageHash() {
		return imageHash;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		imageHash = getImageHash(String.format(CommonUrls.NEXUSPHP_LOGIN_URL,
				url));
		return downloadImage(String.format(
				CommonUrls.NEXUSPHP_FETCH_SECURITY_IMAGE_URL, url, imageHash));
	}

	// @Override
	// public void execGet(String url, final Type resultType) {
	// taskExec.execute(this, url);
	// }

	@SuppressWarnings("resource")
	String getImageHash(String url) {
		HttpClient client = HttpClientManager.getHttpClient();
		request = new HttpGet(url);
		BufferedReader reader = null;
		InputStream in = null;
		try {
			String str;
			Pattern pattern = Pattern.compile("imagehash=(.*?)\"");
			Matcher matcher;
			HttpResponse response = client.execute(request);
			in = response.getEntity().getContent();
			reader = new BufferedReader(new InputStreamReader(in));
			while ((str = reader.readLine()) != null) {
				matcher = pattern.matcher(str);
				if (matcher.find()) {
					return matcher.group(1);
				}
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

	public Bitmap downloadImage(String url) {
		HttpClient httpClient = HttpClientManager.getHttpClient();
		request = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(request);
			byte[] image = EntityUtils.toByteArray(response.getEntity());
			Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0,
					image.length);
			return mBitmap;
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
			request.releaseConnection();
			request.abort();
		}
		return null;
	}

}
