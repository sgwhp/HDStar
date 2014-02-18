package org.hdstar.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hdstar.model.PTSiteSetting;
import org.hdstar.util.DES;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * PT站点设置. <br/>
 * 日期: 2014年2月10日 上午10:15:58 <br/>
 * 
 * @author robust
 */
public class PTSiteSettingManager {
	public static final String MAX = "max";
	public static final String SITE_TYPE = "siteType";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String COOKIE = "cookie";

	public static ArrayList<PTSiteSetting> getAll(Context context) {
		SharedPreferences prefs = getPrefs(context);
		int max = getMax(prefs);
		ArrayList<PTSiteSetting> sites = new ArrayList<PTSiteSetting>();
		for (int i = 0; i < max; i++) {
			sites.add(get(prefs, i));
		}
		return sites;
	}

	public static void getAll(Context context, List<PTSiteSetting> settings) {
		SharedPreferences prefs = getPrefs(context);
		int max = getMax(prefs);
		for (int i = 0; i < max; i++) {
			settings.add(get(prefs, i));
		}
	}

	public static HashMap<String, PTSiteSetting> getAsMap(Context context) {
		SharedPreferences prefs = getPrefs(context);
		int max = getMax(prefs);
		HashMap<String, PTSiteSetting> sites = new HashMap<String, PTSiteSetting>();
		PTSiteSetting setting;
		for (int i = 0; i < max; i++) {
			setting = get(prefs, i);
			sites.put(setting.type, setting);
		}
		return sites;
	}

	public static PTSiteSetting get(SharedPreferences prefs, int order) {
		PTSiteSetting setting = new PTSiteSetting();
		setting.order = order;
		setting.type = prefs.getString(SITE_TYPE + order, "");
		setting.username = prefs.getString(USERNAME + order, "");
		try {
			setting.password = DES.decryptDES(
					prefs.getString(PASSWORD + order, ""), Const.TAG);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setting.cookie = prefs.getString(COOKIE + order, "");
		return setting;
	}

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(Const.PT_SITE_PREFS,
				Context.MODE_PRIVATE);
	}

	public static int getMax(SharedPreferences prefs) {
		return prefs.getInt(MAX, 0);
	}

	public static void remove(Context context, int order) {
		SharedPreferences prefs = getPrefs(context);
		if (prefs.getString(SITE_TYPE + order, null) == null) {
			return;
		}
		Editor edit = prefs.edit();
		int max = getMax(prefs);
		for (int i = order; i < max; i++) {
			int j = i + 1;
			edit.putString(SITE_TYPE + i, prefs.getString(SITE_TYPE + j, null));
			edit.putString(USERNAME + i, prefs.getString(USERNAME + j, null));
			edit.putString(PASSWORD + i, prefs.getString(PASSWORD + j, null));
			edit.putString(COOKIE + i, prefs.getString(COOKIE + j, null));
		}
		edit.remove(SITE_TYPE + max);
		edit.remove(USERNAME + max);
		edit.remove(PASSWORD + max);
		edit.remove(COOKIE + max);

		edit.putInt(MAX, max - 1);

		edit.commit();
	}

	public static void save(Context context, PTSiteSetting setting) {
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		try {
			edit.putString(SITE_TYPE + setting.order, setting.type);
			edit.putString(USERNAME + setting.order, setting.username);
			edit.putString(PASSWORD + setting.order,
					DES.encryptDES(setting.password, Const.TAG));
			edit.putString(COOKIE + setting.order, setting.cookie);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void add(Context context, PTSiteSetting setting) {
		setting.order = getMax(getPrefs(context));
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		try {
			edit.putString(SITE_TYPE + setting.order, setting.type);
			edit.putString(USERNAME + setting.order, setting.username);
			edit.putString(PASSWORD + setting.order,
					DES.encryptDES(setting.password, Const.TAG));
			edit.putString(COOKIE + setting.order, setting.cookie);
			edit.putInt(MAX, setting.order + 1);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
