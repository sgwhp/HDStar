package org.hdstar.ptadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HeaderElement;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class TTG extends PTAdapter {
	private String PHPSESSID = null;// sessionId，ttg在cookies中会用到

	public TTG() {
		super(PTSiteType.TTG);
	}

	@Override
	public BaseAsyncTask<String> login(String username, String password,
			String securityCode) {
		HttpPost post = new HttpPost(CommonUrls.PTSiteUrls.TTG_TAKE_LOGIN);
		List<BasicNameValuePair> nvp = new ArrayList<BasicNameValuePair>();
		nvp.add(new BasicNameValuePair("password", password));
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
							|| !location.equals(CommonUrls.PTSiteUrls.TTG_MY)) {
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
		HttpGet get = new HttpGet(CommonUrls.PTSiteUrls.TTG_LOGOUT);
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
	 * 获取sessionId. <br/>
	 * 
	 * @param res
	 *            http响应
	 */
	private void dealWithSessionId(HttpResponse res) {
		Header header = res.getFirstHeader("Set-Cookie");
		if (header == null) {
			return;
		}
		for (HeaderElement element : header.getElements()) {
			if ("PHPSESSID".equals(element.getName())) {
				PHPSESSID = element.getValue();
			}
		}
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
					dealWithSessionId(res);
					Document doc = Jsoup.parse(in, Const.CHARSET,
							mType.getUrl());
					ArrayList<Torrent> torrents = new ArrayList<Torrent>();
					Torrent t;
					Elements eTorrents = doc.getElementById("torrent_table")
							.child(0).children();
					Elements torrentCols, es;
					Element titles;
					Element url;
					String[] strs;
					for (int i = 1; i < eTorrents.size(); i++) {
						t = new Torrent();
						torrentCols = eTorrents.get(i).children();
						parseTorrentClass(torrentCols.get(0), t);
						// freetype sticky
						if (eTorrents.get(i).attr("class").contains("sticky")) {
							t.sticky = true;
						}
						titles = torrentCols.get(1).child(0);
						url = titles.getElementsByTag("a").get(0);
						es = titles.getElementsByTag("img");
						if (es.size() > 0) {
							t.freeType = es.get(es.size() - 1).attr("src");
						}
						// 标题<a>标签内嵌了<b> --> <font>
						titles = url.child(0).child(0);
						t.title = titles.ownText();
						// ttg副标题可能有多个
						es = titles.getElementsByTag("span");
						for (int j = 0; j < es.size(); j++) {
							t.subtitle = es.get(j).text();
						}
						t.id = Integer.parseInt(eTorrents.get(i).attr("id"));
						// bookmark不解析，hdw种子列表无法显示种子的收藏状态
						// 下载框
						if (eTorrents
								.get(i)
								.attr("style")
								.equals("background-color: rgb(135, 206, 250);")) {
							t.rss = true;
						}
						// comments
						t.comments = torrentCols.get(3).text();
						// time
						t.time = torrentCols.get(4).text();
						// size
						t.size = torrentCols.get(6).text();
						// snatched
						t.snatched = torrentCols.get(7).text();
						strs = torrentCols.get(8).text().split("/");
						// seeders
						t.seeders = strs[0];
						// leachers
						t.leechers = strs[1];
						// uploader
						t.uploader = torrentCols.get(9).text();
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
		String cookieStr = cookie;
		if (PHPSESSID != null) {
			cookieStr += "PHPSESSID=" + PHPSESSID + ";";
		}
		BaseAsyncTask<ArrayList<Torrent>> task = BaseAsyncTask.newInstance(
				cookieStr, get, parser);
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> bookmark(String torrentId) {
		HttpGet get = new HttpGet(String.format(
				CommonUrls.PTSiteUrls.TTG_BOOKMARK, torrentId));
		BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
				new TTGDefaultGetParser());
		return task;
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		List<BasicNameValuePair> nvp = new ArrayList<BasicNameValuePair>();
		nvp.add(new BasicNameValuePair("tid", torrentId));
		HttpPost post = new HttpPost(CommonUrls.PTSiteUrls.TTG_RSS_DOWNLOAD_URL);
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp));
			BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie,
					post, new TTGDefaultGetParser());
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private class TTGDefaultGetParser extends ResponseParser<Boolean> {

		@Override
		public Boolean parse(HttpResponse res, InputStream in) {
			dealWithSessionId(res);
			if (res.getStatusLine().getStatusCode() == 200) {
				msgId = SUCCESS_MSG_ID;
				return true;
			}
			return false;
		}

	}

}
