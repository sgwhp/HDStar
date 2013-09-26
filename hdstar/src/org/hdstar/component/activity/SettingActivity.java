package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;
import org.hdstar.util.CustomHttpClient;
import org.hdstar.util.SoundPoolManager;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class SettingActivity extends BaseActivity {
	CheckBox fetchImage;
	CheckBox sound;

	public SettingActivity(){
		super(R.string.setting);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		fetchImage = (CheckBox) findViewById(R.id.fetchImage);
		sound = (CheckBox) findViewById(R.id.sound);
		fetchImage.setChecked(CustomSetting.loadImage);
		sound.setChecked(CustomSetting.soundOn);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Editor edit = getSharedPreferences(Const.SHARED_PREFS, MODE_PRIVATE).edit();
		CustomSetting.loadImage = fetchImage.isChecked();
		edit.putBoolean("loadImage", CustomSetting.loadImage);
		CustomSetting.soundOn = sound.isChecked();
		if(!CustomSetting.soundOn){
			SoundPoolManager.clear();
		}
		edit.putBoolean("sound", CustomSetting.soundOn);
		edit.commit();
	}
	
	public void onClick(View v){
		switch(v.getId()){
		case R.id.logOut:
			CustomHttpClient.restClient();
			Editor edit = getSharedPreferences(Const.SHARED_PREFS, MODE_PRIVATE).edit();
			edit.remove("cookies");
			edit.commit();
			HDStarApp.cookies = null;
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.clearCache:
			ImageLoader.getInstance().clearDiscCache();
			break;
		}
	}
}