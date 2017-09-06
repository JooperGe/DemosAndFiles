package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

public class WeiboUtil {
	private static final String TAG = "WeiboUtil";
	protected static Handler mHandler = null;
	protected static int mMsgId = 0;
	protected static SsoHandler mSsoHandler = null;
	protected static AuthDialogListener mAuthListener = null;
	protected static String mToken = null;
	protected static String mExpires = null;
	protected static Weibo mWeibo = null;
	protected static boolean mQuerying = false;
	
	public static void releaseWeibo()
	{
		mSsoHandler = null;
		mAuthListener = null;
		mWeibo = null;
	}
	
	public static void queryWeiboToken(Activity context, Handler handler, int msgId) {
		mToken = null;
		mExpires = null;
		mHandler = handler;
		mMsgId = msgId;
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = null;
//		try {
//			packageInfo =packageManager.getPackageInfo("com.sina.weibo", 0);
//		} catch (NameNotFoundException e1) {
//			packageInfo =null;
//			e1.printStackTrace();
//		}	
//		if(packageInfo !=null ){
			 if(mWeibo == null)
					mWeibo = Weibo.getInstance("61849720", "http://www.s3graphics.com");
				if(mSsoHandler == null)
					mSsoHandler = new SsoHandler(context, mWeibo);
				if(mAuthListener == null)
					mAuthListener = new AuthDialogListener();
				mQuerying = true;
				mSsoHandler.authorize(mAuthListener);	
//		}else{
//			mQuerying = true;
//			notifyResult(false);
//		}		
	}
	
	protected static void notifyResult()
	{
		notifyResult(true);
	}
	
	protected static void notifyResult(boolean support)
	{
		if(mHandler != null && mQuerying)
		{
			Message msg = mHandler.obtainMessage(mMsgId);
			msg.obj = prepareResult(support);			
			mHandler.sendMessage(msg);
			mQuerying = false;
		}		
	}
	
	protected static JSONObject prepareResult(boolean support)
	{
		JSONObject objToken = new JSONObject();
		
		try {
			if(mToken != null && mToken.length() > 0)
			{
				objToken.put("token", mToken);					
				objToken.put("status", "0");
			}
			else if(support == false){
				objToken.put("status", "2");
			}else
			{
				objToken.put("status", "1");
			}
			if(mExpires != null && mExpires.length() > 0)
			{
				objToken.put("expires", mExpires);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return objToken;
	}
	
	public static void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			
			if(mHandler != null)
			{
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						notifyResult();
						
					}
				}, 5000);
			}
		}
	}

	static class AuthDialogListener implements WeiboAuthListener {
		public void onComplete(Bundle values) {
			mToken = values.getString("access_token");
			mExpires = values.getString("expires_in");
			notifyResult();
		}

		@Override
		public void onCancel() {
			notifyResult();
		}

		@Override
		public void onError(WeiboDialogError arg0) {
			notifyResult();
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			notifyResult();			
		}
	}
	
	protected void testSendWeiBo(Context context, String token, String content)
	{
		/*List<NameValuePair> lstPair = new ArrayList<NameValuePair>(); 
		BasicNameValuePair pair = new BasicNameValuePair("client_id", "61849720");
		lstPair.add(pair);
		pair = new BasicNameValuePair("redirect_uri", "http://www.s3graphics.com");
		lstPair.add(pair);
		HttpUtil.sendPostCommand(AssistActivity.this, "https://api.weibo.com/oauth2/authorize", lstPair);*/

		List<NameValuePair> lstPair = new ArrayList<NameValuePair>(); 
		BasicNameValuePair pair = new BasicNameValuePair("access_token", token);
		lstPair.add(pair);
		pair = new BasicNameValuePair("status", content);
		lstPair.add(pair);
		HttpUtil.sendPostCommand(context, "https://api.weibo.com/2/statuses/update.json", lstPair);
	}
}
