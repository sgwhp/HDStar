package org.hdstar.widget;

import org.hdstar.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class CustomDialog extends Dialog {

	private static int default_width = 160; // 默认宽度
	private static int default_height = 120;// 默认高度
	
	public CustomDialog(Context context){
		this(context, default_width, default_height, R.layout.loading_dialog,
				R.style.dialog_theme, null, true);
	}
	public CustomDialog(Context context, int msgId, boolean cancelable){
		this(context, R.layout.loading_dialog, R.style.dialog_theme, msgId, cancelable);
	}

	public CustomDialog(Context context, int layout, int style, String msg, boolean cancelable) {
		this(context, default_width, default_height, layout, style, msg, cancelable);
	}

	public CustomDialog(Context context, int layout, int style, int msgId, boolean cancelable) {
		this(context, default_width, default_height, layout, style, context
				.getResources().getString(msgId), cancelable);
	}

	public CustomDialog(Context context, int width, int height, int layout,
			int style, String msg, boolean cancelable) {
		super(context, style);
		// set content
		setContentView(layout);
		if(msg != null) ((TextView) findViewById(R.id.message)).setText(msg);
		// set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		float density = getDensity(context);
		params.width = (int) (width * density);
		params.height = (int) (height * density);
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
        if(!cancelable){
            this.setCancelable(false);
            this.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    switch (keyCode){
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            break;
                    }
                    return false;
                }
            });
        }
	}

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

}
