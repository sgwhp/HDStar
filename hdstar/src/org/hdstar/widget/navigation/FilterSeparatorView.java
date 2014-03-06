/* 
 * Copyright 2010-2013 Eric Kok et al.
 * 
 * Transdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Transdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hdstar.widget.navigation;

import org.hdstar.R;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * A list item that shows a sub header or separator (in underlined Holo style).
 * 
 * @author Eric Kok
 */
public class FilterSeparatorView extends FrameLayout {

	protected String text;

	protected TextView separatorText;
	private boolean mAlreadyInflated = false;

	public FilterSeparatorView(Context context) {
		super(context);
	}

	/**
	 * Sets the text that will be shown in this separator (sub header)
	 * 
	 * @param text
	 *            The new text to show
	 * @return Itself, for convenience of method chaining
	 */
	public FilterSeparatorView setText(String text) {
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
			inflate(getContext(), R.layout.list_item_separator, this);
			separatorText = ((TextView) findViewById(R.id.separator_text));
		}
		super.onFinishInflate();
	}

	public static FilterSeparatorView build(Context context) {
		FilterSeparatorView instance = new FilterSeparatorView(context);
		instance.onFinishInflate();
		return instance;
	}

}
