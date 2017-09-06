package com.viash.voice_assistant.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.viash.voice_assistant.R;

public class AutoUpdateNotificationService extends Service {
	private static final boolean DEBUG = true;
	private static final String TAG = "AutoUpadteNotificationService";
	private static int NOTIFICATION_ID = 19170439;

	public static final String ACTION_ADD_DOWNLOAD = "add_download";
	public static final String ACTION_START_DOWNLOAD = "start_download";
	public static final String ACTION_UPDATE_PERCENT = "update_percent";
	public static final String ACTION_CANCEL = "cancel";

	public static final String NOTIFICATION_CANCEL_ACTION = "com.viash.voice_assistant.NOTIFICATION_CANCEL";
	public static final String NOTIFICATION_STARTDOWNLOAD_ACTION = "com.viash.voice_assistant.NOTIFICATION_STARTDOWNLOAD";

	private static NotificationManager mNotificationManager;
	//private Notification notification = null;

	@Override
	public void onCreate() {
		super.onCreate();

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			if (DEBUG) Log.i(TAG, "action: " + intent.getAction());
			if (ACTION_ADD_DOWNLOAD.equals(intent.getAction())) {
				String clickToDownloadStr = "新版求勾搭，下载有惊喜，即刻体验吧，么么哒！";//this.getResources().getString(R.string.click_to_download);
				addDownloadNotification("哦啦语音助手", clickToDownloadStr, R.drawable.notification_update_logo);
				return;
			}
			if (ACTION_START_DOWNLOAD.equals(intent.getAction())) {
				addDownloadingNotification("下载更新中...");
				return;
			}
			if (ACTION_UPDATE_PERCENT.equals(intent.getAction())) {
				int percent = intent.getIntExtra("percent", 0);
				updateNotification("下载更新中...", percent);
				return;
			}
			if (ACTION_CANCEL.equals(intent.getAction())) {
				cancel();
				return;
			}
		}
	}
 
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		mNotificationManager = null;
		super.onDestroy();
	}

	private void addDownloadNotification(String title, String description,
			int icon) {
		/*if (notification == null ) {
			notification = new Notification(R.drawable.notification_update_logo, title, System.currentTimeMillis());
		}
		else
		{
			notification.icon = R.drawable.notification_update_logo;
			notification.tickerText = title;
			notification.when = System.currentTimeMillis();
		}*/
		
		Notification notification = new Notification(
				R.drawable.notification_update_logo, title, System.currentTimeMillis());		
		notification.contentView = new RemoteViews("com.viash.voice_assistant",
				R.layout.update_notification);
		notification.contentView.setImageViewResource(R.id.autoupdate_logo,
				icon);
		notification.contentView.setTextViewText(R.id.autoupdate_title, title);
		notification.contentView.setTextViewText(R.id.autoupdate_information,
				description);

		Intent intent = new Intent()
				.setAction(NOTIFICATION_STARTDOWNLOAD_ACTION);
		notification.contentIntent = PendingIntent.getBroadcast(this, 0,
				intent, 0);

		notification.flags = Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void addDownloadingNotification(String title) {
		/*if (notification == null) 
		{
			notification = new Notification(
				R.drawable.statusbar_logo, title, System.currentTimeMillis());
		}
		else
		{
			notification.icon = R.drawable.statusbar_logo;
			notification.tickerText = title;
			notification.when = System.currentTimeMillis();
		}*/
		Notification notification = new Notification(
				R.drawable.statusbar_logo, title, System.currentTimeMillis());
		
		notification.contentView = new RemoteViews("com.viash.voice_assistant",
				R.layout.download_notification);
		notification.contentView.setProgressBar(R.id.autoupdate_progress, 100,
				0, false);
		String downloadingStr = this.getResources().getString(R.string.downloading2);
		notification.contentView.setTextViewText(R.id.autoupdate_information, downloadingStr);

		Intent cancelIntent = new Intent();
		cancelIntent.setAction(NOTIFICATION_CANCEL_ACTION);
		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,
				NOTIFICATION_ID, cancelIntent, 0);
		notification.contentView.setOnClickPendingIntent(
				R.id.autoupdate_cancel, cancelPendingIntent);

		Intent intent = new Intent();
		notification.contentIntent = PendingIntent.getActivity(this, 0, intent,
				0);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void updateNotification(String title, int percent) {
		/*if (notification == null) {	
			notification = new Notification(R.drawable.notification_update_logo, title, System.currentTimeMillis());
		}
		else
		{
			notification.icon = R.drawable.notification_update_logo;
			notification.tickerText = title;
			//notification.when = System.currentTimeMillis();//cause flash in huawei phone
		}*/
		Notification notification = new Notification(
				R.drawable.notification_update_logo, title, System.currentTimeMillis());
				
		notification.contentView = new RemoteViews("com.viash.voice_assistant",
				R.layout.download_notification);
		notification.contentView.setProgressBar(R.id.autoupdate_progress, 100,
				percent, false);
		String downloadingStr = this.getResources().getString(R.string.downloading2);
		notification.contentView.setTextViewText(R.id.autoupdate_information, downloadingStr);

		Intent cancelIntent = new Intent();
		cancelIntent.setAction(NOTIFICATION_CANCEL_ACTION);
		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,
				NOTIFICATION_ID, cancelIntent, 0);
		notification.contentView.setOnClickPendingIntent(
				R.id.autoupdate_cancel, cancelPendingIntent);

		Intent intent = new Intent();
		notification.contentIntent = PendingIntent.getActivity(this, 0, intent,
				0);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void cancel() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
		
		this.stopSelf();
		this.onDestroy();
	}
}
