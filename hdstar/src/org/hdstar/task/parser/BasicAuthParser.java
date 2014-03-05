package org.hdstar.task.parser;

import java.io.InputStream;

import org.hdstar.R;

import ch.boye.httpclientandroidlib.HttpResponse;

/**
 * 
 * BasicAuth验证的401处理 <br/>
 * 
 * @author robust
 */
public abstract class BasicAuthParser<T> extends ResponseParser<T> {

	@Override
	public T parse(HttpResponse res, InputStream in) {
		int statusCode = res.getStatusLine().getStatusCode();
		if (statusCode == 401) {
			msgId = R.string.http_401;
			return null;
		}
		return parseContent(res, in);
	}

	public abstract T parseContent(HttpResponse res, InputStream in);

}
