package com.viash.voicelib.msg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

import com.viash.voicelib.utils.MachineUtil;

public class MsgDumpResponse extends MsgRaw{
	public MsgDumpResponse(MsgRaw raw)
	{
		super(raw);
	}
	
	public MsgDumpResponse()
	{
		mId = MsgConst.TS_S_DUMP;
	}
	
	public String saveToFile(Context context)
	{
		Date date = new Date();
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice_assist/dump/";
		File fileParent = new File(fileName);
		if(!fileParent.exists())
			fileParent.mkdirs();
		
		fileName += String.format("%04d%02d%02d_", date.getYear() + 1900, date.getMonth() + 1, date.getDate());		
		fileName += String.format("%02d-%02d-%02d", date.getHours(), date.getMinutes(), date.getSeconds());
		fileName += "(" + MachineUtil.getMachineId(context) + ").log";
		if(!saveToFile(fileName))
			fileName = null;
		
		return fileName;
	}
	
	public boolean saveToFile(String fileName)
	{
		boolean ret = false;
		FileOutputStream fOs = null;
		try {
			fOs = new FileOutputStream(fileName);
			try {
				fOs.write(0x0ff);
				fOs.write(0x0fe);
				fOs.write(getmData());
				ret = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(fOs != null)
				try {
					fOs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return ret;
	}
}
