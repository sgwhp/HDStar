package org.hdstar.util;

import java.text.DecimalFormat;

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
}
