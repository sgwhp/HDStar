package org.hdstar.component.activity;

import java.util.HashMap;

import org.hdstar.R;

import android.os.Bundle;

/**
 * ����
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
}
