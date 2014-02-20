package org.hdstar.model;

import org.hdstar.common.PTSiteType;
import org.hdstar.widget.navigation.SimpleListItem;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * PT站点设置. <br/>
 * 
 * @author robust
 */
public class PTSiteSetting implements Parcelable, SimpleListItem {
	public int order;
	public String type;
	public String username;
	public String password;
	public String cookie;
	public String torrentUrl;// 当前使用的种子页面地址，无需持久化
	public String torrentPageName;//页面名称，无需持久化
	private PTSiteType siteType;

	public PTSiteSetting() {
	}

	public PTSiteSetting(Parcel in) {
		order = in.readInt();
		type = in.readString();
		username = in.readString();
		password = in.readString();
		cookie = in.readString();
		torrentUrl = in.readString();
		torrentPageName = in.readString();
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
		dest.writeString(torrentUrl);
		dest.writeString(torrentPageName);
	}

	public PTSiteType getSiteType() {
		if (siteType == null) {
			siteType = PTSiteType.getByName(type);
		}
		return siteType;
	}

	/**
	 * 复制除了torrentUrl以外的所有属性
	 * 
	 * @param torrentUrl
	 *            torrentUrl的新值
	 * @return
	 */
	public PTSiteSetting copy(String torrentUrl, String torrentPageName) {
		PTSiteSetting setting = new PTSiteSetting();
		setting.order = order;
		setting.type = type;
		setting.username = username;
		setting.password = password;
		setting.cookie = cookie;
		setting.torrentUrl = torrentUrl;
		setting.torrentPageName = torrentPageName;
		return setting;
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

	@Override
	public String getName() {
		if(torrentPageName == null){
			return getSiteType().getName();
		}
		return torrentPageName;
	}

}
