package org.hdstar.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LTALinearLayout extends LinearLayout implements ILTAViewGroup {

	private TouchDelegateGroup mTouchDelegateGroup;

	private int mPreviousWidth = -1;
	private int mPreviousHeight = -1;

	public LTALinearLayout(Context context) {
		super(context);
		init(context);
	}

	public LTALinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		mTouchDelegateGroup = new TouchDelegateGroup(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;

		if (width != mPreviousWidth || height != mPreviousHeight) {

			mPreviousWidth = width;
			mPreviousHeight = height;

			mTouchDelegateGroup.clearTouchDelegates();

			super.setTouchDelegate(mTouchDelegateGroup);
		}
		
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	public void setTouchDelegate(TouchDelegate delegate) {
		addTouchDelegate(delegate);
	}
	
	@Override
	public void addTouchDelegate(TouchDelegate delegate){
		mTouchDelegateGroup.addTouchDelegate(delegate);
	}

	@Override
	public void removeDelegate(TouchDelegate delegate){
		mTouchDelegateGroup.removeTouchDelegate(delegate);
	}

	@Override
	public void addTouchDelegate(Rect rect, View delegateView) {
		mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(rect,
				delegateView));
	}
}
