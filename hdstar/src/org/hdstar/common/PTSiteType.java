package org.hdstar.common;

import org.hdstar.R;

/**
 * 
 * PTվ����. <br/>
 * 
 * @author robust
 */
public enum PTSiteType {
	// chd
	CHDBits("CHDBits", CommonUrls.PTSiteUrls.CHD, new TorrentPageUrl(
			CommonUrls.PTSiteUrls.CHD_TORRENTS_URL, R.string.torrents)),
	// cmct
	CMCT("CMCT", CommonUrls.PTSiteUrls.CMCT, new TorrentPageUrl(
			CommonUrls.PTSiteUrls.CMCT_TORRENTS_URL, R.string.torrents)),
	// hds
	HDSky("HDSky", CommonUrls.HDStar.BASE_URL, new TorrentPageUrl(
			CommonUrls.HDStar.TORRENTS_URL, R.string.torrents)),
	// mt
	MTeam("M-Team", CommonUrls.PTSiteUrls.MT, new TorrentPageUrl[] {
			new TorrentPageUrl(CommonUrls.PTSiteUrls.MT_ADULT_URL,
					R.string.adult),
			new TorrentPageUrl(CommonUrls.PTSiteUrls.MT_TORRENTS_URL,
					R.string.torrents) }),
	// opencd
	OpenCD("OpenCD", CommonUrls.PTSiteUrls.OPEN_CD, new TorrentPageUrl[] {
			new TorrentPageUrl(CommonUrls.PTSiteUrls.OPEN_CD_TORRENTS_URL,
					R.string.torrents),
			new TorrentPageUrl(CommonUrls.PTSiteUrls.OPEN_CD_MUSIC_URL,
					R.string.music) });

	private final String name;// ���ƣ���������ʾ��������Ϊ���ݽ��д��䡢���л���־û�
	private final String url;
	private final TorrentPageUrl[] torrentPages;// ����ҳ���ַ��Ĭ�ϴ򿪵�һ��

	PTSiteType(String name, String url, TorrentPageUrl... pageUrls) {
		this.name = name;
		this.url = url;
		this.torrentPages = pageUrls;
	}

	/**
	 * 
	 * ��ȡ������ʾ������. <br/>
	 * 
	 * @author robust
	 * @return ���ƣ���������ʾ��������Ϊ���ݽ��д��䡢���л���־û�
	 */
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public TorrentPageUrl[] getTorrentPages() {
		return torrentPages;
	}

	/**
	 * 
	 * @author robust
	 * @param name
	 *            enum.name()�����õ���ֵ
	 * @return
	 */
	public static PTSiteType getByName(String name) {
		for (PTSiteType type : PTSiteType.values()) {
			if (type.name().equals(name)) {
				return type;
			}
		}
		return null;
	}

	public static String[] getAllNames() {
		PTSiteType[] types = PTSiteType.values();
		String[] names = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			names[i] = types[i].getName();
		}
		return names;
	}

}
