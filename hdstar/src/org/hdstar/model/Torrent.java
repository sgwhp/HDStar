package org.hdstar.model;

import java.io.Serializable;

public class Torrent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int id;
	public String firstClass;
	public String secondClass;
	public String title;
	public String subtitle;
	public String freeType;
	public boolean bookmark;
	public boolean sticky;
	public String comments;
	public String time;
	public String size;
	public String seeders;
	public String leechers;
	public String snatched;
	public String uploader;
	/** 是否已添加到下载框，可能有多种状态，但只有在再点击一次就能转到普通状态的情况下，该值才为真 */
	public boolean rss;

}
