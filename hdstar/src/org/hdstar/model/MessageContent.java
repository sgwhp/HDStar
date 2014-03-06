package org.hdstar.model;

import java.io.Serializable;

/**
 * pm具体内容. <br/>
 * 
 * @author robust
 */
public class MessageContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 发送人id */
	public int senderId;
	/** 接收人id */
	public int receiverId;
	/** pm内容 */
	public String content;

}
