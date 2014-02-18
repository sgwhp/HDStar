package org.hdstar.component.activity;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.common.PTSiteType;
import org.hdstar.model.PTSiteSetting;
import org.hdstar.widget.fragment.TorrentListFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;

/**
 * 种子
 * 
 * @author robust
 * 
 */
public class TorrentActivity extends BaseStackActivity implements
		OnNavigationListener {
	public static final int COMMIT_ACTION_BAR_ID = Menu.FIRST;
	ArrayList<PTSiteSetting> settings;
	private int curTab = -1;

	public TorrentActivity() {
		super(R.string.torrent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = new ArrayList<PTSiteSetting>();
		// 首先添加HDSky
		PTSiteSetting hdsky = new PTSiteSetting();
		hdsky.type = PTSiteType.HDSky.name();
		hdsky.cookie = getSharedPreferences(Const.SETTING_SHARED_PREFS,
				MODE_PRIVATE).getString("cookies", "");
		settings.add(hdsky);
		// 添加已初始化的站点
		PTSiteSettingManager.getAll(this, settings);

		if (savedInstanceState == null) {
			curTab = 0;
			stackAdapter.fragments.add(TorrentListFragment.newInstance(hdsky));
		} else {
			curTab = savedInstanceState.getInt("curTab");
		}

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		String[] pts = new String[settings.size()];
		for (int i = settings.size() - 1; i >= 0; i--) {
			pts[i] = settings.get(i).type;
		}
		ArrayAdapter<CharSequence> list = new ArrayAdapter<CharSequence>(
				getSupportActionBar().getThemedContext(),
				R.layout.sherlock_spinner_item, pts);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setListNavigationCallbacks(list, this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curTab", curTab);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition != curTab) {
			curTab = itemPosition;
			stackAdapter.clear();
			stackAdapter.forward(TorrentListFragment.newInstance(settings
					.get(itemPosition)));
		}
		return true;
	}

}
