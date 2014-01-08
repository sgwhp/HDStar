package org.hdstar.common;

import java.util.ArrayList;

import org.hdstar.model.RssSetting;
import org.hdstar.util.DES;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RssSettingManager {
	public static final String MAX = "max";
	public static final String LABEL = "label";
	public static final String LINK = "link";

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(Const.RSS_SHARED_PREFS,
				Context.MODE_PRIVATE);
	}

	public static int getMaxRss(SharedPreferences prefs) {
		return prefs.getInt(MAX, 0);
	}

	public static RssSetting get(SharedPreferences prefs, int order) {
		RssSetting setting = new RssSetting();
		try {
			setting.order = order;
			setting.label = prefs.getString(LABEL + order, "");
			setting.link = DES.decryptDES(prefs.getString(LINK + order, ""),
					Const.TAG);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return setting;
	}

	public static ArrayList<RssSetting> getAll(Context context) {
		SharedPreferences prefs = getPrefs(context);
		int max = getMaxRss(prefs);
		ArrayList<RssSetting> list = new ArrayList<RssSetting>();
		for (int i = 0; i < max; i++) {
			list.add(get(prefs, i));
		}
		return list;
	}

	public static void removeRssSettings(SharedPreferences prefs, int order) {
		if (prefs.getString(LABEL + order, null) == null)
			return; // The settings that were requested to be removed do not
					// exist

		// Copy all settings higher than the supplied order number to the
		// previous spot
		Editor edit = prefs.edit();
		int max = getMaxRss(prefs);
		for (int i = order; i < max; i++) {
			int j = i + 1;
			edit.putString(LABEL + i, prefs.getString(LABEL + j, null));
			edit.putString(LINK + i, prefs.getString(LINK + j, null));
		}

		// Remove the last settings, of which we are now sure are no longer
		// required
		edit.remove(LABEL + max);
		edit.remove(LINK + max);

		edit.putInt(MAX, max - 1);
		edit.commit();
	}

	public static void removeRssSettings(Context context, int order) {
		removeRssSettings(getPrefs(context), order);
	}

	public static void save(Context context, RssSetting setting) {
		try {
			SharedPreferences prefs = getPrefs(context);
			Editor edit = prefs.edit();
			edit.putString(LABEL + setting.order, setting.label);
			edit.putString(LINK + setting.order,
					DES.encryptDES(setting.link, Const.TAG));
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void add(Context context, RssSetting setting) {
		try {
			SharedPreferences prefs = getPrefs(context);
			Editor edit = prefs.edit();
			edit.putString(LABEL + setting.order, setting.label);
			edit.putString(LINK + setting.order,
					DES.encryptDES(setting.link, Const.TAG));
			edit.putInt(MAX, setting.order + 1);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
