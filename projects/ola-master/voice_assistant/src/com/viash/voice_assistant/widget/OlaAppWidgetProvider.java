package com.viash.voice_assistant.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.service.VoiceAssistantService;

public class OlaAppWidgetProvider extends AppWidgetProvider {
	private static final boolean DEBUG = true;
	private static final String TAG = "OlaWidget";
	
	public static final String START_CAPTURE_ACTION = "com.viash.voice_assistant.widget.STARTCAPTURE";
	public static int[] appWidgetId;
	public static AppWidgetManager mAppWidgetManager;
	public static RemoteViews mRemoteViews;
	public static Context mContext;

	@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(START_CAPTURE_ACTION)) {
            if (DEBUG)
				Log.i(TAG, "start capture");
			
            mContext = context;
            startCapture();
        }else{
        	super.onReceive(context, intent);
        }
    }
	
	private void startCapture(){
		Intent intent = new Intent(mContext, VoiceAssistantService.class);
		intent.setAction(START_CAPTURE_ACTION);
		mContext.startService(intent);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		appWidgetId = appWidgetIds;
		mContext = context;
		mAppWidgetManager = appWidgetManager;
		mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_ola);
		
		// Create an Intent to launch ExampleActivity
		Intent openIntent = new Intent(context, GuideActivity.class);
		openIntent.setAction(START_CAPTURE_ACTION);
		PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, 0);
		
		// Start capture intent
		Intent startCaptureIntent = new Intent();
		startCaptureIntent.setAction(START_CAPTURE_ACTION);
		PendingIntent startCaptureIPendingIntent = PendingIntent.getBroadcast(context, 0, startCaptureIntent, 0);

		mRemoteViews.setOnClickPendingIntent(R.id.image_ola, openPendingIntent);
		mRemoteViews.setOnClickPendingIntent(R.id.image_speak, startCaptureIPendingIntent);

		// Tell the AppWidgetManager to perform an update on the current app widget
		appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
	}
	
	public static void setUpdateImage(int viewId, int resId){
		if(mRemoteViews == null) return;
		mRemoteViews.setImageViewResource(viewId, resId);
	}
	public static void updateWidget(){
		if(mAppWidgetManager == null) return;
		mAppWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
	}
}
