package com.viash.voice_assistant.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationActionData implements Parcelable {
	public static final int ACT_TYPE_ALARM = 0;
	//protected int mLongitude1;
	//protected int mLatitude1;
	//protected int mLongitude2;
	//protected int mLatitude2;
	protected int mLongitude;
	protected int mLatitude;
	protected int mLongitudeRange;
	protected int mLatitudeRange;
	protected String mPositionName;
	protected int mActionType;
	protected String mAlarmTitle;
	
	protected int alarming = 0;
	
	public static final Parcelable.Creator<LocationActionData> CREATOR = new Parcelable.Creator<LocationActionData>() {

		@Override
		public LocationActionData createFromParcel(Parcel source) {
			return new LocationActionData(source);
		}

		@Override
		public LocationActionData[] newArray(int size) {
			return new LocationActionData[size];
		}

	};

	public LocationActionData(Parcel source) {
		//mLongitude1 = source.readInt();
		//mLatitude1 = source.readInt();
		//mLongitude2 = source.readInt();
		//mLatitude2 = source.readInt();
		mLongitude = source.readInt();
		mLatitude = source.readInt();
		mLongitudeRange = source.readInt();
		mLatitudeRange = source.readInt();
		mPositionName = source.readString();
		mActionType = source.readInt();
		if (mActionType == ACT_TYPE_ALARM)
			mAlarmTitle = source.readString();
		
		alarming = 0;
	}

	public int getmLongitude() {
		return mLongitude;
	}
	
	public int getmLatitude() {
		return mLatitude;
	}
	
	public int getmLongitudeRange() {
		return mLongitudeRange;
	}
	
	public int getmLatitudeRange() {
		return mLatitudeRange;
	}
	
	public String getmPositionName() {
		return mPositionName;
	}
	
	public int getmLongitude1() {
		return mLongitude - mLongitudeRange;
	}

	public int getmLatitude1() {
		return mLatitude - mLatitudeRange;
	}

	public int getmLongitude2() {
		return mLongitude + mLongitudeRange;
	}

	public int getmLatitude2() {
		return mLatitude + mLatitudeRange;
	}
	
	
	public int getmActionType() {
		return mActionType;
	}

	public String getmAlarmTitle() {
		return mAlarmTitle;
	}

	public int isAlarm(){
		return alarming;
	}
	
	public void setAlarming(int isAlarm){
		alarming = isAlarm;
	}
	
	public void setAlarmAction(int longitude, int latitude, int longitudeRange,
			int latitudeRange, String position_name, String title) {
		mLongitude = longitude;
		mLatitude = latitude;
		mLongitudeRange = longitudeRange;
		mLatitudeRange = latitudeRange;
		mPositionName = position_name;
		mAlarmTitle = title;
		mActionType = ACT_TYPE_ALARM;
	}
	/*
	public void setAlarmAction(int longitude1, int latitude1, int longitude2,
			int latitude2, String title) {
		mLongitude1 = longitude1;
		mLatitude1 = latitude1;
		mLongitude2 = longitude2;
		mLatitude2 = latitude2;
		mAlarmTitle = title;
		mActionType = ACT_TYPE_ALARM;
	}
	*/
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeInt(mLongitude1);
		//dest.writeInt(mLatitude1);
		//dest.writeInt(mLongitude2);
		//dest.writeInt(mLatitude2);
		dest.writeInt(mLongitude);
		dest.writeInt(mLatitude);
		dest.writeInt(mLongitudeRange);
		dest.writeInt(mLatitudeRange);
		dest.writeString(mPositionName);
		dest.writeInt(mActionType);
		if (mActionType == ACT_TYPE_ALARM)
			dest.writeString(mAlarmTitle);
	}

	public JSONObject toJsonObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("longitude", mLongitude);
			obj.put("latitude", mLatitude);
			obj.put("longitude_range", mLongitudeRange);
			obj.put("latitude_range", mLatitudeRange);
			if(mAlarmTitle != null)
				obj.put("alarm_title", mAlarmTitle);
			if(mPositionName != null)
				obj.put("position_name", mPositionName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
