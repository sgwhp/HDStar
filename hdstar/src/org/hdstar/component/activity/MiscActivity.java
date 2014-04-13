package org.hdstar.component.activity;

import java.util.HashMap;

import org.hdstar.R;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;

/**
 * 其他
 * @author robust
 *
 */
public class MiscActivity extends BaseActivity {
	HashMap<String, Integer> map = new HashMap<String, Integer>();

	public MiscActivity() {
		super(R.string.misc);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
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
}
