package com.viash.voicelib.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

//import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.viash.voicelib.R;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketUtil {
	protected Socket mSocket;
	protected String mServer;
	protected int mPort;
	protected Context mContext;
	protected DataInputStream mIn;
	protected DataOutputStream mOut;
	protected Handler mCallerHandler;
	protected boolean mIsFree = Boolean.valueOf(false);
	protected String mWaitObj = new String("wait");
	protected List<byte[]> mLstData = new ArrayList<byte[]>();	
	protected SendThread mSendThread = null;
	protected RecThread mRecThread = null;
	protected int mConnectStatus = MsgConst.STATE_SERVER_NOT_CONNECTED;
	protected boolean mIsEncrypt = true;
	private static final long HEART_BEAT_TIME = 60000;

	private static final String TAG = "SocketUtil";

//	private static final String CLIENT_KET_PASSWORD = "123456";// 私钥密码

	private static final String CLIENT_TRUST_PASSWORD = "maqiang";// 信任证书密码

	private static final String CLIENT_AGREEMENT = "TLS";// 使用协议

//	private static final String CLIENT_KEY_MANAGER = "X509";// 密钥管理器

	private static final String CLIENT_TRUST_MANAGER = "X509";//

//	private static final String CLIENT_KEY_KEYSTORE = "BKS";// 密库，这里用的是BouncyCastle密库

	private static final String CLIENT_TRUST_KEYSTORE = "BKS";//
	
	private long mLastAccessTime = 0;
	private long mHeartBeatTime = 0;

	protected class SendThread extends Thread {
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
					//Log.i("SendToServer", "" + size);
					try
					{
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					offset += size;
				}
				//out.write(data);
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
		
		@Override
		public void run() {
			notifyNetConnection(MsgConst.STATE_SERVER_CONNECTING);
			
			if(createSocket())
			{
				if(!mExit)
					notifyNetConnection(MsgConst.STATE_SERVER_CONNECTED);
				
				mRecThread = new RecThread();
				mRecThread.start();
				
				DataOutputStream out = null;

				synchronized (mWaitObj) {
					out = mOut;
				}

				if (out != null) {
					while (!mExit) {
						byte[] data = null;

						synchronized (mWaitObj) {
							if (mLstData.size() > 0) {
								data = mLstData.get(0);
							} else {
								try {
									long startTime = System.currentTimeMillis();
									mWaitObj.wait(HEART_BEAT_TIME);
									if(System.currentTimeMillis() >= startTime + HEART_BEAT_TIME - 1000 && mHeartBeatTime < 20 && !mExit)
									{
										MsgRaw msgRaw = new MsgRaw(MsgConst.TS_C_HEARTBEAT);
										data = msgRaw.prepareRawData();
										mHeartBeatTime++;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

						if (data != null && !mExit) {
							if (!sendToServer(out, data)) {
								if(!mExit)
									notifyNetConnection(MsgConst.STATE_SERVER_NOT_CONNECTED);
								break;
							}
							else
							{
								synchronized (mWaitObj) {
									if (mLstData.size() > 0) {
										data = mLstData.remove(0);
									}
								}
							}
						}
					}
				}
			}
			else
			{
				if(!mExit)
					notifyNetConnection(MsgConst.STATE_SERVER_NOT_CONNECTED);
			}
				
		}
	}

	protected class RecThread extends Thread {
		protected boolean mExit = false;
		
		public void exit()
		{
			mExit = true;
		}
		
		protected boolean recFromServer(DataInputStream in, MsgRaw msgRaw) {
			boolean ret = false;
			Log.i(TAG, "Prepare for receiving from server!");
			ret = msgRaw.readFromStream(in);
			Log.i(TAG, "Received from server!");
			return ret;
		}
		
		@Override
		public void run() {
			DataInputStream in = null;
			Message msg = null;

			synchronized (mWaitObj) {
				in = mIn;
			}

			if (in != null) {
				while (!mExit) {
					MsgRaw msgRaw = new MsgRaw();
					if (recFromServer(in, msgRaw)) {
						mLastAccessTime = System.currentTimeMillis();
						
						msg = mCallerHandler.obtainMessage(
								MsgConst.MSG_DATA_FROM_SERVER, msgRaw);
						mCallerHandler.sendMessage(msg);
					} else {
						if(!mExit)
						{
							notifyNetConnection(MsgConst.STATE_SERVER_NOT_CONNECTED);
						}
						
						break;
					}
				}
			}
		}
	}
	
	protected void notifyNetConnection(int status)
	{
		mConnectStatus = status;

		Message msg = null;
		if(status == MsgConst.STATE_SERVER_CONNECTED)
			msg = mCallerHandler.obtainMessage(MsgConst.MSG_SERVER_CONNECTED);
		else if(status == MsgConst.STATE_SERVER_CONNECTING)
			msg = mCallerHandler.obtainMessage(MsgConst.MSG_SERVER_CONNECTING);
		else if(status == MsgConst.STATE_SERVER_NOT_CONNECTED)
			msg = mCallerHandler.obtainMessage(MsgConst.MSG_SERVER_DISCONNECTED);

		mCallerHandler.sendMessage(msg);
	}

	protected void closeSocket() {
		if (mOut != null) {
			try {
				mOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mOut = null;
		}

		if (mIn != null) {
			try {
				mIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mIn = null;
		}

		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSocket = null;
		}
	}

	protected boolean createSocket() {

		boolean ret = false;
		mLastAccessTime = System.currentTimeMillis();
		if(mIsEncrypt)
		{

			try {
				Log.i(TAG, "Preparing for creating new socket!");
				if(mIsEncrypt)
				{
					SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
					//KeyManagerFactory keyManager = KeyManagerFactory
					//		.getInstance(CLIENT_KEY_MANAGER);
		
					TrustManagerFactory trustManager = TrustManagerFactory
							.getInstance(CLIENT_TRUST_MANAGER);
		
					// KeyStore kks= KeyStore.getInstance(CLIENT_KEY_KEYSTORE); //
					KeyStore tks = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
		
					// kks.load(mContext.getResources().openRawResource(R.drawable.kclient),
					// CLIENT_KET_PASSWORD.toCharArray());
		
					tks.load(mContext.getResources()
							.openRawResource(R.raw.keystore_bks), CLIENT_TRUST_PASSWORD
							.toCharArray());
		
					// keyManager.init(kks,CLIENT_KET_PASSWORD.toCharArray());
					// keyManager.init(null, null);
		
					trustManager.init(tks);
					// trustManager.init((KeyStore)null);
		
					sslContext.init(null, trustManager.getTrustManagers(), null);
	
				
					//mSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(
					//		mServer, mPort);
					mSocket = sslContext.getSocketFactory().createSocket();
				}
				else
				{
					mSocket = new Socket();
				}
				InetSocketAddress socketAddress = new InetSocketAddress(mServer, mPort);			
				mSocket.connect(socketAddress, 5000);
				mSocket.setKeepAlive(true);
				
				//mSocket.setSendBufferSize(100 * 1024);
				mSocket.setSoTimeout(3000000);
				Log.i(TAG, "new socket created!");
				try {
					mIn = new DataInputStream(mSocket.getInputStream());
					mOut = new DataOutputStream(mSocket.getOutputStream());			
					
					
					//Log.i(TAG, "setSoTimeout:" + mSocket.getSoTimeout());
					//mSocket.setSoTimeout(1000000);
					//Log.i(TAG, "setSoTimeout:" + mSocket.getSoTimeout());
					ret = true;
				} catch (IOException e) {
					mSocket = null;
					e.printStackTrace();
				}
				
				Log.i(TAG, "new socket out and in is created!");
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return ret;
	}

	public void create(Context context, String server, int port) {
		create(context, server, port, true);
	}
	
	public void create(Context context, String server, int port, boolean encrypt) {
		mContext = context;
		mServer = server;
		mPort = port;
		mIsEncrypt = encrypt;
	}
	
	class CloseSocketThread extends Thread
	{

		@Override
		public void run() {
			closeSocket();
		}
		
	}
	
	public boolean isConnected()
	{
		return (this.mConnectStatus == MsgConst.STATE_SERVER_CONNECTED);
	}

	public void stopCommunication() {
		if (mSendThread != null)
			mSendThread.exit();
		
		if (mRecThread != null)
			mRecThread.exit();
		
		Log.i(TAG, "stopCommunication start");
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
			mWaitObj.notifyAll();			
		}

		if (mSendThread != null) {
			if(mSendThread.isAlive())
			{
				try {
					mSendThread.join(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			mSendThread = null;
		}

		if (mRecThread != null) {
			if(mRecThread.isAlive())
			{
				try {
					mRecThread.join(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	
				mRecThread = null;
			}
		}
		
		mConnectStatus = MsgConst.STATE_SERVER_NOT_CONNECTED;
		
		Log.i(TAG, "stopCommunication finished");
	}

	public void startCommunication() {
		
		Log.i(TAG, "startCommunication start");
		if(mConnectStatus == MsgConst.STATE_SERVER_NOT_CONNECTED)
		{				
			mHeartBeatTime = 0;
			mWaitObj = new String("wait");
			mConnectStatus = MsgConst.STATE_SERVER_CONNECTING;
			mSendThread = new SendThread();
			mSendThread.start();
		}
		Log.i(TAG, "startCommunication finished");
	}

	public void setCallbackHandler(Handler handler) {
		mCallerHandler = handler;
	}

	public void sendMessage(byte[] data, boolean wait) {
		synchronized (mWaitObj) {
			mHeartBeatTime = 0;
			mLstData.add(data);
			mWaitObj.notify();
			
		}
	}
	
	public boolean isTimeOut()
	{
		boolean ret = false;
		if(System.currentTimeMillis() > mLastAccessTime + 1000000)
		{
			ret = true;
		}
		
		return ret;
	}

}
