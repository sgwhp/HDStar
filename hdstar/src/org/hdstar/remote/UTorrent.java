package org.hdstar.remote;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.TorrentStatus;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.DefaultGetParser;
import org.hdstar.task.ResponseParser;
import org.hdstar.task.UtorrentTask;
import org.hdstar.util.HttpClientManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class UTorrent extends RemoteBase {

	public UTorrent() {
		super(RemoteType.UTorrent);
	}

	private TorrentStatus convertUtorrentStatus(int uStatus, boolean finished) {
		// Convert bitwise int to uTorrent status codes
		// Now based on http://forum.utorrent.com/viewtopic.php?id=50779
		if ((uStatus & 1) == 1) {
			// Started
			if ((uStatus & 32) == 32) {
				// Paused
				return TorrentStatus.Paused;
			} else if (finished) {
				return TorrentStatus.Seeding;
			} else {
				return TorrentStatus.Downloading;
			}
		} else if ((uStatus & 2) == 2) {
			// Checking
			return TorrentStatus.Checking;
		} else if ((uStatus & 16) == 16) {
			// Error
			return TorrentStatus.Error;
		} else if ((uStatus & 128) == 128) {
			// Queued
			return TorrentStatus.Queued;
		} else {
			return TorrentStatus.Waiting;
		}
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList() {
		ResponseParser<ArrayList<RemoteTaskInfo>> parser = new ResponseParser<ArrayList<RemoteTaskInfo>>() {

			@Override
			public ArrayList<RemoteTaskInfo> parse(HttpResponse res,
					InputStream in) {
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(new InputStreamReader(in));
				JsonArray arr = element.getAsJsonObject().getAsJsonArray(
						"torrents");
				ArrayList<RemoteTaskInfo> result = new ArrayList<RemoteTaskInfo>();
				JsonArray torrent;
				RemoteTaskInfo info;
				for (int i = 0; i < arr.size(); i++) {
					torrent = arr.get(i).getAsJsonArray();
					info = new RemoteTaskInfo();
					info.hash = torrent.get(0).getAsString();
					info.title = torrent.get(2).getAsString();
					info.size = torrent.get(3).getAsLong();
					info.completeSize = torrent.get(4).getAsLong();
					info.status = convertUtorrentStatus(torrent.get(1)
							.getAsInt(), info.completeSize == 1000);
					info.uploaded = torrent.get(5).getAsLong();
					info.ratio = torrent.get(6).getAsFloat() / 1000;
					info.upSpeed = torrent.get(7).getAsLong();
					info.dlSpeed = torrent.get(8).getAsLong();
					// 9 eta
					info.label = torrent.get(10).getAsString();
					result.add(info);
				}
				msgId = SUCCESS_MSG_ID;
				return result;
			}
		};

		UtorrentTask<ArrayList<RemoteTaskInfo>> task = UtorrentTask
				.newInstance(ipNPort, String.format(
						Const.Urls.UTORRENT_GET_LIST_URL, ipNPort, "%s"),
						parser);
		return task;
	}

	private String buildParams(String url, String ip, String mode,
			String... hashes) {
		String params = String.format(url, ip, "%s", mode);
		for (String hash : hashes) {
			params += "&hash=" + hash;
		}
		return params;
	}

	private BaseAsyncTask<Boolean> ctrlTask(String mode, String... hashes) {
		ResponseParser<Boolean> parser = new DefaultGetParser();
		UtorrentTask<Boolean> task = UtorrentTask.newInstance(ipNPort,
				buildParams(Const.Urls.UTORRENT_ACTION_URL, ipNPort, mode),
				parser);
		return task;
	}

	@Override
	public BaseAsyncTask<Boolean> start(String... hashes) {
		return ctrlTask("start", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> pause(String... hashes) {
		return ctrlTask("pause", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> stop(String... hashes) {
		return ctrlTask("stop", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> remove(boolean rmFile, String... hashes) {
		if (rmFile) {
			return ctrlTask("removedata", hashes);
		}
		return ctrlTask("remove", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls) {
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
				Const.Urls.UTORRENT_HOME_PAGE, ipNPort));
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
	public BaseAsyncTask<Boolean> addByUrl(String dir, String url) {
		ResponseParser<Boolean> parser = new DefaultGetParser();
		UtorrentTask<Boolean> task = null;
		try {
			task = UtorrentTask.newInstance(ipNPort, String.format(
					Const.Urls.UTORRENT_ACTION_URL, ipNPort, "%s",
					URLEncoder.encode(url, "UTF-8")), parser);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return task;
	}

}
