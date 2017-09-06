package com.viash.voicelib.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Message;
import android.util.Log;

import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;

public class CommunicationUtil extends SocketUtil{
	private static final String TAG = "CommunicationUtil";
	private CommThread mCommThread = null;
	protected List<Boolean> mLstDataWait = new ArrayList<Boolean>();
	
	protected class CommThread extends Thread
	{
		protected boolean mExit = false;
		
		public void exit()
		{
			mExit = true;
		}
		
		protected boolean sendToServer(OutputStream out, byte[] data) {
			boolean ret = false;
			
			Log.i(TAG, "Prepare for send to server! data size is:" + data.length);
			try {
				int offset = 0;
				int size = 1024;
				while(offset < data.length && !mExit)
				{
					if(data.length < offset + size)
						size = (data.length - offset);				

					out.write(data, offset, size);
					try
					{
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					offset += size;
				}
				out.flush();
				ret = true;
			} catch (IOException e) {
				e.printStackTrace();
				if(!mExit)
					notifyNetConnection(MsgConst.STATE_SERVER_NOT_CONNECTED);
			} 
			Log.i(TAG, "Send to server!");
			return ret;
		}
		
		protected boolean recFromServer(DataInputStream in, MsgRaw msgRaw) {
			boolean ret = false;
			Log.i(TAG, "Prepare for receiving from server!");
			ret = msgRaw.readFromStream(in);
			Log.i(TAG, "Received from server!");
			return ret;
		}
		
		protected boolean login(DataInputStream in, DataOutputStream out)
		{
			boolean ret = false;
			if(waitForReturn(in))
			{
				if(sendOneCommand(in, out))
				{
					ret = waitForReturn(in);
				}
			}
			
			return ret;
		}
		
		protected boolean waitForReturn(DataInputStream in)
		{
			boolean ret = false;
			MsgRaw msgRaw = new MsgRaw();
			if (recFromServer(in, msgRaw)) {
				Message msg = mCallerHandler.obtainMessage(
						MsgConst.MSG_DATA_FROM_SERVER, msgRaw);
				mCallerHandler.sendMessage(msg);
				ret = true;
			} 
			
			return ret;
		}
		
		protected boolean sendOneCommand(DataInputStream in, DataOutputStream out)
		{
			boolean ret = true;
			byte[] data = null;
			Boolean waitForReturn = null;
			boolean processed = false;

			while(!processed && !mExit)
			{
				synchronized (mWaitObj) {
					if (mLstData.size() > 0) {
						data = mLstData.get(0);
						waitForReturn = mLstDataWait.get(0);
						processed = true;
					} else {
						try {
							mWaitObj.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
	
				if (data != null && !mExit) {
					ret = sendToServer(out, data);
					if(ret)
					{
						if(waitForReturn)
						{
							ret = waitForReturn(in);
						}
	
						synchronized (mWaitObj) {
							if (mLstData.size() > 0) {
								mLstData.remove(0);
								mLstDataWait.remove(0);
							}
						}
					}
				}
			}
			
			return ret;
		}
		
		@Override
		public void run() {
			DataOutputStream out = null;
			DataInputStream in = null;
			
			notifyNetConnection(MsgConst.STATE_SERVER_CONNECTING);
			
			if(createSocket())
			{
				if(!mExit)
					notifyNetConnection(MsgConst.STATE_SERVER_CONNECTED);
				
				synchronized (mWaitObj) {
					out = mOut;
					in = mIn;
				}
				
				if(in != null && out != null)
				{
					if(login(in, out))
					{
						while (!mExit) {
							if(!sendOneCommand(in, out))
								break;
						}
					}
				}
			}

			if(!mExit)
				notifyNetConnection(MsgConst.STATE_SERVER_NOT_CONNECTED);
		}
	}
	
	public void startCommunication() {
		
		Log.i(TAG, "startCommunication start");
		if(mConnectStatus == MsgConst.STATE_SERVER_NOT_CONNECTED)
		{				
			mWaitObj = new String("wait");
			mConnectStatus = MsgConst.STATE_SERVER_CONNECTING;
			mCommThread = new CommThread();
			mCommThread.start();
		}
		Log.i(TAG, "startCommunication finished");
	}	
	
	public void stopCommunication() {		
		Log.i(TAG, "stopCommunication start");
		if (mCommThread != null)
			mCommThread.exit();
		CloseSocketThread closeSocketThread = new CloseSocketThread();
		closeSocketThread.start();
		try {
			closeSocketThread.join(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		synchronized (mWaitObj) {
			mLstData.clear();
			mLstDataWait.clear();
			mWaitObj.notifyAll();			
		}

		if (mCommThread != null) {
			if(mCommThread.isAlive())
			{
				try {
					mCommThread.join(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			mCommThread = null;
		}
		
		mConnectStatus = MsgConst.STATE_SERVER_NOT_CONNECTED;
		
		Log.i(TAG, "stopCommunication finished");
	}
	
	public void sendMessage(byte[] data, boolean wait) {
		synchronized (mWaitObj) {
			mLstData.add(data);
			mLstDataWait.add(wait);
			mWaitObj.notify();
		}
	}
}
