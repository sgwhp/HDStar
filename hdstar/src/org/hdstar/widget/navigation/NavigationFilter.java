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

import org.hdstar.model.RemoteTaskInfo;

import android.os.Parcelable;

/**
 * Represents a filter, used in the app navigation, that can check if some
 * torrent matches the user-set filter
 * 
 * @author Eric Kok
 */
public interface NavigationFilter extends Parcelable {

	/**
	 * Implementations should check if the supplied torrent matches the filter;
	 * for example a label filter should return true if the torrent's label
	 * equals this items label name.
	 * 
	 * @param torrent
	 *            The torrent to check for matches
	 * @param dormantAsInactive
	 *            If true, dormant (0KB/s, so no data transfer) torrents are
	 *            never actively downloading or seeding
	 * @return True if the torrent matches the filter and should be shown in the
	 *         current screen, false otherwise
	 */
	boolean matches(RemoteTaskInfo torrent, boolean dormantAsInactive);

	/**
	 * Implementations should return a name that can be shown to indicate the
	 * active filter
	 * 
	 * @return The name of the filter item as string
	 */
	String getName();

}
