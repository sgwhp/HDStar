package org.hdstar.widget.adapter;

import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.Torrent;
import org.hdstar.ptadapter.PTAdapter;
import org.hdstar.task.BaseAsyncTask;
import org.hdstar.task.BaseAsyncTask.TaskCallback;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 种子列表适配器
 * @author robust
 */
public class TorrentAdapter extends ExpandableListItemAdapter<Torrent> {
	private Resources res;
	private LayoutInflater inflater;
	PTAdapter adapter;
	private List<Torrent> torrents;
	private Drawable bookmarked, unbookmark;
	private Drawable rss, rssAdded;

	public TorrentAdapter(Context context, PTAdapter adapter,
			List<Torrent> torrents) {
        super(context, R.layout.torrent_item
                , R.id.torrent_group
                , R.id.torrent_child, torrents);
		inflater = LayoutInflater.from(context);
		res = context.getResources();
		this.adapter = adapter;
		this.torrents = torrents;
		Resources res = context.getResources();
		bookmarked = getCompoundDrawable(res, R.drawable.bookmarked_btn_bg_sel);
		unbookmark = getCompoundDrawable(res, R.drawable.unbookmark_btn_bg_sel);
		rss = getCompoundDrawable(res, R.drawable.torrent_rss_sel);
		rssAdded = getCompoundDrawable(res, R.drawable.torrent_rss_added_sel);
	}

    @Override
    public View getTitleView(final int position, View convertView, final ViewGroup parent) {
        final GroupHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.torrent_row_group, null);
            holder = new GroupHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }
        final Torrent t = torrents.get(position);
//        if (isExpanded) {
//            holder.expand.setImageResource(R.drawable.arrow_expand);
//        } else {
//            holder.expand.setImageResource(R.drawable.arrow_collapse);
//        }
        if (t.sticky) {
            holder.sticky.setVisibility(View.VISIBLE);
        } else {
            holder.sticky.setVisibility(View.INVISIBLE);
        }
        holder.freeType.setText(Const.FreeType.getFreeTag(t.freeType));
        holder.title.setText(t.title);
        holder.subtitle.setText(t.subtitle);
        if (t.firstClass != null) {
            String firstClassUri;
            if (!t.firstClass.startsWith("/")) {
                //本地图片，放在assets/pic/torrent_class目录下
                firstClassUri = "assets://pic/torrent_class/" + t.firstClass + ".png";
            } else {
                //网络图片
                firstClassUri = adapter.getType().getUrl() + t.firstClass;
            }
            holder.title.setCompoundDrawables(null, null, null, null);
            ImageLoader.getInstance().loadImage(
                    firstClassUri,
                    HDStarApp.displayOptions, new ImageLoadingListener() {

                        @Override
                        public void onLoadingCancelled(String arg0,
                                                       View arg1) {
                        }

                        @Override
                        public void onLoadingComplete(String arg0,
                                                      View arg1, Bitmap result) {
                            if(result == null){
                                holder.title.setCompoundDrawables(null,
                                        null, null, null);
                                return;
                            }
                            result.setDensity(160);
                            BitmapDrawable drawable = new BitmapDrawable(
                                    res, result);
                            drawable.setBounds(0, 0,
                                    drawable.getIntrinsicWidth(),
                                    drawable.getIntrinsicHeight());
                            holder.title.setCompoundDrawables(drawable,
                                    null, null, null);
                        }

                        @Override
                        public void onLoadingFailed(String arg0, View arg1,
                                                    FailReason arg2) {
                        }

                        @Override
                        public void onLoadingStarted(String arg0, View arg1) {
                        }
                    });
        } else {
            holder.title.setCompoundDrawables(null, null, null, null);
        }
        if (t.secondClass == null) {
            holder.subtitle.setCompoundDrawables(null, null, null, null);
            return convertView;
        }
        String secondClassUri;
        if (!t.secondClass.startsWith("/")) {
            //本地图片，放在assets/pic/torrent_class目录下
            secondClassUri = "assets://pic/torrent_class/" + t.secondClass + ".png";
        } else {
            //网络图片
            secondClassUri = adapter.getType().getUrl() + t.secondClass;
        }
        holder.subtitle.setCompoundDrawables(null, null, null, null);
        ImageLoader.getInstance().loadImage(
                secondClassUri,
                HDStarApp.displayOptions, new ImageLoadingListener() {

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1,
                                                  Bitmap result) {
                        if(result == null){
                            holder.subtitle.setCompoundDrawables(null, null, null, null);
                            return;
                        }
                        result.setDensity(160);
                        BitmapDrawable drawable = new BitmapDrawable(res,
                                result);
                        drawable.setBounds(0, 0,
                                drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight());
                        holder.subtitle.setCompoundDrawables(drawable,
                                null, null, null);
                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1,
                                                FailReason arg2) {
                    }

                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                    }
                });
        return convertView;
    }

    @Override
    public View getContentView(final int position, View convertView, final ViewGroup parent) {
        ChildHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.torrent_row_child, null);
            holder = new ChildHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }
        final Torrent t = torrents.get(position);
        holder.comments.setText(t.comments);
        holder.time.setText(t.time);
        holder.size.setText(t.size);
        // bookmark
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
                BaseAsyncTask.commit(task);
            }
        });
        // rss download
        if (adapter.rssEnable()) {
            holder.rss.setVisibility(View.VISIBLE);
            if (t.rss) {
                holder.rss.setCompoundDrawables(null, rssAdded, null, null);
            } else {
                holder.rss.setCompoundDrawables(null, rss, null, null);
            }
            holder.rss.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final BaseAsyncTask<Boolean> task = adapter.addToRss(t.id
                            + "");
                    task.attach(new TaskCallback<Boolean>() {

                        @Override
                        public void onComplete(Boolean result) {
                            task.detach();
                            t.rss = !t.rss;
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
                    BaseAsyncTask.commit(task);
                }
            });
        } else {
            holder.rss.setVisibility(View.GONE);
        }

        holder.seeders.setText(t.seeders);
        holder.leachers.setText(t.leechers);
        holder.snatched.setText(t.snatched);
        holder.uploader.setText(t.uploader);
        return convertView;
    }

//	@Override
//	public int getGroupCount() {
//		return torrents.size();
//	}
//
//	@Override
//	public int getChildrenCount(int groupPosition) {
//		return 1;
//	}
//
//	@Override
//	public Object getGroup(int groupPosition) {
//		return null;
//	}
//
//	@Override
//	public Object getChild(int groupPosition, int childPosition) {
//		return null;
//	}
//
//	@Override
//	public long getGroupId(int groupPosition) {
//		return 0;
//	}
//
//	@Override
//	public long getChildId(int groupPosition, int childPosition) {
//		return 0;
//	}
//
//	@Override
//	public boolean hasStableIds() {
//		return false;
//	}
//
//	@Override
//	public View getGroupView(int groupPosition, boolean isExpanded,
//			View convertView, ViewGroup parent) {
//		final GroupHolder holder;
//		if (convertView == null) {
//			convertView = inflater.inflate(R.layout.torrent_row_group, null);
//			holder = new GroupHolder(convertView);
//			convertView.setTag(holder);
//		} else {
//			holder = (GroupHolder) convertView.getTag();
//		}
//		final Torrent t = torrents.get(groupPosition);
//		if (isExpanded) {
//			holder.expand.setImageResource(R.drawable.arrow_expand);
//		} else {
//			holder.expand.setImageResource(R.drawable.arrow_collapse);
//		}
//		if (t.sticky) {
//			holder.sticky.setVisibility(View.VISIBLE);
//		} else {
//			holder.sticky.setVisibility(View.INVISIBLE);
//		}
//		holder.freeType.setText(Const.FreeType.getFreeTag(t.freeType));
//		holder.title.setText(t.title);
//		holder.subtitle.setText(t.subtitle);
//		if (t.firstClass != null) {
//            String firstClassUri;
//			if (!t.firstClass.startsWith("/")) {
//                //本地图片，放在assets/pic/torrent_class目录下
//				firstClassUri = "assets://pic/torrent_class/" + t.firstClass + ".png";
//			} else {
//                //网络图片
//				firstClassUri = adapter.getType().getUrl() + t.firstClass;
//			}
//            holder.title.setCompoundDrawables(null, null, null, null);
//            ImageLoader.getInstance().loadImage(
//                    firstClassUri,
//                    HDStarApp.displayOptions, new ImageLoadingListener() {
//
//                        @Override
//                        public void onLoadingCancelled(String arg0,
//                                                       View arg1) {
//                        }
//
//                        @Override
//                        public void onLoadingComplete(String arg0,
//                                                      View arg1, Bitmap result) {
//                            if(result == null){
//                                holder.title.setCompoundDrawables(null,
//                                    null, null, null);
//                                return;
//                            }
//                            result.setDensity(160);
//                            BitmapDrawable drawable = new BitmapDrawable(
//                                    res, result);
//                            drawable.setBounds(0, 0,
//                                    drawable.getIntrinsicWidth(),
//                                    drawable.getIntrinsicHeight());
//                            holder.title.setCompoundDrawables(drawable,
//                                    null, null, null);
//                        }
//
//                        @Override
//                        public void onLoadingFailed(String arg0, View arg1,
//                                                    FailReason arg2) {
//                        }
//
//                        @Override
//                        public void onLoadingStarted(String arg0, View arg1) {
//                        }
//                    });
//		} else {
//			holder.title.setCompoundDrawables(null, null, null, null);
//		}
//		if (t.secondClass == null) {
//			holder.subtitle.setCompoundDrawables(null, null, null, null);
//			return convertView;
//		}
//        String secondClassUri;
//		if (!t.secondClass.startsWith("/")) {
//            //本地图片，放在assets/pic/torrent_class目录下
//            secondClassUri = "assets://pic/torrent_class/" + t.secondClass + ".png";
//		} else {
//            //网络图片
//            secondClassUri = adapter.getType().getUrl() + t.secondClass;
//		}
//        holder.subtitle.setCompoundDrawables(null, null, null, null);
//        ImageLoader.getInstance().loadImage(
//                secondClassUri,
//                HDStarApp.displayOptions, new ImageLoadingListener() {
//
//                    @Override
//                    public void onLoadingCancelled(String arg0, View arg1) {
//                    }
//
//                    @Override
//                    public void onLoadingComplete(String arg0, View arg1,
//                                                  Bitmap result) {
//                        if(result == null){
//                            holder.subtitle.setCompoundDrawables(null, null, null, null);
//                            return;
//                        }
//                        result.setDensity(160);
//                        BitmapDrawable drawable = new BitmapDrawable(res,
//                                result);
//                        drawable.setBounds(0, 0,
//                                drawable.getIntrinsicWidth(),
//                                drawable.getIntrinsicHeight());
//                        holder.subtitle.setCompoundDrawables(drawable,
//                                null, null, null);
//                    }
//
//                    @Override
//                    public void onLoadingFailed(String arg0, View arg1,
//                                                FailReason arg2) {
//                    }
//
//                    @Override
//                    public void onLoadingStarted(String arg0, View arg1) {
//                    }
//                });
//		return convertView;
//	}
//
//	@Override
//	public View getChildView(int groupPosition, int childPosition,
//			boolean isLastChild, View convertView, ViewGroup parent) {
//		ChildHolder holder;
//		if (convertView == null) {
//			convertView = inflater.inflate(R.layout.torrent_row_child, null);
//			holder = new ChildHolder(convertView);
//			convertView.setTag(holder);
//		} else {
//			holder = (ChildHolder) convertView.getTag();
//		}
//		final Torrent t = torrents.get(groupPosition);
//		holder.comments.setText(t.comments);
//		holder.time.setText(t.time);
//		holder.size.setText(t.size);
//		// bookmark
//		if (t.bookmark) {
//			holder.bookmark.setCompoundDrawables(null, bookmarked, null, null);
//			holder.bookmark.setText(R.string.unbookmark);
//		} else {
//			holder.bookmark.setCompoundDrawables(null, unbookmark, null, null);
//			holder.bookmark.setText(R.string.bookmark);
//		}
//		holder.bookmark.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				final BaseAsyncTask<Boolean> task = adapter.bookmark(t.id + "");
//				task.attach(new TaskCallback<Boolean>() {
//
//					@Override
//					public void onComplete(Boolean result) {
//						task.detach();
//						t.bookmark = !t.bookmark;
//						notifyDataSetChanged();
//					}
//
//					@Override
//					public void onCancel() {
//						task.detach();
//					}
//
//					@Override
//					public void onFail(Integer msgId) {
//						task.detach();
//					}
//				});
//				BaseAsyncTask.commit(task);
//			}
//		});
//		// rss download
//		if (adapter.rssEnable()) {
//			holder.rss.setVisibility(View.VISIBLE);
//			if (t.rss) {
//				holder.rss.setCompoundDrawables(null, rssAdded, null, null);
//			} else {
//				holder.rss.setCompoundDrawables(null, rss, null, null);
//			}
//			holder.rss.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					final BaseAsyncTask<Boolean> task = adapter.addToRss(t.id
//							+ "");
//					task.attach(new TaskCallback<Boolean>() {
//
//						@Override
//						public void onComplete(Boolean result) {
//							task.detach();
//							t.rss = !t.rss;
//							notifyDataSetChanged();
//						}
//
//						@Override
//						public void onCancel() {
//							task.detach();
//						}
//
//						@Override
//						public void onFail(Integer msgId) {
//							task.detach();
//						}
//					});
//					BaseAsyncTask.commit(task);
//				}
//			});
//		} else {
//			holder.rss.setVisibility(View.GONE);
//		}
//
//		holder.seeders.setText(t.seeders);
//		holder.leachers.setText(t.leechers);
//		holder.snatched.setText(t.snatched);
//		holder.uploader.setText(t.uploader);
//		return convertView;
//	}
//
//	@Override
//	public boolean isChildSelectable(int groupPosition, int childPosition) {
//		return false;
//	}

	private Drawable getCompoundDrawable(Resources res, int id) {
		Drawable drawable = res.getDrawable(id);
		if (drawable != null) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
		}
		return drawable;
	}

	private static class GroupHolder {
//		ImageView expand;
		ImageView sticky;
		TextView freeType;
		// ImageView freeType;
		// ImageView firstClass;
		// ImageView secondClass;
		TextView title;
		TextView subtitle;

		GroupHolder(View v) {
//			expand = (ImageView) v.findViewById(R.id.expand);
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
		TextView rss;

		ChildHolder(View v) {
			comments = (TextView) v.findViewById(R.id.torrent_comments);
			bookmark = (TextView) v.findViewById(R.id.torrent_bookmark);
			time = (TextView) v.findViewById(R.id.torrent_time);
			size = (TextView) v.findViewById(R.id.torrent_size);
			seeders = (TextView) v.findViewById(R.id.torrent_seeders);
			leachers = (TextView) v.findViewById(R.id.torrent_leachers);
			snatched = (TextView) v.findViewById(R.id.torrent_snatched);
			uploader = (TextView) v.findViewById(R.id.torrent_uploader);
			rss = (TextView) v.findViewById(R.id.torrent_rss);
		}
	}

}
