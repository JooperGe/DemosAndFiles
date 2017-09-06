package com.viash.voicelib.msg;

import java.io.UnsupportedEncodingException;

public class MsgLogin  extends MsgRaw{
	protected String mHwId;
	protected String mUser;
	protected String mPassword;
	protected String mMisc;
	protected byte[] mMiscData = new byte[32];
	
	public MsgLogin(String hwId, String mUser, String mPassword, String mMisc) {
		super();
		this.mHwId = hwId;
		this.mUser = mUser;
		this.mPassword = mPassword;
		this.mMisc = mMisc;
	}
	
	
	public MsgLogin(MsgRaw raw) {
		super(raw);
		// TODO Auto-generated constructor stub
	}
	
	public void setMiscData(byte[] miscData)
	{
		mMisc = null;
		mMiscData = miscData;
	}

	public byte[] prepareRawData()
	{
		byte[] dataTemp;
		//int endIndex = 0;
		int length = 0;
		int dataLen = (9 + 16 * 3) * 2;
		byte[] data = new byte[getHeaderLen() + dataLen];
		prepareHeaderData(data, MsgConst.TS_C_ACCOUNT_INFO, dataLen);
		
		if(mHwId != null && mHwId.length() > 0)
		{
			//endIndex = Math.min(16, mHwId.length() - 1);
			//mHwId.getBytes(0, endIndex, data, MsgRaw.HEADER_SIZE + 18);
			try {
				dataTemp = mHwId.getBytes("UTF-8");
				length = Math.min(15, dataTemp.length);
				System.arraycopy(dataTemp, 0, data, MsgRaw.HEADER_SIZE + 18, length);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(mUser != null && mUser.length() > 0)
		{
			//endIndex = Math.min(16, mUser.length());
			//mUser.getBytes(0, endIndex, data, MsgRaw.HEADER_SIZE + 18 + 16);
			
			try {
				dataTemp = mUser.getBytes("UTF-8");
				length = Math.min(15, dataTemp.length);
				System.arraycopy(dataTemp, 0, data, MsgRaw.HEADER_SIZE + 18 + 16, length);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(mPassword != null && mPassword.length() > 0)
		{
			//endIndex = Math.min(32, mPassword.length());
			//mPassword.getBytes(0, endIndex, data, MsgRaw.HEADER_SIZE + 18 + 32);
			try {
				dataTemp = mPassword.getBytes("UTF-8");
				length = Math.min(31, dataTemp.length);
				System.arraycopy(dataTemp, 0, data, MsgRaw.HEADER_SIZE + 18 + 32, length);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(mMisc != null)
		{
			if(mMisc.length() > 0)
			{
				//endIndex = Math.min(32, mMisc.length());
				//mMisc.getBytes(0, endIndex, data, MsgRaw.HEADER_SIZE + 18 + 32 + 32);
				
				try {
					dataTemp = mMisc.getBytes("UTF-8");
					length = Math.min(31, dataTemp.length);
					System.arraycopy(dataTemp, 0, data, MsgRaw.HEADER_SIZE + 18 + 32 + 32, length);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.arraycopy(mMiscData, 0, data, MsgRaw.HEADER_SIZE + 41 * 2, 32);
		}
		return data;
	}
}
