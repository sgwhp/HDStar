package org.hdstar.common;

public enum RemoteType {
	UTorrentRemote("¦ÌTorrent"), RuTorrentRemote("ruTorrent");
	private final String name;

	public String getName() {
		return name;
	}

	RemoteType(String name) {
		this.name = name;
	}
}
