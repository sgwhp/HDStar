package org.hdstar.remote;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hdstar.common.Const;
import org.hdstar.component.activity.RemoteLoginActivity;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.ResponseParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class RutorrentRemote implements IRemote {

	@Override
	public String getTitle() {
		return "rutorrent";
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			TaskCallback<ArrayList<RemoteTaskInfo>> callback, String ip,
			final Activity context) {
		BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = BaseAsyncTask
				.newInstance();
		task.attach(callback);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cmd", "d.get_throttle_name="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=sch_ignore"));
		params.add(new BasicNameValuePair("cmd", "cat=$d.views="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=seedingtime"));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=addtime"));
		params.add(new BasicNameValuePair("mode", "list"));
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					params, new ResponseParser<ArrayList<RemoteTaskInfo>>() {

						@Override
						public ArrayList<RemoteTaskInfo> parse(
								HttpResponse res, InputStream in) {
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
							JsonObject obj = element.getAsJsonObject()
									.getAsJsonObject("t");
							Set<Entry<String, JsonElement>> set = obj
									.entrySet();
							ArrayList<RemoteTaskInfo> result = new ArrayList<RemoteTaskInfo>();
							RemoteTaskInfo info;
							JsonArray arr;
							for (Entry<String, JsonElement> entry : set) {
								info = new RemoteTaskInfo();
								arr = entry.getValue().getAsJsonArray();
								info.hash = entry.getKey();
								info.open = arr.get(0).getAsInt();
								info.state = arr.get(3).getAsInt();
								info.title = arr.get(4).toString();
								info.size = arr.get(5).getAsLong();
								info.completeSize = arr.get(8).getAsLong();
								info.uploaded = arr.get(9).getAsLong();
								info.ratio = arr.get(10).getAsFloat() / 1000;
								info.upSpeed = arr.get(11).getAsLong();
								info.dlSpeed = arr.get(12).getAsLong();
								result.add(info);
							}
							msgId = SUCCESS_MSG_ID;
							return result;
						}
					});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
		return ctrlTask(callback, ip, context, "remove", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> download(TaskCallback<Boolean> callback,
			Context context, String ip, String dir, String hash,
			ArrayList<String> urls) {
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "loadtorrents"));
		params.add(new BasicNameValuePair("dir_edit", dir));
		params.add(new BasicNameValuePair("rss", hash));
		for (String url : urls) {
			params.add(new BasicNameValuePair("url", url));
		}
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RSS_ACTION_URL, ip),
					params, new ResponseParser<Boolean>() {

						@Override
						public Boolean parse(HttpResponse res, InputStream in) {
							if (res.getStatusLine().getStatusCode() == 200) {
								msgId = SUCCESS_MSG_ID;
								return true;
							}
							return false;
						}
					});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return task;
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> fetchRssList(
			TaskCallback<ArrayList<RssLabel>> callback, String ip) {
		BaseAsyncTask<ArrayList<RssLabel>> task = BaseAsyncTask.newInstance();
		task.attach(callback);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "get"));
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RSS_ACTION_URL, ip),
					params, new ResponseParser<ArrayList<RssLabel>>() {

						@Override
						public ArrayList<RssLabel> parse(HttpResponse res,
								InputStream in) {
							JsonParser parser = new JsonParser();
							JsonElement element = parser
									.parse(new InputStreamReader(in));
							JsonArray arr = element.getAsJsonObject()
									.getAsJsonArray("list");
							Gson gson = new Gson();
							ArrayList<RssLabel> result = gson.fromJson(arr,
									new TypeToken<ArrayList<RssLabel>>() {
									}.getType());
							msgId = SUCCESS_MSG_ID;
							return result;
						}
					});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return task;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(
			TaskCallback<ArrayList<RssLabel>> callback, String ip, String hash) {
		final BaseAsyncTask<ArrayList<RssLabel>> task = new BaseAsyncTask<ArrayList<RssLabel>>();
		task.attach(callback);
		task.execGet(
				String.format(Const.Urls.RUTORRENT_RSS_REFRESH_URL, ip, hash),
				new ResponseParser<ArrayList<RssLabel>>() {

					@Override
					public ArrayList<RssLabel> parse(HttpResponse res,
							InputStream in) {
						JsonParser parser = new JsonParser();
						JsonElement element = parser
								.parse(new InputStreamReader(in));
						JsonArray arr = element.getAsJsonObject()
								.getAsJsonArray("list");
						Gson gson = new Gson();
						ArrayList<RssLabel> result = gson.fromJson(arr,
								new TypeToken<ArrayList<RssLabel>>() {
								}.getType());
						msgId = SUCCESS_MSG_ID;
						return result;
					}
				});
		return task;
	}

	private BaseAsyncTask<Boolean> ctrlTask(TaskCallback<Boolean> callback,
			String ip, Context context, String mode, String... hashes) {
		final BaseAsyncTask<Boolean> task = new BaseAsyncTask<Boolean>();
		task.attach(callback);
		try {
			task.execPost(
					String.format(Const.Urls.RUTORRENT_RPC_ACTION_URL, ip),
					buildParams(mode), new ResponseParser<Boolean>() {

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
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<NameValuePair> buildParams(String mode, String... hashes) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", mode));
		for (String hash : hashes) {
			params.add(new BasicNameValuePair("hash", hash));
		}
		return params;
	}

}
