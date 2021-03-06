package org.hdstar.util;

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
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.common.CustomSetting;
import org.hdstar.component.HDStarApp;

import java.util.List;

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
		if (!CustomSetting.loadImage) {
			return null;
		}
		source = CommonUrls.HDStar.genGetImageUrl(source);
		List<Bitmap> list;
		if ((list = MemoryCacheUtils.findCachedBitmapsForImageUri(source,
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
		// 此处不能使用圆角的图片显示配置
		ImageLoader.getInstance().loadImage(source, HDStarApp.displayOptions,
				new SimpleImageLoadingListener() {
					// private View mView;

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// 以下bug已在UIL.1.9中修复
						// see bug:
						// https://github.com/nostra13/Android-Universal-Image-Loader/issues/356
						// mView = view;
						super.onLoadingStarted(imageUri, view);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
                            if(imageUri.startsWith("assets://pic/smilies/")){
                                //表情图片
                                loadedImage.setDensity(160);
                            }
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
						// mView = null;
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						urlDrawable.setDrawable(c.getResources().getDrawable(
                                R.drawable.url_image_failed));
						container.requestLayout();
						URLImageParser.this.container.invalidate();

						URLImageParser.this.container.setText(container
								.getText());
						// mView = null;
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
