package org.hdstar.remote;

import java.io.InputStream;
import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.hdstar.util.HttpClientManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

/**
 * 
 * ‘∂≥ÃTransmission  ≈‰∆˜. <br/>
 * ≤Œøºtransdroid
 * 
 * @see http://www.transdroid.org/under-the-hood/
 * 
 * @author robust
 */
public class Transmission extends RemoteBase {

	private static final int FOR_ALL = -1;

	private static final String RPC_ID = "id";
	private static final String RPC_NAME = "name";
	private static final String RPC_STATUS = "status";
	private static final String RPC_ERROR = "error";
	private static final String RPC_ERRORSTRING = "errorString";
	private static final String RPC_DOWNLOADDIR = "downloadDir";
	private static final String RPC_RATEDOWNLOAD = "rateDownload";
	private static final String RPC_RATEUPLOAD = "rateUpload";
	private static final String RPC_PEERSGETTING = "peersGettingFromUs";
	private static final String RPC_PEERSSENDING = "peersSendingToUs";
	private static final String RPC_PEERSCONNECTED = "peersConnected";
	// private static final String RPC_PEERSKNOWN = "peersKnown";
	private static final String RPC_ETA = "eta";
	private static final String RPC_DOWNLOADSIZE1 = "haveUnchecked";
	private static final String RPC_DOWNLOADSIZE2 = "haveValid";
	private static final String RPC_UPLOADEDEVER = "uploadedEver";
	private static final String RPC_TOTALSIZE = "sizeWhenDone";
	private static final String RPC_DATEADDED = "addedDate";
	private static final String RPC_DATEDONE = "doneDate";
	private static final String RPC_AVAILABLE = "desiredAvailable";
	private static final String RPC_COMMENT = "comment";

	private static final String RPC_FILE_NAME = "name";
	private static final String RPC_FILE_LENGTH = "length";
	private static final String RPC_FILE_COMPLETED = "bytesCompleted";
	private static final String RPC_FILESTAT_WANTED = "wanted";
	private static final String RPC_FILESTAT_PRIORITY = "priority";

	public Transmission() {
		super(RemoteType.Transmission);
	}

	@Override
	public BaseAsyncTask<Boolean> login(String username, String password) {
		String ip;
		int port;
		String[] sa = ipNPort.split(":");
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
				new UsernamePasswordCredentials(username, password));
		HttpGet request = new HttpGet(String.format(
				CommonUrls.BTClient.TRANSMISSION_HOME_PAGE, ipNPort));
		ResponseParser<Boolean> parser = new ResponseParser<Boolean>(
				R.string.login_error) {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 301) {
					msgId = SUCCESS_MSG_ID;
					return true;
				}

				return false;
			}
		};
		return new BaseAsyncTask<Boolean>(request, parser);
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> start(String... hashes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> pause(String... hashes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> stop(String... hashes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> remove(boolean rmFile, String... hashes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls) {
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> addByUrl(String dir, String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean diskEnable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseAsyncTask<long[]> getDiskInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> setLabel(String label, String... hashes) {
		// TODO Auto-generated method stub
		return null;
	}

}
