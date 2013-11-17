package org.hdstar.widget;

import java.util.ArrayList;

import org.hdstar.R;
import org.hdstar.model.RemoteTaskInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RemoteTaskAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private ArrayList<RemoteTaskInfo> list;
	private String taskInfo;
	
	public RemoteTaskAdapter(Context context, ArrayList<RemoteTaskInfo> list){
		inflater = LayoutInflater.from(context);
		this.list = list;
		taskInfo = context.getString(R.string.task_info);
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
		if(convertView == null){
			convertView = inflater.inflate(R.layout.remote_task_row, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RemoteTaskInfo item = list.get(position);
		holder.title.setText(item.title);
		holder.progress.setProgress(item.progress);
		holder.info.setText(String.format(taskInfo, item.size, item.ratio, item.dlSpeed, item.upSpeed));
		return convertView;
	}
	
	public ArrayList<RemoteTaskInfo> getList(){
		return list;
	}
	
	private static class ViewHolder{
		TextView title, info;
		ProgressBar progress;
		
		ViewHolder(View v){
			title = (TextView) v.findViewById(R.id.title);
			info = (TextView) v.findViewById(R.id.info);
			progress = (ProgressBar) v.findViewById(R.id.progress);
		}
	}

}
