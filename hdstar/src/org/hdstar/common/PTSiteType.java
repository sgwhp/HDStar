package org.hdstar.common;

/**
 * 
 * PT站类型. <br/>
 * 日期: 2014年2月10日 上午10:18:39 <br/>
 * 
 * @author robust
 */
public enum PTSiteType {
	CHDBits("CHDBits", CommonUrls.PTSiteUrls.CHDBITS), CMCT("CMCT",
			CommonUrls.PTSiteUrls.CMCT), HDSky("HDSky",
			CommonUrls.HDStar.BASE_URL), MTeam("M-Team",
			CommonUrls.PTSiteUrls.M_TEAM), OpenCD("OpenCD",
			CommonUrls.PTSiteUrls.OPEN_CD);

	private final String name;
	private final String url;

	PTSiteType(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public static PTSiteType getByName(String name) {
		for (PTSiteType type : PTSiteType.values()) {
			if (type.name.equals(name)) {
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
