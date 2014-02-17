package org.hdstar.ptadapter;

import org.hdstar.common.PTSiteType;

public class CHDBits extends NexusPHP {

	public CHDBits() {
		super(PTSiteType.CHDBits);
	}

	@Override
	public boolean rssEnable() {
		return true;
	}

}
