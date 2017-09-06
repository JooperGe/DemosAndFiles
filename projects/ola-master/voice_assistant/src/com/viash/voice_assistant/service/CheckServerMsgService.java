package com.viash.voice_assistant.service;

import java.util.Date;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CommunicationGetPageUtil;
import com.viash.voicelib.utils.HttpUtil;
import com.viash.voicelib.utils.JsonUtil;
import com.viash.voicelib.utils.ThreadUtil;

public class CheckServerMsgService extends Service{
	private Thread mThread = null;
	private static final String MSG_ID_KEY = "ola_server_last_message_id";
	private static final String MSG_URL = "http://api.olavoice.com/OlaPushHtml/publish/pushhtml";
	private long mMsgId = 0;
	private String mMsgTitle = null;
	private String mMsgUrl = null;	
	private boolean mExit = false;

	@Override
	public void onCreate() {
		if(mThread == null)
			startCheckThread();
		super.onCreate();
	}
	
	@Override
	public void onDestroy()
	{
		mExit = true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {			
		return null;
	}
	
	private void startCheckThread()
	{		
		mThread = new Thread()
		{
			@Override
			public void run() {
				mExit = false;
				ThreadUtil.sleep(10000);
				do
				{
					Date date = new Date();
					if(date.getHours() >= 8 && date.getHours() < 20)
					{
						checkServerMsg();
						//ThreadUtil.sleep(20* 1000);
						checkWelcomePage();
					}					
					ThreadUtil.sleep(3600 * 1000*2);
					//ThreadUtil.sleep(20 * 1000);
				}while(!mExit);
			}			
		};
		mThread.start();
	}
	
	private void checkServerMsg()
	{
		mMsgId = 0;
		String result = HttpUtil.sendGetCommand(this, MSG_URL, "utf8");
		if(result != null)
		{
			try
			{
				int indexStart = result.indexOf('{');
				int indexEnd = result.lastIndexOf('}');
				if(indexStart >= 0 && indexEnd >= 0 && indexEnd > indexStart)
				{				
					result = result.substring(indexStart, indexEnd + 1);
					JSONObject obj = new JSONObject(result);
					mMsgId = JsonUtil.optLong(obj, MsgConst.SERVER_MSG_ID, 0);
					mMsgTitle = JsonUtil.optString(obj, MsgConst.SERVER_MSG_TITLE, null);
					mMsgUrl = JsonUtil.optString(obj, MsgConst.SERVER_MSG_URL, null);	
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if(mMsgId > 0)
		{
			long lastId = 0;
			
			SharedPreferences preferences = getSharedPreferences("user", 0);
			lastId= preferences.getLong(MSG_ID_KEY, lastId);

			if(mMsgId!=lastId)
			{
				if(showNotification())
				{
					Editor editor = preferences.edit();
					editor.putLong(MSG_ID_KEY, mMsgId);
					editor.commit();
				}
			}
		}
	}
	
	private void checkWelcomePage()
	{
		CommunicationGetPageUtil communicationGetPageUtil = new CommunicationGetPageUtil(CheckServerMsgService.this,null);
		communicationGetPageUtil.getDataFromServer();
	}
	private boolean showNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, GuideActivity.class);
		intent.putExtra(MsgConst.SERVER_MSG_ID, mMsgId);
		intent.putExtra(MsgConst.SERVER_MSG_TITLE, mMsgTitle);
		intent.putExtra(MsgConst.SERVER_MSG_URL, mMsgUrl);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);           
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); 
    	
		String title = this.getResources().getString(R.string.ola_msg_notification_title);		
		Notification notification = new Notification(R.drawable.statusbar_logo, title, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;     	
    	notification.setLatestEventInfo(this, title, mMsgTitle, contentIntent);
        int notification_id = (int) (mMsgId % 1000000);
    	notificationManager.notify(notification_id, notification);    	
    	return true;
	}
}
