package com.viash.voicelib.utils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageDownloadTaskUtil extends AsyncTask<Object, Object, Bitmap> {
	private ImageView imageView = null;
	Drawable defaultImage =null;
	@Override
	protected Bitmap doInBackground(Object... params) {
		imageView = (ImageView) params[1];
		if(params.length > 2)
		{
			if(params[2] !=null)
				defaultImage = (Drawable)params[2];
		}
		Bitmap bmp = null;
		
		if (ImageCacheDataUtil.mImageCache.containsKey((String) params[0])) {
			SoftReference<Bitmap> reference = ImageCacheDataUtil.mImageCache
					.get((String) params[0]);
			bmp = reference.get();
		}
		if (null == bmp) {
			try {
				bmp = BitmapFactory.decodeStream(new URL((String) params[0])
						.openStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (null != bmp) {
				ImageCacheDataUtil.mImageCache.put((String) params[0],
						new SoftReference<Bitmap>(bmp)); // 保存到内存
			}
		}

		return bmp;
	}

	protected void onPostExecute(Bitmap result) {
		if(result !=null)
			imageView.setImageBitmap(result);
		else{
			if(defaultImage != null)
			imageView.setImageDrawable(defaultImage);
		}
	}
	
}
