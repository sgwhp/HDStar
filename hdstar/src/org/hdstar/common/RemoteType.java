package org.hdstar.common;

public enum RemoteType {
	UTorrent("��Torrent"), RuTorrent("ruTorrent");
	private final String name;

	public String getName() {
		return name;
	}

	RemoteType(String name) {
		this.name = name;
	}
}
