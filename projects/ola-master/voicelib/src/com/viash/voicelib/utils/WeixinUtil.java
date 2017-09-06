package com.viash.voicelib.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXFileObject;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class WeixinUtil {
	private static final String TAG = "WeixinUtil";
	
	//正式app_id是:"wx8e684dd5e2295a6f";调试app_id:"wxa1151194c03010f0".
	public static final String APP_ID = "wx8e684dd5e2295a6f";
	public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	protected static IWXAPI sApi;
	public static boolean sIsWXAppInstalledAndSupported = false;
	
	private final static String SHARE_PATH = Environment.getExternalStorageDirectory() + "/voice_assist/dump/";
	private final static String IMAGE_FILE_NAME = "ola_share.png";
	
	public static void releaseWeixin()
	{

	}
	
	public static IWXAPI register2Weixin(Context context) {
		return register2Weixin(context, APP_ID);
	}
	
	public static IWXAPI register2Weixin(Context context, String app_id) {
		LogOutput.d(TAG, "register2Weixin");
		if (sApi != null && app_id == APP_ID) {
			return sApi;
		}
		sIsWXAppInstalledAndSupported = false;
		sApi = WXAPIFactory.createWXAPI(context, app_id, true);
		if (isWXAppInstalledAndSupported(context, sApi)) {
			boolean result = sApi.registerApp(APP_ID);
			if (!result) {
				LogOutput.d(TAG, "注册app失败");
				CustomToast.makeToast(context, "注册app失败");//, Toast.LENGTH_LONG).show();
			}
		}
		return sApi;
	}
	
	public static void unregister(IWXAPI api) {
		LogOutput.d(TAG, "unregister");
		api.unregisterApp();
	}
	
	public static boolean isWXAppInstalledAndSupported(Context context, IWXAPI api) {
		//LogOutput.d(TAG, "isWXAppInstalledAndSupported");
		if (sIsWXAppInstalledAndSupported) {
			return sIsWXAppInstalledAndSupported;
		}
		sIsWXAppInstalledAndSupported = api.isWXAppInstalled() && api.isWXAppSupportAPI();
		if (!sIsWXAppInstalledAndSupported) {
			LogOutput.w(TAG, "~~~~~~~~~~~~~~微信客户端未安装或版本过低，请确认");
			CustomToast.makeToast(context, "微信客户端未安装或版本过低，请确认");//, Toast.LENGTH_LONG).show();
		}

		return sIsWXAppInstalledAndSupported;
	}
	
	public static boolean isTimeLineSupportedVersion(Context context, IWXAPI api) {
		//LogOutput.d(TAG, "isTimeLineSupportedVersion");
		int wxSdkVersion = api.getWXAppSupportAPI();
		if (wxSdkVersion < TIMELINE_SUPPORTED_VERSION) {
			CustomToast.makeToast(context, "当前微信版本不支持朋友圈功能，请更新至最新");//, Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
	
	private static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		LogOutput.d(TAG, "bmpToByteArray result.length:"+result.length);
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static boolean sendText(Context context, IWXAPI api, boolean isSend2Friend, String description) {
		LogOutput.d(TAG, "sendText");
		WXTextObject textObj = new WXTextObject(description);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = description;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text");
		req.message = msg;
		req.scene = isSend2Friend ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
		api.sendReq(req);
		
		return true;
	}
	
	public static boolean sendScreenCapture(Context context, IWXAPI api, boolean isSend2Friend, Bitmap bmp, String title, String description) {
		if (isSend2Friend) {
			return sendFile(context, api, true, bmp, title, description);
		}
		else {
			return sendImage(context, api, false, bmp, title, description);
		}
	}
	
	public static boolean sendImage(Context context, IWXAPI api, boolean isSend2Friend, Bitmap bmp, String title, String description) {
		LogOutput.d(TAG, "sendImage");
		WXImageObject imgObj = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
		bmp.recycle();
		msg.thumbData = bmpToByteArray(thumbBmp, true);
		msg.title = title;
		msg.description = description;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = isSend2Friend ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
		api.sendReq(req);
		
		return true;
	}
	
	public static boolean sendFile(Context context, IWXAPI api, boolean isSend2Friend, Bitmap bmp, String title, String description) {
		LogOutput.d(TAG, "sendFile");
		saveBitmap(bmp, IMAGE_FILE_NAME);
		File file = new File(SHARE_PATH, IMAGE_FILE_NAME);
		if (!file.exists()) {
			LogOutput.e(TAG, "bitmap is not exist, file:"+file);
			return false;
		}
		
		WXFileObject fileObj = new WXFileObject();
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = fileObj;
		
		fileObj.setFilePath(file.getAbsolutePath());

		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
		bmp.recycle();
		msg.thumbData = bmpToByteArray(thumbBmp, true);
		msg.title = title;
		msg.description = description;
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("file");
		req.message = msg;
		req.scene = isSend2Friend ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
		api.sendReq(req);
		
		return true;
	}
	
	public static boolean saveBitmap(Bitmap bitmap, String fileName) {
		File file = new File(SHARE_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
		File imageFile = new File(file, fileName);
		try {
			imageFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(imageFile);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
