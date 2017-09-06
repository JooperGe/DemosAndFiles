package com.viash.voice_assistant.component;

import java.util.HashMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import com.viash.voice_assistant.R;

public class DownloadNotification {
	private static int NOTIFICATION_ID = 19170440;
	private static NotificationManager mNotificationManager;
	private static HashMap<String, Notification> mNotification = new HashMap<String, Notification>();
	private static HashMap<String, Integer> mNotificationId = new HashMap<String, Integer>();
	
	private static Activity mActivity;
	private static Context mContext;
	private static int mId = 0;
	
	public static final String NOTIFICATION_CANCEL_ACTION = "com.viash.voice_assistant.NOTIFICATION_CANCEL";
	public static final String NOTIFICATION_STARTDOWNLOAD_ACTION = "com.viash.voice_assistant.NOTIFICATION_STARTDOWNLOAD";
	
	public static void init(Activity activity, Context context) {
		mActivity = activity;
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mId = 0;
	}

	public static void addDownloadOver(String id, String title, String description, int icon){
		addDownloadOver(id, title, description, icon, new Intent());
	}
	public static void addDownloadOver(String id, String title, String description, String downloadAppIcon){
		addDownloadOver(id, title, description, downloadAppIcon, new Intent());
	}
	public static void addDownloadOver(final String id, final String title, final String description, final int icon, final Intent intent){
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Notification notification = addNotification(id, title);
				notification.contentView = new RemoteViews("com.viash.voice_assistant",R.layout.update_notification);
				notification.contentView.setImageViewResource(R.id.autoupdate_logo, icon);
				notification.contentView.setTextViewText(R.id.autoupdate_title, title);
				notification.contentView.setTextViewText(R.id.autoupdate_information, description);
				
				if(intent == null){
					notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
				}else{
					notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
				}
				
		    	notification.flags = Notification.FLAG_AUTO_CANCEL;
		    	
		    	mNotification.put(id, notification);
		    	
		    	mNotificationManager.notify(mNotificationId.get(id), mNotification.get(id));
			}
		});
	}
	public static void addDownloadOver(final String id, final String title, final String description, final String downloadAppIcon, final Intent intent) {
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Notification notification = addNotification(id, title);
				notification.contentView = new RemoteViews("com.viash.voice_assistant",R.layout.update_notification);
				//notification.contentView.setImageViewResource(R.id.autoupdate_logo, icon);
				Bitmap bitmap = BitmapFactory.decodeFile(downloadAppIcon);
				notification.contentView.setImageViewBitmap(R.id.autoupdate_logo, bitmap);
				notification.contentView.setTextViewText(R.id.autoupdate_title, title);
				notification.contentView.setTextViewText(R.id.autoupdate_information, description);
				
				if(intent == null){
					notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
				}else{
					notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
				}
		
		    	notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		    	mNotification.put(id, notification);
		    	
		    	mNotificationManager.notify(mNotificationId.get(id), mNotification.get(id));
			}
		});
	}

	public static void addClickDownloadNotification(final String id, final String title, final String description, final int icon){
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Notification notification = new Notification(R.drawable.notification_update_logo, title, System.currentTimeMillis());
				notification.contentView = new RemoteViews("com.viash.voice_assistant",R.layout.update_notification);
				notification.contentView.setImageViewResource(R.id.autoupdate_logo, icon);
				notification.contentView.setTextViewText(R.id.autoupdate_title, title);
				notification.contentView.setTextViewText(R.id.autoupdate_information, description);
		
		    	Intent intent = new Intent().setAction(NOTIFICATION_STARTDOWNLOAD_ACTION);
		    	intent.putExtra("id", id);
		    	notification.contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
		    	
		    	notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		    	mId++;
		    	if(mId > 1000) mId = 0;
		    	
		    	mNotification.put(id, notification);
		    	mNotificationId.put(id, mId+NOTIFICATION_ID);
		    	
		    	mNotificationManager.notify(mNotificationId.get(id), mNotification.get(id));
			}
		});
	}
	
	public static void addDownloadingNotification(final String id, final String title) {
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Notification notification = addNotification(id, title);
				notification.contentView = new RemoteViews("com.viash.voice_assistant",R.layout.download_notification);
				notification.contentView.setProgressBar(R.id.autoupdate_progress, 100, 0, false);
				notification.contentView.setTextViewText(R.id.autoupdate_information, title);
		
				Intent cancelIntent = new Intent();
		    	cancelIntent.setAction(NOTIFICATION_CANCEL_ACTION);
		    	cancelIntent.putExtra("id", id);
		    	PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, mNotificationId.get(id), cancelIntent, 0);
		    	notification.contentView.setOnClickPendingIntent(R.id.autoupdate_cancel, cancelPendingIntent);
		    	
		    	Intent intent = new Intent();
		    	notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
		    	
		    	mNotification.put(id, notification);
		
		    	mNotificationManager.notify(mNotificationId.get(id), mNotification.get(id));
			}
		});
	}
	
	private static Notification addNotification(String id, String title){
		if(!mNotificationId.containsKey(id)){
			mId++;
	    	if(mId > 1000) mId = 0;
	    	mNotificationId.put(id, (mId+NOTIFICATION_ID));
		}
		Notification notification = new Notification(R.drawable.statusbar_logo, title, System.currentTimeMillis());
		
		return notification;
	}
	
	public static void updateNotification(final String id, final int percent){
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(mNotification.get(id) == null) return;
				mNotification.get(id).contentView.setProgressBar(R.id.autoupdate_progress, 100, percent , false);
				mNotificationManager.notify(mNotificationId.get(id), mNotification.get(id));
			}
		});
	}
	
	public static void cancel(String id) {
		if(mNotificationManager != null && mNotificationId.containsKey(id)){
			mNotificationManager.cancel(mNotificationId.get(id));
			mNotificationId.remove(id);
			mNotification.remove(id);
		}
	}
	
	public static void cancelAll() {
		if(mNotificationManager != null){
			for(Integer id : mNotificationId.values()){
				mNotificationManager.cancel(id);
			}
		}
		mNotificationId.clear();
		mNotification.clear();
	}
}
