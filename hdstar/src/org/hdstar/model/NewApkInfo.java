package org.hdstar.model;

import java.io.Serializable;

public class NewApkInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int appCode;
	public String packageName;
	public int versionCode;
	public String versionName;
	public long size;
	public long patchSize;
	public String desc;
	public String[] pics;
	public String updateDate;
}
