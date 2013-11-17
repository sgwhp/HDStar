package org.hdstar.model;

import java.io.Serializable;

public class RemoteTaskInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String title;
	public long size;
	public float ratio;
	public float upSpeed;
	public float dlSpeed;
	public int progress;

}
