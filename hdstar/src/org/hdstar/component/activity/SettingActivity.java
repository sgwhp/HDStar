package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.PTListFragment;
import org.hdstar.widget.fragment.RemoteSettingFragment;
import org.hdstar.widget.fragment.SettingFragment;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;

/**
 * 设置
 * @author robust
 *
 */
public class SettingActivity extends BaseStackActivity {
	public static final int ACTION_ADD_REMOTE_SETTING = 1;
	public static final int ACTION_PT_SETTING = 2;

	public SettingActivity() {
		super(R.string.setting);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			stackAdapter.add(new SettingFragment());
			int action = getIntent().getIntExtra("action", 0);
			if(action == 0){
				return;
			}
			switch(action){
			case ACTION_ADD_REMOTE_SETTING:
				viewPager.post(new Runnable(){

					@Override
					public void run() {
						stackAdapter.add(RemoteSettingFragment.newInstance(
								RemoteSettingFragment.MODE_ADD, null));
						viewPager.setCurrentItem(1);
					}});
				break;
			case ACTION_PT_SETTING:
				viewPager.post(new Runnable(){

					@Override
					public void run() {
						stackAdapter.add(new PTListFragment());
						viewPager.setCurrentItem(1, true);
					}});
				break;
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}
}
