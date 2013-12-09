package org.hdstar.remote;

import java.util.ArrayList;

import org.hdstar.model.RemoteTaskInfo;
import org.hdstar.model.RssLabel;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;

import android.app.Activity;
import android.content.Context;

public interface IRemote {

	public String getTitle();

	public BaseAsyncTask<ArrayList<RemoteTaskInfo>> fetchList(
			TaskCallback<ArrayList<RemoteTaskInfo>> callback, String ip,
			Activity context);

	public BaseAsyncTask<Boolean> start(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes);

	public BaseAsyncTask<Boolean> pause(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes);

	public BaseAsyncTask<Boolean> stop(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes);

	public BaseAsyncTask<Boolean> delete(TaskCallback<Boolean> callback,
			String ip, Context context, String... hashes);

	public BaseAsyncTask<Boolean> download(TaskCallback<Boolean> callback,
			Context context, String ip, String dir, String hash,
			ArrayList<String> urls);

	public boolean rssEnable();

	public BaseAsyncTask<ArrayList<RssLabel>> fetchRssList(
			TaskCallback<ArrayList<RssLabel>> callback, String ip);

	public BaseAsyncTask<ArrayList<RssLabel>> refreshRssLabel(
			TaskCallback<ArrayList<RssLabel>> callback, String ip, String hash);

}
