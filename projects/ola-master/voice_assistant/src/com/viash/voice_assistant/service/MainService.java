package com.viash.voice_assistant.service;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.viash.voice_assistant.activity.NotificationActivity;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voicelib.utils.HttpUtil;

public class MainService extends Service {
	private static final boolean TESTING = false;
	private static final boolean DEBUG = true;
	private static final String TAG = "MainService";
	
	private List<LocationActionData> mLocationActionData = new ArrayList<LocationActionData>();
	private WifiLocation wifiLocation = null;
	private Geocoder geocoder = null;
	
	// location
	private Timer getLocationTimer = null;
	private static final int GET_LOCATION_DELAY_TIME = 30000;

	// weather
	private static final String WEATHER_PRE_URL = "http://m.weather.com.cn/data/";
	private Timer getWeatherTimer = null;
	private static final int GET_WEATHER_DELAY_TIME = 60 * 60 * 1000;
	
	@Override
	public void onCreate() {
		if(DEBUG) Log.d(TAG, "onCreate");

		if(wifiLocation == null){
			wifiLocation = WifiLocation.getInstance(this.getApplicationContext());
			//wifiLocation.init(this);
		}
		if(geocoder == null){
			geocoder = new Geocoder(this);
		}
		if(getLocationTimer == null){
			getLocationTimer = new Timer();
			getLocationTimer.schedule(new GetLocationTask(), 1000, GET_LOCATION_DELAY_TIME);
		}
		if(getWeatherTimer == null){
			getLocationTimer = new Timer();
			getLocationTimer.schedule(new GetWeatherTask(), 1000, GET_WEATHER_DELAY_TIME);
		}
		
		if(TESTING) test();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new IMainServiceStub(this);
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		if(DEBUG) Log.d(TAG, "onUnbind");
		/*
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		wifiLocation.close();
		*/
		return super.onUnbind(intent);
	}

	private boolean addLocationAlert(LocationActionData action) {
		mLocationActionData.add(action);
		return true;
	}

	private List<LocationActionData> queryLocationAlert() {
		return mLocationActionData;
	}
	
	public boolean deleteLocationAlert(int[] ids) {
		boolean ret = false;
		if(ids.length > 0){
			// Sequence
			for(int i = 0 ; i < ids.length - 1 ; i++){
				for(int j = i + 1 ; j < ids.length ; j++){
					if(ids[i] < ids[j]){
						int temp = ids[i];
						ids[i] = ids[j];
						ids[j] = temp;
					}
				}
			}
			
			// start to delete
			for(int i: ids){
				if(i < mLocationActionData.size()){
					if(DEBUG) Log.i(TAG, "deleteLocationAlert: " + i);
					mLocationActionData.remove(ids[i]);
				}else{
					if(DEBUG) Log.e(TAG, "deleteLocationAlert: i > mLocationActionData.size()");
					return false;
				}
			}
			ret = true;
		}
		return ret;
	}

	static class IMainServiceStub extends IMainService.Stub {
		WeakReference<MainService> service;

		IMainServiceStub(MainService service) {
			this.service = new WeakReference<MainService>(service);
		}

		@Override
		public boolean addLocationAlert(LocationActionData action)
				throws RemoteException {
			return this.service.get().addLocationAlert(action);
		}

		@Override
		public List<LocationActionData> queryLocationAlert()
				throws RemoteException {
			return this.service.get().queryLocationAlert();
		}

		@Override
		public void trigger(String param) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean deleteLocationAlert(int[] ids) throws RemoteException {
			return this.service.get().deleteLocationAlert(ids);
		}

	}

	class GetWeatherTask extends TimerTask{
		@Override
		public void run() {
			// check
			Point pt = wifiLocation.getLocation();
			if(pt != null){
				if(DEBUG) Log.d(TAG, "Point: " + pt.x + "," + pt.y);
				detectWeatherJob(pt);
			}
		}
	}
	
	class GetLocationTask extends TimerTask{
		@Override
		public void run() {
			Point pt = wifiLocation.getLocation();
			if(pt != null){
				if(DEBUG) Log.d(TAG, "Point: " + pt.x + "," + pt.y);
				detectLocationJob(pt);
			}
		}
	}
	
	// ========================== detect weather job ==========================
	private void detectWeatherJob(Point pt) {
		// get location
		try {
			List<Address> location = geocoder.getFromLocation(pt.y / 1000000.0, pt.x / 1000000.0, 1);
			if(location != null && location.size() > 0){
				if(DEBUG) Log.d(TAG, "Locality: " + location.get(0).getLocality() + ", SubLocality: " + location.get(0).getSubLocality());
				// get weather id
				String weatherId = getWeatherId(location.get(0).getLocality(), location.get(0).getSubLocality());
				if(weatherId != null){
					if(DEBUG) Log.d(TAG, "weather id: " + weatherId);
					getWeather(weatherId);
				}else{
					if(DEBUG) Log.d(TAG, "weather id = -1");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void getWeather(String weatherId) {
		String url = WEATHER_PRE_URL + weatherId + ".html";
		String result = HttpUtil.sendGetCommand(this, url);
		if(result != null){
			if(DEBUG) Log.d(TAG, result);
			try {
				JSONObject obj = new JSONObject(result).getJSONObject("weatherinfo");
				if(DEBUG) Log.d("TAG", "temp: " + obj.getString("temp1"));
				
				// do something
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	private String getWeatherId(String locality, String subLocality) {
		String id = null;
		if(TESTING){
			id = "101021300";
		}else{
			//subLocality = "浦东";
			if(GlobalData.getWeatherDatabase() != null){
				// find weather id from weather database
				SQLiteDatabase weatherDB = SQLiteDatabase.openOrCreateDatabase(GlobalData.getWeatherDatabase(), null);
				
				String table = "weathers";
				String[] columns = {"city_name", "area_name", "weather_id"};
				String selection = "`area_name` like '" + subLocality + "'";
				
				Cursor cursor = weatherDB.query(table, columns , selection , null, null, null, null);
				int rows_num = cursor.getCount();
				if(rows_num != 0){
					cursor.moveToFirst();
					id = cursor.getString(2);
					if(DEBUG) Log.d(TAG, "find weaher id: " + id);
				}else{
					locality = locality.replaceAll("市", "");
					if(locality != null){
						selection = "`area_name` like '" + locality + "'";
						
						cursor = weatherDB.query(table, columns , selection , null, null, null, null);
						rows_num = cursor.getCount();
						if(rows_num != 0){
							cursor.moveToFirst();
							id = cursor.getString(2);
							if(DEBUG) Log.d(TAG, "find weaher id: " + id);
						}
					}
				}
				weatherDB.close();
			}
		}
		return id;
	}

	// ========================== detect location job ==========================
	private void detectLocationJob(Point pt) {
		List<Integer> area = detectLocation(pt);
		if(area.size() != 0){
			Intent intent = new Intent(MainService.this, NotificationActivity.class);
			Bundle bundle = new Bundle();
			ArrayList<String> title = new ArrayList<String>();
			for(int i = 0 ; i < area.size() ; i++){
				if(DEBUG) Log.d(TAG, "Detect area " + area.get(i) + ":" + mLocationActionData.get(area.get(i)).mAlarmTitle);
				title.add(mLocationActionData.get(area.get(i)).mAlarmTitle);
			}
			bundle.putStringArrayList("title", title);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainService.this.startActivity(intent);
		}
	}
	private List<Integer> detectLocation(Point pt) {
		List<Integer> ret = new ArrayList<Integer>();
		for(int i = 0 ; i < mLocationActionData.size() ; i++){
			if(DEBUG) Log.d(TAG, "Data " + (i+1) + ": p1(" + mLocationActionData.get(i).getmLongitude1() + "," + mLocationActionData.get(i).getmLatitude1() + ")");
			if(DEBUG) Log.d(TAG, "Data " + (i+1) + ": p2(" + mLocationActionData.get(i).getmLongitude2() + "," + mLocationActionData.get(i).getmLatitude2() + ")");
			
			// find min and max's x and y
			int min_x = 0, max_x = 0, min_y = 0, max_y = 0;
			if(mLocationActionData.get(i).getmLongitude1() > mLocationActionData.get(i).getmLongitude2()){
				max_x = mLocationActionData.get(i).getmLongitude1();
				min_x = mLocationActionData.get(i).getmLongitude2();
			}else{
				max_x = mLocationActionData.get(i).getmLongitude2();
				min_x = mLocationActionData.get(i).getmLongitude1();
			}
			if(mLocationActionData.get(i).getmLatitude1() > mLocationActionData.get(i).getmLatitude2()){
				max_y = mLocationActionData.get(i).getmLatitude1();
				min_y = mLocationActionData.get(i).getmLatitude2();
			}else{
				max_y = mLocationActionData.get(i).getmLatitude2();
				min_y = mLocationActionData.get(i).getmLatitude1();
			}
			
			// start to detect
			if(detect(min_x, max_x, min_y, max_y, pt)){
				if(mLocationActionData.get(i).isAlarm() == 0){
					mLocationActionData.get(i).setAlarming(1);
					ret.add(i);
				}
			}else{
				mLocationActionData.get(i).setAlarming(0);
			}
		}
		return ret;
	}
	private boolean detect(int min_x, int max_x, int min_y, int max_y, Point dp){
		boolean ret = false;
		if(dp.x >= min_x && dp.x <= max_x && dp.y <= max_y && dp.y >= min_y){
			ret = true;
		}else{
			ret = false;
		}
		return ret;
	}
	
	private void test(){
		final int TEST_DELAY_TIME = 30000;
		Timer testTimer = new Timer();
		testTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				Parcel parcel = Parcel.obtain();
				parcel.writeInt(131000000);
				parcel.writeInt(35000000);
				parcel.writeInt(100000000);
				parcel.writeInt(30000000);
				parcel.writeInt(0);
				parcel.writeString("test");
				parcel.setDataPosition(0);
				LocationActionData action = new LocationActionData(parcel);
				mLocationActionData.add(action);
			}
		}, TEST_DELAY_TIME);
	}
}
