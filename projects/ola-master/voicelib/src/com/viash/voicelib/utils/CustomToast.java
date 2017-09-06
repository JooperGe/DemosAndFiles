package com.viash.voicelib.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 自定义Toast
 * @author Harlan
 * @createDate 2012-12-14
 */
public class CustomToast {
	//Context mContext;
	private static boolean isToastAvailable = false; 

	/**
	 * 信息提示
	 * 
	 * @param context
	 * @param content
	 */ 
	
	public static void setToastAvailable(boolean value) {
		isToastAvailable = value;
	}
	
	public static void makeToast(Context context, String content) {
		if (isToastAvailable) {
			Toast.makeText(context, content, Toast.LENGTH_LONG).show();
		}
	}

	public static void showShortText(Context context, int resId) {
		if (isToastAvailable) {
			try {
				Toast.makeText(context, context.getString(resId),Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				LogOutput.e(e.getMessage());
			}
		}
	}
	public static void showShortText(Context context, CharSequence text) {
		if (isToastAvailable) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void showLongText(Context context, int resId) {
		if (isToastAvailable) {
			try {
				Toast.makeText(context, context.getString(resId), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				LogOutput.e(e.getMessage());
			}
		}
	}

	public static void showLongText(Context context, CharSequence text) {
		if (isToastAvailable) {
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		}
	}
}
