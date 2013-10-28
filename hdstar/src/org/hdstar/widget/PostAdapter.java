package org.hdstar.widget;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;
import org.hdstar.model.Post;
import org.hdstar.util.URLImageParser;
import org.hdstar.util.UserClassImageGetter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PostAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Post> items;
	private WeakReference<Context> ref = null;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	public PostAdapter(Context context, List<Post> items) {
		inflater = LayoutInflater.from(context);
		ref = new WeakReference<Context>(context);
		this.items = items;
	}

	public void clearAnimListener() {
		AnimateFirstDisplayListener.displayedImages.clear();
	}

	public List<Post> getList() {
		return items;
	}

	public void setList(List<Post> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	public void clearItems() {
		items.clear();
	}

	public void addAll(List<Post> items) {
		this.items.addAll(items);
	}

	@Override
	public int getCount() {
		if (items != null)
			return items.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if (items != null)
			return items.get(arg0);
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup par) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.post, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Post p = items.get(position);
		holder.username.setText(Html.fromHtml(p.userName));
		holder.userClass.setImageBitmap(UserClassImageGetter.get(
				p.userClassSrc, (Context) ref.get()));
		if (CustomSetting.loadImage) {
			holder.main.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			holder.main.setText(Html.fromHtml(p.body, new URLImageParser(
					holder.main, ref.get()), null));
			try {
				ImageLoader.getInstance()
						.displayImage(
								Const.Urls.SERVER_GET_IMAGE_URL
										+ URLEncoder.encode(p.avatarSrc,
												Const.CHARSET), holder.avatar,
								HDStarApp.displayOptions, animateFirstListener);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			holder.main.setText(Html.fromHtml(p.body));
		}
		holder.info.setText(p.info);
		return convertView;
	}

	private class ViewHolder {
		TextView main, username, info;
		ImageView userClass;
		ImageView avatar;

		ViewHolder(View v) {
			main = (TextView) v.findViewById(R.id.main);
			username = (TextView) v.findViewById(R.id.username);
			userClass = (ImageView) v.findViewById(R.id.user_class);
			info = (TextView) v.findViewById(R.id.info);
			avatar = (ImageView) v.findViewById(R.id.avatar);
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

}
