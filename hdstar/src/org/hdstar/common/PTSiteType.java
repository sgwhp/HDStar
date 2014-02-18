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

	private final String name;// 名称，仅用作显示，不能作为数据进行传输、序列化或持久化
	private final String url;

	PTSiteType(String name, String url) {
		this.name = name;
		this.url = url;
	}

	/**
	 * 
	 * 获取用来显示的名称. <br/>
	 * 
	 * @author robust
	 * @return 名称，仅用作显示，不能作为数据进行传输、序列化或持久化
	 */
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * 
	 * @author robust
	 * @param name
	 *            enum.name()方法得到的值
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
