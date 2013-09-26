package org.hdstar.component;

import java.io.File;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.util.IOUtils;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class HDStarApp extends Application {

	public static String cookies = null;
	public static DisplayImageOptions displayOptions;
	// private LruCache<String, Bitmap> mImageMemoryCache = null;
	// private DiskLruCache mDiskCache = null;
	// private Object mDiskCacheLock = new Object();
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;
	private static final int MEMORY_CACHE_SIZE = 1024 * 1024 * 4;
	// private static final int APP_VERSION = 1;
	// private static final int VALUE_COUNT = 1;
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	// private CompressFormat mCompressFormat = CompressFormat.PNG;// ͼƬѹ����ʽ
	// private static final int mCompressQuality = 70;// ͼƬѹ������

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate() {
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectAll().penaltyDialog().build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectAll().penaltyDeath().build());
		// }
		super.onCreate();

		init(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				context);
		builder.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.memoryCache(new LruMemoryCache(MEMORY_CACHE_SIZE))
				.memoryCacheSize(MEMORY_CACHE_SIZE)
				.memoryCacheSizePercentage(13);
		File discCache = getExternalStorageCacheDir(context, DISK_CACHE_SUBDIR);
		if (discCache != null) {
			builder.discCache(new UnlimitedDiscCache(discCache))
					.discCacheSize(DISK_CACHE_SIZE)
					.discCacheFileCount(100)
					.discCacheFileNameGenerator(new HashCodeFileNameGenerator());
		}
		ImageLoaderConfiguration config = builder.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		displayOptions = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.url_image_loading)
				.showImageForEmptyUri(R.drawable.url_image_loading)
				.showImageOnFail(R.drawable.url_image_failed).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(20))
				.build();
	}

	public static void init(Context context) {
		SharedPreferences shared = context.getSharedPreferences(Const.SHARED_PREFS,
				MODE_PRIVATE);
		cookies = shared.getString("cookies", null);
		CustomSetting.loadImage = shared.getBoolean("loadImage", true);
		CustomSetting.soundOn = shared.getBoolean("sound", true);
		initImageLoader(context);
		// initMemoryCache();
		// File cacheDir = getStorageCacheDir(this, DISK_CACHE_SUBDIR);
		// File externalStorageCacheDir = getExternalStorageCacheDir(this,
		// DISK_CACHE_SUBDIR);
		// // new InitDiskCacheTask().execute(externalStorageCacheDir,
		// cacheDir);
		// synchronized (mDiskCacheLock) {
		// // ���Դ�sd���д���
		// if (externalStorageCacheDir != null && mDiskCache == null) {
		// File cache = externalStorageCacheDir;
		// try {
		// // ����ļ���Ŀ¼�����ڣ��÷������Զ�����
		// mDiskCache = DiskLruCache.open(cache, APP_VERSION,
		// VALUE_COUNT, DISK_CACHE_SIZE);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// // ���ֻ��洢�д���
		// if (cacheDir != null && mDiskCache == null) {
		// File cache = cacheDir;
		// try {
		// // ����ļ���Ŀ¼�����ڣ��÷������Զ�����
		// mDiskCache = DiskLruCache.open(cache, APP_VERSION,
		// VALUE_COUNT, DISK_CACHE_SIZE);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
	}

	// ��ʼ���ڴ滺��
	// private void initMemoryCache() {
	// final int memClass = ((ActivityManager) this
	// .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
	// final int cacheSize = 1024 * 1024 * memClass / 8;
	// mImageMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
	// @Override
	// protected int sizeOf(String key, Bitmap bitmap) {
	// return bitmap.getRowBytes() * bitmap.getHeight();
	// }
	//
	// // �ڴ滺�治��ʱ���������ⲿ����
	// @Override
	// protected void entryRemoved(boolean evicted, String key,
	// Bitmap oldValue, Bitmap newValue) {
	// addBitmapToDiskCache(key, oldValue);
	// super.entryRemoved(evicted, key, oldValue, newValue);
	// }
	// };
	// }
	//
	// public LruCache<String, Bitmap> getMemCache() {
	// return mImageMemoryCache;
	// }
	//
	// // ���ڴ滺���в���
	// public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	// // synchronized (mImageMemoryCache) {
	// if (getBitmapFromMemCache(key) == null) {
	// mImageMemoryCache.put(key, bitmap);
	// }
	// // }
	// }
	//
	// // ���ڴ滺����ȡ��
	// public Bitmap getBitmapFromMemCache(String key) {
	// return mImageMemoryCache.get(key);
	// }
	//
	// /**
	// * ��bitmap��ӵ��ⲿ����
	// *
	// * @param key
	// * bitmap�ı�ʶ
	// * @param bitmap
	// * Ҫ�洢��Bitmap
	// * */
	// public void addBitmapToDiskCache(String key, Bitmap bitmap) {
	// /*
	// * if (getBitmapFromMemCache(key) == null) { mImageMemoryCache.put(key,
	// * bitmap); }
	// */
	// DiskLruCache.Editor editor = null;
	// try {
	// if (mDiskCache == null || (editor = mDiskCache.edit(key)) == null) {
	// return;
	// }
	// if (writeBitmapToFile(bitmap, editor)) {
	// mDiskCache.flush();
	// editor.commit();
	// } else {
	// editor.abort();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// try {
	// if (editor != null) {
	// editor.abort();
	// }
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	// }
	//
	// /**
	// * ���ⲿ������ȡ����
	// * */
	// public Bitmap getBitmapFromDiskCache(String key) {
	// if (mDiskCache == null) {
	// return null;
	// }
	// Bitmap bitmap = null;
	// DiskLruCache.Snapshot snapshot = null;
	// try {
	// snapshot = mDiskCache.get(key);
	// if (snapshot == null) {
	// return null;
	// }
	// final InputStream in = snapshot.getInputStream(0);
	// if (in != null) {
	// final BufferedInputStream buff = new BufferedInputStream(in,
	// IOUtils.IO_BUFFER_SIZE);
	// bitmap = BitmapFactory.decodeStream(buff);
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (IllegalStateException e) {
	// e.printStackTrace();
	// } finally {
	// if (snapshot != null) {
	// snapshot.close();
	// }
	// }
	// return bitmap;
	// }
	//
	// /**
	// * �ر��ⲿ����
	// * */
	// public void closeDiskCache() {
	// try {
	// mDiskCache.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor
	// editor)
	// throws FileNotFoundException, IOException {
	// OutputStream out = null;
	// try {
	// out = new BufferedOutputStream(editor.newOutputStream(0),
	// IOUtils.IO_BUFFER_SIZE);
	// return bitmap.compress(mCompressFormat, mCompressQuality, out);
	// } catch (IllegalStateException e) {
	// e.printStackTrace();
	// } finally {
	// if (out != null) {
	// out.close();
	// }
	// }
	// return false;
	// }
	//
	/**
	 * ��ȡSD������·��
	 * */
	public static File getExternalStorageCacheDir(Context context,
			String uniqueName) {
		File file = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)
				|| !IOUtils.isExternalStorageRemovable()) {
			String cachePath = IOUtils.getExternalCacheDir(context).getPath();
			file = new File(cachePath + File.separator + uniqueName);
		}
		return file;
	}

	/**
	 * �����ֻ��洢����·��
	 * */
	public static File getStorageCacheDir(Context context, String uniqueName) {
		File file = null;
		String cachePath = context.getCacheDir().getPath();
		file = new File(cachePath + File.separator + uniqueName);
		return file;
	}

	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)
				|| !IOUtils.isExternalStorageRemovable() ? IOUtils
				.getExternalCacheDir(context).getPath() : context.getCacheDir()
				.getPath();
		File file = new File(cachePath + File.separator + uniqueName);
		return file;
	}
	//
	// // ��ʼ���ⲿ����
	// class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	//
	// @Override
	// protected Void doInBackground(File... params) {
	// synchronized (mDiskCacheLock) {
	// // ���Դ�sd���д���
	// if (params[0] != null && mDiskCache == null) {
	// File cacheDir = params[0];
	// try {
	// // ����ļ���Ŀ¼�����ڣ��÷������Զ�����
	// mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION,
	// VALUE_COUNT, DISK_CACHE_SIZE);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// // ���ֻ��洢�д���
	// if (params[1] != null && mDiskCache == null) {
	// File cacheDir = params[1];
	// try {
	// // ����ļ���Ŀ¼�����ڣ��÷������Զ�����
	// mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION,
	// VALUE_COUNT, DISK_CACHE_SIZE);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return null;
	// }
	//
	// }
}
