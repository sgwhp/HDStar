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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.parser.DefaultGetParser;
import org.hdstar.task.parser.ResponseParser;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

/**
 * 
 * NexushPHP模板的站点适配器. <br/>
 * 
 * @author robust
 */
public class NexusPHP extends PTAdapter {

	/** 验证码的hash值 */
	protected String imageHash;

	public NexusPHP(PTSiteType type) {
		super(type);
	}

	@Override
	public BaseAsyncTask<Bitmap> getSecurityImage() {
		return new FetchSecurityImgTask(mType.getUrl());
	}

	/**
	 * 构建请求参数 <br/>
	 * 
	 * @param username
	 * @param password
	 * @param securityCode
	 * @return
	 */
	protected List<NameValuePair> buildLoginParams(String username,
			String password, String securityCode) {
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("username", username));
		nvp.add(new BasicNameValuePair("password", password));
		if (needSecurityCode()) {
			nvp.add(new BasicNameValuePair("imagestring", securityCode));
			nvp.add(new BasicNameValuePair("imagehash", imageHash));
		}
		return nvp;
	}

	@Override
	public BaseAsyncTask<String> login(String username, String password,
			String securityCode) {
		HttpPost post = new HttpPost(String.format(
				CommonUrls.NEXUSPHP_TAKE_LOGIN_URL, mType.getUrl()));
		try {
			post.setEntity(new UrlEncodedFormEntity(buildLoginParams(username,
					password, securityCode), Const.CHARSET));
			ResponseParser<String> parser = new ResponseParser<String>(
					R.string.login_error) {

				@Override
				public String parse(HttpResponse res, InputStream in) {
					if (res.getFirstHeader("Location") == null) {
						return null;
					}
					String location = res.getFirstHeader("Location").getValue();
					if (location == null
							|| !location.equals(String.format(
									CommonUrls.NEXUSPHP_HOME_PAGE,
									mType.getUrl()))) {
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

	/**
	 * 
	 * 解析某行种子中的类别列. <br/>
	 * 
	 * @param tClassCol
	 *            类别列，一般为第一列
	 * @param t
	 */
	protected void parseTorrentClass(Element tClassCol, Torrent t) {
		Elements classes = tClassCol.getElementsByTag("img");
		if (classes.size() > 0) {
			t.firstClass = classes.get(0).attr("class");
		}
		if (classes.size() > 1) {
			t.secondClass = classes.get(1).attr("class");
		}
		if ("".equals(t.secondClass)) {
			t.secondClass = "si_notallowed";
		}
	}

	/**
	 * 
	 * 解析某行种子中操作列的下载框状态. <br/>
	 * 
	 * @param tRssRol
	 *            操作列，一般为第二列，与标题在同一列
	 * @param t
	 * @param index
	 *            种子所在行数
	 */
	protected void parseRssDownload(Element tRssRol, Torrent t, int index) {
	}

	/**
	 * 
	 * 获取种子解析器. <br/>
	 * 
	 * @return
	 */
	protected ResponseParser<ArrayList<Torrent>> getTorrentParser() {
		return new ResponseParser<ArrayList<Torrent>>() {

			@Override
			public ArrayList<Torrent> parse(HttpResponse res, InputStream in) {
				try {
					Header header = res.getFirstHeader("Location");
					if (res.getStatusLine().getStatusCode() == 302
							&& header != null
							&& header.getValue().startsWith(
									String.format(
											CommonUrls.NEXUSPHP_LOGIN_URL,
											mType.getUrl()))) {
						// 未登录
						msgId = R.string.not_login;
						return null;
					}
					Document doc = Jsoup.parse(in, Const.CHARSET,
							mType.getUrl());
					ArrayList<Torrent> torrents = new ArrayList<Torrent>();
					Torrent t;
					Elements eTorrents = doc.getElementsByClass("torrents")
							.get(0).child(0).children();
					Elements torrentCols;
					Elements classes;
					Element titles;
					Element url;
					String urlStr;
					Pattern pattern = Pattern.compile("id=(\\d+)");
					for (int i = 1; i < eTorrents.size(); i++) {
						t = new Torrent();
						torrentCols = eTorrents.get(i).children();
						parseTorrentClass(torrentCols.get(0), t);
						// freetype sticky
						titles = torrentCols.get(1).child(0).child(0).child(0)
								.child(0);
						t.subtitle = titles.ownText();
						classes = titles.getElementsByTag("img");
						for (int j = 0; j < classes.size(); j++) {
							String klass = classes.get(j).attr("class");
							if (Const.TorrentTags.STICKY.equals(klass)) {
								t.sticky = true;
								continue;
							}
							t.freeType = klass;
						}
						// titles
						url = titles.getElementsByTag("a").get(0);
						t.title = url.attr("title");
						urlStr = url.attr("href");
						Matcher matcher = pattern.matcher(urlStr);
						if (matcher.find()) {
							t.id = Integer.parseInt(matcher.group(1));
						}
						// bookmark
						String bookmark = torrentCols.get(1)
								.getElementById("bookmark" + (i - 1)).child(0)
								.attr("alt");
						if (Const.TorrentTags.BOOKMARKED.equals(bookmark)) {
							t.bookmark = true;
						}
						// 下载框
						parseRssDownload(torrentCols.get(1), t, i - 1);
						// comments
						t.comments = torrentCols.get(2).text();
						// time
						t.time = torrentCols.get(3).text();
						// size
						t.size = torrentCols.get(4).text();
						// seeders
						t.seeders = torrentCols.get(5).text();
						// leachers
						t.leechers = torrentCols.get(6).text();
						// snatched
						t.snatched = torrentCols.get(7).text();
						// uploader
						t.uploader = torrentCols.get(8).text();
						torrents.add(t);
					}
					setSucceeded();
					return torrents;
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	@Override
	public BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords) {
		String url = String.format(Locale.getDefault(), torrentsUrl, page);
		if (keywords != null && !keywords.equals("")) {
			url += "&search=" + keywords;
		}
		HttpGet get = new HttpGet(url);
		BaseAsyncTask<ArrayList<Torrent>> task = BaseAsyncTask.newInstance(
				cookie, get, getTorrentParser());
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> logout() {
		HttpGet get = new HttpGet(String.format(CommonUrls.NEXUSPHP_LOGOUT_URL,
				mType.getUrl()));
		ResponseParser<Boolean> parser = new ResponseParser<Boolean>() {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getFirstHeader("Location") == null) {
					return false;
				}
				String location = res.getFirstHeader("Location").getValue();
				if (location == null || !location.equals(mType.getUrl() + "/")) {
					return false;
				}
				setSucceeded();
				return true;
			}
		};
		BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>(get, parser);
		task.setNeedContent(false);
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> bookmark(String torrentId) {
		HttpGet get = new HttpGet(String.format(CommonUrls.NEXUSPHP_BOOKMARK,
				mType.getUrl(), torrentId));
		BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
				new DefaultGetParser());
		return task;
	}

	/**
	 * 
	 * 下载nexusphp验证码请求任务. <br/>
	 * 
	 * @author robust
	 */
	private class FetchSecurityImgTask extends BaseAsyncTask<Bitmap> {
		private String url;

		public FetchSecurityImgTask(String url) {
			this.url = url;
			parser = new ResponseParser<Bitmap>(
					R.string.failed_to_download_security_code) {

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

		/**
		 * 
		 * 获取验证码的hash值. <br/>
		 * 
		 * @param url
		 * @return
		 */
		@SuppressWarnings("resource")
		private String getImageHash(String url) {
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
				// if ("Connection reset by peer".equals(e.getMessage())) {
				// HttpClientManager.restClient();
				// }
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

		/**
		 * 下载验证码. <br/>
		 * 
		 * @param url
		 * @return
		 */
		private Bitmap downloadImage(String url) {
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
				// if ("Connection reset by peer".equals(e.getMessage())) {
				// HttpClientManager.restClient();
				// }
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
