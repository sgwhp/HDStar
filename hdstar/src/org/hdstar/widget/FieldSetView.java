package org.hdstar.widget;

import java.util.ArrayList;

import org.hdstar.util.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FieldSetView extends RelativeLayout {
	private final int MAX_LEVEL = 3;
	private int id = 1;
	private ArrayList<TextView> contents;
	// 内容左右边距
	private static final int CONTENT_MARGIN_DP = 5;
	private int contentMargin;
	// 引用文字内部边距
	private static final int LEGEND_PADDING_DP = 2;
	private int legendPadding;
	// 引用文字左边距
	private static final int LEGEND_MARGIN_LEFT_DP = 20;
	private int legendMarginLeft;
	// 引用框边距
	private static final int FRAME_MARGIN_DP = 10;
	private int frameMargin;
	// 引用框内部边距
	private static final int FRAME_PADDING_DP = 10;
	private int framePadding;

	public FieldSetView(Context context) {
		super(context);
		init(context);
	}

	public FieldSetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FieldSetView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		contentMargin = Util.dip2px(context, CONTENT_MARGIN_DP);
		legendPadding = Util.dip2px(context, LEGEND_PADDING_DP);
		legendMarginLeft = Util.dip2px(context, LEGEND_MARGIN_LEFT_DP);
		frameMargin = Util.dip2px(context, FRAME_MARGIN_DP);
		framePadding = Util.dip2px(context, FRAME_PADDING_DP);
		TextView content = genContent();
		contents.add(content);
		addView(content);
	}

	private TextView genContent() {
		TextView content = new TextView(getContext());
		content.setId(id);
		id++;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.setMargins(contentMargin, 0, contentMargin, 0);
		content.setLayoutParams(lp);
		return content;
	}

	private TextView genLegend() {
		TextView legend = new TextView(getContext());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		legend.setLayoutParams(lp);
		legend.setPadding(legendPadding, legendPadding, legendPadding,
				legendPadding);
		return legend;
	}

	public void setText(String text) {
	}

}
