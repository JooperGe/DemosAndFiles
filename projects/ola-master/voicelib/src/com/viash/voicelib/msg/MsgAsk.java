package com.viash.voicelib.msg;
import org.json.JSONObject;

import com.viash.voicelib.utils.MathUtil;

public class MsgAsk  extends MsgRaw{

	protected String mSendStr;

	public MsgAsk(String mSendStr) {
		super();
		this.mSendStr = mSendStr;
	}
	
	public MsgAsk(JSONObject jsonObj, int msgId) {
		super();
		this.mId = msgId;
		this.mSendStr = jsonObj.toString();
	}
	
	public MsgAsk(JSONObject jsonObj) {		
		super();
		this.mId = MsgConst.TS_C_PROMPT;
		this.mSendStr = jsonObj.toString();
	}

	@Override
	public byte[] prepareRawData() {
		int dataLen = (mSendStr.length() + 1) * 2;
		byte[] data = new byte[getHeaderLen() + dataLen];
		prepareHeaderData(data, mId, dataLen);
		MathUtil.convertStringToByteUTF16LE(data, HEADER_SIZE, mSendStr);
		return data;
	}
}
