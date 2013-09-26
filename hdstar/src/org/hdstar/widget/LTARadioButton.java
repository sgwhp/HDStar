package org.hdstar.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class LTARadioButton extends RadioButton {
	private LTAController ltaController;

	public LTARadioButton(Context context) {
		super(context);
		ltaController = new LTAController();
	}

	public LTARadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		ltaController = new LTAController(context, attrs);
	}

	public LTARadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ltaController = new LTAController(context, attrs);
	}

	/**
	 * ����DrawAllocation��ֻ�и������λ�úʹ�С�����ı������Ż���ڴ�����������������Ƶ�������ģ� �ʶ�UI����Ӱ�첻��
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		ltaController.delegateTouch(this, bottom, left, right, top);
	}
}
