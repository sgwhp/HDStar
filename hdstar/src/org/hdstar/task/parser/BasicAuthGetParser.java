package org.hdstar.task.parser;

import java.io.InputStream;

import ch.boye.httpclientandroidlib.HttpResponse;

/**
 * 
 * BasicAuth验证，成功即返回200代码都用此类处理 <br/>
 * 
 * @author robust
 */
public class BasicAuthGetParser extends BasicAuthParser<Boolean> {

	@Override
	public Boolean parseContent(HttpResponse res, InputStream in) {
		int statusCode = res.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			setSucceeded();
			return true;
		}
		return false;
	}

}
