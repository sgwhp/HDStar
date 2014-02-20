package org.hdstar.widget.navigation;

import org.hdstar.R;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LargeFilterSeparatorView extends FrameLayout {

	protected String text;

	protected TextView separatorText;
	private boolean mAlreadyInflated = false;

	public LargeFilterSeparatorView(Context context) {
		super(context);
	}

	/**
	 * Sets the text that will be shown in this separator (sub header)
	 * 
	 * @param text
	 *            The new text to show
	 * @return Itself, for convenience of method chaining
	 */
	public LargeFilterSeparatorView setText(String text) {
		separatorText.setText(text);
		setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.WRAP_CONTENT,
				AbsListView.LayoutParams.WRAP_CONTENT));
		return this;
	}

	/**
	 * The mAlreadyInflated_ hack is needed because of an Android bug which
	 * leads to infinite calls of onFinishInflate() when inflating a layout with
	 * a parent and using the <merge /> tag.
	 * 
	 */
	@Override
	public void onFinishInflate() {
		if (!mAlreadyInflated) {
			mAlreadyInflated = true;
			inflate(getContext(), R.layout.large_list_item_separator, this);
			separatorText = ((TextView) findViewById(R.id.large_separator_text));
		}
		super.onFinishInflate();
	}

	public static LargeFilterSeparatorView build(Context context) {
		LargeFilterSeparatorView instance = new LargeFilterSeparatorView(context);
		instance.onFinishInflate();
		return instance;
	}

}
