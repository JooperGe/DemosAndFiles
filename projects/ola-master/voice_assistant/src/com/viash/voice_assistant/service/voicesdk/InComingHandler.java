package com.viash.voice_assistant.service.voicesdk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.common.Config;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.util.CommunicationUtil;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.utils.CommunicationHelpUtil;
import com.viash.voicelib.utils.HttpUtil;
import com.viash.voicelib.utils.ScreenAndKeyguard;

/**
 * 
 * 拆分 主 service 功能
 * 
 * 事件处理
 * 
 * @author fenglei
 *
 */
@SuppressLint("HandlerLeak")
public class InComingHandler  extends Handler {
	
	private VoiceSdkService mainService;
	
	public InComingHandler(VoiceSdkService service){
		this.mainService = service;
	}
	
	@Override
	public void handleMessage(Message msg) {
		String key;
		switch (msg.what) {
		case MsgConst.CLIENT_ACTION_USER_LOGINED:
			mainService.mNeedRequestUserLevel = true;
			break;
		case MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER:
			mainService.mSendingPrompt = null;
			key = msg.getData().getString("key");
			mainService.cMessenger.remove(key);

			// if(Config.ShouldAlwaysShowTopVoiceBtn())
			// {
			// mVoiceSdkUi.showVoiceView(mProcessState);
			// }
			// else
			// {
			// mVoiceSdkUi.hideUI(true);
			// }
			break;
		case MsgConst.CLIENT_ACTION_REGISTER_CLIENT_MESSENGER:
			mainService.mSendingPrompt = null;
			key = msg.getData().getString("key");
			mainService.mKeyClient = key;

			if (mainService.getApplicationContext().getPackageName().equals(key)) {
				mainService.mVoiceSdkUi.hideUI(true);
			} else if (Config.ShouldAlwaysShowTopVoiceBtn()) {
				mainService.mVoiceSdkUi.showVoiceView(mainService.mProcessState);
			}

			if (key != null) {
				mainService.cMessenger.put(key, msg.replyTo);
			}

			if (msg.arg2 == 1)
				CallServerHelper.getInstance().requestUserLevel();
			break;
		case MsgConst.CLIENT_ACTION_INIT_COMMUNICATION:
			if (msg.arg1 == 1) {
				mainService.mForceConnect = true;
			}
			mainService.initCommunication();
			break;
		case MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER:
			byte[] data = msg.getData().getByteArray("data");
			mainService.mSocketUtil.sendRawMessage(data, true);
			break;
		case MsgConst.CLIENT_ACTION_START_CAPTURE:
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			if (mainService.mSpeechRecognizer != null
					&& mainService.mSpeechRecognizer.isRecognizing()) {
				mainService.mSpeechRecognizer.stopRecognize();
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);

				mainService.mHandler.sendMessageDelayed(
						mainService.mHandler.obtainMessage(mainService.MSG_END_OF_RECORD), 0);
				if (SavedData.isVoiceWakeUpOpen()) {
					if (msg.arg1 == 0) // isUp
						mainService.mHandler.sendMessageDelayed(mainService.mHandler
								.obtainMessage(MsgConst.MSG_START_CAPTURE),
								100);
				}
			} else {
				int isUp = msg.arg1;
				if (isUp == 0) {
					AiTalkShareData.setSpeechStartState(true);
					if (Tts.isPlaying()) {
						Tts.stop(Tts.TTS_NORMAL_PRIORITY);
						mainService.startCapture(mainService.TTS_END_TIME);
					} else {
						mainService.startCapture();
					}
				}
			}
			break;
		case MsgConst.CLIENT_ACTION_START_RECOGNITION:
			String result = (String) msg.obj;
			mainService.mHandler.sendMessageDelayed(mainService.mHandler.obtainMessage(
					MsgConst.MSG_DATA_FROM_TEXT, result), 0);
			break;
		case MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED:
			Message msgNew = mainService.mHandler
					.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
			msgNew.obj = msg.obj;
			msgNew.setData(msg.getData());
			mainService.mHandler.sendMessage(msgNew);
			break;
		case MsgConst.CLIENT_ACTION_SELECTION_ANSWER:
			int option = msg.arg1;
			int type = msg.arg2;
			String[] confimMessage = (String[]) msg.obj;
			// sendSelectionToServer(option, type);
			CallServerHelper.getInstance().sendSelectionToServer(option, type, confimMessage);
			break;
		case MsgConst.CLIENT_ACTION_STOP_TTS:
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			break;
		case MsgConst.CLIENT_ACTION_START_TTS:
			Bundle bundleTts = msg.getData();
			if(bundleTts != null)
			{
				String ttsText = (String)bundleTts.getString("param");		
				Tts.playText(mainService, ttsText, null);
				// mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
			}
			break;
		case MsgConst.CLIENT_ACTION_CAPTURE_VIEW_OK:
			@SuppressWarnings("unchecked")
			final ArrayList<NameValuePair> lstValue = (ArrayList<NameValuePair>) msg.obj;
			final String filePath = msg.getData().getString("file");
			final String url = msg.getData().getString("url");
			final String queryType = msg.getData().getString("queryType");
			new Thread(new Runnable() {
				@Override
				public void run() {
					processPostCaptureViewToWeibo(filePath, url, lstValue,
							queryType);
				}
			}).start();
			break;
		case MsgConst.CLIENT_ACTION_REPORT_UI_INFO:
			Bundle bundle = msg.getData();
			if (bundle != null) {
				mainService.mLastUIInfo = mainService.mCurUIInfo;
				mainService.mCurUIInfo = (String) bundle.getString("param");
			}
			break;
		case MsgConst.CLIENT_ACTION_CLEAR_TALK_HISTORY:
			CallServerHelper.getInstance().clearTalkHistory();
			break;
		case MsgConst.CLIENT_ACTION_CANCEL_RECORD:
			if (mainService.mSpeechRecognizer != null) {
				if (mainService.mSpeechRecognizer.isRecognizing()) {
					mainService.mSpeechRecognizer.abort();
				}
			}
			break;
		case MsgConst.CLIENT_ACTION_WELCOME:
			CommunicationHelpUtil helpUtil = new CommunicationHelpUtil(
					mainService, mainService.mHandler);
			if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE) {
				mainService.SERVER = SavedData.INTERNET_SERVER_IP_HTTP;
				mainService.PORT = 0;
			} else {
				mainService.SERVER = SavedData.getmIP();
				mainService.PORT = SavedData.getmPort();
			}
			helpUtil.setServer(mainService.SERVER, mainService.PORT);
			helpUtil.getHelpDataFromServer(mainService, mainService.mHandler);
			break;
		case MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE:
			String str = (String) msg.obj;
			if (str != null) {
				MsgAnswer msgAnswer = new MsgAnswer(str);
				MsgRaw msgRaw1 = (MsgRaw) msgAnswer;
				ServerMsgProcessor.getInstance().processServerMsg(msgRaw1);
			}
			break;
		case MsgConst.CLIENT_ACTION_SEND_FEEDBACK:
			CallServerHelper.getInstance().sendObjToServer("command", (JSONObject) msg.obj,
					CommunicationUtil.SEND_DATA_TYPE_FEEDBACK);
			break;
		case MsgConst.CLIENT_ACTION_ADD_COMMONDATA:
			mainService.addDataToCommondata(mainService.floatViewIdle.getRecordString(),
					mainService.floatViewIdle.getCommonData());
			mainService.playTtsDataWakeUpByFloatView(mainService.floatViewIdle.getCommonData());
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
		case MsgConst.MSG_START_CAPTURE_OFFLINE:
			if (!mainService.mStartCommunication) {
				mainService.initCommunication();
			}
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			mainService.startCapture(true);
			break;
		case MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING:
			int i = msg.arg1;
			if (i == 1) {
				if (ScreenAndKeyguard.isScreenON(VoiceSdkService.mContext) == false)
					ScreenAndKeyguard
							.turnOnScreen(VoiceSdkService.mContext);
				if (ScreenAndKeyguard
						.isScreenLock(VoiceSdkService.mContext))
					ScreenAndKeyguard
							.unlockScreen(VoiceSdkService.mContext);
			}
			MsgRaw msgRaw = (MsgRaw) msg.obj;
			ServerMsgProcessor.getInstance().processServerMsg(msgRaw);
			break;
		default:
			break;
		}
	}

	private void processPostCaptureViewToWeibo(String filePath, String url,
			ArrayList<NameValuePair> lstValue, String queryType) {
		JSONArray objArray = new JSONArray();
		String webResponse;
		Map<String, File> files = new HashMap<String, File>();
		if (filePath != null) {
			File file = new File(filePath);
			lstValue.add(new BasicNameValuePair("pic", file
					.getAbsolutePath()));
			files.put("pic", file);
			webResponse = HttpUtil.sendMultiPartPostCommand(
					mainService.getApplicationContext(), url, lstValue, files);
		} else {
			webResponse = HttpUtil.sendPostCommand(mainService.getApplicationContext(),
					url, lstValue);
		}

		JSONObject objWebRet = new JSONObject();

		try {
			if (webResponse != null) {
				objWebRet.put("status", "0");
				objWebRet.put("return_data", webResponse);
			} else {
				objWebRet.put("status", "1");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		objArray.put(objWebRet);

		if (objArray != null) {
			Message msg = mainService.mHandler
					.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
			msg.obj = objArray;
			Bundle bundle = new Bundle();
			bundle.putString("type", queryType);
			msg.setData(bundle);
			mainService.mHandler.sendMessage(msg);
		}
	}
}