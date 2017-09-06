package com.viash.voicelib.hardware;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * 手机震动相关操作
 * @author Harlan
 * @createDate 2012-12-4
 */
public class HVibrator {
	private Vibrator vibrator=null;
	public HVibrator(Context context){
		 vibrator=(Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
	}
	
	/**
	 * 开始震动，
	 * @param milliseconds 震动的时间。 
	 */
	public void startVibrator(long milliseconds){
		vibrator.vibrate(milliseconds);
	}
	
	public void stopVibrator(){
		 vibrator.cancel();
	}
	
	public static void enableVibrator(boolean enable)
	{
		
	}


}
