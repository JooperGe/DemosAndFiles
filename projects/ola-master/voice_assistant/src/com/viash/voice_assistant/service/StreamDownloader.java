package com.viash.voice_assistant.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.util.Log;

public class StreamDownloader implements DownloaderListener {
	public static final String TAG = "StreamDownloader";
	public static final boolean DEBUG = true;
	
	public static final int INIT = 0;
	public static final int REQUEST_SUCCESS = 1;
	public static final int DOWNLOAD_SUCCESS = 2;
	public static final int REQUEST_FAILURE = -1;
	public static final int DOWNLOAD_FAILURE = -2;
	public static final int DOWNLOAD_STOP = 3;
	
	private static int failTimes= 0;

	private Context mContext;
	private StreamDownloaderListener mStreamDownloaderListener;

	private int TIMEOUT = 10000;
	private List<Downloader> mDownloader = new ArrayList<Downloader>();
	//private Timer timer = null;
	
	
	public StreamDownloader(Context context, StreamDownloaderListener sdListener){
		mContext = context;
		mStreamDownloaderListener = sdListener;
	}

	public void init(String url, String cookie){
		Downloader downloader = new Downloader(mContext, mStreamDownloaderListener, this);
		downloader.setUrl(url, cookie);
		downloader.setTimeout(TIMEOUT);
		mDownloader.add(downloader);
	}
	
	public void setTimeout(int timeout){
		TIMEOUT = timeout;
	}
	
	public void start(){
		if(DEBUG) Log.d(TAG, "start: " + (mDownloader.size() - 1));
		failTimes = 0;
		Downloader target = mDownloader.get(mDownloader.size() - 1);
		if(target != null){
			new Thread(target).start();
		}
		/*if (timer == null) {
			timer = new Timer(true);
		}
		Log.e("Timer", "Timer Start");
		timer.purge();
		OwnTimerTask task = new OwnTimerTask();
		timer.schedule(task, 10000);*/

	}
	public void stop(){
		/*if (timer != null) {
			timer.purge();
			timer.cancel(); 
			timer = null;
		}*/
		for(int i = 0 ; i < mDownloader.size() ; i++){
			if(DEBUG) Log.d(TAG, "stop: " + i);
			if(mDownloader.get(i) != null)
				mDownloader.get(i).stop();
		}
	}

/*	private class OwnTimerTask extends TimerTask{ 
		
		public void run() {
			Log.e("Timer", "ON TASK");
			transferDataNow();
		}
	};*/
	
	public boolean transferDataNow() {
		if(mDownloader.size() == 0) return false;
		mDownloader.get(mDownloader.size() - 1).requestTransfer();
		/*if (timer == null) {
			timer = new Timer(true);
		}
*/
		//Log.e("Timer", "Timer schedule");
		/*OwnTimerTask task = new OwnTimerTask();
		timer.schedule(task, 10000);*/
		return true;
	}

	@Override
	public void isDone(Downloader downloader) {
		if(DEBUG) Log.d(TAG, "is done: " + mDownloader.indexOf(downloader));
		mDownloader.remove(downloader);
	}
}

class Downloader implements Runnable{
	private int status = 0;
	private boolean isInterrupted = false;
	private DownloaderListener mDownloaderListener;
	
	private Context mContext;
	private StreamDownloaderListener mStreamDownloaderListener;
	private String downloadUrl;
	private String cookies;
	
	private int TIMEOUT = 10000;
	private int downloadBufferSize = 16384;
	private int totalBytesRead;
	
	private int downloader_id;
	private String DOWNLOAD_CACHE_FILE = "downloadcache";
	private HttpURLConnection connect;
	private File downloadFile;
	private int totalSize = 0;
	
	Downloader(Context context, StreamDownloaderListener streamDownloaderListener, DownloaderListener downloaderListener){
		mContext = context;
		mStreamDownloaderListener = streamDownloaderListener;
		mDownloaderListener = downloaderListener;
		downloader_id = (int) (Math.random() * Integer.MAX_VALUE);
		
		// set cache file
		downloadFile = new File(mContext.getCacheDir(), DOWNLOAD_CACHE_FILE + downloader_id);
		if (downloadFile.exists()) {
			downloadFile.delete();
		}
	}
	
	public void requestTransfer() {
		if(!isInterrupted) mStreamDownloaderListener.downloadUpdate(totalBytesRead / 1024, downloadFile, true);
	}

	public void setTimeout(int timeout) {
		TIMEOUT = timeout;
	}

	public void setUrl(String url, String cookie){
		downloadUrl = url;
		cookies = cookie;
		isInterrupted = false;
	}
	
	public void stop(){
		isInterrupted = true;
	}
	
	@Override
	public void run() {
		InputStream in = request();
		if (in != null && !isInterrupted) {
			setStatus(StreamDownloader.REQUEST_SUCCESS);
			boolean ret = download(in);
			if(status == StreamDownloader.REQUEST_SUCCESS){
				if(ret){
					setStatus(StreamDownloader.DOWNLOAD_SUCCESS);
					if(!isInterrupted) mStreamDownloaderListener.downloadSuccess(downloadFile);
				}else{
					setStatus(StreamDownloader.DOWNLOAD_FAILURE);
				}
			}
		} else {
			if(!isInterrupted){
				setStatus(StreamDownloader.REQUEST_FAILURE);
			}
		}
		// remove cache file
		/*
		if (downloadFile.exists()) {
			downloadFile.delete();
		}
		*/
		mDownloaderListener.isDone(this);
	}
	
	private boolean download(InputStream in) {
		FileOutputStream out;
		boolean ret = true;
		try {
			// output to cache file
			out = new FileOutputStream(downloadFile);
			byte buf[] = new byte[downloadBufferSize];
			totalBytesRead = 0;
			do {
				int numread = in.read(buf);
				if (numread <= 0) {
					if(StreamDownloader.DEBUG) Log.d(StreamDownloader.TAG, "numread: " + numread);
					break;
				}
				out.write(buf, 0, numread);
				totalBytesRead += numread;
				int totalKbRead = totalBytesRead / 1024;
				if(!isInterrupted) 
				{
					mStreamDownloaderListener.downloadUpdate(totalBytesRead / 1024, downloadFile, false);
					mStreamDownloaderListener.setDownloadTotalSize(totalSize);//设置下载文件的大小
				}
				
				
				if(StreamDownloader.DEBUG) Log.d(getClass().getName(), "totalKbRead: " + totalKbRead);
			} while (!isInterrupted);
			in.close();

			if (isInterrupted) {
				if(StreamDownloader.DEBUG) Log.d(StreamDownloader.TAG, "is Interrupted.");
				setStatus(StreamDownloader.DOWNLOAD_STOP);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
	
	

	private InputStream request() {
		InputStream inStream;
		try {
			URL rul = new URL(downloadUrl);
			connect = (HttpURLConnection) rul.openConnection();
			if(cookies != null){
				connect.setRequestProperty("Cookie", cookies);
			}
			connect.setRequestMethod("GET");
			connect.setDoOutput(true);
			connect.setReadTimeout(TIMEOUT);
			connect.connect();
			totalSize = connect.getContentLength();//取得下载文件的长度
			int result = connect.getResponseCode();
			if (result == HttpURLConnection.HTTP_OK){
				inStream = new BufferedInputStream(connect.getInputStream());
			} else {
				inStream = null;
			}
			return inStream;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setStatus(int s){
		status = s;
		if(!isInterrupted) mStreamDownloaderListener.statusUpdate(status);
		if(StreamDownloader.DEBUG) Log.d(StreamDownloader.TAG, "now status: " + status);
	}	
}

interface DownloaderListener{
	void isDone(Downloader downloader);
}
