package org.hdstar.util;

import java.text.DecimalFormat;

import org.hdstar.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;

public class Util {
	public static final double KB_TO_B = 1024.0;
	public static final double MB_TO_B = KB_TO_B * 1024;
	public static final double GB_TO_B = MB_TO_B * 1024;
	// 此处数据长度已经超过整型限制
	public static final double TB_TO_B = GB_TO_B * 1024;

	public static CharSequence formatFileSize(long size) {
		final DecimalFormat numFormat = new DecimalFormat("0.##");
		if (size < 820) {
			return size + "B";
		} else if (size < 838860) {
			return numFormat.format(size / KB_TO_B) + "KB";
		} else if (size < 858993459L) {
			return numFormat.format(size / MB_TO_B) + "MB";
		} else if (size < 879609302220L) {
			return numFormat.format(size / GB_TO_B) + "GB";
		} else {
			return numFormat.format(size / TB_TO_B) + "TB";
		}
	}

	/**
	 * 获取app当前版本号
	 * 
	 * @param context
	 * @return 当前版本号
	 */
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

	public static boolean isIp(String str) {
		return str
				.matches("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:[\\d]{1,5})?$");
	}

	/**
	 * 显示退出提示框
	 * 
	 * @param context
	 */
	public static void showExitDialog(final Activity context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.confirm)
				.setIcon(R.drawable.ic_launcher)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// InitActivity.exitApp(BaseStackActivity.this);
								((NotificationManager) context
										.getSystemService(Context.NOTIFICATION_SERVICE))
										.cancelAll();
								context.finish();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create().show();
	}
}
