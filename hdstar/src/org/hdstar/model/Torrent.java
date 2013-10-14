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
	public String subTitle;
	public String freeType;
	public boolean bookmark;
	public boolean sticky;
	public int comments;
	public String time;
	public int size;
	public int seeders;
	public int leechers;
	public User uploader;

}
