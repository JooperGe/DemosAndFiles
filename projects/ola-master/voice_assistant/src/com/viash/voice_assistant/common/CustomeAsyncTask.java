package com.viash.voice_assistant.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CustomeAsyncTask {
	private HashMap<String, SoftReference<Drawable>> imageCache;
	String path = Environment.getExternalStorageDirectory()
			+ "/voice_assist/dingding/";
	private final static String TAG="CustomeAsyncTask";

	public CustomeAsyncTask() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}

	/***
	 * 下载图片
	 * 
	 * @param imageUrl
	 *            图片地址
	 * @param imageCallback
	 *            回调接口
	 * @return
	 */
	@SuppressLint("HandlerLeak")
	public Drawable loadDrawable(
			final String imageUrl,
			final com.viash.voice_assistant.activity.NewAssistActivity.ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}

	/***
	 * 根据URL下载图片（这里要进行判断，先去本地sd中查找，没有则根据URL下载，有则返回该drawable）
	 * 
	 * @param url
	 * @return
	 */
	public Drawable loadImageFromUrl(String imageURL) {
		URL m;
		Drawable d;
		InputStream i = null;
		BufferedInputStream bis;
		try {
			m = new URL(imageURL);
			i = (InputStream) m.getContent();
			bis = new BufferedInputStream(i);
			d = Drawable.createFromStream(bis, "src");
			saveImageToSDcard(d, imageURL);
			bis.close();
			i.close();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return d;
	}

	/**
	 * save image file
	 * 
	 * @param dw
	 * @param url
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public boolean saveImageToSDcard(Drawable dw, String url) {
		try {
			BitmapDrawable bd = (BitmapDrawable) dw;
			Bitmap bm = bd.getBitmap();
			final String fileNa = url.substring(url.lastIndexOf("/") + 1,
					url.length()).toLowerCase();
			File file = new File(path + fileNa);
			long newModifiedTime =System.currentTimeMillis(); 
			file.setLastModified(newModifiedTime);
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
			if (sdCardExist) {
				File ad = new File(path);
				if (!ad.exists()) {
					ad.mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
				}
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			Log.i(TAG, "save image faile!");
		}
		return false;
	}

	/**
	 * get image from sdcard
	 * 
	 * @param imageUrl
	 * @return
	 */
	public Drawable getDrawableFromSDcard(String imageUrl) {
		Bitmap bmpDefaultPic = null;
		// 获得文件路径
		String imageSDCardPath = path
				+ imageUrl.substring(imageUrl.lastIndexOf("/") + 1,
						imageUrl.length()).toLowerCase();
		File file = new File(imageSDCardPath);
		// 检查图片是否存在
		if (!file.exists()) {
			return null;
		}
		bmpDefaultPic = BitmapFactory.decodeFile(imageSDCardPath, null);
		if (bmpDefaultPic != null || (bmpDefaultPic != null && bmpDefaultPic.toString().length() > 3)) {
			Drawable drawable = new BitmapDrawable(bmpDefaultPic);
			return drawable;
		} else
			return null;
	}
	
	public long getDrawableUpdateTime(Drawable d){
		
		return System.currentTimeMillis();
		
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	public HashMap<String, SoftReference<Drawable>> getImageCache() {
		return imageCache;
	}

	public void setImageCache(
			HashMap<String, SoftReference<Drawable>> imageCache) {
		this.imageCache = imageCache;
	}
}