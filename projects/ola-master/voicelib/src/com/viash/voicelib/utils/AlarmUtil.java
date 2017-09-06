package com.viash.voicelib.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

public class AlarmUtil {
	//private static final String TAG = "AlarmUtil";
	private static String mModel = null;
	private static int MAX_QUERY_COUNT = 100;
	
	private static final String[] PACKAGE_ALARM = {
		"com.htc.android.worldclock", 
		"com.android.alarmclock",
		"com.android.deskclock",
		"com.sec.android.app.clockpackage"
	};
	
	private static final String[] URI_PATH = {
		"content://com.htc.android.alarmclock/alarm",
		"content://com.android.alarmclock/alarm",
		"content://com.android.deskclock/alarm",
		"content://com.samsung.sec.android.clockpackage/alarm"
	};

	protected static String mPackageName = null;
	protected static Uri mUri = null;
	private static final boolean DEBUG = false;
	
	/*	
		// HTC "com.htc.android.worldclock.TimerAlert",// "com.htc.android.worldclock.AlarmAlert",//

		// Samsung "com.sec.android.app.clockpackage.ClockPackage",// "com.sec.android.app.clockpackage.alarm.AlarmAlert",//

		// Motorola "com.motorola.blur.alarmclock.AlarmAlert",// "com.motorola.blur.alarmclock.AlarmClock",// "com.motorola.blur.alarmclock.AlarmTimerAlert",

		// Stock Android Clock "com.android.alarmclock.AlarmClock",// 1.5 / 1.6 "com.android.deskclock.DeskClock",// 

		 Stock Clock, Android 2.1:
		 Package name: 'com.android.alarmclock',
		 Activity name: 'com.android.alarmclock.AlarmClock'

		Stock Clock, Android 2.2:
		 Package name: 'com.android.alarmclock',
		 Activity name: 'com.android.alarmclock.AlarmClock'

		Stock Clock, Android 2.3 (2.3.1):
		 Package name: 'com.android.deskclock',
		 Activity name: 'com.android.deskclock.DeskClock'

		Stock Clock, Android 2.3.3:
		 Package name: 'com.android.deskclock',
		 Activity name: 'com.android.deskclock.DeskClock'
*/
	
	public static void init(Context context)
	{		
		mModel = Build.MODEL;
		final Intent intent = new Intent("android.intent.action.SET_ALARM");
		PackageManager pm = context.getPackageManager();		
        final List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
		if(apps != null && apps.size() > 0)
		{
			boolean found = false;
			for(int i = 0; i < PACKAGE_ALARM.length && !found; i++)
			{
				for(ResolveInfo info: apps)
				{
					if(info.activityInfo != null)
					{
						if(info.activityInfo.packageName != null)
						{
							if(PACKAGE_ALARM[i].equals(info.activityInfo.packageName))
							{
								mPackageName = PACKAGE_ALARM[i];
								mUri = Uri.parse(URI_PATH[i]);
								found = true;
								break;
							}
						}
								
					}
				}
			}
		}
	}
	
	public static class AlarmData
	{
		long mId = 0;
		int mTime = 0;
		int mRepeatData = 0;
		String mTitle = null;
		boolean mVibrate = false;
		boolean mEnabled = true;		
		
		public long getmId() {
			return mId;
		}
		public void setmId(long mId) {
			this.mId = mId;
		}
		public int getmTime() {
			return mTime;
		}
		public int getmRepeatData() {
			return mRepeatData;
		}
		public String getmTitle() {
			return mTitle;
		}
		public boolean ismVibrate() {
			return mVibrate;
		}
		public void setmTime(int mTime) {
			this.mTime = mTime;
		}
		public void setmRepeatData(int mRepeatData) {
			this.mRepeatData = mRepeatData;
		}
		public void setmTitle(String mTitle) {
			this.mTitle = mTitle;
		}
		public void setmVibrate(boolean mVibrate) {
			this.mVibrate = mVibrate;
		}
		
		
		public boolean ismEnabled() {
			return mEnabled;
		}
		public void setmEnabled(boolean mEnabled) {
			this.mEnabled = mEnabled;
		}
		public JSONObject toJSonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", "" + mId);
				obj.put("time", "" + mTime);
				obj.put("repeat", "" + mRepeatData);
				if(mTitle != null)
					obj.put("title", mTitle);
				if(mVibrate)
					obj.put("vibrate", "1");
				else
					obj.put("vibrate", "0");
				
				if(mEnabled)
					obj.put("enabled", "1");
				else
					obj.put("enabled", "0");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;			
		}
	}
	
	private static final String FIELD_ID = "_id";
	private static final String FIELD_HOUR = "hour";
	private static final String FIELD_MINUTES = "minutes";
	private static final String FIELD_DAY_OF_WEEK = "daysofweek";
	private static final String FIELD_ENABLED = "enabled";
	private static final String FIELD_VIBRATE = "vibrate";
	private static final String FIELD_MESSAGE = "message";
	//private static final String FIELD_DURATION = "duration";
	//private static final String FIELD_ALARM_TIME = "alarmtime";
	
	protected static int codeRepeat(int repeat)
	{
		if("GT-P6800".equals(mModel) && repeat != 0)
		{
			repeat = (repeat << 1);
			if((repeat & 128) != 0)
				repeat |= 1;
			repeat = (repeat & 127);
		}			
		return repeat;
	}
	
	protected static int decodeRepeat(int repeat)
	{
		
		
		if("GT-P6800".equals(mModel) && repeat != 0)
		{
			if((repeat & 1) != 0)
				repeat |= 128;
			repeat = (repeat >> 1);
		}		
		
		return repeat;
	}
	
	public static int queryAlarmAtTime(Context context, int time, String title, int repeat)
	{
		return queryAlarmAtTimeInternal(context, time, title, codeRepeat(repeat));
	}	
	
	public static boolean addAlarm(Context context, int time, int repeat, String title, boolean vibrate, boolean enable)
	{
		return addAlarmInternal(context, time, codeRepeat(repeat), title, vibrate, enable);
	}
	
	public static boolean modifyAlarm(Context context, int id, int time, int repeat, String title, boolean vibrate, boolean enable)
	{
		return modifyAlarmInternal(context, id, time, codeRepeat(repeat), title, vibrate, enable);
	}
	
	public static List<AlarmData> queryAlarm(Context context, int startTime, int endTime)
	{
		List<AlarmData> lstAlarm = queryAlarmInternal(context, startTime, endTime);		
		if(lstAlarm != null)
		{
			for(AlarmData data : lstAlarm)
			{
				data.setmRepeatData(decodeRepeat(data.getmRepeatData()));
			}
		}
		
		return lstAlarm;
		
	}
	
	public static List<AlarmData> queryAlarm(Context context, int startTime, int endTime,int start_time_addition,int end_time_addition)
	{
		List<AlarmData> lstAlarm = queryAlarmInternal(context, startTime, endTime);
		if(start_time_addition != 0)
		{
		  List<AlarmData> lstAlarmAddition = queryAlarmInternal(context, start_time_addition, end_time_addition);
		  if(lstAlarmAddition != null)
		  {
			  for(AlarmData alarmData : lstAlarmAddition)
			  {
				  lstAlarm.add(alarmData);
			  }
		  }
			  
		}
		if(lstAlarm != null)
		{
			for(AlarmData data : lstAlarm)
			{
				data.setmRepeatData(decodeRepeat(data.getmRepeatData()));
			}
		}
		
		return lstAlarm;
		
	}
	
	protected static List<AlarmData> queryAlarmInternal(Context context, int startTime, int endTime)
	{
		List<AlarmData> lstData = null;
		if(mUri != null)
		{
			if(endTime == 0)
				endTime = 24 * 60;
			if(endTime >= startTime)
			{
				ContentResolver resolver=context.getContentResolver();		 
		        int startHour = startTime / 60;
		        int startMinute = startTime % 60;
		        int endHour = endTime / 60;
		        int endMinute = endTime % 60;
		        
		        String selection = "";
		        
		        if(startHour == endHour)
		        {
		        	selection = FIELD_HOUR + "=" + startHour;
		        	selection += " and " + FIELD_MINUTES + ">=" + startMinute;
		        	selection += " and " + FIELD_MINUTES + "<=" + endMinute;
		        }
		        else
		        {
		        	selection = "(" + FIELD_HOUR + "<" + endHour + " and " + FIELD_HOUR + ">" + startHour + ")";
		        	selection += " or (" + FIELD_HOUR + "=" + startHour + " and " + FIELD_MINUTES + ">=" + startMinute + ")";
		        	selection += " or (" + FIELD_HOUR + "=" + endHour + " and " + FIELD_MINUTES + "<=" + endMinute + ")";
		        }
	
		        
		        Cursor cursor = null;
		        
		        if(mUri.equals(Uri.parse(URI_PATH[3])))
		        {
		        	/*Date dateStart = new Date();
		        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        	sdf.format(dateStart);
		        	dateStart.setHours(startHour);
		        	dateStart.setMinutes(startMinute);
		        	dateStart.setSeconds(0);*/
		        	int timeStart = startHour * 100 + startMinute;//dateStart.getTime();
		        	int timeEnd = endHour * 100 + endMinute;
		        	
		        	
		        	
		        	/*Date dateEnd = new Date();
		        	sdf.format(dateStart);
		        	dateEnd.setHours(endHour);
		        	dateEnd.setMinutes(endMinute);
		        	dateEnd.setSeconds(0);*/
		        	//long timeEnd = dateEnd.getTime();
		        	
		        	if(DEBUG)
		        	{	
		        		//saveDebugToFile("alarmUtil", "queryAlarmInternal() timeStart = "+sdf.format(dateStart));
		        	    //saveDebugToFile("alarmUtil", "queryAlarmInternal() timeStart = "+sdf.format(dateEnd));
		        	}
		        	
		        	selection = "alarmtime>="+timeStart+" and "+"alarmtime<="+timeEnd;
		        	
		        	try
			        {
			        	cursor = resolver.query(mUri, null, selection, null, null);
			        	if(DEBUG)
			        	 saveDebugToFile("alarmUtil", "queryAlarmInternal() timeStart = "+timeStart+" timeEnd = "+timeEnd);
			        }
			        catch(Exception e)
			        {
			        	e.printStackTrace();
			        }
		        	if(cursor != null)
			        {	
		        		lstData = new ArrayList<AlarmUtil.AlarmData>();
		        		for (int i = 0; i < cursor.getCount(); ++i){
		        			if(i >= MAX_QUERY_COUNT)
		        				break;
		        			cursor.moveToPosition(i);			        		
			        		int colId = cursor.getColumnIndex(FIELD_ID);
			        		int colHour = cursor.getColumnIndex(FIELD_HOUR);
			        		int colMinutes = cursor.getColumnIndex(FIELD_MINUTES);
			        		int colDayOfWeek = cursor.getColumnIndex(FIELD_DAY_OF_WEEK);
			        		int colEnabled = cursor.getColumnIndex(FIELD_ENABLED);
			        		int colVibrate = cursor.getColumnIndex(FIELD_VIBRATE);
			        		int colMessage = cursor.getColumnIndex("name");
			        		int repeattype = cursor.getColumnIndex("repeattype");			     
			        		
			        		int alarmTime = cursor.getInt(4);
			        		//Date date = new Date(alarmAlertTime);
			        		if (alarmTime != 0) {
			        			colHour = alarmTime / 100;
				        		colMinutes = alarmTime % 100;
			        		}
			        		else {
			        			colHour = 0;
			        			colMinutes = 0;
			        		}
			        		//colDayOfWeek = date.getDay();
			        		colEnabled = cursor.getInt(1);
			        		colVibrate = cursor.getInt(6);
			        		
			        		if(DEBUG)
			        		{	
			        		    saveDebugToFile("alarmUtil", "queryAlarmInternal()33 colHour = "+colHour);
			                    saveDebugToFile("alarmUtil", "queryAlarmInternal()33 colMinutes = "+colMinutes);
			                    saveDebugToFile("alarmUtil", "queryAlarmInternal()33 colDayOfWeek = "+colDayOfWeek);
			                    //saveDebugToFile("alarmUtil", "queryAlarmInternal()33 alarmAlertTime = "+alarmAlertTime);
			        		}
		                   // Calendar cal = Calendar.getInstance();
		                   /* cal.setTimeInMillis(alarmAlertTime);
		                    Formatter ft=new Formatter(Locale.CHINA);
		                    if(DEBUG)
		                        saveDebugToFile("alarmUtil", "queryAlarmInternal()33  "+ft.format("%1$tY年%1$tm月%1$td日%1$tA，%1$tT %1$tp", cal).toString());*/
			        		
			        		repeattype = cursor.getInt(repeattype);
			        		int repeatCount = 0;
			        		if(((repeattype>>28) & 0xF) == 1)//sunday
			        		{
			        			repeatCount += 1<<6;
			        		}
			        		if(((repeattype>>24) & 0xF) == 1)//monday
			        		{
			        			repeatCount += 1;
			        		}
			        		if(((repeattype>>20) & 0xF) == 1)//tuesday	
			        		{
			        			repeatCount += 1<<1;
			        		}
			        		if(((repeattype>>16) & 0xF) == 1)//wednesday
			        		{
			        			repeatCount += 1<<2;
			        		}
			        		if(((repeattype>>12) & 0xF) == 1)//thursday
			        		{
			        			repeatCount += 1<<3;
			        		}
			        		if(((repeattype>>8) & 0xF) == 1)//friday
			        		{
			        			repeatCount += 1<<4;
			        		}
			        		if(((repeattype>>4) & 0xF) == 1)//saturday
			        		{
			        			repeatCount += 1<<5;
			        		}
			        		
		                    AlarmData data = new AlarmData();
		        			data.setmId(cursor.getLong(colId));
		        			data.setmTime(colHour * 60 + colMinutes);
		        			data.setmTitle(cursor.getString(colMessage));
		        			data.setmVibrate(colVibrate != 0);
		        			data.setmEnabled(colEnabled != 0);
		        			data.setmRepeatData(repeatCount);
		        			lstData.add(data);

				        }
			        	cursor.close();
			        }
		        	
		        }
		        else
		        {
			        try
			        {
			        	cursor = resolver.query(mUri, null, selection, null, FIELD_HOUR + " asc," + FIELD_MINUTES + " asc");
			        }
			        catch(Exception e)
			        {
			        	e.printStackTrace();
			        }
			
			        if(cursor != null)
			        {
			        	if(cursor.moveToFirst()){
			        		lstData = new ArrayList<AlarmUtil.AlarmData>();
			        		int colId = cursor.getColumnIndex(FIELD_ID);
			        		int colHour = cursor.getColumnIndex(FIELD_HOUR);
			        		int colMinutes = cursor.getColumnIndex(FIELD_MINUTES);
			        		int colDayOfWeek = cursor.getColumnIndex(FIELD_DAY_OF_WEEK);
			        		int colEnabled = cursor.getColumnIndex(FIELD_ENABLED);
			        		int colVibrate = cursor.getColumnIndex(FIELD_VIBRATE);
			        		int colMessage = cursor.getColumnIndex(FIELD_MESSAGE);
			        		
			        		if(colId != -1 && colHour != -1 && colMinutes != -1
			        				&& colDayOfWeek != -1 && colEnabled != -1 
			        				&& colVibrate != -1 && colMessage != -1)
			        		{
				        		do{
				        			AlarmData data = new AlarmData();
				        			data.setmId(cursor.getLong(colId));
				        			data.setmTime(cursor.getInt(colHour) * 60 + cursor.getInt(colMinutes));
				        			data.setmTitle(cursor.getString(colMessage));
				        			data.setmVibrate(cursor.getInt(colVibrate) != 0);
				        			data.setmEnabled(cursor.getInt(colEnabled) != 0);
				        			data.setmRepeatData(cursor.getInt(colDayOfWeek));
				        			lstData.add(data);
				        			
				        		}while(cursor.moveToNext() && lstData.size() < MAX_QUERY_COUNT);
			        		}
				        }
			        	cursor.close();
			        }
		        }
			}
		}
		
		return lstData;
	}
	
	protected static boolean addAlarmUsingIntent(Context context, int time, String title)
	{
		boolean ret = false;
		if(mUri != null)
		{
			int hour = time / 60;
		    int minutes = time % 60;
			Intent i = new Intent("android.intent.action.SET_ALARM"); 
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setPackage(mPackageName);
			i.putExtra("android.intent.extra.alarm.MESSAGE", title); 
			i.putExtra("android.intent.extra.alarm.HOUR", hour); 
			i.putExtra("android.intent.extra.alarm.MINUTES", minutes); 
			i.putExtra("android.intent.extra.alarm.SKIP_UI", true);
			try
			{
				context.startActivity(i);
				ret = true;
			}
			catch(ActivityNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	protected static boolean isTimeWithin24(int time, int repeat)
	{
		boolean ret = false;
		
		Date date = new Date();
		int day = 0;
		int curTime = date.getHours() * 60 + date.getMinutes();
		if(curTime >= time)
		{
			day = (date.getDay() + 6) % 7;
		}
		else
		{
			day = (date.getDay() + 5) % 7;
		}
		
		if(((1 << day) & repeat) != 0)
			ret = true;
		return ret;
	}
	
	protected static ContentValues composeValue(int time, int repeat, String title, boolean vibrate, boolean enable)
	{
		int hour = time / 60;
        int minutes = time % 60;
        ContentValues values = new ContentValues();
        if(mUri.equals(Uri.parse(URI_PATH[3])))
        {
        	values.put("alarmtime", hour*100+minutes);
	        values.put("active", enable);
        }
        else
        {
	        values.put(FIELD_HOUR, hour);
	        values.put(FIELD_MINUTES, minutes);
	        values.put(FIELD_MESSAGE, title);
	        values.put(FIELD_VIBRATE, vibrate);
	        values.put(FIELD_DAY_OF_WEEK, repeat);
	        values.put(FIELD_ENABLED, enable);
        }
        return values;
	}
	
	public static boolean addAlarmInternal(Context context, int time, int repeat, String title, boolean vibrate, boolean enable)
	{
		boolean ret = false;
		if(mUri != null)
		{
			if(repeat == 0)
			{
				ret = addAlarmUsingIntent(context, time, title);
			}
			else 
			{
		        ContentResolver resolver = context.getContentResolver();	
		        ContentValues values = composeValue(time, repeat, title, vibrate, enable);    		        
		        Uri uriNew = null;
		        
		        
		        if(isTimeWithin24(time, repeat))
				{
					ret = addAlarmUsingIntent(context, time, title);	
					if(ret)
					{
						UpdateTimerToRepeat thread = new UpdateTimerToRepeat(context, time, repeat, title);
						thread.start();
					}
					else
					{
						CustomToast.makeToast(context, "设置闹钟失败");//, Toast.LENGTH_SHORT).show();
					}
				}
		        else
		        {
		        	try
			        {
			        	uriNew = resolver.insert(mUri, values);
			        	if(uriNew != null)
			        		ret = true;
			        }
			        catch(Exception e)
			        {
			        	e.printStackTrace();
			        }
		        }
			}
		}

        return ret;
	}
	
	public static boolean modifyAlarmInternal(Context context, int id, int time, int repeat, String title, boolean vibrate, boolean enable)
	{
		boolean ret = false;
		if(!enable)
		{
			ContentValues values = composeValue(time, repeat, title, vibrate, enable);
			ret = updateValue(context, id, values);
		}
		else
		{
			if(mUri.equals(Uri.parse(URI_PATH[3])))
			{
				ContentValues values = composeValue(time, repeat, title, vibrate, enable);
				ret = updateValue(context, id, values);	
			}
			else
			{
				long[] ids = new long[1];
				ids[0] = id;
				deleteAlarm(context, ids);
				ret = addAlarmInternal(context, time, repeat, title, vibrate, enable);
			}
		}
		return ret;
		/*boolean ret = false;
        int hour = time / 60;
        int minutes = time % 60;
        
        Uri uri = Uri.parse(URL);
        
        ContentResolver resolver = context.getContentResolver();	
        ContentValues values = new ContentValues();
        values.put(FIELD_HOUR, hour);
        values.put(FIELD_MINUTES, minutes);
        values.put(FIELD_MESSAGE, title);
        values.put(FIELD_VIBRATE, vibrate);
        values.put(FIELD_DAY_OF_WEEK, repeat);
        values.put(FIELD_ENABLED, "1");

        try
        {
        	String where = FIELD_ID + "=" + id;
        	resolver.update(uri, values, where, null);
        	ret = true;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        return ret;*/
	}
	
	protected static boolean updateValue(Context context, int id, ContentValues values)
	{
		boolean ret = false;
		try
        {
			ContentResolver resolver = context.getContentResolver();	
    		Uri uri=Uri.withAppendedPath(mUri, "/" + id);
        	int rows = resolver.update(uri, values, null, null);
        	if(rows >0)
        		ret = true;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
		
		return ret;
	}
	
	public static boolean updateEnableState(Context context, int id, boolean enable)
	{
		ContentValues values = new ContentValues();
        values.put(FIELD_ENABLED, enable);    
        
    	return updateValue(context, id, values);
	}
	
	public static void deleteAlarm(Context context, int startTime, int endTime)
	{
		if(mUri != null)
		{
			if(endTime >= startTime)
			{
				ContentResolver resolver=context.getContentResolver();		 
		        int startHour = startTime / 60;
		        int startMinute = startTime % 60;
		        int endHour = endTime / 60;
		        int endMinute = endTime % 60;
		        
		        String selection = "";
		        
		        if(startHour == endHour)
		        {
		        	selection = FIELD_HOUR + "=" + startHour;
		        	selection += " and " + FIELD_MINUTES + ">=" + startMinute;
		        	selection += " and " + FIELD_MINUTES + "<=" + endMinute;
		        }
		        else
		        {
		        	selection = "(" + FIELD_HOUR + "<" + endHour + " and " + FIELD_HOUR + ">" + startHour + ")";
		        	selection += " or (" + FIELD_HOUR + "=" + startHour + " and " + FIELD_MINUTES + ">=" + startMinute + ")";
		        	selection += " or (" + FIELD_HOUR + "=" + endHour + " and " + FIELD_MINUTES + "<=" + endMinute + ")";
		        }
	
		        try
		        {
		        	resolver.delete(mUri, selection, null);	      
		        }
		        catch(Exception e)
		        {
		        	e.printStackTrace();
		        }
			}
		}
	}
	
	public static boolean deleteAlarm(Context context, long[] ids)
	{
		boolean ret = false;
		if(mUri != null)
		{
			if(ids.length > 0)
			{
				ContentResolver resolver=context.getContentResolver();		 
				String selection = "";
				for(int i = 0; i < ids.length; i++)
				{
					if(i != 0)
						selection += " or ";
			        selection += FIELD_ID + "=" + ids[i];
				}
		
				try
				{
					resolver.delete(mUri, selection, null);	 
					ret = true;
				}
				catch(Exception e)
		        {
		        	e.printStackTrace();
		        }
			}
		}
		
		return ret;
	}
	
	protected static int queryAlarmAtTimeInternal(Context context, int time, String title, int repeat)
	{
		int ret = 0;
		if(mUri != null)
		{
			String selection = "";
	       	selection = FIELD_HOUR + "=" + (time / 60);
	       	selection += " and " + FIELD_MINUTES + "=" + (time % 60);
	       	if(title != null && title.length() > 0)
	       	{	       	
	       		selection += " and " + FIELD_MESSAGE + "=\'" + title + "\'";
	       	}
	       	
	       	selection += " and " + FIELD_DAY_OF_WEEK + "=" + repeat;
	        
	        Cursor cursor = null;
	        ContentResolver resolver=context.getContentResolver();	
	        try
	        {
	        	cursor = resolver.query(mUri, null, selection, null, null);
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	
	        if(cursor != null)
	        {
	        	int lastOne = 0;
	        	if(cursor.moveToFirst())
	        	{
	        		if(title == null)
	        			title = "";
	        		int colId = cursor.getColumnIndex(FIELD_ID);
	        		int colTitle = cursor.getColumnIndex(FIELD_MESSAGE);
	        		if(colId != -1 && colTitle != -1)
	        		{
		        		do
		        		{		        			
		        			lastOne = cursor.getInt(colId);
		        			String message = cursor.getString(colTitle);
		        			if(title.equalsIgnoreCase(message))
		        			{
		        				ret = lastOne;
		        				break;
		        			}
		        		}while(cursor.moveToNext());
		        		if(ret == 0)
		        			ret = lastOne;
	        		}
	        	}
	        	cursor.close();
	        }
		}
        
        return ret;
	}
	
	protected static class UpdateTimerToRepeat extends Thread
	{
		protected Context mContext;
		protected int mTime;
		protected int mRepeat;
		protected String mTitle;
		
		public UpdateTimerToRepeat(Context context, int time, int repeat, String title)
		{
			mContext = context;
			mTime = time;
			mRepeat = repeat;
			mTitle = title;
		}

		@Override
		public void run() {
			int retry = 5;
			do
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				int id = queryAlarmAtTimeInternal(mContext, mTime, mTitle, 0);
				if(id > 0)
				{
			        ContentValues values = new ContentValues();
			        values.put(FIELD_DAY_OF_WEEK, mRepeat);    
			        
		        	updateValue(mContext, id, values);
					break;
				}
				retry--;
			}while(retry > 0);
			
			super.run();
		}
		
	}
	protected static void saveDebugToFile(String msg1, String msg2) {
		if (DEBUG) {
			Date date = new Date();
			String fileName = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/voice_assist/debug/";
			File fileParent = new File(fileName);
			if (!fileParent.exists())
				fileParent.mkdirs();

			String sDate = String.format("%04d%02d%02d_",
					date.getYear() + 1900, date.getMonth() + 1, date.getDate());
			String time = String.format("%02d:%02d:%02d", date.getHours(),
					date.getMinutes(), date.getSeconds());

			FileOutputStream fOs = null;
			try {
				fOs = new FileOutputStream(fileName + sDate + "state.txt", true);
				try {
					fOs.write(time.getBytes());

					if (msg1 != null) {
						fOs.write("\t".getBytes());
						fOs.write(msg1.getBytes());
					}

					if (msg2 != null) {
						fOs.write("\t".getBytes());
						fOs.write(msg2.getBytes());
					}

					fOs.write("\r\n".getBytes());

				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (fOs != null)
					try {
						fOs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}	
}
