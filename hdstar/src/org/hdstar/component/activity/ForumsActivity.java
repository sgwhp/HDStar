package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.widget.fragment.ForumFragment;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

/**
 * ÂÛÌ³
 * 
 * @author robust
 * 
 */
public class ForumsActivity extends BaseStackActivity implements
		OnNavigationListener {
	private int curTab = -1;

	public ForumsActivity() {
		super(R.string.forums);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			curTab = 0;
			stackAdapter.fragments.add(ForumFragment
					.newInstance(CommonUrls.HDStar.CHAT_ROOM_URL));
		} else {
			curTab = savedInstanceState.getInt("curForum");
		}

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.forums, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curForum", curTab);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition != curTab) {
			curTab = itemPosition;
			stackAdapter.clear();
			stackAdapter.forward(ForumFragment
					.newInstance(CommonUrls.HDStar.FORUM_URLS[itemPosition]));
		}
		return true;
	}
}
