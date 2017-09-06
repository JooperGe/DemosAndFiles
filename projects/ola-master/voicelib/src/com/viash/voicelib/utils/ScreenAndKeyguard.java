package com.viash.voicelib.utils;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class ScreenAndKeyguard {
	public static boolean isDisableCalled = false;
	private static KeyguardManager myKM = null;
	private static KeyguardLock myKL = null;
	
	public static synchronized void initialize(Context context) {
		if (myKM == null) {
			myKM = (KeyguardManager) context
					.getSystemService(Context.KEYGUARD_SERVICE);			
		}
	}
	
	public static boolean isScreenLock(Context context)
	{
		boolean ret = false;
		KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);   
	      
	    if (mKeyguardManager.inKeyguardRestrictedInputMode()) {  
             ret = true;
	    } 
	    return ret;
	}
	
	public static boolean isScreenON(Context context)
	{
		boolean ret = false;
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if(pm.isScreenOn())
			ret = true;
		return ret;	
	}

	public static synchronized void unlockScreen(Context context)
	{
		initialize(context);
  
        if(myKM.inKeyguardRestrictedInputMode())
        {
        	lockScreen(context);
        	myKL = myKM.newKeyguardLock("unlock");
        	if(isDisableCalled == false)
        	{
        		myKL.disableKeyguard();
        		//Log.e("ppp", "unlockScreen");
        	}
        	isDisableCalled = true;
        }
        else{
        	myKL = null;
        }
        
	}
	
	public static synchronized void lockScreen(Context context)
	{
		if(myKM != null)
		{
			if (myKL != null)
			{
		        if(isDisableCalled){
		    		myKL.reenableKeyguard();
		    		//Log.e("ppp", "lockScreen");
		        }
		        isDisableCalled = false;
		        myKL = null;
			}
		}
	}
	public static void turnOnScreen(Context context)
	{
		//获取电源管理器对象  
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);  
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag  
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK,"bright");  
        //点亮屏幕  
        wl.acquire();  
        //释放  
        //wl.release();
	}
	
}
