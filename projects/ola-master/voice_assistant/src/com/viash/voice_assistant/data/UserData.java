package com.viash.voice_assistant.data;

import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

@SuppressLint({ "WorldWriteableFiles", "WorldReadableFiles" })
public class UserData {
	private static final String AppShare = "aola";
	private static final String USER_USERNAME = "username";
	private static final String USER_PASSWORD = "password";
	private static final String PHONE_BINDED = "phone";
	private static final String VCODE_EXPIRE_DATE = "vcode_expire_date";
	private static final String VCODE_MOBILE = "vcode_mobile";
	//private static final String LOCK_HOME_KEY_ENABLED = "lock_home_key_enabled";
	//private static boolean mIsLockHomeKey = false;
	private static Context context;
	
	public static void saveUserInfo(Context context,String username,String password){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putString(USER_USERNAME, username);
		editor.putString(USER_PASSWORD, password);
		editor.commit();
		UserData.context = context;
		GlobalData.setUserLoggedin(true);
	}
	 
	public static String getPwd(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		return sp.getString(USER_PASSWORD, null);
	}
	
	public static String getUserName(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		return sp.getString(USER_USERNAME, null);
	}
	
	public static void exit(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		//editor.putString(USER_USERNAME, null);
		editor.putString(USER_PASSWORD, null);
		editor.commit();
		GlobalData.setUserLoggedin(false);
		//NewAssistActivity.isUserLoggedin = false;
	}
	
	/**
	 * Get user Info 
	 * 0 username
	 * 1 password
	 * @param context
	 * @return
	 */
	public static String[] getUserInfo(Context context){
		String[] array = new String[2];
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		array[0] = sp.getString(USER_USERNAME, null);
		array[1] = sp.getString(USER_PASSWORD, null);
		return array;
	}
	
	public static String getPhone(Context context) {
		SharedPreferences sp = context.getSharedPreferences(AppShare, Context.MODE_WORLD_READABLE);
		String phone = sp.getString(PHONE_BINDED, null);
		return phone;
	}
	
	public static void setPhone(Context context, String phone) {
		SharedPreferences sp = context.getSharedPreferences(AppShare, Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putString(PHONE_BINDED, phone);
		editor.commit();
	}
	
	public static boolean isPhoneBinded(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		return sp.getString(PHONE_BINDED, null) !=null;
	}
	
	public static void setVCodeExpireDate(Context context, long expireDate) {
		SharedPreferences sp = context.getSharedPreferences(AppShare, Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putLong(VCODE_EXPIRE_DATE, expireDate);
		editor.commit();
	}
	
	public static long getVCodeExpireDate(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		return sp.getLong(VCODE_EXPIRE_DATE, 0);
	}
	
	public static void setVCodeMobile(Context context, String mobile) {
		SharedPreferences sp = context.getSharedPreferences(AppShare, Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putString(VCODE_MOBILE, mobile);
		editor.commit();
	}
	
	public static String getVCodeMobile(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		return sp.getString(VCODE_MOBILE, null);
	}
	
	public static boolean isLockHomekeyEnable(Context context) {
		if (UserData.context == null) {
	   		UserData.context = context;
	   	}
		String defaultPackage = getDefaultPackage(context);
		if ( defaultPackage != null && defaultPackage.equals("com.viash.voice_assistant")) {
			return true;
		}
		return false;
	}
	
	public static String getDefaultPackage(Context context){
    	Intent startMain = new Intent(Intent.ACTION_MAIN);
	   	startMain.addCategory(Intent.CATEGORY_HOME);
		if (UserData.context == null) {
	   		UserData.context = context;
	   	}
	   	PackageManager manager = context.getPackageManager();
	   	ActivityInfo aInfo = startMain.resolveActivityInfo(manager, PackageManager.GET_SHARED_LIBRARY_FILES);
	   	    	       	
	   	final List<ResolveInfo> apps = manager.queryIntentActivities(startMain, 0);
	   	Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
   	 
		if (apps != null) {
			final int count = apps.size();
			for (int i = 0; i < count; i++) {
				ResolveInfo info = apps.get(i);
				if (aInfo != null) {
					if (aInfo.applicationInfo.packageName
							.equals(info.activityInfo.packageName)) {
						return info.activityInfo.packageName;
					}
				}
				continue;
			}
		}
		return null;
   }
	
	public static boolean doesContainHistoryApps(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> list = am.getRecentTasks(20, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		if (list.size() == 1 ) { //except our own task.
			return false;
		}
		return true;
	}
	
/*	private static void beginDocument(XmlPullParser parser,
			String firstElementName) throws XmlPullParserException, IOException {

		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) {
			// Empty
		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		if (!parser.getName().equals(firstElementName)) {
			throw new XmlPullParserException("Unexpected start tag: found "
					+ parser.getName() + ", expected " + firstElementName);
		}
	}

	private static void nextElement(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) {
			// Empty
		}
	}*/
}
