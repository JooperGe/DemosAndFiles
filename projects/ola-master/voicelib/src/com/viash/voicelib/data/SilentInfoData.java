package com.viash.voicelib.data;

import org.json.JSONObject;

import android.content.Context;

public class SilentInfoData  extends BaseData{
	protected int mLevel = 0;
	protected int mScore = 0;
	protected int mNextLevelScore = 0;
	protected int mSpecialTime = 0;
	
	public SilentInfoData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		mLevel = obj.optInt("level");
		mScore = obj.optInt("score");
		mNextLevelScore = obj.optInt("next_level_score");
		mSpecialTime = obj.optInt("special_time");
		
		mParseResult = true;
		return mParseResult;
	}

	public int getmLevel() {
		return mLevel;
	}

	public int getmScore() {
		return mScore;
	}

	public int getmNextLevelScore() {
		return mNextLevelScore;
	}

	public int getmSpecialTime() {
		return mSpecialTime;
	}
	
}
