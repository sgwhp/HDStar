package org.hdstar.ptadapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class MTeam extends NexusPHP {
	// mt种子类别图片style属性值类似"background-image: url(pic/category/chd/scenetorrents/en/cat_hd.gif);"
	private Pattern pattern = Pattern.compile("url\\((.*?)\\)");

	public MTeam() {
		super(PTSiteType.MTeam);
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
		Elements classes = tClassCol.child(0).getElementsByTag("img");
		if (classes.size() > 0) {
			Matcher matcher = pattern.matcher(classes.get(0).attr("style"));
			if (matcher.find()) {
				t.firstClass = "/" + matcher.group(1);
			}
		}
	}

}
