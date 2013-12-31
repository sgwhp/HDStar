package org.hdstar.common;

public enum RemoteType {
	UTorrentRemote("��Torrent"), RuTorrentRemote("ruTorrent");
	private final String name;

	public String getName() {
		return name;
	}

	RemoteType(String name) {
		this.name = name;
	}
}
