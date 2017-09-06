package com.viash.voicelib.utils.alarm;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.viash.voicelib.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class Alarm implements Parcelable {
    public static final Parcelable.Creator<Alarm> CREATOR
            = new Parcelable.Creator<Alarm>() {
                public Alarm createFromParcel(Parcel p) {
                    return new Alarm(p);
                }

                public Alarm[] newArray(int size) {
                    return new Alarm[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeInt(tab);
        p.writeInt(silent ? 1 : 0);
        p.writeString(rTime);
        /*p.writeInt(year);
        p.writeInt(month);
        p.writeInt(day);
*/    }
    public static class Columns implements BaseColumns {
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.viash.voice_assistant.db/alarm");
        public static final String HOUR = "hour";
        public static final String MINUTES = "minutes";
        public static final String DAYS_OF_WEEK = "daysofweek";
        public static final String ALARM_TIME = "alarmtime";
        public static final String ENABLED = "enabled";
        public static final String VIBRATE = "vibrate";
        public static final String MESSAGE = "message";
        public static final String TAB     = "tab";
        public static final String RTIME   = "remindtime";
     /*   public static final String YEAR    = "year";
        public static final String MONTH   = "month";  
        public static final String DAY     =  "day";*/
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";
        public static final String WHERE_ENABLED = ENABLED + "=1";
        static final String[] ALARM_QUERY_COLUMNS = {
            _ID, HOUR, MINUTES, DAYS_OF_WEEK, ALARM_TIME,
            ENABLED, VIBRATE, MESSAGE ,RTIME,TAB};

        public static final int ALARM_ID_INDEX = 0;
        public static final int ALARM_HOUR_INDEX = 1;
        public static final int ALARM_MINUTES_INDEX = 2;
        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
        public static final int ALARM_TIME_INDEX = 4;
        public static final int ALARM_ENABLED_INDEX = 5;
        public static final int ALARM_VIBRATE_INDEX = 6;
        public static final int ALARM_MESSAGE_INDEX = 7;
        public static final int ALARM_REMINDTIME_INDEX=8;
        public static final int ALARM_TAB_INDEX = 9;
    /*    public static final int ALARM_TIME_INDEX = 9;
        public static final int ALARM_MONTH_INDEX = 10;
        public static final int ALARM_DAY_INDEX = 11;*/
        
    }
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes;
    public DaysOfWeek daysOfWeek;
    public long       time;
    public String     rTime;
    public boolean    vibrate;
    public String     label;
    public boolean    silent;
    public  int       tab;
/*    public int        year;
    public int        month;
    public int        day;*/

    public Alarm(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        rTime = c.getString(Columns.ALARM_REMINDTIME_INDEX);
        tab = c.getInt(Columns.ALARM_TAB_INDEX);
        /*year = c.getInt(Columns.ALARM_YEAR_INDEX);
        month = c.getInt(Columns.ALARM_MONTH_INDEX);
        day = c.getInt(Columns.ALARM_DAY_INDEX);*/
        }
   
    public JSONObject toJSonObject()
	{
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", "" + id);
			obj.put("time", "" + time);
			obj.put("repeat", "" + daysOfWeek.mDays);
			if(label != null)
				obj.put("title",label);
			if(vibrate)
				obj.put("vibrate", "1");
			else
				obj.put("vibrate", "0");
			
			if(enabled)
				obj.put("enabled", "1");
			else
				obj.put("enabled", "0");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;			
	}
    
    public JSONObject getRemindJsonObject(){
    	
    	JSONObject object = new JSONObject();
    	
    	
    	 try {
			object.put("id",id);
			object.put("advance_time",0);
			object.put("title", label);
			object.put("Availability",1);
			object.put("end_time", rTime);
			object.put("all_day",0);
			object.put("start_time", rTime);
			object.put("reminder_method",1);
			object.put("has_alarm",1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	return object;
    }
    
    public Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();
        tab = p.readInt();
        silent = p.readInt() == 1;
        rTime = p.readString();
        /*year = p.readInt();
        month = p.readInt();
        day = p.readInt();*/
    }

    public Alarm() {
        id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        vibrate = true;
        daysOfWeek = new DaysOfWeek(0);  
    }

    public String getLabelOrDefault(Context context) {
        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
        }
        return label;
    }
   public static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
		public int mDays;

		public DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ? context.getText(R.string.never).toString()
						: "";
			}

			// every day
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1)
					dayCount++;
				days >>= 1;
			}

			// short or long form?
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs
					.getWeekdays();

			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					ret.append(dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(context.getText(R.string.day_concat));
				}
			}
			return ret.toString();
		}

		private boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }
}
