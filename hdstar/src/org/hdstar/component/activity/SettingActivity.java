package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.SettingFragment;

import android.os.Bundle;

/**
 * 设置
 * @author robust
 *
 */
public class SettingActivity extends BaseStackActivity {

	public SettingActivity() {
		super(R.string.setting);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			stackAdapter.add(new SettingFragment());
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}
}
