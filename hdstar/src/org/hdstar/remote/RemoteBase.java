package org.hdstar.remote;

import java.util.ArrayList;

import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;

import android.app.Activity;

public abstract class RemoteBase {
	protected String ip;

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public abstract String getTitle();

	public abstract BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			Activity context);

	public abstract BaseAsyncTask<Boolean> start(String... hashes);

	public abstract BaseAsyncTask<Boolean> pause(String... hashes);

	public abstract BaseAsyncTask<Boolean> stop(String... hashes);

	public abstract BaseAsyncTask<Boolean> delete(String... hashes);

	public abstract BaseAsyncTask<Boolean> download(String dir, String hash,
			ArrayList<String> urls);

	public abstract boolean rssEnable();

	public abstract BaseAsyncTask<ArrayList<RssLabel>> fetchRssList();

	public abstract BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(
			String hash);

	public abstract boolean diskEnable();

	/**
	 * 
	 * @param callback
	 * @param ip
	 * @return ���鳤��Ϊ2���ֱ����ܿռ�Ϳ��ÿռ�
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

}
