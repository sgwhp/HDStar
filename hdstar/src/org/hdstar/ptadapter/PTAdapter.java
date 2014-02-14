package org.hdstar.ptadapter;

import java.util.ArrayList;

import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;

import android.graphics.Bitmap;

public abstract class PTAdapter {
	private PTSiteType mType;
	protected String mUrl;

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public PTSiteType getmType() {
		return mType;
	}

	public void setmType(PTSiteType mType) {
		this.mType = mType;
	}

	public boolean needSecurityCode() {
		return false;
	}

	public BaseAsyncTask<Bitmap> getSecurityImage() {
		return null;
	}

	public abstract BaseAsyncTask<String> login(String username,
			String password, String securityCode);

	public abstract BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page);

}
