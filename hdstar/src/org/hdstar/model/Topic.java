package org.hdstar.model;

import java.util.List;

public class Topic {
	public String title = "";
	public int topicId = 0;
	public String author = "";
	public String lastPost = "";
	public String follow = "";// “回复/查看”信息
	public boolean read = false;
	public boolean locked = false;
	public boolean sticky = false;
	public String postTime = "";
	public List<String> pageIndex = null;// 页码
	public List<Integer> pageList = null;// 页码（url的页码索引，从0开始）
}