package org.hdstar.common;

import org.hdstar.util.EncodeDecode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RemoteSetting {
	private SharedPreferences shared;
	private String remoteName;
	private static final String IP = "IP";
	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String DOWNLOAD_DIR = "DownloadDir";
	private static final String RM_FILE = "RemoveFile";

	public String ip;
	public String username;
	public String password;
	public String downloadDir;
	public boolean rmFile;

	public RemoteSetting(Context context, RemoteType type) {
		shared = context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this.remoteName = type.name();
		init();
	}

	public RemoteSetting(Context context, String remoteName) {
		shared = context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this.remoteName = remoteName;
		init();
	}

	private void init() {
		ip = getIp("");
		username = getUsername("");
		password = getPassword("");
		downloadDir = getDownloadDir("");
		rmFile = isRemoveFile(false);
	}

	// public String getIp() {
	// return ip;
	// }
	//
	// public String getUsername() {
	// return username;
	// }
	//
	// public String getPassword() {
	// return password;
	// }
	//
	// public String getDownloadDir() {
	// return downloadDir;
	// }

	public boolean isRemoveFile() {
		return rmFile;
	}

	public String getIp(String defValue) {
		return shared.getString(remoteName + IP, defValue);
	}

	public String getUsername(String defValue) {
		return shared.getString(remoteName + USERNAME, defValue);
	}

	public String getPassword(String defValue) {
		return EncodeDecode.decode(shared.getString(remoteName + PASSWORD,
				defValue));
	}

	public String getDownloadDir(String defValue) {
		return shared.getString(remoteName + DOWNLOAD_DIR, defValue);
	}

	public boolean isRemoveFile(boolean defValue) {
		return shared.getBoolean(remoteName + RM_FILE, defValue);
	}

	public void saveIp(String ip) {
		Editor editor = shared.edit();
		editor.putString(remoteName + IP, ip);
		editor.commit();
	}

	public void saveUsername(String username) {
		Editor editor = shared.edit();
		editor.putString(remoteName + USERNAME, username);
		editor.commit();
	}

	public void savePassword(String password) {
		Editor editor = shared.edit();
		editor.putString(remoteName + PASSWORD, EncodeDecode.encode(password));
		editor.commit();
	}

	public void saveDownloadDir(String downloadDir) {
		Editor editor = shared.edit();
		editor.putString(remoteName + DOWNLOAD_DIR, downloadDir);
		editor.commit();
	}

	public void saveRemoveFile(boolean removeFile) {
		Editor editor = shared.edit();
		editor.putBoolean(remoteName + RM_FILE, removeFile);
		editor.commit();
	}
}
