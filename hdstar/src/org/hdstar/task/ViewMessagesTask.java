package org.hdstar.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.model.Message;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

public class ViewMessagesTask extends MyAsyncTask<List<Message>> {

	public ViewMessagesTask(String cookie) {
		super(cookie);
	}

	@Override
	protected List<Message> doInBackground(String... params) {
		InputStream in = null;
		request = new HttpGet(Const.Urls.SERVER_VIEW_MESSAGES_URL);
		request.setHeader("Cookie", cookie);
		try {
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				in = response.getEntity().getContent();
				Gson gson = new Gson();
				ResponseWrapper<List<Message>> wrapper = gson.fromJson(
						new InputStreamReader(in),
						new TypeToken<ResponseWrapper<List<Message>>>() {
						}.getType());
				setMessageId(TaskCallback.SUCCESS_MSG_ID);
				return wrapper.body;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			setMessageId(R.string.time_out);
			CustomHttpClient.restClient();
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			request.abort();
			if ("Connection reset by peer".equals(e.getMessage())) {
				CustomHttpClient.restClient();
			}
		} catch (IOException e) {
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

}
