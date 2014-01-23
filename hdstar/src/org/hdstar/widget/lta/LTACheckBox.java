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
