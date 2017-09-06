package com.viash.voicelib.data;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

public class SdkActionData extends BaseData{
	@SuppressWarnings("unused")
	private static final String TAG = "SdkActionData";
	protected String mActionName;
	protected String mParam;
	
	public SdkActionData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		mActionName = obj.optString("Action", null);
		mParam = obj.optString("Parameter", "");
		if(mActionName != null)
			mParseResult = true;
		return mParseResult;
	}
	
	public boolean doAction(Context context, Handler handler)
	{
		boolean ret = false;
		
		return ret;
	}
	
	public String getActionName(){
		return mActionName;
	}
	
	public String getParam(){
		return mParam;
	}
}
