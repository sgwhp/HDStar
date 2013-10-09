package org.hdstar.widget;

import java.util.ArrayList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.model.Message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter implements
		OnCheckedChangeListener {
	private LayoutInflater inflater;
	private List<MessageWithCheck> list = new ArrayList<MessageWithCheck>();
	private int selectedCount = 0;
	private int boxType;

	public MessageAdapter(Context context, List<Message> list, int boxType) {
		inflater = LayoutInflater.from(context);
		for (Message msg : list) {
			this.list.add(new MessageWithCheck(msg));
		}
		this.boxType = boxType;
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
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
			convertView = inflater.inflate(R.layout.message_item, null);
			holder = new ViewHolder(convertView);
			holder.check.setOnCheckedChangeListener(this);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MessageWithCheck msg = list.get(position);
		holder.check.setTag(position);
		holder.subject.setText(msg.msg.subject);
		holder.sender.setText(msg.msg.sender);
		if (boxType == 2) {
			holder.replyer.setVisibility(View.VISIBLE);
			holder.replyer.setText(msg.msg.replyer);
		}
		holder.time.setText(msg.msg.time);
		holder.check.setChecked(msg.check);
		if (msg.msg.read) {
			convertView.setBackgroundResource(R.color.read_bg);
			holder.subject.getPaint().setFakeBoldText(false);
		} else {
			convertView.setBackgroundResource(R.color.unread_bg);
			holder.subject.getPaint().setFakeBoldText(true);
		}
		return convertView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		list.get((Integer) buttonView.getTag()).check = isChecked;
		if (isChecked) {
			selectedCount++;
		} else {
			selectedCount--;
		}
	}

	public void setList(List<Message> list) {
		this.list.clear();
		if (list == null) {
			return;
		}
		for (Message msg : list) {
			this.list.add(new MessageWithCheck(msg));
		}
	}

	public List<MessageWithCheck> getList() {
		return list;
	}

	public int getSelectedCount() {
		return selectedCount;
	}

	private static class ViewHolder {
		TextView subject;
		TextView sender;
		TextView replyer;
		TextView time;
		CheckBox check;

		ViewHolder(View v) {
			subject = (TextView) v.findViewById(R.id.messageSubject);
			sender = (TextView) v.findViewById(R.id.sender);
			replyer = (TextView) v.findViewById(R.id.replyer);
			time = (TextView) v.findViewById(R.id.time);
			check = (CheckBox) v.findViewById(R.id.checkBox);
		}
	}

	private static class MessageWithCheck {
		Message msg;
		boolean check;

		MessageWithCheck(Message msg) {
			this.msg = msg;
		}
	}
}
