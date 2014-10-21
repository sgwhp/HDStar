package org.hdstar.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

/**
 * Created by robust on 2014-09-14.
 */
public class ImgTagHandler implements Html.TagHandler {

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if(tag.equalsIgnoreCase("img")){
            int len = output.length();
            ImageSpan[] images = output.getSpans(len-1, len, ImageSpan.class);
            String imgURL = images[0].getSource();
            output.setSpan(new ImgSpan(imgURL), len-1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            Log.v("whp", "handle " + len + output.toString());
//            if(opening){
//                output.setSpan(new ImgSpan(), len, len, Spanned.SPAN_MARK_MARK);
//            } else {
//                Object obj = getLast(output, ImgSpan.class);
//                if(obj == null){
//                    return;
//                }
//                int where = output.getSpanStart(obj);
//                output.removeSpan(obj);
//                if (where != len) {
//                    output.setSpan(new ImgSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                }
//            }
        }
    }

//    private Object getLast(Editable text, Class kind) {
//        Object[] objs = text.getSpans(0, text.length(), kind);
//
//        if (objs.length == 0) {
//            return null;
//        } else {
//            for(int i = objs.length; i>0; i--) {
//                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
//                    return objs[i-1];
//                }
//            }
//            return null;
//        }
//    }
}
