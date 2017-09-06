package com.viash.voicelib.utils;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoaderCacheUtil {
	private static HashMap<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();

	public static Bitmap loadBitmap(String imageURL) {
		Bitmap bm = null;
		if (mImageCache.containsKey(imageURL)) {
			SoftReference<Bitmap> reference = mImageCache.get(imageURL);
			bm = reference.get();
		}
		if (null == bm) {
			bm = getNetBitmap2(imageURL);
			if (null != bm) {
				mImageCache.put(imageURL, new SoftReference<Bitmap>(bm)); // 保存到内存
			}
		}

		return bm;
	}

	// 从网络下载图片,方法2,本方法下载图片可能出现返回200状态但得到的bitmap为null的情况
/*	private static Bitmap getNetBitmap(String imageURL) {
		Bitmap bitmap = null;
		try {
			// 要下载的图片的地址，
			URL url = new URL(imageURL);// 获取到路径
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();// http协议连接对象
			conn.setRequestMethod("GET");// 这里是不能乱写的，详看API方法
			conn.setConnectTimeout(9000);// 别超过10秒。
			int state = conn.getResponseCode();
			if (state == 200) {
				InputStream inputStream = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(inputStream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}*/

	// 从网络下载图片,方法2,请使用本方法下载图片,
	public static Bitmap getNetBitmap2(String imageURL) {
		Bitmap bitmap = null;
		try {
			URL url = new URL(imageURL);// 获取到路径
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();// http协议连接对象
			conn.setRequestMethod("GET");// 这里是不能乱写的，详看API方法
			conn.setConnectTimeout(9000);// 别超过10秒。
			int state = conn.getResponseCode();
			if (state == 200) {
				int length = (int) conn.getContentLength();// 获取长度
				InputStream is = conn.getInputStream();
				if (length != -1) {
					byte[] imgData = new byte[length];
					byte[] temp = new byte[512];
					int readLen = 0;
					int destPos = 0;
					while ((readLen = is.read(temp)) > 0) {
						System.arraycopy(temp, 0, imgData, destPos, readLen);
						destPos += readLen;
					}
					bitmap = BitmapFactory.decodeByteArray(imgData, 0,
							imgData.length);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
