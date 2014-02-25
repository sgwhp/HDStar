package org.hdstar.ptadapter;

import org.hdstar.common.CommonUrls;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.DefaultGetParser;
import org.jsoup.nodes.Element;

import ch.boye.httpclientandroidlib.client.methods.HttpGet;

public class CHDBits extends NexusPHP {

	public CHDBits() {
		super(PTSiteType.CHDBits);
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		HttpGet get = new HttpGet(String.format(
				CommonUrls.PTSiteUrls.CHD_RSS_DOWNLOAD_URL, torrentId));
		BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(cookie, get,
				new DefaultGetParser());
		return task;
	}

	@Override
	protected void parseRssDownload(Element tRssRol, Torrent t, int index) {
		if ("delrssdown".equals(tRssRol.getElementById("rssdown" + (index))
				.child(0).attr("class"))) {
			t.rss = true;
		}
	}

}
