package com.viash.voice_assistant.observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;

import com.viash.voice_assistant.service.LockScreenService;

public class SmsObserver extends ContentObserver {

	private Handler mHandler = null;
	private ContentResolver mResolver = null;
	private static long mLastDate = 0;
	
	private static final String[] sItems={
		SMS.ID,
		//SMS.TYPE,
		SMS.ADDRESS,
		SMS.BODY,
		SMS.DATE,
		SMS.PROTOCOL,
		SMS.READ
	};
	
	private static final String[] mMaxDate = { "max(" + SMS.DATE + ") as " + SMS.DATE };

	private static final String SELECTION = SMS.DATE + " > %s"+
			" and (" + SMS.TYPE + "=" + SMS.MESSAGE_TYPE_INBOX + ")";
	private static final String FINDMAXDATE = SMS.TYPE + "=" + SMS.MESSAGE_TYPE_INBOX;
	
	public SmsObserver(Handler handler) {
		super(handler);
	}

	public SmsObserver(ContentResolver resolver, Handler handler) {
		super(handler);
		mResolver = resolver;
		Cursor c = mResolver.query(SMS.CONTENT_URI, mMaxDate, FINDMAXDATE, null, null);
		if (c.moveToFirst()) {
			mLastDate = c.getLong(c.getColumnIndex(SMS.DATE));
		}
		c.close();
		mHandler = handler;
	}


	

	public void onChange(boolean bSelfChange) {
		super.onChange(bSelfChange);
		Cursor c = mResolver.query(SMS.CONTENT_URI, sItems, String.format(SELECTION, mLastDate),
				null, null);
		if(c == null)
		{
			return;
		}
		
		if (c.moveToFirst()) {
			// Read the contents of the SMS;
			//long id=c.getLong(c.getColumnIndex(SMS.ID));
			//int type=c.getInt(c.getColumnIndex(SMS.TYPE));
			int protocol=c.getInt(c.getColumnIndex(SMS.PROTOCOL));
			long date=c.getLong(c.getColumnIndex(SMS.DATE));
			String phone=c.getString(c.getColumnIndex(SMS.ADDRESS));
			String body=c.getString(c.getColumnIndex(SMS.BODY));
			//int read = c.getInt(c.getColumnIndex(SMS.READ));

			if(protocol == SMS.PROTOCOL_SMS && body != null && mLastDate != date)
			{
				Message msg = new Message();
				msg.what = LockScreenService.SMS_OBSERVER_MSG;
				String[] item = new String[2];
				item[0] = phone;
				item[1] = body;
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
			mLastDate = date;
		}
		c.close();
	}
	
	public interface SMS extends BaseColumns {
	    public static final Uri CONTENT_URI = Uri.parse("content://sms");
	    public static final String ID  = "_id";
	    public static final String TYPE = "type";
	    public static final String ADDRESS = "address";
	    public static final String DATE = "date";
	    public static final String BODY = "body";
	    public static final String PROTOCOL = "protocol";
	    public static final String READ="read";
	    
	    public static final int MESSAGE_TYPE_ALL    = 0;
	    public static final int MESSAGE_TYPE_INBOX  = 1;
	    public static final int MESSAGE_TYPE_SENT   = 2;
	    public static final int MESSAGE_TYPE_DRAFT  = 3;
	    public static final int MESSAGE_TYPE_OUTBOX = 4;
	    public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
	    public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later

	    public static final int MESSAGE_READ_UNREAD = 0;
	    
	    public static final int PROTOCOL_SMS = 0;//SMS_PROTO
	    public static final int PROTOCOL_MMS = 1;//MMS_PROTO
	}
}
