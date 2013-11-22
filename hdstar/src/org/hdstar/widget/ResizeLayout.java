package org.hdstar.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 尺寸发生改变时，触发回调，用于监听软键盘的弹出关闭
 * 
 * @author robust
 * 
 */
public class ResizeLayout extends LinearLayout {
	private OnResizeListener mListener;

	public interface OnResizeListener {
		void OnResize(int w, int h, int oldw, int oldh);
	}

	public void setOnResizeListener(OnResizeListener l) {
		mListener = l;
	}

	public ResizeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mListener != null) {
			mListener.OnResize(w, h, oldw, oldh);
		}
	}

}
