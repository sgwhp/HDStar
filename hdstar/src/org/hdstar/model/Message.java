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
	/** �Ƿ��Ѷ� */
	public boolean read = true;
	/** ���� */
	public String subject = "";
	/** ������ */
	public String sender = "";
	/** �ظ��� */
	public String replyer = "";
	/** ʱ�� */
	public String time = "";
	/** id */
	public int id;

}
