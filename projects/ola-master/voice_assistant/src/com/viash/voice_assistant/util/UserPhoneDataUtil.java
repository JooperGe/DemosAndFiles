package com.viash.voice_assistant.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.AppUtil.AppInfo;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.ContactUtil.ContactInfo;

public class UserPhoneDataUtil {
	private static final String TAG = "UserPhoneDataUtil";
	
	public static int DATA_TYPE_CONTACT = 0;
	public static int DATA_TYPE_APP = 1;
	private static final String[] DATA_TYPE_NAME = {"contact", "applist"};
	//private static final String[] FILE_NAME = {"c_contacts_lzma.bin", "c_apps_lzma.bin"};
	private static final String[] FILE_NAME_UTF8 = {"c_contacts_h_gz.bin", "c_apps_h_gz.bin"};
	private static final String[] FILE_NAME_UTF16 = {"c_contacts_gz.bin", "c_apps_gz.bin"};
	private static String[] FILE_NAME = FILE_NAME_UTF16;
	
	private static byte[] prepareZipData(int dataType, JSONObject dataObj)
	{
		byte ret[] = new byte[0];
		String sData = dataObj.toString();
		byte data[];
		try {
			data = sData.getBytes("UTF-16LE");
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1024);
			BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(byteOut));
			out.write(data);
			out.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	private static byte[] prepareCompressedData_GZIP(int dataType, JSONObject dataObj)
	{
		byte ret[] = new byte[0];
		JSONObject jsonObj = new JSONObject();
		long time = System.currentTimeMillis();
		Log.i(TAG, "start compressing " + DATA_TYPE_NAME[dataType]);
		try {
			String sData;
			if(dataType == DATA_TYPE_APP)
			{
				sData = dataObj.toString();
			}
			else
			{
				jsonObj.put("data_type", DATA_TYPE_NAME[dataType]);	
				if(dataObj != null)
					jsonObj.put("data", dataObj);
				sData = jsonObj.toString();
			}
			Log.i(TAG, "sData.length " + sData.length());
			if(sData.length() > 0)
			{
				byte data[] = null;
				if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
					data = sData.getBytes();
				else
					data = sData.getBytes("UTF-16LE");
				
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1024);
				GZIPOutputStream gos = new GZIPOutputStream(byteOut);
				gos.write(data);
				gos.close();
				ret = byteOut.toByteArray();
				byteOut.close();
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		Log.i(TAG, "Compressing finished:" + (System.currentTimeMillis() - time));
		return ret;
	}
	
	private static byte[] prepareCompressedData(int dataType, JSONObject dataObj)
	{
		byte ret[] = new byte[0];
		boolean eos = false;
		JSONObject jsonObj = new JSONObject();
		long time = System.currentTimeMillis();
		Log.i(TAG, "start compressing " + DATA_TYPE_NAME[dataType]);
		try {
			jsonObj.put("data_type", DATA_TYPE_NAME[dataType]);	
			if(dataObj != null)
				jsonObj.put("data", dataObj);
			String sData = jsonObj.toString();
			Log.i(TAG, "sData.length " + sData.length());
			if(sData.length() > 0)
			{
				byte data[] = sData.getBytes("UTF-16LE");
				ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1024);
				
				SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();
				encoder.SetNumFastBytes(5);
				encoder.SetMatchFinder(1);
				encoder.SetLcLpPb(3, 0, 2);
				encoder.SetDictionarySize(1<<18);
				encoder.SetEndMarkerMode(eos);
				encoder.WriteCoderProperties(byteOut);
				long fileSize;
				if (eos)
					fileSize = -1;
				else
					fileSize = data.length;
				for (int i = 0; i < 8; i++)
					byteOut.write((int)(fileSize >>> (8 * i)) & 0xFF);
				encoder.Code(byteIn, byteOut, -1, -1, null);
				ret = byteOut.toByteArray();
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		Log.i(TAG, "Compressing finished:" + (System.currentTimeMillis() - time));
		return ret;
	}
	
	private static boolean saveDataToFile(Context context, byte[] data, String file)
	{
		boolean ret = false;
		FileOutputStream fOs = null;
		try {
			fOs = context.openFileOutput(file, Context.MODE_PRIVATE);
			fOs.write(data);				
		} catch (FileNotFoundException e) {				
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(fOs != null)
		{
			try {
				fOs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	protected static boolean isSameAsFile(Context context, String file, byte[] data)
	{
		boolean ret = false;
		
		FileInputStream fIs = null;
		try {
			fIs = context.openFileInput(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if(fIs != null)
		{
			byte[] dataNew = new  byte[data.length + 1];
			int flLen = 0;
			try {
				flLen = fIs.read(dataNew);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if(flLen == data.length)
			{
				for(int i = 0; i < data.length; i++)
				{
					if(data[i] != dataNew[i])
						break;
					if(i + 1== data.length )
						ret = true;
				}	
				if(data.length == 0)
					ret = true;
			}
			try {
				fIs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	private static byte[] readFile(Context context, String file)
	{
		byte[] ret = null;
		FileInputStream fIs = null;
		try {
			fIs = context.openFileInput(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if(fIs != null)
		{
			byte[] data = new byte[1024];
			int len = 0;
			try {
				ByteArrayOutputStream byOut = new ByteArrayOutputStream();
				do
				{
					len = fIs.read(data);
					if(len > 0)
						byOut.write(data, 0, len);						
				}while(len > 0);
				ret = byOut.toByteArray();
				if(ret.length == 0)
					ret = null;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	private static String decompressFromFile_GZIP(Context context, String file)
	{
		final int BUFFER_SIZE = 320;
		String ret = null;
		long time = System.currentTimeMillis();
		Log.i(TAG, "start decompressing " + file);
		FileInputStream fIs = null;
		try {
			fIs = context.openFileInput(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if(fIs != null)
		{
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				int bytesRead;
				while ((bytesRead = fIs.read(b)) != -1) {
				   bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
				
				GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
				ByteArrayOutputStream byOut = new ByteArrayOutputStream();
				byte[] buf = new byte[BUFFER_SIZE];
		        int len;
		        while ((len = gzipStream.read(buf)) > 0) {
		        	byOut.write(buf, 0, len);
		        }
		        
		        if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
		        	ret = new String(byOut.toByteArray(), "UTF-8");
		        else
		        	ret = new String(byOut.toByteArray(), "UTF-16LE");
		        
		        gzipStream.close();  
		        byOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				fIs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		Log.i(TAG, "Finished decompress:" + (System.currentTimeMillis() - time));
		return ret;
	}

	private static String decompressFromFile(Context context, String file)
	{
		String ret = null;
		long time = System.currentTimeMillis();
		Log.i(TAG, "start decompressing " + file);
		FileInputStream fIs = null;
		try {
			fIs = context.openFileInput(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if(fIs != null)
		{
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			try {
				if (fIs.read(properties, 0, propertiesSize) == propertiesSize)
				{
					SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
					if (decoder.SetDecoderProperties(properties))
					{
						long outSize = 0;
						try {
							for (int i = 0; i < 8; i++)
							{
								int v;
								v = fIs.read();
								if (v >= 0)
								{
									outSize |= ((long)v) << (8 * i);
								}
								else
								{
									outSize = 0;
									break;
								}
							}
						}
						catch (IOException e) {
							outSize = 0;
							e.printStackTrace();
						}
						
						if(outSize > 0)
						{
							ByteArrayOutputStream byOut = new ByteArrayOutputStream((int)outSize);
							try {								
								if(decoder.Code(fIs, byOut, outSize))
								{
									ret = new String(byOut.toByteArray(), "UTF-16LE");
								}
									
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				fIs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		Log.i(TAG, "Finished decompress:" + (System.currentTimeMillis() - time));
		return ret;
	}
	
	public static void setStringTypeInData(boolean isUtf16)
	{
		if(isUtf16)
			FILE_NAME = FILE_NAME_UTF16;
		else
			FILE_NAME = FILE_NAME_UTF8;
	}

	public static byte[] initSavedData(Context context, int dataType)
	{
		byte[] data = null;		
			
		String sData = decompressFromFile_GZIP(context, FILE_NAME[dataType]);
		
		if(sData != null && sData.length() > 0)
		{
			JSONObject obj;
			try {
				obj = new JSONObject(sData);
				if(obj != null)
					obj = obj.optJSONObject("data");
				if(dataType == DATA_TYPE_CONTACT)
				{				
					List<ContactInfo> lstContact = ContactUtil.parseFromJson(context, obj);
					ContactUtil.setmLatestContactsInfo(lstContact);
				}
				/*else if(dataType == DATA_TYPE_APP)
				{
					List<AppInfo> lstApp = AppUtil.parseFromJson(context, obj);
					AppUtil.setmLatestLstApp(lstApp);					
				}*/
				data = readFile(context, FILE_NAME[dataType]);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}			
		}
		
		
		return data;
	}
	
	public static boolean saveData(Context context, byte[] data, int dataType)
	{
		return saveDataToFile(context, data, FILE_NAME[dataType]);
	}
	
	public static void startCollectData(Context context, Handler handler, int msgId, int dataType)
	{
		CollectDataThread thread = new CollectDataThread(context, handler, msgId, dataType);
		thread.start();
	}
	
	private static class CollectDataThread extends Thread
	{
		Context mContext = null;
		Handler mHandler = null;
		int mMsgId = 0;
		int mDataType = DATA_TYPE_CONTACT;
		
		public CollectDataThread(Context context, Handler handler, int msgId, int dataType) {
			mContext = context;
			mHandler = handler;
			mMsgId = msgId;
			mDataType = dataType;
		}
		
		@Override
		public void run() {
			JSONObject objData = null;
			//List lstData = null;
			if(mDataType == DATA_TYPE_CONTACT)
			{
				List<ContactInfo> lstContact = ContactUtil.findAllContacts(mContext, true);
				objData = ContactUtil.getJsonObjectOfContacts(lstContact);
			}
			else if(mDataType == DATA_TYPE_APP)
			{
				try{
					JSONObject obj = new JSONObject();
					JSONArray jsonApps = new JSONArray();
					objData = new JSONObject();
					List<AppInfo> lstApp = AppUtil.findAllApp(mContext, true);
					jsonApps = AppUtil.getJsonObjectOfApps(lstApp).getJSONArray("applist");
					obj.put("result", jsonApps);
					obj.put("type", "applist");
					objData.put("data", obj);
					objData.put("data_type", "answer");
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			byte[] byData = prepareCompressedData_GZIP(mDataType, objData);
			
			if(mDataType == DATA_TYPE_APP)
			{
				Message msg = mHandler.obtainMessage(mMsgId, mDataType, 0);
				msg.obj = byData;				
				mHandler.sendMessage(msg);	
			}
			else if(!isSameAsFile(mContext, FILE_NAME[mDataType], byData))
			{
				Message msg = mHandler.obtainMessage(mMsgId, mDataType, 0);
				msg.obj = byData;				
				mHandler.sendMessage(msg);	
			}			
			super.run();
		}		
	}
}
