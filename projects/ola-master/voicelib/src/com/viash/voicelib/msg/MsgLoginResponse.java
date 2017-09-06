package com.viash.voicelib.msg;

public class MsgLoginResponse  extends MsgRaw{
	short mStatus;
	String mErrMsg;

	public MsgLoginResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MsgLoginResponse(MsgRaw raw) {
		super(raw);
		mStatus = (short) (mData[0] + (mData[1] << 8));
	}
	
	public boolean isLogin()
	{
		return (mStatus == 0);
	}

}
