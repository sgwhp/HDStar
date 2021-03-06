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
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * View that represents some {@link SimpleListItem} object specifically used to
 * represent a navigation filter item.
 * 
 * @author Eric Kok
 */
public class FilterListItemView extends FrameLayout {

	protected TextView itemText;
	private boolean mAlreadyInflated = false;

	public FilterListItemView(Context context) {
		super(context);
	}

	public void bind(SimpleListItem filterItem) {
		itemText.setText(filterItem.getName());
	}
	
	public void bind(CharSequence name){
		itemText.setText(name);
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
			inflate(getContext(), R.layout.list_item_filter, this);
			itemText = ((TextView) findViewById(R.id.item_text));
		}
		super.onFinishInflate();
	}

	public static FilterListItemView build(Context context) {
		FilterListItemView instance = new FilterListItemView(context);
		instance.onFinishInflate();
		return instance;
	}

}
