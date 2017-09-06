package com.viash.voicelib.utils.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.viash.voicelib.R;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;

public class AlarmUtil {
	public static final String ALARM_ALERT_ACTION = "com.viash.voice_assistant.alarm.ALARM_ALERT";
	public static final String ALARM_DONE_ACTION = "com.viash.voice_assistant.alarm.ALARM_DONE";
	public static final String ALARM_SNOOZE_ACTION = "com.viash.voice_assistant.alarm.ALARM_SNOOZE";
	public static final String ALARM_DISMISS_ACTION = "com.viash.voice_assistant.alarm.ALARM_DISMISS";
	public static final String ALARM_DISMISS_ALARM_WARN = "com.viash.voice_assistant.alarm.ALARM_WARN_DISMISS";
	public static final String ALARM_CANCEL_SNOOZE_ACTION = "com.viash.voice_assistant.alarm.ALARM_CANCEL_SNOOZE";
	public static final String ALARM_REMIND_ACTION = "com.viash.voice_assistant.alarm.ALARM_REMIND";
	public static final String ALARM_KILLED = "alarm_killed";
	public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";
	public static final String ALARM_ALERT_SILENT = "silent";
	public static final String CANCEL_SNOOZE = "cancel_snooze";
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";
	public static final String ALARM_ID = "alarm_id";
	private static int MAX_QUERY_COUNT = 100;
	final static String PREF_SNOOZE_ID = "snooze_id";
	final static String PREF_SNOOZE_TIME = "snooze_time";
	private static Context mContext ;
	private static final String DEFAULT_SNOOZE = "10";

	public final static String DM12 = "E h:mm aa";
	public final static String DM24 = "E k:mm";

	public final static String M12 = "h:mm aa";
	public final static String M24 = "kk:mm";
	public static  Uri  mUri = Uri.parse("content://com.viash.voice_assistant.db/alarm");

	private static final String TAG = "AlarmUtil";

	private static Handler mHandler;
	
	private static final String IS_FIRST_ALARM ="is_first_alarm";

	private static boolean isFirstAlarm = false;


	public static void setHandler(Handler handler){
		mHandler = handler;
	}

	public static boolean addAlarm(Context context, Alarm alarm) {
		mContext = context ;
		ContentValues values = createContentValues(alarm);
		Uri uri = context.getContentResolver().insert(
				Alarm.Columns.CONTENT_URI, values);
		alarm.id = (int) ContentUris.parseId(uri);

		long timeInMillis = alarm.time;

		Log.i(TAG, "-------->>uri: "+uri.toString()+"  id:"+alarm.id+"  timeInMills: "+timeInMillis);

		//将新建的闹钟时间和之前某闹钟或被推迟的时间 做比较，若响铃时间冲突 则取消之前的闹钟
		if (alarm.enabled) {
			clearSnoozeIfNeeded(context, timeInMillis);
		}

		setNextAlert(context);
		return true;
	}
	
	public static boolean addAlarm(Context context, int time, int repeat, String title, boolean vibrate, boolean enable,String rTime,int tab)
	{
		Alarm alarm = new Alarm();
		alarm.id = -1;
		alarm.enabled = enable;
		alarm.hour = time/60;
		alarm.minutes = time%60; 
		alarm.daysOfWeek =  new Alarm.DaysOfWeek(repeat);
		alarm.time = calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();
		alarm.rTime = rTime;
		alarm.vibrate = vibrate;
		alarm.label = title;
		alarm.tab = tab;
		/*alarm.year = 0;
		alarm.month = 0;
		alarm.day = 0;*/
		return addAlarm(context, alarm);

	}
	

	public static boolean modifyAlarm(Context context, int id, int time, int repeat, String title, boolean vibrate, boolean enable,String rtime,int tab)
	{
		ContentResolver resolver=context.getContentResolver();  
	      
       Alarm alarm = getAlarm(resolver,id);
       if (alarm == null) {
         alarm = new Alarm();
         alarm.tab = tab;
       }

		alarm.id = id;
		alarm.enabled = enable;
		alarm.hour =  time / 60; 
		alarm.minutes = time % 60;
		alarm.rTime = rtime;
		alarm.daysOfWeek =  new Alarm.DaysOfWeek(repeat); 
		alarm.vibrate = vibrate;
		alarm.label = title;
		Log.e("naozhong", ""+alarm.daysOfWeek.mDays);
		return setAlarm(context, alarm);
	}

	
	public static int queryAlarmAtTime(Context context, int time, String title, int repeat)
	{  
		Log.e("queryAlarmAtTime", ""+time);

		return queryAlarmAtTimeInternal(context, time, title, repeat);
	}	

	protected static List<Alarm> queryAlarmInternal(Context context, int startTime, int endTime,int tab)
	{
		List<Alarm> lstData = null;
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
					selection = "hour" + "=" + startHour;
					selection += " and " + "minutes" + ">=" + startMinute;
					selection += " and " + "minutes" + "<=" + endMinute;
					selection += " and " + " tab " + "=" + tab;

				}
				else
				{
					selection = "tab" + "=" + tab;
					selection += " and (" + "hour" + "<" + endHour + " and " + "hour" + ">" + startHour + ")";
					selection += " or (" + "hour" + "=" + startHour + " and " + "minutes" + ">=" + startMinute + ")";
					selection += " or (" + "hour" + "=" + endHour + " and " + "minutes" + "<=" + endMinute + ")";
					//selection += "and (" + "tab" + "=" + tab+ ")";
				}
				Log.i(TAG, "--->>selection:"+selection);
				Cursor cursor = null;

				if(true)
				{
					/*Date dateStart = new Date();
		        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        	sdf.format(dateStart);
		        	dateStart.setHours(startHour);
		        	dateStart.setMinutes(startMinute);
		        	dateStart.setSeconds(0);
		        	long timeStart = dateStart.getTime();



		        	Date dateEnd = new Date();
		        	sdf.format(dateStart);
		        	dateEnd.setHours(endHour);
		        	dateEnd.setMinutes(endMinute);
		        	dateEnd.setSeconds(0);
		        	long timeEnd = dateEnd.getTime();
		         	selection = "alarmtime>="+timeStart+" and "+"alarmtime<="+timeEnd;*/

					try
					{
						Log.e("EEEEE", selection);
						cursor = resolver.query(mUri, null, selection, null, null);

					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					if(cursor != null)
					{
						if(cursor.moveToFirst()){
							lstData = new ArrayList<Alarm>();
							int colId = cursor.getColumnIndex("_id");
							int colHour = cursor.getColumnIndex("hour");
							int colMinutes = cursor.getColumnIndex("minutes");
							int colDayOfWeek = cursor.getColumnIndex("daysofweek");
							int colEnabled = cursor.getColumnIndex("enabled");
							int colVibrate = cursor.getColumnIndex("vibrate");
							int colMessage = cursor.getColumnIndex("message");
							int colTime = cursor.getColumnIndex("remindtime");
							int colTab = cursor.getColumnIndex("tab");
							/*int colYear = cursor.getColumnIndex("year");
							int colMonth = cursor.getColumnIndex("month");
							int colDay = cursor.getColumnIndex("day");*/
							//long alarmAlertTime = cursor.getLong(3);
							//Date date = new Date(alarmAlertTime);			       
							int hour = cursor.getInt(colHour);
							int minutes = cursor.getInt(colMinutes);
							int dayOfWeek = cursor.getInt(colDayOfWeek);
							int enabled = cursor.getInt(colEnabled);
							int vibrate = cursor.getInt(colVibrate);
							String message = cursor.getString(colMessage);
							String time = cursor.getString(colTime);
							int tab_ = cursor.getInt(colTab);
							/*int  year = cursor.getInt(colYear);
							int month = cursor.getInt(colMonth);
							int day = cursor.getInt(colDay);*/
							/*Calendar cal = Calendar.getInstance();
		                    cal.setTimeInMillis(alarmAlertTime);
		                    Formatter ft=new Formatter(Locale.CHINA);*/


							Alarm data = new Alarm();
							data.id=(int) cursor.getLong(colId);
							data.time= hour * 60 + minutes;
							data.rTime = time;
							data.label= message;
							data.vibrate=(vibrate != 0);
							data.enabled=(enabled != 0);
							data.daysOfWeek=new Alarm.DaysOfWeek(dayOfWeek);
							data.tab = tab_;
							/*data.year = year;
							data.month = month;
							data.day = day;*/
							lstData.add(data);
							Log.e("eee",""+data.time);

							if(colId != -1 && colHour != -1 && colMinutes != -1
									&& colDayOfWeek != -1 && colEnabled != -1 
									&& colVibrate != -1 && colMessage != -1)
							{
								while(cursor.moveToNext() && lstData.size() < MAX_QUERY_COUNT)
								{
									colId = cursor.getColumnIndex("_id");
									colHour = cursor.getColumnIndex("hour");
									colMinutes = cursor.getColumnIndex("minutes");
									colDayOfWeek = cursor.getColumnIndex("daysofweek");
									colEnabled = cursor.getColumnIndex("enabled");
									colVibrate = cursor.getColumnIndex("vibrate");
									colMessage = cursor.getColumnIndex("message");
									colTime = cursor.getColumnIndex("remindtime");
									colTab = cursor.getColumnIndex("tab");
									/*colYear = cursor.getColumnIndex("year");
									colMonth = cursor.getColumnIndex("month");
									colDay = cursor.getColumnIndex("day");*/

									//long alarmAlertTime = cursor.getLong(3);
									//Date date = new Date(alarmAlertTime);			       
									hour = cursor.getInt(colHour);
									minutes = cursor.getInt(colMinutes);
									dayOfWeek = cursor.getInt(colDayOfWeek);
									enabled = cursor.getInt(colEnabled);
									vibrate = cursor.getInt(colVibrate);
									message = cursor.getString(colMessage);
									time = cursor.getString(colTime);
									tab_ = cursor.getInt(colTab);
									/*year = cursor.getInt(colYear);
									month = cursor.getInt(colMonth);
									day = cursor.getInt(colDay);*/


									Alarm data1 = new Alarm();
									data1.id=(int) (cursor.getLong(colId));
									data1.time= hour * 60 + minutes;
									data1.rTime = time;
									data1.label=( message);
									data1.vibrate=( vibrate != 0);
									data1.enabled=( enabled != 0);
									data1.daysOfWeek=new Alarm.DaysOfWeek(dayOfWeek);
									data1.tab = tab_;
									/*data1.year = year;
									data1.month = month;
									data.day = day;*/
									Log.e("eee",""+data.time);
									lstData.add(data1);				        			
								}}}

						//ft.close();

						cursor.close();
					}

				}
				/* else
		        {
			        try
			        {
			        	cursor = resolver.query(mUri, null, selection, null, "hour" + " asc," + "minutes" + " asc");
			        }
			        catch(Exception e)
			        {
			        	e.printStackTrace();
			        }

			        if(cursor != null)
			        {
			        	if(cursor.moveToFirst()){
			        		lstData = new ArrayList<Alarm>();
			        		int colId = cursor.getColumnIndex("_id");
			        		int colHour = cursor.getColumnIndex("hour");
			        		int colMinutes = cursor.getColumnIndex("minutes");
			        		int colDayOfWeek = cursor.getColumnIndex("daysofweek");
			        		int colEnabled = cursor.getColumnIndex("enabled");
			        		int colVibrate = cursor.getColumnIndex("vibrate");
			        		int colMessage = cursor.getColumnIndex("message");

			        		if(colId != -1 && colHour != -1 && colMinutes != -1
			        				&& colDayOfWeek != -1 && colEnabled != -1 
			        				&& colVibrate != -1 && colMessage != -1)
			        		{
				        		do{
				        			Alarm data = new Alarm();
				        			data.id=(int) (cursor.getLong(colId));
				        			data.time=(cursor.getInt(colHour) * 60 + cursor.getInt(colMinutes));
				        			data.label=(cursor.getString(colMessage));
				        			data.vibrate=(cursor.getInt(colVibrate) != 0);
				        			data.enabled=(cursor.getInt(colEnabled) != 0);
				        			data.daysOfWeek=new Alarm.DaysOfWeek((cursor.getInt(colDayOfWeek)));
				        			lstData.add(data);

				        		}while(cursor.moveToNext() && lstData.size() < MAX_QUERY_COUNT);
			        		}
				        }
			        	cursor.close();
			        }
		        }*/
			}
		}

		return lstData;
	}

	public static List<Alarm> queryAlarm(Context context, int startTime, int endTime,int tab)
	{
		List<Alarm> lstAlarm = queryAlarmInternal(context, startTime, endTime,tab);

		//Log.e("EEEEE", "" + lstAlarm.size());
		return lstAlarm;

	}

	protected static int queryAlarmAtTimeInternal(Context context, int time, String title, int repeat)
	{
		int ret = 0;

		if(mUri != null)
		{
			Cursor cursor = null;
			ContentResolver resolver=context.getContentResolver();	
			cursor= getAlarmsCursor(resolver);
			if(cursor != null)
			{
				int lastOne = 0;
				if(cursor.moveToFirst())
				{
					if(title == null)
						title = "";
					int colId = cursor.getColumnIndex("_id");
					int colTitle = cursor.getColumnIndex("message");
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

	public static boolean deleteAlarm(Context context, long[] ids){

		for(int i=0;i<ids.length;i++){
			deleteAlarm( context, ids[i]);
		}
		return true;
	}
	public static boolean deleteAlarm(Context context, long alarmId) {
		if (alarmId == -1)
			return false;

		ContentResolver contentResolver = context.getContentResolver();
		disableSnoozeAlert(context, alarmId);

		Uri uri = ContentUris
				.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
		contentResolver.delete(uri, "", null);

		setNextAlert(context);
		return true;
	}

	public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null,
				Alarm.Columns.DEFAULT_SORT_ORDER);
	}

	private static Cursor getFilteredAlarmsCursor(ContentResolver contentResolver) {
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
				null, null);
	}

	private static Cursor getFilteredAlarmByTab(ContentResolver contentResolver,int tab){
		String where = "tab"+"="+tab;
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, where,
				null, null);
	}
	
	private static Cursor getFilteredAlarmByTabEna(ContentResolver contentResolver,int tab,int enable){
		String where = "enabled"+"="+enable;
		where += " and "+"tab"+"="+tab;
		
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, where,
				null, null);
	}
	

	private static ContentValues createContentValues(Alarm alarm) {
		ContentValues values = new ContentValues(8);
		if (!alarm.daysOfWeek.isRepeatSet()) {
			Log.i(TAG, "----------->>>当前闹钟非重复");
		}

		values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
		values.put(Alarm.Columns.HOUR, alarm.hour);
		values.put(Alarm.Columns.MINUTES, alarm.minutes);
		values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
		values.put(Alarm.Columns.ALARM_TIME, alarm.time);
		values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
		values.put(Alarm.Columns.MESSAGE, alarm.label);	
		values.put(Alarm.Columns.RTIME, alarm.rTime);
		values.put(Alarm.Columns.TAB, alarm.tab);
		/*values.put(Alarm.Columns.YEAR, alarm.year);
		values.put(Alarm.Columns.MONTH,alarm.month);
		values.put(Alarm.Columns.DAY,alarm.day);*/
		return values;
	}

	private static void clearSnoozeIfNeeded(Context context, long alarmTime) {
		SharedPreferences prefs = context.getSharedPreferences(
				"AlarmClock", 0);
		long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
		Log.i(TAG,"  之前延迟闹钟的时间："+snoozeTime+"   新建闹钟开始时间："+alarmTime);
		if (alarmTime < snoozeTime) {
			clearSnoozePreference(context, prefs);
		}
	}

	public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
		Cursor cursor = contentResolver.query(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
		Alarm alarm = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				alarm = new Alarm(cursor);
			}
			cursor.close();
		}
		return alarm;
	}

	public static boolean setAlarm(Context context, Alarm alarm) {
		ContentValues values = createContentValues(alarm);
		ContentResolver resolver = context.getContentResolver();
		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);

		long timeInMillis = calculateAlarm(alarm);

		if (alarm.enabled) {
			disableSnoozeAlert(context, alarm.id);
			clearSnoozeIfNeeded(context, timeInMillis);
			saveSnoozeAlert(context,alarm.id,timeInMillis);
		}

		setNextAlert(context);
		return true;
	}

	public static void enableAlarm(final Context context, final int id,
			boolean enabled) {
		enableAlarmInternal(context, id, enabled);
		setNextAlert(context);
	}

	public static void enableAlarmInternal(final Context context,
			final int id, boolean enabled) {
		enableAlarmInternal(context,
				getAlarm(context.getContentResolver(), id), enabled);
	}

	private static void enableAlarmInternal(final Context context,
			final Alarm alarm, boolean enabled) {
		Log.e("EEEEE", "" + enabled);
		if (alarm == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();

		ContentValues values = new ContentValues(2);
		values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);
		if (enabled) {
			long time = 0;
			if (!alarm.daysOfWeek.isRepeatSet()) {
				time = calculateAlarm(alarm);
			}
			values.put(Alarm.Columns.ALARM_TIME, time);
		} else {
			disableSnoozeAlert(context, alarm.id);
		}

		Log.e("EEEEE", "" + values);

		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);
	}

	public static Alarm calculateNextAlert(final Context context) {
		Alarm alarm = null;
		long minTime = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
		Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Alarm a = new Alarm(cursor);
					if (a.time == 0) {
						a.time = calculateAlarm(a);
					} else if (a.time < now) {
						enableAlarmInternal(context, a, false);
						continue;
					}
					if (a.time < minTime) {
						minTime = a.time;
						alarm = a;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return alarm;
	}

	public static Alarm calculateFirstAlarm(final Context context){
		Alarm alarm = null;
		List<Alarm> alarms = new ArrayList<Alarm>();
		Calendar calendar = Calendar.getInstance();
		Date date = null ;
		int nowDay = calendar.get(Calendar.DAY_OF_YEAR);
		int comDay = 0;
		long minTime = 0;
		Cursor cursor = getFilteredAlarmByTab(context.getContentResolver(),1);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Alarm a = new Alarm(cursor);
					date = new Date(a.time);
					calendar.setTime(date);
					comDay = calendar.get(Calendar.DAY_OF_YEAR);
					if (nowDay == comDay){
						if(a.hour >= 0 && a.hour<24) {
							alarms.add(a);
						}
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		if(alarms.size() != 0){
			minTime = alarms.get(0).time;
			for(int m = 0;m<alarms.size();m++){
				if(minTime >= alarms.get(m).time)
					alarm = alarms.get(m);
			}
		}
		System.out.println("------->>>>first_alarm_id:"+alarm.id);
		return alarm;
	}


	public static void disableExpiredAlarms(final Context context) {
		Log.e("EEEEE", "disableExpiredAlarms");
		Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
		long now = System.currentTimeMillis();

		if (cur.moveToFirst()) {
			do {
				Alarm alarm = new Alarm(cur);
				if (alarm.time != 0 && alarm.time < now) {
					enableAlarmInternal(context, alarm, false);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	public static void setNextAlert(final Context context) {
		Log.e("beforeenableSnoozeAlert","");
		if (!enableSnoozeAlert(context)) {
			Log.e("beforecalculateNextAlert","");
			Alarm alarm = calculateNextAlert(context);
			Log.e("aftercalculateNextAlert","");
			if (alarm != null) {
				Log.e("beforedenableAlert",""+alarm.enabled);
				enableAlert(context, alarm, alarm.time);
				Log.e("afterdenableAlert",""+alarm.enabled);
			} else {
				disableAlert(context);
			}
		}
	}

	private static void enableAlert(Context context, final Alarm alarm,
			final long atTimeInMillis) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		if (true) {
			Log.v("Spencer", "** setAlert id " + alarm.id + " atTime "
					+ atTimeInMillis);
		}

		Intent intent = new Intent(ALARM_ALERT_ACTION);
		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(ALARM_RAW_DATA, out.marshall());

		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

		setStatusBarIcon(context, true);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(atTimeInMillis);
		String timeString = formatDayAndTime(context, c);
		saveNextAlarm(context, timeString);
	}

	static void disableAlert(Context context) {
		Log.e("beforedisableAlert","disableAlert");
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				new Intent(ALARM_ALERT_ACTION),
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		setStatusBarIcon(context, false);
		Log.e("disableAlert","disableAlert");
		saveNextAlarm(context, "");
	}

	@SuppressLint("NewApi")
	public static void saveSnoozeAlert(final Context context, final int id,
			final long time) {
		SharedPreferences prefs = context.getSharedPreferences(
				"AlarmClock", 0);
		if (id == -1) {
			clearSnoozePreference(context, prefs);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			ed.putInt(PREF_SNOOZE_ID, id);
			ed.putLong(PREF_SNOOZE_TIME, time);
			ed.apply();
		}
		setNextAlert(context);
	}

	public  static void disableSnoozeAlert(final Context context, final long alarmId) {
		SharedPreferences prefs = context.getSharedPreferences(
				"AlarmClock", 0);
		int snoozeId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (snoozeId == -1) {
			return;
		} else if (snoozeId == alarmId) {
			clearSnoozePreference(context, prefs);
		}
	}

	@SuppressLint("NewApi")
	private static void clearSnoozePreference(final Context context,
			final SharedPreferences prefs) {
		final int alarmId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (alarmId != -1) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(alarmId);
		}

		final SharedPreferences.Editor ed = prefs.edit();
		ed.remove(PREF_SNOOZE_ID);
		ed.remove(PREF_SNOOZE_TIME);
		ed.apply();
	};

	private static Alarm mAlarm = new Alarm();
	private static boolean enableSnoozeAlert(final Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				"AlarmClock", 0);

		int id = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (id == -1) {
			return false;
		}
		long time = prefs.getLong(PREF_SNOOZE_TIME, -1);
		mAlarm = getAlarm(context.getContentResolver(), id);
		if (mAlarm == null) {
			return false;
		}
		mAlarm.time = time;

		enableAlert(context, mAlarm, time);
		return true;
	}

	private static void setStatusBarIcon(Context context, boolean enabled) {
		Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
		alarmChanged.putExtra("alarmSet", enabled);
		context.sendBroadcast(alarmChanged);
	}


	//计算该闹钟的发生的时间
	private static long calculateAlarm(Alarm alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek)
				.getTimeInMillis();
	}

	static Calendar calculateAlarm(int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);

		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0)
			c.add(Calendar.DAY_OF_WEEK, addDays);
		return c;
	}

	public static String formatTime(final Context context, int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {
		Calendar c = calculateAlarm(hour, minute, daysOfWeek);
		return formatTime(context, c);
	}

	public static String formatTime(final Context context, Calendar c) {
		String format = get24HourMode(context) ? M24 : M12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	private static String formatDayAndTime(final Context context, Calendar c) {
		String format = get24HourMode(context) ? DM24 : DM12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	static void saveNextAlarm(final Context context, String timeString) {
		Settings.System.putString(context.getContentResolver(),
				Settings.System.NEXT_ALARM_FORMATTED, timeString);
		Log.e("saveNextAlarm","saveNextAlarm");
	}

	public static boolean get24HourMode(final Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}

	private static long snoozeTime ;
	public static void snooze(Context context,int id,int time,int tab){
		mContext = context;
		Calendar calendar = Calendar.getInstance();
		Alarm alarm = null;
		int snoozeMinutes = 0;
		if(time<=0){
			final String snooze =
					PreferenceManager.getDefaultSharedPreferences(mContext)
					.getString("snooze_duration",DEFAULT_SNOOZE);
			snoozeMinutes = Integer.parseInt(snooze);
		}else{
			snoozeMinutes = time;
		}

		if(id != -1){
			alarm = getAlarm(mContext.getContentResolver(), id);
		}else{
			alarm = getLastRemindOrAlarm(mContext,tab);
		}

		if(alarm != null){
			Log.i(TAG, "---------->>>id:"+alarm.id+"   time:"+snoozeMinutes);
			if(alarm.id == mAlarm.id){
				snoozeTime = mAlarm.time+(1000 * 60 * snoozeMinutes);
			}else{
				snoozeTime = alarm.time
						+ (1000 * 60 * snoozeMinutes);
			}
		AlarmUtil.saveSnoozeAlert(mContext, alarm.id,
				snoozeTime);
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(snoozeTime);
		String label = alarm.getLabelOrDefault(mContext);
		label = mContext.getString(R.string.alarm_notify_snooze_label, label);
		Intent cancelSnooze = new Intent(ALARM_CANCEL_SNOOZE_ACTION);
		// Intent cancelSnooze = new Intent(mContext, AlarmReceiver.class);0
		cancelSnooze.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		cancelSnooze.setAction(AlarmUtil.CANCEL_SNOOZE);
		cancelSnooze.putExtra(AlarmUtil.ALARM_ID, id);
		PendingIntent broadcast =
				PendingIntent.getBroadcast(mContext, id, cancelSnooze, 0);
		NotificationManager nm = getNotificationManager();
		Notification n = new Notification(R.drawable.stat_notify_alarm,
				label, 0);
		n.setLatestEventInfo(mContext, label,
				mContext.getString(R.string.alarm_notify_snooze_text,
						AlarmUtil.formatTime(mContext, c)), broadcast);
		n.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		nm.notify(id, n);

		String displayTime = mContext.getString(R.string.alarm_alert_snooze_set,
				snoozeMinutes);
		Toast.makeText(mContext, displayTime, Toast.LENGTH_SHORT).show();
		}else{
			int minutes = calendar.get(Calendar.MINUTE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int times = (hour * 60)+ minutes + time;
			AlarmUtil.addAlarm(mContext, times, 0, "",false, true, "",1);
			
		}
		mContext.stopService(new Intent(AlarmUtil.ALARM_ALERT_ACTION));
	}

	public static void stop(int id,boolean killed){
		if (!killed) {
			NotificationManager nm = getNotificationManager();
			nm.cancel(id);
			mContext.stopService(new Intent(AlarmUtil.ALARM_ALERT_ACTION));
		}
	}

	/*	public static void dismiss(){
		Intent intent = new Intent(AlarmUtil.ALARM_DISMISS_ALARM_WARN);
		mContext.startActivity(intent);
	}*/
	
	
	private static Alarm getLastRemindOrAlarm(Context context,int tab){
		Alarm alarm = null;
		List<Alarm> alarms = new ArrayList<Alarm>();
		Calendar calendar = Calendar.getInstance();
		int nowMintute = 0;
		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		int remindMinute = 0;
		int remindHour = 0;
		int max = 0;
		Cursor cursor = getFilteredAlarmByTabEna(context.getContentResolver(),tab,0);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Alarm a = new Alarm(cursor);
							alarms.add(a);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		if(alarms.size() != 0){
			max = alarms.get(0).id;
			for(int m = 0;m<alarms.size();m++){
				if(max <= alarms.get(m).id)
					alarm = alarms.get(m);
			}
		}
		if(alarm != null){
			nowMintute = calendar.get(Calendar.MINUTE);
			remindHour = alarm.hour;
			remindMinute = alarm.minutes;
			if(nowHour == remindHour){
				if((nowMintute - remindMinute)>10){
					return null;
				}
			}else if((nowHour - remindHour) == 1){
				int endMinutes = (60 - remindMinute);
				if((endMinutes + nowMintute) > 10){
					return null ;
				}
			}else{
				return null;
			}
		}
		
		return alarm;
	}
	

	private static NotificationManager getNotificationManager() {
		return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}


	public static void setIsFirstAlarm(boolean b,Context context){
		AlarmUtil.isFirstAlarm = b ;
		SharedPreferences preferences = context.getSharedPreferences("shared_data", 0);
		Editor editor = preferences.edit();
		editor.putBoolean(IS_FIRST_ALARM,b);
		editor.commit();
	}

	public static boolean getIsFirstAlarm(Context context){
		SharedPreferences preferences = context.getSharedPreferences("shared_data", 0);
		isFirstAlarm = preferences.getBoolean(IS_FIRST_ALARM, false);
		return isFirstAlarm;
	}

	public static boolean isFirstAlarm(int id ,Context context){
		Alarm alarm = AlarmUtil.calculateFirstAlarm(context);
		return id == alarm.id;
	}

	public static void sendMsgToServer(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("command", "firstAlarm");
			jsonObject.put("data_type","command");


			MsgAsk msgAsk = new MsgAsk(jsonObject);
			byte[] data = msgAsk.prepareRawData();
			Bundle bundle = new Bundle();
			bundle.putByteArray("data", data);

			Message msg = new Message();
			msg.what = MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER;
			msg.setData(bundle);
			mHandler.sendMessage(msg);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




}
