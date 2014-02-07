package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.CommonSettingFragment;

import android.os.Bundle;

/**
 * …Ë÷√
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
			stackAdapter.add(new CommonSettingFragment());
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}
}
