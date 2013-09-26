package org.hdstar.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.hdstar.R;
import org.hdstar.common.Const;
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
		if (!source.startsWith("http://")) {
			source = Const.Urls.BASE_URL + "/" + source;
		}
		source = Const.Urls.SERVER_GET_IMAGE_URL + source;
		try {
			URLEncoder.encode(source, Const.CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							urlDrawable.setDrawable(new BitmapDrawable(c
									.getResources(), loadedImage));
							// urlDrawable.drawable.setBounds(0, 0,
							// 0 + urlDrawable.drawable
							// .getIntrinsicWidth(),
							// 0 + urlDrawable.drawable
							// .getIntrinsicHeight());
							URLImageParser.this.container.invalidate();

							// URLImageParser.this.container.setText(container.getText());
						}
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