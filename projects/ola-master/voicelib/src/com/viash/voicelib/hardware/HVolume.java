package com.viash.voicelib.hardware;

import com.viash.voicelib.R;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.LogOutput;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * 音量管理
 * 
 * @author Harlan
 * @createDate 2012-12-4
 */
public class HVolume {
	
	private static final String TAG ="HVolume";
	private static int volumeType = AudioManager.STREAM_MUSIC;

	/**
	 * 设置是否为静音
	 * @param isMute (true 静音)
	 */
	public static void setMute(Context context,boolean isMute) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audioManager.setStreamMute( volumeType, isMute); // 设置是否静音
	}

	public static void addVolume(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		int maxVolume =audioManager.getStreamMaxVolume( volumeType);
		int current =audioManager.getStreamVolume( volumeType );
		if(current >= maxVolume){
			CustomToast.showShortText(context, context.getResources().getString(R.string.maxVolume));
		}
		else{
			audioManager.adjustStreamVolume(volumeType,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_ALLOW_RINGER_MODES); 
		}
		
		// 调高声音
	}
	
	public static void minusVolume(Context context) {
		// 第一个参数：声音类型
		// 第二个参数：调整音量的方向
		// 第三个参数：可选的标志位
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		int minVolume=0;
		int current =audioManager.getStreamVolume(volumeType);
		if(current<=minVolume){
			CustomToast.showShortText(context, context.getResources().getString(R.string.minVolume));
		}else{
		audioManager.adjustStreamVolume(volumeType,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_ALLOW_RINGER_MODES);// 调低声音
		}
	}
	
	/**
	 * 
	 * @param current volume 0 ~ 100
	 */
	public static void setPercentCurrentVolume(Context context,int volume){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		int current =audioManager.getStreamVolume(volumeType);
		int maxVolume =audioManager.getStreamMaxVolume(volumeType);
		Log.e(TAG, "current:" + current);
	    volume = (int)(current * (double)volume / 100.0);
	    Log.e(TAG,"volume:"+volume);
	    if(volume>maxVolume)volume=maxVolume;
	    if(volume<0)volume=0;	
	    audioManager.setStreamVolume(volumeType,volume, 0);
	
	}
	
	/**
	 * 
	 * @param volume 0 ~ 100
	 */
	public static void setPercentVolume(Context context,int volume){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		
		if(volume >=0 && volume <=100){
			int maxVolume =audioManager.getStreamMaxVolume(volumeType);
			maxVolume  =(int)(maxVolume * (double)volume / 100.0); 
			audioManager.setStreamVolume(volumeType, maxVolume, 0);
		}else{
			LogOutput.e(TAG, "volume: " + volume);
		}
	}
	
	
	
	
	
	
	public static void setVolume(Context context,int volume){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audioManager.setStreamVolume(volumeType, volume, 0);
	}
	
	public static int getVolume(Context context){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		return audioManager.getStreamVolume(volumeType);
	}
	
	
	
	/**
	 * 最小音量
	 */
	public static void setMinVolume(Context context){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audioManager.adjustStreamVolume(volumeType,
				0, AudioManager.FLAG_ALLOW_RINGER_MODES);
	}
	
	/**
	 * 最大音量
	 */
	
	public static  void setMaxVolume(Context context){
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audioManager.adjustStreamVolume(volumeType,
				audioManager.getStreamMaxVolume(volumeType), AudioManager.FLAG_SHOW_UI);
	}
	
	
	/**		
	 * @para volumeTypeValue is :
	 *      AudioManager.STREAM_VOICE_CALL  0;
			AudioManager.STREAM_SYSTEM   	1;
			AudioManager.STREAM_RING  		2;
			AudioManager.STREAM_MUSIC   	3;
			AudioManager.STREAM_ALARM  		4;
			AudioManager.STREAM_NOTIFICATION  5;
	*/
	public static void setVolumeType(int volumeTypeValue) {

		//int i = AudioManager.STREAM_VOICE_CALL;
		if (volumeTypeValue < 0 || volumeTypeValue > 5 ) {
			volumeType = AudioManager.STREAM_MUSIC;
			return;
		}
		volumeType = volumeTypeValue;
	}
	
}
