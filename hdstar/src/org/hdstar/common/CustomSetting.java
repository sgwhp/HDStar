package org.hdstar.common;

import java.io.Serializable;

import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;

public class CustomSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 回复时显示的设备名称 */
	public static final String DEVICE_DEFAULT = "Android客户端";
	public static String device = DEVICE_DEFAULT;
	/** 是否加载网络图片 */
	public static final boolean LOAD_IMAGE_DEFAULT = true;
	public static boolean loadImage = LOAD_IMAGE_DEFAULT;
	/** 是否开启声音 */
	public static final boolean SOUND_ON_DEFAULT = true;
	public static boolean soundOn = SOUND_ON_DEFAULT;
	/** 回复后是否自动刷新 */
	public static final boolean AUTO_REFRESH_DEFAULT = false;
	public static boolean autoRefresh = AUTO_REFRESH_DEFAULT;
	/** 应用服务器地址 */
	public static final String SERVER_ADDRESS_DEFAULT = "http://hdstar.ap01.aws.af.cm/";
	public static String serverAddress = SERVER_ADDRESS_DEFAULT;
	/** 是否开启代理模式 */
	public static final boolean ENABLE_PROXY_DEFAULT = false;
	public static boolean enableProxy = ENABLE_PROXY_DEFAULT;

	public static boolean fade;// 动画淡入淡出效果
	public static TransitionEffect anim = TransitionEffect.Standard;// 动画效果

	public static void setDefault() {
		device = DEVICE_DEFAULT;
		loadImage = LOAD_IMAGE_DEFAULT;
		soundOn = SOUND_ON_DEFAULT;
		autoRefresh = AUTO_REFRESH_DEFAULT;
		serverAddress = SERVER_ADDRESS_DEFAULT;
		enableProxy = ENABLE_PROXY_DEFAULT;
	}

	public static String animToString() {
		return anim.name();
	}

	public static TransitionEffect stringToAnim(String name) {
		for (TransitionEffect effect : TransitionEffect.values()) {
			if (effect.name().equals(name)) {
				return effect;
			}
		}
		return TransitionEffect.Standard;
	}

	/**
	 * 获取当前的应用服务器地址<br/>
	 * 
	 * @return 如果 开启了代理，则返回代理服务器地址
	 */
	public static String getCurServerAddr() {
		if (!enableProxy) {
			return serverAddress;
		}
		return CommonUrls.HDStar.PROXY_SERVER_ADDR;
	}

	public static void setServerAddress(String addr) {
		serverAddress = addr;
	}

}
