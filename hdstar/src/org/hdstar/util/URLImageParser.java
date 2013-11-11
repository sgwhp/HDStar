package org.hdstar.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class URLImageParser implements ImageGetter {
	Context c;
	TextView container;

	/***
	 * Construct the URLImageParser which will execute AsyncTask and refresh the
	 * container
	 * 
	 * @param t
	 * @param c
	 */
	public URLImageParser(TextView t, Context c) {
		this.c = c;
		this.container = t;
	}

	public Drawable getDrawable(String source) {
		if(!CustomSetting.loadImage){
			return null;
		}
		if (source.startsWith("pic/smilies/")) {
			source = "assets://" + source;
		} else if (!source.startsWith("http://")) {
			source = Const.Urls.SERVER_GET_IMAGE_URL + Const.Urls.BASE_URL
					+ "/" + source;
		} else {
			source = Const.Urls.SERVER_GET_IMAGE_URL + source;
		}
		try {
			URLEncoder.encode(source, Const.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		List<Bitmap> list = null;
		if ((list = MemoryCacheUtil.findCachedBitmapsForImageUri(source,
				ImageLoader.getInstance().getMemoryCache())) != null
				&& list.size() > 0) {
			Bitmap bitmap = list.get(0);
			Drawable d = new BitmapDrawable(c.getResources(), bitmap);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
		final URLDrawable urlDrawable = new URLDrawable(c.getResources());
		urlDrawable.setDrawable(c.getResources().getDrawable(
				R.drawable.url_image_loading));
		// get the actual source
		// ImageGetterAsyncTask asyncTask =
		// new ImageGetterAsyncTask( urlDrawable);
		//
		// asyncTask.execute(source);

		// return reference to URLDrawable where I will change with actual image
		// from
		// the src tag
		ImageLoader.getInstance().loadImage(source, HDStarApp.displayOptions,
				new SimpleImageLoadingListener() {
					@SuppressWarnings("unused")
					private View mView;

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// see bug:
						// https://github.com/nostra13/Android-Universal-Image-Loader/issues/356
						mView = view;
						super.onLoadingStarted(imageUri, view);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							urlDrawable.setDrawable(new BitmapDrawable(c
									.getResources(), loadedImage));
							// 可以解决图片载入后重叠的问题，但是textview会被设定死了高度
							// container.setHeight(container.getHeight()
							// + drawable.getIntrinsicHeight());
							// container.setEllipsize(null);
							container.requestLayout();
							URLImageParser.this.container.invalidate();

							URLImageParser.this.container.setText(container
									.getText());
						}
						mView = null;
					}

					// @Override
					// public void onLoadingCancelled(String imageUri, View
					// view) {
					// super.onLoadingCancelled(imageUri, view);
					// Log.v("whp", (imageUri + " load image canceled"));
					// }

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						urlDrawable.setDrawable(c.getResources().getDrawable(
								R.drawable.url_image_failed));
						container.requestLayout();
						URLImageParser.this.container.invalidate();

						URLImageParser.this.container.setText(container
								.getText());
						// Log.v("whp", imageUri + " load image failed");
						mView = null;
					}
				});

		return urlDrawable;
	}

	public class URLDrawable extends BitmapDrawable {
		// the drawable that you need to set, you could set the initial drawing
		// with the loading image if you need to
		protected Drawable drawable;

		public URLDrawable(Resources res) {
			super(res);
		}

		public void setDrawable(Drawable d) {
			drawable = d;
			drawable.setBounds(0, 0, d.getIntrinsicWidth(),
					d.getIntrinsicHeight());
			this.setBounds(drawable.getBounds());
		}

		@Override
		public void draw(Canvas canvas) {
			// override the draw to facilitate refresh function later
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}
}