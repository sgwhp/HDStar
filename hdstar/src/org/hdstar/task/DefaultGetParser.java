package org.hdstar.task;

import java.io.InputStream;

import ch.boye.httpclientandroidlib.HttpResponse;

public class DefaultGetParser extends ResponseParser<Boolean> {

	@Override
	public Boolean parse(HttpResponse res, InputStream in) {
		if (res.getStatusLine().getStatusCode() == 200) {
			msgId = SUCCESS_MSG_ID;
			return true;
		}
		return false;
	}

}
