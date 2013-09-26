package org.hdstar.model;

import java.util.List;

public class Topic {
	public String title = "";
	public int topicId = 0;
	public String author = "";
	public String lastPost = "";
	public String follow = "";
	public boolean read = false;
	public boolean locked = false;
	public boolean sticky = false;
	public String postTime = "";
	public List<String> pageIndex = null;
	public List<Integer> pageList = null;
}