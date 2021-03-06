package org.hdstar.component.activity;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.common.PTSiteSettingManager;
import org.hdstar.model.PTSiteSetting;
import org.hdstar.widget.fragment.TorrentListFragment;
import org.hdstar.widget.navigation.PTFilterListDropDownAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.umeng.analytics.MobclickAgent;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 种子
 * 
 * @author robust
 * 
 */
public class TorrentActivity extends BaseStackActivity implements
		OnNavigationListener {
	ArrayList<PTSiteSetting> settings;
	private int curTab = -1;
	private PTFilterListDropDownAdapter navigationSpinnerAdapter;

	public TorrentActivity() {
		super(R.string.torrent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = new ArrayList<PTSiteSetting>();
//		// 首先添加HDSky
//		PTSiteSetting hdsky = new PTSiteSetting();
//		hdsky.type = PTSiteType.HDSky.name();
//		hdsky.cookie = HDStarApp.cookies;
//		settings.add(hdsky);
        // 添加已保存设置的站点
        PTSiteSettingManager.getAll(this, settings);
        if (settings.size() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setIcon(R.drawable.icon)
                    .setMessage(R.string.no_remote_setting)
                    .setPositiveButton(R.string.add,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent intent = new Intent(TorrentActivity.this, SettingActivity.class);
                                    intent.putExtra("action", SettingActivity.ACTION_PT_SETTING);
                                    startActivity(intent);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).create().show();
            return;
        } else {
            if (savedInstanceState == null) {
                curTab = 0;
                stackAdapter.fragments.add(TorrentListFragment.newInstance(settings.get(0)));
                stackAdapter.notifyDataSetChanged();
            } else {
                curTab = savedInstanceState.getInt("curTab");
            }

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            // String[] pts = new String[settings.size()];
            // for (int i = settings.size() - 1; i >= 0; i--) {
            // pts[i] = settings.get(i).getSiteType().getName();
            // }
            // ArrayAdapter<CharSequence> list = new ArrayAdapter<CharSequence>(
            // getSupportActionBar().getThemedContext(),
            // R.layout.sherlock_spinner_item, pts);
            // list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
            navigationSpinnerAdapter = new PTFilterListDropDownAdapter(this,
                    settings);
            getSupportActionBar().setListNavigationCallbacks(
                    navigationSpinnerAdapter, this);
        }
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curTab", curTab);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == curTab) {
			return true;
		}
		PTSiteSetting setting = (PTSiteSetting) navigationSpinnerAdapter
				.getItem(itemPosition);
		// 未初始化
		if (setting.cookie == null || "".equals(setting.cookie)) {
			Crouton.makeText(this, R.string.unintialized, Style.CONFIRM);
			return true;
		}
		curTab = itemPosition;
		navigationSpinnerAdapter.updateTorrentPage(setting);
		stackAdapter.clear();
		stackAdapter.forward(TorrentListFragment.newInstance(setting));
        stackAdapter.notifyDataSetChanged();
		return true;
	}

}
