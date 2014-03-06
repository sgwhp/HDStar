package org.hdstar.widget.navigation;

import java.util.List;

import org.hdstar.model.PTSiteSetting;
import org.hdstar.widget.adapter.MergeAdapter;

import android.content.Context;

/**
 * pt站选择列表适配器
 * @author robust
 *
 */
public class PTFilterListAdapter extends MergeAdapter {
	
	public PTFilterListAdapter(Context context, List<PTSiteSetting> settings){
		for(PTSiteSetting setting : settings){
			addAdapter(new PTFilterListItemAdapter(context, setting));
		}
	}

}
