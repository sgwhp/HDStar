package org.hdstar.util;

import org.hdstar.R;
import org.hdstar.common.CustomSetting;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPoolManager {
	static int soundID;
	static SoundPool soundPool = null;

	public static void load(Context context) {
		if (CustomSetting.soundOn && soundPool == null) {
			soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
			soundID = soundPool.load(context, R.raw.loaded, 1);
		}
	}

	public static void clear() {
		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
		}
	}

	public static void play(Context context) {
		if (soundPool == null) {
			return;
		}
		int mode = ((AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
		if (mode == AudioManager.RINGER_MODE_NORMAL) {
			soundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}
}
