package org.hdstar.model;

import java.io.Serializable;

public class RemoteTaskInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String hash;
	public String title;
	public long size;
	public long uploaded;
	public long downloaded;
	public float ratio;
	public long upSpeed;
	public long dlSpeed;
	public int progress = -1;//0-100
	// public int open;
	public TorrentStatus status;
	public String label;

}
