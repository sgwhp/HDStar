package org.hdstar.model;

import java.util.List;

public class Topic {
	public String title = "";
	public int topicId = 0;
	public String author = "";
	public String lastPost = "";
	public String follow = "";// ���ظ�/�鿴����Ϣ
	public boolean read = false;
	public boolean locked = false;
	public boolean sticky = false;
	public String postTime = "";
	public List<String> pageIndex = null;// ҳ��
	public List<Integer> pageList = null;// ҳ�루url��ҳ����������0��ʼ��
}