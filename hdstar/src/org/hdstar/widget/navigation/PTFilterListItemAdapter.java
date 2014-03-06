package org.hdstar.widget.navigation;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.common.PTSiteType;
import org.hdstar.common.TorrentPageUrl;
import org.hdstar.model.PTSiteSetting;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PTFilterListItemAdapter extends BaseAdapter {
	private final Context context;
	private List<PTSiteSetting> items = new ArrayList<PTSiteSetting>();
	protected LargeFilterSeparatorView torrentPageSeparator;

	public PTFilterListItemAdapter(Context context, PTSiteSetting setting) {
		this.context = context;
		items.add(setting);
		PTSiteType type = setting.getSiteType();
		TorrentPageUrl[] torrentUrls = type.getTorrentPages();
		setting.torrentUrl = torrentUrls[0].url;
		if (torrentUrls.length > 1) {
			for (TorrentPageUrl torrentUrl : torrentUrls) {
				items.add(setting.copy(torrentUrl.url,
						context.getString(torrentUrl.nameResId)));
			}
		}
		torrentPageSeparator = LargeFilterSeparatorView.build(context).setText(
				setting.getSiteType().getName());
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public PTSiteSetting getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			// 第一项增加分隔线
			return torrentPageSeparator;
		}
		FilterListItemView filterItemView;
		if (convertView == null || !(convertView instanceof SimpleListItemView)) {
			filterItemView = FilterListItemView.build(context);
		} else {
			filterItemView = (FilterListItemView) convertView;
		}
		filterItemView.bind(getItem(position));
		return filterItemView;
	}

}
