package org.hdstar.common;

import java.util.ArrayList;

import org.hdstar.util.EncodeDecode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RemoteSettingManager {
	private SharedPreferences prefs;
	public static final String MAX = "max";
	public static final String REMOTE_TYPE = "RemoteType";
	public static final String IP = "IP";
	public static final String NAME = "Name";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String DOWNLOAD_DIR = "DownloadDir";
	public static final String RM_FILE = "RemoveFile";

	public int order;
	public RemoteType type;
	public String ip;
	public String username;
	public String password;
	public String downloadDir;
	public boolean rmFile;

	public ArrayList<RemoteSetting> getAll(Context context) {
		prefs = context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
		int max = getMaxRemote();
		ArrayList<RemoteSetting> list = new ArrayList<RemoteSetting>();
		for(int i = 0; i < max; i++){
			list.add(get(i));
		}
		return list;
	}
	
	public RemoteSetting get(int order){
		RemoteSetting setting = new RemoteSetting();
		setting.order = order;
		setting.type = prefs.getString(REMOTE_TYPE + order, "");
		setting.name = prefs.getString(NAME + order, "");
		setting.ip = prefs.getString(IP + order, "");
		setting.username = prefs.getString(USERNAME + order, "");
		setting.password = EncodeDecode.decode(prefs.getString(PASSWORD + order, ""));
		setting.downloadDir = prefs.getString(DOWNLOAD_DIR + order, "");
		setting.rmFile = prefs.getBoolean(RM_FILE, false);
		return setting;
	}
	
//	public int getOrder(int defValue){
//		return shared.getInt("order", defValue);
//	}
//
//	public String getIp(String defValue) {
//		return shared.getString(IP + order, defValue);
//	}
//
//	public String getUsername(String defValue) {
//		return shared.getString(USERNAME + order, defValue);
//	}
//
//	public String getPassword(String defValue) {
//		return EncodeDecode
//				.decode(shared.getString(PASSWORD + order, defValue));
//	}
//
//	public String getDownloadDir(String defValue) {
//		return shared.getString(DOWNLOAD_DIR + order, defValue);
//	}
//
//	public boolean isRemoveFile(boolean defValue) {
//		return shared.getBoolean(RM_FILE + order, defValue);
//	}
//
//	public void saveIp(String ip) {
//		Editor editor = shared.edit();
//		editor.putString(IP + order, ip);
//		editor.commit();
//	}
//
//	public void saveUsername(String username) {
//		Editor editor = shared.edit();
//		editor.putString(USERNAME + order, username);
//		editor.commit();
//	}
//
//	public void savePassword(String password) {
//		Editor editor = shared.edit();
//		editor.putString(PASSWORD + order, EncodeDecode.encode(password));
//		editor.commit();
//	}
//
//	public void saveDownloadDir(String downloadDir) {
//		Editor editor = shared.edit();
//		editor.putString(DOWNLOAD_DIR + order, downloadDir);
//		editor.commit();
//	}
//
//	public void saveRemoveFile(boolean removeFile) {
//		Editor editor = shared.edit();
//		editor.putBoolean(RM_FILE + order, removeFile);
//		editor.commit();
//	}

	public int getMaxRemote() {
		return prefs.getInt(MAX, 0);
	}

	public void removeRemoteSettings(int order) {
		if (prefs.getString(REMOTE_TYPE + order, null) == null)
			return; // The settings that were requested to be removed do not
					// exist

		// Copy all settings higher than the supplied order number to the
		// previous spot
		Editor edit = prefs.edit();
		int max = getMaxRemote();
		for (int i = order; i < max; i++) {
			int j = i + 1;
			edit.putString(NAME + i,
					prefs.getString(NAME + j, null));
			edit.putString(REMOTE_TYPE + i,
					prefs.getString(REMOTE_TYPE + j, null));
			edit.putString(IP + i,
					prefs.getString(IP + j, null));
			edit.putString(USERNAME + i,
					prefs.getString(USERNAME + j, null));
			edit.putString(PASSWORD + i,
					prefs.getString(PASSWORD + j, null));
			edit.putString(DOWNLOAD_DIR + i,
					prefs.getString(DOWNLOAD_DIR + j, null));
			edit.putBoolean(RM_FILE + i, prefs.getBoolean(RM_FILE + j, false));
		}

		// Remove the last settings, of which we are now sure are no longer
		// required
		edit.remove(NAME + max);
		edit.remove(REMOTE_TYPE + max);
		edit.remove(USERNAME + max);
		edit.remove(IP + max);
		edit.remove(PASSWORD + max);
		edit.remove(DOWNLOAD_DIR + max);
		edit.remove(RM_FILE + max);
		
		edit.putInt(MAX, max-1);
		edit.commit();

	}
}
