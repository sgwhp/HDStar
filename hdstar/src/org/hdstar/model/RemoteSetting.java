package org.hdstar.model;

import org.hdstar.common.Const;
import org.hdstar.common.RemoteSettingManager;
import org.hdstar.common.RemoteType;
import org.hdstar.util.EncodeDecode;
import org.hdstar.widget.navigation.SimpleListItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSetting implements Parcelable, SimpleListItem {

	public int order = -1;
	public String type;
	public String name;
	public String ip;
	public String username;
	public String password;
	public String downloadDir;
	public boolean rmFile;

	public RemoteSetting() {
	}

	public RemoteSetting(Parcel in) {
		order = in.readInt();
		type = in.readString();
		name = in.readString();
		ip = in.readString();
		username = in.readString();
		password = in.readString();
		downloadDir = in.readString();
		rmFile = in.readByte() != 0;
	}

	public static RemoteType getRemoteType(String typeStr) {
		for (RemoteType type : RemoteType.values()) {
			if (type.name().equals(typeStr)) {
				return type;
			}
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(order);
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(ip);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeString(downloadDir);
		dest.writeByte((byte) (rmFile ? 1 : 0));
	}

	public static final Parcelable.Creator<RemoteSetting> CREATOR = new Parcelable.Creator<RemoteSetting>() {

		@Override
		public RemoteSetting createFromParcel(Parcel source) {
			return new RemoteSetting(source);
		}

		@Override
		public RemoteSetting[] newArray(int size) {
			return new RemoteSetting[size];
		}
	};

	public void saveIp(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Const.REMOTE_SHARED_PREFS, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(RemoteSettingManager.IP + order, ip);
		editor.commit();
	}

	public void saveUsername(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Const.REMOTE_SHARED_PREFS, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(RemoteSettingManager.USERNAME + order, username);
		editor.commit();
	}

	public void savePassword(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Const.REMOTE_SHARED_PREFS, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(RemoteSettingManager.PASSWORD + order,
				EncodeDecode.encode(password));
		editor.commit();
	}

	public void saveRemoveFile(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Const.REMOTE_SHARED_PREFS, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(RemoteSettingManager.RM_FILE + order, rmFile);
		editor.commit();
	}

	public void saveDownloadDir(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Const.REMOTE_SHARED_PREFS, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(RemoteSettingManager.DOWNLOAD_DIR + order, downloadDir);
		editor.commit();
	}

	@Override
	public String getName() {
		return name;
	}
}
