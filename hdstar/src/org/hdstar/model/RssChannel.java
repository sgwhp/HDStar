package org.hdstar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class RssChannel implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String title;
	public String link;
	public String description;
	public Date pubDate;
	public long lastBuildDate;
	public List<String> categories;
	public List<RssItem> items;
	public String image;

	public RssChannel() {
		this.categories = new ArrayList<String>();
		this.items = new ArrayList<RssItem>();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(title);
		out.writeString(link);
		out.writeString(description);
		out.writeLong(pubDate == null ? -1 : pubDate.getTime());
		out.writeLong(lastBuildDate);
		out.writeTypedList(items);
		out.writeStringList(categories);
		out.writeString(image);
	}

	public static final Parcelable.Creator<RssChannel> CREATOR = new Parcelable.Creator<RssChannel>() {
		public RssChannel createFromParcel(Parcel in) {
			return new RssChannel(in);
		}

		public RssChannel[] newArray(int size) {
			return new RssChannel[size];
		}
	};

	private RssChannel(Parcel in) {
		this();
		id = in.readInt();
		title = in.readString();
		link = in.readString();
		description = in.readString();
		long pubDateIn = in.readLong();
		pubDate = pubDateIn == -1 ? null : new Date(pubDateIn);
		lastBuildDate = in.readLong();
		categories = new ArrayList<String>();
		in.readTypedList(items, RssItem.CREATOR);
		in.readStringList(categories);
		image = in.readString();
	}

}
