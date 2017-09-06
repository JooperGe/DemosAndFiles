package com.viash.voicelib.data;

import org.json.JSONObject;
import android.content.Context;

public class QuestionData extends BaseData{
	public QuestionData(Context context, JSONObject obj, BaseData data) {
		super(data);
		mParseResult = parseFromJson(context, obj);
	}
	
	public QuestionData(BaseData data) {
		super(data);
	}
	
	public QuestionData() {
		super();
	}
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		int present = obj.optInt("Present");
		String str = obj.optString("Content", null);
		if(str != null)
		{
			if((present & 1) == 1)
			{
				updateTTSString(str);
			}
			if((present & 2) == 2)
			{
				updateDisplayString(str);
			}
			
			mParseResult = true;
		}
		return mParseResult;
	}

	public boolean isDataNeedAnswer()
	{
		return true;
	}
}
