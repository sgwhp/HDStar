package org.hdstar.component.activity;

import org.hdstar.R;
import org.hdstar.widget.fragment.MessageBoxFragment;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;

public class MessageActivity extends BaseStackActivity implements
		OnNavigationListener {
	public static final int DELETE_MENU_ITEM_ID = Menu.FIRST;
	private int curTab = -1;

	public MessageActivity() {
		super(R.string.message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			curTab = 0;
			stackAdapter.fragments.add(MessageBoxFragment.newInstance(0));
		} else {
			curTab = savedInstanceState.getInt("curForum");
		}
		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.boxType, R.layout.sherlock_spinner_item);
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
			stackAdapter.forward(MessageBoxFragment.newInstance(itemPosition));
		}
		return true;
	}
}