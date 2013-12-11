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

	public RemoteSetting(Context context, RemoteType type) {
		shared = context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this.remoteName = type.name();
	}
	
	public RemoteSetting(Context context, String remoteName){
		shared = context.getSharedPreferences(Const.REMOTE_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this.remoteName = remoteName;
	}

	public String getIp() {
		return getIp(null);
	}

	public String getUsername() {
		return getUsername(null);
	}

	public String getPassword() {
		return getPassword(null);
	}

	public String getDownloadDir() {
		return getDownloadDir(null);
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
}
