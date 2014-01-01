package org.hdstar.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RssSetting implements Parcelable{
	public int order;
	public String label;
	public String link;
	
	public RssSetting(){}
	
	public RssSetting(Parcel in){
		order = in.readInt();
		label = in.readString();
		link = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(order);
		dest.writeString(label);
		dest.writeString(link);
	}
	
	public static final Parcelable.Creator<RssSetting> CREATOR = new Parcelable.Creator<RssSetting>() {

		@Override
		public RssSetting createFromParcel(Parcel source) {
			return new RssSetting(source);
		}

		@Override
		public RssSetting[] newArray(int size) {
			return new RssSetting[size];
		}
	};

}
