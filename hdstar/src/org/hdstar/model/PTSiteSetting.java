package org.hdstar.model;

import org.hdstar.common.PTSiteType;
import org.hdstar.widget.navigation.SimpleListItem;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * PTվ������. <br/>
 * 
 * @author robust
 */
public class PTSiteSetting implements Parcelable, SimpleListItem {
	public int order;
	public String type;
	public String username;
	public String password;
	public String cookie;
	public String torrentUrl;// ��ǰʹ�õ�����ҳ���ַ������־û�
	public String torrentPageName;//ҳ�����ƣ�����־û�
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
	 * ���Ƴ���torrentUrl�������������
	 * 
	 * @param torrentUrl
	 *            torrentUrl����ֵ
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
