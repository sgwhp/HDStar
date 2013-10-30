package org.hdstar.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hdstar.common.Const;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

public class MyTextParser {
	private Context mContext;
	private String[] mSmileyTexts;
	private Pattern mPattern;
	private HashMap<String, Integer> mSmileyToRes;
	private HashMap<String, Integer> smileToCode;

	public MyTextParser(Context context) {
		mContext = context;
		smileToCode = new HashMap<String, Integer>();
		mSmileyTexts = new String[Const.DEFAULT_SMILEY_RES_IDS.length];
		for (int i = 0; i < mSmileyTexts.length; i++) {
			mSmileyTexts[i] = "[sm" + i + "]";
			smileToCode.put(mSmileyTexts[i], i);
		}

		mSmileyToRes = buildSmileyToRes();
		mPattern = buildPattern();
	}

	private HashMap<String, Integer> buildSmileyToRes() {
		if (Const.DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
			// Log.w("SmileyParser", "Smiley resource ID/text mismatch");
			// 表情的数量需要和数组定义的长度一致！
			throw new IllegalStateException("Smiley resource ID/text mismatch");
		}

		HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(
				mSmileyTexts.length);
		for (int i = 0; i < mSmileyTexts.length; i++) {
			smileyToRes.put(mSmileyTexts[i], Const.DEFAULT_SMILEY_RES_IDS[i]);
		}

		return smileyToRes;
	}

	// 构建正则表达式
	private Pattern buildPattern() {
		return Pattern.compile("\\[sm[0-9]+\\]", Pattern.DOTALL);
	}

	// 根据文本替换成图片
	public CharSequence toSpan(CharSequence text) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {
			Integer resId = mSmileyToRes.get(matcher.group());
			if (resId != null)
				builder.setSpan(new ImageSpan(mContext, resId),
						matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}

	public String toImg(CharSequence text) {
		String body = text.toString();
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {
			String temp = matcher.group();
			String code = "[img]" + Const.links[smileToCode.get(temp)]
					+ "[/img]";
			body = body.replace(temp, code);
		}
		return body;
	}

	public static String toBBCode(String text) {
		// 替换所有的换行
		// text = text.replaceAll("<br\\s.*?>", "\n");
		// 替换引用的后半部分
		text = text.replaceAll("</fieldset>", "[/quote]");
		// 替换引用的前半部分
		Pattern pattern = Pattern.compile(
				"<fieldset>\\s.*?<legend>([^<]*?)</legend>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);
		String temp = "";
		while (matcher.find()) {
			// matcher.replaceFirst("[quote=" + matcher.group(1) + "]");
			int index = matcher.group(1).indexOf(":");
			if (index != -1) {
				temp = matcher.group(1).substring(index + 2,
						matcher.group(1).length());
			} else {
				temp = matcher.group(1);
			}
			text = text.replace(matcher.group(), "[quote=" + temp + "]");
		}
		// 替换表情
		// pattern = Pattern.compile("<img.*?alt=\"(\\[em\\d+\\])\"\\s.*?/>",
		// Pattern.DOTALL);
		pattern = Pattern.compile("<img[^>]*?alt=\"(\\[em\\d+\\])\"\\s.*?/>",
				Pattern.DOTALL);
		matcher = pattern.matcher(text);
		while (matcher.find()) {
			// matcher.replaceFirst(matcher.group(1));
			text = text.replace(matcher.group(), matcher.group(1));
		}
		// 替换图片
		// pattern = Pattern.compile("<img\\s.*?src=\"([^\"]+)\".*?/>",
		// Pattern.DOTALL);
		pattern = Pattern.compile("<img[^>]*?src=\"([^\"]+)\".*?/>",
				Pattern.DOTALL);
		matcher = pattern.matcher(text);
		while (matcher.find()) {
			// matcher.replaceFirst("[img]" + matcher.group(1) + "[/img]");
			text = text.replace(matcher.group(), "[img]" + matcher.group(1)
					+ "[/img]");
		}
		// 替换超链接
		pattern = Pattern.compile("<a\\s.*?href=\"([^\"]+)\"[^>]*>(.*?)</a>",
				Pattern.DOTALL);
		matcher = pattern.matcher(text);
		while (matcher.find()) {
			// matcher.replaceFirst("[url=" + matcher.group(1) + "]" +
			// matcher.group(2) + "[/url]");
			text = text.replace(matcher.group(), "[url=" + matcher.group(1)
					+ "]" + matcher.group(2) + "[/url]");
		}
		// 替换<i>标签
		text.replaceAll("<i>", "[i]");
		text.replaceAll("<i\\>", "[/i]");
		// 替换视频
		pattern = Pattern.compile("<embed\\s.*?src=\"(.*?)\".*?</embed>");
		matcher = pattern.matcher(text);
		while (matcher.find()) {
			text = text.replace(matcher.group(), matcher.group(1));
		}
		return text;
	}

	public static String toQuote(String text, String username) {
		Document doc = Jsoup.parseBodyFragment(text);
		Elements elements = doc.getElementsByTag("div");
		if (elements.size() == 0) {
			return text;
		}
		text = elements.get(0).html();
		text = toBBCode(text);
		text = "[quote=" + username + "]" + text + "[/quote]";
		Spanned s = Html.fromHtml(text);
		text = s.toString();
		return text;
	}

	public static String toReplyPM(Context context, int sender, String text) {
		text = toBBCode(text);
		String userName = context.getSharedPreferences(Const.SETTING_SHARED_PREFS,
				Activity.MODE_PRIVATE).getString("username", "");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		text += "<br>-------- [url=userdetails.php?id=" + sender + "]"
				+ userName + "[/url][i] Wrote at "
				+ format.format(new Date(System.currentTimeMillis()))
				+ ":[/i] --------<br>";
		Spanned s = Html.fromHtml(text);
		text = s.toString();
		return text;
	}

	public static String toReplySubject(String subject) {
		Pattern pattern = Pattern.compile("Re\\(([0-9]+)\\):");
		Matcher matcher = pattern.matcher(subject);
		StringBuffer buffer = new StringBuffer();
		if (matcher.find()) {
			int num = Integer.parseInt(matcher.group(1));
			num++;

			matcher.appendReplacement(buffer, "Re(" + num + "):");
			matcher.appendTail(buffer);
		} else {
			buffer.append(subject);
			if(subject.startsWith("Re")){
				buffer.insert(2, "(2)");
			} else {
				buffer.insert(0, "Re: ");
			}
		}
		return buffer.toString();
	}
}
