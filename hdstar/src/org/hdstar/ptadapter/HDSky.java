package org.hdstar.ptadapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class HDSky extends NexusPHP {
	private String imageHash;

	@Override
	public BaseAsyncTask<Bitmap> getSecurityImage() {
		return new FetchSecurityImgTask(mUrl);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

	@Override
	public BaseAsyncTask<String> login(String username, String password,
			String securityCode) {
		HttpPost post = new HttpPost(String.format(
				CommonUrls.NEXUSPHP_TAKE_LOGIN_URL, mUrl));
		try {
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("username", username));
			nvp.add(new BasicNameValuePair("password", password));
			nvp.add(new BasicNameValuePair("imagestring", securityCode));
			nvp.add(new BasicNameValuePair("imagehash", imageHash));
			post.setEntity(new UrlEncodedFormEntity(nvp, Const.CHARSET));
			ResponseParser<String> parser = new ResponseParser<String>(
					R.string.login_error) {

				@Override
				public String parse(HttpResponse res, InputStream in) {
					if (res.getFirstHeader("Location") == null) {
						return null;
					}
					String location = res.getFirstHeader("Location").getValue();
					if (location == null
							|| !location.equals(CommonUrls.NEXUSPHP_HOME_PAGE)) {
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
			};
			BaseAsyncTask<String> task = new BaseAsyncTask<String>(post, parser);
			task.setNeedContent(false);
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page) {
		return null;
	}

	private class FetchSecurityImgTask extends BaseAsyncTask<Bitmap> {
		private String url;

		public FetchSecurityImgTask(String url) {
			this.url = url;
			parser = new ResponseParser<Bitmap>() {

				@Override
				public Bitmap parse(HttpResponse res, InputStream in) {
					return null;
				}
			};
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			imageHash = getImageHash(String.format(
					CommonUrls.NEXUSPHP_LOGIN_URL, url));
			return downloadImage(String.format(
					CommonUrls.NEXUSPHP_FETCH_SECURITY_IMAGE_URL, url,
					imageHash));
		}

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
				parser.setSucceeded();
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
}
