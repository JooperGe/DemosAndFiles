package com.viash.voicelib.utils;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class ClientPropertyUtil {
	private static final String TAG = "ClientPropertyUtil";
	
	public static int getVersionCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.getStackTrace();
		}
		return verCode;
	}

	public static String getVersionName(Context context) {
		String verName = "";
		try {
			int verCode = context.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionCode;
			verName = context.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionName;
			verName += " build" + verCode;
		} catch (NameNotFoundException e) {
			e.getStackTrace();
		}
		return verName;
	}
	
	public static JSONObject getJsonObject(Context context)
	{
		JSONObject obj = new JSONObject();
		String result = Build.BRAND;
		if(result != null)
		{
			try {
				obj.put("brand", result);
			} catch (Exception e) {
			}
		}
		
		result = Build.MANUFACTURER;
		if(result != null)
		{
			try {
				obj.put("manufacturer", result);
			} catch (Exception e) {
			}
		}
		
		result = Build.PRODUCT;
		if(result != null)
		{
			try {
				obj.put("product", result);
			} catch (Exception e) {
			}
		}
		
		result = Build.MODEL;
		if(result != null)
		{
			try {
				obj.put("model", result);
			} catch (Exception e) {
			}
		}
		
		result = Build.VERSION.RELEASE;
		if(result != null)
		{
			try {
				obj.put("sdk_ver", result);
			} catch (Exception e) {
			}
		}
		
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
		if(telephonyManager != null)
		{
			int providerId = 0;
			String IMSI = telephonyManager.getSubscriberId();  
			if(IMSI != null)
			{
		        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {  
		            providerId = 1;	//"中国移动"
		        } else if (IMSI.startsWith("46001")) {  
		        	providerId = 2;	 //"中国联通"
		        } else if (IMSI.startsWith("46003")) { 
		        	providerId = 3;	//"中国电信"
		        } 
		        try {
					obj.put("carrier_operator", providerId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
	        String imei = telephonyManager.getDeviceId();
	        if(imei != null)
				try {
					obj.put("imei", imei);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		
		try
		{
			obj.put("client_os", 0);
			obj.put("cpu_platform", 0);
			//obj.put("sdk_ver", 8);
			obj.put("package_name", context.getPackageName());
			obj.put("locale", Locale.getDefault().toString());
			obj.put("client_version", getVersionName(context));
			if (getPhoneNumber(context) != null && !getPhoneNumber(context).equals("") ) {
				obj.put("phone_number", getPhoneNumber(context));
			}
			if(HelpStatisticsUtil.isNeedToSendHelpFirstStatistics())
			{
				JSONObject jobj = new JSONObject();
				jobj = HelpStatisticsUtil.jsonObj.getJSONObject(HelpStatisticsUtil.HELP_FIRST);
				obj.put(HelpStatisticsUtil.HELP_FIRST, jobj);
			}
			if(HelpStatisticsUtil.isNeedToSendHelpAllStatistics())
			{
				JSONArray jArray = new JSONArray();
				jArray = HelpStatisticsUtil.jsonObj.getJSONArray(HelpStatisticsUtil.HELP_ALL);
				obj.put(HelpStatisticsUtil.HELP_ALL, jArray);
			}
			if(HelpStatisticsUtil.isNeedToSendHtmlPushStatistics())
			{
				obj.put(HelpStatisticsUtil.HTML_PUSH, HelpStatisticsUtil.jsonObj.getJSONArray(HelpStatisticsUtil.HTML_PUSH));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		if(dm != null)
		{
			try
			{
				obj.put("screen_width", dm.widthPixels);
				obj.put("screen_height", dm.heightPixels);
				obj.put("density", dm.densityDpi);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		Log.i(TAG, obj.toString());
		return obj;
	}
	
	private static String getPhoneNumber(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
		
	}
}
