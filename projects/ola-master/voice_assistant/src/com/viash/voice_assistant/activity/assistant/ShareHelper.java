package com.viash.voice_assistant.activity.assistant;

import android.content.Intent;
import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.renren.PhotoServiceActivity;
import com.viash.voicelib.utils.LogOutput;
import com.viash.voicelib.utils.WeixinUtil;


/**
 * 拆分 主activity 功能
 * 
 * @author fenglei
 *
 */
public class ShareHelper {

	private static final boolean DEBUG = true;
	private static final String TAG = "ShareHelper";
	
	private NewAssistActivity mainActivity;
	private static ShareHelper _instance = null;

	private ShareHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static ShareHelper init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new ShareHelper(main);
		return _instance;
	}

	public static ShareHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init ShareHelper");

		return _instance;
	}
	

	public boolean send2Weixin(int to, int type, String title,
			String description, Bitmap bm) {
		LogOutput.d(TAG, "send2Weixin bm");
		IWXAPI api = WeixinUtil.register2Weixin(mainActivity);

		return WeixinUtil.sendScreenCapture(mainActivity, api, false, bm, title,
				description);
	}

	public boolean send2Weixin(int to, int type, String title,
			String description, String url) {
		LogOutput.d(TAG, "send2Weixin");
		IWXAPI api = WeixinUtil.register2Weixin(mainActivity);
		boolean isToFriend = (to == 1) ? false : true;
		switch (type) {
		case 0:// Text
			WeixinUtil.sendText(mainActivity, api, isToFriend, description);
			break;
		case 1:// image
			break;
		case 2:// video
			break;
		case 3:// music
			break;
		case 4:// webpage
			break;
		case 5:// screen_capture
			Bitmap screenBmp = ScreenCaptureHelper.getInstantce().loadBitmapFromView(mainActivity.mLstView, true);
			if (screenBmp != null) {
				WeixinUtil.sendScreenCapture(mainActivity, api, isToFriend, screenBmp,
						title, description);
			}
			break;
		default:
			break;
		}

		return true;
	}

	public boolean send2Renren(int to, int type, String title,
			String description, String url) {
		LogOutput.d(TAG, "send2Renren");
		switch (type) {
		case 0:// Text
			break;
		case 1:// image
			break;
		case 2:// video
			break;
		case 3:// music
			break;
		case 4:// webpage
			break;
		case 5:// screen_capture
			ScreenCaptureHelper.getInstantce().startCaptureImage();
			Intent intent = new Intent(mainActivity, PhotoServiceActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mainActivity.startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}

}
