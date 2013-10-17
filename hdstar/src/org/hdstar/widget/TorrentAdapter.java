package org.hdstar.widget;

import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.Torrent;
import org.hdstar.task.BaseAsyncTask.TaskCallback;
import org.hdstar.task.OriginTask;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TorrentAdapter extends BaseExpandableListAdapter {
	// private Context context;
	private LayoutInflater inflater;
	private List<Torrent> torrents;
	private Drawable bookmarked;
	private Drawable unbookmark;

	public TorrentAdapter(Context context, List<Torrent> torrents) {
		// this.context = context;
		inflater = LayoutInflater.from(context);
		this.torrents = torrents;
		bookmarked = context.getResources().getDrawable(
				R.drawable.bookmarked_btn_bg_sel);
		bookmarked.setBounds(0, 0, bookmarked.getIntrinsicWidth(),
				bookmarked.getIntrinsicHeight());
		unbookmark = context.getResources().getDrawable(
				R.drawable.unbookmark_btn_bg_sel);
		unbookmark.setBounds(0, 0, unbookmark.getIntrinsicWidth(),
				unbookmark.getIntrinsicHeight());
	}

	@Override
	public int getGroupCount() {
		return torrents.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.torrent_row_group, null);
			holder = new GroupHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}
		final Torrent t = torrents.get(groupPosition);
		if (isExpanded) {
			holder.expand.setImageResource(R.drawable.arrow_expand);
		} else {
			holder.expand.setImageResource(R.drawable.arrow_collapse);
		}
		if (t.sticky) {
			holder.sticky.setVisibility(View.VISIBLE);
		} else {
			holder.sticky.setVisibility(View.INVISIBLE);
		}
		holder.title.setText(t.title);
		holder.subtitle.setText(t.subtitle);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.torrent_row_child, null);
			holder = new ChildHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		final Torrent t = torrents.get(groupPosition);
		holder.comments.setText(t.comments + "");
		holder.time.setText(t.time);
		holder.size.setText(t.size);
		if (t.bookmark) {
			holder.bookmark.setCompoundDrawables(null, bookmarked, null, null);
			holder.bookmark.setText(R.string.unbookmark);
		} else {
			holder.bookmark.setCompoundDrawables(null, unbookmark, null, null);
			holder.bookmark.setText(R.string.bookmark);
		}
		holder.bookmark.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final OriginTask<Void> task = OriginTask
						.newInstance(HDStarApp.cookies);
				task.attach(new TaskCallback<Void>() {

					@Override
					public void onComplete(Void result) {
						task.detach();
						t.bookmark = !t.bookmark;
						notifyDataSetChanged();
					}

					@Override
					public void onCancel() {
						task.detach();
					}

					@Override
					public void onFail(Integer msgId) {
						task.detach();
					}
				});
				task.execGet(Const.Urls.BOOKMARK_URL + t.id, Void.class);
			}
		});
		holder.seeders.setText(t.seeders + "");
		holder.leachers.setText(t.leechers + "");
		holder.snatched.setText(t.snatched + "");
		holder.uploader.setText(t.uploader);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private static class GroupHolder {
		ImageView expand;
		ImageView sticky;
		ImageView freeType;
		ImageView firstClass;
		ImageView secondClass;
		TextView title;
		TextView subtitle;

		GroupHolder(View v) {
			expand = (ImageView) v.findViewById(R.id.expand);
			sticky = (ImageView) v.findViewById(R.id.sticky);
			firstClass = (ImageView) v.findViewById(R.id.first_class);
			secondClass = (ImageView) v.findViewById(R.id.second_class);
			title = (TextView) v.findViewById(R.id.torrent_title);
			title.getPaint().setFakeBoldText(true);
			subtitle = (TextView) v.findViewById(R.id.torrent_subtitle);
		}
	}

	private static class ChildHolder {
		TextView comments;
		TextView bookmark;
		TextView time;
		TextView size;
		TextView seeders;
		TextView leachers;
		TextView snatched;
		TextView uploader;

		ChildHolder(View v) {
			comments = (TextView) v.findViewById(R.id.torrent_comments);
			bookmark = (TextView) v.findViewById(R.id.torrent_bookmark);
			time = (TextView) v.findViewById(R.id.torrent_time);
			size = (TextView) v.findViewById(R.id.torrent_size);
			seeders = (TextView) v.findViewById(R.id.torrent_seeders);
			leachers = (TextView) v.findViewById(R.id.torrent_leachers);
			snatched = (TextView) v.findViewById(R.id.torrent_snatched);
			uploader = (TextView) v.findViewById(R.id.torrent_uploader);
		}
	}

}
