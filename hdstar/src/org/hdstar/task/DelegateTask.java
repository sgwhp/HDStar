package org.hdstar.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 连接代理服务器(HDStarService)的请求任务
 * 
 * @author wuhongping
 * 
 * @param <T>
 *            返回结果
 */
public class DelegateTask<T> extends MyAsyncTask<T> {

	public DelegateTask(String cookie) {
		super(cookie);
	}

	@Override
	protected T doInBackground(String... params) {
		HttpClient client = CustomHttpClient.getHttpClient();
		BufferedInputStream in = null;
		request.setHeader("Cookie", cookie);
		try {
			HttpResponse response = client.execute(request);
			in = new BufferedInputStream(response.getEntity().getContent(),
					8192);

			Gson gson = new Gson();
			ResponseWrapper<T> wrapper = gson.fromJson(
					new InputStreamReader(in),
					new TypeToken<ResponseWrapper<T>>() {
					}.getType());
			setMessageId(TaskCallback.SUCCESS_MSG_ID);
			return wrapper.body;
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			request.abort();
			e.printStackTrace();
		} catch (ConnectException e) {
			request.abort();
			setMessageId(R.string.connection_refused);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			request.abort();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (IOException e) {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.abort();
			e.printStackTrace();
		} catch (Exception e) {
			request.abort();
			e.printStackTrace();
		} finally {
			IOUtils.closeInputStreamIgnoreExceptions(in);
			request.releaseConnection();
		}
		return null;
	}

	public void get(String url) {
		request = new HttpGet(url);
		this.execute("");
	}

	public void post(String url, List<NameValuePair> nvp)
			throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(nvp, Const.CHARSET));
		request = post;
		this.execute("");
	}

}
