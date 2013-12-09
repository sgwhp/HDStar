package org.hdstar.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RssLabel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String label;
	public int auto;// 布尔值，只有0,1
	public int enabled;// 布尔值，只有0,1
	public String hash;
	public String url;
	public ArrayList<RssItem> items;

}
