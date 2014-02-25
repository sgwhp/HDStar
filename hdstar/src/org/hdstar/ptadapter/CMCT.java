package org.hdstar.ptadapter;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.DefaultGetParser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.client.methods.HttpGet;

public class CMCT extends NexusPHP {

	public CMCT() {
		super(PTSiteType.CMCT);
	}

	@Override
	protected void parseTorrentClass(Element tClassCol, Torrent t) {
		Elements classes = tClassCol.child(0).getElementsByTag("img");
		if (classes.size() > 0) {
			t.firstClass = classes.get(0).attr("style");
			t.firstClass = "cmct"
					+ t.firstClass.substring(t.firstClass.lastIndexOf("/"),
							t.firstClass.indexOf("."));
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
