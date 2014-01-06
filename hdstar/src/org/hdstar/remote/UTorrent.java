package org.hdstar.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.hdstar.task.UtorrentTask;
import org.hdstar.util.HttpClientManager;
import org.hdstar.util.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.app.Activity;
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;

public class UTorrent extends RemoteBase {

	public UTorrent() {
		super(RemoteType.UTorrent);
	}

	@Override
	public String getTitle() {
		return "¦ÌTorrent";
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			final Activity context) {
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
					info.state = torrent.get(1).getAsInt();
					info.title = torrent.get(2).getAsString();
					info.size = torrent.get(3).getAsLong();
					info.completeSize = torrent.get(4).getAsLong();
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
		String params = String.format(url, ip, mode);
		for (String hash : hashes) {
			params += "&hash=" + hash;
		}
		return params;
	}

	private BaseAsyncTask<Boolean> ctrlTask(String mode, String... hashes) {
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.execGet(
				buildParams(Const.Urls.RUTORRENT_RPC_ACTION_URL, ipNPort, mode,
						hashes), new ResponseParser<Boolean>() {

					@Override
					public Boolean parse(HttpResponse res, InputStream in) {
						if (res.getStatusLine().getStatusCode() == 200) {
							msgId = SUCCESS_MSG_ID;
							return true;
						}
						return false;
					}
				});
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
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> add(String dir, String hash,
			ArrayList<String> urls) {
		return null;
	}

	@Override
	public boolean rssEnable() {
		return false;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> fetchRssList() {
		return null;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(String hash) {
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
		// TODO Auto-generated method stub
		return null;
	}

}
