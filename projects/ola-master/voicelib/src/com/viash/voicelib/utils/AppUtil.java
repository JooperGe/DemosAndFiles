package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class AppUtil {
	private static final String TAG = "AppUtil";

	protected static List<Intent> getFixedIntent(Context context, String appName)
	{
		List<Intent> lstIntent = new ArrayList<Intent>();
		Intent intent = null;
		
		String[][] types1 = {
				//{"WebBrowser", "text/html"},
				//{"ImageBrowser", "image/jpeg"},
				//{"MusicPlayer", "audio/x-mpeg"},
				//{"VideoPlayer", "video/mpeg"}
				
				{"WebBrowser", "http://google.com.hk", null},
				{"ImageBrowser", "file:///mnt/sdcard/test_u.jpg", "image/jpeg"},
				{"MusicPlayer", "file:///mnt/sdcard/test_u.mp3", "audio/mp3"},
				{"VideoPlayer", "file:///mnt/sdcard/test_u.mp4", "video/mpeg"}
		};
		
		
		String[][] types2 = {				
				{"Search", Intent.ACTION_SEARCH, null},
				{"Calendar", "com.android.calendar", "com.android.calendar.LaunchActivity"},
				{"Camera", "com.android.camera", "com.android.camera.Camera"},
				{"Camera", MediaStore.ACTION_IMAGE_CAPTURE, null},
				{"Calculator", "com.android.calculator2", "com.android.calculator2.Calculator"},
				{"Contacts","com.android.contacts.action.LIST_ALL_CONTACTS", null},
				{"Setting", "com.android.settings", "com.android.settings.Settings"},
				{"AlarmClock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl"},
				{"AlarmClock", "com.android.deskclock", "com.android.deskclock.DeskClock"},
				{"Call", "android.intent.action.CALL_BUTTON", null},				
		};		
		
		for(String[] type : types1)
		{
			if(type[0].equalsIgnoreCase(appName))
			{
				intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				Uri uri = Uri.parse(type[1]); 
				
				if(type[2] != null)
					intent.setDataAndType(uri, type[2]);
				else
					intent.setData(uri);
				lstIntent.add(intent);
				break;
			}
		}
		
		if(intent == null)
		{
			for(String[] type : types2)
			{
				if(type[0].equalsIgnoreCase(appName))
				{
					intent = new Intent();
					if(type[2] == null)
						intent.setAction(type[1]);
					else
						intent.setComponent(new ComponentName(type[1], type[2]));
					
					lstIntent.add(intent);
				}
			}
		}
		return lstIntent;
	}
	
	
	protected static List<String> getFixedAppString(String appName)
	{
		List<String> lstName = new ArrayList<String>();
		
		String[][] types = {
				{"Calendar", "日历", "行事历", "日程", "calendar"},
				{"Calculator", "计算器", "calculator"},
				{"Contacts","联系人", "通讯录", "Contacts"},
				{"Setting", "设置", "settings"},
				/*{"AlarmClock", "闹钟", "时钟","Clock", "Alarm"},*/
				{"Email", "邮件", "邮箱", "Gmail"},
				{"Gallery", "相册", "图库", "照片", "Gallery", "Gallery3D"},
				{"Sms", "短信", "信息", "短消息", "Message"}
		};		
		
		for(String[] type : types)
		{
			if(type[0].equalsIgnoreCase(appName))
			{
				for(int i = 1; i < type.length; i++)
				{ 
					lstName.add(type[i]);
				}
				break;
			}
		}		
		
		return lstName;
	}

	
	protected static List<AppInfo> mLatestLstApp = new ArrayList<AppInfo>();
	
	public static class AppInfo
	{
		protected String mName;
		protected String mVer;
		protected String mVendor;
		protected Intent mIntent;
		protected String mPackageName;
		
		public AppInfo(String mName, String mVer, String mVendor, Intent mIntent,String mPackageName) {
			super();
			this.mName = mName;
			this.mVer = mVer;
			this.mVendor = mVendor;
			this.mIntent = mIntent;
			this.mPackageName = mPackageName;
		}

		public AppInfo() {
		}

		public JSONObject toJsonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("title", mName);
				
				if(mVer != null)
					obj.put("version", mVer);
				
				if(mVendor != null)
					obj.put("vendor", mVendor);	
				
				if(mPackageName != null)
					obj.put("package_name", mPackageName);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return obj;
		}

		public boolean parseFromJson(JSONObject objApp) {
			mName = objApp.optString("title", null);
			mVer = objApp.optString("version", null);
			mVendor = objApp.optString("vendor", null);
			return mName != null;
		}
	}
	
	public static void listAllProviders(Context context)
	{
		PackageManager pm = context.getPackageManager();
		List<ProviderInfo> lstProviders = pm.queryContentProviders(null, 0, 0);
        if(lstProviders != null)
        {
        	for(int j = 0; j < lstProviders.size(); j++)
        	{
        		Log.e(TAG, lstProviders.get(j).authority + "  permission:" +  lstProviders.get(j).readPermission);
        	}
        }
	}
	
	public static List<AppInfo> findAllApp(Context context, boolean saveToLatest)
	{					
		List<AppInfo> lstApp = new ArrayList<AppUtil.AppInfo>();
		long time = System.currentTimeMillis();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);                
		for(int i=0;i<packages.size();i++) 
		{ 
			PackageInfo packageInfo = packages.get(i); 
			String version = packageInfo.versionName;
	        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	        mainIntent.setPackage(packageInfo.packageName);

	        final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
	        if(apps != null)
	        {
	        	for(ResolveInfo resolveInfo : apps)
	        	{
	        		if(resolveInfo.activityInfo != null && resolveInfo.activityInfo.name != null)
	        		{	        				
		        		Intent intent = new Intent(Intent.ACTION_MAIN);
		        		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        		ComponentName cn = new ComponentName(packageInfo.packageName, resolveInfo.activityInfo.name);
		        		intent.setComponent(cn);
		        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
		        		AppInfo info = new AppInfo(resolveInfo.loadLabel(pm).toString().trim(), version, null, intent,packageInfo.packageName);
						lstApp.add(info);
	        		}
	        	}
	        }		
		}	
		Log.i(TAG, "Query Time:" + (System.currentTimeMillis() - time) + "ms");
		if(saveToLatest)
			setmLatestLstApp(lstApp);
		return lstApp;
	}
	
	public static JSONObject getJsonObjectOfApps(List<AppInfo> lstApp)
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonApps = new JSONArray();
		long time = System.currentTimeMillis();
		
		if(lstApp != null)
		{
			for(AppInfo info : lstApp)
			{			
				jsonApps.put(info.toJsonObject());
			}
			
			try {
				jsonObject.put("applist", jsonApps);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		Log.i(TAG, "Convert Time:" + (System.currentTimeMillis() - time) + "ms");
		return jsonObject;
	}
	
	public static boolean launchApp(Context context, String appName, boolean exactly)
	{
		boolean ret = false;
		Intent intent = null;
		List<AppInfo> lstApp = getmLatestLstApp();
		for(AppInfo info : lstApp)
		{
			if(exactly)
			{
				if(info.mName.equalsIgnoreCase(appName))
				{
					intent = info.mIntent;		
					if(intent == null)
						intent = findIntentByAppName(context, appName);
				}
			}
			else
			{
				if(info.mName.contains(appName))
				{
					intent = info.mIntent;
				}
			}
			
			if(intent != null)
			{
				try
				{
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
					ret = true;
				}
				catch(ActivityNotFoundException e)
				{
					e.printStackTrace();
				}
				break;
			}		
		}
		
		return ret;
	}
	
	public static boolean launchFixedApp(Context context, String appName)
	{
		boolean ret = false;
		if(appName.equalsIgnoreCase("Email"))
		{
//			Intent intent = new Intent(android.content.Intent.ACTION_SEND);			
//			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "邮件");
//			intent.setType("message/rfc822") ;
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("mailto:?subject=邮件"));
						
			try
			{
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Intent intentSelection = Intent.createChooser(intent, "选择");
				intentSelection.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentSelection);
				ret = true;
			}
			catch(ActivityNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		else
		{		
			List<Intent> lstIntent = getFixedIntent(context, appName);
			for(Intent intent : lstIntent)
			{
				try
				{
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
					ret = true;
					break;
				}
				catch(ActivityNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
			
		if(!ret)
		{
			List<String> lstName = getFixedAppString(appName);
			if(lstName.size() > 0)
			{
				for(String name : lstName)
				{
					ret = launchApp(context, name, false);
					if(ret)
						break;
				}
			}
		}

		return ret;
	}
	
	public static void listAllApps(Context context)
	{
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent();
		List<ResolveInfo> lstResolve = pm.queryIntentActivities(intent, 0);
		for(ResolveInfo resolve: lstResolve)
		{
			Log.e("App", resolve.activityInfo.packageName);
		}
		
	}


	public synchronized static List<AppInfo> getmLatestLstApp() {
		return mLatestLstApp;
	}


	public synchronized static void setmLatestLstApp(List<AppInfo> mLatestLstApp) {
		AppUtil.mLatestLstApp = mLatestLstApp;
	}
	
	public static List<AppInfo> parseFromJson(Context context, JSONObject obj)
	{
		List<AppInfo> lstApp = new ArrayList<AppUtil.AppInfo>();
		JSONArray array = obj.optJSONArray("applist");
		if(array != null)
		{
			for(int i = 0; i < array.length(); i++)
			{
				try {
					JSONObject objApp = array.getJSONObject(i);
					if(objApp != null)
					{
						AppInfo info = new AppInfo();
						if(info.parseFromJson(objApp))
						{
							lstApp.add(info);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return lstApp;
	}
	
	
	public static void createShortCut(Context context, int iconId, String shortcutName, Intent intent)
	{
		String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
		String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
		Intent shortcutIntent = new Intent(ACTION_INSTALL_SHORTCUT);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		shortcutIntent.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, iconId));
		context.sendBroadcast(shortcutIntent);
	}
	
	public static Intent findIntentByAppName(Context context, String name)
	{
		Intent intent = null;
		PackageManager pm = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        if(apps != null)
        {
        	for(ResolveInfo resolveInfo : apps)
        	{
        		if(name.equalsIgnoreCase(resolveInfo.loadLabel(pm).toString().trim()))
        		{
	        		if(resolveInfo.activityInfo != null && resolveInfo.activityInfo.name != null)
	        		{	        				
		        		intent = new Intent(Intent.ACTION_MAIN);
		        		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        		ComponentName cn = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
		        		intent.setComponent(cn);
		        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
		        		break;
	        		}
        		}
        	}
        }	
        return intent;
	}
	
	public static List<ComponentName> findIntentByActionName(Context context, String actionName)
	{
		List<ComponentName> lstComponent = new ArrayList<ComponentName>();
		PackageManager pm = context.getPackageManager();

        final Intent intent = new Intent(actionName, null);
        final List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        if(apps != null)
        {
        	for(ResolveInfo resolveInfo : apps)
        	{
        		if(resolveInfo.activityInfo != null && resolveInfo.activityInfo.name != null)
        		{	        				
	        		ComponentName cn = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
	        		lstComponent.add(cn);
        		}
        	}
        }	
        return lstComponent;
	}

	public static ComponentName findTopApp(Context context)
	{
		ComponentName component = null;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> lstTask = am.getRunningTasks(1);

		if(lstTask != null && lstTask.size() > 0)
		{
			component = lstTask.get(0).topActivity;
		}
		return component;
	}
	
	public static String findAppVersionByPackageName(Context context, String packageName)
	{
		List<AppInfo> lstApp = new ArrayList<AppUtil.AppInfo>();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0); 
		
		for(int i=0;i<packages.size();i++) 
		{ 
			PackageInfo packageInfo = packages.get(i); 
			if(packageInfo.packageName.equals(packageName))
			{				
		        return packageInfo.versionName;		
			}						
		}
		return null;		
	}
	
	public static String getProgramNameByPackageName(Context context,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		String name = null;
		try {
			name = pm.getApplicationLabel(
					pm.getApplicationInfo(packageName,
							PackageManager.GET_META_DATA)).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}
}
