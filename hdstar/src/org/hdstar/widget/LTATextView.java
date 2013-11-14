package org.hdstar.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class LTATextView extends TextView {

	private LTAController ltaController;

	public LTATextView(Context context) {
		super(context);
		ltaController = new LTAController();
	}

	public LTATextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ltaController = new LTAController(context, attrs);
	}

	public LTATextView(Context context, AttributeSet attrs, int defStyle) {
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
