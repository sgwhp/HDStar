package org.hdstar.common;

public enum RemoteType {
	UTorrent("¦ÌTorrent"), RuTorrent("ruTorrent");
	private final String name;

	public String getName() {
		return name;
	}

	RemoteType(String name) {
		this.name = name;
	}

	public static String[] getAllNames() {
		RemoteType[] types = RemoteType.values();
		String[] names = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			names[i] = types[i].getName();
		}
		return names;
	}
}
