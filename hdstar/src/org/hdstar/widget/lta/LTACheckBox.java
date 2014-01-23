package org.hdstar.widget.lta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * 
 * @author robust
 * 
 */
public class LTACheckBox extends CheckBox {
	private LTAController ltaController;

	public LTACheckBox(Context context) {
		super(context);
		ltaController = new LTAController();
	}

	public LTACheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		ltaController = new LTAController(context, attrs);
	}

	public LTACheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ltaController = new LTAController(context, attrs);
	}

	/**
	 * 关于DrawAllocation，只有该组件的位置和大小发生改变的情况才会存在创建对象的情况，不是频繁发生的， 故对UI性能影响不大
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		ltaController.delegateTouch(this, bottom, left, right, top);
	}
}
