package com.viash.voicelib.data;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.viash.voicelib.utils.JsonUtil;

public class SdkCommandData extends BaseData{
	private static final String TAG = "SdkCommandData";
	protected String mCommand = null;
	protected String mParam1 = null;
	protected String mParam2 = null;
	
	public SdkCommandData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		
		mCommand = JsonUtil.optString(obj, "name", null);
		mParam1 = JsonUtil.optString(obj, "param1", null);
		mParam2 = JsonUtil.optString(obj, "param2", null);
		if(mCommand != null)
			mParseResult = true;
		
		Log.e(TAG, mCommand);
		return mParseResult;
	}
	
	public boolean doAction(Context context, Handler handler)
	{
		boolean ret = false;
		if(mCommand != null)
		{
		//	ret = mServerCommand.action(context, handler);
			//todo
		}		
		
		return ret;
	}
	
	public String getCommand(){
		return mCommand;
	}
	
	public String getParam1(){
		return mParam1;
	}
	
	public String getParam2(){
		return mParam2;
	}
}
