package org.hdstar.remote;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.RemoteType;
import org.hdstar.model.Label;
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
	protected RemoteType mType;
	// ����ip�Ͷ˿�
	protected String ipNPort;
	/** �����б�����б�ǩ */
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

	/**
	 * 
	 * ������¼����. <br/>
	 * 
	 * @param username
	 *            �û���
	 * @param password
	 *            ����
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> login(String username,
			String password);

	/**
	 * 
	 * ������ȡ�����б�����. <br/>
	 * 
	 * @return
	 */
	public abstract BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList();

	/**
	 * 
	 * ������ʼ��������. <br/>
	 * 
	 * @param hashes
	 *            ��������hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> start(String... hashes);

	/**
	 * 
	 * ������ͣ��������. <br/>
	 * 
	 * @param hashes
	 *            ��������hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> pause(String... hashes);

	/**
	 * 
	 * ����ֹͣ��������. <br/>
	 * 
	 * @param hashes
	 *            ��������hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> stop(String... hashes);

	/**
	 * 
	 * ����ɾ����������. <br/>
	 * 
	 * @param rmFile
	 *            �Ƿ��Ƴ��ļ�
	 * @param hashes
	 *            ��������hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> remove(boolean rmFile,
			String... hashes);

	/**
	 * 
	 * ���������������. <br/>
	 * 
	 * @param dir
	 *            ����Ŀ¼
	 * @param hashes
	 *            ��������hash
	 * @param urls
	 *            �������ӵ�ַ
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls);

	/**
	 * 
	 * ������url��ӵ�����������. <br/>
	 * 
	 * @param dir
	 *            ����Ŀ¼
	 * @param url
	 *            ���ӵ�ַ
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> addByUrl(String dir, String url);

	public abstract boolean diskEnable();

	/**
	 * ������ȡ������Ϣ����
	 * 
	 * @return ���鳤��Ϊ2���ֱ����ܿռ�Ϳ��ÿռ�
	 */
	public abstract BaseAsyncTask<long[]> getDiskInfo();

	/**
	 * 
	 * �������ñ�ǩ��������. <br/>
	 * 
	 * @param label
	 *            ��ǩ
	 * @param hashes
	 *            ��������hash
	 * @return
	 */
	public abstract BaseAsyncTask<Boolean> setLabel(String label,
			String... hashes);

}
