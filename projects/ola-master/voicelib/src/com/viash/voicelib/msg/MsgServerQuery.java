package com.viash.voicelib.msg;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MsgServerQuery extends MsgRaw{
	private static final String TAG = "MsgServerQuery";
	protected String mQuery;

	public MsgServerQuery() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public MsgServerQuery(String query)
	{
		mQuery = query;
		mId = MsgConst.TS_S_QUERY;
	}

	public MsgServerQuery(MsgRaw raw) {
		super(raw);

		try {
			mQuery = new String(mData, 0, mData.length, "UTF-16LE");
			Log.i(TAG, mQuery);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getmQuery() {
		return mQuery;
	}
	
	public JSONObject getQueryData()
	{
		JSONObject obj = null;
		if(mQuery != null)
		{
			JSONObject objRoot;
			try {
				objRoot = new JSONObject(mQuery);
				String dataType = objRoot.getString("data_type");
				if("query".equalsIgnoreCase(dataType))
				{
					obj = objRoot.getJSONObject("data");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return obj;
	}
}
