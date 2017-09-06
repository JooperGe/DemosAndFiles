package com.viash.voice_assistant.media;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

/*
 * Usage 1:
 * 		StreamMediaPlayer streamMediaPlayer;
 * 		streamMediaPlayer = new StreamMediaPlayer(getApplicationContext(), handler, file_url, cookie);
 * 		new Thread(streamMediaPlayer).start();
 * 
 *  Usage 2:
 *  	StreamMediaPlayerListener smpListener = new StreamMediaPlayerListener(){
 *  		@Override
 *  		public void statusUpdate(int status) {
 *  
 *  		}
 *  	};
 * 		StreamMediaPlayer streamMediaPlayer;
 * 		streamMediaPlayer = new StreamMediaPlayer(getApplicationContext(), smpListener, file_url, cookie);
 * 		new Thread(streamMediaPlayer).start();
 */
public class StreamMediaPlayer implements Runnable {
	public static final String TAG = "StreamMediaPlayer";
	public static final boolean DEBUG = false;
	
	public static final int REQUEST_SUCCESS = 1;
	public static final int DOWNLOAD_SUCCESS = 2;
	public static final int REQUEST_FAILURE = -1;
	public static final int DOWNLOAD_FAILURE = -2;
	public static final int PLAY_COMPLETE = 3;
	public static final int PLAY_ERROR = -3;
	
	private static final int PLAY_MIN_LIMIT = 1000;
	private static final int TRANSFER_TO_PLAYER = 5000;
	private static final String DOWNLOAD_CACHE_FILE = "downloadcachefile.dat";
	private static final String MEDIA_BUFFER_FILE = "mediabufferfile.dat";
	
	private HttpURLConnection connect;
	
	private MediaPlayer mediaPlayer;
	private File downloadingMediaFile;
	private FileOutputStream out;

	/*
	 * Status:
	 * 		0: init
	 * 			-1(): request fail
	 * 		1(REQUEST_SUCCESS): request success, start to download and playing
	 * 			-2: download fail
	 * 		2(DOWNLOAD_SUCCESS): download success
	 * 			-3: play error
	 * 		3(PLAY_COMPLETE): playing success
	 */
	private int status = 0;
	private boolean isInterrupted;
	private int totalKbRead;
	private int counter = 0;
	
	private final Handler handler = new Handler();
	private StreamMediaPlayerListener mStreamMediaPlayerListener = null;
	private Handler mHandler = null;
	private Context mContext = null;
	private String reqUrl = null;
	private String cookies = null;
	public StreamMediaPlayer(Context context, Handler h, String url, String cookie){
		mContext = context;
		mHandler = h;
		reqUrl = url;
		cookies = cookie;
	}
	public StreamMediaPlayer(Context context, Handler h, String url){
		mContext = context;
		mHandler = h;
		reqUrl = url;
	}
	public StreamMediaPlayer(Context context, StreamMediaPlayerListener streamMediaPlayerListener, String url, String cookie){
		mContext = context;
		mStreamMediaPlayerListener = streamMediaPlayerListener;
		reqUrl = url;
		cookies = cookie;
	}
	public StreamMediaPlayer(Context context, StreamMediaPlayerListener streamMediaPlayerListener, String url){
		mContext = context;
		mStreamMediaPlayerListener = streamMediaPlayerListener;
		reqUrl = url;
	}

	public void stop(){
		isInterrupted = true;
	}
	
	@Override
	public void run() {
		if((mHandler == null && mStreamMediaPlayerListener == null) || mContext == null || reqUrl == null){
			return;
		}
		
		status = 0;
		InputStream in = request();
		if (in != null) {
			setStatus(REQUEST_SUCCESS);
			boolean ret = download(in);
			if(status == REQUEST_SUCCESS){
				if(ret){
					setStatus(DOWNLOAD_SUCCESS);
				}else{
					setStatus(DOWNLOAD_FAILURE);
				}
			}
		} else {
			setStatus(REQUEST_FAILURE);
		}
	}
	
	private void setStatus(int s){
		status = s;
		if(mHandler != null){
			mHandler.sendEmptyMessage(status);
		}else{
			mStreamMediaPlayerListener.statusUpdate(status);
		}
		if(DEBUG) Log.d(TAG, "now status: " + status);
	}
	
	private boolean download(InputStream in) {
		boolean ret = true;
		try {
			// set cache file
			downloadingMediaFile = new File(mContext.getCacheDir(), DOWNLOAD_CACHE_FILE);
			if (downloadingMediaFile.exists()) {
				downloadingMediaFile.delete();
			}
			
			// output to cache file
			out = new FileOutputStream(downloadingMediaFile);
			byte buf[] = new byte[16384];
			int totalBytesRead = 0;
			do {
				int numread = in.read(buf);
				if (numread <= 0) {
					if(DEBUG) Log.d(TAG, "numread: " + numread);
					break;
				}
				out.write(buf, 0, numread);
				totalBytesRead += numread;
				totalKbRead = totalBytesRead / 1024;
				testMediaBuffer();
				
				if(DEBUG){
					if(mediaPlayer != null){
						Log.d(getClass().getName(), "totalKbRead: " + totalKbRead + ", meida: " + mediaPlayer.getCurrentPosition() + "/" + mediaPlayer.getDuration());
					}else{
						Log.d(getClass().getName(), "totalKbRead: " + totalKbRead);
					}
				}
			} while (validateNotInterrupted() && status == REQUEST_SUCCESS);
			in.close();

			if (validateNotInterrupted()) {
				fireDataFullyLoaded();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
	
	private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				transferBufferToMediaPlayer();
				downloadingMediaFile.delete();
			}
		};
		handler.post(updater);
	}
	
	private void testMediaBuffer() {
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				if (mediaPlayer == null) {
					// if totalKbRead >= 32kb, then start to play 
					if (totalKbRead >= 32) {
						try {
							startMediaPlayer();
						} catch (Exception e) {
							Log.e(getClass().getName(), "Error copying buffered content.", e);
						}
					}
				}else if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= TRANSFER_TO_PLAYER) {
					transferBufferToMediaPlayer();
				}
			}
		};
		handler.post(updater);
	}
	
	private void startMediaPlayer() {
		try {
			File bufferedFile = new File(mContext.getCacheDir(), MEDIA_BUFFER_FILE + (counter++));
			moveFile(downloadingMediaFile, bufferedFile);
			if(DEBUG) Log.i(getClass().getName(), "Buffered File Path:" + bufferedFile.getAbsolutePath());
			if(DEBUG) Log.i(getClass().getName(), "Buffered File Length:" + bufferedFile.length() + "");
			mediaPlayer = createMediaPlayer(bufferedFile);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.start();
		} catch (IOException e) {
			Log.e(getClass().getName(), "Error initializing the MediaPlayer", e);
		}
	}
	
	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
		MediaPlayer mPlayer = new MediaPlayer();
		mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "Error in MediaPlayer:(" + what + ")with extra(" + extra + ")");
				setStatus(PLAY_ERROR);
				return false;
			}
		});
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(status == DOWNLOAD_FAILURE){
					
				}else if(status == DOWNLOAD_SUCCESS){
					setStatus(PLAY_COMPLETE);
				}
			}
		});
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();
		fis.close();
		return mPlayer;
	}

	private void transferBufferToMediaPlayer() {
		try {
			int curPosition = mediaPlayer.getCurrentPosition();
			File oldBufferedFile = new File(mContext.getCacheDir(), MEDIA_BUFFER_FILE + counter);
			File bufferedFile = new File(mContext.getCacheDir(), MEDIA_BUFFER_FILE + (counter++));
			bufferedFile.deleteOnExit();
			moveFile(downloadingMediaFile, bufferedFile);
			mediaPlayer.pause();
			mediaPlayer = createMediaPlayer(bufferedFile);
			mediaPlayer.seekTo(curPosition);
			boolean needWait = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= PLAY_MIN_LIMIT;
			if (!needWait || status == DOWNLOAD_SUCCESS) {
				mediaPlayer.start();
			}
			oldBufferedFile.delete();
		} catch (Exception e) {
			Log.e(getClass().getName(), "Error updating to newly loaded content", e);
		}
	}
	
	public void moveFile(File oldLocation, File newLocation) throws IOException {
		if (oldLocation.exists()) {
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));
			try {
				byte buff[] = new byte[8192];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
				}
			} catch (IOException e) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException e) {
					Log.e(getClass().getName(), "Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
				}
			}
		} else {
			throw new IOException( "Old location does not exist when transferring" + oldLocation.getPath() + " to " + newLocation.getPath());
		}
	}
	
	private InputStream request() {
		InputStream inStream;
		try {
			URL rul = new URL(reqUrl);
			connect = (HttpURLConnection) rul.openConnection();
			if(cookies != null){
				connect.setRequestProperty("Cookie", cookies);
			}
			connect.setRequestMethod("GET");
			connect.setDoOutput(true);
			connect.setReadTimeout(10000);
			connect.connect();
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
		}
		return null;
	}
	
	private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
			return false;
		} else {
			return true;
		}
	}
}
