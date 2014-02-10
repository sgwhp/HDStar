package org.hdstar.common;

/**
 * 
 * PT站类型. <br/>
 * 日期: 2014年2月10日 上午10:18:39 <br/>
 * 
 * @author robust
 */
public enum PTSiteType {
	NexusPHP("NexusPHP");

	private final String name;

	PTSiteType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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
