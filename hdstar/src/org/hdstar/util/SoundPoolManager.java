package org.hdstar.util;

import java.util.Calendar;
import java.util.Locale;

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
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		//23:00至次日8:00以及13:00至14:00不开启声音提示
		if (h > 23 || h < 8 || h == 13) {
			return;
		}
		int mode = ((AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
		if (mode == AudioManager.RINGER_MODE_NORMAL) {
			soundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}
}
