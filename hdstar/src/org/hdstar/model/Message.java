package org.hdstar.model;

import java.io.Serializable;

/**
 * pm. <br/>
 * 
 * @author robust
 */
public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 是否已读 */
	public boolean read = true;
	/** 主题 */
	public String subject = "";
	/** 发送人 */
	public String sender = "";
	/** 回复人 */
	public String replyer = "";
	/** 时间 */
	public String time = "";
	/** id */
	public int id;

}
