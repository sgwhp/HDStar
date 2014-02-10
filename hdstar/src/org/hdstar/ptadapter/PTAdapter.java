package org.hdstar.ptadapter;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.PTSiteType;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask;

import ch.boye.httpclientandroidlib.NameValuePair;

import android.graphics.Bitmap;

public abstract class PTAdapter {
	private PTSiteType mType;
	
	public PTSiteType getmType() {
		return mType;
	}

	public void setmType(PTSiteType mType) {
		this.mType = mType;
	}

	public abstract BaseAsyncTask<Bitmap> getSecurityImage();
	
	public abstract BaseAsyncTask<String> login(List<NameValuePair> nvp);
	
	public abstract BaseAsyncTask<ArrayList<Torrent>> getTorrents(int page);

}
