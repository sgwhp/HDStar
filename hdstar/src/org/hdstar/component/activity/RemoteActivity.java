package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.RemoteFragment;

import android.os.Bundle;

public class RemoteActivity extends BaseStackActivity {
	public RemoteActivity() {
		super(R.string.remote);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null){
			stackAdapter.fragments.add(RemoteFragment.newInstance());
		}
	}
}
