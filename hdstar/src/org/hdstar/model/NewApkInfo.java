package org.hdstar.model;

import java.io.Serializable;

/**
 * 
 * App������ʱ�����°汾��apk��Ϣ. <br/>
 * 
 * @author robust
 */
public class NewApkInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Ӧ�õ�id����������Ҫ��¼����Ӧ��idΪ1 */
	public int appCode;
	/** ���� */
	public String packageName;
	/** �汾�� */
	public int versionCode;
	/** �汾���� */
	public String versionName;
	/** �°�apk��С */
	public long size;
	/** ��������С */
	public long patchSize;
	/** ���� */
	public String desc;
	/** ��ͼ��ַ */
	public String[] pics;
	/** ����ʱ�� */
	public String updateDate;
}
