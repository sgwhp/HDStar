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
	 * ���ô���ί��
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
	 * ���ö���Ĵ�����Χ
	 * 
	 * @param addition
	 *            ��Ҫ���ӵĴ�С����λpx
	 */
	public void setAddition(int addition) {
		mTouchAdditionBottom = addition;
		mTouchAdditionLeft = addition;
		mTouchAdditionRight = addition;
		mTouchAdditionTop = addition;
	}

	/**
	 * ���õײ��������Ĵ�����Χ
	 * 
	 * @param additionBottom
	 *            ��Ҫ���ӵĴ�С����λpx
	 */
	public void setAdditionBottom(int additionBottom) {
		mTouchAdditionBottom = additionBottom;
	}

	/**
	 * ��������������Ĵ�����Χ
	 * 
	 * @param additionLeft
	 *            ��Ҫ���ӵĴ�С����λpx
	 */
	public void setAdditionLeft(int additionLeft) {
		mTouchAdditionLeft = additionLeft;
	}

	/**
	 * �����ұ��������Ĵ�����Χ
	 * 
	 * @param additionRight
	 *            ��Ҫ���ӵĴ�С����λpx
	 */
	public void setAdditionRight(int additionRight) {
		mTouchAdditionRight = additionRight;
	}

	/**
	 * ���ö����������Ĵ�����Χ
	 * 
	 * @param additionTop
	 *            ��Ҫ���ӵĴ�С����λpx
	 */
	public void setAdditionTop(int additionTop) {
		mTouchAdditionTop = additionTop;
	}
}
