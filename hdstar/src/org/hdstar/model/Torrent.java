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
	/** �Ƿ�����ӵ����ؿ򣬿����ж���״̬����ֻ�����ٵ��һ�ξ���ת����ͨ״̬������£���ֵ��Ϊ�� */
	public boolean rss;

}
