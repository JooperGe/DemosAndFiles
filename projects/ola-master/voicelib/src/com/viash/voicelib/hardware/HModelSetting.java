package com.viash.voicelib.hardware;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;

/**
 * 手机情景模式设置
 * 
 * @author Harlan
 * @createDate 2012-12-10
 */
public class HModelSetting {
	

	/**
	 * 关闭声音
	 */
	public static void closeRing(Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		
	}

	/**
	 * 打开声音
	 */
	public static void openRing(Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}




	/**
	 * 打开振动
	 * @param context
	 */
	public static void openVibrate(Context context) {
		/*AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_ON);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_ON);*/
		AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	}
	
	/**
	 * 关闭振动
	 * @param context
	 */
	public static void closeVibrate(Context context) {
		/*AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_OFF);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_OFF);*/
		AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	/**
	 * 添加音量
	 */
	public static void addVolume(Context context) {
		AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audio.adjustVolume(AudioManager.ADJUST_RAISE, 0);
	}

	/**
	 * 减小音量
	 */
	public static void minishVolume(Context context) {
		AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		audio.adjustVolume(AudioManager.ADJUST_LOWER, 0);
	}

}
