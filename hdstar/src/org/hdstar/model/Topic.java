package org.hdstar.model;

import java.util.List;

public class Topic {
	public String title = "";
	public int topicId = 0;
	public String author = "";
	public String lastPost = "";
	/** ���ظ�/�鿴����Ϣ */
	public String follow = "";
	public boolean read = false;
	public boolean locked = false;
	public boolean sticky = false;
	public String postTime = "";
	/** ҳ�� */
	public List<String> pageIndex = null;
	/** ҳ�루url��ҳ����������0��ʼ�� */
	public List<Integer> pageList = null;
}