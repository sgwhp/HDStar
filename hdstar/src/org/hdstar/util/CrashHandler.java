package org.hdstar.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 
 * UncaughtExceptionHandler：线程未捕获异常控制器是用来处理未捕获异常的。 如果程序出现了未捕获异常默认情况下则会出现强行关闭对话框
 * 实现该接口并注册为程序中的默认未捕获异常处理 这样当未捕获异常发生时，就可以做些异常处理操作 例如：收集异常信息，发送错误报告 等。
 * 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {

	/** Debug Log Tag */
	public static final String TAG = "CrashHandler";

	/** 是否开启日志输出, 在Debug状态下开启, 在Release状态下关闭以提升程序性能 */
	public static final boolean DEBUG = true;

	/** CrashHandler实例 */
	private static volatile CrashHandler instance;

	/** 程序的Context对象 */
	private Context mContext;

	/** 系统默认的UncaughtException处理类 */
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (instance == null) {
			synchronized (CrashHandler.class) {
				if (instance == null) {
					instance = new CrashHandler();
				}
			}
		}
		return instance;
	}

	/**
	 * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
	 * 
	 * @param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			final String msg = ex.getLocalizedMessage();
			// 使用Toast来显示异常信息
			new Thread() {
				@Override
				public void run() {
					// Toast 显示需要出现在一个线程的消息队列中
					Looper.prepare();
					Toast.makeText(mContext, "程序出错啦:" + msg, Toast.LENGTH_LONG)
							.show();
					Looper.loop();
				}
			}.start();

			// Sleep一会后结束程序
			// 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error : ", e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(10);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		} else {
			// logCat warning
			Log.w(TAG, "handleException", ex);

			// 写入错误信息到本地文件
			processException(ex);
		}

		return true;

	}

	/**
	 * 处理Exception(记录日志). <br/>
	 */
	public void processException(final Throwable th) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(
					HDStarApp.getExternalStorageCacheDir(mContext,
							Const.LOG_FILE), true));
			writer.append(SDF.format(new Date()));
			writer.append(":\n");
			th.printStackTrace(writer);
			writer.append("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
