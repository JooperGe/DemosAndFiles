package com.viash.voice_assistant.common;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class WifiLocation {
	private static final String TAG = "WifiLocation";
	private LocationClient mLocationClient;
	private WifiLocationListenner mListener;
	private Point mPoint = null;
	private BDLocation mLocation = null;
	private static WifiLocation mInstance = null;
	private WifiLocation (Context context) {
		init(context);
	}
	
	public static WifiLocation getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new WifiLocation(context);
		}
		return mInstance;
	}
	
	private void init(Context context) {
		close();
		mLocationClient = new LocationClient(context);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(false);
		option.setCoorType("gcj02");
		option.setLocationMode(LocationMode.Battery_Saving);
		option.setScanSpan(2 * 60 * 1000);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		mListener = new WifiLocationListenner();
		mLocationClient.registerLocationListener(mListener);
		mLocationClient.requestLocation();
	}
	

	public Point getLocation()
	{
		return mPoint;
	}
	
	public BDLocation getBDLocation()
	{
		return mLocation;
	}
	
	public JSONObject getJsonOfLocation()
	{
		JSONObject obj = new JSONObject();
		if(mLocation != null)
		{
			try {
				obj.put("longitude", (int)(mLocation.getLongitude() * 1E6));			
				obj.put("latitude", (int)(mLocation.getLatitude() * 1E6));
				if(mLocation.getProvince() != null)
					obj.put("province", mLocation.getProvince());
				
				if(mLocation.getCity() != null)
					obj.put("city", mLocation.getCity());
				
				if(mLocation.getDistrict() != null)
					obj.put("district", mLocation.getDistrict());
				
				if(mLocation.getStreet() != null)
					obj.put("street", mLocation.getStreet());
				
				if(mLocation.getStreetNumber() != null)
					obj.put("number", mLocation.getStreetNumber());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return obj;
	}
	
	public void close()
	{
		if(mListener != null)
		{
			mLocationClient.unRegisterLocationListener(mListener);
			mListener = null;
		}
		
		if(mLocationClient != null)
		{
			mLocationClient.stop();
			mLocationClient = null;
		}
	}
	
	public class WifiLocationListenner implements BDLocationListener 
	{	
		
		@Override	
		public void onReceiveLocation(BDLocation location) 
		{		
			if (location != null)			
			{
				if(mPoint == null)
					mPoint = new Point();
				if(location != null)
					mLocation = location;

				mPoint.set((int)(location.getLongitude() * 1E6), (int)(location.getLatitude() * 1E6));
				Log.i(TAG, mPoint.toString());
					
			}
		}
		
		public void onReceivePoi(BDLocation poiLocation) 
		{			
			
		}
	}
}