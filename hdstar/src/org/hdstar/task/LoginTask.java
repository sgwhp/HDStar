package org.hdstar.task;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;

public class LoginTask extends BaseAsyncTask<String> {

	@Override
	public void execPost(String url, List<NameValuePair> nvp,
			final String location) throws UnsupportedEncodingException {
		super.execPost(url, nvp, new ResponseParser<String>(
				R.string.login_error) {
			@Override
			public String parse(HttpResponse res, InputStream in) {
				if (res.getFirstHeader("Location") == null) {
					return null;
				}
				String location = res.getFirstHeader("Location").getValue();
				if (location == null || !location.equals(Const.Urls.HOME_PAGE)) {
					return null;
				}
				String cookieStr = "";
				Header[] cookies = res.getHeaders("set-cookie");
				for (Header h : cookies) {
					String str = h.getValue();
					cookieStr += str.substring(0, str.indexOf(";") + 1);
				}
				msgId = ResponseParser.SUCCESS_MSG_ID;
				return cookieStr;
			}
		});
	}

}
