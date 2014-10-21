package org.hdstar.util;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by robust on 2014-09-14.
 */
public class ImgSpan extends ClickableSpan {
    private String mUrl;

    public ImgSpan(String url){
        mUrl = url;
    }

    @Override
    public void onClick(View widget) {
    }

    public String getSrc(){
        return mUrl;
    }
}
