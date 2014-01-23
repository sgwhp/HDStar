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

import java.util.List;

import org.hdstar.R;
import org.hdstar.model.RemoteSetting;
import org.hdstar.widget.adapter.MergeAdapter;
import org.hdstar.widget.adapter.ViewHolderAdapter;
import org.hdstar.widget.navigation.StatusType.StatusTypeFilter;

import android.content.Context;
import android.view.View;

/**
 * List adapter that holds filter items, that is, servers, view types and
 * labels. A header item is inserted where appropriate.
 * 
 * @author Eric Kok <br/>
 *         edit by robust
 */
public class FilterListAdapter extends MergeAdapter {

	protected Context context;
	private FilterListItemAdapter serverItems = null;
	private FilterListItemAdapter statusTypeItems = null;
	private FilterListItemAdapter labelItems = null;
	protected ViewHolderAdapter statusTypeSeparator;
	protected ViewHolderAdapter labelSeperator;
	protected ViewHolderAdapter serverSeparator;

	public FilterListAdapter(Context context) {
		this.context = context;
	}

	/**
	 * Update the list of available servers
	 * 
	 * @param servers
	 *            The new list of available servers
	 */
	public void updateServers(List<RemoteSetting> servers) {
		if (this.serverItems == null && servers != null) {
			serverSeparator = new ViewHolderAdapter(FilterSeparatorView.build(
					context).setText(context.getString(R.string.remote_server)));
			serverSeparator.setViewVisibility(servers.isEmpty() ? View.GONE
					: View.VISIBLE);
			addAdapter(serverSeparator);
			this.serverItems = new FilterListItemAdapter(context, servers);
			addAdapter(serverItems);
		} else if (this.serverItems != null && servers != null) {
			serverSeparator.setViewVisibility(servers.isEmpty() ? View.GONE
					: View.VISIBLE);
			this.serverItems.update(servers);
		} else {
			serverSeparator.setViewVisibility(View.GONE);
			this.serverItems = null;
		}
	}

	/**
	 * Update the list of available status types
	 * 
	 * @param statusTypes
	 *            The new list of available status types
	 */
	public void updateStatusTypes(List<StatusTypeFilter> statusTypes) {
		if (this.statusTypeItems == null && statusTypes != null) {
			statusTypeSeparator = new ViewHolderAdapter(FilterSeparatorView
					.build(context).setText(context.getString(R.string.status)));
			statusTypeSeparator
					.setViewVisibility(statusTypes.isEmpty() ? View.GONE
							: View.VISIBLE);
			addAdapter(statusTypeSeparator);
			this.statusTypeItems = new FilterListItemAdapter(context,
					statusTypes);
			addAdapter(statusTypeItems);
		} else if (this.statusTypeItems != null && statusTypes != null) {
			statusTypeSeparator
					.setViewVisibility(statusTypes.isEmpty() ? View.GONE
							: View.VISIBLE);
			this.statusTypeItems.update(statusTypes);
		} else {
			statusTypeSeparator.setViewVisibility(View.GONE);
			this.statusTypeItems = null;
		}
	}

	/**
	 * Update the list of available labels
	 * 
	 * @param labels
	 *            The new list of available labels
	 */
	public void updateLabels(List<Label> labels) {
		if (this.labelItems == null && labels != null) {
			labelSeperator = new ViewHolderAdapter(FilterSeparatorView.build(
					context).setText(context.getString(R.string.label)));
			labelSeperator.setViewVisibility(labels.isEmpty() ? View.GONE
					: View.VISIBLE);
			addAdapter(labelSeperator);
			this.labelItems = new FilterListItemAdapter(context, labels);
			addAdapter(labelItems);
		} else if (this.labelItems != null && labels != null) {
			labelSeperator.setViewVisibility(labels.isEmpty() ? View.GONE
					: View.VISIBLE);
			this.labelItems.update(labels);
		} else {
			labelSeperator.setViewVisibility(View.GONE);
			this.labelItems = null;
		}
	}

}
