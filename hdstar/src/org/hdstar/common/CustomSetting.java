package org.hdstar.common;

import java.io.Serializable;

import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;

public class CustomSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 回复时显示的设备名称 */
	public static String device = "Android客户端";
	/** 是否加载网络图片 */
	public static boolean loadImage = true;
	/** 是否开启声音 */
	public static boolean soundOn = true;
	/** 回复后是否自动刷新 */
	public static boolean autoRefresh = false;
	/** 应用服务器地址 */
	public static String serverAddress = "http://hdstar.ap01.aws.af.cm/";
	/** 是否开启代理模式 */
	public static boolean enableProxy = false;

	public static boolean fade;// 动画淡入淡出效果
	public static TransitionEffect anim = TransitionEffect.CubeIn;;// 动画效果

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
