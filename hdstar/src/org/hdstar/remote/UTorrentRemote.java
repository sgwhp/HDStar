package org.hdstar.remote;

import java.io.InputStream;
import java.util.ArrayList;

import org.hdstar.common.Const;
import org.hdstar.common.RemoteType;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.hdstar.task.UtorrentTask;

import android.app.Activity;
import ch.boye.httpclientandroidlib.HttpResponse;

public class UTorrentRemote extends RemoteBase {

	public UTorrentRemote() {
		super(RemoteType.UTorrentRemote);
	}

	@Override
	public String getTitle() {
		return "¦ÌTorrent";
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			final Activity context) {
		UtorrentTask<ArrayList<RemoteTaskInfo>> task = UtorrentTask
				.newInstance(ipNPort);
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
	public BaseAsyncTask<Boolean> delete(String... hashes) {
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> download(String dir, String hash,
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
		// TODO Auto-generated method stub
		return null;
	}

}
