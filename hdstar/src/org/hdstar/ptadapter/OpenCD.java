package org.hdstar.ptadapter;

import java.io.IOException;
import java.io.InputStream;
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
import org.hdstar.task.NotSingletonHttpClientTask;
import org.hdstar.task.parser.DefaultGetParser;
import org.hdstar.task.parser.ResponseParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

/**
 * 
 * OpenCD爬虫适配器. <br/>
 * OpenCD在使用单例HttpClient时，一定时间内重复请求会直接跳转到登录页面，具体原因未知，暂时不能使用单例的HttpClient
 * 
 * @author robust
 */
public class OpenCD extends NexusPHP {
	// opencd种子类别图片style属性值类似"width:30px;height:30px;background-image: url(plugin/style/chs/type_408.gif);"
	private Pattern pattern = Pattern.compile("url\\((.*?)\\)");

	public OpenCD() {
		super(PTSiteType.OpenCD);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

	@Override
	protected List<NameValuePair> buildLoginParams(String username,
			String password, String securityCode) {
		List<NameValuePair> nvp = super.buildLoginParams(username, password,
				securityCode);
		nvp.add(new BasicNameValuePair("ssl", "yes"));
		nvp.add(new BasicNameValuePair("trackerssl", "yes"));
		return nvp;
	}

	@Override
	protected void parseTorrentClass(Element tClassCol, Torrent t) {
		Matcher matcher = pattern.matcher(tClassCol.attr("style"));
		if (matcher.find()) {
			t.firstClass = "/" + matcher.group(1);
		}
		Elements classes = tClassCol.child(0).getElementsByTag("img");
		if (classes.size() > 0) {
			matcher = pattern.matcher(classes.get(0).attr("style"));
			if (matcher.find()) {
				t.secondClass = "/" + matcher.group(1);
			}
		}
	}

	@Override
	protected ResponseParser<ArrayList<Torrent>> getTorrentParser() {
		return new ResponseParser<ArrayList<Torrent>>() {

			@Override
			public ArrayList<Torrent> parse(HttpResponse res, InputStream in) {
				try {
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
						// subtitle
						titles = torrentCols.get(1).child(0).child(0).child(0)
								.child(0);
						classes = titles.children();
						t.subtitle = "";
						StringBuilder subtitle = new StringBuilder();
						// 第一个是主标题，第二个是<br>
						for (int j = 2; j < classes.size(); j++) {
							subtitle.append(classes.get(j).text()).append("|");
						}
						if (subtitle.length() > 1) {
							subtitle.deleteCharAt(subtitle.length() - 1);
						}
						t.subtitle = subtitle.toString();
						// sticky
						classes = titles.getElementsByTag("img");
						if (classes.size() > 0) {
							if (Const.TorrentTags.STICKY.equals(classes.get(0)
									.attr("class"))) {
								t.sticky = true;
							}
						}
						// title
						url = titles.getElementsByTag("a").get(0);
						t.title = url.attr("title");
						urlStr = url.attr("href");
						Matcher matcher = pattern.matcher(urlStr);
						if (matcher.find()) {
							t.id = Integer.parseInt(matcher.group(1));
						}
						// freetype
						classes = torrentCols.get(1).child(0).child(0).child(0)
								.child(1).getElementsByTag("img");
						if (classes.size() > 0) {
							t.freeType = classes.get(0).attr("class");
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
		BaseAsyncTask<ArrayList<Torrent>> task = NotSingletonHttpClientTask
				.newInstance(cookie, get, getTorrentParser());
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> bookmark(String torrentId) {
		HttpGet get = new HttpGet(String.format(CommonUrls.NEXUSPHP_BOOKMARK,
				mType.getUrl(), torrentId));
		BaseAsyncTask<Boolean> task = NotSingletonHttpClientTask.newInstance(
				cookie, get, new DefaultGetParser());
		return task;
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		HttpGet get = new HttpGet(String.format(
				CommonUrls.PTSiteUrls.CMCT_RSS_DOWNLOAD_URL, torrentId));
		BaseAsyncTask<Boolean> task = NotSingletonHttpClientTask.newInstance(
				cookie, get, new DefaultGetParser());
		return task;
	}

	@Override
	protected void parseRssDownload(Element tRssRol, Torrent t, int index) {
		if ("bookmark_rss".equals(tRssRol.getElementById("addtorss" + index)
				.child(0).attr("class"))) {
			t.rss = true;
		}
	}

}
