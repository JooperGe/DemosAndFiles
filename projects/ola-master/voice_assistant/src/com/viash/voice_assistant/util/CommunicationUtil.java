package com.viash.voice_assistant.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//import cn.yunzhisheng.asr.a.o;

import com.viash.voice_assistant.data.GlobalData;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.msg.MsgServerQuery;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.HttpsSSLSocketFactory;
import com.viash.voicelib.utils.MachineUtil;

public class CommunicationUtil {
	private static String TAG = "CommunicationUtil";
	protected Context mContext;
	protected String mServer;
	protected HttpClient mHttpClient;
	protected boolean mExit;
	protected boolean mExitSession;
	protected String mSessionId;

	protected static final int ERR_OK = 0;
	protected static final int ERR_NET = -1;
	protected static final int ERR_SESSION = -2;
	protected static final int ERR_NO_SERVER = -3;

	protected static final int TYPE_POST_ACCOUNT = 0;
	protected static final int TYPE_POST_DELETE = 1;
	protected static final int TYPE_POST_PROMPT = 2;
	protected static final int TYPE_POST_LOGON = 3;
	protected static final int TYPE_POST_ANSWER = 4;
	protected static final int TYPE_POST_DUMP = 5;
	public static final int SEND_DATA_TYPE_STATISTICS = 1;
	public static final int SEND_DATA_TYPE_FEEDBACK = 2;

//	protected static final String SERVER_HEADER = "https://SERVER/olaweb/webservice/v1";
//	protected static final String SERVER_HEADER = "http://original.olami.ai/borui/olaweb/webservice/v1";
	protected static final String SERVER_HEADER = "http://api.olavoice.com/borui/olaweb/webservice/v1";
	
	protected static final int CONNECT_TIMEOUT = 5000;
	protected static final int SO_TIMEOUT = 20000;
	protected String mWaitData = new String("WaitData");
	protected String mWaitStart = new String("WaitStart");
	protected List<SendData> mLstData = new ArrayList<SendData>();
	protected CommunicationThread mThread;
	protected byte[] mAccountData;
	protected byte[] mContactData;
	protected byte[] mAppData;
	protected byte[] mClientInfoData;
	protected Handler mCallerHandler;
	protected boolean mNeedWaitStart;
	protected boolean USING_DEBUG_SERVER = false;
	private JSONObject jsonObject;

	public CommunicationUtil() {
		mExit = false;
		mExitSession = false;
		mSessionId = "";
		mNeedWaitStart = true;
	}

	public void init(Context context) {
		mContext = context;
		if (USING_DEBUG_SERVER)
			mHttpClient = new DefaultHttpClient();
		else
			mHttpClient = HttpsSSLSocketFactory.createMyHttpClient(
					CONNECT_TIMEOUT, SO_TIMEOUT);

		// mHttpClient = new DefaultHttpClient();
		mThread = new CommunicationThread();
		mThread.start();
	}

	public void destroy() {
		mExit = true;
		synchronized (mWaitStart) {
			mWaitStart.notify();
		}

		synchronized (mWaitData) {
			mWaitData.notify();
		}

		if (mThread != null)
			try {
				mThread.join(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void setCallbackHandler(Handler handler) {
		mCallerHandler = handler;
	}

	public void setServer(String server, int port) {

		if (server.equals("api.olavoice.com") && port == 0) {
			mServer = SERVER_HEADER.replace("SERVER", "api.olavoice.com");
		} else if (server.equals("api.olavoice.com")) {
			mServer = SERVER_HEADER.replace("SERVER", server + ":" + port);
		} else {
			mServer = SERVER_HEADER.replace("SERVER", server + ":" + port)
					.replace("https", "http");
		}
		Log.i(TAG, mServer);
	}

	public void sendMessage(byte[] data, int compressType, int postType) {
		synchronized (mWaitData) {
			mLstData.add(new SendData(data, compressType, postType));
			mWaitData.notify();
		}
	}

	public void sendMessage(byte[] data, int compressType, int postType,
			int isHaveHelpStatistics) {
		synchronized (mWaitData) {
			if (isHaveHelpStatistics != 0)
				mLstData.add(new SendData(data, compressType, postType,
						isHaveHelpStatistics));
			else
				mLstData.add(new SendData(data, compressType, postType));
			mWaitData.notify();
		}
	}

	public void clearMessage() {
		synchronized (mWaitData) {
			mLstData.clear();
		}
	}

	public void sendRawMessage(byte[] data, boolean wait) {
		MsgRaw msgRaw = new MsgRaw();
		msgRaw.parseFromData(data);
		int compressMode = msgRaw.getmCompressMode();
		int msgId = msgRaw.getmId();

		byte[] dataNew = null;
		if (compressMode == MsgRaw.COMPRESS_NONE) {
			dataNew = msgRaw.getmData();
			if (dataNew != null) {
				String sData;
				try {
					sData = new String(dataNew, 0, dataNew.length, "UTF-16LE");
					Log.i(TAG, sData + "*****");
					dataNew = sData.getBytes();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else {
			dataNew = msgRaw.getmData();
		}

		if (msgId == MsgConst.TS_C_ANSWER)
			msgId = TYPE_POST_ANSWER;
		else if (msgId == MsgConst.TS_C_DUMP)
			msgId = TYPE_POST_DUMP;
		else
			msgId = TYPE_POST_PROMPT;

		sendMessage(dataNew, compressMode, msgId);
	}

	public void sendRawMessage(byte[] data, boolean wait,
			int isHaveHelpStatistics) {
		MsgRaw msgRaw = new MsgRaw();
		msgRaw.parseFromData(data);
		int compressMode = msgRaw.getmCompressMode();
		int msgId = msgRaw.getmId();

		byte[] dataNew = null;
		if (compressMode == MsgRaw.COMPRESS_NONE) {
			dataNew = msgRaw.getmData();
			if (dataNew != null) {
				String sData;
				try {
					sData = new String(dataNew, 0, dataNew.length, "UTF-16LE");
					Log.i(TAG, sData + "*****");
					dataNew = sData.getBytes();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else {
			dataNew = msgRaw.getmData();
		}

		if (msgId == MsgConst.TS_C_ANSWER)
			msgId = TYPE_POST_ANSWER;
		else if (msgId == MsgConst.TS_C_DUMP)
			msgId = TYPE_POST_DUMP;
		else
			msgId = TYPE_POST_PROMPT;

		sendMessage(dataNew, compressMode, msgId, isHaveHelpStatistics);
	}

	public void startCommunication() {
		clearMessage();
		synchronized (mWaitStart) {
			mNeedWaitStart = false;
			mWaitStart.notify();
		}
	}

	public void startNewSession(String user, String pwd) {
		startCommunication();

		JSONObject obj = new JSONObject();
		try {
			if (user == null)
				user = "";
			if (pwd == null)
				pwd = "";
			obj.put("Deviceid", MachineUtil.getMachineId(mContext));
			obj.put("Userid", user);
			obj.put("Pwd", pwd);
//			obj.put("Clientid", "---这里请填写我们提供的最新的ClientID------");
			obj.put("Clientid", "E87635F9-4CF9-4A5A-90C4-392A3562E0B6");
			
			if (GlobalData.SOFTWARE_MODE_RELEASE == GlobalData
					.getSoftwareMode()) {
				obj.put("Clientmod", "real");
			} else {
				obj.put("Clientmod", "real_log");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendMessage(obj.toString().getBytes(), MsgRaw.COMPRESS_NONE,
				TYPE_POST_ACCOUNT);
	}

	protected int httpCall(SendData data, int retryTime) {
		int error = ERR_NET;

		// Log.i(TAG, "httpCall start:");
		HttpPost post = null;

		String url = prepareUrl(data.mPostType);
		post = new HttpPost(url);

		if (data.mCompressType == MsgRaw.COMPRESS_GZ) {
			post.setHeader("Content-Type", "Json/gzip");
		} else {
			post.setHeader("Content-Type", "Json");
			if (data.mData != null) {
				String d = new String(data.mData);
				Log.i(TAG, "data:" + d);
			}

		}

		if (data.mData != null) {
			ByteArrayEntity entity = new ByteArrayEntity(data.mData);
			post.setEntity(entity);
		}

		HttpResponse response;
		try {
			// Log.i(TAG, "start:" + url);
			response = mHttpClient.execute(post);
			int status = response.getStatusLine().getStatusCode();
			// Log.i(TAG, "status:" + status);
			if (status == 200) {
				// String result = EntityUtils.toString(response.getEntity());
				HttpEntity entityRes = response.getEntity();
				InputStream in = entityRes.getContent();

				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				StringBuffer out = new StringBuffer();
				String tempString = null;

				while ((tempString = br.readLine()) != null) {
					tempString += "\n";
					out.append(tempString);
				}
				// byte[] b = new byte[4096];
				// int n;
				// while((n = in.read(b)) >= 0)
				// {
				// out.append(new String(b, 0, n));
				// }
				String result = out.toString();
//				 result = getJsonObject(result);
				Log.i(TAG, "result:" + result);

				// if (result.length() > 0) {
				error = processServerData(result, data.mPostType);
				// }
				if (data.isHaveStatistics == SEND_DATA_TYPE_STATISTICS) {
					HelpStatisticsUtil.deleteStatistics();
				} else if (data.isHaveStatistics == SEND_DATA_TYPE_FEEDBACK) {
					mCallerHandler
							.sendEmptyMessage(MsgConst.SERVICE_ACTION_SEND_FEEDBACK_SUCCESS);
				}

			} else if (status == 400) {
				error = ERR_SESSION;
			}
			if ((status != 200)
					&& (data.isHaveStatistics == SEND_DATA_TYPE_FEEDBACK)) {
				mCallerHandler
						.sendEmptyMessage(MsgConst.SERVICE_ACTION_SEND_FEEDBACK_FAILED);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			error = ERR_NO_SERVER;
		}

		if (error == ERR_NET && retryTime >= 1) {
			error = httpCall(data, retryTime - 1);
		}

		// Log.e(TAG, "httpCall end:" + error);
		return error;
	}


	protected int processServerData(String data, int postType) {
		int error = ERR_OK;
		Message msg = null;

		switch (postType) {
		case TYPE_POST_ACCOUNT:
			try {
				JSONObject obj = new JSONObject(data);
				obj = obj.optJSONObject("rsp");
				if (obj != null) {
					mSessionId = obj.optString("sessionid", "");
					String welcome = obj.optString("welcome", null);
					int loginStatus = obj.optInt("status", -1);
					if (welcome != null
							&& GlobalData.SOFTWARE_MODE_RELEASE != GlobalData
									.getSoftwareMode()) {
						GlobalData.setmServer_version(welcome);
					}
					if (loginStatus == 0) {
						GlobalData.setUserLoggedin(true);
						mCallerHandler
								.sendEmptyMessage(MsgConst.CLIENT_ACTION_USER_LOGINED);
					} else {
						GlobalData.setUserLoggedin(false);
					}
					mCallerHandler
							.sendEmptyMessage(MsgConst.CLIENT_ACTION_UPDATA_USER_LOG_STATUS);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mCallerHandler.sendEmptyMessage(MsgConst.MSG_SERVER_DATA_ERROR);
			}
			break;
		case TYPE_POST_DELETE:
			break;
		case TYPE_POST_PROMPT:
		case TYPE_POST_ANSWER:
			try {
				JSONObject obj = new JSONObject(data);
				String data_type = obj.optString("data_type");
				
				if (data_type.length() == 0
						|| data_type.equalsIgnoreCase("answer")) {
					MsgAnswer answer = new MsgAnswer(data);
					msg = mCallerHandler.obtainMessage(
							MsgConst.MSG_DATA_FROM_SERVER, answer);
					mCallerHandler.sendMessage(msg);
				} else if (data_type.equals("query")) {
					MsgServerQuery query = new MsgServerQuery(data);
					msg = mCallerHandler.obtainMessage(
							MsgConst.MSG_DATA_FROM_SERVER, query);
					mCallerHandler.sendMessage(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				mCallerHandler.sendEmptyMessage(MsgConst.MSG_SERVER_DATA_ERROR);
			}
			break;
		case TYPE_POST_LOGON:
			break;
		case TYPE_POST_DUMP:
			MsgRaw msgRaw = null;
			try {
				msgRaw = new MsgRaw(MsgConst.TS_S_DUMP,
						data.getBytes("UTF-16LE"), MsgRaw.COMPRESS_NONE);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (msgRaw != null) {
				msg = mCallerHandler.obtainMessage(
						MsgConst.MSG_DATA_FROM_SERVER, msgRaw);
				mCallerHandler.sendMessage(msg);
			} else {
				// mCallerHandler.sendEmptyMessage(MsgConst.MSG_SERVER_NET_BROKEN);
				mCallerHandler.sendEmptyMessage(MsgConst.MSG_SERVER_DATA_ERROR);
			}
			break;
		}

		if (mSessionId.length() == 0)
			error = ERR_SESSION;
		return error;
	}

	// static int i = 0;

	protected String prepareUrl(int postType) {
		String url = "";
		String sessionData = "?session=" + mSessionId;
		/*
		 * if (i == 10) { sessionData = "?session=" + "1234567"; i = 0; } i++;
		 */
		switch (postType) {
		case TYPE_POST_ACCOUNT:
			url = mServer + "/account";
			break;
		case TYPE_POST_DELETE:
			url = mServer + "/account" + sessionData;
			break;
		case TYPE_POST_PROMPT:
			url = mServer + "/prompt" + sessionData;
			break;
		case TYPE_POST_LOGON:
			url = mServer + "/logon" + sessionData;
			break;
		case TYPE_POST_ANSWER:
			url = mServer + "/answer" + sessionData;
			break;
		case TYPE_POST_DUMP:
			url = mServer + "/userdump" + sessionData;
			break;
		}

		return url;
	}

	class CommunicationThread extends Thread {
		void waitCommunicationStart() {
			synchronized (mWaitStart) {
				if (mNeedWaitStart) {
					try {
						mWaitStart.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		void needNewSession() {
			synchronized (mWaitStart) {
				mNeedWaitStart = true;
			}
		}

		@Override
		public void run() {
			int error = -1;
			while (!mExit) {
				waitCommunicationStart();

				while (!mExit) {
					SendData data = null;
					synchronized (mWaitData) {
						if (mLstData.size() > 0) {
							data = mLstData.get(0);
						} else {
							try {
								mWaitData.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

					if (data != null && !mExit) {
						error = httpCall(data, 0); // remove retry.
						if (error == ERR_OK) {
							synchronized (mWaitData) {
								if (mLstData.size() > 0) {
									data = mLstData.remove(0);
								}
							}
						} else if (error == ERR_NET) {
							// Just tell UI that the network is broken
							clearMessage();
							mCallerHandler
									.sendEmptyMessage(MsgConst.MSG_SERVER_NET_BROKEN);
						} else if (error == ERR_SESSION) {
							// Let SDK start new session
							clearMessage();

							mCallerHandler
									.sendEmptyMessage(MsgConst.MSG_SERVER_SESSION_BROKEN);

							needNewSession();
							break;
						} else if (error == ERR_NO_SERVER) {
							clearMessage();
							mCallerHandler
									.sendEmptyMessage(MsgConst.MSG_SERVER_NET_BROKEN);
						}
					}
				}
			}
		}
	};

	class SendData {
		public SendData(byte[] data, int compressType, int postType) {
			mData = data;
			mCompressType = compressType;
			mPostType = postType;
		}

		public SendData(byte[] data, int compressType, int postType,
				int isHaveStatistics) {
			mData = data;
			mCompressType = compressType;
			mPostType = postType;
			this.isHaveStatistics = isHaveStatistics;
		}

		public byte[] mData;
		public int mCompressType;
		public int mPostType;
		public int isHaveStatistics;
	}
}
