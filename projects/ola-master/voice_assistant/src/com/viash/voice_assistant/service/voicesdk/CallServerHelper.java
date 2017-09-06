package com.viash.voice_assistant.service.voicesdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.AppData;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.util.CommunicationUtil;
import com.viash.voice_assistant.util.UserPhoneDataUtil;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.ContactUtil.ContactInfo;

/**
 * 
 * 拆分 主 service功能
 * 
 * @author fenglei
 *
 */
public class CallServerHelper {
	private VoiceSdkService mainService;
	private static CallServerHelper _instance = null;
	
	private CallServerHelper(VoiceSdkService service){
		this.mainService = service;
	}
	public static CallServerHelper init(VoiceSdkService s){
		if(null == _instance)
			_instance = new CallServerHelper(s);
		return _instance;
	}
	
	public static CallServerHelper getInstance(){
		if(null == _instance)
			throw new RuntimeException("please init CallServerHelper");
		return _instance;
	}

	
	/**
	 * Contact
	 */
	public void notifyServerContactAdded(long id) {
		ContactInfo infoContact = ContactUtil.findContactById((int) id);
		if (infoContact != null) {
			List<ContactInfo> lstInfo = new ArrayList<ContactUtil.ContactInfo>();
			lstInfo.add(infoContact);

			JSONObject obj = ContactUtil.getJsonObjectOfContacts(lstInfo);
			sendObjToServer("add_contact", obj);
		}
	}

	public void notifyServerContactDeleted(long id) {
		JSONArray array = new JSONArray();
		JSONObject objId = new JSONObject();
		try {
			objId.put("id", id);
			array.put(objId);
			JSONObject obj = new JSONObject();
			obj.put("contact_ids", array);
			sendObjToServer("delete_contact", obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void clearTalkHistory() {
		if (HelpStatisticsUtil.isNeedToSendStatistics()) {
			try {
				JSONObject jobj = new JSONObject();
				JSONObject obj = new JSONObject();
				if (HelpStatisticsUtil.isNeedToSendHelpFirstStatistics()) {
					jobj = HelpStatisticsUtil.jsonObj
							.getJSONObject(HelpStatisticsUtil.HELP_FIRST);
					obj.put(HelpStatisticsUtil.HELP_FIRST, jobj);
				}
				if (HelpStatisticsUtil.isNeedToSendHelpAllStatistics()) {
					obj.put(HelpStatisticsUtil.HELP_ALL,
							HelpStatisticsUtil.jsonObj
									.getJSONArray(HelpStatisticsUtil.HELP_ALL));
				}
				if (HelpStatisticsUtil.isNeedToSendHtmlPushStatistics()) {
					obj.put(HelpStatisticsUtil.HTML_PUSH,
							HelpStatisticsUtil.jsonObj
									.getJSONArray(HelpStatisticsUtil.HTML_PUSH));
				}
				if (HelpStatisticsUtil.isNeedToSendFloatViewStatistics()) {
					obj.put(HelpStatisticsUtil.FLOAT_VIEW_ON_DESK,
							HelpStatisticsUtil.jsonObj
									.getJSONObject(HelpStatisticsUtil.FLOAT_VIEW_ON_DESK));
				}
				sendObjToServer("clear_talk_history", obj,
						CommunicationUtil.SEND_DATA_TYPE_STATISTICS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else
			sendObjToServer("clear_talk_history", new JSONObject());
		JSONObject jsonSpeech = new JSONObject();
		try {
			jsonSpeech.put("type", "logout");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		sendObjToServer("command", jsonSpeech);
		GlobalData.setUserLoggedin(false);
		if (mainService.mSocketUtil != null) {
			if (mainService.floatViewIdle != null) {
				if (!mainService.floatViewIdle.isHide())
					return;
			}
			// mSocketUtil.destroy();
			// mSocketUtil = null;
		}
	}


	public void sendSelectionToServer(int option, int type) {
		if (Tts.isPlaying()) {
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
		}
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", "selection");
			JSONObject dataObj = new JSONObject();
			dataObj.put("answer", option);
			ParamHelper.getInstance().appendModificationInfo(dataObj);
			jsonObj.put("data", dataObj);

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null && mainService.mSocketUtil != null) {
				mainService.mSocketUtil.sendRawMessage(data, true);
				CommunicationData commData = new CommunicationData(
						DataConst.FROM_MIC);
				if (type == 0) {
					for (int i = 0; i < 10; i++) {
						if (((option >> i) & 1) == 1) {
							commData.setDisplayText(mainService.getString(R.string.select)
									+ (i + 1));
							break;
						}
					}
				} else {
					if (option == 0)
						commData.setDisplayText(mainService.getResources().getString(
								R.string.no));
					else
						commData.setDisplayText(mainService.getResources().getString(
								R.string.yes));
				}
				CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
						commData);

				// mServerResponsed = false;
				mainService.setWatiServerResponse(true);

				CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mainService.switchToSpecialStatus(mainService.SPECIAL_STATUS_NORMAL);
	}

	public void sendSelectionToServer(int option, int type, String[] confimMessage) {
		if (Tts.isPlaying()) {
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
		}
		
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", "selection");
			JSONObject dataObj = new JSONObject();
			dataObj.put("answer", option);
			if(confimMessage != null){
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i < confimMessage.length; i++) {
					jsonArray.put(confimMessage[i]);
				}
				dataObj.put("confim", jsonArray);
				ParamHelper.getInstance().appendModificationContactInfo(dataObj);
			}
			ParamHelper.getInstance().appendModificationInfo(dataObj);
			jsonObj.put("data", dataObj);

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null && mainService.mSocketUtil != null) {
				mainService.mSocketUtil.sendRawMessage(data, true);
				CommunicationData commData = new CommunicationData(
						DataConst.FROM_MIC);
				if (type == 0) {
					for (int i = 0; i < 10; i++) {
						if (((option >> i) & 1) == 1) {
							commData.setDisplayText(mainService.getString(R.string.select)
									+ (i + 1));
							break;
						}
					}
				} else {
					if (option == 0)
						commData.setDisplayText(mainService.getResources().getString(
								R.string.no));
					else
						commData.setDisplayText(mainService.getResources().getString(
								R.string.yes));
				}
				CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
						commData);

				// mServerResponsed = false;
				mainService.setWatiServerResponse(true);

				CallClientHelper.getInstance().notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mainService.switchToSpecialStatus(mainService.SPECIAL_STATUS_NORMAL);
	}


	public void sendUserDataToServer() {
		if (FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE) {
			FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE = false;
			return;
		}
		JSONObject obj;
		String[] userInfo = UserData.getUserInfo(mainService);
		if (mainService.mSocketUtil == null) {
			mainService.mSocketUtil = new CommunicationUtil();
			mainService.mSocketUtil.setCallbackHandler(mainService.mHandler);
			mainService.mSocketUtil.init(mainService);
		}
		mainService.mSocketUtil.startNewSession(userInfo[0], userInfo[1]);

		JSONObject clientProperty = ClientPropertyUtil.getJsonObject(mainService);
		String nickname = AppData.getNickname(mainService);
		if (nickname != null && nickname.length() > 0) {
			try {
				clientProperty.put("client_nickname", nickname);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (HelpStatisticsUtil.isNeedToSendStatistics())
			sendObjToServer("client_property", clientProperty,
					CommunicationUtil.SEND_DATA_TYPE_STATISTICS);
		else
			sendObjToServer("client_property", clientProperty);

		if (mainService.SAVE_USER_PHONE_DATA) {
			UserPhoneDataUtil.setStringTypeInData(false);
			/*
			 * byte[] byData = UserPhoneDataUtil.initSavedData(this,
			 * UserPhoneDataUtil.DATA_TYPE_APP); if (byData != null) { byte[]
			 * newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
			 * MsgConst.TS_C_PROMPT, byData);
			 * mSocketUtil.sendRawMessage(newData, false); }
			 */
			/*
			 * UserPhoneDataUtil.startCollectData(this, mHandler,
			 * MSG_USER_DATA_REFRESH, UserPhoneDataUtil.DATA_TYPE_APP);
			 */
			byte[] byData;
			byData = UserPhoneDataUtil.initSavedData(mainService,
					UserPhoneDataUtil.DATA_TYPE_CONTACT);
			if (byData != null) {
				byte[] newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
						MsgConst.TS_C_PROMPT, byData);
				mainService.mSocketUtil.sendRawMessage(newData, false);
			}
			UserPhoneDataUtil.startCollectData(mainService, mainService.mHandler,
					mainService.MSG_USER_DATA_REFRESH, UserPhoneDataUtil.DATA_TYPE_CONTACT);

		} else {
			obj = ContactUtil.getJsonObjectOfContacts(ContactUtil
					.getmLatestContactsInfo());
			sendObjToServer("contact", obj);
			// saveJsonToFile("/mnt/sdcard/contacts.txt", obj);
			// obj = AppUtil.getJsonObjectOfApps(AppUtil.getmLatestLstApp());
			// sendObjToServer("applist", obj);
		}
	}

	public void requestUserLevel() {
		if (mainService.mKeyClient != null && mainService.mKeyClient.equals(mainService.VOICE_ASSISTANT_PACKAGE)) {
			mainService.mNeedRequestUserLevel = false;
			JSONObject obj = new JSONObject();
			try {
				obj.put("request", 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			sendObjToServer("request_user_level", obj);
			mainService.mAskingUserLevel = true;
		}
	}

	public void sendObjToServer(String dataType, JSONObject dataObj) {
		JSONObject jsonObj = new JSONObject();
		try {
			if (dataType != null) {
				jsonObj.put("data_type", dataType);
				jsonObj.put("data", dataObj);
				if(dataType.equals("delete_contact")){
					JSONArray contactId = dataObj.optJSONArray("contact_ids");
					for (int i = 0; i < contactId.length(); i++) {
						JSONObject jsonObject = contactId.getJSONObject(i);
						int id = jsonObject.getInt("id");
						ContactUtil.delContact(mainService.mContext, id);
					}
				}
				
			} else {
				jsonObj = dataObj;
			}
			

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null && mainService.mSocketUtil != null) {
				mainService.mSocketUtil.sendRawMessage(data, false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void sendObjToServer(String dataType, JSONObject dataObj,
			int isHaveHelpStatistics) {
		JSONObject jsonObj = new JSONObject();
		try {
			if (dataType != null) {
				jsonObj.put("data_type", dataType);
				jsonObj.put("data", dataObj);
			} else {
				jsonObj = dataObj;
			}
			
			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null && mainService.mSocketUtil != null) {
				mainService.mSocketUtil.sendRawMessage(data, false, isHaveHelpStatistics);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
