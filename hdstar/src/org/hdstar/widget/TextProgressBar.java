package org.hdstar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import org.hdstar.util.Util;

public class TextProgressBar extends ProgressBar {
	private String text;
	private Paint mPaint;
	private Rect rect = new Rect();
	private static final float DEFAULT_TEXT_SIZE = 16.0f;
    private float textSize;

	public TextProgressBar(Context context) {
		super(context);
        initText();
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initText();
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initText();
	}

	@Override
	public synchronized void setProgress(int progress) {
		setText(progress);
		super.setProgress(progress);
		invalidate();
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// this.setText();
		this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		mPaint.setTextSize(textSize);
        mPaint.setFakeBoldText(true);
		int x = (getWidth() / 2) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(this.text, x, y, this.mPaint);
	}

	// 初始化，画笔
	private void initText() {
        textSize = Util.dip2px(getContext(), DEFAULT_TEXT_SIZE);
		this.mPaint = new Paint();
		this.mPaint.setColor(Color.WHITE);

	}

	// 设置文字内容
	private void setText(int progress) {
		int i = (int) (progress * 100.0 / this.getMax());
		this.text = String.valueOf(i) + "%";
	}
}
