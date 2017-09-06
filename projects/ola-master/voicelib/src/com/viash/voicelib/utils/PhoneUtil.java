package com.viash.voicelib.utils;

import android.os.Environment;

public class PhoneUtil {
	/**
	 * 判断是否有SD卡或SD卡是否可用。
	 * 
	 * @return
	 */
	public static  boolean sdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
}
