package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;

public class CallUtil {
	private static int MAX_QUERY_COUNT = 100;
	//private static final String TAG = "CallUtil";
	public static class SmsData extends PhoneData
	{
		protected String mContent;

		public String getmContent() {
			return mContent;
		}

		public void setmContent(String mContent) {
			this.mContent = mContent;
		}
		
		public JSONObject toJSonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", mId);
				if(mContactName != null)
					obj.put("contact_id", mContactName);
				if(mContactPhone != null)
					obj.put("contact_number", mContactPhone);
				if(mTime != 0)
					obj.put("time", "" + mTime);
				if(mTime != 0)
					obj.put("sms_type", mType);
				if(mContent != null)
					obj.put("content", mContent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return obj;
		}
	}
	
	public static class PhoneData
	{
		public static final int TYPE_SENT = 2;
		public static final int TYPE_UNREAD = 3;
		public static final int TYPE_READ = 1;
		public static final int TYPE_ALL_RECEIVED = 4;
		public static final int TYPE_ALL = 5;
		protected long mId;
		protected String mContactName;
		protected String mContactPhone;
		protected long mTime;		
		protected int mType = 1;
		public long getmId() {
			return mId;
		}
		public void setmId(long mId) {
			this.mId = mId;
		}
		public String getmContactName() {
			return mContactName;
		}
		public void setmContactName(String mContactName) {
			this.mContactName = mContactName;
		}		

		public String getmContactPhone() {
			return mContactPhone;
		}
		public void setmContactPhone(String mContactPhone) {
			this.mContactPhone = mContactPhone;
		}
		public long getmTime() {
			return mTime;
		}
		public void setmTime(long mTime) {
			this.mTime = mTime;
		}

		public int getmType() {
			return mType;
		}
		public void setmType(int mType) {
			this.mType = mType;
		}
		
		public JSONObject toJSonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", mId);
				if(mContactName != null)
					obj.put("contact_name", mContactName);
				if(mContactPhone != null)
					obj.put("contact_number", mContactPhone);
				if(mTime != 0)
					obj.put("time", "" + mTime);
				if(mTime != 0)
					obj.put("call_type", mType);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return obj;
		}
	}
	
	public static List<PhoneData> getMissedCalls(Context context) {
	     List<PhoneData> lstPhone = null;
	     String[] projection = new String[] {Calls._ID, Calls.CACHED_NAME, Calls.NUMBER, Calls.DATE};
	     String selections = Calls.TYPE + "=" + Calls.MISSED_TYPE;
	     Cursor callCursor = context.getContentResolver().query(Calls.CONTENT_URI, projection, selections, null, Calls.DEFAULT_SORT_ORDER);
	     if (callCursor != null && callCursor.moveToFirst()) {
	    	 lstPhone = new ArrayList<CallUtil.PhoneData>();
	         do
	         {
	        	 PhoneData data = new PhoneData();
	        	 data.setmId(callCursor.getLong(callCursor.getColumnIndex(Calls._ID)));
	        	 data.setmContactName(callCursor.getString(callCursor.getColumnIndex(Calls.CACHED_NAME)));
	        	 data.setmContactPhone(callCursor.getString(callCursor.getColumnIndex(Calls.NUMBER)));
	        	 data.setmTime(callCursor.getLong(callCursor.getColumnIndex(Calls.DATE)));	     
	        	 lstPhone.add(data);
	         }while(callCursor.moveToNext());
	         callCursor.close();
	     }
	     return lstPhone;
	 }
	public static List<PhoneData> queryCalls(Context context, int call_type, String contact_name, String contact_number, long start_time, long end_time) {
	     List<PhoneData> lstPhone = null;
	     String[] projection = new String[] {Calls._ID, Calls.CACHED_NAME, Calls.NUMBER, Calls.DATE, Calls.TYPE};
	     String selections = "";
	     
	     if(call_type == PhoneData.TYPE_SENT || call_type == PhoneData.TYPE_READ || call_type == PhoneData.TYPE_UNREAD)
	    	 selections += Calls.TYPE + "=" + call_type;
	     else if(call_type == PhoneData.TYPE_ALL_RECEIVED)
	    	 selections += Calls.TYPE + "!=" + PhoneData.TYPE_SENT;
	     
	     if(contact_name != null && contact_name.length() > 0)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " AND ";
	    	 selections += Calls.CACHED_NAME + "=\'" + contact_name + "\'";
	     }
	     
	     if(contact_number != null && contact_number.length() > 0)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " AND ";
	    	 selections += Calls.NUMBER + "=\'" + contact_number + "\'";
	     }
	     if(start_time != 0)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " AND ";
	    	 selections += Calls.DATE + ">=" + start_time;
	     }
	     if(end_time != 0)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " AND ";
	    	 selections += Calls.DATE + "<=" + end_time;
	     }
	     
	     try
	     {
		     Cursor callCursor = context.getContentResolver().query(Calls.CONTENT_URI, projection, selections, null, Calls.DEFAULT_SORT_ORDER);
		     if (callCursor != null && callCursor.moveToFirst()) {
		    	 lstPhone = new ArrayList<CallUtil.PhoneData>();
		         do
		         {
		        	 String phoneNumber = callCursor.getString(callCursor.getColumnIndex(Calls.NUMBER));
		        	 if(phoneNumber != null && phoneNumber.length() > 2)
		        	 {
			        	 PhoneData data = new PhoneData();
			        	 data.setmId(callCursor.getLong(callCursor.getColumnIndex(Calls._ID)));
			        	 data.setmContactName(callCursor.getString(callCursor.getColumnIndex(Calls.CACHED_NAME)));
			        	 data.setmContactPhone(phoneNumber);
			        	 data.setmTime(callCursor.getLong(callCursor.getColumnIndex(Calls.DATE)));	     
			        	 data.setmType(callCursor.getInt(callCursor.getColumnIndex(Calls.TYPE)));
			        	 
			        	 lstPhone.add(data);
		        	 }
		         }while(callCursor.moveToNext() && lstPhone.size() < MAX_QUERY_COUNT);
		         callCursor.close();
		     }
	     }
	     catch(Exception e)
	     {
	    	 e.printStackTrace();
	     }
	     return lstPhone;
	 }
	
	private static final String FIELD_SMS_ID = "_id";
	private static final String FIELD_SMS_PHONE = "address";
	private static final String FIELD_SMS_PERSON = "person";
	private static final String FIELD_SMS_DATE = "date";
	//private static final String FIELD_SMS_PROTOCOL = "protocol";	//0 SMS_RPOTO, 1 MMS_PROT
	private static final String FIELD_SMS_READ = "read"; //0 unread, 1 read
	private static final String FIELD_SMS_BODY = "body";
	private static final String FIELD_SMS_TYPE = "type";	//1 received，2 sent
		
	//_id => 短消息序号 如100  
	//thread_id => 对话的序号 如100  
	//address => 发件人地址，手机号.如+8613811810000  
	//person => 发件人，返回一个数字就是联系人列表里的序号，陌生人为null  
	//date => 日期  long型。如1256539465022  
	//protocol => 协议 0 SMS_RPOTO, 1 MMS_PROTO   
	//read => 是否阅读 0未读， 1已读   
	//status => 状态 -1接收，0 complete, 64 pending, 128 failed   
	//type => 类型 1是接收到的，2是已发出   
	//body => 短消息内容   
	//service_center => 短信服务中心号码编号。如+8613800755500  

			
	public static int SMS_STATE_SENT = 2;
	public static  int SMS_STATE_RECEIVED = 1;
	public static  int SMS_STATE_ALL = 0;
	public static  int SMS_READ_READ = 1;
	public static  int SMS_READ_UNREAD = 2;
	public static  int SMS_READ_ALL = 0;

	public static List<SmsData> querySms(Context context, String contact_name, String contact_number, long startTime, long endTime, int smsType, int max_count) {
	     List<SmsData> lstSms = null;
	     boolean phoneExist = false;
	     
	     //String[] projection = new String[] {FIELD_SMS_ID, FIELD_SMS_PHONE, FIELD_SMS_PERSON, FIELD_SMS_DATE, FIELD_SMS_BODY, FIELD_SMS_TYPE, FIELD_SMS_READ};
	     String selections = "";
	     if(max_count == 0)
	    	 max_count = MAX_QUERY_COUNT;
	     
	     if(smsType == PhoneData.TYPE_SENT)
	     {
	    	 selections += FIELD_SMS_TYPE + "=" + SMS_STATE_SENT;
	     }
	     else if(smsType == PhoneData.TYPE_UNREAD)
	     {
	    	 selections += FIELD_SMS_TYPE + "=" + SMS_STATE_RECEIVED + " AND " + FIELD_SMS_READ + "=\'0\'";
	     }
	     else if(smsType == PhoneData.TYPE_READ)
	     {
	    	 selections += FIELD_SMS_TYPE + "=" + SMS_STATE_RECEIVED + " AND " + FIELD_SMS_READ + "=\'1\'";
	     }
	     else if(smsType == PhoneData.TYPE_ALL_RECEIVED)
	     {
	    	 selections += FIELD_SMS_TYPE + "=" + SMS_STATE_RECEIVED;
	     }
	     
	     if(startTime != 0)
	     {
	    	 if(selections.length() > 0)
	    		 selections += " AND ";
	    	 selections += FIELD_SMS_DATE + ">=" + startTime;
	     }
	     
	     if(endTime != 0)
	     {
	    	 if(selections.length() > 0)
	    		 selections += " AND ";
	    	 selections += FIELD_SMS_DATE + "<=" + endTime;
	     }
	     
	     
	     
	     if(contact_number != null && contact_number.length() > 0)
	     {
	    	 phoneExist = true;
	    	 if(selections.length() > 0)
	    		 selections += " AND ";
	    	 selections += FIELD_SMS_PHONE + "=\'" + contact_number + "\'";
	     }
	     else if(contact_name != null && contact_name.length() > 0)
	     {
	    	 List<String> lstPhones = ContactUtil.findPhonesById(context, contact_name);
	    	 if(lstPhones != null && lstPhones.size() > 0)
	    	 {
	    		 if(selections.length() > 0)
		    		 selections += " AND (";
	    		 
	    		 boolean firstPhone = true;
	    		 for(int i = 0; i < lstPhones.size(); i++)
	    		 {
	    			 String phone_number = lstPhones.get(i);
	    			 
	    			 
	    			 if(phone_number.length() > 2)
	    			 {
	    				 phoneExist = true;
		    			 if(!firstPhone)
		    				 selections += " OR ";
		    			 selections += FIELD_SMS_PHONE + "=\'" + phone_number + "\'";
		    			 firstPhone = false;
	    			 }
	    			 
	    			 //String phone_Origin = phone_number;	
	    			 phone_number = phone_number.trim().replace("+", "");
	    			 phone_number = phone_number.trim().replace("-", "");
	    			 phone_number = phone_number.trim().replace(" ", "");
	    			 if(phone_number.length() > 8)
	    			 {
	    				 phoneExist = true;
		    			 if(!firstPhone)
		    				 selections += " OR ";
		    			 selections += FIELD_SMS_PHONE + " like \'%" + phone_number + "\'";
		    			 firstPhone = false;
	    			 }
	    		 }
	    		 selections += ")";
	    	 }
	     }
	     else
	     {
	    	 phoneExist = true;
	     }

	     if(phoneExist)
	     {
		     Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms"), null, selections, null, Calls.DEFAULT_SORT_ORDER);
		     if (cursor != null && cursor.moveToFirst()) {
		    	 lstSms = new ArrayList<CallUtil.SmsData>();
		         do
		         {
		        	 SmsData data = new SmsData();
		        	 data.setmId(cursor.getLong(cursor.getColumnIndex(FIELD_SMS_ID)));
		        	 //String personId = cursor.getString(cursor.getColumnIndex(FIELD_SMS_PERSON));
		        	 //data.setmContactName(personId);
		        	 /*if(personId != null)
		        	 {
		        		 data.setmContactName(ContactUtil.findContactNameById(context, personId));
		        	 }*/	        	 
		        	 data.setmContactPhone(cursor.getString(cursor.getColumnIndex(FIELD_SMS_PHONE)));
		        	 List<String> lstNames = ContactUtil.findContactNameByPhone(context, data.getmContactPhone());
		        	 if(lstNames != null && lstNames.size() > 0)
		        		 data.setmContactName(lstNames.get(0));
		        	 data.setmTime(cursor.getLong(cursor.getColumnIndex(FIELD_SMS_DATE)));	   
		        	 data.setmContent(cursor.getString(cursor.getColumnIndex(FIELD_SMS_BODY)));
		        	 
		        	 if(cursor.getInt(cursor.getColumnIndex(FIELD_SMS_TYPE)) == SMS_STATE_SENT)
		        	 {
		        		 data.setmType(PhoneData.TYPE_SENT);
		        	 }
		        	 else if(cursor.getInt(cursor.getColumnIndex(FIELD_SMS_READ)) != 0)
		        	 {
		        		 data.setmType(PhoneData.TYPE_READ);
		        	 }
		        	 else
		        	 {
		        		 data.setmType(PhoneData.TYPE_UNREAD);
		        	 }    	
		        	 
		        	 lstSms.add(data);
		         }while(cursor.moveToNext() && lstSms.size() < max_count);
		         cursor.close();
		     }
	     }	     

	     return lstSms;
	 }
	
	public static List<SmsData> querySms(Context context, long[] ids) {
	     List<SmsData> lstSms = null;
	     String[] projection = new String[] {FIELD_SMS_ID, FIELD_SMS_PHONE, FIELD_SMS_PERSON, FIELD_SMS_DATE, FIELD_SMS_BODY, FIELD_SMS_TYPE, FIELD_SMS_READ};
	     String selections = "";
	     
	     for(long id : ids)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " or ";
	    	 selections += FIELD_SMS_ID + "=" + id;
	     }	     

	     Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms"), projection, selections, null, Calls.DEFAULT_SORT_ORDER);
	     if (cursor != null && cursor.moveToFirst()) {
	    	 lstSms = new ArrayList<CallUtil.SmsData>();
	         do
	         {
	        	 SmsData data = new SmsData();
	        	 data.setmId(cursor.getLong(cursor.getColumnIndex(FIELD_SMS_ID)));
	        	 //String personId = cursor.getString(cursor.getColumnIndex(FIELD_SMS_PERSON));
	        	 //data.setmContactName(personId);
//	        	 if(personId != null)
//	        	 {
//	        		 data.setmContactName(ContactUtil.findContactNameById(context, personId));
//	        	 }
	        	 data.setmContactPhone(cursor.getString(cursor.getColumnIndex(FIELD_SMS_PHONE)));
	        	 List<String> lstNames = ContactUtil.findContactNameByPhone(context, data.getmContactPhone());
	        	 if(lstNames != null && lstNames.size() > 0)
	        		 data.setmContactName(lstNames.get(0));
	        	 data.setmTime(cursor.getLong(cursor.getColumnIndex(FIELD_SMS_DATE)));	   
	        	 data.setmContent(cursor.getString(cursor.getColumnIndex(FIELD_SMS_BODY)));
	        	 if(cursor.getInt(cursor.getColumnIndex(FIELD_SMS_TYPE)) == SMS_STATE_SENT)
	        	 {
	        		 data.setmType(PhoneData.TYPE_SENT);
	        	 }
	        	 else if(cursor.getInt(cursor.getColumnIndex(FIELD_SMS_READ)) != 0)
	        	 {
	        		 data.setmType(PhoneData.TYPE_READ);
	        	 }
	        	 else
	        	 {
	        		 data.setmType(PhoneData.TYPE_UNREAD);
	        	 }
	        	 
	        	 lstSms.add(data);
	         }while(cursor.moveToNext());
	         cursor.close();
	     }	     

	     return lstSms;
	 }
	
	public static boolean addSmsToSendBox(Context context, String contact_number, String content) {
		// boolean ret = false;

		ContentValues values = new ContentValues();
		values.put(FIELD_SMS_PHONE, contact_number);
		values.put(FIELD_SMS_BODY, content);
		Date date = new Date();
		values.put(FIELD_SMS_DATE, date.getTime());
		Uri uri = context.getContentResolver().insert(
				Uri.parse("content://sms/sent"), values);

		return (uri != null);
     }
	
	public static boolean addSmsToInBox(Context context,
			String contact_number, String content) {
		ContentValues values = new ContentValues();
		values.put(FIELD_SMS_PHONE, contact_number);
		values.put(FIELD_SMS_BODY, content);
		values.put(FIELD_SMS_READ, 0);
		Date date = new Date();
		values.put(FIELD_SMS_DATE, date.getTime());
		Uri uri = context.getContentResolver().insert(
				Uri.parse("content://sms/inbox"), values);

		return (uri != null);
	}
	
	
	public static int updateSms2Read(Context context, List<Long> lstIds) {
	     int ret = 0;
	     String selections = "";
	     
	     for(Long id : lstIds)
	     {
	    	 if(selections.length() != 0)
	    		 selections += " or ";
	    	 selections += FIELD_SMS_ID + "=" + id;
	     }	     
	     
	     ContentValues values = new ContentValues();
	     values.put(FIELD_SMS_READ, 1);
	     ret = context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, selections, null);    

	     return ret;
	 }
}
