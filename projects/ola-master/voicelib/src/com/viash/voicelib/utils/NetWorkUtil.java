package com.viash.voicelib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtil {
	public static boolean isWIFIConnected(Context context){ 
		boolean ret = false;
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);////获取系统的连接服务   
		NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
		if(activeNetworkInfo != null)
		{
			if(activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI){  
				ret = activeNetworkInfo.isConnected();
			}
		}
		
		return ret;
	}
	
	public static boolean isNetConnected(Context context){ 
		boolean ret = false;
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);////获取系统的连接服务   
		NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
		if(activeNetworkInfo != null)
		{
			ret = activeNetworkInfo.isConnected();
		}
		
		return ret;
	}
}
