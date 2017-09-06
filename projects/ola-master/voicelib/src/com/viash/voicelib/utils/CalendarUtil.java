package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;

public class CalendarUtil {
	//private static final String TAG = "CalendarUtil";
	private static final int MAX_QUERY_COUNT = 99;
	public static final String ALARM_PREFIX = "闹钟:";
	protected static String mCalenderUrl;
	protected static String mCalendaerEventUrl;
	protected static String mCalendarRemiderUrl;
	protected static String mCalendarInstancesWhen;
	protected static String mCalendarAttendeeUrl;
	
	private static String FIELD_CALENDAR_ID = "calendar_id";
	private static String FIELD_BEGIN = "begin";
	private static String FIELD_END = "end";
	private static String FIELD_EVENT_ID = "event_id";
	private static String FIELD_ID = "_id";
	private static String FIELD_TITLE = "title";
	
	private static String FIELD_EVENT_LOCATION = "eventLocation";
	private static String FIELD_DESCRIPTION = "description";
	private static String FIELD_RRULE = "rrule";
	private static String FIELD_DATE_START = "dtstart";
	private static String FIELD_HAS_ALARM = "hasAlarm";
	
	private static String FIELD_ALL_DAY = "allDay";
	private static String FIELD_DURATION = "duration";
	private static String FIELD_DATE_END = "dtend";
	private static String FIELD_EVENT_TIMEZONE = "eventTimezone";	
	private static String FIELD_HAS_ATTENDEE_DATA = "hasAttendeeData";
	private static String FIELD_ATTENDEE_NAME = "attendeeName";
	private static String FIELD_MINUTES = "minutes";
	private static String FIELD_METHOD = "method";
	private static String FIELD_AVAILABILITY = "availability";
	
	private static String FIELD_DELETED = "deleted";
	
	private static int METHOD_ALERT = 1;

	static
	{
		init();
	}
	
	public static void init()
	{
		if(Integer.parseInt(Build.VERSION.SDK) >=8) {		 
			mCalenderUrl ="content://com.android.calendar/calendars";
			mCalendaerEventUrl ="content://com.android.calendar/events";
			mCalendarRemiderUrl ="content://com.android.calendar/reminders";
			mCalendarInstancesWhen = "content://com.android.calendar/instances/when";
			mCalendarAttendeeUrl = "content://com.android.calendar/attendees";
		}
		else
		{
			mCalenderUrl ="content://calendar/calendars";
			mCalendaerEventUrl ="content://calendar/events";
			mCalendarRemiderUrl ="content://calendar/reminders";
			mCalendarInstancesWhen = "content://calendar/instances/when";
			mCalendarAttendeeUrl = "content://calendar/attendees";
		}
	}
	
	public static boolean deleteEvents(Context context, long[] ids)
	{
		boolean ret = false;
		try
		{
			if(ids.length > 0)
			{
				String selection = "";
				for(int i = 0; i < ids.length; i++)
				{
					if(selection.length() > 0)
						selection += " or ";
					
					selection += FIELD_ID + "=" + ids[i];				
				}
				
				int rows = context.getContentResolver().delete(Uri.parse(mCalendaerEventUrl), selection, null);
				if(rows > 0)
					ret = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public static List<EventData> queryAlarmEvent(Context context)
	{
		List<EventData> lstEvent = new ArrayList<CalendarUtil.EventData>();
		String selection = FIELD_TITLE + " LIKE " + "\'" + ALARM_PREFIX + "%\'";
		Cursor eventCursor = null;
        try
        {
        	eventCursor = context.getContentResolver().query(Uri.parse(mCalendaerEventUrl),null,selection, null,null);
        }
        catch(SQLiteException e)
        {
        	selection = selection.replace(FIELD_ID, "events." + FIELD_ID);
        	eventCursor = context.getContentResolver().query(Uri.parse(mCalendaerEventUrl),null,selection, null,null);
        }
        
        if(eventCursor != null)
        {
	        if(eventCursor.moveToFirst())
	        {
	        	int colId = eventCursor.getColumnIndex(FIELD_ID);
	        	int colTitle = eventCursor.getColumnIndex(FIELD_TITLE);
	        	int colEventLocation = eventCursor.getColumnIndex(FIELD_EVENT_LOCATION);
	        	int colDescription = eventCursor.getColumnIndex(FIELD_DESCRIPTION);
	        	int colRrule = eventCursor.getColumnIndex(FIELD_RRULE);
	        	int colDateStart = eventCursor.getColumnIndex(FIELD_DATE_START);
	        	int colHasAlarm = eventCursor.getColumnIndex(FIELD_HAS_ALARM);
	        	int colAllDay = eventCursor.getColumnIndex(FIELD_ALL_DAY);
	        	int colDuration = eventCursor.getColumnIndex(FIELD_DURATION);
	        	int colDateEnd = eventCursor.getColumnIndex(FIELD_DATE_END);
	        	int colTimeZone = eventCursor.getColumnIndex(FIELD_EVENT_TIMEZONE);
	        	int colDeleted = eventCursor.getColumnIndex(FIELD_DELETED);
	        	
	        	if(colTitle != -1 && colEventLocation != -1 && colDescription != -1 &&
	        			colRrule != -1 && colDateStart != -1 && colHasAlarm != -1 &&
	        			colAllDay != -1 && colDuration != -1 && colDateEnd != -1 &&
	        			colTimeZone != -1 && colId != -1)
	        	{ 
		            do
		            {		           
		            	// In some mobile, record is not really deleted, but just set "deleted" to 1
		            	boolean deleted = false;
		            	if(colDeleted != -1)
		            	{
		            		if(eventCursor.getInt(colDeleted) != 0)
		            			deleted = true;
		            	}
	            	
		            	if(!deleted)
		            	{
			            	EventData eventData = new EventData();
			            	eventData.setmEventId(eventCursor.getLong(colId));
			            	String title = eventCursor.getString(colTitle);
			            	eventData.setmTitle(title);				            	
			            	eventData.setmLocation(eventCursor.getString(colEventLocation));
			            	eventData.setmDescription(eventCursor.getString(colDescription));
			            	eventData.setmRule(eventCursor.getString(colRrule));
			            	eventData.setmStart(eventCursor.getLong(colDateStart));
			            	eventData.setmHasAlarm(eventCursor.getInt(colHasAlarm) != 0);
			            	eventData.setmIsAllDay(eventCursor.getInt(colAllDay) != 0);
			            	eventData.setmDuration(eventCursor.getLong(colDuration));
			            	
		            		lstEvent.add(eventData);
		            	}
		            }while(eventCursor.moveToNext());
	        	}
	        }   
	        eventCursor.close(); 
        }
		return lstEvent;
	}
	
	protected static List<InstanceData> queryInstance(Context context, long startTime, long endTime)
	{
		List<InstanceData> lstInstance = new ArrayList<CalendarUtil.InstanceData>();
		long temptime = 0;
		try
		{
			if(endTime == 0)
				endTime = Long.MAX_VALUE;
			if (startTime == 0) {
				temptime = System.currentTimeMillis();
			}
			else {
				temptime = startTime;
			}
			String uri = mCalendarInstancesWhen + "/" + temptime + "/" + endTime;
			String projectionInstance[] = {FIELD_ID, FIELD_EVENT_ID, FIELD_BEGIN, FIELD_END};
	
	        Cursor instanceCursor = context.getContentResolver().query(Uri.parse(uri),projectionInstance,null,null, FIELD_BEGIN + " asc");
	        if(instanceCursor != null)
	        {
		        if(instanceCursor.moveToFirst())
		        {
		        	int colBegin = instanceCursor.getColumnIndex(FIELD_BEGIN);
		        	int colEnd = instanceCursor.getColumnIndex(FIELD_END);
		        	int colEventId = instanceCursor.getColumnIndex(FIELD_EVENT_ID);
		        	int colIntanceId = instanceCursor.getColumnIndex(FIELD_ID);
		        	
		        	if(colBegin != -1 && colEnd != -1 && colEventId != -1 && colIntanceId != -1)
		        	{
			            do
			            {
			            	long begin = instanceCursor.getLong(colBegin);
			            	long end = instanceCursor.getLong(colEnd);
			            	long eventId = instanceCursor.getLong(colEventId);
			            	long instanceId = instanceCursor.getLong(colIntanceId);
			            	
			            	InstanceData instanceData = new InstanceData();
			            	instanceData.setmStartTime(begin);
			            	instanceData.setmEndTime(end);
			            	instanceData.setmInstanceId(instanceId);
			            	instanceData.setmEventId(eventId);            	
			            	lstInstance.add(instanceData);
			            //	Log.e(TAG, "Begin:" + begin + " End:" + end + " eventId:" + eventId + " instanceId" + instanceId);			            	
			            }while(instanceCursor.moveToNext() && lstInstance.size() < MAX_QUERY_COUNT);
		        	}
		        }
		        
	        }
	        if (startTime == 0 && lstInstance != null && lstInstance.size() < MAX_QUERY_COUNT) {
	        	uri = mCalendarInstancesWhen + "/" + 0 + "/" + temptime;
	        	instanceCursor = context.getContentResolver().query(Uri.parse(uri),projectionInstance,null,null, FIELD_BEGIN + " desc");
	        	if(instanceCursor != null)
		        {
			        if(instanceCursor.moveToFirst())
			        {
			        	int colBegin = instanceCursor.getColumnIndex(FIELD_BEGIN);
			        	int colEnd = instanceCursor.getColumnIndex(FIELD_END);
			        	int colEventId = instanceCursor.getColumnIndex(FIELD_EVENT_ID);
			        	int colIntanceId = instanceCursor.getColumnIndex(FIELD_ID);
			        	
			        	if(colBegin != -1 && colEnd != -1 && colEventId != -1 && colIntanceId != -1)
			        	{
			        		do
				            {
				            	long begin = instanceCursor.getLong(colBegin);
				            	long end = instanceCursor.getLong(colEnd);
				            	long eventId = instanceCursor.getLong(colEventId);
				            	long instanceId = instanceCursor.getLong(colIntanceId);
				            	
				            	InstanceData instanceData = new InstanceData();
				            	instanceData.setmStartTime(begin);
				            	instanceData.setmEndTime(end);
				            	instanceData.setmInstanceId(instanceId);
				            	instanceData.setmEventId(eventId);
				            	lstInstance.add(instanceData);
				            }while(instanceCursor.moveToNext() && lstInstance.size() < MAX_QUERY_COUNT);
			        	}
			        }
		        }
	        }
	        instanceCursor.close();   
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	        
	    return lstInstance;		
	}
	
	protected static void queryEvent(Context context, HashMap<Long, CalendarUtil.EventData> eventMap, List<Long> lstEventId)
	{
		if(lstEventId != null && lstEventId.size() > 0)
		{
		    Cursor eventCursor = null;
		    try
		    {
		    	eventCursor = context.getContentResolver().query(Uri.parse(mCalendaerEventUrl),null,null, null,null);
		    }
		    catch(SQLiteException e)
		    {
		    	e.printStackTrace();
		    }
		    
		    if(eventCursor != null)
		    {
		        if(eventCursor.moveToFirst())
		        {
		        	int colId = eventCursor.getColumnIndex(FIELD_ID);
		        	int colTitle = eventCursor.getColumnIndex(FIELD_TITLE);
		        	int colEventLocation = eventCursor.getColumnIndex(FIELD_EVENT_LOCATION);
		        	int colDescription = eventCursor.getColumnIndex(FIELD_DESCRIPTION);
		        	int colRrule = eventCursor.getColumnIndex(FIELD_RRULE);
		        	int colDateStart = eventCursor.getColumnIndex(FIELD_DATE_START);
		        	int colHasAlarm = eventCursor.getColumnIndex(FIELD_HAS_ALARM);
		        	int colAllDay = eventCursor.getColumnIndex(FIELD_ALL_DAY);
		        	int colDuration = eventCursor.getColumnIndex(FIELD_DURATION);
		        	int colDateEnd = eventCursor.getColumnIndex(FIELD_DATE_END);
		        	int colTimeZone = eventCursor.getColumnIndex(FIELD_EVENT_TIMEZONE);
		        	int colHasAttendee = eventCursor.getColumnIndex(FIELD_HAS_ATTENDEE_DATA);
		        	int colAvailability = eventCursor.getColumnIndex(FIELD_AVAILABILITY);
		        	
		        	if(colTitle != -1 && colEventLocation != -1 && colDescription != -1 &&
		        			colRrule != -1 && colDateStart != -1 && colHasAlarm != -1 &&
		        			colAllDay != -1 && colDuration != -1 && colDateEnd != -1 &&
		        			colTimeZone != -1 && colId != -1)
		        	{        			
			        	
			            do
			            {
			            	//showCursor(eventCursor);
			            	long idEvent = eventCursor.getLong(colId);
			            	if(lstEventId.contains(idEvent))
			            	{			            	
				            	EventData eventData = new EventData();
				            	eventData.setmEventId(eventCursor.getLong(colId));
				            	String title = eventCursor.getString(colTitle);
				            	eventData.setmTitle(title);				            	
				            	eventData.setmLocation(eventCursor.getString(colEventLocation));
				            	eventData.setmDescription(eventCursor.getString(colDescription));
				            	eventData.setmRule(eventCursor.getString(colRrule));
				            	eventData.setmStart(eventCursor.getLong(colDateStart));
				            	eventData.setmHasAlarm(eventCursor.getInt(colHasAlarm) != 0);
				            	eventData.setmIsAllDay(eventCursor.getInt(colAllDay) != 0);
				            	eventData.setmDuration(eventCursor.getLong(colDuration));
				            	
				            	if(colAvailability != -1)
				            		eventData.setmAvailability(eventCursor.getInt(colAvailability));
		            	
				            	if(eventCursor.getInt(colHasAttendee) != 0)
				            	{
				            		try
				            		{
					            		String selectionAttendee = FIELD_EVENT_ID + "=" + eventData.getmEventId();
					            		String projectionAttendee[] = new String[1];
					            		projectionAttendee[0] = FIELD_ATTENDEE_NAME;
					            		Cursor attendeeCursor = context.getContentResolver().query(Uri.parse(mCalendarAttendeeUrl),null,selectionAttendee, null,null);
					            		if(attendeeCursor != null)
					            		{
					            			if(attendeeCursor.moveToFirst())
					            			{
					            				int colAttendeeName = attendeeCursor.getColumnIndex(FIELD_ATTENDEE_NAME);
					            				if(colAttendeeName != -1)
					            				{
						            				List<String> lstAttendee = new ArrayList<String>();
						            				do
						            				{
						            					ProviderUtil.showCursor(attendeeCursor);
						            					String attendeeName = attendeeCursor.getString(colAttendeeName);
						            					if(attendeeName != null && attendeeName.length() > 0)
						            					{
						            						lstAttendee.add(attendeeName);				            						
						            					}
						            				}while(attendeeCursor.moveToNext());
						            				eventData.setmLstAttendee(lstAttendee);
					            				}
					            			}
					            			attendeeCursor.close();
					            		}
				            		}
				            		catch (Exception e) {
										e.printStackTrace();
									}						
				            	}
		            	
		
			            		if(eventData.ismHasAlarm())
			            		{
			            			try
			            			{					            		
					            		String selectionReminder = FIELD_EVENT_ID + "=" + eventData.getmEventId();
					            		final String[] columnsToReturn = new String[] {
					            				FIELD_EVENT_ID,
					            				FIELD_MINUTES,
					            				FIELD_METHOD
					            	        };
			
					            		Cursor cursorReminder = context.getContentResolver().query(Uri.parse(mCalendarRemiderUrl),columnsToReturn,selectionReminder, null,null);
					            		if(cursorReminder != null)
					            		{
					            			if(cursorReminder.moveToFirst())
					            			{
					            				int colAdvaceTime = cursorReminder.getColumnIndex(FIELD_MINUTES);
					            				if(colAdvaceTime != -1)
					            				{
					            					eventData.setmAdvanceTime(cursorReminder.getLong(colAdvaceTime));					            					
					            				}
					            				int colMethod = cursorReminder.getColumnIndex(FIELD_METHOD);
					            				if(colMethod != -1)
					            				{
					            					eventData.setmReminderMethod(cursorReminder.getInt(colMethod));
					            				}
					            			}
					            			cursorReminder.close();
					            		}
				            		}
				            		catch(Exception e)
				            		{
				            			e.printStackTrace();
				            		}
				            	}
		            	
			            		eventMap.put(eventData.getmEventId(), eventData);
			            	}
			            }while(eventCursor.moveToNext());
		        	}
		        }   
		        eventCursor.close();
		    }
		}
	}
	
	public static List<InstanceData> queryCalendar(Context context, long startTime, long endTime)
	{
		List<InstanceData> lstInstance = queryInstance(context, startTime, endTime);

		if(lstInstance != null && lstInstance.size() > 0)
		{
			List<Long> lstEventId = new ArrayList<Long>();
		    for(int i = 0; i < lstInstance.size(); i++)
		    {
		    	long idEvent = lstInstance.get(i).getmEventId();

		    	if(!lstEventId.contains(idEvent))
		    	{
		    		lstEventId.add(idEvent);
		    	}
		    }
			    
			HashMap<Long, EventData> mapEvent = new HashMap<Long, CalendarUtil.EventData>();
			queryEvent(context, mapEvent, lstEventId);
			        
	        for(int j = lstInstance.size() - 1; j >= 0; j--)
	        {
	        	InstanceData instanceData = lstInstance.get(j);
	        	EventData eventData = mapEvent.get(instanceData.getmEventId());
	        	if(eventData != null)
	        	{
	        		instanceData.setmEventData(eventData);
	        		
	        		//In howe's htc mobile, there is no availability field
	        		//if(eventData.getmAvailability() == 1)
	        		{
		        		if(instanceData.getmEndTime() == instanceData.getmStartTime() + 60000)
		        		{
		        			instanceData.setmEndTime(instanceData.getmStartTime());
		        		}
	        		}
	        	}
	        	else
	        	{
	        		lstInstance.remove(j);
	        	}
	        }
		}

		return lstInstance;
	}
	
	protected static String getCalendarUser(Context context)
	{
		String idCalendar = null;
	
		try
		{
			Cursor userCursor = context.getContentResolver().query(Uri.parse(mCalenderUrl),null,null,null,null);
				
	        if(userCursor != null)
	        {	        	
		        if(userCursor.getCount() >0) {
		            userCursor.moveToFirst();
//		            for(int i = 0; i < userCursor.getColumnCount(); i++)
//		            {
//		            	Log.e(TAG, userCursor.getColumnName(i) + ":" + userCursor.getString(i));
//		            }
		            idCalendar = userCursor.getString(userCursor.getColumnIndex("_id"));
		        }      
		        userCursor.close();
	        }
	        if(idCalendar == null)
	        {
	        	ContentValues values = new ContentValues();
	        	values.put("account_name", "LocalCalendar");
	        	values.put("account_type", "LOCAL");
	        	values.put("name", "LocalCalendar");
	        	values.put("calendar_displayName", "LocalCalendar");
	        	values.put("calendar_color", 0xff0000ff);
	        	values.put("calendar_access_level", 700);
	        	values.put("ownerAccount", true);
	        	values.put("visible", 1);
	        	//Uri uriNew = context.getContentResolver().insert(Uri.parse(mCalenderUrl), values);
	        	Uri calUri = Uri.parse(mCalenderUrl);
	        	calUri = calUri.buildUpon()
		        	    .appendQueryParameter("caller_is_syncadapter", "true")
		        	    .appendQueryParameter("account_name", "LocalCalendar")
		        	    .appendQueryParameter("account_type", "LOCAL")
		        	    .build();
		        Uri uriNew = context.getContentResolver().insert(calUri, values);
    	
	        	
	        	if(uriNew != null)
	        		idCalendar = uriNew.getLastPathSegment();
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        
        return idCalendar;
	}
	
	public static boolean modifyCalendarEvent(Context context, EventData data)
	{
		if(data.getmEventId() != 0)
		{
			long[] ids = new long[1];
			ids[0] = data.getmEventId();
			deleteEvents(context, ids);
		}
		data.setmEventId(0);
		return addCalendarEvent(context, data);		
	}
		
	public static boolean addCalendarEvent(Context context, EventData data)
	{
		boolean ret = false;
		String calId ="";
		boolean repeat = false;
		int retry = 0;
		
		try
		{
			calId = getCalendarUser(context);

	        if(calId != null)
	        {
		        ContentValues event =new ContentValues();
		        event.put(FIELD_CALENDAR_ID, calId);		        
		        
		        if(data.getmTitle() != null)
		        	event.put(FIELD_TITLE, data.getmTitle());
		        
		        if(data.getmLocation() != null)
		        	event.put(FIELD_EVENT_LOCATION, data.getmLocation());
		
		        if(data.getmDescription() != null)
		        	event.put(FIELD_DESCRIPTION, data.getmDescription());
		        
		        if(data.getmRule() != null)
		        {
		        	event.put(FIELD_RRULE, data.getmRule());
		        	repeat = true;
		        }
		
		        event.put(FIELD_DATE_START, data.getmStart());
		        
		        if(data.ismHasAlarm())
		        	event.put(FIELD_HAS_ALARM,1);
		        else
		        	event.put(FIELD_HAS_ALARM,0);
	        
		        if(data.ismIsAllDay())
		        {
		        	event.put(FIELD_ALL_DAY,1);			        	
		        	//if(data.getmDuration() != 0)
			        {
			        	event.put(FIELD_DURATION, "P" + (data.getmDuration()+  + DateUtils.DAY_IN_MILLIS - 1) / DateUtils.DAY_IN_MILLIS + "D");
			        }
		        	event.put(FIELD_DATE_END, data.getmStart() + data.getmDuration());
		        }
		        else
		        {
		        	event.put(FIELD_ALL_DAY,0);
		        	//if(data.getmDuration() != 0)
			        {
			        	event.put(FIELD_DURATION, "P" + data.getmDuration() / DateUtils.SECOND_IN_MILLIS + "S");
			        }
		        	event.put(FIELD_DATE_END, data.getmStart() + data.getmDuration());
		        }
		        
		        // One and only one can be set for "dtend" and "duration"
		        if(repeat)
		        	event.remove(FIELD_DATE_END);
		        else
		        	event.remove(FIELD_DURATION);
	        
		        event.put(FIELD_EVENT_TIMEZONE, TimeZone.getDefault().getID());
		        
		        List<String> lstAttendee = data.getmLstAttendee();
		        if(lstAttendee != null && lstAttendee.size() > 0)
		        {
		        	event.put(FIELD_HAS_ATTENDEE_DATA, 1);
		        }
		        else
		        {
		        	event.put(FIELD_HAS_ATTENDEE_DATA, 0);
		        }
		        
		        event.put(FIELD_AVAILABILITY, data.getmAvailability());
		
			    Uri uriEvent = Uri.parse(mCalendaerEventUrl);

	        	if(data.getmEventId() != 0)
	        	{
	        		String selection = FIELD_ID + "=" + data.getmEventId();
	        		
	        		
	        		//In howe's htc phone,there is no FIELD_AVAILABILITY
	        		while(retry < 2)
	        		{
		        		try
		        		{
		        			context.getContentResolver().update(uriEvent, event, selection, null);
		        			ret = true;
		        			break;
		        		}
		        		catch(Exception e)
		        		{
		        			retry++;
		        			event.remove(FIELD_AVAILABILITY);
		        			uriEvent = Uri.parse(mCalendaerEventUrl);
		        			ret = false;
		        		}
	        		}
	        	}
	        	else
	        	{
			        Uri newEvent = null;
			        
			        while(retry < 2)
	        		{
		        		try
		        		{
		        			newEvent = context.getContentResolver().insert(uriEvent, event);
		        			break;
		        		}
		        		catch(Exception e)
		        		{
		        			retry++;
		        			event.remove(FIELD_AVAILABILITY);
		        			uriEvent = Uri.parse(mCalendaerEventUrl);
		        		}
	        		}
			
			        if(newEvent != null)
			        {
				        long idEvent = Long.parseLong(newEvent.getLastPathSegment());
				        ContentValues values =new ContentValues();
				        values.put(FIELD_EVENT_ID, idEvent);
				        values.put(FIELD_MINUTES, data.getmAdvanceTime());
				        values.put(FIELD_METHOD, data.getmReminderMethod());
				        
				        context.getContentResolver().insert(Uri.parse(mCalendarRemiderUrl), values);
				        ret = true;				        
				        
				        if(lstAttendee != null && lstAttendee.size() > 0)
				        {
				        	for(int i = 0 ; i < lstAttendee.size(); i++)
				        	{
					        	String attendeeName = lstAttendee.get(i);
					        	if(attendeeName != null && attendeeName.length() > 0)
					        	{
				        			ContentValues attendee =new ContentValues();
				        			attendee.put(FIELD_EVENT_ID, idEvent);
						        	attendee.put(FIELD_ATTENDEE_NAME, attendeeName);
						        	Uri uriAttendee = Uri.parse(mCalendarAttendeeUrl);
						        	context.getContentResolver().insert(uriAttendee, attendee);
					        	}				        	
				        	}
				        }
			        }
	        	}
	        }        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			        
        return ret;
	}
	
	public static class InstanceData
	{
		protected long mInstanceId;
		protected long mStartTime;
		protected long mEndTime;
		protected long mEventId;
		protected EventData mEventData;
		public long getmInstanceId() {
			return mInstanceId;
		}
		public long getmStartTime() {
			return mStartTime;
		}
		public long getmEndTime() {
			return mEndTime;
		}
		public void setmInstanceId(long mInstanceId) {
			this.mInstanceId = mInstanceId;
		}
		public void setmStartTime(long mStartTime) {
			this.mStartTime = mStartTime;
		}
		public void setmEndTime(long mEndTime) {
			this.mEndTime = mEndTime;
		}

		public long getmEventId() {
			return mEventId;
		}
		public EventData getmEventData() {
			return mEventData;
		}
		public void setmEventId(long mEventId) {
			this.mEventId = mEventId;
		}
		public void setmEventData(EventData mEventData) {
			this.mEventData = mEventData;
		}
				
		public JSONObject toJsonObject() 
		{		
			JSONObject obj = null;
			if(mEventData != null)
			{
				obj = new JSONObject();
				try {
					obj.put("id", mEventData.getmEventId());					
					obj.put("start_time", mStartTime);
					obj.put("end_time", mEndTime);
					if(mEventData.getmTitle() != null)
						obj.put("title", mEventData.getmTitle());
					if(mEventData.getmLocation() != null)
						obj.put("location", mEventData.getmLocation());
					if(mEventData.getmDescription() != null)
						obj.put("description", mEventData.getmDescription());
					if(mEventData.getmRule() != null && mEventData.getmRule().length() > 0)
						obj.put("repeat_rule", mEventData.getmRule());
					obj.put("all_day", (mEventData.ismIsAllDay() ? "1" : "0"));
					obj.put("has_alarm", (mEventData.ismHasAlarm() ? "1" : "0"));
					obj.put("advance_time", mEventData.getmAdvanceTime());
					obj.put("reminder_method", mEventData.getmReminderMethod());
					obj.put("Availability", mEventData.getmAvailability());
					
					List<String> lstAddendee = mEventData.mLstAttendee;
					if(lstAddendee != null && lstAddendee.size() > 0)
					{
						JSONArray array = new JSONArray();
						for(int i = 0; i < lstAddendee.size(); i++)
						{
							array.put(lstAddendee.get(i));
						}
						obj.put("attendees", array);
					}
				} catch (JSONException e) {
					obj = null;
					e.printStackTrace();
				}
			}			
			return obj;
		}
	}
	
	public static class EventData
	{		
		protected long mEventId;
		protected String mTitle;
		protected String mLocation;
		protected String mDescription;
		protected long mStart;
		protected boolean mHasAlarm= true;
		protected long mAdvanceTime = 0;;
		protected boolean mIsAllDay = false;
		protected String mRule;
		protected long mDuration = 0;
		protected boolean mIsRepeated = false;
		protected long mUntil = 0;
		protected int mReminderMethod = METHOD_ALERT;
		protected List<String> mLstAttendee;	
		protected int mAvailability = 0;
		
		public long getmEventId() {
			return mEventId;
		}
		public void setmEventId(long mEventId) {
			this.mEventId = mEventId;
		}
		public String getmTitle() {
			return mTitle;
		}
		public void setmTitle(String mTitle) {
			this.mTitle = mTitle;
		}
		public String getmDescription() {
			return mDescription;
		}
		public void setmDescription(String mDescription) {
			this.mDescription = mDescription;
		}
		public long getmStart() {
			return mStart;
		}
		public void setmStart(long mStart) {
			this.mStart = mStart;
		}

		public boolean ismHasAlarm() {
			return mHasAlarm;
		}
		public void setmHasAlarm(boolean mHasAlarm) {
			this.mHasAlarm = mHasAlarm;
		}
		public long getmAdvanceTime() {
			return mAdvanceTime;
		}
		public void setmAdvanceTime(long mAdvanceTime) {
			this.mAdvanceTime = mAdvanceTime;
		}
		public String getmLocation() {
			return mLocation;
		}
		public void setmLocation(String mLocation) {
			this.mLocation = mLocation;
		}
		public boolean ismIsAllDay() {
			return mIsAllDay;
		}
		public void setmIsAllDay(boolean mIsAllDay) {
			this.mIsAllDay = mIsAllDay;
		}
		public String getmRule() {
			return mRule;
		}
		public void setmRule(String mRule) {
			this.mRule = mRule;
		}
		public boolean ismIsRepeated() {
			return mIsRepeated;
		}
		public void setmIsRepeated(boolean mIsRepeated) {
			this.mIsRepeated = mIsRepeated;
		}
		public long getmDuration() {
			return mDuration;
		}
		public void setmDuration(long mDuration) {
			this.mDuration = mDuration;
		}
		public long getmUntil() {
			return mUntil;
		}
		public void setmUntil(long mUntil) {
			this.mUntil = mUntil;
		}
		public List<String> getmLstAttendee() {
			return mLstAttendee;
		}
		public void setmLstAttendee(List<String> mLstAttendee) {
			this.mLstAttendee = mLstAttendee;
		}
		public int getmReminderMethod() {
			return mReminderMethod;
		}
		public void setmReminderMethod(int mReminderMethod) {
			this.mReminderMethod = mReminderMethod;
		}
		public int getmAvailability() {
			return mAvailability;
		}
		public void setmAvailability(int mAvailability) {
			this.mAvailability = mAvailability;
		}	
		
	}
	
	//Open calendar app and jump to specified date(month)
	public static boolean openCalendarApp(Context context, int year, int month)
	{  
		GregorianCalendar calDate = new GregorianCalendar(year - 1900, month,1);		
		Intent calIntent = context.getPackageManager().getLaunchIntentForPackage("com.android.calendar");	
		calIntent.putExtra("allDay", true);
		calIntent.putExtra("beginTime", calDate.getTimeInMillis());
		calIntent.putExtra("endTime", calDate.getTimeInMillis());
		calIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(calIntent);				
		return true;
	}
	
	public static void queryCalendarAlert(Context context)
	{
		 Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendar_alerts"), null,null,null, null);
		 if(cursor != null)
		 {
			 if(cursor.moveToFirst())
			 {
				 do
				 {
					 ProviderUtil.showCursor(cursor);
				 }while(cursor.moveToNext());
			 }
		 }
	}
}
