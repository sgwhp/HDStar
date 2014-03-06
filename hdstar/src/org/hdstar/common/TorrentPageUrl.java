package org.hdstar.common;

/**
 * 站点种子页面信息. <br/>
 * 
 * @author robust
 */
public class TorrentPageUrl {
	public final String url;
	public final int nameResId;

	public TorrentPageUrl(String url, int nameResId) {
		this.url = url;
		this.nameResId = nameResId;
	}
}
