package org.hdstar.widget;

import java.util.List;

import org.hdstar.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PageAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	List<String> pageIdx = null;

	public PageAdapter(Context context, List<String> pageIdx){
		inflater = LayoutInflater.from(context);
		try{
			Integer.parseInt(pageIdx.get(pageIdx.size()-1));
		} catch(NumberFormatException e){
			pageIdx.remove(pageIdx.size()-1);
		}
		this.pageIdx = pageIdx;
	}
	
	@Override
	public int getCount() {
		return pageIdx.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.page_row, null);
			holder = new ViewHolder();
			holder.text = (TextView)convertView.findViewById(R.id.page);
			convertView.setTag(holder);
		} else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.text.setText(pageIdx.get(position));
		return convertView;
	}
	
	private class ViewHolder{
		TextView text;
	}
}