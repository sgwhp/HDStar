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
	// 头像链接地址
	public String avatarSrc = "";
	// 等级图片链接地址
	public String userClassSrc = "";
	// 上传量、下载量等信息
	public String info = "";
	public String body = "";
	public boolean delete;
	public boolean edit;
	// 签名
	public String signature = "";
}