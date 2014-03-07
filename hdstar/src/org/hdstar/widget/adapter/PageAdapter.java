package org.hdstar.widget.adapter;

import org.hdstar.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * 论坛帖子页码列表适配器. <br/>
 * 
 * @author robust
 */
public class PageAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private int maxPage;
	private String pageIndex;

	public PageAdapter(Context context, int maxPage) {
		inflater = LayoutInflater.from(context);
		this.maxPage = maxPage;
		pageIndex = context.getString(R.string.pageIndex);
	}

	@Override
	public int getCount() {
		return maxPage;
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
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.page_row, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.page);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.text.setText(String.format(pageIndex, (position + 1)));
		return convertView;
	}

	private class ViewHolder {
		TextView text;
	}
}
