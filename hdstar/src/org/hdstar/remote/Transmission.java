package org.hdstar.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.TorrentStatus;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.parser.BasicAuthGetParser;
import org.hdstar.task.parser.BasicAuthParser;
import org.hdstar.task.parser.ResponseParser;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

/**
 * 
 * 远程Transmission适配器，rpc暂无标签功能. <br/>
 * 
 * 部分功能参考transdroid
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

	private static String sessionId;
	private int rpcVersion = -1;

	public Transmission() {
		super(RemoteType.Transmission);
	}

	// @Override
	// public BaseAsyncTask<Boolean> login(String username, String password) {
	// String ip;
	// int port;
	// String[] sa = setting.ip.split(":");
	// ip = sa[0];
	// if (sa.length == 2) {
	// port = Integer.parseInt(sa[1]);
	// } else {
	// port = 80;
	// }
	// HttpHost targetHost = new HttpHost(ip, port, "http");
	// DefaultHttpClient client = (DefaultHttpClient) HttpClientManager
	// .getHttpClient();
	// client.getCredentialsProvider().setCredentials(
	// new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	// new UsernamePasswordCredentials(username, password));
	// HttpGet request = new HttpGet(String.format(
	// CommonUrls.BTClient.TRANSMISSION_HOME_PAGE, setting.ip));
	// ResponseParser<Boolean> parser = new ResponseParser<Boolean>(
	// R.string.login_error) {
	//
	// @Override
	// public Boolean parse(HttpResponse res, InputStream in) {
	// if (res.getStatusLine().getStatusCode() == 301) {
	// msgId = SUCCESS_MSG_ID;
	// return true;
	// }
	//
	// return false;
	// }
	// };
	// return new BaseAsyncTask<Boolean>(request, parser);
	// }

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList() {
		try {
			JSONObject request = new JSONObject();
			// Request all torrents from server
			JSONArray fields = new JSONArray();
			final String[] fieldsArray = new String[] { RPC_ID, RPC_NAME,
					RPC_ERROR, RPC_ERRORSTRING, RPC_STATUS, RPC_DOWNLOADDIR,
					RPC_RATEDOWNLOAD, RPC_RATEUPLOAD, RPC_PEERSGETTING,
					RPC_PEERSSENDING, RPC_PEERSCONNECTED, RPC_ETA,
					RPC_DOWNLOADSIZE1, RPC_DOWNLOADSIZE2, RPC_UPLOADEDEVER,
					RPC_TOTALSIZE, RPC_DATEADDED, RPC_DATEDONE, RPC_AVAILABLE,
					RPC_COMMENT };
			for (String field : fieldsArray) {
				fields.put(field);
			}
			request.put("fields", fields);
			BasicAuthParser<ArrayList<RemoteTaskInfo>> parser = new BasicAuthParser<ArrayList<RemoteTaskInfo>>() {

				@Override
				public ArrayList<RemoteTaskInfo> parseContent(HttpResponse res,
						InputStream in) {
					try {
						JSONObject jObj = resultToJSON(in).getJSONObject(
								"arguments");
						// Parse response
						ArrayList<RemoteTaskInfo> result = new ArrayList<RemoteTaskInfo>();
						RemoteTaskInfo info;
						JSONArray rarray = jObj.getJSONArray("torrents");
						for (int i = 0; i < rarray.length(); i++) {
							// Add the parsed torrent to the list
							JSONObject tor = rarray.getJSONObject(i);
							// Error is a number, see
							// https://trac.transmissionbt.com/browser/trunk/libtransmission/transmission.h#L1747
							// We only consider it a real error if it is local
							// (blocking), which is error code 3
							boolean hasError = tor.getInt(RPC_ERROR) == 3;
							info = new RemoteTaskInfo();
							info.hash = tor.getInt(RPC_ID) + "";
							info.title = tor.getString(RPC_NAME);
							info.size = tor.getLong(RPC_TOTALSIZE);
							info.uploaded = tor.getLong(RPC_UPLOADEDEVER);
							info.downloaded = tor.getLong(RPC_DOWNLOADSIZE1);
							info.ratio = info.uploaded * 100 / info.downloaded;
							// TODO upspeed and etc
							info.status = hasError ? TorrentStatus.Error
									: getStatus(tor.getInt(RPC_STATUS));
							result.add(info);
						}

						// Return the list
						return result;
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			TransmissionTask<ArrayList<RemoteTaskInfo>> task = new TransmissionTask<ArrayList<RemoteTaskInfo>>(
					String.format(CommonUrls.BTClient.TRANSMISSION_RPC_URL,
							setting.ip), parser, buildRequestObject(
							"torrent-get", request).toString());
			return task;
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		try {
			TransmissionTask<Boolean> task = new TransmissionTask<Boolean>(
					String.format(CommonUrls.BTClient.TRANSMISSION_RPC_URL,
							setting.ip), new BasicAuthGetParser(),
					buildRequestObject(
							"torrent-remove",
							buildTorrentRequestObject(hashes[0],
									"delete-local-data", rmFile)).toString());
			return task;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls) {
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> addByUrl(String dir, String url) {
		JSONObject request = new JSONObject();
		try {
			request.put("filename", url);

			TransmissionTask<Boolean> task = new TransmissionTask<Boolean>(
					String.format(CommonUrls.BTClient.TRANSMISSION_RPC_URL,
							setting.ip), new BasicAuthGetParser(),
					buildRequestObject("torrent-add", request).toString());
			return task;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean diskEnable() {
		return false;
	}

	@Override
	public BaseAsyncTask<long[]> getDiskInfo() {
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> setLabel(String label, String... hashes) {
		return null;
	}

	private JSONObject buildTorrentRequestObject(String torrentID,
			String extraKey, boolean extraValue) throws JSONException {
		return buildTorrentRequestObject(Long.parseLong(torrentID), extraKey,
				extraValue);
	}

	private JSONObject buildTorrentRequestObject(long torrentID,
			String extraKey, boolean extraValue) throws JSONException {

		// Build request for one specific torrent
		JSONObject request = new JSONObject();
		if (torrentID != FOR_ALL) {
			JSONArray ids = new JSONArray();
			ids.put(torrentID); // The only id to add
			request.put("ids", ids);
		}
		if (extraKey != null) {
			request.put(extraKey, extraValue);
		}
		return request;

	}

	private JSONObject buildRequestObject(String sendMethod,
			JSONObject arguments) throws JSONException {

		// Build request for method
		JSONObject request = new JSONObject();
		request.put("method", sendMethod);
		request.put("arguments", arguments);
		request.put("tag", 0);
		return request;
	}

	private JSONObject resultToJSON(InputStream in) throws JSONException,
			IOException {
		return new JSONObject(IOUtils.inputStream2String(in));
	}

	private TorrentStatus getStatus(int status) {
		if (rpcVersion <= -1) {
			return TorrentStatus.Unknown;
		} else if (rpcVersion >= 14) {
			switch (status) {
			case 0:
				return TorrentStatus.Paused;
			case 1:
				return TorrentStatus.Waiting;
			case 2:
				return TorrentStatus.Checking;
			case 3:
				return TorrentStatus.Queued;
			case 4:
				return TorrentStatus.Downloading;
			case 5:
				return TorrentStatus.Queued;
			case 6:
				return TorrentStatus.Seeding;
			}
			return TorrentStatus.Unknown;
		} else {
			return TorrentStatus.getStatus(status);
		}
	}

	/**
	 * Transmission请求任务，获取rpc版本号、处理409（session过期） <br/>
	 * 
	 * @author robust
	 */
	private class TransmissionTask<T> extends BaseAsyncTask<T> {
		private String url;
		private final String SESSION_HEADER = "X-Transmission-Session-Id";
		private String param;// json格式的请求数据

		public TransmissionTask(String url, ResponseParser<T> parser,
				String param) {
			this.url = url;
			this.parser = parser;
			this.param = param;
		}

		@Override
		protected T doInBackground(String... params) {
			try {
				if (rpcVersion < 0) {
					// 先获取transmission的版本
					request = new HttpPost(String.format(
							CommonUrls.BTClient.TRANSMISSION_RPC_URL,
							setting.ip));
					((HttpPost) request).setEntity(new StringEntity(
							buildRequestObject("session-get", new JSONObject())
									.toString(), "UTF-8"));
					makeRequest(request, true);
				}
				request = new HttpPost(url);
				((HttpPost) request)
						.setEntity(new StringEntity(param, "UTF-8"));
				return makeRequest(request, false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
			return null;
		}

		private T makeRequest(HttpRequestBase req, boolean getRpcVersion) {
			DefaultHttpClient client = (DefaultHttpClient) HttpClientManager
					.getHttpClient();
			InputStream in = null;
			try {
				if (sessionId != null) {
					req.addHeader(SESSION_HEADER, sessionId);
				}
				req.setHeader("Cookie", cookie);
				HttpResponse response = client.execute(request);
				// 401在parser中集中处理，故此处注释掉
				// if (response.getStatusLine().getStatusCode() == 401) {
				// parser.setMessageId(R.string.http_401);
				// return null;
				// }
				if (response.getStatusLine().getStatusCode() == 409) {
					// session过期
					req.abort();
					sessionId = response.getFirstHeader(SESSION_HEADER)
							.getValue();
					req.addHeader(SESSION_HEADER, sessionId);
					response = client.execute(req);
				}
				if (getRpcVersion) {
					// 仅获取rpc版本号，无需返回结果，一般在获取transmission时会用到
					in = response.getEntity().getContent();
					rpcVersion = resultToJSON(in).getJSONObject("arguments")
							.getInt("rpc-version");
					return null;
				}
				if (needContent) {
					in = response.getEntity().getContent();
				}
				if (parser != null) {
					return parser.parse(response, in);
				}
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				setMessageId(R.string.time_out);
				HttpClientManager.getHttpClient().getConnectionManager()
						.closeExpiredConnections();
			} catch (ConnectTimeoutException e) {
				setMessageId(R.string.time_out);
				HttpClientManager.getHttpClient().getConnectionManager()
						.closeExpiredConnections();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				setMessageId(R.string.connection_refused);
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
				if ("Connection reset by peer".equals(e.getMessage())) {
					HttpClientManager.restClient();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeInputStreamIgnoreExceptions(in);
				req.releaseConnection();
				req.abort();
			}
			return null;
		}
	}

}
