package org.hdstar.model;

import java.io.Serializable;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class RssItem implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String title;
	public String link;
	public String description;
	public Date pubDate;
	public String enclosureUrl;
	public String enclosureType;
	public long enclosureLength;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(id);
		out.writeString(title);
		out.writeString(link);
		out.writeString(description);
		out.writeLong(pubDate == null ? -1 : pubDate.getTime());
		out.writeString(enclosureUrl);
		out.writeString(enclosureType);
		out.writeLong(enclosureLength);
	}

	public String getTheLink() {
		if (this.enclosureUrl != null) {
			return this.enclosureUrl;
		} else {
			return this.link;
		}
	}

	public static final Parcelable.Creator<RssItem> CREATOR = new Parcelable.Creator<RssItem>() {
		public RssItem createFromParcel(Parcel in) {
			return new RssItem(in);
		}

		public RssItem[] newArray(int size) {
			return new RssItem[size];
		}
	};

	public RssItem() {
	}

	private RssItem(Parcel in) {
		id = in.readInt();
		title = in.readString();
		link = in.readString();
		description = in.readString();
		long pubDateIn = in.readLong();
		pubDate = pubDateIn == -1 ? null : new Date(pubDateIn);
		enclosureUrl = in.readString();
		enclosureType = in.readString();
		enclosureLength = in.readLong();
	}

}
