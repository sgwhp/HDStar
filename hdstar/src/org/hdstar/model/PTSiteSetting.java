package org.hdstar.model;

import org.hdstar.common.PTSiteType;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * PT站点设置. <br/>
 * 日期: 2014年2月10日 上午10:37:14 <br/>
 * 
 * @author robust
 */
public class PTSiteSetting implements Parcelable {
	public int order;
	public String type;
	public String username;
	public String password;
	public String cookie;
	private PTSiteType siteType;

	public PTSiteSetting() {
	}

	public PTSiteSetting(Parcel in) {
		order = in.readInt();
		type = in.readString();
		username = in.readString();
		password = in.readString();
		cookie = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(order);
		dest.writeString(type);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeString(cookie);
	}

	public PTSiteType getSiteType() {
		if (siteType == null) {
			siteType = PTSiteType.getByName(type);
		}
		return siteType;
	}

	public static final Parcelable.Creator<PTSiteSetting> CREATOR = new Parcelable.Creator<PTSiteSetting>() {

		@Override
		public PTSiteSetting createFromParcel(Parcel source) {
			return new PTSiteSetting(source);
		}

		@Override
		public PTSiteSetting[] newArray(int size) {
			return new PTSiteSetting[size];
		}
	};

}
