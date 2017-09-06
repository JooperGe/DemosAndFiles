package com.viash.voice_assistant.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.iflytek.aitalk4.FilesCopyUtil;
import com.viash.voice_assistant.handler.AppCrashHandler;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.HttpsSSLSocketFactory;


public class UploadService extends Service {

	private String filePath = AppCrashHandler.filePath;
	private String url = "http://api.olavoice.com/OlaPushHtml/publish/oladump";
	private List<File> mList = null;
	protected HttpClient mHttpClient;
	protected static final int CONNECT_TIMEOUT = 5000;
	protected static final int SO_TIMEOUT = 20000;
	private int maxSize = 100*1024;
	private int uploadCount = 0;
	private uploadThread mThread;
	@Override
	public void onCreate() {
		super.onCreate();
		init();
		uploadFiles();
		mThread= new uploadThread();
		mThread.start();
	}
	
	private void init()
	{
		mHttpClient = HttpsSSLSocketFactory.createMyHttpClient(CONNECT_TIMEOUT,SO_TIMEOUT);
	}
	private void uploadFiles()
	{
		mList = FilesCopyUtil.getFileSort(filePath);
		
	}
	
	class uploadThread extends Thread{
		
		@Override
		public void run() {
			HttpPost httpPost = new HttpPost(url);
			InputStream ipStream;
			HttpResponse response;	
			String version = ""+ClientPropertyUtil.getVersionCode(UploadService.this);
			httpPost.setHeader("olaversion",version);
			
			try
			{
				
				for(File file : mList)
				{				
				   long length =  file.length();
				   if(length >= maxSize)
				   {
					   file.delete();
					   continue;
				   }				   
				   httpPost.setHeader("filename", file.getName());
				   ipStream = new FileInputStream(file);
				   InputStreamEntity reqEntity = new InputStreamEntity(ipStream, file.length()); 				
				   httpPost.setEntity(reqEntity);
				   response = mHttpClient.execute(httpPost);
				   if(response.getStatusLine().getStatusCode() == 200)
				   {					   											
					   file.delete();
					   uploadCount++;
					   if(uploadCount >= 5)
					   {
						   break;
					   }
					   mHttpClient.getConnectionManager().shutdown();
					   init();
				   }
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(mThread != null)
			{
				mThread.interrupt();
				mThread = null;
			}
			UploadService.this.stopSelf();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mThread != null)
		{
			mThread.interrupt();
			mThread = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}