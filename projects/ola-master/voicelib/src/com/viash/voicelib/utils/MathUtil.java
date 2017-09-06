package com.viash.voicelib.utils;

import java.io.UnsupportedEncodingException;

public class MathUtil {
	public static int swapInt(int value)
	{
		int newValue = ((value >> 24) & 0x00ff) + ((value >> 8) & 0x00ff00) + ((value << 8) & 0x00ff0000) + ((value << 24) & 0xff000000);
		return newValue;
	}
	
	public static void convertIntToByteLE(byte[] data, int offset, int value)
	{
		data[offset++] = (byte)(value & 0xff);
		data[offset++] = (byte)((value >> 8) & 0xff);
		data[offset++] = (byte)((value >> 16) & 0xff);
		data[offset] = (byte)((value >> 24) & 0xff);
	}
	
	public static void convertStringToByteUTF16LE(byte[] data, int offset, String value)
	{
		byte[] temp;
		try {
			temp = value.getBytes("UTF-16LE");
			if(temp.length > 0)
			{
				System.arraycopy(temp, 0, data, offset, temp.length);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
