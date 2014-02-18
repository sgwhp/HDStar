package org.hdstar.common;

/**
 * 
 * PTվ����. <br/>
 * ����: 2014��2��10�� ����10:18:39 <br/>
 * 
 * @author robust
 */
public enum PTSiteType {
	CHDBits("CHDBits", CommonUrls.PTSiteUrls.CHDBITS), CMCT("CMCT",
			CommonUrls.PTSiteUrls.CMCT), HDSky("HDSky",
			CommonUrls.HDStar.BASE_URL), MTeam("M-Team",
			CommonUrls.PTSiteUrls.M_TEAM), OpenCD("OpenCD",
			CommonUrls.PTSiteUrls.OPEN_CD);

	private final String name;// ���ƣ���������ʾ��������Ϊ���ݽ��д��䡢���л���־û�
	private final String url;

	PTSiteType(String name, String url) {
		this.name = name;
		this.url = url;
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
