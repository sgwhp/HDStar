package org.hdstar.widget.adapter;

import java.util.HashMap;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.model.Torrent;
import org.hdstar.ptadapter.PTAdapter;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TorrentAdapter extends BaseExpandableListAdapter {
	private LayoutInflater inflater;
	PTAdapter adapter;
	private List<Torrent> torrents;
	private Drawable bookmarked;
	private Drawable unbookmark;
	private HashMap<String, Drawable> klasses = new HashMap<String, Drawable>();

	public TorrentAdapter(Context context, PTAdapter adapter,
			List<Torrent> torrents) {
		inflater = LayoutInflater.from(context);
		this.adapter = adapter;
		this.torrents = torrents;
		Resources res = context.getResources();
		bookmarked = getCompoundDrawable(res, R.drawable.bookmarked_btn_bg_sel);
		unbookmark = getCompoundDrawable(res, R.drawable.unbookmark_btn_bg_sel);
		klasses.put("c_anime", getCompoundDrawable(res, R.drawable.c_anime));
		klasses.put("c_hqaudio", getCompoundDrawable(res, R.drawable.c_hqaudio));
		klasses.put("c_doc", getCompoundDrawable(res, R.drawable.c_doc));
		klasses.put("c_misc", getCompoundDrawable(res, R.drawable.c_misc));
		klasses.put("c_movies", getCompoundDrawable(res, R.drawable.c_movie));
		klasses.put("c_music", getCompoundDrawable(res, R.drawable.c_music));
		klasses.put("c_pad", getCompoundDrawable(res, R.drawable.c_pad));
		klasses.put("c_sports", getCompoundDrawable(res, R.drawable.c_sport));
		klasses.put("c_tvshows", getCompoundDrawable(res, R.drawable.c_tv_show));
		klasses.put("c_tvseries",
				getCompoundDrawable(res, R.drawable.c_tv_series));
		klasses.put("si_bdh264", getCompoundDrawable(res, R.drawable.bd_h264));
		klasses.put("si_bdvc1", getCompoundDrawable(res, R.drawable.bd_vc1));
		klasses.put("si_bdmpeg2", getCompoundDrawable(res, R.drawable.bd_mpeg2));
		klasses.put("si_hddvdh264",
				getCompoundDrawable(res, R.drawable.hddvd_h264));
		klasses.put("si_hddvdvc1",
				getCompoundDrawable(res, R.drawable.hddvd_vc1));
		klasses.put("si_hddvdmpeg2",
				getCompoundDrawable(res, R.drawable.hddvd_mpeg2));
		klasses.put("si_remuxh264",
				getCompoundDrawable(res, R.drawable.remux_h264));
		klasses.put("si_remuxvc1",
				getCompoundDrawable(res, R.drawable.remux_vc1));
		klasses.put("si_remuxmpeg2",
				getCompoundDrawable(res, R.drawable.remux_mpeg2));
		klasses.put("si_avchd", getCompoundDrawable(res, R.drawable.avchd));
		klasses.put("si_hdtvh264",
				getCompoundDrawable(res, R.drawable.hdtv_h264));
		klasses.put("si_hdtvmpeg2",
				getCompoundDrawable(res, R.drawable.hdtv_mpeg2));
		klasses.put("si_dvdr", getCompoundDrawable(res, R.drawable.dvdr));
		klasses.put("si_riph264", getCompoundDrawable(res, R.drawable.rip_h264));
		klasses.put("si_ripxvid", getCompoundDrawable(res, R.drawable.rip_xvid));
		klasses.put("si_cdflac", getCompoundDrawable(res, R.drawable.cd_flac));
		klasses.put("si_cdape", getCompoundDrawable(res, R.drawable.cd_ape));
		klasses.put("si_cddts", getCompoundDrawable(res, R.drawable.cd_dts));
		klasses.put("si_cdother", getCompoundDrawable(res, R.drawable.cd_other));
		klasses.put("si_extractflac",
				getCompoundDrawable(res, R.drawable.extract_flac));
		klasses.put("si_extractdts",
				getCompoundDrawable(res, R.drawable.extract_dts));
		klasses.put("si_extractac3",
				getCompoundDrawable(res, R.drawable.extract_ac3));
		klasses.put("si_notallowed",
				getCompoundDrawable(res, R.drawable.notallowed));
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
		holder.freeType.setText(Const.FreeType.getFreeTag(t.freeType));
		holder.title.setText(t.title);
		Drawable d = klasses.get(t.firstClass);
		if (d != null) {
			holder.title.setCompoundDrawables(d, null, null, null);
		}
		d = klasses.get(t.secondClass);
		if (d != null) {
			holder.subtitle.setCompoundDrawables(d, null, null, null);
		}
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
		holder.comments.setText(t.comments);
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
				final BaseAsyncTask<Boolean> task = adapter.bookmark(t.id + "");
				task.attach(new TaskCallback<Boolean>() {

					@Override
					public void onComplete(Boolean result) {
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
				BaseAsyncTask.taskExec.execute(task);
			}
		});
		holder.seeders.setText(t.seeders);
		holder.leachers.setText(t.leechers);
		holder.snatched.setText(t.snatched);
		holder.uploader.setText(t.uploader);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private Drawable getCompoundDrawable(Resources res, int id) {
		Drawable drawable = res.getDrawable(id);
		if (drawable != null) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
		}
		return drawable;
	}

	private static class GroupHolder {
		ImageView expand;
		ImageView sticky;
		TextView freeType;
		// ImageView freeType;
		// ImageView firstClass;
		// ImageView secondClass;
		TextView title;
		TextView subtitle;

		GroupHolder(View v) {
			expand = (ImageView) v.findViewById(R.id.expand);
			sticky = (ImageView) v.findViewById(R.id.sticky);
			freeType = (TextView) v.findViewById(R.id.free_type);
			// firstClass = (ImageView) v.findViewById(R.id.first_class);
			// secondClass = (ImageView) v.findViewById(R.id.second_class);
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
