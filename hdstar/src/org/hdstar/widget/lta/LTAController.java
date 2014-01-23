package org.hdstar.widget.lta;

import org.hdstar.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;

class LTAController {
	private static final int TOUCH_ADDITION = 20;
	private int mTouchAdditionBottom = 0;
	private int mTouchAdditionLeft = 0;
	private int mTouchAdditionRight = 0;
	private int mTouchAdditionTop = 0;
	private int mPreviousLeft = -1;
	private int mPreviousRight = -1;
	private int mPreviousBottom = -1;
	private int mPreviousTop = -1;
	private TouchDelegate mDelegate;

	LTAController() {
	}

	LTAController(Context context, AttributeSet attrs) {
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.LargeTouchableAreaView);
		int addition = (int) a.getDimension(
				R.styleable.LargeTouchableAreaView_addition, TOUCH_ADDITION);
		mTouchAdditionBottom = addition;
		mTouchAdditionLeft = addition;
		mTouchAdditionRight = addition;
		mTouchAdditionTop = addition;
		mTouchAdditionBottom = (int) a.getDimension(
				R.styleable.LargeTouchableAreaView_additionBottom,
				mTouchAdditionBottom);
		mTouchAdditionLeft = (int) a.getDimension(
				R.styleable.LargeTouchableAreaView_additionLeft,
				mTouchAdditionLeft);
		mTouchAdditionRight = (int) a.getDimension(
				R.styleable.LargeTouchableAreaView_additionRight,
				mTouchAdditionRight);
		mTouchAdditionTop = (int) a.getDimension(
				R.styleable.LargeTouchableAreaView_additionTop,
				mTouchAdditionTop);
		a.recycle();
	}

	/**
	 * 设置触摸委托
	 * 
	 * @param v
	 * @param bottom
	 * @param left
	 * @param right
	 * @param top
	 */
	void delegateTouch(View v, int bottom, int left, int right, int top) {
		if (left != mPreviousLeft || top != mPreviousTop
				|| right != mPreviousRight || bottom != mPreviousBottom) {
			mPreviousLeft = left;
			mPreviousTop = top;
			mPreviousRight = right;
			mPreviousBottom = bottom;
			final View parent = (View) v.getParent();
			if(parent instanceof ILTAViewGroup){
				((ILTAViewGroup) parent).removeDelegate(mDelegate);
			}
			mDelegate = new TouchDelegate(new Rect(left
					- mTouchAdditionLeft, top - mTouchAdditionTop, right
					+ mTouchAdditionRight, bottom + mTouchAdditionBottom), v);
			parent.setTouchDelegate(mDelegate);
		}
	}

	/**
	 * 设置额外的触摸范围
	 * 
	 * @param addition
	 *            需要增加的大小，单位px
	 */
	public void setAddition(int addition) {
		mTouchAdditionBottom = addition;
		mTouchAdditionLeft = addition;
		mTouchAdditionRight = addition;
		mTouchAdditionTop = addition;
	}

	/**
	 * 设置底部区域额外的触摸范围
	 * 
	 * @param additionBottom
	 *            需要增加的大小，单位px
	 */
	public void setAdditionBottom(int additionBottom) {
		mTouchAdditionBottom = additionBottom;
	}

	/**
	 * 设置左边区域额外的触摸范围
	 * 
	 * @param additionLeft
	 *            需要增加的大小，单位px
	 */
	public void setAdditionLeft(int additionLeft) {
		mTouchAdditionLeft = additionLeft;
	}

	/**
	 * 设置右边区域额外的触摸范围
	 * 
	 * @param additionRight
	 *            需要增加的大小，单位px
	 */
	public void setAdditionRight(int additionRight) {
		mTouchAdditionRight = additionRight;
	}

	/**
	 * 设置顶部区域额外的触摸范围
	 * 
	 * @param additionTop
	 *            需要增加的大小，单位px
	 */
	public void setAdditionTop(int additionTop) {
		mTouchAdditionTop = additionTop;
	}
}
