package org.hdstar.util;

import org.hdstar.common.CommonUrls;
import org.hdstar.widget.fragment.ForumFragment;
import org.hdstar.widget.fragment.PMFragment;
import org.hdstar.widget.fragment.StackFragment;
import org.hdstar.widget.fragment.TopicFragment;

import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;

/**
 * 超链接跳转处理
 * 
 * @author robust
 * 
 */
public class CustomLinkMovementMethod extends LinkMovementMethod {
	private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();
	private static StackFragment mFragment;

	public boolean onTouchEvent(android.widget.TextView widget,
			android.text.Spannable buffer, android.view.MotionEvent event) {
		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
			if (link.length != 0) {
				StackFragment f;
				String url = link[0].getURL();
				if (url.startsWith(CommonUrls.HDStar.VIEW_FORUM_BASE_URL)) {
					f = ForumFragment.newInstance(url);
					if (f != null) {
						mFragment.push(f);
					}
				} else if (url
						.startsWith(CommonUrls.HDStar.VIEW_TOPIC_BASE_URL)) {
					f = TopicFragment.newInstance(url);
					if (f != null) {
						mFragment.push(f);
					}
				} else if (url.startsWith(CommonUrls.HDStar.SEND_PM_URL)) {
					f = PMFragment.newInstance(url);
					if (f != null) {
						mFragment.push(f);
					}
				}
				// else if (url.contains("tel"))
				// {
				// } else if (url.contains("mailto"))
				// {
				// }
				return true;
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}

	public static void attach(StackFragment stackFragment) {
		if (mFragment == null) {
			mFragment = stackFragment;
		}
	}

	public static void detach() {
		mFragment = null;
	}

	public static android.text.method.MovementMethod getInstance() {
		return linkMovementMethod;
	}
}
