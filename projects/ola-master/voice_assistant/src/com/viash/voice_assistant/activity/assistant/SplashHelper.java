package com.viash.voice_assistant.activity.assistant;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CommunicationGetPageUtil;

/**
 * 拆分 主 activity 功能
 * 
 * @author fenglei
 *
 */
public class SplashHelper {

	private static final boolean DEBUG = true;
	private static final String TAG = "InitHelper";

	private NewAssistActivity mainActivity;
	private static SplashHelper _instance = null;

	private SplashHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static SplashHelper init(NewAssistActivity main) {
		if (null == _instance)
			_instance = new SplashHelper(main);
		return _instance;
	}

	public static SplashHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init SplashHelper");

		return _instance;
	}

	public void openSplash() {
		if ((mainActivity.view_welcome != null)
				&& (mainActivity.guideDisplayDelay != GuideActivity.GUIDE_DISPLAY_DELAY)) {
			if (VoiceAssistantService.mServerState != MsgConst.STATE_SERVER_CONNECTED) {
				/*
				 * if(isBaidu) {
				 * view_welcome.setBackgroundDrawable(this.getResources
				 * ().getDrawable(R.drawable.welcome_baidu)); }
				 */
				try {
					CommunicationGetPageUtil communicationGetPageUtil = new CommunicationGetPageUtil(
							mainActivity, null);
					mainActivity.welcomePageBitmap = communicationGetPageUtil
							.isNeedShowWelcomePage(mainActivity);
					if (mainActivity.welcomePageBitmap != null) {
						mainActivity.welcomePageDrawable = new BitmapDrawable(
								mainActivity.welcomePageBitmap);
						mainActivity.view_welcome
								.setBackgroundDrawable(mainActivity.welcomePageDrawable);
					} else if (mainActivity.isBaidu) {
						mainActivity.view_welcome
								.setBackgroundDrawable(mainActivity
										.getResources().getDrawable(
												R.drawable.welcome_baidu));
					}
					communicationGetPageUtil = null;
				} catch (Exception e) {
					e.printStackTrace();
					if (mainActivity.isBaidu) {
						mainActivity.view_welcome
								.setBackgroundDrawable(mainActivity
										.getResources().getDrawable(
												R.drawable.welcome_baidu));
					}
				}
				mainActivity.view_welcome.setVisibility(View.VISIBLE);
			}
		}
	}

	public void closeSplash() {
		if ((mainActivity.view_welcome != null)
				&& (mainActivity.guideDisplayDelay != GuideActivity.GUIDE_DISPLAY_DELAY)) {
			try {
				Thread.sleep(700);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainActivity.view_welcome.setVisibility(View.GONE);
			if (mainActivity.welcomePageBitmap != null) {
				mainActivity.welcomePageBitmap.recycle();
				mainActivity.welcomePageBitmap = null;
			}
			if (mainActivity.welcomePageDrawable != null) {
				mainActivity.welcomePageDrawable = null;
			}
		}
	}

}
