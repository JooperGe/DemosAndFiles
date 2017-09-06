package com.viash.voice_assistant.service.voicesdk;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.receiver.SmsReceiver;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.speech.IRecognizeListener;
import com.viash.voicelib.msg.MsgConst;


/**
 * 
 * 拆分 主 service 功能
 * 
 * @author fenglei
 *
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class BRRecognizerListener implements IRecognizeListener{
	
	private static final boolean DEBUG = true;
	private static final String TAG = "BRRecognizerListener";
	
	private VoiceSdkService mainService;
	
	public BRRecognizerListener(VoiceSdkService service){
		this.mainService = service;
	}
	
	@Override
	public void onResults(String result, String recordFileName) {
		if (IncomingCallShareState.isIncomgingCall()) {
			String[] mAnswerCallArray = mainService
					.getResources().getStringArray(
							R.array.incoming_call_answer_call_commmand);
			String[] mEndCallArray = mainService
					.getResources().getStringArray(
							R.array.incoming_call_end_call_commmand);
			String action = null;
			for (String str : mEndCallArray) {
				if (result.contains(str)) {
					action = "1";
				}
			}
			if (action == null) {
				for (String str : mAnswerCallArray) {
					if (result.contains(str)) {
						action = "0";
					}
				}
			}
			if (action != null) {
				Bundle b = new Bundle();
				b.putString("action", action);
				mainService.handleIncomingCall(b);
			} else {
				Intent intent = new Intent();
				intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING);
				mainService.sendBroadcast(intent);
			}
			return;
		}
		if (SmsReceiver.isReplySMS()) {
			String[] mSMSNoDataArr = mainService
					.getResources().getStringArray(
							R.array.sms_not_reply_command);
			String[] mSMSReplyDataArr = mainService
					.getResources().getStringArray(
							R.array.sms_reply_command);
			String[] mSMSPlayDataArr = mainService
					.getResources().getStringArray(
							R.array.sms_play_content_command);
			int replyStatus = 0; // 1, reply; 2, Play TTS, 3, not reply;
									// 0 other VR command;

			for (String str : mSMSPlayDataArr) {
				if (result.contains(str)) {
					Log.i(TAG, "contains Play TTS:  " + str);
					replyStatus = 2;
					break;
				}
			}

			if (replyStatus != 2) {
				for (String str : mSMSReplyDataArr) {
					if (result.contains(str)) {
						Log.i(TAG, "contains Reply:  " + str);
						replyStatus = 1;
						break;
					}
				}
			}

			if (replyStatus != 0) {
				for (String str : mSMSNoDataArr) {
					if (result.contains(str)) {
						Log.i(TAG, "contains Not Reply " + str);
						replyStatus = 3;
						break;
					}
				}
			}
			if (replyStatus == 1) {
				Intent intent = new Intent(mainService,
						GuideActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mainService.getApplicationContext().startActivity(intent);
				result = mSMSReplyDataArr[0];
				SavedData.setLockMessage(result);
				return;
			} else if (replyStatus == 2) {
				SmsReceiver.playSMSContent();
				return;
			} else if (replyStatus == 3) {
				if (Build.VERSION.SDK_INT >= 8 && mainService.mAudioManager != null) {
					mainService.mAudioManager
							.abandonAudioFocus(mainService.mAudioFocusChangeListener);
				}
				SmsReceiver.setReplySMS(false);
				SavedData.setLockMessage(null);
				/*
				 * if(SmsReceiver.isServiceNeedStop()) { Log.i(TAG,
				 * "Need to close the services "); stopSelf(); return; }
				 */
				mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
				return;
			} else {
				SmsReceiver.setReplySMS(false);
				SavedData.setLockMessage(null);
				/*
				 * if(SmsReceiver.isServiceNeedStop()) { Log.i(TAG,
				 * "Need to close the services "); stopSelf(); return; }
				 */
			}
		}

		Message msg = mainService.mHandler.obtainMessage(mainService.MSG_RESULTS, result);
		mainService.mHandler.sendMessage(msg);
	}

	@Override
	public void onError(int errCode) {
		Message msg = mainService.mHandler.obtainMessage(mainService.MSG_ERROR, errCode, 0);
		mainService.mHandler.sendMessage(msg);
		mainService.mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
		if (SmsReceiver.isReplySMS()) {
			SmsReceiver.setReplySMS(false);
			/*
			 * if(SmsReceiver.isServiceNeedStop()) { Log.i(TAG,
			 * "Need to close the services "); stopSelf(); return; }
			 */
		}
		if (IncomingCallShareState.isIncomgingCall()) {
			Intent intent = new Intent();
			intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD);
			intent.putExtra("startRecord",
					IncomingCallShareState.START_PLAY_TTS_WITHOUT_DELAY);
			mainService.sendBroadcast(intent);
		}
	}

	@Override
	public void onEndOfSpeech() {
		mainService.mHandler.sendEmptyMessage(mainService.MSG_END_OF_SPEECH);
	}

	@Override
	public void onBeginningOfSpeech() {
		mainService.mHandler.sendEmptyMessage(mainService.MSG_BEGIN_OF_SPEECH);
	}

	@Override
	public void onCancel() {
		mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
		mainService.mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
	}

	@Override
	public void onVolumeUpdate(int newVolume) {
		Bundle bundle = new Bundle();
		bundle.putInt("volume", newVolume);

		CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME,
				bundle);
		if (mainService.floatViewIdle != null) {
			if (!mainService.floatViewIdle.isHide())
				mainService.floatViewIdle.updateVolumeView(newVolume);
		}

	}
}
