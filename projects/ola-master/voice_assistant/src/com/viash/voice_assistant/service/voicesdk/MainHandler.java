package com.viash.voice_assistant.service.voicesdk;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.component.SendDataIndicationDialog;
import com.viash.voice_assistant.component.UpdateVersionDialog;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.MusicService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.util.UserPhoneDataUtil;
import com.viash.voice_assistant.widget.FeedBackView;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.alarm.AlarmUtil;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * 
 * 拆分主service 功能
 * 
 * 
 * @author fenglei
 *
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class MainHandler extends Handler {
	
	private VoiceSdkService mainService;
	
	public MainHandler(VoiceSdkService service){
		this.mainService = service;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgConst.MSG_SERVER_NET_BROKEN:
			CustomToast.makeToast(mainService,
					mainService.getString(R.string.connect_failed));// ,
														// Toast.LENGTH_SHORT).show();
			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_BROKEN);
			if (mainService.mProcessState != MsgConst.UI_STATE_SPEAKING)
				CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);

			/*
			 * if(IncomingCallShareState.isIncomgingCall()) { Intent
			 * intent1 = new Intent();
			 * intent1.setAction(com.viash.voicelib
			 * .data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING
			 * ); sendBroadcast(intent1); }
			 */
			if (mainService.floatViewIdle != null) {
				if (!mainService.floatViewIdle.isHide()) {
					mainService.floatViewIdle
							.showErrorString("抱歉，您没有连接到网络，或网络状态异常请查看后重新再试");
					break;
				}
			}
			break;
		case MsgConst.MSG_SERVER_DATA_ERROR:
			CustomToast.makeToast(mainService,
					mainService.getString(R.string.server_dataerror));// ,
															// Toast.LENGTH_SHORT).show();
			if (mainService.mProcessState != MsgConst.UI_STATE_SPEAKING)
				CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			if (mainService.floatViewIdle != null) {
				if (!mainService.floatViewIdle.isHide()) {
					mainService.floatViewIdle
							.showErrorString(mainService.getString(R.string.server_dataerror));
					break;
				}
			}
			break;
		case MsgConst.MSG_DATA_FROM_SERVER:
			MsgRaw msgRaw = (MsgRaw) msg.obj;
			ServerMsgProcessor.getInstance().processServerMsg(msgRaw);
			break;
		case MsgConst.MSG_SERVER_SESSION_BROKEN:
			CallServerHelper.getInstance().sendUserDataToServer();
			mainService.isControlFromWidget = true;
			if (mainService.mSendingPrompt != null && mainService.mSocketUtil != null)
				mainService.mSocketUtil.sendRawMessage(mainService.mSendingPrompt, true);

			/*
			 * if(IncomingCallShareState.isIncomgingCall()) { Intent
			 * intent1 = new Intent();
			 * intent1.setAction(com.viash.voicelib
			 * .data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING
			 * ); sendBroadcast(intent1); }
			 */
			break;
		case MsgConst.MSG_DATA_FROM_TEXT:
			mainService.mInputType = 1;
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

		case MsgConst.MSG_DATA_FROM_VOICE:
			String sVoice = (String) msg.obj;
			boolean isCurrentTypeNULL = true;
			if ((HelpStatisticsUtil.currentType != null)
					|| (HelpStatisticsUtil.helpType != null))
				isCurrentTypeNULL = false;
			if (mainService.floatViewIdle != null) {
				mainService.floatViewIdle.setRecordString(sVoice);
			}
			ServerMsgProcessor.getInstance().processVoiceMsg(sVoice);
			if (IncomingCallShareState.isIncomgingCall()) {
				Intent intent = new Intent();
				intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD);
				intent.putExtra(
						"startRecord",
						IncomingCallShareState.START_PLAY_TTS_DELAY_SECONDS);
				mainService.sendBroadcast(intent);
			}
			mainService.mInputType = 0;
			if (isCurrentTypeNULL == false) {
				Message newMsg = new Message();
				newMsg.what = MsgConst.SERVICE_ACTION_CLOSE_HELP_GUIDE_VIEW;
				CallClientHelper.getInstance().sendBackToClient(newMsg);
			}
			AlarmUtil.setIsFirstAlarm(false, mainService.getApplicationContext());
			break;
		// case MSG_UI_IDLE:
		// notifyClientState(MsgConst.UI_STATE_UNINIT);
		// break;
		case VoiceSdkService.MSG_UI_INIT:
			CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			break;
		case VoiceSdkService.MSG_UI_IN_RECORDING:
			CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_SPEAKING);
			break;
		case VoiceSdkService.MSG_UI_STOP_RECORDING:
			CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_RECOGNIZING);
			break;
		case VoiceSdkService.MSG_TTS_PLAY_END:
			if (Build.VERSION.SDK_INT >= 8 && mainService.mAudioManager != null) {
				mainService.mAudioManager.abandonAudioFocus(mainService.mAudioFocusChangeListener);
//				Log.i("abandonAudioFocus", "MSG_TTS_PLAY_END");
			}
			if (!mainService.playTts(++mainService.mTtsIndex)) {
				/*
				 * if (SavedData.getmAutoStartRecord() && mNeedAnswer )
				 * { startCapture(TTS_END_TIME); }
				 */
				// sendBackToClient(msg);
				Message newMsg = new Message();
				newMsg.what = MsgConst.SERVICE_ACTION_TTS_PLAY_END;
				if (mainService.mNeedAnswer) {
					newMsg.arg1 = 1;
				} else {
					newMsg.arg1 = 0;
				}
				CallClientHelper.getInstance().sendBackToClient(newMsg);
				if (SavedData.isVoiceWakeUpOpen()
						&& (mainService.mNeedAnswer == false)) {
					if (MusicService.isPlayingState() == false)
						mainService.mHandler.sendMessage(mainService.mHandler
								.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE));
				}
			}
			break;
		case MsgConst.SERVICE_ACTION_TTS_PLAY_END:
			if (Build.VERSION.SDK_INT >= 8 && mainService.mAudioManager != null) {
				mainService.mAudioManager
						.abandonAudioFocus(mainService.mAudioFocusChangeListener);
				// Log.i("abandonAudioFocus",
				// "SERVICE_ACTION_TTS_PLAY_END");
			}
			{
				Message newMsg = new Message();
				newMsg.copyFrom(msg);
				CallClientHelper.getInstance().sendBackToClient(newMsg);
			}
			break;
		case VoiceSdkService.MSG_USER_DATA_REFRESH:
			byte[] byData = (byte[]) msg.obj;
			int dataType = msg.arg1;
			byte[] newData;
			if (byData != null) {
				if (dataType == UserPhoneDataUtil.DATA_TYPE_APP) {
					newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
							MsgConst.TS_C_ANSWER, byData);
				} else {
					newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
							MsgConst.TS_C_PROMPT, byData);
				}
				if (mainService.mSocketUtil != null) {
					mainService.mSocketUtil.sendRawMessage(newData, false);
				}
				UserPhoneDataUtil.saveData(mainService,
						byData, dataType);
			}
			break;
		case VoiceSdkService.MSG_BEGINNING_OF_RECORD:
			if (mainService.mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
				mainService.mAudioManager.requestAudioFocus(
						mainService.mAudioFocusChangeListener,
						AudioManager.STREAM_VOICE_CALL,
						AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			}
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_IN_RECORDING);
			break;
		case VoiceSdkService.MSG_END_OF_RECORD:
			break;
		case VoiceSdkService.MSG_BEGIN_OF_SPEECH:
			/*
			 * floatViewIdle =
			 * FloatViewIdle.getInstance(VoiceSdkService.this);
			 * if(floatViewIdle != null) { if(!floatViewIdle.isHide())
			 * floatViewIdle.showOnLineVRView(); }
			 */
			break;
		case VoiceSdkService.MSG_BUFFER_RECEIVERD:
			break;
		case VoiceSdkService.MSG_END_OF_SPEECH:
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_STOP_RECORDING);
			mainService.floatViewIdle = FloatViewIdle
					.getInstance(mainService);
			if (mainService.floatViewIdle != null) {
				if (!mainService.floatViewIdle.isHide())
					mainService.floatViewIdle.updateViewSendToServer();
			}
			break;
		case VoiceSdkService.MSG_ERROR:
			if (IncomingCallShareState.isIncomgingCall()) {
				/*
				 * if(msg.arg1 == 1) { Intent intent = new Intent();
				 * intent.setAction(com.viash.voicelib.data.AppData.
				 * COM_VIASH_VOICE_ASSISTANT_START_RECORD);
				 * intent.putExtra("startRecord",
				 * IncomingCallShareState.START_PLAY_TTS_WITHOUT_DELAY);
				 * sendBroadcast(intent); }
				 */
			} else {
				String error = mainService.getResources().getString(
						R.string.voiceassistantservice_vr_error_1);
				if (msg.arg1 == SpeechRecognizer.ERR_MIC_CREATE)
					error = mainService.getResources().getString(
							R.string.voiceassistantservice_vr_error_2);

				// Toast.LENGTH_SHORT).show();

				mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
				if (mainService.floatViewIdle != null) {
					if (!mainService.floatViewIdle.isHide()) {
						if (NetWorkUtil
								.isNetConnected(mainService)) {
							mainService.floatViewIdle
									.showErrorString("有异常，请检查是否可以上网，然后大点声再试试.");
						}
						break;
					}
				}
				CustomToast.makeToast(mainService, error);
				if (SavedData.isVoiceWakeUpOpen())
					mainService.mHandler.sendMessageDelayed(
							mainService.mHandler.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE),
							500);
			}
			break;
		case VoiceSdkService.MSG_RESULTS:
			if (msg.obj != null) {
				String result = (String) msg.obj;
				mainService.mHandler.sendMessageDelayed(mainService.mHandler.obtainMessage(
						MsgConst.MSG_DATA_FROM_VOICE, result), 0);
			}

			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
			break;
		case MsgConst.MSG_BLUETOOTH_FOUND_START:
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_SEARCHING_START);
			break;
		case MsgConst.MSG_BLUETOOTH_FOUND:
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_SEARCHING_FOUND);
			List<?> list = (List<?>) msg.obj;
			mainService.sendBluetoothList(list);
			break;
		case MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED:
			JSONArray objArray = (JSONArray) msg.obj;
			String queryType = msg.getData().getString("type");
			ServerMsgProcessor.getInstance().processQueryAnswer(queryType, objArray, msg.arg1);
			break;
		case VoiceSdkService.MSG_END_OF_UPLOAD:
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
			break;
		case VoiceSdkService.MSG_UPLOAD_ERROR:
			mainService.mHandler.sendEmptyMessage(mainService.MSG_UI_INIT);
			break;
		case VoiceSdkService.MSG_WAKEN:
			mainService.startCapture();
			break;
		case MsgConst.MSG_CONTACT_ADDED:
			long id = msg.arg1;
			CallServerHelper.getInstance().notifyServerContactAdded(id);
			break;
		case MsgConst.MSG_CONTACT_MODIFIED:
			CallServerHelper.getInstance().notifyServerContactDeleted(msg.arg1);
			CallServerHelper.getInstance().notifyServerContactAdded(msg.arg2);
			break;
		case MsgConst.MSG_CONTACT_DELETED:
			CallServerHelper.getInstance().notifyServerContactDeleted(msg.arg1);
			break;
		case MsgConst.MSG_APPLIST_CHANGED:
			if (mainService.isApplistUpLoad) {// the applist is already uploade to
									// the server
				String packageName = (String) msg.obj;
				packageName = packageName.substring(8);// "package:"
				String name = AppUtil.getProgramNameByPackageName(
						mainService, packageName);
				JSONObject jsonObj = new JSONObject();
				JSONObject obj = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				try {

					if (msg.arg1 == 1) {// add an App
						String appVersion = AppUtil
								.findAppVersionByPackageName(
										mainService,
										packageName);
						jsonObj.put("title", name);
						jsonObj.put("version", appVersion);
						jsonObj.put("package_name", packageName);
						jsonArray.put(jsonObj);
						obj.put("app_name", jsonArray);
						CallServerHelper.getInstance().sendObjToServer("add_app", obj);
						AppUtil.findAllApp(mainService, true);// update
																		// applist
					} else if (msg.arg1 == 2) {// remove an App
						jsonObj.put("package_name", packageName);
						jsonArray.put(jsonObj);
						obj.put("app_name", jsonArray);
						CallServerHelper.getInstance().sendObjToServer("delete_app", obj);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			break;
		case MsgConst.MSG_PLAY_VIDEO:
			mainService.playVideo(msg.arg1, msg.arg2, (String) msg.obj);
			break;
		case MsgConst.MSG_HANDLE_INCOMING_CALL:
			mainService.handleIncomingCall(msg.getData());
			break;
		case MsgConst.MSG_START_CAPTURE:
			if (!mainService.mStartCommunication) {
				mainService.initCommunication();
			}
			if (mainService.mSpeechRecognizer != null
					&& mainService.mSpeechRecognizer.isRecognizing()) {
				mainService.mSpeechRecognizer.stopRecognize();
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				mainService.mHandler.sendMessageDelayed(
						mainService.mHandler.obtainMessage(mainService.MSG_END_OF_RECORD), 0);
			} else {
				if (Tts.isPlaying()) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
					mainService.startCapture(mainService.TTS_END_TIME);
				} else {
					mainService.startCapture();
				}
			}
			break;
		case MsgConst.MSG_STOP_CAPTURE:
			if (mainService.mSpeechRecognizer != null
					&& mainService.mSpeechRecognizer.isRecognizing()) {
				mainService.mSpeechRecognizer.stopRecognize();
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				mainService.mHandler.sendMessageDelayed(
						mainService.mHandler.obtainMessage(mainService.MSG_END_OF_RECORD), 0);
			}
			break;
		case MsgConst.CLIENT_ACTION_ABORT_VR_BY_PHONE_OR_SMS:// Enter
																// setting
																// screen
																// stop
																// VR.
			if (mainService.mSpeechRecognizer != null)
				mainService.mSpeechRecognizer.abort();
			break;
		case MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE:
			String str = (String) msg.obj;
			if (str != null) {
				MsgAnswer msgAnswer = new MsgAnswer(str);
				MsgRaw msgRaw1 = (MsgRaw) msgAnswer;
				ServerMsgProcessor.getInstance().processServerMsg(msgRaw1);
			}
			break;
		case MsgConst.SERVICE_ACTION_SEND_FEEDBACK_SUCCESS:
			FeedBackView
					.showDialog(SendDataIndicationDialog.SEND_SUCCESS);
			break;
		case MsgConst.SERVICE_ACTION_SEND_FEEDBACK_FAILED:
			FeedBackView
					.showDialog(SendDataIndicationDialog.SEND_FAILED);
			break;
		case MsgConst.CLIENT_ACTION_START_TTS:
			String ttsText = (String) msg.obj;
			if (ttsText != null) {
				if (mainService.mSpeechRecognizer != null)
					mainService.mSpeechRecognizer.abort();
				Tts.playText(mainService, ttsText, null);
			}
			break;
		case MsgConst.CLIENT_ACTION_CANCEL_RECORD:
			if (mainService.mSpeechRecognizer != null) {
				if (mainService.mSpeechRecognizer.isRecognizing()) {
					mainService.mSpeechRecognizer.abort();
					if (SavedData.isVoiceWakeUpOpen())
						mainService.mHandler.sendMessage(mainService.mHandler
								.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE));
				}
			}
			break;
		case MsgConst.CLIENT_ACTION_DISPLAY_UPDATE_DIALOG:
			Bundle bundleData = msg.getData();
			int build_version = bundleData.getInt("build_version");
			String version = bundleData.getString("version");
			String description = bundleData.getString("description");
			String update_url = bundleData.getString("update_url");
			long file_size = bundleData.getLong("file_size");
			String file_name = bundleData.getString("file_name");
			UpdateVersionDialog dialog = new UpdateVersionDialog(
					mainService, build_version, version,
					update_url, description, file_size, file_name);
			dialog.show();
			break;
		case MsgConst.CLIENT_ACTION_DISPLAY_VERSION_UPDATE:
			Toast.makeText(mainService, (String) msg.obj,
					Toast.LENGTH_LONG).show();
			break;
		case MsgConst.MSG_START_CAPTURE_OFFLINE:
			if (!mainService.mStartCommunication) {
				mainService.initCommunication();
			}
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			mainService.startCapture(true);
			break;
		default:
			Message newMsg = new Message();
			newMsg.copyFrom(msg);
			CallClientHelper.getInstance().sendBackToClient(newMsg);
			break;
		}
	}
}
