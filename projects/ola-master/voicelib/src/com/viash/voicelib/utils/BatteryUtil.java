package com.viash.voicelib.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryUtil {
	public static boolean isCharging(Context context)
	{
		boolean ret = false;
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); 
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1); 
		if(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
		{
			ret = true;
		}
		
		return ret;
	}
}
