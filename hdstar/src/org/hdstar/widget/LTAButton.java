package org.hdstar.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * 
 * @author robust
 * 
 */
public class LTAButton extends Button {
	private LTAController ltaController;

	public LTAButton(Context context) {
		super(context);
		ltaController = new LTAController();
	}

	public LTAButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		ltaController = new LTAController(context, attrs);
	}

	public LTAButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ltaController = new LTAController(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		ltaController.delegateTouch(this, bottom, left, right, top);
	}
}
