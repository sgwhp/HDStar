package org.hdstar.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class Util {
	public static final long KB_TO_B = 1024;
	public static final long MB_TO_B = 1024 * 1024;
	public static final long GB_TO_B = 1024 * 1024 * 1024;
	public static final long TB_TO_B = 1024 * 1024 * 1024 * 1024;

	public static String formatFileSize(long size) {
		final DecimalFormat numFormat = new DecimalFormat("0.##");
		if (size < 820) {
			return size + "B";
		} else if (size < 838860) {
			return numFormat.format(size * 1.0 / KB_TO_B) + "KB";
		} else if (size < 858993459L) {
			return numFormat.format(size * 1.0 / MB_TO_B) + "MB";
		} else if (size < 879609302220L) {
			return numFormat.format(size * 1.0 / GB_TO_B) + "GB";
		} else {
			return numFormat.format(size * 1.0 / TB_TO_B) + "TB";
		}
	}

	public static int getVersionCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static boolean isIp(String str){
		return str.matches("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:[\\d]{1,5})?$");
	}
}
