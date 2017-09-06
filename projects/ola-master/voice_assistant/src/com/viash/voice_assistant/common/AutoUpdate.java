package com.viash.voice_assistant.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.handler.AutoUpdateHandler;
import com.viash.voice_assistant.service.AutoUpdateNotificationService;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.HttpUtil;
import com.viash.voicelib.utils.JsonUtil;

public class AutoUpdate {
	private static final boolean DEBUG = true;
	private static final String TAG = "AutoUpdate";
	public static final String AutoUpdateID = "autoupdate";
	
	private static Context mContext;
	private static Handler mHandler;
	
	private static final String INTERNAL_SEVER_NAME = "http://portal.olavoice.com/";	
	private static String LOCAL_APK_PATH = "voice_assist";
	//private static String UPDATE_APK_PREFIX = "voice_assistant_update";
	private static final String VersionServerUrl = "http://www.ola.com.cn/update/ola/update.txt";
	private static String UpdateServerAPKUrl = null;

	private static final int UPDATEDPTIME = 1000;
	private static int downloadPercent = 0;
	private static boolean interrupt = false;
	private static File updateFile = null;
	private static File saveFile;
	
	private static boolean isDownloading = false;
	private static int mVersionCode = 0;
	//private static String mVersionName = null;
	private static int serverVersionCode = 0;
	//private static String serverReleaseTime = null;
    private static long mFile_size;
    private static String mFile_name;
    
	public static void init(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;		
		mVersionCode = getVersionCode();
		if(GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE){
			//VersionServerUrl = "http://www.olavoice.com/ClientInfo/update.txt";
			UpdateServerAPKUrl = null;			
		}else{
			//VersionServerUrl = INTERNAL_SEVER_NAME + "ola_version.txt";
			UpdateServerAPKUrl = INTERNAL_SEVER_NAME + "voice_assistant.apk";
		}
	}

	public static boolean start() {
		if (mContext == null || mHandler == null)
			return false;
		
		if(VersionServerUrl == null)
			return false;
		
		if(isDownloading)
			return false;
		
		checkVersion();
		return true;
	}
	
	private static boolean updateFileExist() {
		boolean ret = false;
		/*File path = new File(Environment.getExternalStorageDirectory(), "/" + LOCAL_APK_PATH);
		if(!path.exists()) path.mkdirs();
		File[] file = path.listFiles();
		if(file != null){
			List<File> tempUpdateFile = new ArrayList<File>();
			for(int i = 0 ; i < file.length ; i++){
				if(filterUpdateFile(file[i].getName())){
					tempUpdateFile.add(file[i]);
					ret = true;
				}
			}
			if(ret == true){
				updateFile = findLatestUpdateFile(tempUpdateFile);
				if(updateFile == null){
					ret = false;
				}else{
					// check is the update file same as server's file
					ret = compareWithServer(updateFile);					
					if(ret == false){
						updateFile.delete();
					}
				}
			}
		}*/
		File file = new File(Environment.getExternalStorageDirectory()+"/" + LOCAL_APK_PATH+"/"+mFile_name);
		if(file.exists())
		{
			if(file.length() == mFile_size)
				ret = true;
			else
				file.delete();
		}
		return ret;
	}

	/*private static boolean compareWithServer(File updateFile) {
		boolean ret = false;
		long size = updateFile.length();

		try {
			URL url = new URL(UpdateServerAPKUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				int totalLength = connection.getContentLength();
				if(DEBUG) Log.d(TAG, "file size/server size: " + size + "/" + totalLength);
				if(totalLength == size){
					ret = true;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}*/

	/*private static File findLatestUpdateFile(List<File> tempUpdateFile) {
		File retFile = null;
		int latestNum = -1;
		int latestPoint = -1;
		//if(tempUpdateFile.size() == 1){
		//	int num = Integer.valueOf(tempUpdateFile.get(0).getName().substring(UpdateServerAPK.length() + 1, tempUpdateFile.get(0).getName().lastIndexOf(".")));
		//	if(DEBUG) Log.d(TAG, "update version: " + num);
		//	retFile = tempUpdateFile.get(0);
		//}else{
			for(int i = 0 ; i < tempUpdateFile.size() ; i++){
				int num = Integer.valueOf(tempUpdateFile.get(i).getName().substring(UPDATE_APK_PREFIX.length() + 1, tempUpdateFile.get(i).getName().lastIndexOf(".")));
				if(DEBUG) Log.d(TAG, "update version: " + num + " - " + mVersionCode);
				
				if(mVersionCode >= num || serverVersionCode > num){
					tempUpdateFile.get(i).delete();
					if(DEBUG) Log.d(TAG, "delete file: " + tempUpdateFile.get(i).getAbsolutePath());
				}else{
					if(latestNum < num){
						latestNum = num;
						latestPoint = i;
					}
				}
			}
			if(latestPoint != -1){
				retFile = tempUpdateFile.get(latestPoint);
			}
		//}
		return retFile;
	}

	private static boolean filterUpdateFile(String name) {
		boolean ret = false;
		int p = name.lastIndexOf(".");
		if(p > 0){
			String extension = name.substring(p+1, name.length());
			if(extension.equals("apk") && name.indexOf(UPDATE_APK_PREFIX) == 0){
				ret = true;
			}
		}
		return ret;
	}*/

	public static void stop(){
		Intent intent = new Intent(mContext, AutoUpdateNotificationService.class);
		intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
		mContext.startService(intent);
		
		isDownloading = false;
		interrupt = true;
	}
	
	public static void free(){
		if(saveFile.exists()){
			saveFile.delete();
		}
	}

	private static void checkVersion() {
		Runnable checkVersionThread = new Runnable() {
			@Override
			public void run() {
				String result = HttpUtil.sendGetCommand(mContext, VersionServerUrl);
				try {
					if (result == null) {
						mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_SERVERERROR);
					} else {
						JSONObject obj = new JSONObject(result);
						serverVersionCode = JsonUtil.optInt(obj, "build_version", -1);
						//serverReleaseTime = JsonUtil.optString(obj, "release_time", null);
						UpdateServerAPKUrl = JsonUtil.optString(obj, "update_url", UpdateServerAPKUrl);
						mFile_name = JsonUtil.optString(obj, "file_name", null);
						checkVersion(mVersionCode, serverVersionCode);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(checkVersionThread).start();
	}

	private static void checkVersion(int mVersionCode, int serverVersionCode) {
		if (serverVersionCode > mVersionCode) {
			if(updateFileExist()){
				mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_HASUPDATE);
			}else{
				//mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_NEWVERSION);
				mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_DIRECTLY);
			}
		} else {
			mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_NONEWVERSION);
		}
	}

	public static boolean isDownloading()
	{
		return isDownloading;
	}
	public static void startAutoUpdateFromDialog(int version,String url,long file_size,String file_name)
	{
		serverVersionCode = version;
		UpdateServerAPKUrl = url;
		mFile_size = file_size;
		mFile_name = file_name;
		checkVersion(mVersionCode, serverVersionCode);
	}
	
	public static void download(Context context) {
		if(context == null) return;
		mContext = context;
		
		if(mHandler == null) mHandler = new AutoUpdateHandler(mContext);
		
		Intent intent = new Intent(mContext, AutoUpdateNotificationService.class);
		intent.setAction(AutoUpdateNotificationService.ACTION_START_DOWNLOAD);
		mContext.startService(intent);
		
		isDownloading = true;
		interrupt = false;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			saveFile = new File(Environment.getExternalStorageDirectory(), "/" + LOCAL_APK_PATH + "/" + mFile_name);
			if(DEBUG) Log.d(TAG, "save File: " + saveFile.getAbsolutePath());
			
			Runnable downloadThread = new Runnable() {
				@Override
				public void run() {
					HttpURLConnection connection = null;
					try {
						// HTTP connection reuse which was buggy pre-froyo
						//if (Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.ECLAIR_MR1) {
						//	System.setProperty("http.keepAlive", "false");
						//}
						
						URL url = new URL(UpdateServerAPKUrl);
						connection = (HttpURLConnection) url.openConnection();
						connection.connect();
						if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
							int totalLength = connection.getContentLength();
							InputStream is = connection.getInputStream();
							FileOutputStream fileOutputStream = null;
							if (is != null) {
								// update download percent thread
								Runnable udpThread = new Runnable(){
									@Override
									public void run() {
										try {
											Thread.sleep(UPDATEDPTIME);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										
										do{
											Message msg = new Message();
											Bundle bundle = new Bundle();
											bundle.putInt("percent", downloadPercent);
											msg.setData(bundle);
											msg.what = MsgConst.MSG_UPDATE_VERSION_DOWNLOADUPDATE;
											mHandler.sendMessage(msg);
											
											try {
												Thread.sleep(UPDATEDPTIME);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}while(downloadPercent < 100 && !interrupt);
									}
								};
								new Thread(udpThread).start();
								
								// start to download
								fileOutputStream = new FileOutputStream(saveFile);
								byte[] b = new byte[32768];
								int read = -1;
								int totalRead = 0;
								while ((read = is.read(b)) != -1 && !interrupt) {
									fileOutputStream.write(b, 0, read);
									totalRead += read;
									downloadPercent = (int) (((float) totalRead/(float) totalLength)*100);
								}
							}else{
								mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_SERVERERROR);
							}
							fileOutputStream.flush();
							if (fileOutputStream != null) {
								fileOutputStream.close();
							}
							if(interrupt){
								mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_DOWNLOADSTOP);
								free();
							}else{
								downloadPercent = 100;
								mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_DOWNLOADSUCCESS);
							}
						}else{
							mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_SERVERERROR);
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
						interrupt = true;
						free();
						mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_SERVERERROR);
					} catch (IOException e) {
						e.printStackTrace();
						interrupt = true;
						free();
						mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_SERVERERROR);
					} finally {
						isDownloading = false;
						if(connection != null){
							connection.disconnect();
							connection = null;
						}
					}
				}
			};
			new Thread(downloadThread).start();
		} else {
			isDownloading = false;
			mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_NOSDCARD);
		}
	}

	public static void update() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				if(updateFileExist()){
					Intent intent = new Intent(Intent.ACTION_VIEW);
					updateFile = new File(Environment.getExternalStorageDirectory()+"/" + LOCAL_APK_PATH+"/"+mFile_name);
					Uri uri = Uri.fromFile(updateFile);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(uri, "application/vnd.android.package-archive");
					mContext.startActivity(intent);
				}else{
					mHandler.sendEmptyMessage(MsgConst.MSG_UPDATE_VERSION_UPDATEFAIL);
				}
			}
		}).start();
	}

	protected static int getVersionCode() {
		int verCode = -1;
		try {
			verCode = mContext.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.getStackTrace();
		}
		return verCode;
	}

	protected static String getVersionName() {
		String verName = "";
		try {
			int verCode = mContext.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionCode;
			verName = mContext.getPackageManager().getPackageInfo("com.viash.voice_assistant", 0).versionName;
			verName += " build" + verCode;
		} catch (NameNotFoundException e) {
			e.getStackTrace();
		}
		return verName;
	}
	
	public static void test(){
		// update download percent thread
		Runnable udpThread = new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(UPDATEDPTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				downloadPercent = 0;
				interrupt = false;
				do{
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("percent", downloadPercent);
					msg.setData(bundle);
					msg.what = MsgConst.MSG_UPDATE_VERSION_DOWNLOADUPDATE;
					mHandler.sendMessage(msg);
					
					downloadPercent += 10;
					try {
						Thread.sleep(UPDATEDPTIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}while(downloadPercent < 100 && !interrupt);
			}
		};
		new Thread(udpThread).start();
	}
}
