package org.hdstar.widget;

import org.hdstar.common.Const;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SmilesAdapter extends BaseAdapter {
	private Context context;

	public SmilesAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return Const.DEFAULT_SMILEY_RES_IDS.length;
	}

	@Override
	public Object getItem(int arg0) {
		return Const.links[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		ImageView image = new ImageView(context);
		image.setImageResource(Const.DEFAULT_SMILEY_RES_IDS[position]);
		return image;
	}

	public int getSmile(int position) {
		return Const.DEFAULT_SMILEY_RES_IDS[position];
	}

}
