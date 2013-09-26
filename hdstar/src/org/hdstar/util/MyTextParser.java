package org.hdstar.util;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.common.Const;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MyTextParser{
    private Context mContext;
    private String[] mSmileyTexts;
    private Pattern mPattern;
    private HashMap<String, Integer> mSmileyToRes;
    private HashMap<String, Integer> smileToCode;
    

    public MyTextParser(Context context) {
        mContext = context;
        smileToCode = new HashMap<String, Integer>();
        mSmileyTexts = new String[Const.DEFAULT_SMILEY_RES_IDS.length];
        for(int i = 0; i < mSmileyTexts.length; i++){
        	mSmileyTexts[i] = "[sm" + i +"]";
        	smileToCode.put(mSmileyTexts[i], i);
        }
        
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    private HashMap<String, Integer> buildSmileyToRes() {
        if (Const.DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
//        	Log.w("SmileyParser", "Smiley resource ID/text mismatch");
            //�����������Ҫ�����鶨��ĳ���һ�£�
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], Const.DEFAULT_SMILEY_RES_IDS[i]);
        }

        return smileyToRes;
    }

    //�����������ʽ
    private Pattern buildPattern() {
    	return Pattern.compile("\\[sm[0-9]+\\]", Pattern.DOTALL);
    }

    //�����ı��滻��ͼƬ
    public CharSequence toSpan(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            Integer resId = mSmileyToRes.get(matcher.group());
            if(resId != null) 
            	builder.setSpan(new ImageSpan(mContext, resId),matcher.start(), 
            		matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
    
    public String toImg(CharSequence text){
    	String body = text.toString();
    	Matcher matcher = mPattern.matcher(text);
    	while(matcher.find()){
    		String temp = matcher.group();
    		String code = "[img]" + Const.links[smileToCode.get(temp)] + "[/img]";
    		body = body.replace(temp, code);
    	}
    	return body;
    }
    
    public static String toBBCode(String text, String username){
    	Document doc = Jsoup.parseBodyFragment(text);
    	Elements elements = doc.getElementsByTag("div");
    	text = elements.get(0).html();
    	//�滻���еĻ���
    	text = text.replaceAll("<br\\s.*?/>", "\n");
    	//�滻���õĺ�벿��
    	text = text.replaceAll("</fieldset>", "[/quote]");
    	//�滻���õ�ǰ�벿��
    	Pattern pattern = Pattern.compile("<fieldset>\\s.*?<legend>([^<]*?)</legend>", Pattern.DOTALL);
    	Matcher matcher = pattern.matcher(text);
    	String temp = "";
    	while(matcher.find()){
    		//matcher.replaceFirst("[quote=" + matcher.group(1) + "]");
    		int index = matcher.group(1).indexOf(":");
    		if(index != -1){
    			temp = matcher.group(1).substring(index + 2, matcher.group(1).length());
    		} else{
    			temp = matcher.group(1);
    		}
    		text = text.replace(matcher.group(), "[quote=" + temp + "]");
    	}
    	//�滻����
    	pattern = Pattern.compile("<img.*?alt=\"(\\[em\\d+\\])\"\\s.*?/>", Pattern.DOTALL);
    	matcher = pattern.matcher(text);
    	while(matcher.find()){
    		//matcher.replaceFirst(matcher.group(1));
    		text = text.replace(matcher.group(), matcher.group(1));
    	}
    	//�滻ͼƬ
    	pattern = Pattern.compile("<img\\s.*?src=\"([^\"]+)\".*?/>", Pattern.DOTALL);
    	matcher = pattern.matcher(text);
    	while(matcher.find()){
    		//matcher.replaceFirst("[img]" + matcher.group(1) + "[/img]");
    		text = text.replace(matcher.group(), "[img]" + matcher.group(1) + "[/img]");
    	}
    	//�滻������
    	pattern = Pattern.compile("<a\\s.*?href=\"([^\"]+)\"[^>]*>(.*?)</a>", Pattern.DOTALL);
    	matcher = pattern.matcher(text);
    	while(matcher.find()){
    		//matcher.replaceFirst("[url=" + matcher.group(1) + "]" + matcher.group(2) + "[/url]");
    		text = text.replace(matcher.group(), "[url=" + matcher.group(1) + "]" + matcher.group(2) + "[/url]");
    	}
    	//�滻��Ƶ
    	pattern = Pattern.compile("<embed\\s.*?src=\"(.*?)\".*?</embed>");
    	matcher = pattern.matcher(text);
    	while(matcher.find()){
    		text = text.replace(matcher.group(), matcher.group(1));
    	}
    	//Log.v("quote", text);
    	text = "[quote=" + username + "]" + text + "[/quote]";
    	Spanned s = Html.fromHtml(text);
    	text = s.toString();
    	return text;
    }
}
