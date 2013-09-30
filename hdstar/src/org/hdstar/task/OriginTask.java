package org.hdstar.task;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;

/**
 * ����ԭʼ����������������
 * 
 * @author robust
 * 
 * @param <T>
 *            ���ؽ��
 */
public class OriginTask<T> extends MyAsyncTask<T> {

	public OriginTask(String cookie) {
		super(cookie);
	}

	public OriginTask(String cookie, ResponseParser<T> parser) {
		super(cookie, parser);
	}

	@Override
	public void execPost(String url, List<NameValuePair> nvp)
			throws UnsupportedEncodingException {
		execPost(url, nvp, "");
	}

	@Override
	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		parser = new ResponseParser<T>() {
			@Override
			public T parse(HttpResponse res, InputStream in) {
				if (res.getStatusLine().getStatusCode() == 302
						&& (location.equals("") || location.equals(res
								.getFirstHeader("Location")))) {
					setMessageId(ResponseParser.SUCCESS_MSG_ID);
				}
				return null;
			}
		};
		super.execPost(url, nvp);
	}

}
