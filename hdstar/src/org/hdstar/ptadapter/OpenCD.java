package org.hdstar.ptadapter;

import org.hdstar.common.PTSiteType;

public class OpenCD extends NexusPHP {

	public OpenCD() {
		super(PTSiteType.OpenCD);
	}

	@Override
	public boolean needSecurityCode() {
		return true;
	}

}
