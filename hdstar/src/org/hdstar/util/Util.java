package org.hdstar.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class Util {
	public static String formatFileSize(long size) {
		final DecimalFormat numFormat = new DecimalFormat("0.##");
		if (size < 820) {
			return size + "B";
		} else if (size < 838860) {
			return numFormat.format(size / 1024.0) + "KB";
		} else if (size < 858993459L) {
			return numFormat.format(size / (1024 * 1024.0)) + "MB";
		} else {
			return numFormat.format(size / (1024 * 1024 * 1024.0)) + "GB";
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
}
