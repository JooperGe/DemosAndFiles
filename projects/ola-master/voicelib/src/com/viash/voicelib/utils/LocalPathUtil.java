package com.viash.voicelib.utils;

import android.os.Environment;

public class LocalPathUtil {
	public static String ROOT = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/voice_assist/";
	public static String CACHE_MUSIC = ROOT + "cache/music/";
	public static String IMAGE_MUSIC = ROOT + "image/music/";
	public static String CACHE_IMG_SHOPPING = ROOT +"cache/shoping/";
	public static String IMAGE_APP_LOGO = ROOT +"image/app/";

}
