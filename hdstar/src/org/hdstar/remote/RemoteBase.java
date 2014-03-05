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
 * ��������Ҫ��RemoteType�е�һ��
 * 
 * @see org.hdstar.common.RemoteType
 * @author robust
 * 
 */
public abstract class RemoteBase {
	protected RemoteType mType;
	/** ������Ϣ������ip���˿ڡ��û���������� */
	protected RemoteSetting setting;
	/** �����б�����б�ǩ */
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
		// ����auth��֤����
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
	// * ������¼����. <br/>
	// *
	// * @param username
	// * �û���
	// * @param password
	// * ����
	// * @return
	// */
	// public abstract BaseAsyncTask<Boolean> login(String username,
	// String password);

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
