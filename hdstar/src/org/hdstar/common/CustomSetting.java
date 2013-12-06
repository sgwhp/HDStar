package org.hdstar.common;

import java.io.Serializable;

import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;

public class CustomSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String device = "Android客户端";
	public static boolean loadImage = true;
	public static boolean soundOn = true;
	public static boolean autoRefresh = false;
	public static String serverAddress = "http://hdsky.eu01.aws.af.cm/";
	// public static String serverAddress =
	// "http://10.10.28.113:8084/HDStarService/";

	public static boolean fade;// 动画淡入淡出效果
	public static TransitionEffect anim = TransitionEffect.CubeIn;;// 动画效果

	public static String animToString() {
		return anim.name();
		// switch (anim) {
		// case Tablet:
		// return "Tablet";
		// case CubeIn:
		// return "CubeIn";
		// case CubeOut:
		// return "CubeOut";
		// case FlipVertical:
		// return "FlipVertical";
		// case FlipHorizontal:
		// return "FlipHorizontal";
		// case Stack:
		// return "Stack";
		// case ZoomIn:
		// return "ZoomIn";
		// case ZoomOut:
		// return "ZoomOut";
		// case RotateUp:
		// return "RotateUp";
		// case RotateDown:
		// return "RotateDown";
		// case Accordion:
		// return "Accordion";
		// default:
		// return "Standard";
		// }
	}

	public static TransitionEffect stringToAnim(String name) {
		if ("Tablet".equals(name)) {
			return TransitionEffect.Tablet;
		}
		if ("CubeIn".equals(name)) {
			return TransitionEffect.CubeIn;
		}
		if ("CubeOut".equals(name)) {
			return TransitionEffect.CubeOut;
		}
		if ("FlipVertical".equals(name)) {
			return TransitionEffect.FlipVertical;
		}
		if ("FlipHorizontal".equals(name)) {
			return TransitionEffect.FlipHorizontal;
		}
		if ("Stack".equals(name)) {
			return TransitionEffect.Stack;
		}
		if ("ZoomIn".equals(name)) {
			return TransitionEffect.ZoomIn;
		}
		if ("ZoomOut".equals(name)) {
			return TransitionEffect.ZoomOut;
		}
		if ("RotateUp".equals(name)) {
			return TransitionEffect.RotateUp;
		}
		if ("RotateDown".equals(name)) {
			return TransitionEffect.RotateDown;
		}
		if ("Accordion".equals(name)) {
			return TransitionEffect.Accordion;
		}
		if ("GoEX".equals(name)) {
			return TransitionEffect.GoEX;
		}
		return TransitionEffect.Standard;
	}

}
