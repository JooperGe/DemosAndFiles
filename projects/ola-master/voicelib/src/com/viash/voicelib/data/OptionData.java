package com.viash.voicelib.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.viash.voicelib.utils.JsonUtil;

import android.content.Context;

public class OptionData extends BaseData{
	public static final int OPTION_CALL_PEOPLE = 0x00010001;
	public static final int OPTION_CALL_NUMBER = 0x00010002;
	public static final int OPTION_CALL_RECORD = 0x00010003;
	public static final int OPTION_SMS_PEOPLE = 0x00020001;
	public static final int OPTION_SMS_NUMBER = 0x00020002;
	public static final int OPTION_MEMO_CONTENT = 0x00030001;
	public static final int OPTION_MUSIC_NAME = 0x00040001;
	public static final int OPTION_MUSIC_ALBUM = 0x00040002;
	public static final int OPTION_NEWS_NAME = 0x00050001;
	public static final int OPTION_APP_NAME = 0x00060001;
	public static final int OPTION_CALENDAR_TASK = 0x00070001;
	public static final int OPTION_CALENDAR_ALARM = 0x00070002;
	public static final int OPTION_POEM_TITLE = 0x00080001;
	public static final int OPTION_CONTACT_PEOPLE = 0x00090001;
	public static final int OPTION_PEOPLE_ADDRESS = 0x00090002;
	public static final int OPTION_SHOPING= 0x00100001;
	public static final int OPTION_MOBILE_SETTING_BLUETOOTH= 0x00110001;
	public static final int OPTION_VIDEO= 0x00120001;
	public static final int OPTION_POI= 0x00130001;
	public static final int OPTION_COOKING= 0x00140001;
	public static final int OPTION_PERSON_ENCYCLOPEDIA= 0x00150001;
	public static final int OPTION_Q_A= 0x00160001;
		
	
	protected List<String> mLstOption = new ArrayList<String>();
	protected DescriptionData mDescriptionData = null;
	
	protected int mCurrentOption = 0;
	protected int mOptionId = 0;
	protected JSONObject jsonObject = null;
		
	public OptionData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
		setIsSelectionData(true);
	}
	
	public DescriptionData getDescriptionData() {
		return mDescriptionData;
	}
	
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		updateDisplayString(obj.optString("Display", null));
		updateTTSString(obj.optString("Speak", null));
		String sId = obj.optString("selection_id");
		//sId ="0x" +sId ;		
		try
		{
			mOptionId = Integer.parseInt(sId, 16);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
		JSONArray array = obj.optJSONArray("SelectionBody");
		if(array != null)
		{
			for(int i = 0; i < array.length(); i++)
			{
				String option = array.optString(i, null);
				if(option != null)
					mLstOption.add(option);
			}
			
			mParseResult = true;
		}
		
		if(mOptionId == OPTION_PEOPLE_ADDRESS || mOptionId == OPTION_POI)
		{
			jsonObject = obj.optJSONObject("SelectionTitle");
		}
		
		JSONObject objAppendix = obj.optJSONObject("description_obj");
		if (objAppendix != null){
			mParseResult = parseDescription(context, objAppendix );
		}
		
		return mParseResult;
	}
	
	public final List<String> getOptions()
	{
		return mLstOption;
	}

	public int getCurrentOption() {
		return mCurrentOption;
	}

	public void setCurrentOption(int mCurrentOption) {
		this.mCurrentOption = mCurrentOption;
	}
	
	public boolean isDataNeedAnswer()
	{
		return true;
	}

	public int getOptionId() {
		return mOptionId;
	}

	public void setOptionId(int mOptionId) {
		this.mOptionId = mOptionId;
	}
	
	public JSONObject getJsonObject()
	{
		return jsonObject;
	}
	
	public boolean parseDescription(Context context, JSONObject obj) {
		DescriptionData descData = new DescriptionData();
		try {
			descData.filter = JsonUtil.optInt(obj, "filter", 0);
			descData.filters = JsonUtil.optStringArray(obj, "filters");
			descData.default_input = JsonUtil.optString(obj, "default_input", null);
		} catch (Exception e) {
			e.printStackTrace();
			mDescriptionData = null;
			return false;
		}
		mDescriptionData = descData;
		return true;
	}
	
}
