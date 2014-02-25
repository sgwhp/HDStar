package org.hdstar.ptadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.DefaultGetParser;
import org.hdstar.task.ResponseParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class HDWing extends PTAdapter {

	public HDWing() {
		super(PTSiteType.HDWing);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

	@Override
	public BaseAsyncTask<Bitmap> getSecurityImage() {
		ResponseParser<Bitmap> parser = new ResponseParser<Bitmap>() {

			@Override
			public Bitmap parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() != 200) {
					return null;
				}
				byte[] image;
				try {
					image = EntityUtils.toByteArray(res.getEntity());
					Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0,
							image.length);
					msgId = SUCCESS_MSG_ID;
					return mBitmap;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		HttpGet get = new HttpGet(CommonUrls.PTSiteUrls.HDW_GET_SECURITY_IMG);
		BaseAsyncTask<Bitmap> task = new BaseAsyncTask<Bitmap>(get, parser);
		task.setNeedContent(false);
		return task;
	}

	@Override
	public BaseAsyncTask<String> login(String username, String password,
			String securityCode) {
		HttpPost post = new HttpPost(CommonUrls.PTSiteUrls.HDW_TAKE_LOGIN);
		List<BasicNameValuePair> nvp = new ArrayList<BasicNameValuePair>();
		nvp.add(new BasicNameValuePair("code", securityCode));
		nvp.add(new BasicNameValuePair("password", password));
		nvp.add(new BasicNameValuePair("submit", "登录"));
		nvp.add(new BasicNameValuePair("username", username));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp));
			ResponseParser<String> parser = new ResponseParser<String>() {

				@Override
				public String parse(HttpResponse res, InputStream in) {
					if (res.getFirstHeader("Location") == null) {
						return null;
					}
					String location = res.getFirstHeader("Location").getValue();
					if (location == null
							|| !location
									.equals(CommonUrls.PTSiteUrls.HDW_HOME_PAGE)) {
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
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> logout() {
		HttpGet get = new HttpGet(CommonUrls.PTSiteUrls.HDW_LOGOUT);
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
				msgId = ResponseParser.SUCCESS_MSG_ID;
				return true;
			}
		};
		BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>(get, parser);
		task.setNeedContent(false);
		return task;
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
			t.firstClass = classes.get(0).attr("src");
			t.firstClass = "hdw"
					+ t.firstClass.substring(t.firstClass.lastIndexOf("/"), t.firstClass.indexOf("."));
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
		String src = tRssRol.getElementById("bi_" + t.id).attr("src");
		if ("/images/basket_added.gif".equals(src)) {
			t.rss = true;
		}
	}

	@Override
	public BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords) {
		String url = String.format(Locale.getDefault(), torrentsUrl, page);
		if (keywords != null && !keywords.equals("")) {
			url += "&search=" + keywords;
		}
		ResponseParser<ArrayList<Torrent>> parser = new ResponseParser<ArrayList<Torrent>>() {

			@Override
			public ArrayList<Torrent> parse(HttpResponse res, InputStream in) {
				try {
					Document doc = Jsoup.parse(in, Const.CHARSET,
							mType.getUrl());
					ArrayList<Torrent> torrents = new ArrayList<Torrent>();
					Torrent t;
					Elements eTorrents = doc
							.getElementsByClass("torrents_list").get(0)
							.child(0).children();
					Elements torrentCols, imgs;
					Element titles;
					Element url;
					String urlStr;
					Pattern pattern = Pattern.compile("id=(\\d+)");
					for (int i = 1; i < eTorrents.size(); i++) {
						t = new Torrent();
						torrentCols = eTorrents.get(i).children();
						parseTorrentClass(torrentCols.get(0), t);
						// freetype sticky
						titles = torrentCols.get(1);
						if (titles.child(0).attr("class").contains("sticky")) {
							t.sticky = true;
						}
						url = titles.child(0).child(0).child(0).child(1);
						imgs = url.getElementsByTag("a");
						if (imgs.size() > 0) {
							t.freeType = imgs.get(imgs.size() - 1).attr("src");
						}
						t.subtitle = url.ownText();
						// child(0)是<b>标签
						t.title = url.child(0).text();
						// titles
						urlStr = imgs.get(0).attr("href");
						Matcher matcher = pattern.matcher(urlStr);
						if (matcher.find()) {
							t.id = Integer.parseInt(matcher.group(1));
						}
						// bookmark不解析，hdw种子列表无法显示种子的收藏状态
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
					msgId = ResponseParser.SUCCESS_MSG_ID;
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
		HttpGet get = new HttpGet(url);
		BaseAsyncTask<ArrayList<Torrent>> task = BaseAsyncTask.newInstance(
				cookie, get, parser);
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> bookmark(String torrentId) {
		HttpGet get = new HttpGet(String.format(
				CommonUrls.PTSiteUrls.HDW_BOOKMARK, torrentId));
		BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
				new DefaultGetParser());
		return task;
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		List<BasicNameValuePair> nvp = new ArrayList<BasicNameValuePair>();
		nvp.add(new BasicNameValuePair("torrentid", torrentId));
		HttpPost post = new HttpPost(String.format(
				CommonUrls.PTSiteUrls.HDW_RSS_DOWNLOAD_URL,
				System.currentTimeMillis()));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp));
			BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie,
					post, new DefaultGetParser());
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
