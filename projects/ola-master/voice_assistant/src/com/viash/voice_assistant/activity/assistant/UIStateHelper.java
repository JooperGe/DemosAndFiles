package com.viash.voice_assistant.activity.assistant;

import android.util.Log;
import android.view.View;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voicelib.msg.MsgConst;

/**
 * 拆分主activity 功能
 * 
 * 主ui状态管理
 * @author fenglei
 *
 */
public class UIStateHelper {
	private static final boolean DEBUG = true;
	private static final String TAG = "UIStateHelper";
	
	private NewAssistActivity mainActivity;
	private static UIStateHelper _instance = null;

	private UIStateHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static UIStateHelper init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new UIStateHelper(main);
		return _instance;
	}

	public static UIStateHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init UIStateHelper");

		return _instance;
	}
	
	public void setProcessingState(int state) {

		Log.i("notifyClientState", "aa" + state);
		mainActivity.mProcessingState = state;

		if (VoiceAssistantService.mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED
				&& !(com.viash.voice_assistant.common.Config.WHICH_SERVER
						.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))) {
			// mBtnSendText.setEnabled(true);
			mainActivity.mBtnSwitchToText.setEnabled(true);
			mainActivity.mBtnSwitchToVoice.setEnabled(true);
			mainActivity.mBtnSendText.setEnabled(true);
			mainActivity.btn_voiceSpeak.setEnabled(true);

			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			mainActivity.imgv_voice_volume.setVisibility(View.INVISIBLE);
			mainActivity.rotateview.stopRotate();
			mainActivity.rotateview.setVisibility(View.GONE);

			mainActivity.initVoiceView();
			// voiceView.init(); // Clear animation if server is disconnected.
		} else {
			switch (state) {
			case MsgConst.UI_STATE_UNINIT:
				// clearTipsEvent(false);
				mainActivity.mBtnSendText.setEnabled(false);
				mainActivity.btn_voiceSpeak.setEnabled(false);
				mainActivity.mBtnSwitchToText.setEnabled(false);
				mainActivity.btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
				break;
			case MsgConst.UI_STATE_INITED:
				Log.i(TAG, "UI_STATE_INITED");
				// init state is OK
				// clearTipsEvent(false);
				// startTipsEvent();
				if (mainActivity.isServerResponsed()) {
					mainActivity.mBtnSendText.setEnabled(true);
					mainActivity.btn_voiceSpeak.setEnabled(true);
					mainActivity.initVoiceView();
				} else {
					mainActivity.mBtnSendText.setEnabled(false);
					mainActivity.btn_voiceSpeak.setEnabled(false);
				}
				mainActivity.mBtnSwitchToText.setEnabled(true);
				mainActivity.mBtnSwitchToVoice.setEnabled(true);
				break;
			case MsgConst.UI_STATE_SPEAKING:
				// Start speaking
				Log.i(TAG, "UI_STATE_SPEAKING");
				// clearTipsEvent(true);
				mainActivity.mBtnSendText.setEnabled(false);
				mainActivity.mBtnSwitchToVoice.setEnabled(false);
				// mMusicPlayerView.speakPause();
				// voiceView.startSpeak();
				mainActivity.btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_pressed);
				mainActivity.imgv_voice_volume.setVisibility(View.VISIBLE);
				break;
			case MsgConst.UI_STATE_RECOGNIZING:
				// start to recogine
				// clearTipsEvent(true);
				// mMusicPlayerView.speakRecoer();
				mainActivity.mBtnSendText.setEnabled(false);
				mainActivity.btn_voiceSpeak.setEnabled(false);
				mainActivity.mBtnSwitchToVoice.setEnabled(false);
				// voiceView.startLoading();
				mainActivity.btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_loading);
				mainActivity.rotateview.setVisibility(View.VISIBLE);
				mainActivity.imgv_voice_volume.setVisibility(View.GONE);
				mainActivity.rotateview.startRotate();
				break;
			}
		}
	}
	
	public void updateMicImage(int volume) {
		switch (volume) {
		case 1:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			break;
		case 2:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume02);
			break;
		case 3:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume03);
			break;
		case 4:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume04);
			break;
		case 5:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume05);
			break;
		case 6:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume06);
			break;
		case 7:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume07);
			break;
		case 8:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume08);
			break;
		case 9:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume09);
			break;
		case 10:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume10);
			break;
		case 11:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume11);
			break;
		case 12:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume12);
			break;
		default:
			mainActivity.imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			break;
		}
	}
	

	public void updateStatusView() {
		if (GlobalData.isUserLoggedin()) {// && isUserLoggedin) {
			mainActivity.mLayoutBeforeLogin.setVisibility(View.GONE);
			mainActivity.mLayoutLoginInfo.setVisibility(View.VISIBLE);
			String[] userInfo = UserData.getUserInfo(mainActivity);
			String username = "";
			if (userInfo.length > 0) {
				username = userInfo[0];
			}

			mainActivity.mTv_Username.setText(username);

			int resId = 0;
			int userLevel = SavedData.getmUserLevel();
			switch (userLevel) {
			case 1:
				resId = R.drawable.statusbar_bronze_1;
				break;
			case 2:
				resId = R.drawable.statusbar_bronze_2;
				break;
			case 3:
				resId = R.drawable.statusbar_bronze_3;
				break;
			case 4:
				resId = R.drawable.statusbar_bronze_4;
				break;
			case 5:
				resId = R.drawable.statusbar_silver_1;
				break;
			case 6:
				resId = R.drawable.statusbar_silver_2;
				break;
			case 7:
				resId = R.drawable.statusbar_silver_3;
				break;
			case 8:
				resId = R.drawable.statusbar_silver_4;
				break;
			case 9:
				resId = R.drawable.statusbar_gold_1;
				break;
			case 10:
				resId = R.drawable.statusbar_gold_2;
				break;
			case 11:
				resId = R.drawable.statusbar_gold_3;
				break;
			case 12:
				resId = R.drawable.statusbar_gold_4;
				break;
			default:
				resId = R.drawable.statusbar_gold_4;
				break;
			}
			if (userLevel <= 0) {
				mainActivity.mIcon_Medal.setVisibility(View.INVISIBLE);
			} else {
				mainActivity.mIcon_Medal.setVisibility(View.VISIBLE);
				mainActivity.mIcon_Medal.setImageResource(resId);
			}

			mainActivity.mTv_Score.setText(String.valueOf(SavedData.getmUserScore()));
			if (SavedData.getmUserSpecialTime() == 0) {
				mainActivity.mIcon_Arrow.setVisibility(View.GONE);
			} else {
				mainActivity.mIcon_Arrow.setVisibility(View.VISIBLE);
				if (userLevel / 4 == 0) {
					mainActivity.mIcon_Arrow.setImageResource(R.drawable.statusbar_up_1);
					mainActivity.mTv_Score.setTextColor(mainActivity.getResources().getColor(
							R.color.statusbar_score_bronze));
				} else if (userLevel / 4 == 1) {
					mainActivity.mIcon_Arrow.setImageResource(R.drawable.statusbar_up_2);
					mainActivity.mTv_Score.setTextColor(mainActivity.getResources().getColor(
							R.color.statusbar_score_silver));
				} else if (userLevel / 4 == 2) {
					mainActivity.mIcon_Arrow.setImageResource(R.drawable.statusbar_up_3);
					mainActivity.mTv_Score.setTextColor(mainActivity.getResources().getColor(
							R.color.statusbar_score_gold));
				}
			}

			long currentDate = System.currentTimeMillis();
			long expireDate = UserData.getVCodeExpireDate(mainActivity);
			if (UserData.isPhoneBinded(mainActivity)) {
				if (currentDate < expireDate) {
					mainActivity.icon_authenticate_user.setVisibility(View.GONE);
				} else {
					mainActivity.icon_authenticate_user.setVisibility(View.VISIBLE);
				}
			} else {
				mainActivity.icon_authenticate_user.setVisibility(View.GONE);
			}
		} else {
			mainActivity.mLayoutBeforeLogin.setVisibility(View.VISIBLE);
			mainActivity.mLayoutLoginInfo.setVisibility(View.GONE);
		}

	}

	
}
