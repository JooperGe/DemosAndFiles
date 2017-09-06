package com.viash.voice_assistant.component;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.SavedData;

public class VoiceWakeUpIndicationDialog extends Dialog{

	public static final int NOTIFICATION_ID = 20141010;
	public static final String NOTIFICATION_CLOSE_VOICE_WAKE_UP = "com.viash.voice_assistant.NOTIFICATION_CLOSE_VOICE_WAKE_UP";
	public static final String NOTIFICATION_START_CAPTURE_OFFLINE = "com.viash.voice_assistant.NOTIFICATION_START_CAPTURE_OFFLINE";
	private static Context mContext;
	private String  mContent;
	private Handler mHandler;
	private ImageView imagv_close;
	private static NotificationManager mNotificationManager;
	
	public VoiceWakeUpIndicationDialog(Context context,String content,Handler handler) {
		super(context);
		mContext = context;
		mContent = content;
		mHandler = handler;
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		this.setTitle(null);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);	     				
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.layout_voice_wakeup_indication);
		imagv_close = (ImageView) findViewById(R.id.imgv_close);
		imagv_close.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				VoiceWakeUpIndicationDialog.this.cancel();
			}
			
		});
		addNotificaticon();
	}
		
	public void addNotificaticon()
	{			
		VoiceWakeUpIndicationDialog.this.cancel();
		Notification notification = new Notification(R.drawable.wake_voice_64, "哦啦语音唤醒功能已开启", System.currentTimeMillis());
		notification.contentView = new RemoteViews("com.viash.voice_assistant",R.layout.voice_wakeup_notification);
		Intent closeIntent = new Intent();
		closeIntent.setAction(NOTIFICATION_CLOSE_VOICE_WAKE_UP);
		closeIntent.putExtra("id", NOTIFICATION_ID);
    	PendingIntent closePendingIntent = PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, closeIntent, 0);
		notification.contentView.setOnClickPendingIntent(R.id.imgv_close, closePendingIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT |Notification.FLAG_NO_CLEAR;
		Intent intent = new Intent();
    	notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
}
