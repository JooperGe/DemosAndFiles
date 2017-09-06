package com.viash.voicelib.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class MachineUtil {
	public static String getMachineId(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tm.getDeviceId();
		if (deviceId == null || deviceId.trim().length() == 0) {
			deviceId = getUniqueId(context);
		}
		
		if (deviceId == null || deviceId.trim().length() == 0) {
			deviceId = getLocalMacAddress(context);
			if(deviceId != null)
				deviceId = deviceId.replace(":", "");
		}
		
		if(deviceId == null)
			deviceId = "unknown";
		else if(deviceId.length() > 15)
			deviceId = deviceId.substring(0, 16);
		return deviceId;
	}

	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}
	
	public static String getUniqueId(Context context)
	{
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

}
