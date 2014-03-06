package org.hdstar.ptadapter;

import java.util.ArrayList;

import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;

import android.graphics.Bitmap;

public abstract class PTAdapter {
	protected PTSiteType mType;
	protected String cookie;
	protected String torrentsUrl;// 种子页面地址

	public PTAdapter(PTSiteType type) {
		mType = type;
		// 默认使用第一个
		torrentsUrl = mType.getTorrentPages()[0].url;
	}

	public PTSiteType getType() {
		return mType;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public void setTorrentsUrl(String url) {
		torrentsUrl = url;
	}

	/**
	 * 
	 * 登录是否需要验证码. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public boolean needSecurityCode() {
		return false;
	}

	/**
	 * 
	 * 构建获取验证码图片任务. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public BaseAsyncTask<Bitmap> getSecurityImage() {
		return null;
	}

	/**
	 * 
	 * 构建登录任务. <br/>
	 * 
	 * @author robust
	 * @param username
	 *            账号
	 * @param password
	 *            密码
	 * @param securityCode
	 *            验证码（可选）
	 * @return
	 */
	public abstract BaseAsyncTask<String> login(String username,
			String password, String securityCode);

	/**
	 * 
	 * 退出. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> logout();

	/**
	 * 
	 * 构建获取种子列表任务. <br/>
	 * 
	 * @author robust
	 * @param page
	 *            页码
	 * @param keywords
	 *            关键字
	 * @return
	 */
	public abstract BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords);

	/**
	 * 
	 * 收藏或取消收藏. <br/>
	 * 
	 * @author robust
	 * @param torrentId
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> bookmark(String torrentId);

	/**
	 * 
	 * 是否支持rss下载（下载框）. <br/>
	 * 
	 * @author robust
	 * @return
	 */
	public boolean rssEnable() {
		return false;
	}

	/**
	 * 
	 * 构建添加到rss下载任务. <br/>
	 * 
	 * @author robust
	 * @param torrentId
	 *            种子id
	 * @return
	 */
	public BaseAsyncTask<Boolean> addToRss(String torrentId) {
		return null;
	}

}
