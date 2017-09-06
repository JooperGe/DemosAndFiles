package com.viash.voicelib.utils;

import android.database.Cursor;
import android.util.Log;

public class ProviderUtil {
	private static final String TAG = "ProviderUtil";
	public static void showCursor(Cursor cursor)
	{
		for(int i = 0; i < cursor.getColumnCount(); i++)
		{
			Log.e(TAG, cursor.getColumnName(i) + ":" + cursor.getString(i));
		}
	}
}
