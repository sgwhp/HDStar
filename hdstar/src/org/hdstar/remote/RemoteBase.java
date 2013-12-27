package org.hdstar.remote;

import java.util.ArrayList;

import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;

import android.app.Activity;

/**
 * 子类命名要与RemoteType中的一致
 * 
 * @see org.hdstar.common.RemoteType
 * @author robust
 * 
 */
public abstract class RemoteBase {
	protected RemoteType type;
	// 包括ip和端口
	protected String ipNPort;

	public RemoteBase(RemoteType type) {
		this.type = type;
	}

	public RemoteType getRemoteType() {
		return type;
	}

	public void setIpNPort(String ipNPort) {
		this.ipNPort = ipNPort;
	}

	public String getIpNPort() {
		return ipNPort;
	}

	public String getTitle() {
		return "";
	}

	public abstract BaseAsyncTask<Boolean> login(String username,
			String password);

	public abstract BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			Activity context);

	public abstract BaseAsyncTask<Boolean> start(String... hashes);

	public abstract BaseAsyncTask<Boolean> pause(String... hashes);

	public abstract BaseAsyncTask<Boolean> stop(String... hashes);

	public abstract BaseAsyncTask<Boolean> remove(boolean rmFile,
			String... hashes);

	public abstract BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls);

	public abstract boolean rssEnable();

	public abstract BaseAsyncTask<ArrayList<RssLabel>> fetchRssList();

	public abstract BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(
			String hash);

	public abstract boolean diskEnable();

	/**
	 * 
	 * @return 数组长度为2，分别是总空间和可用空间
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

}
