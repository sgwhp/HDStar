package org.hdstar.model;

import java.io.Serializable;
import java.util.HashSet;

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

	public HashSet<String> getPicsSet() {
		HashSet<String> set = new HashSet<String>();
		if (pics != null) {
			for (String pic : pics) {
				set.add(pic);
			}
		}
		return set;
	}
}
