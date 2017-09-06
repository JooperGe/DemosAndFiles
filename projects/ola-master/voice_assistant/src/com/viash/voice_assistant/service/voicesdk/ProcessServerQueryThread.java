package com.viash.voice_assistant.service.voicesdk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.util.UserPhoneDataUtil;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CalendarUtil;
import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.HttpUtil;
import com.viash.voicelib.utils.JsonUtil;
import com.viash.voicelib.utils.MediaUtil;
import com.viash.voicelib.utils.MemoUtil;
import com.viash.voicelib.utils.CalendarUtil.InstanceData;
import com.viash.voicelib.utils.CallUtil.PhoneData;
import com.viash.voicelib.utils.CallUtil.SmsData;
import com.viash.voicelib.utils.MediaUtil.AudioInfo;
import com.viash.voicelib.utils.MemoUtil.MemoData;
import com.viash.voicelib.utils.alarm.Alarm;
import com.viash.voicelib.utils.alarm.AlarmUtil;

/**
 * 
 * 拆分主service 功能
 * 
 * @author fenglei
 *
 */
public class ProcessServerQueryThread implements Runnable {
	/**
	 * 
	 */
	private final VoiceSdkService mainService;
	String queryType;
	JSONObject obj;

	public ProcessServerQueryThread(VoiceSdkService voiceSdkService, String type, JSONObject obj) {
		mainService = voiceSdkService;
		this.queryType = type;
		this.obj = obj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		JSONArray objArray = new JSONArray();

		int require_num;
		int start_record_id;
		int total_num = 0;
		try {
			require_num = obj.optInt("require_num");
			start_record_id = obj.optInt("start_record_id");
		} catch (Exception e) {
			e.printStackTrace();
			require_num = 0;
			start_record_id = 0;
		}
		if (queryType.equalsIgnoreCase("calendar_info")) {
			long start_time = obj.optLong("start_time");
			long end_time = obj.optLong("end_time");

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(start_time);
			int sTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60)
					+ calendar.get(Calendar.MINUTE);
			calendar.setTimeInMillis(end_time);
			int eTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60)
					+ calendar.get(Calendar.MINUTE);
			if (start_time == 0 || end_time == 0) {
				sTime = 0;
				eTime = 0;
			}

			List<InstanceData> lstCalendar = CalendarUtil.queryCalendar(
					mainService.getApplicationContext(), start_time, end_time);
			if (lstCalendar != null) {
				total_num = lstCalendar.size();
				lstCalendar = mainService.getSubList(lstCalendar, require_num,
						start_record_id);
			}
			if (lstCalendar != null && lstCalendar.size() > 0) {
				for (InstanceData instanceData : lstCalendar) {
					objArray.put(instanceData.toJsonObject());
				}
			}
			boolean isUseLocalCalendar = false;
			if (lstCalendar == null)
				isUseLocalCalendar = true;
			else if (lstCalendar.size() == 0)
				isUseLocalCalendar = true;

			if (isUseLocalCalendar) {
				List<Alarm> lstAlarm = AlarmUtil.queryAlarm(
						mainService.getApplicationContext(), sTime, eTime, 2);

				if (lstAlarm != null) {
					total_num = lstAlarm.size();
					lstAlarm = mainService.getSubList(lstAlarm, require_num,
							start_record_id);
				}
				if (lstAlarm != null && lstAlarm.size() > 0) {
					for (Alarm alarm : lstAlarm) {
						objArray.put(alarm.getRemindJsonObject());
					}
				}
			}
		} else if (queryType.equalsIgnoreCase("sms")) {
			int sms_type = obj.optInt("sms_type");
			String contact_name = obj.optString("contact_id", null);
			String contact_number = obj.optString("contact_number", null);
			String[] contact_number_list = JsonUtil.optStringArray(obj,
					"contact_number_list");
			long start_time = obj.optLong("start_time");
			long end_time = obj.optLong("end_time");
			List<SmsData> lstSmsData = null;
			if (contact_number_list != null
					&& contact_number_list.length > 0) {
				lstSmsData = new ArrayList<SmsData>();
				List<SmsData> lstSmstemp = null;
				for (int i = 0; i < contact_number_list.length; i++) {
					lstSmstemp = CallUtil.querySms(mainService.getApplicationContext(),
							contact_name, contact_number_list[i],
							start_time, end_time, sms_type, 0);
					if (lstSmstemp != null) {
						lstSmsData.addAll(lstSmstemp);
					}
				}
			} else {
				lstSmsData = CallUtil.querySms(mainService.getApplicationContext(),
						contact_name, contact_number, start_time, end_time,
						sms_type, 0);
			}
			if (lstSmsData.size() > 0) {
				total_num = lstSmsData.size();
				lstSmsData = mainService.getSubList(lstSmsData, require_num,
						start_record_id);
			}
			if (lstSmsData != null && lstSmsData.size() > 0) {
				for (SmsData smsData : lstSmsData) {
					objArray.put(smsData.toJSonObject());
				}
			}
		} else if (queryType.equalsIgnoreCase("calls")) {
			int call_type = obj.optInt("call_type");
			String contact_name = obj.optString("contact_name", null);
			String contact_number = obj.optString("contact_number", null);
			long start_time = obj.optLong("start_time");
			long end_time = obj.optLong("end_time");
			List<PhoneData> lstPhoneData = CallUtil.queryCalls(
					mainService.getApplicationContext(), call_type, contact_name,
					contact_number, start_time, end_time);
			if (lstPhoneData != null) {
				total_num = lstPhoneData.size();
				lstPhoneData = mainService.getSubList(lstPhoneData, require_num,
						start_record_id);
			}
			if (lstPhoneData != null && lstPhoneData.size() > 0) {
				for (PhoneData phoneData : lstPhoneData) {
					objArray.put(phoneData.toJSonObject());
				}
			}
		} else if (queryType.equalsIgnoreCase("bluetooth")) {
			mainService.mBlueTooth.searchBluetooth();
			objArray = null;
			Message msg = mainService.mHandler.obtainMessage(VoiceSdkService.MSG_BLUETOOTH_GETLIST);
			mainService.mHandler.sendMessageDelayed(msg, 1200);
		} else if (queryType.equalsIgnoreCase("new_email")) {

		} else if (queryType.equalsIgnoreCase("alarm_info")) {
			int startTime = obj.optInt("start_time");
			int endTime = obj.optInt("end_time");
			int start_time_addition = obj.optInt("start_time_addition");
			int end_time_addition = obj.optInt("end_time_addition");
			/*
			 * List<AlarmData> lstAlarm = AlarmUtil.queryAlarm(
			 * getApplicationContext(), startTime,
			 * endTime,start_time_addition,end_time_addition);
			 */
			List<Alarm> lstAlarm = AlarmUtil.queryAlarm(
					mainService.getApplicationContext(), startTime, endTime, 1);
			if (lstAlarm != null) {
				total_num = lstAlarm.size();
				lstAlarm = mainService.getSubList(lstAlarm, require_num,
						start_record_id);
			}
			if (lstAlarm != null && lstAlarm.size() > 0) {
				/*
				 * for (AlarmData alarm : lstAlarm) {
				 * objArray.put(alarm.toJSonObject()); }
				 */
				for (Alarm alarm : lstAlarm) {
					objArray.put(alarm.toJSonObject());
				}
			}
		} else if (queryType.equalsIgnoreCase("memo")) {
			List<MemoData> lstMemo = MemoUtil
					.queryMemo(mainService.getApplicationContext());
			if (lstMemo != null) {
				total_num = lstMemo.size();
				lstMemo = mainService.getSubList(lstMemo, require_num, start_record_id);
			}
			if (lstMemo != null && lstMemo.size() > 0) {
				for (MemoData memo : lstMemo) {
					objArray.put(memo.toJSonObject());
					Log.i(VoiceSdkService.TAG, memo.toJSonObject().toString());
				}
			}
		} else if (queryType.equalsIgnoreCase("sina_weibo_token")) {
			objArray = null;
			Bundle bundle = new Bundle();
			bundle.putString("type", queryType);
			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_WEIBO, bundle);
		} else if (queryType.equalsIgnoreCase("web_data")) {
			String webResponse = "";
			String method = obj.optString("request_method");
			String url = obj.optString("request_url");
			if ("get".equalsIgnoreCase(method)) {
				webResponse = HttpUtil.sendGetCommand(
						mainService.getApplicationContext(), url);
			} else if ("post".equalsIgnoreCase(method)) {
				int key_index = 1;
				List<NameValuePair> lstValue = null;
				while (true) {
					if (lstValue == null)
						lstValue = new ArrayList<NameValuePair>();
					String param_key = obj.optString("param_key_"
							+ key_index, null);
					String param_value = obj.optString("param_value_"
							+ key_index, null);
					if (param_key != null && param_value != null) {
						if (param_key.length() > 0
								&& param_value.length() > 0)
							lstValue.add(new BasicNameValuePair(param_key,
									param_value));
					} else {
						break;
					}
					key_index++;
				}
				int needCaptureImage = obj.optInt("need_capture_image");
				if (needCaptureImage == 1) {
					Message msg = mainService.mHandler
							.obtainMessage(MsgConst.SERVICE_ACTION_CAPTURE_VIEW);
					msg.obj = lstValue;
					Bundle bundle = new Bundle();
					bundle.putString("type", queryType);
					bundle.putString("url", url);
					msg.setData(bundle);
					CallClientHelper.getInstance().sendBackToClient(msg);
					return;
				} else {
					webResponse = HttpUtil.sendPostCommand(
							mainService.getApplicationContext(), url, lstValue);
				}
				/*
				 * JSONArray arrayKey = obj.optJSONArray("param_key");
				 * JSONArray arrayValue = obj.optJSONArray("param_value");
				 * List<NameValuePair> lstValue = new
				 * ArrayList<NameValuePair>(); if (arrayKey != null &&
				 * arrayValue != null && arrayKey.length() ==
				 * arrayValue.length()) { for (int i = 0; i <
				 * arrayKey.length(); i++) { String key =
				 * arrayKey.optString(i, ""); String value =
				 * arrayValue.optString(i, ""); if (key.length() > 0 &&
				 * value.length() > 0) lstValue.add(new
				 * BasicNameValuePair(key, value)); } } webResponse =
				 * HttpUtil.sendPostCommand( getApplicationContext(), url,
				 * lstValue);
				 */
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
		} else if (queryType.equalsIgnoreCase("video")) {

		} else if (queryType.equalsIgnoreCase("music")) {
			String title = obj.optString("title");
			String artist = obj.optString("artist");
			String album = obj.optString("album");

			List<AudioInfo> lstAudio = MediaUtil.queryMusic(
					mainService.getApplicationContext(), title, album, artist);
			if (lstAudio != null && lstAudio.size() > 0) {
				for (AudioInfo audioInfo : lstAudio) {
					objArray.put(audioInfo.toJsonObject());
				}
			}
		} else if (queryType.equalsIgnoreCase("music_playing")) {
			objArray = null;
			Bundle bundle = new Bundle();
			bundle.putString("type", queryType);
			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_MUSIC, bundle);
		} else if (queryType.equalsIgnoreCase("position_alarm")) {
			objArray = null;
			Bundle bundle = new Bundle();
			bundle.putString("type", queryType);
			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_POSITION_ALARM,
					bundle);
		} else if (queryType.equalsIgnoreCase("applist")) {
			UserPhoneDataUtil.startCollectData(mainService,
					mainService.mHandler, VoiceSdkService.MSG_USER_DATA_REFRESH,
					UserPhoneDataUtil.DATA_TYPE_APP);

			VoiceSdkService.isApplistUpLoad = true;
			return;
		}

		if (objArray != null) {
			Message msg = mainService.mHandler
					.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
			msg.obj = objArray;
			msg.arg1 = total_num;
			Bundle bundle = new Bundle();
			bundle.putString("type", queryType);
			msg.setData(bundle);
			mainService.mHandler.sendMessage(msg);
		}
	}
}