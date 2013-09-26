package org.hdstar.model;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean read = true;
	public String subject="";
	public String sender="";
	public String replyer="";
	public String time="";
	public int id;

}
