package org.hdstar.component;

import java.io.File;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.model.ResponseWrapper;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.DelegateTask;
import org.hdstar.util.IOUtils;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.google.gson.reflect.TypeToken;
import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;
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
	public static boolean hasNewMessage = false;
	public static DisplayImageOptions roundedDisplayOptions;
	public static DisplayImageOptions displayOptions;
	// private LruCache<String, Bitmap> mImageMemoryCache = null;
	// private DiskLruCache mDiskCache = null;
	// private Object mDiskCacheLock = new Object();
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;
	private static final int MEMORY_CACHE_SIZE = 1024 * 1024 * 4;
	// private static final int APP_VERSION = 1;
	// private static final int VALUE_COUNT = 1;
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	// private CompressFormat mCompressFormat = CompressFormat.PNG;// 图片压缩格式
	// private static final int mCompressQuality = 70;// 图片压缩质量

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate() {
		super.onCreate();

		init(getApplicationContext());
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ImageLoader.getInstance().clearMemoryCache();
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
				.showImageOnLoading(R.drawable.url_image_loading)
				.showImageForEmptyUri(R.drawable.url_image_loading)
				.showImageOnFail(R.drawable.url_image_failed)
				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
				.build();
		roundedDisplayOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.url_image_loading)
				.showImageForEmptyUri(R.drawable.url_image_loading)
				.showImageOnFail(R.drawable.url_image_failed)
				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
	}

	public static void init(Context context) {
		SharedPreferences shared = context.getSharedPreferences(
				Const.SETTING_SHARED_PREFS, MODE_PRIVATE);
		cookies = shared.getString("cookies", null);
		CustomSetting.loadImage = shared.getBoolean("loadImage", true);
		CustomSetting.soundOn = shared.getBoolean("sound", true);
		CustomSetting.device = shared.getString("device", CustomSetting.device);
		CustomSetting.autoRefresh = shared.getBoolean("autoRefresh", false);
		CustomSetting.serverAddress = shared.getString("serverAddr",
				CustomSetting.serverAddress);
		CustomSetting.fade = shared.getBoolean("fade", false);
		CustomSetting.anim = CustomSetting.stringToAnim(shared.getString(
				"anim", TransitionEffect.CubeIn.name()));
		CommonUrls.HDStar.initServerAddr(CustomSetting.serverAddress);
		initImageLoader(context);
		// initMemoryCache();
		// File cacheDir = getStorageCacheDir(this, DISK_CACHE_SUBDIR);
		// File externalStorageCacheDir = getExternalStorageCacheDir(this,
		// DISK_CACHE_SUBDIR);
		// // new InitDiskCacheTask().execute(externalStorageCacheDir,
		// cacheDir);
		// synchronized (mDiskCacheLock) {
		// // 尝试从sd卡中创建
		// if (externalStorageCacheDir != null && mDiskCache == null) {
		// File cache = externalStorageCacheDir;
		// try {
		// // 如果文件或目录不存在，该方法会自动创建
		// mDiskCache = DiskLruCache.open(cache, APP_VERSION,
		// VALUE_COUNT, DISK_CACHE_SIZE);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// // 在手机存储中创建
		// if (cacheDir != null && mDiskCache == null) {
		// File cache = cacheDir;
		// try {
		// // 如果文件或目录不存在，该方法会自动创建
		// mDiskCache = DiskLruCache.open(cache, APP_VERSION,
		// VALUE_COUNT, DISK_CACHE_SIZE);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
	}

	/**
	 * 查看是否有未读消息
	 */
	public void checkMessage() {
		final DelegateTask<Boolean> task = DelegateTask.newInstance(cookies);
		task.attach(new TaskCallback<Boolean>() {

			@Override
			public void onComplete(Boolean result) {
				task.detach();
				if (!result) {
					return;
				}
				hasNewMessage = true;
				Intent intent = new Intent(Const.NEW_MESSAGE_ACTION);
				intent.putExtra("req", Const.NEW_MESSAGE_REQ_REFRESH);
				HDStarApp.this.sendBroadcast(intent);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Intent nIntent = new Intent(Const.NEW_MESSAGE_ACTION);
				nIntent.putExtra("req", Const.NEW_MESSAGE_REQ_VIEW);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						getApplicationContext(), 0, nIntent, 0);

				Notification notification = new NotificationCompat.Builder(
						HDStarApp.this).setSmallIcon(R.drawable.ic_launcher)
						.setTicker(getText(R.string.have_new_message))
						.setContentTitle(getText(R.string.notification_title))
						.setContentText(getText(R.string.have_new_message))
						.setContentIntent(pendingIntent).build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(0, notification);
			}

			@Override
			public void onCancel() {
				task.detach();
			}

			@Override
			public void onFail(Integer msgId) {
				task.detach();
			}
		});
		task.execGet(CommonUrls.HDStar.SERVER_CHECK_MESSAGE_URL,
				new TypeToken<ResponseWrapper<Boolean>>() {
				}.getType());
	}

	// 初始化内存缓存
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
	// // 内存缓存不足时，可移至外部缓存
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
	// // 往内存缓存中插入
	// public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	// // synchronized (mImageMemoryCache) {
	// if (getBitmapFromMemCache(key) == null) {
	// mImageMemoryCache.put(key, bitmap);
	// }
	// // }
	// }
	//
	// // 从内存缓存中取出
	// public Bitmap getBitmapFromMemCache(String key) {
	// return mImageMemoryCache.get(key);
	// }
	//
	// /**
	// * 将bitmap添加到外部缓存
	// *
	// * @param key
	// * bitmap的标识
	// * @param bitmap
	// * 要存储的Bitmap
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
	// * 从外部缓存中取数据
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
	// * 关闭外部缓存
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
	 * 获取SD卡缓存路径
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
	 * 缓存手机存储缓存路径
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
	// // 初始化外部缓存
	// class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	//
	// @Override
	// protected Void doInBackground(File... params) {
	// synchronized (mDiskCacheLock) {
	// // 尝试从sd卡中创建
	// if (params[0] != null && mDiskCache == null) {
	// File cacheDir = params[0];
	// try {
	// // 如果文件或目录不存在，该方法会自动创建
	// mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION,
	// VALUE_COUNT, DISK_CACHE_SIZE);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// // 在手机存储中创建
	// if (params[1] != null && mDiskCache == null) {
	// File cacheDir = params[1];
	// try {
	// // 如果文件或目录不存在，该方法会自动创建
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
