package org.hdstar.model;

import java.io.Serializable;

public class Post implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String userName = "";
	public int uid;
	// ͷ�����ӵ�ַ
	public String avatarSrc = "";
	// �ȼ�ͼƬ���ӵ�ַ
	public String userClassSrc = "";
	// �ϴ���������������Ϣ
	public String info = "";
	public String body = "";
	public boolean delete;
	public boolean edit;
	// ǩ��
	public String signature = "";
}