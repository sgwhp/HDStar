package org.hdstar.remote;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.RemoteType;
import org.hdstar.model.Label;
import org.hdstar.model.RemoteSetting;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.util.HttpClientManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

/**
 * 子类命名要与RemoteType中的一致
 * 
 * @see org.hdstar.common.RemoteType
 * @author robust
 * 
 */
public abstract class RemoteBase {
	protected RemoteType mType;
	/** 设置信息，包括ip、端口、用户名、密码等 */
	protected RemoteSetting setting;
	/** 任务列表的所有标签 */
	protected ArrayList<Label> mLabels = new ArrayList<Label>();

	public RemoteBase(RemoteType type) {
		this.mType = type;
	}

	public RemoteType getRemoteType() {
		return mType;
	}

	public RemoteSetting getSetting() {
		return setting;
	}

	public void setSetting(RemoteSetting setting) {
		this.setting = setting;
		// 设置auth验证参数
		String ip;
		int port;
		String[] sa = setting.ip.split(":");
		ip = sa[0];
		if (sa.length == 2) {
			port = Integer.parseInt(sa[1]);
		} else {
			port = 80;
		}
		HttpHost targetHost = new HttpHost(ip, port, "http");
		DefaultHttpClient client = (DefaultHttpClient) HttpClientManager
				.getHttpClient();
		client.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(setting.username,
						setting.password));
	}

	public List<Label> getLabels() {
		return mLabels;
	}

	// /**
	// *
	// * 构建登录请求. <br/>
	// *
	// * @param username
	// * 用户名
	// * @param password
	// * 密码
	// * @return
	// */
	// public abstract BaseAsyncTask<Boolean> login(String username,
	// String password);

	/**
	 * 
	 * 构建获取任务列表请求. <br/>
	 * 
	 * @return
	 */
	public abstract BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList();

	/**
	 * 
	 * 构建开始任务请求. <br/>
	 * 
	 * @param hashes
	 *            所有任务hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> start(String... hashes);

	/**
	 * 
	 * 构建暂停任务请求. <br/>
	 * 
	 * @param hashes
	 *            所有任务hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> pause(String... hashes);

	/**
	 * 
	 * 构建停止任务请求. <br/>
	 * 
	 * @param hashes
	 *            所有任务hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> stop(String... hashes);

	/**
	 * 
	 * 构建删除任务请求. <br/>
	 * 
	 * @param rmFile
	 *            是否移除文件
	 * @param hashes
	 *            所有任务hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> remove(boolean rmFile,
			String... hashes);

	/**
	 * 
	 * 构建添加任务请求. <br/>
	 * 
	 * @param dir
	 *            下载目录
	 * @param hashes
	 *            所有任务hash
	 * @param urls
	 *            新增种子地址
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls);

	/**
	 * 
	 * 构建由url添加单个任务请求. <br/>
	 * 
	 * @param dir
	 *            下载目录
	 * @param url
	 *            种子地址
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> addByUrl(String dir, String url);

	public abstract boolean diskEnable();

	/**
	 * 构建获取磁盘信息请求
	 * 
	 * @return 数组长度为2，分别是总空间和可用空间
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

	/**
	 * 
	 * 构建设置标签任务请求. <br/>
	 * 
	 * @param label
	 *            标签
	 * @param hashes
	 *            所有任务hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> setLabel(String label,
			String... hashes);

}
