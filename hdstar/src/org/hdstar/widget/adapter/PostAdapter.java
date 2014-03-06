package org.hdstar.widget.adapter;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.Const;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.FieldSetVO;
import org.hdstar.model.Post;
import org.hdstar.util.CustomLinkMovementMethod;
import org.hdstar.util.MyTextParser;
import org.hdstar.util.URLImageParser;
import org.hdstar.util.UserClassImageGetter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PostAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Post> posts;
	private SparseArray<FieldSetVO> quotes;
	private WeakReference<Context> ref = null;
	private OnPostClickListener mListener;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	public PostAdapter(Context context, List<Post> items,
			OnPostClickListener listener) {
		inflater = LayoutInflater.from(context);
		ref = new WeakReference<Context>(context);
		this.posts = items;
		quotes = new SparseArray<FieldSetVO>();
		this.mListener = listener;
	}

	public void clearAnimListener() {
		AnimateFirstDisplayListener.displayedImages.clear();
	}

	public List<Post> getList() {
		return posts;
	}

	public void setList(List<Post> items) {
		this.posts.clear();
		this.posts.addAll(items);
	}

	public void clearItems() {
		posts.clear();
	}

	public void addAll(List<Post> items) {
		// "   帖子：595    上传：3.754  TB   下载：35.75  GB   分享率：107.505" 去除空白字符
		Pattern pattern = Pattern
				.compile(
						"\\s+([^\\s]+)\\s+([^\\s]+\\s+[^\\s]+)\\s+([^\\s]+\\s+[^\\s]+)\\s+([^\\s]+)",
						Pattern.DOTALL);
		Matcher matcher;
		for (Post post : items) {
			matcher = pattern.matcher(post.info);
			if (matcher.find()) {
				post.info = matcher.group(1) + "\n"
						+ matcher.group(2).replaceAll("\\s+", "") + "\n"
						+ matcher.group(3).replaceAll("\\s+", "") + "\n"
						+ matcher.group(4);
			}
		}
		this.posts.addAll(items);
	}

	@Override
	public int getCount() {
		if (posts != null)
			return posts.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if (posts != null)
			return posts.get(arg0);
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup par) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.post, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Post p = posts.get(position);
		holder.username.setText(Html.fromHtml(p.userName));
		holder.floor.setText(p.floor);
		holder.userClass.setImageBitmap(UserClassImageGetter.get(
				p.userClassSrc, (Context) ref.get()));
		FieldSetVO quote = quotes.get(position);
		if (quote == null) {
			quote = new FieldSetVO();
			quotes.put(position, quote);
			MyTextParser.toFiledSetVO(quote, p.body, 0);
		}
		// if (CustomSetting.loadImage) {
		holder.contentOuter.setText(Html.fromHtml(quote.content,
				new URLImageParser(holder.contentOuter, ref.get()), null));
		// holder.contentOuter.setMovementMethod(CustomLinkMovementMethod
		// .getInstance());
		// setMovementMethod后需要调用以下方法，不能在xml里配置
		// holder.contentOuter.setFocusable(false);
		// holder.contentOuter.setFocusableInTouchMode(false);
		if (quote.fieldSet != null) {
			holder.legend.setVisibility(View.VISIBLE);
			holder.frameOuter.setVisibility(View.VISIBLE);
			holder.legend.setText(quote.legend);
			quote = quote.fieldSet;
			holder.contentMiddle.setText(Html.fromHtml(quote.content,
					new URLImageParser(holder.contentMiddle, ref.get()), null));
			// holder.contentMiddle.setMovementMethod(CustomLinkMovementMethod
			// .getInstance());
			// setMovementMethod后需要调用以下方法，不能在xml里配置
			// holder.contentMiddle.setFocusable(false);
			// holder.contentMiddle.setFocusableInTouchMode(false);
			if (quote.fieldSet != null) {
				holder.legendInner.setVisibility(View.VISIBLE);
				holder.frameInner.setVisibility(View.VISIBLE);
				holder.legendInner.setText(quote.legend);
				quote = quote.fieldSet;
				holder.contentInner.setText(Html.fromHtml(quote.content,
						new URLImageParser(holder.contentInner, ref.get()),
						null));
				// holder.contentInner.setMovementMethod(CustomLinkMovementMethod
				// .getInstance());
				// setMovementMethod后需要调用以下方法，不能在xml里配置
				// holder.contentInner.setFocusable(false);
				// holder.contentInner.setFocusableInTouchMode(false);
				if (quote.fieldSet != null) {
					holder.frameMore.setVisibility(View.VISIBLE);
				} else {
					holder.frameMore.setVisibility(View.GONE);
				}
			} else {
				holder.legendInner.setVisibility(View.GONE);
				holder.frameInner.setVisibility(View.GONE);
			}
		} else {
			holder.legend.setVisibility(View.GONE);
			holder.frameOuter.setVisibility(View.GONE);
		}
		// holder.main.setText(Html.fromHtml(p.body, new URLImageParser(
		// holder.main, ref.get()), null));
		// holder.main.setMovementMethod(CustomLinkMovementMethod
		// .getInstance());
		// holder.main.setFocusable(false);
		// holder.main.setFocusableInTouchMode(false);
		try {
			ImageLoader.getInstance().displayImage(
					CommonUrls.HDStar.SERVER_GET_IMAGE_URL
							+ URLEncoder.encode(p.avatarSrc, Const.CHARSET),
					holder.avatar, HDStarApp.roundedDisplayOptions,
					animateFirstListener);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// }
		// else {
		// holder.main.setText(Html.fromHtml(p.body));
		// }
		holder.info.setText(p.info);
		holder.pm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.pm(p.uid);
			}
		});
		if (p.quote) {
			holder.quote.setVisibility(View.VISIBLE);
			holder.quote.setClickable(true);
			holder.quote.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.quote(p);
				}
			});
		} else {
			holder.quote.setVisibility(View.INVISIBLE);
			holder.quote.setClickable(false);
		}
		if (p.edit) {
			holder.edit.setVisibility(View.VISIBLE);
			holder.edit.setEnabled(true);
			holder.edit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.edit(p, position == 0);
				}
			});
		} else {
			holder.edit.setVisibility(View.INVISIBLE);
			holder.edit.setClickable(false);
		}
		if (p.delete) {
			holder.delete.setVisibility(View.VISIBLE);
			holder.delete.setEnabled(true);
			holder.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.delete(p.id, position == 0);
				}
			});
		} else {
			holder.delete.setVisibility(View.INVISIBLE);
			holder.delete.setClickable(false);
		}
		return convertView;
	}

	private class ViewHolder {
		TextView username, info, floor, contentOuter, legend, contentInner,
				legendInner, contentMiddle, frameMore, pm, quote, edit, delete;
		View frameOuter, frameInner;
		ImageView userClass;
		ImageView avatar;

		ViewHolder(View v) {
			// main = (TextView) v.findViewById(R.id.main);
			contentOuter = (TextView) v.findViewById(R.fieldset.content_outer);
			contentOuter.setMovementMethod(CustomLinkMovementMethod
					.getInstance());
			frameOuter = v.findViewById(R.fieldset.frame_outer);
			legend = (TextView) v.findViewById(R.fieldset.legend);
			contentMiddle = (TextView) v
					.findViewById(R.fieldset.content_middle);
			contentMiddle.setMovementMethod(CustomLinkMovementMethod
					.getInstance());
			frameInner = v.findViewById(R.fieldset.frame_inner);
			legendInner = (TextView) v.findViewById(R.fieldset.legend_inner);
			contentInner = (TextView) v.findViewById(R.fieldset.content_inner);
			contentInner.setMovementMethod(CustomLinkMovementMethod
					.getInstance());
			frameMore = (TextView) v.findViewById(R.fieldset.frame_more);

			pm = (TextView) v.findViewById(R.id.pm);
			quote = (TextView) v.findViewById(R.id.quote);
			edit = (TextView) v.findViewById(R.id.edit);
			delete = (TextView) v.findViewById(R.id.delete);
			username = (TextView) v.findViewById(R.id.username);
			userClass = (ImageView) v.findViewById(R.id.user_class);
			info = (TextView) v.findViewById(R.id.info);
			avatar = (ImageView) v.findViewById(R.id.avatar);
			floor = (TextView) v.findViewById(R.id.floor);
		}
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	public static interface OnPostClickListener {
		public void quote(Post p);

		public void pm(int receiver);

		/**
		 * 编辑帖子
		 * 
		 * @author robust
		 * @param p
		 * @param top
		 *            是否主楼
		 */
		public void edit(Post p, boolean top);

		public void delete(int id, boolean isFirst);
	}

}
