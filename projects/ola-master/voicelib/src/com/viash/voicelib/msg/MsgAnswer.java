package com.viash.voicelib.msg;

import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

import android.content.Context;

import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.utils.LogOutput;

public class MsgAnswer extends MsgRaw{
	private static final String TAG = "MsgAnswer";
	protected String mAnswer;

	public MsgAnswer() {
		super();
	}
	
	public MsgAnswer(String answer)
	{
		mAnswer = answer;
		mId = MsgConst.TS_S_PROMPT;
	}

	public MsgAnswer(MsgRaw raw) {
		super(raw);

		try {
			mAnswer = new String(mData, 0, mData.length, "UTF-16LE");
			LogOutput.i(TAG, mAnswer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String getmAnswer() {
		return mAnswer;
	}

	public CommunicationData getCommunicationData(Context context)
	{
		CommunicationData data = new CommunicationData(DataConst.FROM_SERVER);
		
		/*mAnswer = "{\"Just Talk Dialog Outputs\":[{\"Sentence\":{\"Present\":1, \"Content\":\"感谢使用听说对话系统，祝您生活愉快\"}}," +
				"{\"Sentence\":{\"Present\":3, \"Content\":\"有什么可以帮您的?\"}}," +
				"{\"Question\":{\"Present\":2, \"Content\":\"请问你想查哪里的天气情况\"}}," +
				"{\"Confirmation\":{\"Present\":3, \"Content\":\"请您确认，您是想打给旭上的张经理吗\"}}," +
				"{\"Selection\":{\"Display\":\"请问您要给下面哪一位打电话\", \"Speak\":\"请问您是要给张敏还是张平还是张三打电话?\", \"SelectionBody\":" +
				"[\"张敏\",\"张平\",\"张三\"]" +
				"}},{\"App\":{\"App Name\":\"PhoneCall\", \"PhoneNumber\":\"1380000000\"}}," +
				"{\"PreFormatted\":{\"Type\":\"HTML\",\"Content\":\"<A Long HTML Stream>\"}}]}";*/
		            		
		if(!data.setJsonString(context, mAnswer))
			data = null;
		
		return data;		
	}
	
	public JSONObject getJsonData()
	{
		JSONObject obj = null;
		try
		{
			obj = new JSONObject(mAnswer);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return obj;
	}
	
}
