package org.hdstar.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RssLabel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String label;
	public int auto;// ����ֵ��ֻ��0,1
	public int enabled;// ����ֵ��ֻ��0,1
	public String hash;
	public String url;
	public ArrayList<RssItem> items;

}
