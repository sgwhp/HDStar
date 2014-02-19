package org.hdstar.ptadapter;

import java.util.List;

import org.hdstar.common.PTSiteType;

import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class MTeam extends NexusPHP {

	public MTeam() {
		super(PTSiteType.MTeam);
	}

	@Override
	protected List<NameValuePair> buildLoginParams(String username,
			String password, String securityCode) {
		List<NameValuePair> nvp = super.buildLoginParams(username, password,
				securityCode);
		nvp.add(new BasicNameValuePair("ssl", "yes"));
		nvp.add(new BasicNameValuePair("trackerssl", "yes"));
		return nvp;
	}

}
