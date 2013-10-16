package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.TorrentListFragment;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;

public class TorrentActivity extends BaseStackActivity {
	public static final int COMMIT_ACTION_BAR_ID = Menu.FIRST;
	private int curTab = -1;

	public TorrentActivity() {
		super(R.string.torrent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			curTab = 0;
			stackAdapter.fragments.add(TorrentListFragment.newInstance());
		} else {
			curTab = savedInstanceState.getInt("curTab");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curTab", curTab);
	}

}
