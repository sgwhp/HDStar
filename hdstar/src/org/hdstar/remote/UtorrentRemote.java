package org.hdstar.remote;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.hdstar.common.Const;
import org.hdstar.component.activity.RemoteLoginActivity;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;
import org.hdstar.task.UtorrentTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import ch.boye.httpclientandroidlib.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class UtorrentRemote implements IRemote {

	@Override
	public String getTitle() {
		return "¦Ìtorrent";
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			TaskCallback<ArrayList<RemoteTaskInfo>> callback, String ip,
			final Activity context) {
		UtorrentTask<ArrayList<RemoteTaskInfo>> task = UtorrentTask
				.newInstance(ip);
		task.attach(callback);
		task.execGet(Const.Urls.UTORRENT_GET_LIST_URL,
				new ResponseParser<ArrayList<RemoteTaskInfo>>() {

					@Override
					public ArrayList<RemoteTaskInfo> parse(HttpResponse res,
							InputStream in) {
						if (res.getStatusLine().getStatusCode() == 401) {
							Intent intent = new Intent(context,
									RemoteLoginActivity.class);
							context.startActivity(intent);
							context.finish();
							return null;
						}
						JsonParser parser = new JsonParser();
						JsonElement element = parser
								.parse(new InputStreamReader(in));
						JsonArray arr = element.getAsJsonObject()
								.getAsJsonArray("torrents");
						ArrayList<RemoteTaskInfo> result = new ArrayList<RemoteTaskInfo>();
						RemoteTaskInfo info;
						// JsonArray arr;
						// for (Entry<String, JsonElement> entry : set) {
						// info = new RemoteTaskInfo();
						// arr = entry.getValue().getAsJsonArray();
						// info.hash = entry.getKey();
						// info.open = arr.get(0).getAsInt();
						// info.state = arr.get(3).getAsInt();
						// info.title = arr.get(4).toString();
						// info.size = arr.get(5).getAsLong();
						// info.completeSize = arr.get(8).getAsLong();
						// info.uploaded = arr.get(9).getAsLong();
						// info.ratio = arr.get(10).getAsFloat() / 1000;
						// info.upSpeed = arr.get(11).getAsLong();
						// info.dlSpeed = arr.get(12).getAsLong();
						// result.add(info);
						// }
						msgId = SUCCESS_MSG_ID;
						return result;
					}
				});
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

	private BaseAsyncTask<Boolean> ctrlTask(TaskCallback<Boolean> callback,
			String ip, Context context, String mode, String... hashes) {
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(callback);
		task.execGet(
				buildParams(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip, mode,
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
	public BaseAsyncTask<Boolean> start(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes) {
		return ctrlTask(callback, ip, context, "start", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> pause(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes) {
		return ctrlTask(callback, ip, context, "pause", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> stop(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes) {
		return ctrlTask(callback, ip, context, "stop", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> delete(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes) {
		return null;
	}

	@Override
	public BaseAsyncTask<Boolean> download(TaskCallback<Boolean> callback,
			Context context, String ip, String dir, String hash,
			ArrayList<String> urls) {
		return null;
	}

	@Override
	public boolean rssEnable() {
		return false;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> fetchRssList(
			TaskCallback<ArrayList<RssLabel>> callback, String ip) {
		return null;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(
			TaskCallback<ArrayList<RssLabel>> callback, String ip, String hash) {
		return null;
	}

}
