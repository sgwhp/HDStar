package org.hdstar.remote;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.RemoteType;
import org.hdstar.component.activity.RemoteLoginActivity;
import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;
import org.hdstar.util.HttpClientManager;

import android.app.Activity;
import android.content.Intent;
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class RuTorrentRemote extends RemoteBase {

	public RuTorrentRemote() {
		super(RemoteType.RuTorrentRemote);
	}

	@Override
	public String getTitle() {
		return "rutorrent";
	}

	@Override
	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			final Activity context) {
		HttpPost post = new HttpPost(String.format(
				Const.Urls.RUTORRENT_RPC_ACTION_URL, ipNPort));
		ResponseParser<ArrayList<RemoteTaskInfo>> parser = new ResponseParser<ArrayList<RemoteTaskInfo>>() {

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
				JsonElement element = parser.parse(new InputStreamReader(in));
				JsonObject obj = element.getAsJsonObject().getAsJsonObject("t");
				Set<Entry<String, JsonElement>> set = obj.entrySet();
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
					info.label = arr.get(14).getAsString();
					result.add(info);
				}
				msgId = SUCCESS_MSG_ID;
				return result;
			}
		};
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cmd", "d.get_throttle_name="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=sch_ignore"));
		params.add(new BasicNameValuePair("cmd", "cat=$d.views="));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=seedingtime"));
		params.add(new BasicNameValuePair("cmd", "d.get_custom=addtime"));
		params.add(new BasicNameValuePair("mode", "list"));
		try {
			post.setEntity(new UrlEncodedFormEntity(params, Const.CHARSET));
			BaseAsyncTask<ArrayList<RemoteTaskInfo>> task = BaseAsyncTask
					.newInstance(post, parser);
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
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
		return ctrlTask("remove", hashes);
	}

	@Override
	public BaseAsyncTask<Boolean> download(String dir, String hash,
			ArrayList<String> urls) {
		HttpPost post = new HttpPost(String.format(
				Const.Urls.RUTORRENT_RSS_ACTION_URL, ipNPort));
		ResponseParser<Boolean> parser = new ResponseParser<Boolean>() {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 200) {
					msgId = SUCCESS_MSG_ID;
					return true;
				}
				return false;
			}
		};
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "loadtorrents"));
		params.add(new BasicNameValuePair("dir_edit", dir));
		params.add(new BasicNameValuePair("rss", hash));
		for (String url : urls) {
			params.add(new BasicNameValuePair("url", url));
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(params, Const.CHARSET));
			final BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(post,
					parser);
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> fetchRssList() {
		HttpPost post = new HttpPost(String.format(
				Const.Urls.RUTORRENT_RSS_ACTION_URL, ipNPort));
		ResponseParser<ArrayList<RssLabel>> parser = new ResponseParser<ArrayList<RssLabel>>() {

			@Override
			public ArrayList<RssLabel> parse(HttpResponse res, InputStream in) {
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(new InputStreamReader(in));
				JsonArray arr = element.getAsJsonObject()
						.getAsJsonArray("list");
				Gson gson = new Gson();
				ArrayList<RssLabel> result = gson.fromJson(arr,
						new TypeToken<ArrayList<RssLabel>>() {
						}.getType());
				msgId = SUCCESS_MSG_ID;
				return result;
			}
		};
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "get"));
		try {
			post.setEntity(new UrlEncodedFormEntity(params, Const.CHARSET));
			BaseAsyncTask<ArrayList<RssLabel>> task = BaseAsyncTask
					.newInstance(post, parser);
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(String hash) {
		HttpGet get = new HttpGet(String.format(
				Const.Urls.RUTORRENT_RSS_REFRESH_URL, ipNPort, hash));
		ResponseParser<ArrayList<RssLabel>> parser = new ResponseParser<ArrayList<RssLabel>>() {

			@Override
			public ArrayList<RssLabel> parse(HttpResponse res, InputStream in) {
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(new InputStreamReader(in));
				JsonArray arr = element.getAsJsonObject()
						.getAsJsonArray("list");
				Gson gson = new Gson();
				ArrayList<RssLabel> result = gson.fromJson(arr,
						new TypeToken<ArrayList<RssLabel>>() {
						}.getType());
				msgId = SUCCESS_MSG_ID;
				return result;
			}
		};
		final BaseAsyncTask<ArrayList<RssLabel>> task = BaseAsyncTask
				.newInstance(get, parser);
		return task;
	}

	private BaseAsyncTask<Boolean> ctrlTask(String mode, String... hashes) {
		HttpPost post = new HttpPost(String.format(
				Const.Urls.RUTORRENT_RPC_ACTION_URL, ipNPort));
		ResponseParser<Boolean> parser = new ResponseParser<Boolean>() {

			@Override
			public Boolean parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 200) {
					msgId = SUCCESS_MSG_ID;
					return true;
				}
				return false;
			}
		};
		try {
			post.setEntity(new UrlEncodedFormEntity(buildParams(mode),
					Const.CHARSET));
			final BaseAsyncTask<Boolean> task = BaseAsyncTask.newInstance(post,
					parser);
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

	@Override
	public boolean diskEnable() {
		return true;
	}

	@Override
	public BaseAsyncTask<long[]> getDiskInfo() {
		HttpGet get = new HttpGet(String.format(
				Const.Urls.RUTORRENT_DISK_SPACE_URL, ipNPort)
				+ System.currentTimeMillis());
		ResponseParser<long[]> parser = new ResponseParser<long[]>(
				R.string.get_disk_space_failed) {

			@Override
			public long[] parse(HttpResponse res, InputStream in) {
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(new InputStreamReader(in))
						.getAsJsonObject();
				long[] space = new long[2];
				space[0] = obj.get("total").getAsLong();
				space[1] = obj.get("free").getAsLong();
				msgId = SUCCESS_MSG_ID;
				return space;
			}
		};
		BaseAsyncTask<long[]> diskTask = BaseAsyncTask.newInstance(get, parser);
		return diskTask;
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
				Const.Urls.RUTORRENT_HOME_PAGE, ipNPort));
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

}
