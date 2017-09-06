package com.viash.voicelib.data;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

public class BaseData {
	protected String mDisplayStr = null;
	protected String mTtsStr = null;
	protected String mTagStr = null;
	protected int mFrom;
	
	private boolean mIsSelectionData = false;
	
	public boolean isSelectionData() {
		return mIsSelectionData;
	}

	public void setIsSelectionData(boolean mIsSelectionData) {
		this.mIsSelectionData = mIsSelectionData;
	}

	protected boolean mParseResult = false;
	protected boolean mModified = false;
	
	public BaseData(BaseData data) {
		if (data == null) {
			return;
		}
		mergeData(data);
	}
	
	public BaseData(Context context, JSONObject obj, BaseData data) {
		if (data == null) {
			return;
		}
		mergeData(data);
		mParseResult = parseFromJson(context, obj);
	}
	
	public BaseData() {}
	
	public boolean doAction(Context context)
	{
		return doAction(context, null);
	}
	
	public boolean doAction(Context context, Handler handler)
	{
		return true;
	}

	public String getTtsString()
	{
		return mTtsStr;
	}
	
	public String getDisplayString()
	{
		return mDisplayStr;
	}
	
	public String getTagString()
	{
		return mTagStr;
	}
	public boolean parseFromJson(Context context, JSONObject obj)
	{
		return mParseResult;
	}
	
	public boolean isDataNeedAnswer()
	{
		return false;
	}
	
	public String getActionResult(Context context)
	{
		return null;
	}
	
	public int getFrom() {
		return mFrom;
	}

	public void setFrom(int mFrom) {
		this.mFrom = mFrom;
	}
	
	public boolean getParseResult() {
		return mParseResult;
	}
	public void setDisplayString(String str){
		mDisplayStr = str;
	}
	public void updateDisplayString(String str)
	{
		if (mDisplayStr!= null){
			mDisplayStr += str;
		} else {
			mDisplayStr = str;
		}
	}
	
	public void updateTagString(String str)
	{
		mTagStr = str;
	}
	protected void updateTTSString(String str)
	{
		if (mTtsStr!= null){
			mTtsStr += str;
		} else {
			mTtsStr = str;
		}
	}
	
	public void mergeData(BaseData base)
	{
		if (base == null) {
			return;
		}
		updateDisplayString(base.mDisplayStr);
		updateTTSString(base.mTtsStr);
	}
	
	public boolean isModified() {
		return mModified ;
	}

	public void setModified(boolean mModified) {
		this.mModified = mModified;
	}
	
	public class DescriptionData{
		public int filter;
		public String[] filters;
		public String default_input;
	}
}
