package com.via.android.voice.floatview;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FloatViewService extends Service{
	private static final String TAG = "FloatViewService";
	public static final String START_LOGO_VIEW = "create_logo_view";
	public static final String STOP_LOGO_VIEW = "release_logo_view";
	
	public static boolean serverIsStart = false;
	
	private VIAApplication application = null;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		Notification notification = new Notification(R.drawable.logo, "Foreground Service Started.", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NewAssistActivity.class), 0);
		notification.setLatestEventInfo(this, "Foreground Service", "Foreground Service Started.", contentIntent);
		this.startForeground(1, notification);
		serverIsStart = true;
		application = (VIAApplication) getApplicationContext();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(application == null) application = (VIAApplication) getApplicationContext();
		if(intent != null){
			if (START_LOGO_VIEW.equalsIgnoreCase(intent.getAction())) {
				application.createView(VIAApplication.LOGO_FLOAT_VIEW);
			} else if (STOP_LOGO_VIEW.equalsIgnoreCase(intent.getAction())) {
				application.releaseView(VIAApplication.LOGO_FLOAT_VIEW);
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		this.stopForeground(false);
		application.releaseView(VIAApplication.LOGO_FLOAT_VIEW);
		serverIsStart = false;
	}
}
