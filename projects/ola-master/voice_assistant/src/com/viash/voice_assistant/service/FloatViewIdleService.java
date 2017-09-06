package com.viash.voice_assistant.service;

import java.util.ArrayList;
import java.util.List;

import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.data.SavedData;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;



public class FloatViewIdleService extends Service {

	private static Handler mHandler;  
	private FloatViewIdle floatViewIdle;
	private final static int REFRESH_FLOAT_VIEW = 1;
	private boolean is_vertical = true;
	@Override
	public void onCreate() {
		super.onCreate();
		initHandler();		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(REFRESH_FLOAT_VIEW), 500);
		FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE = false;
		is_vertical = true;
		return START_STICKY;
	}
	protected void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH_FLOAT_VIEW:
					updateFloatView();
					mHandler.sendMessageDelayed(mHandler.obtainMessage(REFRESH_FLOAT_VIEW), 1000);
					break;
				}
			}
		};
	}
		
	 private void updateFloatView()
	 {
		boolean isOnIdle = isHome();
		floatViewIdle = FloatViewIdle.getInstance(FloatViewIdleService.this);		
     	if(isOnIdle && SavedData.isFloatViewOnDesk())
     	{       		
     		if(floatViewIdle.getFloatViewType() == 0)
     		{	 			
     		    floatViewIdle.show();
     		}
     		else if(floatViewIdle.getFloatViewType() == floatViewIdle.FLOAT_ICON_VIEW_TYPE||
     				floatViewIdle.getFloatViewType() == floatViewIdle.FLOAT_RECORD_VIEW_TYPE)
     		{
     			if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
	    		{
	    			if(is_vertical == true)
	    			{
	    			   floatViewIdle.swapWidthAndHeight();	
	    			   is_vertical = false;
	    			}
	    		}
	    		else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
	    		{
	    			if(is_vertical == false)
	    			{
	    			   floatViewIdle.swapWidthAndHeight();
	    			   is_vertical = true;
	    			}
	    		}	
     		}
     	}
     	else
     	{
     		floatViewIdle.hide();      			
     	}
	 }
	 
	 private boolean isHome() 
	 {  
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
	    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1); 
		try{ 
        if(rti.size() == 0)
        {
        	return true;
        }else
        {
	        if(rti.get(0).topActivity.getPackageName().equals("com.viash.voice_assistant"))
	        	return false;
	        else
	        	return getHomes().contains(rti.get(0).topActivity.getPackageName()); 
			}
		}
		catch(Exception e)
		{		
		   return true;
		}
	 }  
	 
	 private List<String> getHomes() 
	 {  
        List<String> names = new ArrayList<String>();  
        PackageManager packageManager = this.getPackageManager();  
        Intent intent = new Intent(Intent.ACTION_MAIN);  
        intent.addCategory(Intent.CATEGORY_HOME);  
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,  
                PackageManager.MATCH_DEFAULT_ONLY);  
        for (ResolveInfo ri : resolveInfo) {  
            names.add(ri.activityInfo.packageName);  
        }  
        return names;  
	 }  
	 
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(floatViewIdle != null)
		   floatViewIdle.setFloatViewType(0);
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}