package org.hdstar.model;

import java.io.Serializable;

public class RemoteTaskInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String hash;
	public String title;
	public long size;
	public float ratio;
	public long upSpeed;
	public long dlSpeed;
	public long completeSize;
	public int state;

}