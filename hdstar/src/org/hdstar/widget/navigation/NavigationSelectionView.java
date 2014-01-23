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
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * View that displays the user-selected server and display filter inside the
 * action bar list navigation spinner
 * 
 * @author Eric Kok <br/>
 *         edit by robust
 */
public class NavigationSelectionView extends LinearLayout {

	protected TextView filterText;
	protected TextView serverText;
	private boolean mAlreadyInflated = false;

	public NavigationSelectionView(Context context) {
		super(context);
	}

	/**
	 * Binds the names of the current connected server and selected filter to
	 * this navigation view.
	 * 
	 * @param currentServer
	 *            The name of the server currently connected to
	 * @param currentFilter
	 *            The name of the filter that is currently selected
	 */
	public void bind(String currentServer, String currentFilter) {
		serverText.setText(currentServer);
		filterText.setText(currentFilter);
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
			inflate(getContext(), R.layout.actionbar_navigation, this);
			filterText = ((TextView) findViewById(R.id.filter_text));
			serverText = ((TextView) findViewById(R.id.server_text));
		}
		super.onFinishInflate();
	}

	public static NavigationSelectionView build(Context context) {
		NavigationSelectionView instance = new NavigationSelectionView(context);
		instance.onFinishInflate();
		return instance;
	}

}
