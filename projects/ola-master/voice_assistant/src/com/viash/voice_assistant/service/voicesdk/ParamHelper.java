package com.viash.voice_assistant.service.voicesdk;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.util.Log;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.ConfirmData;

/**
 * 
 * 拆分主 service 功能
 * @author fenglei
 *
 */
public class ParamHelper {

	private VoiceSdkService mainService;
	private static ParamHelper _instance = null;
	
	private ParamHelper(VoiceSdkService service){
		this.mainService = service;
	}
	public static ParamHelper init(VoiceSdkService s){
		if(null == _instance)
			_instance = new ParamHelper(s);
		return _instance;
	}
	
	public static ParamHelper getInstance(){
		if(null == _instance)
			throw new RuntimeException("please init ParamHelper");
		return _instance;
	}


	public void appendModificationContactInfo(JSONObject dataObj) {
		JSONArray optJSONArray = dataObj.optJSONArray("confim");
		dataObj.remove("confim");
		JSONObject objContact = new JSONObject();
		try {
			objContact.put("type", "contact");
			objContact.put("name", optJSONArray.get(0));
			objContact.put("contactnumber", optJSONArray.get(1));
			dataObj.put("appendix", objContact);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressLint("SdCardPath")
	public void appendTestSdkCommand(JSONObject objSpeech) {
		JSONObject obj = new JSONObject();
		try {
			objSpeech.put("current_app_info", obj);
			JSONArray array = new JSONArray();
			array.put("Play");
			array.put("Stop");
			array.put("Selection");
			obj.put("cmdlist", array);

			JSONObject objSub = new JSONObject();
			objSub.put("url", "/mnt/sdcard/test.mp3");
			objSub.put("type", "music");
			obj.put("curfocus", objSub);

			array = new JSONArray();

			array.put(mainService.getString(R.string.voiceassistantservice_idle_star1));
			array.put(mainService.getString(R.string.voiceassistantservice_idle_star2));
			obj.put("curlist", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void appendSdkCommand(JSONObject obj) {
		if (mainService.mCurUIInfo != null && mainService.mCurUIInfo.length() > 0) {
			try {
				JSONObject currentAppInfoObj = new JSONObject(mainService.mCurUIInfo);
				obj.put("current_app_info", currentAppInfoObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mainService.mCurUIInfo = "";
		}
	}

	public void appendTtsHint(JSONObject obj) {
		if (mainService.mTtsIndex < mainService.mLstTtsFlag.size()) {
			if (mainService.mLstTtsFlag.get(mainService.mTtsIndex) != null) {
				try {
					obj.put("tts_position", mainService.mLstTtsFlag.get(mainService.mTtsIndex));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void appendModificationInfo(JSONObject obj) {

			if (mainService.mLatestServerData != null) {
				if (mainService.mLatestServerData.isModified()) {
					List<BaseData> lsvData = mainService.mLatestServerData.getLstData();
					if (lsvData == null) {
						return;
					}
					for (BaseData data : lsvData) {
						if (data instanceof ConfirmData && data.isModified()) {
							String content = ((ConfirmData) data).getSmsData()
									.getContent();
							if (mainService.mSpecialStatus == mainService.SPECIAL_STATUS_SMS) {
								JSONObject objSms = new JSONObject();
								try {
									objSms.put("type", "sms");
									objSms.put("sms_content", content);
									obj.put("appendix", objSms);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		
	}

	public void appendLocationInfo(JSONObject obj) {
		Point pt = mainService.mWifiLocation.getLocation();
		if (pt != null) {
			if (!mainService.mCurLocation.equals(pt) || mainService.isControlFromWidget) {
				mainService.mCurLocation = new Point(pt);
				mainService.isControlFromWidget = false;
				JSONObject objLocation = mainService.mWifiLocation.getJsonOfLocation();
				SavedData.setmLastLocation(objLocation.toString());
				if (objLocation != null)
					try {
						objLocation.put("is_last", "0");
						obj.put("location", objLocation);
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
		} else {
			String lastLocation = SavedData.getmLastLocation();
			if (lastLocation != null && lastLocation.length() > 0) {
				try {
					JSONObject objLocation = new JSONObject(lastLocation);
					if (objLocation != null) {
						objLocation.put("is_last", "1");
						obj.put("location", objLocation);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
