package org.hdstar.widget.lta;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

public interface ILTAViewGroup {
	public void addTouchDelegate(TouchDelegate delegate);
	
	public void removeDelegate(TouchDelegate delegate);

	public void addTouchDelegate(Rect rect, View delegateView);
}
