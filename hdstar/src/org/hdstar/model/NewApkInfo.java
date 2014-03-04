package org.hdstar.model;

import java.io.Serializable;

/**
 * 
 * App可升级时，最新版本的apk信息. <br/>
 * 
 * @author robust
 */
public class NewApkInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 应用的id，服务器需要记录，本应用id为1 */
	public int appCode;
	/** 包名 */
	public String packageName;
	/** 版本号 */
	public int versionCode;
	/** 版本名称 */
	public String versionName;
	/** 新版apk大小 */
	public long size;
	/** 增量包大小 */
	public long patchSize;
	/** 描述 */
	public String desc;
	/** 截图地址 */
	public String[] pics;
	/** 更新时间 */
	public String updateDate;
}
