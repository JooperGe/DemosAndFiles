package com.viash.voicelib.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

/**
 * 图片缓存类，不能实例化。
 * @author Harlan Song
 * @createDate 2013-1-18
 */
public class ImageCacheDataUtil {
	public static HashMap<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();
	
}
