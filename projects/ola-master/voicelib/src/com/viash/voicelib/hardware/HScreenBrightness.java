package com.viash.voicelib.hardware;
import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;

import com.viash.voicelib.R;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.LogOutput;

/**
 * 屏幕亮度相关操作
 * 
 * @author Harlan
 * @createDate 2012-12-4
 */
public class HScreenBrightness {
	private static final String TAG ="HScreenBrightness";
	private static Activity mActivity = null;
	/**
	 * 增加亮度
	 * @param activity
	 */
	public static void addBrightness(Activity activity) {
		int normal = Settings.System.getInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		/*if (normal < 80) {
			normal = 80;
		}*/
		if(normal>=254){
			CustomToast.showShortText(activity, activity.getResources().getString(R.string.maxBright));
		}
		if (normal < 245)
			normal += 50;
		// 根据当前进度改变亮度
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, normal);
		normal = Settings.System.getInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, -1);
		WindowManager.LayoutParams wl = activity.getWindow().getAttributes();

		float tmpFloat = (float) normal / 255;
		if (tmpFloat > 0 && tmpFloat <= 1) {
			wl.screenBrightness = tmpFloat;
		}
		activity.getWindow().setAttributes(wl);
		saveBrightness(activity.getContentResolver(), normal);
	}

	/**
	 * 降低亮度
	 * @param activity
	 */
	public static void minusBrightness(Activity activity) {

		int normal = Settings.System.getInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		if(normal==0){
			CustomToast.showShortText(activity, activity.getResources().getString(R.string.minBright));
		}
	
		if (normal < 50) {
			normal = 0;
		}
		else {
			normal -= 50;
		}
		// 根据当前进度改变亮度
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, normal);
		normal = Settings.System.getInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, -1);
		WindowManager.LayoutParams wl = activity.getWindow().getAttributes();

		float tmpFloat = (float) normal / 255;
		if (tmpFloat > 0 && tmpFloat <= 1) {
			wl.screenBrightness = tmpFloat;
		}
		if (tmpFloat == 0) {
			wl.screenBrightness = (float) 0.01;
		}
		activity.getWindow().setAttributes(wl);
		saveBrightness(activity.getContentResolver(), normal);
	}

	/**
	 * 保存亮度设置状态
	 * @param resolver
	 * @param brightness
	 */
	public static void saveBrightness(ContentResolver resolver, int brightness) {
		Uri uri = android.provider.Settings.System
				.getUriFor("screen_brightness");
		android.provider.Settings.System.putInt(resolver, "screen_brightness",
				brightness);
		// resolver.registerContentObserver(uri, true, myContentObserver);
		resolver.notifyChange(uri, null);
	}
	
	public static void setMaxBrightness(Activity activity){
		setParcentBrightness(activity, 100);
//		saveBrightness(context.getContentResolver(),225);
	}
	
	public static void setMinBrightness(Activity activity){
		setParcentBrightness(activity, 0);
//		saveBrightness(context.getContentResolver(),0);
	}
	
	/**
	 * 
	 * @param activity
	 * @param parcent 0~100
	 */
	public static void setParcentBrightness(Activity activity,int parcent){
		if(parcent >=0 && parcent <=100){
			int value = 0;
			if(parcent != 0)
				value =(int)((double)(255 * parcent)) / 100 ;
			else
				value = parcent;
			Settings.System.putInt(activity.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, value);
			value = Settings.System.getInt(activity.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, -1);
			WindowManager.LayoutParams wl = activity.getWindow().getAttributes();

			float tmpFloat = (float) value / 255;
			if (tmpFloat > 0 && tmpFloat <= 1) {
				wl.screenBrightness = tmpFloat;
			}
			if (tmpFloat == 0) {
				wl.screenBrightness = (float) 0.01;
			}
			activity.getWindow().setAttributes(wl);
			saveBrightness(activity.getContentResolver(),value);
		}else{
			LogOutput.w(TAG, "parcent:" + parcent);
		}
	}
	
	public static void setActivity(Activity activity)
	{
		mActivity = activity;
	}
	
	public static void addBrightness() {
		if(mActivity != null)
			addBrightness(mActivity);
	}
	
	public static void minusBrightness() {
		if(mActivity != null)
			minusBrightness(mActivity);
	}
	
	public static void setParcentBrightness(int parcent){
		if(mActivity != null)
			setParcentBrightness(mActivity, parcent);
	}
}
