package org.hdstar.ptadapter;

import java.util.ArrayList;

import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;

import android.graphics.Bitmap;

public abstract class PTAdapter {
	protected PTSiteType mType;

	public PTAdapter(PTSiteType type) {
		mType = type;
	}

	public PTSiteType getType() {
		return mType;
	}

	public void setType(PTSiteType type) {
		this.mType = type;
	}

	/**
	 * 
	 * ��¼�Ƿ���Ҫ��֤��. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public boolean needSecurityCode() {
		return false;
	}

	/**
	 * 
	 * ������ȡ��֤��ͼƬ����. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public BaseAsyncTask<Bitmap> getSecurityImage() {
		return null;
	}

	/**
	 * 
	 * ������¼����. <br/>
	 * 
	 * @author robust
	 * @param username
	 *            �˺�
	 * @param password
	 *            ����
	 * @param securityCode
	 *            ��֤�루��ѡ��
	 * @return
	 */
	public abstract BaseAsyncTask<String> login(String username,
			String password, String securityCode);

	/**
	 * 
	 * �˳�. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> logout();

	/**
	 * 
	 * ������ȡ�����б�����. <br/>
	 * 
	 * @author robust
	 * @param page
	 *            ҳ��
	 * @param keywords
	 *            �ؼ���
	 * @return
	 */
	public abstract BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords);

	/**
	 * 
	 * �Ƿ�֧��rss���أ����ؿ�. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public boolean rssEnable() {
		return false;
	}

	/**
	 * 
	 * ������ӵ�rss��������. <br/>
	 * 
	 * @author robust
	 * @param torrentId
	 *            ����id
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> addToRss(String torrentId);

}
