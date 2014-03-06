package org.hdstar.model;

import java.util.List;

public class Topic {
	public String title = "";
	public int topicId = 0;
	public String author = "";
	public String lastPost = "";
	/** “回复/查看”信息 */
	public String follow = "";
	public boolean read = false;
	public boolean locked = false;
	public boolean sticky = false;
	public String postTime = "";
	/** 页码 */
	public List<String> pageIndex = null;
	/** 页码（url的页码索引，从0开始） */
	public List<Integer> pageList = null;
}
