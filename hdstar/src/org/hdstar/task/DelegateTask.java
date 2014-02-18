package org.hdstar.task;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;

/**
 * 连接代理服务器(HDStarService)的请求任务
 * 
 * @author robust
 * 
 * @param <T>
 *            返回结果
 */
public class DelegateTask<T> extends BaseAsyncTask<T> {

	public DelegateTask(String cookie) {
		super(cookie);
	}

	public static <T> DelegateTask<T> newInstance(String cookie) {
		return new DelegateTask<T>(cookie);
	}

	@Override
	public void execGet(String url, final Type resultType) {
		parser = new DelegateGetParser<T>(resultType);
		super.execGet(url, resultType);
	}

	@Override
	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		parser = new ResponseParser<T>() {
			@Override
			public T parse(HttpResponse res, InputStream in) {
				if (location != null
						&& location.equals(res.getFirstHeader("Location")
								.getValue())) {
					return null;
				}
				setMessageId(ResponseParser.SUCCESS_MSG_ID);
				return null;
			}
		};
		super.execPost(url, nvp);
	}

}
