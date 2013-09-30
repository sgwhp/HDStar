package org.hdstar.task;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;

import org.hdstar.model.ResponseWrapper;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;

import com.google.gson.Gson;

/**
 * ���Ӵ��������(HDStarService)����������
 * 
 * @author robust
 * 
 * @param <T>
 *            ���ؽ��
 */
public class DelegateTask<T> extends MyAsyncTask<T> {

	public DelegateTask(String cookie) {
		super(cookie);
	}

	@Override
	public void execGet(String url, final Type resultType) {
		parser = new ResponseParser<T>() {
			@Override
			public T parse(HttpResponse res, InputStream in) {
				Gson gson = new Gson();
				ResponseWrapper<T> wrapper = gson.fromJson(
						new InputStreamReader(in), resultType);
				setMessageId(ResponseParser.SUCCESS_MSG_ID);
				return wrapper.body;
			}
		};
		super.execGet(url, resultType);
	}

	@Override
	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		parser = new ResponseParser<T>() {
			@Override
			public T parse(HttpResponse res, InputStream in) {
				if (location != null
						&& location.equals(res.getFirstHeader("Location"))) {
					return null;
				}
				setMessageId(ResponseParser.SUCCESS_MSG_ID);
				return null;
			}
		};
		super.execPost(url, nvp);
	}

}
