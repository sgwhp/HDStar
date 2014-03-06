package org.hdstar.ptadapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.parser.DefaultGetParser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.client.methods.HttpGet;

public class CMCT extends NexusPHP {
	// cmct种子类别图片style属性值类似"background-image: url(pic/category/chd/bluenvelopes/720p.png);"
	private Pattern pattern = Pattern.compile("url\\((.*?)\\)");

	public CMCT() {
		super(PTSiteType.CMCT);
	}

	@Override
	protected void parseTorrentClass(Element tClassCol, Torrent t) {
		Elements classes = tClassCol.child(0).getElementsByTag("img");
		if (classes.size() > 0) {
			Matcher matcher = pattern.matcher(classes.get(0).attr("style"));
			if (matcher.find()) {
				t.firstClass = "/" + matcher.group(1);
			}
		}
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		HttpGet get = new HttpGet(String.format(
				CommonUrls.PTSiteUrls.CMCT_RSS_DOWNLOAD_URL, torrentId));
		BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
				new DefaultGetParser());
		return task;
	}

	@Override
	protected void parseRssDownload(Element tRssRol, Torrent t, int index) {
		if ("subscription".equals(tRssRol
				.getElementById("subscription" + index).child(0).attr("class"))) {
			t.rss = true;
		}
	}

}
