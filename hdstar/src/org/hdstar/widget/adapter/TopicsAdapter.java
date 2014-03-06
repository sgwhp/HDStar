package org.hdstar.widget.adapter;

import java.util.List;

import org.hdstar.R;
import org.hdstar.model.Topic;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TopicsAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Topic> items;

	public TopicsAdapter(Context context, List<Topic> items) {
		inflater = LayoutInflater.from(context);
		this.items = items;
	}

	public List<Topic> getList() {
		return items;
	}

	public void setList(List<Topic> topics) {
		items.clear();
		items.addAll(topics);
	}

	public void itemsAddAll(List<Topic> items) {
		this.items.addAll(items);
	}

	public void clearItems() {
		items.clear();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup par) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.topic_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.author = (TextView) convertView.findViewById(R.id.author);
			holder.author.setTextColor(Color.BLACK);
			holder.follow = (TextView) convertView.findViewById(R.id.follow);
			holder.follow.setTextColor(Color.BLACK);
			holder.last_reply = (TextView) convertView
					.findViewById(R.id.last_reply);
			holder.last_reply.setTextColor(Color.BLACK);
			holder.tag = (ImageView) convertView.findViewById(R.id.tag);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Topic t = items.get(position);
		holder.title.setText(t.title);
		holder.author.setText(t.author);
		holder.follow.setText(t.follow);
		holder.last_reply.setText(t.lastPost);
		if (t.read) {
			if (t.locked)
				holder.tag.setImageResource(R.drawable.locked2);
			else if (t.sticky)
				holder.tag.setImageResource(R.drawable.sticky2);
			else
				holder.tag.setImageResource(R.drawable.read);
		} else {
			if (t.locked)
				holder.tag.setImageResource(R.drawable.locked);
			else if (t.sticky)
				holder.tag.setImageResource(R.drawable.sticky);
			else
				holder.tag.setImageResource(R.drawable.unread);
		}
		return convertView;
	}

	private class ViewHolder {
		TextView title, author, follow, last_reply;
		ImageView tag;
	}

}
