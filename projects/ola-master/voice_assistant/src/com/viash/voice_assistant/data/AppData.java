package com.viash.voice_assistant.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.viash.voice_assistant.R;


public class AppData {
	private static final String AppShare = "aola";
	private static final String APP_VERSION = "appVersion";
	private static final String NICKNAME = "nickname";
	private static final String IS_NICKNAME_SENDTO_SERVER = "is_nickname_sendto_server";
	
	public static int getLastAppVersion(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		int appVersion = sp.getInt(APP_VERSION,0);
		return appVersion;
	}
	
	public static void saveCurrentAppVersion(Context context, int appVersion) {
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putInt(APP_VERSION, appVersion);
		editor.commit();
	}
	
	public static boolean isAppVersionUpdated(Context context, int currentAppVersion){
		if(getLastAppVersion(context) == currentAppVersion){
			return false;
		}else{
			return true;
		}
	}
	
	public static String getNickname(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		String nickname = sp.getString(NICKNAME, context.getResources().getString(R.string.setting_default_usercall));
		return nickname;
	}
	
	public static void setNickname(Context context, String nickname) {
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putString(NICKNAME, nickname);
		editor.commit();
	}
	
	public static boolean getIsNicknameSendToServer(Context context){
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_READABLE);
		boolean nicknameFlag = sp.getBoolean(IS_NICKNAME_SENDTO_SERVER, false);
		return nicknameFlag;
	}
	
	public static void setIsNicknameSendToServer(Context context, boolean nicknameFlag) {
		SharedPreferences sp = context.getSharedPreferences(AppShare,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putBoolean(IS_NICKNAME_SENDTO_SERVER, nicknameFlag);
		editor.commit();
	}
	
}
