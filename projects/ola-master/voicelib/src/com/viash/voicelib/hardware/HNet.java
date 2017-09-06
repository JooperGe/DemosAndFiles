package com.viash.voicelib.hardware;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 网络相关操作
 * 
 * @author Harlan
 * @createDate 2012-12-5
 */
public class HNet {
	private WifiManager wifiManager;
	private ConnectivityManager connectivityManager;
	Class connectivityManagerClass;
	private Field iConnectivityManagerField;
	private Object iConnectivityManager;
	Class iConnectivityManagerClass;
	private Method mobileNet;
	private Context context;
	private Method mSetMethod;
	private Method mGetMethod;
	private TelephonyManager telephonyManager;

	public HNet(Context context) {
		this.context = context;
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		// initMobileNetwork();
		initNetWork();
	}

	/**
	 * 开启WIFI
	 */
	public void openWifi() {
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * 关闭wifi
	 */
	public void closeWifi() {
		if (wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}

	}

	/*
	 * 开启网络
	 */
	public void openNetWork() {
		openWifi();
	}

	/*
	 * 关闭网络
	 */
	public void closeNetwork() {
		closeWifi();
//		closeMobileDataNetwork();
	}

	/*
	 * 开启移动数据网络
	 */

	public void openMobileDataNetwork() {
		try {
			// mobileNet.invoke(iConnectivityManager, true);
			// mSetMethod.invoke(, args)
			if (Build.VERSION.SDK_INT > 19) {
				mSetMethod.invoke(telephonyManager, true);
			} else {
				mSetMethod.invoke(connectivityManager, true);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 关闭移动数据网络
	 */

//	public void closeMobileDataNetwork() {
//		try {
//			// mobileNet.invoke(iConnectivityManager, false);
//			if (Build.VERSION.SDK_INT > 19) {
//				mSetMethod.invoke(telephonyManager, false);
////				try {
////					openDataConnect();
////				} catch (SecurityException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				} catch (ClassNotFoundException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				} catch (NoSuchMethodException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
//			} else {
//				mSetMethod.invoke(connectivityManager, false);
//			}
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public void initNetWork() {
		Class[] getArgArray = null;
		Class[] setArgArray = new Class[] { boolean.class };
		Object[] getArgInvoke = null;
		try {
			if (Build.VERSION.SDK_INT > 19) {
				mGetMethod = telephonyManager.getClass().getMethod(
						"getDataEnabled", getArgArray);
				mSetMethod = telephonyManager.getClass().getMethod(
						"setDataEnabled", setArgArray);
			} else {
				mGetMethod = connectivityManager.getClass().getMethod(
						"getMobileDataEnabled", getArgArray);
				mSetMethod = connectivityManager.getClass().getMethod(
						"setMobileDataEnabled", setArgArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 获取系统网络设置方法
	 */
	public void openDataConnect() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Method dataConnSwitchmethod;
		Class telephonyManagerClass;
		Object ITelephonyStub;
		Class ITelephonyClass;
		boolean isEnabled = false;

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 获取当前的状态
		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
			isEnabled = true;
		} else {
			isEnabled = false;
		}
		telephonyManagerClass = Class.forName(telephonyManager.getClass()
				.getName());
		Method getITelephonyMethod = telephonyManagerClass
				.getDeclaredMethod("getITelephony");
		getITelephonyMethod.setAccessible(true);
		ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
		ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
		if (isEnabled) {
			dataConnSwitchmethod = ITelephonyClass
					.getDeclaredMethod("disableDataConnectivity");
		} else {
			dataConnSwitchmethod = ITelephonyClass
					.getDeclaredMethod("enableDataConnectivity");
		}
		dataConnSwitchmethod.setAccessible(true);
		dataConnSwitchmethod.invoke(ITelephonyStub);
	}

	// public void initMobileNetwork(){
	// try {
	// connectivityManagerClass =
	// Class.forName(connectivityManager.getClass().getName());
	// iConnectivityManagerField =
	// connectivityManagerClass.getDeclaredField("mService");
	// iConnectivityManagerField.setAccessible(true);
	// iConnectivityManager =
	// iConnectivityManagerField.get(connectivityManager);
	// iConnectivityManagerClass =
	// Class.forName(iConnectivityManager.getClass().getName());
	// // mobileNet =
	// iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled",
	// Boolean.TYPE);
	// // mobileNet.setAccessible(true);
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (NoSuchFieldException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalArgumentException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (NoSuchMethodException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	/**
	 * 判断是否有可用网络
	 * 
	 * @return
	 */
	public boolean netStatus() {
		boolean flag = false;
		ConnectivityManager cwjManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cwjManager.getActiveNetworkInfo() != null) {
			flag = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return flag;
	}

	/**
	 * 得到当然使用的网络情况
	 * 
	 * @return（WIFI、MOBILE）
	 */
	public String NetWorkStatus() {
		String result = null;
		boolean flag = false;
		/*
		 * ConnectivityManager cwjManager = (ConnectivityManager) context
		 * .getSystemService(Context.CONNECTIVITY_SERVICE);
		 */
		NetworkInfo netWorkInfo = connectivityManager.getActiveNetworkInfo();
		Log.i("Judge", "netWorkInfo:" + netWorkInfo);
		if (netWorkInfo != null) {
			flag = connectivityManager.getActiveNetworkInfo().isAvailable();
			if (flag) {
				result = netWorkInfo.getTypeName();
			}
		}
		return result;
	}

}
