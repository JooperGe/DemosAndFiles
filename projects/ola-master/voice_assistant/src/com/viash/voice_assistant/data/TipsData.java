package com.viash.voice_assistant.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TipsData {
	private static final String TIPS_PREF = "tipsPref";

	public static long getLastDate(Context context, String key) {
		long date = 0;
		SharedPreferences sp = context.getSharedPreferences(TIPS_PREF,Context.MODE_WORLD_READABLE);
		date = sp.getLong(key, 0);
		return date;
	}
	
	public static void setCurrentDate(Context context, String key, long date) {
		SharedPreferences sp = context.getSharedPreferences(TIPS_PREF,Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putLong(key, date);
		editor.commit();
	}
}
