package com.viash.voice_assistant.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class LogRecordDataNet {

	public static final int STATE_OK = 0;
	public static final int STATE_FAILED = 100;
	private static final String TAG = "LogRecordDataNet";
	
	public static final int TS_C_SAVESOUND_EX = 103;
	
	protected static String mServer;
	protected static int mPort;
	protected static Socket mSocket;
	protected static int mMsgId;
	protected static byte[] mSendData;
	protected static DataInputStream mIs = null;
	protected static DataOutputStream mOs = null;	
	protected static int mState = -1;
	protected static Thread mThread;	
	protected static Handler mHander;
	protected static boolean mIsConnected = false;
	protected static boolean flushFlag = false;
	protected static File mFile = null;
	protected static InetAddress addr = null;
	
	public static void Init()
	{
	    mMsgId = TS_C_SAVESOUND_EX;
		mServer = "Voicedb.olavoice.com";//"10.27.129.79";
		try{
		    addr = InetAddress.getByName(mServer);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		mPort = 10001;
	}
	public static boolean connectToServer()
	{
		return connect(mServer, mPort, mMsgId);
	}
	
	public static boolean connect(String server, int port, int msgId)
	{
		mServer = server;
		mPort = port;
		mMsgId = msgId;
		
	
		if(mOs != null)
		{
			try {
				mOs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mState = STATE_FAILED;
			}
			mOs = null;
		}
		
		if(mIs != null)
		{
			try {
				mIs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mState = STATE_FAILED;
			}
			mIs = null;
		}
		
		if(mSocket != null)
		{
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mState = STATE_FAILED;
			}
			mSocket = null;
		}
		
		mState = STATE_FAILED;
		mIsConnected = false;
		mSocket = new Socket();
		InetSocketAddress address = new InetSocketAddress(addr, mPort);
		try {
			mSocket.connect(address, 5000);
			mIs = new DataInputStream(mSocket.getInputStream());
			mOs = new DataOutputStream(mSocket.getOutputStream());
			
			if(mIs != null && mOs != null)
			{
				mState = STATE_OK;
				mIsConnected = true;
				Log.i(TAG, "connect() mIsConnected = " + mIsConnected);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mState = STATE_FAILED;
		}
		
		return 	mIsConnected;			
	}
	
	public static void close(int msgId)
	{		
		mMsgId = msgId;
		
		mThread = new Thread()
		{
			@Override
			public void run() {
				mState = STATE_OK;
				if(mOs != null)
				{
					try {
						mOs .close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mState = STATE_FAILED;
					}
					mOs = null;
				}
				
				if(mIs != null)
				{
					try {
						mIs .close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mState = STATE_FAILED;
					}
					mIs = null;
				}
				
				if(mSocket != null)
				{
					try {
						mSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mState = STATE_FAILED;
					}
					mSocket = null;
				}
				
				mIsConnected = false;
				//mHander.sendEmptyMessage(0);
			}			
		};
		
		mThread.start();
	}
	
	protected static boolean sendData(byte[] data, int dataLen) {
		boolean ret = false;
		
		Log.i(TAG, "Prepare for send to server! data size is:" + dataLen+" mIsConnected = "+mIsConnected);	
		try {
			int offset = 0;
			int size = 1024;
			
			if(mOs == null)
            {	
              return ret;
            }
			while(offset < dataLen)
			{
				if(dataLen < offset + size)
					size = (dataLen - offset);				
                
				mOs.write(data, offset, size);
				//Log.i(TAG, "sendData() size = "+size);
				try
				{
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				offset += size;
			}
			if(flushFlag)
			  mOs.flush();
			else
			  flushFlag = true;	
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Log.i(TAG, "Send to server!");
	  return ret;
	}
	
	protected static boolean sendFile(File mFileName) {
		mFile = mFileName;
		boolean ret = false;
		if(mOs == null)
        {	
          return ret;
        }
		if((mFile != null) && (mFile.length() > 0 ))
		{
			//Log.i(TAG, "sendFile()11 mFileName = "+mFile);
			FileInputStream fIs;
			try {
				fIs = new FileInputStream(mFile);
				int readed = 0;
				byte[] data = new byte[1024*40];
				
				do
				{
					try {
						readed = fIs.read(data);
						if(readed > 0)
							sendData(data, readed);
					} catch (IOException e) {					
						e.printStackTrace();
						break;
					}
					
				}while(readed >= 0);
			} catch (FileNotFoundException e1) {
				//Log.i(TAG, "sendFile()22 mFileName ="+mFile.length());
				e1.printStackTrace();
			}
		}
		flushFlag = false;
		
		ret = true;
		return ret;
	}	
	
    protected static boolean sendToServer()
    {		
		return sendData(mSendData, mSendData.length);
	}
    
    protected static int receiveFromServer()
    {
    	int result = -1;
    	try{
    		if(mIs != null)
    	      result = mIs.read();
    	    //Log.i(TAG, "receiveFromServer() result = "+result);
    	}catch(Exception e)
    	{
    	  e.printStackTrace();
    	}
		return result;
    	
    }
    public static int getResultState()
	{
		return mState;
	}
    
}
