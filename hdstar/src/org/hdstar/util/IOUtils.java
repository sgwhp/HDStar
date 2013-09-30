package org.hdstar.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

public class IOUtils {
	public static final int IO_BUFFER_SIZE = 8 * 1024;

	private IOUtils() {
	};

	@SuppressLint("NewApi")
	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	@SuppressLint("NewApi")
	public static File getExternalCacheDir(Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache";
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ cacheDir);
	}

	public static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static void closeInputStreamIgnoreExceptions(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void closeFileIgnoreExceptions(RandomAccessFile file) {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getExternalDownloadDir() {
		return Environment.getExternalStorageDirectory().getPath()
				+ "/hdstar/download";
	}

	/**
	 * 根据目录和文件名，自动选择一个可用的名称，遇到重复命名的文件，自动在文件名后加上(number)
	 * 
	 * @param parent
	 *            需要创建文件所在目录
	 * @param fileName
	 *            需要创建的文件名
	 * @return 新的文件名
	 * */
	public static String autoRenameFile(String parent, String fileName) {
		File dir = new File(parent);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		int endPoint = fileName.lastIndexOf('.');
		if (endPoint == -1) {
			endPoint = fileName.length();
		}
		String suffix = fileName.substring(endPoint, fileName.length());
		String name = fileName.substring(0, endPoint);
		File file = new File(parent + File.separator + name + suffix);
		int count = 0;
		while (file.exists()) {
			count++;
			name = name + "(" + count + ")";
			file = new File(parent + File.separator + name + suffix);
		}
		return name + suffix;
	}

	public static String inputStream2String(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder();
		String str;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while ((str = reader.readLine()) != null) {
			builder.append(str);
		}
		return builder.toString();
	}
}
