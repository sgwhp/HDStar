package org.hdstar.model;

import java.io.Serializable;

/**
 * pm��������. <br/>
 * 
 * @author robust
 */
public class MessageContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** ������id */
	public int senderId;
	/** ������id */
	public int receiverId;
	/** pm���� */
	public String content;

}
