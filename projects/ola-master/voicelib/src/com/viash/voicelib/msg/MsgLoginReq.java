package com.viash.voicelib.msg;

import java.io.UnsupportedEncodingException;
import java.sql.Time;

public class MsgLoginReq  extends MsgRaw{
	Time mTime;

	public MsgLoginReq() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MsgLoginReq(MsgRaw raw) {
		super(raw);
		String sTime;
		try {
			sTime = new String(mData, 0, mData.length, "UTF-16LE");
			mTime = Time.valueOf(sTime);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public Time getmTime() {
		return mTime;
	}
	
	

}
