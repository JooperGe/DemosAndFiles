package com.viash.voice_assistant.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.DownloadNotification;
import com.viash.voicelib.utils.CustomToast;

public class AppDownloadManager {
	private static boolean DEBUG = true;
	private static String TAG = "AppDownloadManager";
	
	public static String APP_DOWNLOAD_FAIL = "com.viash.voice_assistant.APP_DOWNLOAD_FAIL";
	
	private static HashMap<String, DownloadThread> mDownloadThread = new HashMap<String, DownloadThread>();
	
	private static final int UPDATEDPTIME = 1000;
	private static Context mContext;
	private static Handler mHandler;
	
	public static boolean startDownload(Context context, Handler handler, String title, String url)//, String size, String version)
	{
		mContext = context;
		mHandler = handler;
		if(!mDownloadThread.containsKey(url) && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			DownloadThread downloadThread = new DownloadThread(title, url);
			downloadThread.start();
			mDownloadThread.put(url, downloadThread);
			return true;
		}else{
			return false;
		}
	}
	public static boolean startDownload(Context context, Handler handler, String title, String url, String app_icon)//, String size , String version)
	{
		mContext = context;
		mHandler = handler;
		if(!mDownloadThread.containsKey(url) && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			DownloadThread downloadThread = new DownloadThread(title, url, app_icon);
			downloadThread.start();
			mDownloadThread.put(url, downloadThread);
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean stopDownload(Context context, String url)
	{
		mContext = context;
		DownloadThread downloadThread = mDownloadThread.get(url);
		if(downloadThread != null){
			downloadThread.stopDownload();
			mDownloadThread.remove(url);
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isUrlDownloading(String url)
	{
		boolean ret = false;
		if(mDownloadThread.get(url) != null)
		{
			ret = true;
		}
		
		
		return ret;
	}
	
	static class DownloadThread extends Thread{
		private static final String DOWNLOAD_FOLDER = "Download";
		boolean interrupt = false;
		String downloadTitle;
		String downloadUrl;
		String mDownloadAppIcon = null;
		//double downloadSize;
		//String version;
		File saveFile;
		int downloadPercent = 0;
		Timer mTimer;
		public DownloadThread(String title, String url){//, String download_size_string , String version) {
			this.downloadTitle = title;
			this.downloadUrl = url;
			this.interrupt = false;
			/*if (download_size_string != null) {
				String tempString = download_size_string.replace("MB", "");
				try{
					this.downloadSize = Double.parseDouble(tempString);
				}catch (Exception e) {
					e.printStackTrace();
					this.downloadSize = 0.0;
				}
			}else {
				downloadSize = 0.0;
			}*/
			
			mTimer = new Timer();
			//String filename = title;//url.substring(url.lastIndexOf("/") + 1, url.length());
			//saveFile = new File(Environment.getExternalStorageDirectory(), "/Download/" + filename + version);
		}
		public void downLoadFromHttp(HttpURLConnection connHttp){
			try {
//				connHttp.connect();
				if (connHttp.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.e(TAG, connHttp.getURL().toString());
					String newUrl = connHttp.getURL().toString();
					if (!newUrl.contains("404.html")) {
						int totalLength = connHttp.getContentLength();
						initFolder();
						String filename = newUrl.substring(newUrl.lastIndexOf("/") + 1, newUrl.length());
						filename = filename.replace("mumayi", "哦啦语音");
						saveFile = new File(Environment.getExternalStorageDirectory(),"/Download/" + filename);
						
						
						
						/*double totalSize = ((double)totalLength) / 1024 / 1024;
						BigDecimal bg = new BigDecimal(totalSize);
						totalSize = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					
						//Log.e(TAG, "totalLength is: " + totalLength + " Total size is " + totalSize + " downloadSize is " + downloadSize );
						//if ( downloadSize == 0.0 || ((downloadSize + 0.2) > totalSize && (downloadSize - 0.2) < totalSize)) {*/	
						
						if(checkIsFileExist(saveFile, totalLength)){
							showToast(downloadTitle + " 已存在本地端!");
							Intent intent = new Intent(Intent.ACTION_VIEW);
							Uri uri = Uri.fromFile(saveFile);
							intent.setDataAndType(uri, "application/vnd.android.package-archive");
							if(mDownloadAppIcon == null){
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", R.drawable.notification_update_logo, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}else{
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", mDownloadAppIcon, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}
						}else{
							InputStream is = connHttp.getInputStream();
							FileOutputStream fileOutputStream = null;
							if (is != null) {
								// update download percent thread
								mTimer.schedule(new TimerTask(){
									@Override
									public void run() {
										DownloadNotification.updateNotification(downloadUrl, downloadPercent);
									}
								}, 1000, UPDATEDPTIME);
								
								// start to download
								fileOutputStream = new FileOutputStream(saveFile);
								byte[] b = new byte[2048];
								int read = -1;
								int totalRead = 0;
								while ((read = is.read(b)) != -1 && !interrupt) {
									fileOutputStream.write(b, 0, read);
									totalRead += read;
									downloadPercent = (int) (((float) totalRead/(float) totalLength)*100);
								}
							}else{
								downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
								sendDownloadFailMessage(downloadUrl);
							}
							fileOutputStream.flush();
							if (fileOutputStream != null) {
								fileOutputStream.close();
							}
							if(interrupt){
								downloadOver("下载中断", downloadTitle + " 已中断下载!", R.drawable.notification_update_logo);
								free();
							}else{
								showToast(downloadTitle + " 下载完成!");
								downloadPercent = 100;
								Intent intent = new Intent(Intent.ACTION_VIEW);
								Uri uri = Uri.fromFile(saveFile);
								intent.setDataAndType(uri, "application/vnd.android.package-archive");
								if(mDownloadAppIcon == null){
									downloadOver("下载完成", downloadTitle + "   - 点击安装", R.drawable.notification_update_logo, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}else{
									downloadOver("下载完成", downloadTitle + "   - 点击安装", mDownloadAppIcon, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}
							}
						}
					}
					else {
						downloadOver("下载失败", downloadTitle + " 下载链接已失效或是该软件已经下架!",R.drawable.notification_update_logo);
						sendDownloadFailMessage(downloadUrl);
					}
				}else{
					downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
					sendDownloadFailMessage(downloadUrl);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} catch (IOException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} finally {
				if(connHttp != null){
					connHttp.disconnect();
					connHttp = null;
				}
				if(connection != null){
					connection.disconnect();
					connection = null;
				}
				mTimer.cancel();
				//DownloadNotification.cancel(downloadUrl);
				mDownloadThread.remove(downloadUrl);
			}
		}
		public void downLoadFromHttps(HttpsURLConnection connHttps){
			
			try {
//				connHttps.connect();
				if (connHttps.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.e(TAG, connHttps.getURL().toString());
					String newUrl = connHttps.getURL().toString();
					if (!newUrl.contains("404.html")) {
						int totalLength = connHttps.getContentLength();
						initFolder();
						String filename = newUrl.substring(newUrl.lastIndexOf("/") + 1, newUrl.length());
						filename = filename.replace("mumayi", "哦啦语音");
						saveFile = new File(Environment.getExternalStorageDirectory(),"/Download/" + filename);
						
						
						
						/*double totalSize = ((double)totalLength) / 1024 / 1024;
						BigDecimal bg = new BigDecimal(totalSize);
						totalSize = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					
						//Log.e(TAG, "totalLength is: " + totalLength + " Total size is " + totalSize + " downloadSize is " + downloadSize );
						//if ( downloadSize == 0.0 || ((downloadSize + 0.2) > totalSize && (downloadSize - 0.2) < totalSize)) {*/	
						
						if(checkIsFileExist(saveFile, totalLength)){
							showToast(downloadTitle + " 已存在本地端!");
							Intent intent = new Intent(Intent.ACTION_VIEW);
							Uri uri = Uri.fromFile(saveFile);
							intent.setDataAndType(uri, "application/vnd.android.package-archive");
							if(mDownloadAppIcon == null){
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", R.drawable.notification_update_logo, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}else{
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", mDownloadAppIcon, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}
						}else{
							InputStream is = connHttps.getInputStream();
							FileOutputStream fileOutputStream = null;
							if (is != null) {
								// update download percent thread
								mTimer.schedule(new TimerTask(){
									@Override
									public void run() {
										DownloadNotification.updateNotification(downloadUrl, downloadPercent);
									}
								}, 1000, UPDATEDPTIME);
								
								// start to download
								fileOutputStream = new FileOutputStream(saveFile);
								byte[] b = new byte[2048];
								int read = -1;
								int totalRead = 0;
								while ((read = is.read(b)) != -1 && !interrupt) {
									fileOutputStream.write(b, 0, read);
									totalRead += read;
									downloadPercent = (int) (((float) totalRead/(float) totalLength)*100);
								}
							}else{
								downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
								sendDownloadFailMessage(downloadUrl);
							}
							fileOutputStream.flush();
							if (fileOutputStream != null) {
								fileOutputStream.close();
							}
							if(interrupt){
								downloadOver("下载中断", downloadTitle + " 已中断下载!", R.drawable.notification_update_logo);
								free();
							}else{
								showToast(downloadTitle + " 下载完成!");
								downloadPercent = 100;
								Intent intent = new Intent(Intent.ACTION_VIEW);
								Uri uri = Uri.fromFile(saveFile);
								intent.setDataAndType(uri, "application/vnd.android.package-archive");
								if(mDownloadAppIcon == null){
									downloadOver("下载完成", downloadTitle + "   - 点击安装", R.drawable.notification_update_logo, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}else{
									downloadOver("下载完成", downloadTitle + "   - 点击安装", mDownloadAppIcon, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}
							}
						}
					}
					else {
						downloadOver("下载失败", downloadTitle + " 下载链接已失效或是该软件已经下架!", R.drawable.notification_update_logo);
						sendDownloadFailMessage(downloadUrl);
					}
				}else{
					downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
					sendDownloadFailMessage(downloadUrl);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} catch (IOException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} finally {
				if(connHttps != null){
					connHttps.disconnect();
					connHttps = null;
				}
				if(connection != null){
					connection.disconnect();
					connection = null;
				}
				mTimer.cancel();
				//DownloadNotification.cancel(downloadUrl);
				mDownloadThread.remove(downloadUrl);
			}
		}
		public DownloadThread(String title, String url, String app_icon) {//, String download_size_string , String version) {
			this.downloadTitle = title;
			this.downloadUrl = url;
			this.mDownloadAppIcon = app_icon;
			/*if (download_size_string != null) {
				String tempString = download_size_string.replace("MB", "");
				try{
					this.downloadSize = Double.parseDouble(tempString);
				}catch (Exception e) {
					e.printStackTrace();
					this.downloadSize = 0.0;
				}
			}else {
				downloadSize = 0.0;
			}
*/			//this.version = version;
			this.interrupt = false;
			
			mTimer = new Timer();
			/*String filename = title;//url.substring(url.lastIndexOf("/") + 1, url.length());
			if(filename.indexOf(".") >= 0){
				saveFile = new File(Environment.getExternalStorageDirectory(), "/Download/" + filename + version);
			}else{
				saveFile = new File(Environment.getExternalStorageDirectory(), "/Download/" + filename + version + ".apk");
			}*/
		}
		public void stopDownload(){
			if(DEBUG) Log.d(TAG, "is stoping: " + downloadUrl);
			interrupt = true;
		}
		HttpURLConnection connection = null;
		
		@Override
		public void run() {
			DownloadNotification.addDownloadingNotification(downloadUrl, downloadTitle + " 下载档案中...");
			try {
				URL url = new URL(downloadUrl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setInstanceFollowRedirects(false);
				connection.connect();
				int responseCode = connection.getResponseCode();
				if(responseCode == 302){
					 String location = connection.getHeaderField("Location"); 
					 URL newUrl = new URL(location);
					 if(newUrl.toString().contains("https")){
						 HttpsURLConnection conn = (HttpsURLConnection) newUrl.openConnection();  
						 conn.connect();
						 if(conn.getURL().toString().endsWith(".apk")){
							 downLoadFromHttps(conn);
						 }else{
							 conn.setInstanceFollowRedirects(true);
							 downLoadFromHttps(conn);
						 }
					 }else if(location.contains("http")){
						 HttpURLConnection conn = (HttpURLConnection) newUrl.openConnection(); 
						 conn.connect();
						 if(conn.getURL().toString().endsWith(".apk")){
							 downLoadFromHttp(conn);
						 }else{
							 conn.setInstanceFollowRedirects(true);
							 downLoadFromHttp(conn);
						 }
					 }
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*//if(DEBUG) Log.d(TAG, "is downloading: " + downloadUrl + " > " + saveFile.getAbsolutePath());
			DownloadNotification.addDownloadingNotification(downloadUrl, downloadTitle + " 下载档案中...");
			
			HttpURLConnection connection = null;
			try {
				URL url = new URL(downloadUrl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setInstanceFollowRedirects(true);
				connection.connect();
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.e(TAG, connection.getURL().toString());
					String newUrl = connection.getURL().toString();
					if (!newUrl.contains("404.html") && newUrl.endsWith(".apk")) {
						int totalLength = connection.getContentLength();
						
						String filename = newUrl.substring(newUrl.lastIndexOf("/") + 1, newUrl.length());
						filename = filename.replace("mumayi", "哦啦语音");
						saveFile = new File(Environment.getExternalStorageDirectory(), "/Download/" + filename);
						double totalSize = ((double)totalLength) / 1024 / 1024;
						BigDecimal bg = new BigDecimal(totalSize);
						totalSize = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					
						//Log.e(TAG, "totalLength is: " + totalLength + " Total size is " + totalSize + " downloadSize is " + downloadSize );
						//if ( downloadSize == 0.0 || ((downloadSize + 0.2) > totalSize && (downloadSize - 0.2) < totalSize)) {	
						
						if(checkIsFileExist(saveFile, totalLength)){
							showToast(downloadTitle + " 已存在本地端!");
							Intent intent = new Intent(Intent.ACTION_VIEW);
							Uri uri = Uri.fromFile(saveFile);
							intent.setDataAndType(uri, "application/vnd.android.package-archive");
							if(mDownloadAppIcon == null){
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", R.drawable.notification_update_logo, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}else{
								downloadOver("已下载完成", downloadTitle + " 已存在本地端!   - 点击安装", mDownloadAppIcon, intent);
								showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
							}
						}else{
							InputStream is = connection.getInputStream();
							FileOutputStream fileOutputStream = null;
							if (is != null) {
								// update download percent thread
								mTimer.schedule(new TimerTask(){
									@Override
									public void run() {
										DownloadNotification.updateNotification(downloadUrl, downloadPercent);
									}
								}, 1000, UPDATEDPTIME);
								
								// start to download
								fileOutputStream = new FileOutputStream(saveFile);
								byte[] b = new byte[2048];
								int read = -1;
								int totalRead = 0;
								while ((read = is.read(b)) != -1 && !interrupt) {
									fileOutputStream.write(b, 0, read);
									totalRead += read;
									downloadPercent = (int) (((float) totalRead/(float) totalLength)*100);
								}
							}else{
								downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
								sendDownloadFailMessage(downloadUrl);
							}
							fileOutputStream.flush();
							if (fileOutputStream != null) {
								fileOutputStream.close();
							}
							if(interrupt){
								downloadOver("下载中断", downloadTitle + " 已中断下载!", R.drawable.notification_update_logo);
								free();
							}else{
								showToast(downloadTitle + " 下载完成!");
								downloadPercent = 100;
								Intent intent = new Intent(Intent.ACTION_VIEW);
								Uri uri = Uri.fromFile(saveFile);
								
								
								
								intent.setDataAndType(uri, "application/vnd.android.package-archive");
								if(mDownloadAppIcon == null){
									downloadOver("下载完成", downloadTitle + "   - 点击安装", R.drawable.notification_update_logo, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}else{
									downloadOver("下载完成", downloadTitle + "   - 点击安装", mDownloadAppIcon, intent);
									showToast(this.downloadTitle+"已经下载完成，请在通知栏点击安装");
								}
							}
						}
					}
					else {
						downloadOver("下载失败", downloadTitle + " 下载链接已失效或是该软件已经下架!", R.drawable.notification_update_logo);
						sendDownloadFailMessage(downloadUrl);
					}
				}else{
					downloadOver("下载失败", downloadTitle + " 连接服务器失败!", R.drawable.notification_update_logo);
					sendDownloadFailMessage(downloadUrl);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} catch (IOException e) {
				e.printStackTrace();
				interrupt = true;
				free();
				downloadOver("下载错误", downloadTitle + " 下载时发生错误!", R.drawable.notification_update_logo);
				sendDownloadFailMessage(downloadUrl);
			} finally {
				if(connection != null){
					connection.disconnect();
					connection = null;
				}
				mTimer.cancel();
				//DownloadNotification.cancel(downloadUrl);
				mDownloadThread.remove(downloadUrl);
			}*/
		}
		private void sendDownloadFailMessage(String downloadUrl) {
			Intent intent = new Intent();
			intent.setAction(APP_DOWNLOAD_FAIL);
			intent.putExtra("id", downloadUrl);
			mContext.sendBroadcast(intent);
		}
		private void initFolder() {
			String filepath = Environment.getExternalStorageDirectory().getPath();
			File file = new File(filepath, DOWNLOAD_FOLDER);
			if (!file.exists()) {
				file.mkdirs();
			}
			
		}
		private boolean checkIsFileExist(File file, int totalLength) {
			if(file.exists()){
				if(file.length() == totalLength){
					return true;
				}
			}
			return false;
		}
		private void downloadOver(String title, String description, int icon) {
			DownloadNotification.addDownloadOver(downloadUrl, title, description, icon);
		}
		private void downloadOver(String title, String description, int icon, Intent intent) {
			DownloadNotification.addDownloadOver(downloadUrl, title, description, icon, intent);
		}
		/*private void downloadOver(String title, String description, String downloadAppIcon) {
			DownloadNotification.addDownloadOver(downloadUrl, title, description, downloadAppIcon);
		}*/
		private void downloadOver(String title, String description, String downloadAppIcon, Intent intent) {
			DownloadNotification.addDownloadOver(downloadUrl, title, description, downloadAppIcon, intent);
		}
		private void free() {
			if(saveFile.exists()){
				saveFile.delete();
			}
		}
	}
	
	public static void showToast(final String info) {
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				CustomToast.makeToast(mContext, info.toString());//, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
