package com.viash.voicelib.msg;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.viash.voicelib.utils.MathUtil;

public class MsgRaw{
	//private static final String TAG = "MsgRaw";
	public static final int COMPRESS_NONE = 0x00000000;
	public static final int COMPRESS_LZMA = 0x00010000;
	public static final int COMPRESS_GZ = 0x00020000;
			
	public static final int NET_VERSION = 0x00000001;
	protected static final int HEADER_SIZE = 12;
	
	protected int mVersion = NET_VERSION;
	protected int mId = 0;
	protected int mDataLen = 0;
	protected int mCompressMode = COMPRESS_NONE;
	protected byte[] mData;
	
	public MsgRaw(int msgId, byte[] data, int compressMode)
	{
		mId = msgId;
		mData = data;
		mCompressMode = compressMode;
	}
	
	public MsgRaw(int msgId)
	{
		mId = msgId;
		mData = null;
		mCompressMode = COMPRESS_NONE;
	}
	
	public int getmVersion() {
		return mVersion;
	}
	public void setmVersion(int mVersion) {
		this.mVersion = mVersion;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}
	public int getmDataLen() {
		return mDataLen;
	}
	public void setmDataLen(int mDataLen) {
		this.mDataLen = mDataLen;
	}
	
	
	
	public int getmCompressMode() {
		return mCompressMode;
	}

	public void setmCompressMode(int mCompressMode) {
		this.mCompressMode = mCompressMode;
	}

	public byte[] getmData() {
		return mData;
	}
	
	public MsgRaw()
	{
	}
	
	public MsgRaw(MsgRaw raw)
	{
		mVersion = raw.mVersion;
		mId = raw.mId;
		mDataLen = raw.mDataLen;
		mData = raw.mData;
	}
	
	protected int getHeaderLen()
	{
		return HEADER_SIZE;
	}
	
	
	protected void prepareHeaderData(byte[] data, int id, int dataLen)
	{
		MathUtil.convertIntToByteLE(data, 0, (NET_VERSION | mCompressMode));
		MathUtil.convertIntToByteLE(data, 4, id);
		MathUtil.convertIntToByteLE(data, 8, dataLen);
	}
	
	public byte[] prepareRawData()
	{
		byte[] data = new byte[HEADER_SIZE];
		int len = 0;
		if(mData != null)
			len = mData.length;
		prepareHeaderData(data, mId, len);
		return data;
	}
	
	public static byte[] prepareRawData(int compressMethod, int id, byte[] entity)
	{
		byte[] data = new byte[HEADER_SIZE + entity.length];
		MathUtil.convertIntToByteLE(data, 0, (NET_VERSION | compressMethod));
		MathUtil.convertIntToByteLE(data, 4, id);
		MathUtil.convertIntToByteLE(data, 8, entity.length);
		System.arraycopy(entity, 0, data, HEADER_SIZE, entity.length);
		return data;
	}
	
	/*
	public boolean writeToStream(DataOutputStream streamOut)
	{
		boolean ret = false;
		try {
			byte[] data = prepareRawData();
			streamOut.write(data);
			ret = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}*/
	
	public boolean readFromStream(DataInputStream streamIn)
	{
		boolean ret = false;

		try {
			mVersion = MathUtil.swapInt(streamIn.readInt());
			mCompressMode = (mVersion & 0xFFFF0000);
			mVersion = (mVersion & 0x0000FFFF);
		
			mId =  MathUtil.swapInt(streamIn.readInt());
			mDataLen =  MathUtil.swapInt(streamIn.readInt());
			
			
			if(mDataLen > 0)
				mData = new byte[mDataLen];
			
			int len = 0;			
			while(len < mDataLen)
			{		
				int read = streamIn.read(mData, len, mDataLen - len);
				if(read != -1)
					len += read;
				else
					break;
			}
			
			ret = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		return ret;
	}
	
	public void parseFromData(byte[] data)
	{
		DataInputStream streamIn = new DataInputStream(new ByteArrayInputStream(data));
		readFromStream(streamIn);		
	}
}
