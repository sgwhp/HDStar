package org.hdstar.task.parser;

import java.io.InputStream;

import ch.boye.httpclientandroidlib.HttpResponse;

public class DefaultGetParser extends ResponseParser<Boolean> {

	@Override
	public Boolean parse(HttpResponse res, InputStream in) {
		if (res.getStatusLine().getStatusCode() == 200) {
			setSucceeded();
			return true;
		}
		return false;
	}

}
