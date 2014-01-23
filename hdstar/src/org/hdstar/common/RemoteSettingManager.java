package org.hdstar.common;

import java.util.ArrayList;

import org.hdstar.model.RemoteSetting;
import org.hdstar.util.DES;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RemoteSettingManager {
	public static final String MAX = "max";
	public static final String DEFAULT = "default";
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

	/**
	 * 
	 * 获取默认的远程服务器设置顺序. <br/>
	 * 
	 * @author robust
	 * @param context
	 * @return
	 */
	public static int getDefault(Context context) {
		SharedPreferences prefs = getPrefs(context);
		return prefs.getInt(DEFAULT, 0);
	}

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
	}

	public static RemoteSetting get(SharedPreferences prefs, int order) {
		RemoteSetting setting = new RemoteSetting();
		try {
			setting.order = order;
			setting.type = prefs.getString(REMOTE_TYPE + order, "");
			setting.name = prefs.getString(NAME + order, "");
			setting.ip = prefs.getString(IP + order, "");
			setting.username = prefs.getString(USERNAME + order, "");
			setting.password = DES.decryptDES(
					prefs.getString(PASSWORD + order, ""), Const.TAG);
			setting.downloadDir = prefs.getString(DOWNLOAD_DIR + order, "");
			setting.rmFile = prefs.getBoolean(RM_FILE + order, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public static void save(Context context, RemoteSetting setting,
			boolean isDefault) {
		try {
			SharedPreferences prefs = getPrefs(context);
			Editor edit = prefs.edit();
			if (isDefault) {
				edit.putInt(DEFAULT, setting.order);
			}
			edit.putString(NAME + setting.order, setting.name);
			edit.putString(REMOTE_TYPE + setting.order, setting.type);
			edit.putString(IP + setting.order, setting.ip);
			edit.putString(USERNAME + setting.order, setting.username);
			edit.putString(PASSWORD + setting.order,
					DES.encryptDES(setting.password, Const.TAG));
			edit.putString(DOWNLOAD_DIR + setting.order, setting.downloadDir);
			edit.putBoolean(RM_FILE + setting.order, setting.rmFile);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void add(Context context, RemoteSetting setting,
			boolean isDefault) {
		try {
			SharedPreferences prefs = getPrefs(context);
			Editor edit = prefs.edit();
			if (isDefault) {
				edit.putInt(DEFAULT, setting.order);
			}
			edit.putString(NAME + setting.order, setting.name);
			edit.putString(REMOTE_TYPE + setting.order, setting.type);
			edit.putString(IP + setting.order, setting.ip);
			edit.putString(USERNAME + setting.order, setting.username);
			edit.putString(PASSWORD + setting.order,
					DES.decryptDES(setting.password, Const.TAG));
			edit.putString(DOWNLOAD_DIR + setting.order, setting.downloadDir);
			edit.putBoolean(RM_FILE + setting.order, setting.rmFile);
			edit.putInt(MAX, setting.order + 1);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
