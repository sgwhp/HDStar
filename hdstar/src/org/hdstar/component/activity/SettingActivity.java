package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.RemoteSettingFragment;
import org.hdstar.widget.fragment.SettingFragment;

import android.os.Bundle;

/**
 * 设置
 * @author robust
 *
 */
public class SettingActivity extends BaseStackActivity {
	public static final int ACTION_ADD_REMOTE_SETTING = 1;

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
				stackAdapter.add(RemoteSettingFragment.newInstance(
						RemoteSettingFragment.MODE_ADD, null));
				viewPager.setCurrentItem(1);
				break;
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}
}
