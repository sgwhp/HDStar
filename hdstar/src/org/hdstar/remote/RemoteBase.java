package org.hdstar.remote;

import java.util.ArrayList;

import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;

/**
 * ��������Ҫ��RemoteType�е�һ��
 * 
 * @see org.hdstar.common.RemoteType
 * @author robust
 * 
 */
public abstract class RemoteBase {
	protected RemoteType type;
	// ����ip�Ͷ˿�
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

	public abstract BaseAsyncTask<Boolean> login(String username,
			String password);

	public abstract BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList();

	public abstract BaseAsyncTask<Boolean> start(String... hashes);

	public abstract BaseAsyncTask<Boolean> pause(String... hashes);

	public abstract BaseAsyncTask<Boolean> stop(String... hashes);

	public abstract BaseAsyncTask<Boolean> remove(boolean rmFile,
			String... hashes);

	public abstract BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls);

	public abstract BaseAsyncTask<Boolean> addByUrl(String dir, String url);

	public abstract boolean diskEnable();

	/**
	 * 
	 * @return ���鳤��Ϊ2���ֱ����ܿռ�Ϳ��ÿռ�
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

}
