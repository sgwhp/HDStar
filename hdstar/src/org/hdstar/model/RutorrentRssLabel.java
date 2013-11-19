package org.hdstar.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RutorrentRssLabel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String label;
	public boolean auto;
	public boolean enabled;
	public String hash;
	public String url;
	public ArrayList<RutorrentRssItem> items;

}
