package org.hdstar.common;

import java.io.Serializable;

import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;

public class CustomSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String device = "Android�ͻ���";
	public static boolean loadImage = true;
	public static boolean soundOn = true;
	public static boolean autoRefresh = false;
	 public static String serverAddress = "http://hdstar.ap01.aws.af.cm/";

	public static boolean fade;// �������뵭��Ч��
	public static TransitionEffect anim = TransitionEffect.CubeIn;;// ����Ч��

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

}
