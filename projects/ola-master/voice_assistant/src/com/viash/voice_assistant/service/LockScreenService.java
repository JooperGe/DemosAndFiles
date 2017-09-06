package com.viash.voice_assistant.service;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.viash.voice_assistant.activity.LockActivity;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.observer.SmsObserver;
import com.viash.voicelib.data.AppData;

public class LockScreenService extends Service {
	private final static String TAG = "LockScreenService";
	private static Intent lockIntent;
	private KeyguardManager keyguardManager = null;
	private KeyguardManager.KeyguardLock keyguardLock = null;
	
	private SmsObserver mObserver = null;
	
	public static int presshome = 0;
	public static String packagenameString ;
	public static String ClassnameString ;
	
	public static int SMS_OBSERVER_MSG = 5050;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@SuppressLint("Recycle")
	@Override
	public void onCreate() {
		super.onCreate();
		lockIntent = new Intent(LockScreenService.this,LockActivity.class);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		LockScreenService.this.registerReceiver(mScreenOffReceiver,mScreenOffFilter);
		IntentFilter mScreenPhoneFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		LockScreenService.this.registerReceiver(mScreenOffReceiver,mScreenPhoneFilter);
		LockScreenService.this.registerReceiver(mScreenOffReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		
		PackageManager manager = getPackageManager();
	
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        addSMSObserver();
        
        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));        
        if (apps != null) {
        	
            final int count = apps.size();

            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                
                try {
					Field system = ResolveInfo.class.getDeclaredField("system");
					system.setAccessible(true);
					Object value = system.get(info);
					Log.i("HOME","LockScreenService value.toString() = "+value.toString());
					if("true".equals(value.toString()))
					{
						packagenameString = info.activityInfo.packageName;
	                	ClassnameString = info.activityInfo.name;
					}
					Log.i("HOME","LockScreenService packagenameString = "+packagenameString);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                /*Parcel  dest =  Parcel.obtain();
                
                info.writeToParcel(dest, 0);
                Log.i("HOME", info.toString());
               
                dest.setDataPosition( dest.dataSize()- 4);
                int first = dest.readInt();
                Log.i("HOME", "" + dest.dataSize() + "      "+ first);
               
                if (first == 1) {
                   	packagenameString = info.activityInfo.packageName;
                	ClassnameString = info.activityInfo.name;
                }*/
                continue;
            }
        }    
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "OnDestroy");
		this.getContentResolver().unregisterContentObserver(mObserver);
		
		LockScreenService.this.unregisterReceiver(mScreenOffReceiver);
		startService(new Intent(LockScreenService.this, LockScreenService.class));	
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}
	boolean   flag=true;
	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
		static final String SYSTEM_REASON = "reason";  
		static final String SYSTEM_HOME_KEY = "homekey";//home key  
		static final String SYSTEM_RECENT_APPS = "recentapps";//long home key  

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, intent.getAction());
			
			 if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  
		            String reason = intent.getStringExtra(SYSTEM_REASON);  
		            if (reason != null) {  
		                if (reason.equals(SYSTEM_HOME_KEY)) {  
		                    Log.i(TAG, SYSTEM_HOME_KEY);
		                    
		                    presshome = 1;
		                      
		                } else if (reason.equals(SYSTEM_RECENT_APPS)) {  
		                    Log.i(TAG, SYSTEM_RECENT_APPS);
		                    presshome = 2;		                    
		                }  
		            }  
		        }  

			
			if (((intent.getAction().equals(Intent.ACTION_SCREEN_OFF)|| intent.getAction().equals(Intent.ACTION_SCREEN_ON)))&&flag) {
				if (SavedData.isAllowLock()){
					keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
					keyguardLock = keyguardManager.newKeyguardLock("");
					keyguardLock.disableKeyguard(); 
					startActivity(lockIntent);
				}
			}
			if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
				flag=false;
			}else{
				TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);   
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
				flag=true;
			}
		}
		
		PhoneStateListener listener=new PhoneStateListener(){
			 
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				// TODO Auto-generated method stub
				//state µ±Ç°×´Ì¬ incomingNumber,Ã²ËÆÃ»ÓÐÈ¥µçµÄAPI
				super.onCallStateChanged(state, incomingNumber);
				switch(state){
				case TelephonyManager.CALL_STATE_IDLE:
					flag=true;
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					flag=false;
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					flag=false;
					//Êä³öÀ´µçºÅÂë
					break;
				}
			}
	 
		};
	};
	   
	private void addSMSObserver() {
		Log.i(TAG, "add a SMS observer. ");
		ContentResolver resolver = getContentResolver();
		SMSHandler handler = new SMSHandler();
		mObserver = new SmsObserver(resolver, handler);
		resolver.registerContentObserver(SmsObserver.SMS.CONTENT_URI, true, mObserver);
	}
	
	
	@SuppressLint("HandlerLeak")
	public class SMSHandler extends Handler{
		private static final String TAG="SMSHandler";
		
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == SMS_OBSERVER_MSG) {
				String[] items = (String[])msg.obj;
				
				Intent intent = new Intent();
				intent.setAction(AppData.RECEIVE_SMS_ACTION);
				intent.putExtra(AppData.RECEIVE_SMS_ACTION,items);
				sendBroadcast(intent);
				Log.i(TAG, items[0] + "   " + items[1]);
			}
		}
	}

}
