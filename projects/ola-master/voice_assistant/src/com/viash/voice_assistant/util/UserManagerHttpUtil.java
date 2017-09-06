package com.viash.voice_assistant.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.viash.voicelib.utils.HttpUtil;

public class UserManagerHttpUtil {
	private static final String TAG = "UserManagerHttpUtil";
	public static final String USER_MANAGER_URL = "http://api.olavoice.com/usermanager/";//"http://10.27.129.59:8080/usermanager/";
	
	public static final long VCODE_AVAILABLE_PERIOD = 1000 * 60 * 30;//30 minutes
	
	public static final int RET_SUCCESS = 0;
	public static final int RET_UID_BE_USED = 1;
	public static final int RET_MOBILE_VCODE_ERROR = 2;
	public static final int RET_UID_PWD_NOT_MATCHED = 3;
	public static final int RET_PARAMATER_ERROR = 4;
	public static final int RET_INTERVAL_LESS_THAN_60S = 5;
	public static final int RET_VCODE_SEND_FAILED = 6;
	public static final int RET_MOBILE_BE_USED = 7;
	public static final int RET_OTHER_ERROR = 8;

	public static int getCode(Context context, String phone) {
		if (phone == null || phone.length() == 0){
			Log.e(TAG, "phone is null or empty");
			return -1;
		}
		String url = USER_MANAGER_URL + "validatemobile?mobile=" + phone;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		int status = -1;
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return status;
	}
	
	public static int getCode(Context context, String user_id, String phone) {
		if (user_id == null || user_id.length() == 0 || phone == null || phone.length() == 0){
			Log.e(TAG, "user_id or phone is null or empty");
			return -1;
		}
		String url = USER_MANAGER_URL + "validatemobileuser?mobile=" + phone + "&user=" + user_id;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		int status = -1;
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return status;
	}
	
	public static int submitCode(Context context, String user_id, String password, String phone, String code) {
		if (user_id == null || user_id.length() == 0 || password == null || password.length() == 0 ||
				phone == null || phone.length() == 0 || code == null || code.length() == 0){
			Log.e(TAG, "user_id, password, phone or code is null or empty");
			return -1;
		}
		String url = USER_MANAGER_URL + "changemobile?user=" + user_id + "&password=" + password + "&mobile=" + phone + "&vcode=" + code;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		int status = -1;
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
				return status;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return status;
	}

	public static boolean resetPwd(Context context, String user_id, String newPassword, String phone, String code) {
		if (user_id == null || user_id.length() == 0 || newPassword == null || newPassword.length() == 0 ||
				phone == null || phone.length() == 0 || code == null || code.length() == 0){
			Log.e(TAG, "user_id, password, phone or code is null or empty");
			return false;
		}
		String url = USER_MANAGER_URL + "resetpassword?user=" + user_id + "&password=" + newPassword + "&mobile=" + phone + "&vcode=" + code;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				int status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
				return (status == RET_SUCCESS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	public static String getBindedPhone(Context context, String user_id, String password) {
		if (user_id == null || user_id.length() == 0 || password == null || password.length() == 0){
			Log.e(TAG, "user_id or password is null or empty");
			return null;
		}
		String url = USER_MANAGER_URL + "userdesc?user=" + user_id + "&password=" + password;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		String phone = null;
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				int status = obj.optInt("status");
				phone = obj.optString("mobile");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status + ",phone:"+phone);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return phone;
	}
	
	public static boolean isMobileBeUsed(Context context, String mobile) {
		if (mobile == null || mobile.length() == 0){
			Log.e(TAG, "mobile is null or empty");
			return false;
		}
		String url = USER_MANAGER_URL + "checkmobile?mobile=" + mobile;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				int status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
				return (status == RET_MOBILE_BE_USED);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean isUserBeUsed(Context context, String user) {
		if (user == null || user.length() == 0){
			Log.e(TAG, "user is null or empty");
			return false;
		}
		String url = USER_MANAGER_URL + "checkuser?user=" + user;
		Log.d(TAG, "~~~~~~~~~~~~~~~~~url:" + url);
		String result = HttpUtil.sendGetCommand(context, url);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~result:" + result);
		
		if(result != null){
			try {
				JSONObject obj = new JSONObject(result);
				int status = obj.optInt("status");
				Log.d("TAG", "~~~~~~~~~~~~~~status: " + status);
				return (status == RET_UID_BE_USED);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
