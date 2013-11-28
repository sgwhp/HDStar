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
	public String avatarSrc = "";
	public String userClassSrc = "";
	public String info = "";
	public String body = "";
	public boolean delete;
	public boolean edit;
	public String signature = "";
}