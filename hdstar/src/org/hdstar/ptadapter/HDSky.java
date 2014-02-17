package org.hdstar.ptadapter;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.ResponseParser;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class HDSky extends NexusPHP {

	public HDSky() {
		super(PTSiteType.HDSky);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

	@Override
	public BaseAsyncTask<String> login(String username, String password,
			String securityCode) {
		HttpPost post = new HttpPost(String.format(
				CommonUrls.NEXUSPHP_TAKE_LOGIN_URL, mType.getUrl()));
		try {
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("username", username));
			nvp.add(new BasicNameValuePair("password", password));
			nvp.add(new BasicNameValuePair("imagestring", securityCode));
			nvp.add(new BasicNameValuePair("imagehash", imageHash));
			post.setEntity(new UrlEncodedFormEntity(nvp, Const.CHARSET));
			ResponseParser<String> parser = new ResponseParser<String>(
					R.string.login_error) {

				@Override
				public String parse(HttpResponse res, InputStream in) {
					if (res.getFirstHeader("Location") == null) {
						return null;
					}
					String location = res.getFirstHeader("Location").getValue();
					if (location == null
							|| !location.equals(CommonUrls.NEXUSPHP_HOME_PAGE)) {
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
			};
			BaseAsyncTask<String> task = new BaseAsyncTask<String>(post, parser);
			task.setNeedContent(false);
			return task;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page,
			String keywords) {
		return null;
	}
}
