package org.hdstar.component.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import org.hdstar.R;
import org.hdstar.common.CommonUrls;
import org.hdstar.component.HDStarApp;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * 图片展示界面
 * Created by robust on 2014-10-10.
 */
public class ImageActivity extends Activity {
    private PhotoView photoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        photoView = (PhotoView) findViewById(R.id.iv_photo);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        String url = getIntent().getStringExtra("url");
        ImageLoader imageLoader = ImageLoader.getInstance();
        List<Bitmap> list;
        //图片已下载
        if ((list = MemoryCacheUtils.findCachedBitmapsForImageUri(url,
                imageLoader.getMemoryCache())) != null) {
            photoView.setImageBitmap(list.get(0));
            return;
        }
        if(DiskCacheUtils.findInCache(url, imageLoader.getDiskCache()) != null){
            imageLoader.displayImage(url, photoView);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        //略缩图已下载
        String thumbnail = CommonUrls.HDStar.SERVER_GET_IMAGE_URL
                + CommonUrls.HDStar.BASE_URL + "/"+ url;
        if ((list = MemoryCacheUtils.findCachedBitmapsForImageUri(thumbnail,
                imageLoader.getMemoryCache())) != null) {
            photoView.setImageBitmap(list.get(0));
        } else if(DiskCacheUtils.findInCache(thumbnail, imageLoader.getDiskCache()) != null){
            imageLoader.displayImage(url, photoView);
        }
        //获取网络图片
        imageLoader.loadImage(url, HDStarApp.displayOptions,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        photoView.setImageBitmap(loadedImage);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        photoView.setImageResource(R.drawable.url_image_failed);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
