package org.hdstar.model;

import java.io.Serializable;

public class ResponseWrapper <T> implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int resCode;
	public String msg;
	public T body;
}
