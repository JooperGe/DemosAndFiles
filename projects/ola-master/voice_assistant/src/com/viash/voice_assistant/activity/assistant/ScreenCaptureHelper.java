package com.viash.voice_assistant.activity.assistant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.View;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;


/**
 * 拆分 主activity 功能
 * 
 * @author fenglei
 *
 */
public class ScreenCaptureHelper {
	private static final boolean DEBUG = true;
	private static final String TAG = "InitHelper";
	
	private NewAssistActivity mainActivity;
	private static ScreenCaptureHelper _instance = null;

	private ScreenCaptureHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static ScreenCaptureHelper init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new ScreenCaptureHelper(main);
		return _instance;
	}

	public static ScreenCaptureHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init ScreenCaptureHelper");

		return _instance;
	}
	
	public File startCaptureImage() {
		File file = null;
		Bitmap cachefile = loadBitmapFromView(mainActivity.mLstView, true);
		if (cachefile != null) {
			try {
				file = mainActivity.getFileStreamPath("cachefile.png");
				FileOutputStream fos = new FileOutputStream(file);
				cachefile.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				file = null;
				e.printStackTrace();
			} catch (IOException e) {
				file = null;
				e.printStackTrace();
			}
		}
		return file;
	}

	public Bitmap loadBitmapFromView(View view, boolean addWaterMark) {
		Bitmap bitmap = null;
		try {
			int width = view.getWidth();
			int height = view.getHeight();
			if (width != 0 && height != 0) {
				bitmap = Bitmap.createBitmap(width, height,
						Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);

				view.layout(0, 0, width, height);
				view.draw(canvas);

				if (addWaterMark) {
					Bitmap watermark = BitmapFactory.decodeResource(
							mainActivity.getResources(), R.drawable.ola_icon_watermark);
					int left = width - watermark.getWidth() - 1;
					int top = height - watermark.getHeight() - 1;
					if (left < 0)
						left = 0;
					if (top < 0)
						top = 0;

					canvas.drawBitmap(watermark, left, top, null);
				}
			}
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}

	public Bitmap addWatermark(Bitmap src, Bitmap watermark) {
		if (src == null) {
			Log.d(TAG, "src is null");
			return null;
		}
		if (watermark == null) {
			Log.d(TAG, "watermark is null");
			return src;
		}

		int sWid = src.getWidth();
		int sHei = src.getHeight();
		int wWid = watermark.getWidth();
		int wHei = watermark.getHeight();
		if (sWid == 0 || sHei == 0) {
			return null;
		}

		if (sWid < wWid || sHei < wHei) {
			return src;
		}

		Bitmap bitmap = Bitmap.createBitmap(sWid, sHei, Config.ARGB_8888);
		try {
			Canvas cv = new Canvas(bitmap);
			cv.drawBitmap(src, 0, 0, null);
			cv.drawBitmap(watermark, sWid - wWid - 5, sHei - wHei - 5, null);
			cv.save(Canvas.ALL_SAVE_FLAG);
			cv.restore();
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}

}
