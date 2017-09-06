package com.viash.voice_assistant.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;

import com.ryong21.encode.Speex;
import com.viash.voicelib.utils.MathUtil;


public class LogRecordData {
	public static final int version = 0xfffe0001;
	public static File mFileWriting = null;
	public static String mFileName = null;
	public static ArrayList<String[]> mTextList = null;
	private static Thread mSendThread = null;
	@SuppressLint("UseValueOf")
	private static Boolean mMutex = new Boolean(true);
	private static boolean mExit = false;
	private static FileOutputStream mFileOut = null;
	private static boolean mIsWifi = false;
	protected LogRecordDataNet mRecordDataComm;
	public static String mMachineId = null;
	public static ArrayList<String> mSoundTextList = null;
	public static String mEncodedFileName = null;
	private static String TAG = "LogRecordData";
	
	public static void init(String machineId)
	{
	    mMachineId = machineId;
		mExit = false;
		mTextList = new ArrayList<String[]>();
		mSoundTextList = new ArrayList<String>();
		mSendThread = new Thread()
		{
			@Override
			public void run() {
				while(!mExit)
				{
					String[] datas = null;
					synchronized (mMutex) {
						if(mTextList.size() > 0)
						{
							datas = mTextList.remove(0);
						}
					}
					
					if(datas != null)
					{
						sendDataToServer(datas[0], datas[1]);
					}
					else
					{
						try {
							synchronized (mMutex) {
								mMutex.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		};
		
		mSendThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mSendThread.start();
	}
	
	public static void unInit()
	{

		mExit = true;
		synchronized (mMutex)
		{
			mMutex.notify();
		}
		mTextList = null;
		deleteFiles();
	}
	
	private static void deleteFiles()
	{
		try{
		    File file1 = new File(mFileName);
		    File file2 = new File(mFileName+".encode");
			if(file1.exists())
				file1.delete();
			if(file2.exists())
				file2.delete();
		}catch(Exception e)
		{
		    e.printStackTrace();
		}
	}
	
	public static boolean startNewData(boolean isWifi)
	{
		boolean ret = false;
		mIsWifi = isWifi;
		if(mIsWifi)
		{
			Date date = new Date();
			mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice_assist/pcm/";
			File fileParent = new File(mFileName);
			if(!fileParent.exists())
				fileParent.mkdirs();
			
			mFileName += String.format("%04d%02d%02d_", date.getYear() + 1900, date.getMonth() + 1, date.getDate());		
			mFileName += String.format("%02d-%02d-%02d", date.getHours(), date.getMinutes(), date.getSeconds());
			mFileName += ".pcm";
			mFileOut = null;
			
			ret = true;
		}
		return ret;
	}
	
	public static void writeData(byte[] data, int len)
	{
		if(mIsWifi)
		{
			if(mFileOut == null)
			{
				try {
					mFileOut = new FileOutputStream(mFileName);
				} catch (FileNotFoundException e) {
					try {
						  if(mFileOut!=null)
						    mFileOut.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					mFileOut = null;
					e.printStackTrace();
				}
			}
			
			if(mFileOut != null)
			{
				try {
					mFileOut.write(data, 0, len);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void writefinish(String text)
	{
		if(mIsWifi)
		{
			if(mFileOut != null && mFileName != null)
			{
				try {
					mFileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	
				synchronized (mMutex) {
					String[] datas = new String[2];
					datas[0] = text;
					datas[1] = mFileName;
					if (mTextList != null) {
						mTextList.add(datas);
						mSoundTextList.add(text);
					}
					mMutex.notify();
				}
			}
		}
	}
	
	public static int getTextLength(String text)
	{
		int length = 0;		
		try{
		    length = text.getBytes("UTF-8").length;
		}catch(Exception e)
    	{
			e.printStackTrace();
		}
		//Log.d(TAG,"getTextLength = "+length);
        return length;
	}
	public static int getDecodedFileLength()
	{
		File file = new File(mFileName + ".encode");
		return (int) file.length();
	}
	protected static void prepareHeader(byte[] data, int id, int len,String text) 
	{
		int sign = 0x11070500;
		int dataFormat = 1;//0:wave 1:speex
		int offSet = 0;
		byte[]  machineName = new byte[32];
		byte[]  machineId = new byte[32];
		byte[]  temp;
		final String mMachineName = Build.MODEL;

		try{
		    temp = mMachineName.getBytes("UTF-8");
		    for(int i=0;i<temp.length && i < 32;i++)
			{
			    machineName[i] = temp[i];
			}
		}catch(Exception e)
		{e.printStackTrace();}		
		
		try{
            temp = mMachineId.getBytes("UTF-8");  
            for(int i=0;i<temp.length && i < 32;i++)
    		{
    		    machineId[i] = temp[i];
    		}
		}catch(Exception e)
		{e.printStackTrace();}
		
		MathUtil.convertIntToByteLE(data, 0, len);//消息长度
		MathUtil.convertIntToByteLE(data, 4, version);//版本号
		MathUtil.convertIntToByteLE(data, 8, id);//消息类型    
		
		
		MathUtil.convertIntToByteLE(data, 12, sign);//标志
		MathUtil.convertIntToByteLE(data, 16, getTextLength(text));//text的byte长度
		MathUtil.convertIntToByteLE(data, 20, getDecodedFileLength());//sound文件长度
		MathUtil.convertIntToByteLE(data, 24, dataFormat);//数据格式
		offSet += 24+4;
		try{
		    System.arraycopy(machineName, 0, data, offSet, 32);//机器名称
		}catch(Exception e)
		{e.printStackTrace();}
		
		offSet += 32;
		System.arraycopy(machineId, 0, data, offSet, 32);//机器编号
		offSet += 32;
		
		int length = 0;
		try{
    	    length = text.getBytes("UTF-8").length;	
		    //Log.d(TAG,"prepareHeader()55 text = "+text+"length = "+length);
    	    System.arraycopy(text.getBytes("UTF-8"), 0,data, offSet, length);//copy到data对应位置
    	    offSet += length;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendDataToServer(String text, String fileName)
	{
		if(mIsWifi)
		{
			FileInputStream fIs = null;		
			
			try {
				fIs = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			if(fIs != null)
			{				
				Speex speex = new Speex();
				if(speex.init())
				{
					int frameSize = speex.getFrameSize();
					byte[] bIn = new byte[frameSize * 2];	
					short[] sIn = new short[frameSize];
					byte[] bOut = new byte[frameSize];
					int read = 0;
					try {
						mEncodedFileName = fileName + ".encode";
						FileOutputStream flOut = new FileOutputStream(mEncodedFileName);
						while((read = fIs.read(bIn)) != -1)
						{
							for(int i = 0; i < read / 2; i++)
							{
								sIn[i] = (short) (bIn[2 * i + 1] << 8);
								sIn[i] += bIn[2 * i];
								//sIn[i] = (short) ((bIn[i *2]&0xff) | (bIn[i *2+1]&0xff) <<8);
							}
							int encodedSize = speex.encode(sIn, 0, bOut, read / 2);
							////Log.e("sendDataToServer", "encode size:" + encodedSize);
							flOut.write(bOut, 0, encodedSize);
						}
						
						//Leo begin
						//下面是对pcm文件压缩后的文件进行解码过程
						/*final String TAG = "LogRecordData";
						
						Log.d(TAG, "sendDataToServer66 speex.getFrameSize() = "+speex.getFrameSize());
						try{
							String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice_assist/pcm/";
	                        FileOutputStream flDecodeOut = new FileOutputStream(new File(filename + "decode.pcm"));
	                        FileInputStream  flInput = new FileInputStream(new File(filename+"1.spe"));
	                        Log.d(TAG, "sendDataToServer77");
	                        
	                        byte[]  bencoded = new byte[70];
	    					short[] bdecoded = new short[320*2];
	    					
	    					while((read = flInput.read(bencoded))!=-1)
	    					{
	    						try{
	    							Log.d(TAG, "sendDataToServer88 read = "+read);
	    							int i= 0;
	    						    int decodedSize = speex.decode(bencoded, bdecoded, read);
	    						    Log.d(TAG, "sendDataToServer99 decodedSize = "+decodedSize);
	    						    
	    						    byte[] bOutBuffer = new byte[320*2];
	    						    
	    						    for(i=0; i<decodedSize; i++)
	    						    {
	    						    	bOutBuffer[i*2] = (byte) (bdecoded[i] & 0xff);
	    						    	bOutBuffer[i*2+1] = (byte) ((bdecoded[i]>>>8) & 0xff);
	    						    }
	    						    flDecodeOut.write(bOutBuffer, 0, decodedSize*2);
	    						    
	    						}catch(Exception e)
	    						{
	    						   e.printStackTrace();
	    						}
	    						
	    					}
	    					if(flInput != null)
	    						  flInput.close();
	    						if(flDecodeOut != null)
	    						{
	    							flDecodeOut.close();
	    						}   						
						}catch(Exception e)
						{
							e.printStackTrace();
						}*/
						//Leo end
						
						speex.close();
						
						if(flOut != null)
						{
							flOut.close();
						}				
					} catch (IOException e) {
						e.printStackTrace();
					}			
					
									
					try {
						fIs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	
					startToSendData(text);	
				}
			}			
			
		}
	}
	
	public static void closeNet()
	{		
		int returnResult = LogRecordDataNet.receiveFromServer();
			//Log.d(TAG,"startToSendData()44 returnResult = "+returnResult);
		if(returnResult != -1)
		   LogRecordDataNet.close(LogRecordDataNet.TS_C_SAVESOUND_EX);
		else
		   LogRecordDataNet.close(LogRecordDataNet.TS_C_SAVESOUND_EX);	
		
    }
	public static void startToSendData(String text)
	{
		int HeaderAndTextlen = 0;
		int msgHeaderLen = 4*3;
		File file = new File(mEncodedFileName);
		int fileLen = (int) file.length();
		HeaderAndTextlen = 4*4 + 32*2 + getTextLength(text);
		byte[] data = new byte[msgHeaderLen+HeaderAndTextlen];
		
		LogRecordDataNet.Init();//初始化server IP，端口号，message id
		
		if(LogRecordDataNet.connectToServer())//创建socket链接
		{
			//Log.d(TAG,"startToSendData()11 length = "+HeaderAndTextlen+" fileLen = "+fileLen);
			//准备发送除文件的数据，去掉pcm压缩文件，文件长度不包括message的Header长度
			prepareHeader(data,LogRecordDataNet.TS_C_SAVESOUND_EX,HeaderAndTextlen+fileLen,text);
			
			//Log.d(TAG,"startToSendData()22");
			LogRecordDataNet.sendData(data, HeaderAndTextlen+msgHeaderLen);//发送除pcm压缩文件外的头部数据
			//Log.d(TAG,"startToSendData()33 mEncodedFileName = "+mEncodedFileName);
			
			LogRecordDataNet.sendFile(file);//发送pcm压缩文件
		
		}
		closeNet();//关闭socket链接
		file = null;
		deleteFiles();//删除文件
	}
}
