package org.hdstar.common;

/**
 * 
 * PTվ����. <br/>
 * ����: 2014��2��10�� ����10:18:39 <br/>
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
