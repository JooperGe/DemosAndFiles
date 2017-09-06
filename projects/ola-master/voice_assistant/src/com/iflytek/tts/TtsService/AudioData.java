package com.iflytek.tts.TtsService;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioData {
	private static AudioTrack mAudio = null;
	private static final String TAG = "TtsService(audio)";
	private static int mStreamType = AudioManager.STREAM_MUSIC;
	private static int mSampleRate = 16000;
	private static int mBuffSize = 8000; 
	private static boolean mPlaying = false;
		
	static {
		//int minSize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10;
		//if(minSize > 64 * 1024)
		//	minSize = 64 * 1024;
		int minSize = mBuffSize;
		mAudio = new AudioTrack(mStreamType
				,mSampleRate,AudioFormat.CHANNEL_CONFIGURATION_MONO 
				,AudioFormat.ENCODING_PCM_16BIT
				,minSize * 2,AudioTrack.MODE_STREAM );		
		Log.d(TAG," AudioTrack create ok");
	}
	
	/**
	 * For C call 
	 */
	public static  void onJniOutData(int len,byte [] data){	
		
			if (null == mAudio){
				Log.e(TAG," mAudio null");
				return;
			}
			if (mAudio.getState() != AudioTrack.STATE_INITIALIZED ){
				Log.e(TAG," mAudio STATE_INITIALIZED");
				return;
			}
			
			if(len > 0)
			{
				try{
					mAudio.write(data, 0, len);	
					if(!mPlaying)
					{
						mAudio.play();
						mPlaying = true;
					}
				}catch (Exception e){
					Log.e(TAG,e.toString());
				}
			}
	}
	
	/**
	 * For C Watch Call back
	 * @param nProcBegin
	 */
	public static void onJniWatchCB(int nProcBegin){
		Log.d(TAG,"onJniWatchCB  process begin = " + nProcBegin);

	}
	
	public static void close(){
		if(mAudio == null ){
			return;
		}
		if(mPlaying)
		{
			mPlaying = false;
			mAudio.stop();
		}
	}
	
}


