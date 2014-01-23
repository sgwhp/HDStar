package org.hdstar.remote;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.RemoteType;
import org.hdstar.model.Label;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;

/**
 * 子类命名要与RemoteType中的一致
 * 
 * @see org.hdstar.common.RemoteType
 * @author robust
 * 
 */
public abstract class RemoteBase {
	protected RemoteType mType;
	// 包括ip和端口
	protected String ipNPort;
	protected ArrayList<Label> mLabels = new ArrayList<Label>();

	public RemoteBase(RemoteType type) {
		this.mType = type;
	}

	public RemoteType getRemoteType() {
		return mType;
	}

	public void setIpNPort(String ipNPort) {
		this.ipNPort = ipNPort;
	}

	public String getIpNPort() {
		return ipNPort;
	}

	public List<Label> getLabels() {
		return mLabels;
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
	 * @return 数组长度为2，分别是总空间和可用空间
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

}
