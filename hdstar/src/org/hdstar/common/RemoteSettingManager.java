package org.hdstar.common;

import java.util.ArrayList;

import org.hdstar.util.EncodeDecode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RemoteSettingManager {
	public static final String MAX = "max";
	public static final String REMOTE_TYPE = "RemoteType";
	public static final String IP = "IP";
	public static final String NAME = "Name";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String DOWNLOAD_DIR = "DownloadDir";
	public static final String RM_FILE = "RemoveFile";

	public static ArrayList<RemoteSetting> getAll(Context context) {
		SharedPreferences prefs = getPrefs(context);
		int max = getMaxRemote(prefs);
		ArrayList<RemoteSetting> list = new ArrayList<RemoteSetting>();
		for (int i = 0; i < max; i++) {
			list.add(get(prefs, i));
		}
		return list;
	}

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
	}

	public static RemoteSetting get(SharedPreferences prefs, int order) {
		RemoteSetting setting = new RemoteSetting();
		setting.order = order;
		setting.type = prefs.getString(REMOTE_TYPE + order, "");
		setting.name = prefs.getString(NAME + order, "");
		setting.ip = prefs.getString(IP + order, "");
		setting.username = prefs.getString(USERNAME + order, "");
		setting.password = EncodeDecode.decode(prefs.getString(
				PASSWORD + order, ""));
		setting.downloadDir = prefs.getString(DOWNLOAD_DIR + order, "");
		setting.rmFile = prefs.getBoolean(RM_FILE, false);
		return setting;
	}

	public static int getMaxRemote(SharedPreferences prefs) {
		return prefs.getInt(MAX, 0);
	}

	public static void removeRemoteSettings(SharedPreferences prefs, int order) {
		if (prefs.getString(REMOTE_TYPE + order, null) == null)
			return; // The settings that were requested to be removed do not
					// exist

		// Copy all settings higher than the supplied order number to the
		// previous spot
		Editor edit = prefs.edit();
		int max = getMaxRemote(prefs);
		for (int i = order; i < max; i++) {
			int j = i + 1;
			edit.putString(NAME + i, prefs.getString(NAME + j, null));
			edit.putString(REMOTE_TYPE + i,
					prefs.getString(REMOTE_TYPE + j, null));
			edit.putString(IP + i, prefs.getString(IP + j, null));
			edit.putString(USERNAME + i, prefs.getString(USERNAME + j, null));
			edit.putString(PASSWORD + i, prefs.getString(PASSWORD + j, null));
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

		edit.putInt(MAX, max - 1);
		edit.commit();
	}

	public static void removeRemoteSettings(Context context, int order) {
		removeRemoteSettings(getPrefs(context), order);
	}

	public static void save(Context context, RemoteSetting setting) {
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(NAME + setting.order, setting.name);
		edit.putString(REMOTE_TYPE + setting.order, setting.type);
		edit.putString(IP + setting.order, setting.ip);
		edit.putString(USERNAME + setting.order, setting.username);
		edit.putString(PASSWORD + setting.order,
				EncodeDecode.encode(setting.password));
		edit.putString(DOWNLOAD_DIR + setting.order, setting.downloadDir);
		edit.putBoolean(RM_FILE + setting.order, setting.rmFile);
		edit.commit();
	}

	public static void add(Context context, RemoteSetting setting) {
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(NAME + setting.order, setting.name);
		edit.putString(REMOTE_TYPE + setting.order, setting.type);
		edit.putString(IP + setting.order, setting.ip);
		edit.putString(USERNAME + setting.order, setting.username);
		edit.putString(PASSWORD + setting.order,
				EncodeDecode.encode(setting.password));
		edit.putString(DOWNLOAD_DIR + setting.order, setting.downloadDir);
		edit.putBoolean(RM_FILE + setting.order, setting.rmFile);
		edit.putInt(MAX, setting.order + 1);
		edit.commit();
	}
}
