package org.hdstar.widget.navigation;

import java.util.List;

import org.hdstar.model.PTSiteSetting;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class PTFilterListDropDownAdapter extends PTFilterListAdapter {
	protected FilterListItemView navigationSelectionView;
	private PTSiteSetting currentTorrentPage;

	public PTFilterListDropDownAdapter(Context context,
			List<PTSiteSetting> settings) {
		super(context, settings);
		navigationSelectionView = FilterListItemView.build(context);
		currentTorrentPage = settings.get(0);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(currentTorrentPage.torrentPageName != null){
			navigationSelectionView.bind(currentTorrentPage.getSiteType().getName() + "|" + currentTorrentPage.torrentPageName );
		} else{
			navigationSelectionView.bind(currentTorrentPage.getSiteType().getName());
		}
		return navigationSelectionView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, convertView, parent);
	}
	
	public void updateTorrentPage(PTSiteSetting setting){
		currentTorrentPage = setting;
		if(currentTorrentPage.torrentPageName != null){
			navigationSelectionView.bind(currentTorrentPage.getSiteType().getName() + "|" + currentTorrentPage.torrentPageName );
		} else{
			navigationSelectionView.bind(currentTorrentPage.getSiteType().getName());
		}
	}

}
