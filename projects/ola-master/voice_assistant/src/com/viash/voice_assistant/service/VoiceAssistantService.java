package com.viash.voice_assistant.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.VIAApplication;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.common.Corpus;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.common.LogRecordData;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.data.AppData;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.observer.ContactContentObserver;
import com.viash.voice_assistant.receiver.AppListChangedReceiver;
import com.viash.voice_assistant.receiver.IncomingCallReceiver;
import com.viash.voice_assistant.receiver.SmsReceiver;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.speech.IRecognizeListener;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.util.UserPhoneDataUtil;
import com.viash.voice_assistant.widget.OlaAppWidgetProvider;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.SMSJsonData;
import com.viash.voicelib.data.SdkCommandData;
import com.viash.voicelib.hardware.HBluetooth;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgDumpResponse;
import com.viash.voicelib.msg.MsgLogin;
import com.viash.voicelib.msg.MsgLoginResponse;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.msg.MsgServerQuery;
import com.viash.voicelib.utils.AlarmUtil;
import com.viash.voicelib.utils.AlarmUtil.AlarmData;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.AppUtil.AppInfo;
import com.viash.voicelib.utils.BatteryUtil;
import com.viash.voicelib.utils.CalendarUtil;
import com.viash.voicelib.utils.CalendarUtil.InstanceData;
import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.CallUtil.PhoneData;
import com.viash.voicelib.utils.CallUtil.SmsData;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.ContactUtil.ContactInfo;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HttpUtil;
import com.viash.voicelib.utils.JsonUtil;
import com.viash.voicelib.utils.MachineUtil;
import com.viash.voicelib.utils.MediaUtil;
import com.viash.voicelib.utils.MediaUtil.AudioInfo;
import com.viash.voicelib.utils.MemoUtil;
import com.viash.voicelib.utils.MemoUtil.MemoData;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.SocketUtil;
import com.viash.voicelib.utils.TtsUtil;

public class VoiceAssistantService extends Service {
	/**
	 * Define
	 */
	private static final boolean DEBUG = true;
	private static final String TAG = "VoiceAssistantService";
	private static final String VOICE_ASSISTANT_PACKAGE = "com.viash.voice_assistant";

	private static final int MSG_BEGINNING_OF_RECORD = 6;
	private static final int MSG_END_OF_RECORD = 7;
	private static final int MSG_END_OF_UPLOAD = 8;
	private static final int MSG_UPLOAD_ERROR = 9;
	private static final int MSG_WAKEN = 10;
	private static final int MSG_UI_IN_RECORDING = 17;
	private static final int MSG_UI_STOP_RECORDING = 18;
	private static final int MSG_UI_IDLE = 19;
	private static final int MSG_TTS_PLAY_END = 30;

	public static final int MSG_USER_DATA_REFRESH = 40;
	
	private static final int MSG_BLUETOOTH_GETLIST = 50;
	public static final int MSG_UI_SEARCHING_START = 51;
	public static final int MSG_UI_SEARCHING_FOUND = 52;

	protected static final int SPECIAL_STATUS_NORMAL = 0;
	protected static final int SPECIAL_STATUS_SMS = 1;

	public static String SERVER;// = "10.27.27.228";
	public static int PORT = 80;

	private static final int TTS_END_TIME = 200;
	private static final int SERVER_RESPONSE_TIMEOUT = 15000;
	private static int mServerTimeOut = SERVER_RESPONSE_TIMEOUT;
	private boolean mClientNeedFirstPrompt = true;

	/**
	 * Variables
	 */
	public static final boolean SAVE_USER_PHONE_DATA = true;

	protected Handler mHandler;
	public static boolean mServerResponsed = false;
	public static int mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

	protected boolean mNeedAnswer = false;
	protected CommunicationData mLatestServerData = null;

	private Corpus mCorpus;

	protected int mSpecialStatus = SPECIAL_STATUS_NORMAL;

	private boolean isControlFromWidget = false;

	// Voice
	protected int mTtsIndex = 0;
	protected List<String> mLstTts = new ArrayList<String>();
	protected List<String> mLstTtsFlag = new ArrayList<String>();

	/**
	 * Socket
	 */
	protected SocketUtil mSocketUtil;
	protected List<byte[]> msgToServerQueue = new ArrayList<byte[]>();
	protected Runnable timeoutRunnable = null;

	/**
	 * Messenger
	 */
	protected Messenger mMessenger = new Messenger(new InComingHandler());
	protected HashMap<String, Messenger> cMessenger = new HashMap<String, Messenger>();
	protected List<Message> msgQueue = new ArrayList<Message>();

	protected String mKeyClient = null;
	protected boolean mNeedRequestUserLevel = false;
	protected int mProcessState = MsgConst.UI_STATE_INITED;
	protected boolean mAskingUserLevel = false;

	protected boolean mIsCharging = false;
	protected int mWakeupByAudioEnable = MsgConst.WAKEUP_AUDIO_DISABLE;
	ContactContentObserver myContentObserver;
	
	//Loneway
	private int mInputType = 0;
	
	public static boolean mStarted = false;

	@SuppressLint("HandlerLeak")
	class InComingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String key;
			switch (msg.what) {
			case MsgConst.CLIENT_ACTION_USER_LOGINED:
				mNeedRequestUserLevel = true;
				break;
			case MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER:
				key = msg.getData().getString("key");

				if (DEBUG)
					Log.i(TAG, "client unregister: " + key);

				cMessenger.remove(key);
				application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
				break;
			case MsgConst.CLIENT_ACTION_REGISTER_CLIENT_MESSENGER:
				boolean isConnected = false;
				if (msg.arg1 == 1)
					mClientNeedFirstPrompt = true;
				else
					mClientNeedFirstPrompt = false;

				if (mSocketUtil != null && mSocketUtil.isTimeOut())
					mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

				if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED) {
					initCommunication();
				} else {
					checkServerConnection();
					isConnected = true;
				}

				key = msg.getData().getString("key");
				mKeyClient = key;
				if (DEBUG)
					Log.i(TAG, "client register: " + key);
				if (key != null && !cMessenger.containsKey(key)) {
					// set third party
					if (key.equals(VoiceSdkServiceInterface.THIRDPARTY_NAME)) {
						isThirdParty = true;
					}

					/*
					 * only support one messenger, therefore need clear all
					 * client messenger!
					 */
					cMessenger.clear();
					cMessenger.put(key, msg.replyTo);
					if (isConnected && key.equals(VOICE_ASSISTANT_PACKAGE)) {
						if (mNeedRequestUserLevel)
							requestUserLevel();
						else
							notifyClientState(MsgConst.UI_STATE_INITED);
					}
				} else {
					// register failed

				}

				// purgeThirdMessageQueue();
				if (isControlFromWidget || isFloatLogo) {
					isControlFromWidget = false;
					isFloatLogo = false;
					purgeMessageQueue();
					purgeServerMessageQueue();
				}
				if (isConnected && key.equals(VOICE_ASSISTANT_PACKAGE)) {
					sendBackToClient(MsgConst.SERVICE_ACTION_CLOSE_SPLASH);
				}

				// if nickname had changed, send to server.
				boolean isNicknameSentToServer = AppData
						.getIsNicknameSendToServer(VoiceAssistantService.this);
				if (isNicknameSentToServer) {
					AppData.setIsNicknameSendToServer(
							VoiceAssistantService.this, false);
					sendUserDataToServer();
				}
				break;
			case MsgConst.CLIENT_ACTION_INIT_COMMUNICATION:
				initCommunication();
				break;
			case MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER:
				byte[] data = msg.getData().getByteArray("data");
				mSocketUtil.sendMessage(data, true);
				break;
			case MsgConst.CLIENT_ACTION_START_CAPTURE:
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				if (mSpeechRecognizer != null
						&& mSpeechRecognizer.isRecognizing()) {
					mSpeechRecognizer.stopRecognize();
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);

					mHandler.sendMessageDelayed(
							mHandler.obtainMessage(MSG_END_OF_RECORD), 0);

					if (DEBUG)
						Log.i(TAG, "stop recognize by hand");
				} else {
					int isUp = msg.arg1;
					if (isUp == 0) {
						if (checkNetConnection()) {
							if (mServerState != MsgConst.STATE_SERVER_CONNECTED) {
								Tts.stop(Tts.TTS_NORMAL_PRIORITY);

								if (DEBUG)
									Log.i(TAG, "Reconnect to server");

								if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED) {
									notifyClientState(MsgConst.UI_STATE_INITED);
									initCommunication();
								}
							} else {
								if (DEBUG)
									Log.i(TAG, "start recognize by hand");

								setWakeupByAudio(MsgConst.WAKEUP_AUDIO_DISABLE);
								if (Tts.isPlaying()) {
									Tts.stop(Tts.TTS_NORMAL_PRIORITY);
									startCapture(TTS_END_TIME);
								} else {
									startCapture();
								}
							}
						}
					}
				}
				break;
			case MsgConst.CLIENT_ACTION_START_RECOGNITION:
				String result = (String) msg.obj;
				mHandler.sendMessageDelayed(mHandler.obtainMessage(
						MsgConst.MSG_DATA_FROM_TEXT, result), 0);
				break;
			case MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED:
				Message msgNew = mHandler
						.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
				msgNew.obj = msg.obj;
				msgNew.setData(msg.getData());
				mHandler.sendMessage(msgNew);
				break;
			case MsgConst.CLIENT_ACTION_SELECTION_ANSWER:
				int option = msg.arg1;
				int type = msg.arg2;
				sendSelectionToServer(option, type);
				break;
			case MsgConst.CLIENT_ACTION_STOP_TTS:
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				break;
			case MsgConst.CLIENT_ACTION_CAPTURE_VIEW_OK:
				@SuppressWarnings("unchecked")
				final ArrayList<NameValuePair> lstValue = (ArrayList<NameValuePair>) msg.obj;
				final String filePath = msg.getData().getString("file");
				final String url = msg.getData().getString("url");
				final String queryType = msg.getData().getString("queryType");
				new Thread(new Runnable() {
					@Override
					public void run() {
						processPostCaptureViewToWeibo(filePath, url, lstValue,
								queryType);
					}
				}).start();
				break;
			case MsgConst.CLIENT_ACTION_CREATE_FLOATING_WINDOW:
				mCurrentApInfo = msg.getData().getString("param");
				if (DEBUG)
					Log.d(TAG, "client param: " + mCurrentApInfo);
				if (mServerState != MsgConst.STATE_SERVER_CONNECTED) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);

					if (DEBUG)
						Log.i(TAG, "Reconnect to server");

					if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED)
						initCommunication();

					sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED);
				} else {
					application
							.createView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
				}
				break;
			case MsgConst.CLIENT_ACTION_SET_WAKEUP_BY_AUDIO:// Enter setting screen stop VR.
				if (mSpeechRecognizer != null)
					mSpeechRecognizer.abort();
				break;
			case MsgConst.CLIENT_ACTION_REENTRY_WAKEUP_BY_AUDIO:// send a tip msg wake up auto VR
				if (mIsCharging) {
					Message msg1 = mHandler.obtainMessage(MSG_ERROR, 0, 0);
					mHandler.sendMessage(msg1);
				}
				break;
			case MsgConst.CLIENT_ACTION_CANCEL_RECORD:
				
				if(mSpeechRecognizer != null)
				{
					if (mSpeechRecognizer.isRecognizing()) {
						mSpeechRecognizer.abort();			
					}
				}
				break;
			default:
				break;
			}
		}

		private void processPostCaptureViewToWeibo(String filePath, String url,
				ArrayList<NameValuePair> lstValue, String queryType) {
			JSONArray objArray = new JSONArray();
			String webResponse;
			Map<String, File> files = new HashMap<String, File>();
			if (filePath != null) {
				File file = new File(filePath);
				lstValue.add(new BasicNameValuePair("pic", file
						.getAbsolutePath()));
				files.put("pic", file);
				webResponse = HttpUtil.sendMultiPartPostCommand(
						getApplicationContext(), url, lstValue, files);
			} else {
				webResponse = HttpUtil.sendPostCommand(getApplicationContext(),
						url, lstValue);
			}

			JSONObject objWebRet = new JSONObject();

			try {
				if (webResponse != null) {
					objWebRet.put("status", "0");
					objWebRet.put("return_data", webResponse);
				} else {
					objWebRet.put("status", "1");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			objArray.put(objWebRet);

			if (objArray != null) {
				Message msg = mHandler
						.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
				msg.obj = objArray;
				Bundle bundle = new Bundle();
				bundle.putString("type", queryType);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * Audio
	 */
	private static final int MSG_BEGIN_OF_SPEECH = 1;
	private static final int MSG_BUFFER_RECEIVERD = 2;
	private static final int MSG_END_OF_SPEECH = 3;
	private static final int MSG_ERROR = 4;
	private static final int MSG_RESULTS = 5;
	private static final int MSG_UI_INIT = 16;

	protected AudioManager mAudioManager = null;
    private static OnAudioFocusChangeListener mAudioFocusChangeListener = null;
	public static SpeechRecognizer mSpeechRecognizer = null;
	protected IRecognizeListener mRecognizerListener = null;

	/**
	 * Location
	 */
	protected WifiLocation mWifiLocation;
	public static Point mCurLocation = new Point(0, 0);

	/**
	 * Bluetooth
	 */
	protected HBluetooth mBlueTooth;

	/**
	 * Logo Floating Window
	 */
	public static final String FLOATLOGO_START_CAPTURE_ACTION = "com.viash.voice_assistant.floatlogo.STARTCAPTURE";
	public static final String FLOATLOGO_STOP_CAPTURE_ACTION = "com.viash.voice_assistant.floatlogo.STOPCAPTURE";
	private boolean isFloatLogo = false;

	/**
	 * Third-party
	 */

	private boolean isThirdParty = false;
	private boolean isInitSentence = false;
	private String mCurrentApInfo = "";
	private List<Message> thirdMsgQueue = new ArrayList<Message>();
	private VIAApplication application;
	BroadcastReceiver mReceiver = null;

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate() {
		if (DEBUG)
			Log.d(TAG, "onCreate");

		application = (VIAApplication) getApplicationContext();
		mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;
		mServerResponsed = false;
		mIsCharging = BatteryUtil.isCharging(this);
		Log.i(TAG, "IsCharging:" + mIsCharging);

		SavedData.init(getApplicationContext());
		initWifiLocation();
		initHandler();
		mHandler.postDelayed(new InitData(), 100);
		// new Thread(new InitData()).start();

		// Add an Observer for Contacts
		myContentObserver = new ContactContentObserver(mHandler, this);
		this.getContentResolver().registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, myContentObserver);

		LogRecordData.init(MachineUtil.getMachineId(this)); // LeoLi

		initBroadcastReceiver();
		new AppListChangedReceiver(this, mHandler);
		SmsReceiver.setHandler(mHandler);
		IncomingCallReceiver.setHandler(mHandler);
		
		if (Build.VERSION.SDK_INT >= 8) {
			if (mAudioManager == null) {
				mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			}
			mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
				
			    @TargetApi(Build.VERSION_CODES.FROYO)
				public void onAudioFocusChange(int focusChange) {  
			        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			        	//TODO
			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {  
			        	//TODO
			        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
			        	//TODO
			        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) { 
			        	//TODO
			        }  
			    }  
			}; 			
		}
		mStarted = true;
		//initSms();
	}
	
	//protected SmsReceiver mSmsReceiver;
	
	/*protected void initSms() {
		mSmsReceiver = new SmsReceiver();
		IntentFilter filter = new IntentFilter(com.viash.voicelib.data.AppData.SENT_SMS_ACTION);
		filter.addAction(com.viash.voicelib.data.AppData.RECEIVE_SMS_ACTION);
		filter.setPriority(2147483647);
		SmsReceiver.setHandler(mHandler);
		registerReceiver(mSmsReceiver, filter);
	}*/

	private void notifyChargeStatus(boolean isCharging) {
		mIsCharging = isCharging;
	}

	private void initBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					NetworkInfo networkInfo = intent
							.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					if (networkInfo != null) {
						Log.e(TAG, "Network State changed---available="
								+ networkInfo.isAvailable() + " connected="
								+ networkInfo.isConnected());
						if ((!networkInfo.isAvailable())
								|| (!networkInfo.isConnected())) {
							if (mSocketUtil != null
									&& mSocketUtil.isConnected()) {
								mSocketUtil.stopCommunication();
								serverIsDisconnected(true);
							}
						}
					}
				} else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
					notifyChargeStatus(true);
				} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
					notifyChargeStatus(false);
				}
			}

		};
		registerReceiver(mReceiver, filter);
	}

	private void unInitBroadcastReceiver() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent != null) {
			if (OlaAppWidgetProvider.START_CAPTURE_ACTION.equals(intent
					.getAction())) {
				widgetStartCapture();
				return;
			} else if (VoiceSdkServiceInterface.THIRDPARTY_START_CAPTURE_ACTION.equals(intent
					.getAction())) {
				if (DEBUG)
					Log.i(TAG, "THIRDPARTY_START_CAPTURE_ACTION");
				thridStartCapture();
				return;
			} else if (VoiceSdkServiceInterface.THIRDPARTY_STOP_CAPTURE_ACTION
					.equals(intent.getAction())) {
				if (DEBUG)
					Log.i(TAG, "THIRDPARTY_STOP_CAPTURE_ACTION");
				thirdStopCapture();
				return;
			} else if (VoiceSdkServiceInterface.THIRDPARTY_CLOSE_COMMUNICATIONVIEW_ACTION.equals(intent
					.getAction())) {
				application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			} else if (FLOATLOGO_START_CAPTURE_ACTION
					.equals(intent.getAction())) {
				isFloatLogo = true;
				logoStartCapture();
			} else if (FLOATLOGO_STOP_CAPTURE_ACTION.equals(intent.getAction())) {
				logoStopCapture();
			} else if (VoiceSdkServiceInterface.THIRDPARTY_CLOSED.equals(intent.getAction())) {
				application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
			}
		}
		//IncomingCallReceiver.setHandler(mHandler);
		if (mSocketUtil == null)
			initCommunication();
	}

	private void logoStopCapture() {
		if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) {
			mSpeechRecognizer.stopRecognize();
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MSG_END_OF_RECORD), 0);

			if (cMessenger.get(VOICE_ASSISTANT_PACKAGE) != null)
				isFloatLogo = false;

			if (DEBUG)
				Log.i(TAG, "stop recognize by logo");
		} else {
			if (DEBUG)
				Log.e(TAG, "mSpeechRecognizer error.");

			isFloatLogo = false;
		}
	}

	private void logoStartCapture() {
		if (mSpeechRecognizer != null && !mSpeechRecognizer.isRecognizing()) {
			if (mServerState == MsgConst.STATE_SERVER_CONNECTED) {
				if (DEBUG)
					Log.i(TAG, "start recognize by logo");

				if (Tts.isPlaying()) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
					startCapture(TTS_END_TIME);
				} else {
					startCapture();
				}
			} else {
				if (DEBUG)
					Log.i(TAG, "Reconnect to server");

				if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED)
					initCommunication();
			}
		} else {
			if (DEBUG)
				Log.e(TAG, "mSpeechRecognizer not init success.");
		}
	}

	private void thirdStopCapture() {
		if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) {
			mSpeechRecognizer.stopRecognize();
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MSG_END_OF_RECORD), 0);

			if (DEBUG)
				Log.i(TAG, "stop recognize by third-party");
		} else {
			if (DEBUG)
				Log.e(TAG, "mSpeechRecognizer error.");

			application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
			isThirdParty = false;
		}
	}

	private void thridStartCapture() {
		if (mSpeechRecognizer != null && !mSpeechRecognizer.isRecognizing()) {
			if (mServerState == MsgConst.STATE_SERVER_CONNECTED) {
				if (DEBUG)
					Log.i(TAG, "start recognize by third-party");

				isThirdParty = true;
				if (Tts.isPlaying()) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
					startCapture(TTS_END_TIME);
				} else {
					startCapture();
				}
			} else {
				if (DEBUG)
					Log.i(TAG, "Reconnect to server");

				if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED)
					initCommunication();

				application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
				sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED);
			}
		} else {
			if (DEBUG)
				Log.e(TAG, "mSpeechRecognizer not init success.");
		}
	}

	private void widgetStartCapture() {
		if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) {
			mSpeechRecognizer.stopRecognize();
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MSG_END_OF_RECORD), 0);

			if (DEBUG)
				Log.i(TAG, "stop recognize by hand");
		} else {
			if (!isControlFromWidget) {
				isControlFromWidget = true;
				if (mServerState != MsgConst.STATE_SERVER_CONNECTED) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);

					if (DEBUG)
						Log.i(TAG, "Reconnect to server");
					notifyClientState(MsgConst.UI_STATE_INITED);

					if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED)
						initCommunication();
				} else {
					if (DEBUG)
						Log.i(TAG, "start recognize by hand");

					if (Tts.isPlaying()) {
						Tts.stop(Tts.TTS_NORMAL_PRIORITY);
						startCapture(TTS_END_TIME);
					} else {
						startCapture();
					}
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (DEBUG)
			Log.d(TAG, "onBind) package name: "
					+ intent.getComponent().getPackageName());
		cMessenger.clear();
		return mMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (DEBUG)
			Log.d(TAG, "onUnbind) package name: "
					+ intent.getComponent().getPackageName());
		// cMessenger.remove(intent.getComponent().getPackageName());
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		if (DEBUG)
			Log.d(TAG, "onDestroy");
		super.onDestroy();

		mStarted = false;
		if (myContentObserver != null) {
			getContentResolver().unregisterContentObserver(myContentObserver);
		}
		unInitBroadcastReceiver();
		// unInitAppsIsUpdateReceiver();

		if (mSocketUtil != null)
			mSocketUtil.stopCommunication();

		if (mWifiLocation != null)
			mWifiLocation.close();

		if(mSpeechRecognizer != null)
		{
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();			
			}			

			mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}

		/*if (mSmsReceiver != null)
			unregisterReceiver(mSmsReceiver);*/
		
		cMessenger.clear();
		LogRecordData.unInit();
		//startService(new Intent(VoiceAssistantService.this, VoiceAssistantService.class));
	}

	@SuppressLint("HandlerLeak")
	protected void initHandler() {
		mHandler = new Handler() {
			@TargetApi(Build.VERSION_CODES.FROYO)
			@Override
			public void handleMessage(Message msg) {
				if (DEBUG)
					Log.i(TAG, "msg id: " + msg.what);

				//Bundle bundle = new Bundle();

				switch (msg.what) {
				case MsgConst.MSG_DATA_FROM_SERVER:	
					/**/
					MsgRaw msgRaw = (MsgRaw) msg.obj;
					processServerMsg(msgRaw);
					break;
				case MsgConst.MSG_SERVER_CONNECTING:
					sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_CONNECTING);
					if(IncomingCallShareState.isIncomgingCall()){			
					}else
						CustomToast.makeToast(VoiceAssistantService.this,
								getString(R.string.connecting));
								//Toast.LENGTH_SHORT).show();
					if (isFloatLogo) {
						logoStartCapture();
					}
					break;
				case MsgConst.MSG_SERVER_CONNECTED:
					serverIsConnected();					
					break;
				case MsgConst.MSG_SERVER_DISCONNECTED:
					serverIsDisconnected(true);
					break;
				case MsgConst.MSG_DATA_FROM_TEXT:
					mInputType = 1;
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				case MsgConst.MSG_DATA_FROM_VOICE:
					String sVoice = (String) msg.obj;
					// Leo Begin
					Log.i(TAG, "MSG_DATA_FROM_VOICE " + sVoice);
					// Leo End
					if (sVoice != null) {
						if (DEBUG)
							Log.d(TAG, "data from voice: " + sVoice);

						if (isControlFromWidget || isFloatLogo) {
							Intent intent = new Intent(
									VoiceAssistantService.this, GuideActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							getApplicationContext().startActivity(intent);
						}
						processVoiceMsg(sVoice);			
						if(IncomingCallShareState.isIncomgingCall())
						{
							Intent intent = new Intent();
							intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD);
							intent.putExtra("startRecord", IncomingCallShareState.START_PLAY_TTS_DELAY_SECONDS);
							sendBroadcast(intent);
						}
					} else {
						if (DEBUG)
							Log.d(TAG, "data from voice: is null");
						if (isThirdParty) {
							application
									.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
							isThirdParty = false;
						}
						isFloatLogo = false;
					}
					mInputType = 0;
					break;
				case MSG_UI_IDLE:
					notifyClientState(MsgConst.UI_STATE_UNINIT);
					break;
				case MSG_UI_INIT:
					notifyClientState(MsgConst.UI_STATE_INITED);
					break;
				case MSG_UI_IN_RECORDING:
					notifyClientState(MsgConst.UI_STATE_SPEAKING);
					break;
				case MSG_UI_STOP_RECORDING:
					notifyClientState(MsgConst.UI_STATE_RECOGNIZING);
					break;
				case MSG_TTS_PLAY_END:
					if (Build.VERSION.SDK_INT >= 8 && mAudioManager != null) {
						mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
					}
					if(GlobalData.getNewAssistantAcitivityRunFlag() == false)
						break;
					if (!playTts(++mTtsIndex)) {
						if (SavedData.isAllowWakeupByAudio() && mIsCharging) {
							if (SavedData.getmAutoStartRecord()
									&& mNeedAnswer
									&& NewAssistActivity.layout_voice
											.getVisibility() == View.VISIBLE) {
								setWakeupByAudio(MsgConst.WAKEUP_AUDIO_ENABLE_NOWAIT);
								mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
							} else {
								setWakeupByAudio(MsgConst.WAKEUP_AUDIO_ENABLE_WAIT);
							}
							startCapture(TTS_END_TIME);
						} else {
							if (SavedData.getmAutoStartRecord()
									&& mNeedAnswer
									&& NewAssistActivity.layout_voice
											.getVisibility() == View.VISIBLE) {
								setWakeupByAudio(MsgConst.WAKEUP_AUDIO_DISABLE);
								startCapture(TTS_END_TIME);
							} else if ((mCorpus != null) && mCorpus.mSupport) {
								if (mCorpus.mRunningCorpus) {
									// execute next un-null corpus command
									mCorpus.runCorpus();
								}
								else {
									mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
								}
							}
						}
					}
					break;
				case MSG_USER_DATA_REFRESH:
					byte[] byData = (byte[]) msg.obj;
					int dataType = msg.arg1;
					if (byData != null) {
						byte[] newData = MsgRaw.prepareRawData(
								MsgRaw.COMPRESS_GZ, MsgConst.TS_C_PROMPT,
								byData);
						mSocketUtil.sendMessage(newData, false);
						UserPhoneDataUtil.saveData(VoiceAssistantService.this,
								byData, dataType);
					}
					break;
				case MSG_BEGINNING_OF_RECORD:
					if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
						mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
		                    AudioManager.STREAM_VOICE_CALL,
		                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
					}
					mHandler.sendEmptyMessage(MSG_UI_IN_RECORDING);
					if (isControlFromWidget) {
						OlaAppWidgetProvider.setUpdateImage(R.id.image_mic,
								R.drawable.widget_mic_speaking);
						OlaAppWidgetProvider.updateWidget();
					}
					break;
				case MSG_END_OF_RECORD:
					if (isControlFromWidget) {
						OlaAppWidgetProvider.setUpdateImage(R.id.image_mic,
								R.drawable.widget_mic);
						OlaAppWidgetProvider.updateWidget();
					}
					break;
				case MSG_BEGIN_OF_SPEECH:
					break;
				case MSG_BUFFER_RECEIVERD:
					break;
				case MSG_END_OF_SPEECH:
					mHandler.sendEmptyMessage(MSG_UI_STOP_RECORDING);
					if (isControlFromWidget) {
						OlaAppWidgetProvider.setUpdateImage(R.id.image_mic,
								R.drawable.widget_mic);
						OlaAppWidgetProvider.updateWidget();
					}
					break;
				case MSG_ERROR:
					String error = getResources().getString(R.string.voiceassistantservice_vr_error_1);
					if (msg.arg1 == SpeechRecognizer.ERR_MIC_CREATE)
						error = getResources().getString(R.string.voiceassistantservice_vr_error_2);
					if (IncomingCallShareState.isIncomgingCall()) 
					{
						/*if(msg.arg1 == 1)
						{
							Intent intent = new Intent();
							intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD);
							intent.putExtra("startRecord", IncomingCallShareState.START_PLAY_TTS_WITHOUT_DELAY);
							sendBroadcast(intent);
						}*/
					} 
					else
					{
						CustomToast.makeToast(VoiceAssistantService.this, error);
								//Toast.LENGTH_SHORT).show();
						// voiceBtn.setEnabled(true);
						mHandler.sendEmptyMessage(MSG_UI_INIT);
						isControlFromWidget = false;
						isFloatLogo = false;
						if (isThirdParty) {
							application
									.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
							isThirdParty = false;
						}
						if ((AiTalkShareData.getSpeechStartState() == false)
								&& (SavedData.isAllowWakeupByAudio() && mIsCharging))//when auto VR start, should start a tts tip first. 
						{
							parseTtsData(getResources().getString(R.string.voiceassistantservice_please_say)
									+ AiTalkShareData.recognize_words);
							playTts(0);
						}
					}
					break;
				case MSG_RESULTS:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						mHandler.sendMessageDelayed(mHandler.obtainMessage(
								MsgConst.MSG_DATA_FROM_VOICE, result), 0);
					}

					mHandler.sendEmptyMessage(MSG_UI_INIT);
					// voiceBtn.setEnabled(true);
					break;
				case MsgConst.MSG_BLUETOOTH_FOUND_START:
					mHandler.sendEmptyMessage(MSG_UI_SEARCHING_START);
					break;
				case MsgConst.MSG_BLUETOOTH_FOUND:
					mHandler.sendEmptyMessage(MSG_UI_SEARCHING_FOUND);
					@SuppressWarnings("unchecked")
					List<BluetoothDevice> list = (List<BluetoothDevice>) msg.obj;
					sendBluetoothList(list);
					break;
				case MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED:
					JSONArray objArray = (JSONArray) msg.obj;
					String queryType = msg.getData().getString("type");
					processQueryAnswer(queryType, objArray);
					break;
				case MSG_END_OF_UPLOAD:
					mHandler.sendEmptyMessage(MSG_UI_INIT);
					break;
				case MSG_UPLOAD_ERROR:
					mHandler.sendEmptyMessage(MSG_UI_INIT);
					break;
				case MSG_WAKEN:
					startCapture();
					break;
				case MsgConst.MSG_CONTACT_ADDED:
					long id = msg.arg1;
					notifyServerContactAdded(id);
					break;
				case MsgConst.MSG_CONTACT_MODIFIED:
					notifyServerContactDeleted(msg.arg1);
					notifyServerContactAdded(msg.arg2);
					break;
				case MsgConst.MSG_CONTACT_DELETED:
					notifyServerContactDeleted(msg.arg1);
					break;
				case MsgConst.MSG_APPLIST_CHANGED:
					List<AppInfo> appInfos = AppUtil.findAllApp(
							VoiceAssistantService.this, true);
					JSONObject obj = AppUtil.getJsonObjectOfApps(appInfos);
					sendObjToServer("applist", obj);
					break;
				case MsgConst.MSG_PLAY_VIDEO:
					playVideo(msg.arg1, msg.arg2, (String) msg.obj);
					break;
				// Leo Begin
				case MsgConst.MSG_HANDLE_INCOMING_CALL:
					handleIncomingCall(msg.getData());
					break;
				// Leo End
				case MsgConst.MSG_SERVER_NO_RESPONSE:
					serverResonseTimeout();
					break;	
				case MsgConst.MSG_START_CAPTURE:
					if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) 
					{
				        mSpeechRecognizer.stopRecognize();
				        Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_END_OF_RECORD), 0);
					} else 
					{						
						if (Tts.isPlaying()) 
						{
							Tts.stop(Tts.TTS_NORMAL_PRIORITY);
							startCapture(TTS_END_TIME);
						} 
						else
						{
							startCapture();
						}
					}
					break;
				case MsgConst.MSG_STOP_CAPTURE:
					if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) 
					{
				        mSpeechRecognizer.stopRecognize();
				        Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_END_OF_RECORD), 0);				        
					}
					break;
				case MsgConst.CLIENT_ACTION_ABORT_VR_BY_PHONE_OR_SMS:// Enter setting screen stop VR.
					if (mSpeechRecognizer != null)
						mSpeechRecognizer.abort();
					break;					
				default:
					Message newMsg = new Message();
					newMsg.copyFrom(msg);
					sendBackToClient(newMsg);
					break;
				}
			}
		};
	}

	private void handleIncomingCall(Bundle bundle) {
		String action = bundle.getString("action");
		if (action.equals("0")) {// answer the phone.
			Intent intent1 = new Intent();
			intent1.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_ANSWER_ACTION);
			intent1.putExtra("answerAction", IncomingCallShareState.ANSWER_RING);
			sendBroadcast(intent1);
		} else {// hang off.
			Intent intent1 = new Intent();
			intent1.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_ANSWER_ACTION);
			intent1.putExtra("answerAction", IncomingCallShareState.END_CALL);
			sendBroadcast(intent1);
		}

	}
	
	/**
	 * Contact
	 */
	protected void notifyServerContactAdded(long id) {
		ContactInfo infoContact = ContactUtil.findContactById((int) id);
		if (infoContact != null) {
			List<ContactInfo> lstInfo = new ArrayList<ContactUtil.ContactInfo>();
			lstInfo.add(infoContact);

			JSONObject obj = ContactUtil.getJsonObjectOfContacts(lstInfo);
			sendObjToServer("add_contact", obj);
		}
	}

	protected void notifyServerContactDeleted(long id) {
		JSONArray array = new JSONArray();
		JSONObject objId = new JSONObject();
		try {
			objId.put("id", id);
			array.put(objId);
			JSONObject obj = new JSONObject();
			obj.put("contact_ids", array);
			sendObjToServer("delete_contact", obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void sendSelectionToServer(int option, int type) {
		if (Tts.isPlaying()) {
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);
		}
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", "selection");
			JSONObject dataObj = new JSONObject();
			dataObj.put("answer", option);
			appendModificationInfo(dataObj);
			jsonObj.put("data", dataObj);
			Log.i(TAG, jsonObj.toString());

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				mSocketUtil.sendMessage(data, true);
				CommunicationData commData = new CommunicationData(
						DataConst.FROM_MIC);
				if (type == 0) {
					for (int i = 0; i < 10; i++) {
						if (((option >> i) & 1) == 1) {
							commData.setDisplayText(getString(R.string.select)
									+ (i + 1));
							break;
						}
					}
				} else {
					if (option == 0)
						commData.setDisplayText(this.getResources().getString(
								R.string.no));
					else
						commData.setDisplayText(this.getResources().getString(
								R.string.yes));
				}
				sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
						commData);

				// mServerResponsed = false;
				setWatiServerResponse(true);

				notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		switchToSpecialStatus(SPECIAL_STATUS_NORMAL);
	}
	
	protected void processVoiceMsg(String sVoice) {
		JSONObject jsonObj = new JSONObject();
		try {
			JSONObject jsonSpeech = new JSONObject();
			jsonSpeech.put("text", sVoice);
			jsonSpeech.put("input_type", mInputType);
			appendLocationInfo(jsonSpeech);
			jsonObj.put("data_type", "stt");
			jsonObj.put("data", jsonSpeech);
			appendModificationInfo(jsonSpeech);
			appendTtsHint(jsonSpeech);
			// appendTestSdkCommand(jsonSpeech);
			appendSdkCommand(jsonSpeech);
			
			if(IncomingCallShareState.isIncomgingCall()){
				JSONObject objSpecialEvent = new JSONObject();
				objSpecialEvent.put("event_type", 1);// 1.incoming call 2,receive message.				
				if(IncomingCallShareState.getName().equals(IncomingCallShareState.UNKNOWN_NAME))
				{
					objSpecialEvent.put("caller_name",null);
				}
				else
				{	
				    objSpecialEvent.put("caller_name",
						IncomingCallShareState.getName());
				}
				objSpecialEvent.put("caller_number",
						IncomingCallShareState.getNumber());
				jsonSpeech.put("special_event", objSpecialEvent);
			}
			
			if(SmsReceiver.isReplySMS()){
				SmsReceiver.setReplySMS(false);
				JSONObject objSpecialEvent = new JSONObject();
				objSpecialEvent.put("event_type", 2);// 1.incoming call 2,receive message.				
				objSpecialEvent.put("caller_name",
						SmsReceiver.getContactName());
				objSpecialEvent.put("caller_number",
						SmsReceiver.getPhoneNumber());
				jsonSpeech.put("special_event", objSpecialEvent);
			}

			Log.i(TAG, jsonObj.toString());
			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				if (isControlFromWidget || isFloatLogo) {
					msgToServerQueue.add(data);
				} else {
					mSocketUtil.sendMessage(data, true);
				}

				CommunicationData commData = new CommunicationData(
						DataConst.FROM_MIC);
				commData.setDisplayText(sVoice);
				if (!isThirdParty) {
					sendBackToClient(
							MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
							commData);
					// mAdapter.addData(commData);
				} else {
					clearMessageQueue();
					addMessageQueue(
							MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
							commData);
				}

				// mServerResponsed = false;
				setWatiServerResponse(true);

				notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("SdCardPath")
	protected void appendTestSdkCommand(JSONObject objSpeech) {
		JSONObject obj = new JSONObject();
		try {
			objSpeech.put("current_app_info", obj);
			JSONArray array = new JSONArray();
			array.put("Play");
			array.put("Stop");
			array.put("Selection");
			obj.put("cmdlist", array);

			JSONObject objSub = new JSONObject();
			objSub.put("url", "/mnt/sdcard/test.mp3");
			objSub.put("type", "music");
			obj.put("curfocus", objSub);

			array = new JSONArray();
			
			array.put(getString(R.string.voiceassistantservice_idle_star1));
			array.put(getString(R.string.voiceassistantservice_idle_star2));
			obj.put("curlist", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void appendSdkCommand(JSONObject obj) {
		if(mCurrentApInfo.length() > 0)
		{
			if (isThirdParty) {
				try {
					JSONObject currentAppInfoObj = new JSONObject(mCurrentApInfo);
					obj.put("current_app_info", currentAppInfoObj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void appendTtsHint(JSONObject obj) {
		if (mTtsIndex < mLstTtsFlag.size()) {
			if (mLstTtsFlag.get(mTtsIndex) != null) {
				try {
					obj.put("tts_position", mLstTtsFlag.get(mTtsIndex));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void appendModificationInfo(JSONObject obj) {
		if (mLatestServerData != null) {
			if (mLatestServerData.isModified()) {
				List<BaseData> lsvData = mLatestServerData.getLstData();
				if (lsvData == null) {
					return;
				}
				for(BaseData data: lsvData) {
					if (data instanceof ConfirmData && data.isModified()) {
						String content = ((ConfirmData)data).getSmsData().getContent();
						if (mSpecialStatus == SPECIAL_STATUS_SMS) {
							JSONObject objSms = new JSONObject();
							try {
								objSms.put("type", "sms");
								objSms.put("sms_content", content);
								obj.put("appendix", objSms);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	protected void appendLocationInfo(JSONObject obj) {
		Point pt = mWifiLocation.getLocation();
		if (pt != null) {
			if (!mCurLocation.equals(pt) || isControlFromWidget) {
				mCurLocation = new Point(pt);
				JSONObject objLocation = mWifiLocation.getJsonOfLocation();
				SavedData.setmLastLocation(objLocation.toString());
				if (objLocation != null)
					try {
						objLocation.put("is_last", "0");
						obj.put("location", objLocation);
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
		} else {
			String lastLocation = SavedData.getmLastLocation();
			if (lastLocation != null && lastLocation.length() > 0) {
				try {
					JSONObject objLocation = new JSONObject(lastLocation);
					if (objLocation != null) {
						objLocation.put("is_last", "1");
						obj.put("location", objLocation);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void processServerMsg(MsgRaw msgRaw) {
		byte[] data = null;
		switch (msgRaw.getmId()) {
		case MsgConst.TS_S_REQLOGIN:
			String user = MachineUtil.getMachineId(this);
			String[] userInfo = UserData.getUserInfo(this);
			MsgLogin msgLogin = new MsgLogin(user, userInfo[0], userInfo[1], "");

			byte[] client_token = new byte[] { 0x0B, 0x64, 0x7B, 0x01, 0x1B,
					(byte) 0xAF, 0x4e, (byte) 0xe2, (byte) 0x81, 0x65, 0x23,
					(byte) 0x97, 0x52, (byte) 0xA3, (byte) 0xB7, 0x6F, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			// byte[] client_token = new byte[] {0x60, (byte) 0xEF, (byte) 0xB1,
			// 0x42, (byte) 0xF1, (byte) 0xFA, 0x45, 0x08,
			// (byte) 0x95, (byte) 0xDC, (byte) 0xC3, 0x2E,
			// (byte) 0xC4, 0x4B, (byte) 0xCD, 0x53,
			// 0x00, 0x00, 0x00, 0x00,
			// 0x00, 0x00, 0x00, 0x00,
			// 0x00, 0x00, 0x00, 0x00,
			// 0x00, 0x00, 0x00, 0x00
			// };

			if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG)
				client_token[16] = 1;

			msgLogin.setMiscData(client_token);
			data = msgLogin.prepareRawData();

			Log.i(TAG, "TS_S_REQLOGIN:" + System.currentTimeMillis());
			if (data != null)
				mSocketUtil.sendMessage(data, true);
			break;
		case MsgConst.TS_S_RSPLOGIN:
			Log.i(TAG, "TS_S_RSPLOGIN:" + System.currentTimeMillis());
			MsgLoginResponse msgResponse = new MsgLoginResponse(msgRaw);
			if (!msgResponse.isLogin()) {
				UserData.exit(this);
			}
			GlobalData.setUserLoggedin(msgResponse.isLogin());
			mHandler.sendEmptyMessage(MsgConst.CLIENT_ACTION_UPDATA_USER_LOG_STATUS);

			sendUserDataToServer();
			requestUserLevel();
			// mServerResponsed = true;
			setWatiServerResponse(false);

			if (mProcessState != MsgConst.UI_STATE_SPEAKING)
				notifyClientState(MsgConst.UI_STATE_INITED);
			sendBackToClient(MsgConst.SERVICE_ACTION_CLOSE_SPLASH);
			break;
		case MsgConst.TS_S_PROMPT:
			mServerTimeOut = SERVER_RESPONSE_TIMEOUT;
			MsgAnswer msgAnswer = new MsgAnswer(msgRaw);
			processServerAnswer(msgAnswer);
			break;
		case MsgConst.TS_S_DUMP:
			MsgDumpResponse msgDump = new MsgDumpResponse(msgRaw);
			String fileName = msgDump.saveToFile(this);

			if (fileName != null) {
				CustomToast.makeToast(this,
						getString(R.string.save_file_in) + fileName);
						//Toast.LENGTH_LONG).show();
			} else {
				CustomToast.makeToast(this, getString(R.string.save_file_failed));
						//Toast.LENGTH_SHORT).show();
			}
			break;
		case MsgConst.TS_S_QUERY:
			setWatiServerResponse(false);
			MsgServerQuery msgQuery = new MsgServerQuery(msgRaw);
			processServerQuery(msgQuery);
			break;

		case MsgConst.TS_S_CHECK_STATUS:
			mHandler.removeMessages(MsgConst.MSG_SERVER_NO_RESPONSE);
			break;
		default:
			break;
		}
	}

	protected void processServerQuery(MsgServerQuery msgQuery) {
		JSONObject obj = msgQuery.getQueryData();
		if (obj != null) {
			String queryType = obj.optString("type");
			if (queryType != null) {
				new Thread(new PorcessServerqueryThread(queryType, obj))
						.start();
				// if(objArray != null)
				// processQueryAnswer(queryType, objArray);
			}
		}
	}

	protected void processServerAnswer(MsgAnswer msgAnswer) {
		JSONObject obj = msgAnswer.getJsonData();
		if (obj != null) {
			String data_type = obj.optString("data_type");

			
			if (data_type != null && data_type.equals("answer")) {
				sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_RESPONSE, obj);
			} else {
				switchToSpecialStatus(SPECIAL_STATUS_NORMAL);

				CommunicationData commData = msgAnswer
						.getCommunicationData(this);
				mLatestServerData = commData;
				if (commData != null) {
					boolean needStartOla = false;
					List<BaseData> lstData = commData.getLstData();
					if (isThirdParty) {
						if (lstData != null) {
							needStartOla = true;
							for (BaseData baseData : lstData) {
								if (baseData instanceof SdkCommandData) {
									needStartOla = false;
									processSpecialSdkCommandData((SdkCommandData) baseData);
								}
							}
							if (!isInitSentence && needStartOla) {
								// start ola
								// Intent intent = new
								// Intent(VoiceAssistantService.this,
								// GuideAct.class);
								// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								// getApplicationContext().startActivity(intent);

								// add queue
								addMessageQueue(
										MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
										commData);

								// start communication floating windows
								application
										.createView(VIAApplication.THIRD_COMMUNICATION_VIEW);

								// set data to communication view
								purgeThirdMessageQueue();
							}
						}

						if (!(!isInitSentence && needStartOla)) {
							// release Floating Window
							application
									.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
						} else {
							// set to init button
							application.getThridVoiceFV().initVoiceButton();
						}
					} else {
						// if(!isThirdParty || needStartOla){
						if (mClientNeedFirstPrompt || !isInitSentence) {
							sendBackToClient(
									MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA,
									commData);
							if (lstData != null) {
								String tts = "";

								mNeedAnswer = false;
								boolean isSilentInfoData = false;
								boolean isNeedUsePriority = false;
								for (BaseData baseData : lstData) {									
									if (!mNeedAnswer) {
										if (commData.isSilentInfoMsg()) {
											mNeedAnswer = true;
											isSilentInfoData = true;
										}
										else {
											mNeedAnswer = baseData.isDataNeedAnswer();
										}
									}
									if (baseData.getTtsString() != null)
										tts += baseData.getTtsString();

									baseData.doAction(this, mHandler);
									String actionResult = baseData
											.getActionResult(this);
									if (actionResult != null) {
										tts += actionResult;
									}

									if (baseData instanceof ConfirmData) {
										processSpecialConfirmData((ConfirmData) baseData);
									}

									if (baseData instanceof PreFormatData) {
										processSpecialPreformatData((PreFormatData) baseData);
										{
											int type = ((PreFormatData)baseData).getmDataType();
											if((type == PreFormatData.TYPE_HTML) || (type == PreFormatData.JSON_POEM)) {
												isNeedUsePriority = true;
											}
										}	
									}
									
									if(baseData instanceof OptionData)
									{
										int type = ((OptionData)baseData).getOptionId();
										if(type == OptionData.OPTION_NEWS_NAME) {
											isNeedUsePriority = true;	
										}
									}
								}

								if (tts.length() > 0) {
									parseTtsData(tts);
									if(isNeedUsePriority){
										playTts(0,Tts.TTS_NORMAL_PRIORITY);
									}
									else{
										playTts(0);
									}
								} else {
									if (SavedData.getmAutoStartRecord()
											&& mNeedAnswer && !isSilentInfoData
											&& NewAssistActivity.layout_voice
													.getVisibility() == View.VISIBLE) {
										startCapture();
									}
								}
							}
						}
					}
				}

				// mServerResponsed = true;
				setWatiServerResponse(false);
				isThirdParty = false;
				isInitSentence = false;

				if (mProcessState != MsgConst.UI_STATE_SPEAKING)
					notifyClientState(MsgConst.UI_STATE_INITED);
			}
		} else {
			CustomToast.makeToast(this, getString(R.string.voiceassistantservice_server_data_error));//, Toast.LENGTH_SHORT).show();
		}
	}
	
	protected boolean playTts(int index) {

		return playTts(index, Tts.TTS_LOW_PRIORITY);

	}
	@TargetApi(Build.VERSION_CODES.FROYO)
	protected boolean playTts(int index,int priority) {
		boolean ret = false;
		if (SavedData.getLockMessage()!= null) {
			return ret;
		}
		if(GlobalData.getNewAssistantAcitivityRunFlag() == false) {
			return ret;
		}
		if (IncomingCallShareState.isIncomgingCall()){
			return ret;
		}
		if (SmsReceiver.isReplySMS()) {
			return ret;
		}

		if (index < mLstTts.size()) {
			if (mLstTts.get(index) != null) {
				Tts.ITtsListener listener = new Tts.ITtsListener() {
					@Override
					public void onPlayEnd() {
						mHandler.sendEmptyMessage(MSG_TTS_PLAY_END);
					}
				};
				if(IncomingCallShareState.isIncomgingCall()){				
				}else
				if (SavedData.getVoiceSetting()) {
					if (mSpeechRecognizer != null
							&& mSpeechRecognizer.isRecognizing()) {
						Tts.playText(this, "", listener,priority);
						mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
						if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
							mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
			                    AudioManager.STREAM_VOICE_CALL,
			                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
						}
						return true;
					}
					Tts.playText(this, mLstTts.get(index), listener,priority);
					mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
					if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
						mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
		                    AudioManager.STREAM_VOICE_CALL,
		                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
					}
				}
				ret = true;
			}
		}

		return ret;
	}

	protected void parseTtsData(String ttsData) {
		// ttsData = "this is a |*com1*|part1|*com1*|part2|*com1*|part3";
		ttsData = ttsData.replace(getString(R.string.voiceassistantservice_ola), getString(R.string.voiceassistantservice_ola_nick));
		int indexStart = 0;
		int indexEnd = 0;
		int indexCurrent = 0;
		String commentStart = "[*";
		String commentEnd = "*]";
		mTtsIndex = 0;
		mLstTts.clear();
		mLstTtsFlag.clear();

		indexStart = ttsData.indexOf(commentStart, indexCurrent);
		if (indexStart > 0) {
			mLstTts.add(ttsData.substring(indexCurrent, indexStart));
			mLstTtsFlag.add(null);
			indexCurrent = indexStart;
		} else if (indexStart == -1) {
			mLstTts.add(ttsData.substring(indexCurrent));
			mLstTtsFlag.add(null);
			indexCurrent = ttsData.length();
		}

		while (indexCurrent < ttsData.length()) {
			indexStart = indexCurrent;
			indexEnd = ttsData.indexOf(commentEnd, indexStart + 2);
			if (indexEnd == -1) {
				mLstTts.add(ttsData.substring(indexCurrent));
				mLstTtsFlag.add(null);
				break;
			} else {
				indexCurrent = ttsData.indexOf(commentStart, indexEnd + 2);
				mLstTtsFlag.add(ttsData.substring(indexStart + 2, indexEnd));
				if (indexCurrent != -1) {
					mLstTts.add(ttsData.substring(indexEnd + 2, indexCurrent));
				} else {
					mLstTts.add(ttsData.substring(indexEnd + 2));
					break;
				}
			}

		}
	}

	protected void processSpecialSdkCommandData(SdkCommandData sdkData) {
		if (DEBUG)
			Log.d(TAG,
					"SdkCommand: " + sdkData.getCommand() + " - ["
							+ sdkData.getParam1() + "]");
		// send back to third-party
		Bundle bundle = new Bundle();
		bundle.putString("commandname", sdkData.getCommand());
		bundle.putString("parameter", sdkData.getParam1());
		sendBackToClient(MsgConst.SERVICE_ACTION_SDKCOMMAND_RESPONSE, bundle);
	}

	protected void processSpecialConfirmData(ConfirmData confirmData) {
			
		if (confirmData.getType() == ConfirmData.TYPE_SMS) {
			switchToSpecialStatus(SPECIAL_STATUS_SMS);
		}
	}

	protected void processSpecialPreformatData(PreFormatData preformatData) {
		if (preformatData.getmDataType() == PreFormatData.JSON_SMS) {
			SMSJsonData jsonSms = (SMSJsonData) preformatData.getJsonData();
			if (jsonSms != null && jsonSms.mLstSms != null) {
				List<Long> lstSmsIds = new ArrayList<Long>();
				for (SmsData smsData : jsonSms.mLstSms) {
					if (smsData.getmType() == SmsData.TYPE_UNREAD) {
						lstSmsIds.add(smsData.getmId());
					}
				}

				if (lstSmsIds.size() > 0) {
					CallUtil.updateSms2Read(this, lstSmsIds);
				}
			}
		}
	}

	protected void sendUserDataToServer() {
		JSONObject obj;

		mServerTimeOut = SERVER_RESPONSE_TIMEOUT + 5;
		JSONObject clientProperty = ClientPropertyUtil.getJsonObject(this);
		String nickname = AppData.getNickname(this);
		if (nickname != null && nickname.length() > 0) {
			try {
				clientProperty.put("client_nickname", nickname);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sendObjToServer("client_property", clientProperty);

		if (SAVE_USER_PHONE_DATA) {
			UserPhoneDataUtil.setStringTypeInData(true);
			byte[] byData = UserPhoneDataUtil.initSavedData(this,
					UserPhoneDataUtil.DATA_TYPE_APP);
			if (byData != null) {
				byte[] newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
						MsgConst.TS_C_PROMPT, byData);
				mSocketUtil.sendMessage(newData, false);
			}
			UserPhoneDataUtil.startCollectData(this, mHandler,
					MSG_USER_DATA_REFRESH, UserPhoneDataUtil.DATA_TYPE_APP);

			byData = UserPhoneDataUtil.initSavedData(this,
					UserPhoneDataUtil.DATA_TYPE_CONTACT);
			if (byData != null) {
				byte[] newData = MsgRaw.prepareRawData(MsgRaw.COMPRESS_GZ,
						MsgConst.TS_C_PROMPT, byData);
				mSocketUtil.sendMessage(newData, false);
			}
			UserPhoneDataUtil.startCollectData(this, mHandler,
					MSG_USER_DATA_REFRESH, UserPhoneDataUtil.DATA_TYPE_CONTACT);

		} else {
			obj = ContactUtil.getJsonObjectOfContacts(ContactUtil
					.getmLatestContactsInfo());
			sendObjToServer("contact", obj);
			// saveJsonToFile("/mnt/sdcard/contacts.txt", obj);
			obj = AppUtil.getJsonObjectOfApps(AppUtil.getmLatestLstApp());
			sendObjToServer("applist", obj);
		}
	}

	protected void requestUserLevel() {
		if (mKeyClient != null && mKeyClient.equals(VOICE_ASSISTANT_PACKAGE)) {
			mNeedRequestUserLevel = false;
			JSONObject obj = new JSONObject();
			try {
				obj.put("request", 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			sendObjToServer("request_user_level", obj);
			mAskingUserLevel = true;
		}
	}

	protected void sendObjToServer(String dataType, JSONObject dataObj) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", dataType);
			jsonObj.put("data", dataObj);

			Log.i(TAG, jsonObj.toString());

			MsgAsk msgAsk = new MsgAsk(jsonObj);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				if (mSocketUtil == null) {
					initCommunication();
				}
				mSocketUtil.sendMessage(data, false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void serverIsDisconnected(boolean showToast) {
		setWatiServerResponse(false);
		mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

		sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED);
		notifyClientState(MsgConst.UI_STATE_INITED);

		if(IncomingCallShareState.isIncomgingCall())
		{
			/*(Intent intent1 = new Intent();
			intent1.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING);
			sendBroadcast(intent1);*/
		}
		else
		if (showToast) {
			CustomToast.makeToast(VoiceAssistantService.this, this.getString(R.string.connect_failed));
					//Toast.LENGTH_SHORT).show();
		}

		if (isThirdParty) {
			application.releaseView(VIAApplication.THIRD_VOICE_BUTTON_VIEW);
			isThirdParty = false;
		} else {
			sendBackToClient(MsgConst.SERVICE_ACTION_CLOSE_SPLASH);
		}
		
	}

	protected void serverIsConnected() {
		mServerState = MsgConst.STATE_SERVER_CONNECTED;
		isInitSentence = true;

		sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_CONNECTED);
		notifyClientState(MsgConst.UI_STATE_INITED);

		if(IncomingCallShareState.isIncomgingCall()) {
			 /*Intent intent1 = new Intent();
			 intent1.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING);
			 intent1.putExtra("startRecord", IncomingCallShareState.START_PLAY_TTS_WITHOUT_DELAY);
			 sendBroadcast(intent1);*/
		}else
			CustomToast.makeToast(VoiceAssistantService.this,
					getString(R.string.connected));//, Toast.LENGTH_SHORT).show();
		// if is control from widget then auto start to capture
		if (isControlFromWidget) {
			if (Tts.isPlaying()) {
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				startCapture(TTS_END_TIME);
			} else {
				startCapture();
			}
		} 
	}

	protected void serverIsConnecting() {
		sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_CONNECTING);
		notifyClientState(MsgConst.UI_STATE_INITED);
		if(IncomingCallShareState.isIncomgingCall()) {
		}else
			CustomToast.makeToast(VoiceAssistantService.this,
					getString(R.string.connecting));//, Toast.LENGTH_SHORT).show();
	}

	private void sendBackToClient(Message msg) {
		if (DEBUG)
			Log.i(TAG, "sendBackToClient) aid: " + msg.what);

		if (isControlFromWidget || isFloatLogo) {
			// if(msg.what != MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE){
			msgQueue.add(msg);
			if (DEBUG)
				Log.d(TAG, "add to Message Queue.");
			// }
		} else {
			for (Messenger messenger : cMessenger.values()) {
				try {
					if (messenger != null) {
						messenger.send(msg);
						Log.d(TAG, "Message sended.");
					}
				} catch (RemoteException e) {
					cMessenger.remove(messenger);
					e.printStackTrace();
				}
			}
		}
	}

	private void sendBackToClient(int aid) {
		Message msg = new Message();
		msg.what = aid;
		sendBackToClient(msg);
	}

	private void sendBackToClient(int aid, Bundle bundle) {
		Message msg = new Message();
		msg.what = aid;
		msg.setData(bundle);
		sendBackToClient(msg);
	}

	private void sendBackToClient(int aid, Object obj) {
		Message msg = new Message();
		msg.what = aid;
		msg.obj = obj;
		sendBackToClient(msg);
	}

	private void addMessageQueue(int aid, Object obj) {
		Message msg = new Message();
		msg.what = aid;
		msg.obj = obj;
		thirdMsgQueue.add(msg);
	}

	private void clearMessageQueue() {
		thirdMsgQueue.clear();
	}

	private void purgeThirdMessageQueue() {
		if (DEBUG)
			Log.d(TAG, "start purge Third-party Message Queue.");
		application.getThridCommunicationFV().clearData();
		if (thirdMsgQueue.size() > 0) {
			for (int i = 0; i < thirdMsgQueue.size(); i++) {
				// sendBackToClient(thirdMsgQueue.get(i));
				Object obj = thirdMsgQueue.get(i).obj;
				if (obj != null
						&& application.getThridCommunicationFV() != null
						&& obj instanceof CommunicationData) {
					CommunicationData commData = (CommunicationData) obj;
					application.getThridCommunicationFV().setData(commData);
				}

				// process the last communication data to speech
				if (i == (thirdMsgQueue.size() - 1)) {
					if (obj != null && obj instanceof CommunicationData) {
						CommunicationData commData = (CommunicationData) obj;
						List<BaseData> lstData = commData.getLstData();
						if (lstData != null) {
							String tts = "";

							for (BaseData baseData : lstData) {
								if (baseData.getTtsString() != null)
									tts += baseData.getTtsString();
							}

							if (tts.length() > 0) {
								parseTtsData(tts);
								playTts(0);
							}
						}
					}
				}
			}

			/*
			 * Bundle bundle = new Bundle(); bundle.putInt("state",
			 * STATE_INITED);
			 * sendBackToClient(MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE,
			 * bundle); sendBackToClient(MsgConst.SERVICE_ACTION_CLOSE_SPLASH);
			 */
		}
		thirdMsgQueue.clear();
	}

	private void purgeMessageQueue() {
		Log.d(TAG, "start purge Message Queue.");
		if (msgQueue.size() > 0) {
			for (int i = 0; i < msgQueue.size(); i++) {
				sendBackToClient(msgQueue.get(i));
			}

			notifyClientState(MsgConst.UI_STATE_INITED);
			sendBackToClient(MsgConst.SERVICE_ACTION_CLOSE_SPLASH);
		}
		msgQueue.clear();
	}

	private void purgeServerMessageQueue() {
		Log.d(TAG, "start purge server Message Queue.");
		for (int i = 0; i < msgToServerQueue.size(); i++) {
			mSocketUtil.sendMessage(msgToServerQueue.get(i), true);
		}
		msgToServerQueue.clear();
	}

	class InitData implements Runnable {
		@Override
		public void run() {
			if (DEBUG)
				Log.d(TAG, "InitData");
			initVoiceEngine();
			initUserData();
			mCorpus = new Corpus(mHandler);
		}
	}

	private void initUserData() {
		mBlueTooth = new HBluetooth(this, mHandler);
	}

	protected void initCommunication() {
		switchToSpecialStatus(SPECIAL_STATUS_NORMAL);
		mCurLocation = new Point(0, 0);
		if (mSocketUtil != null)
			mSocketUtil.stopCommunication();

		mSocketUtil = new SocketUtil();

		mSocketUtil.setCallbackHandler(mHandler);

		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE) {
			SERVER = SavedData.INTERNET_SERVER_IP;
			PORT = 80;
		} else {
			SERVER = SavedData.getmIP();
			PORT = SavedData.getmPort();
		}
		System.out.println("IP:" + SERVER + "PORT:" + PORT);
		// SERVER = "192.168.1.104";

		// Leo for Debug Begin
		// SERVER = "10.27.254.240";
		// PORT = 8080;//80;//
		// Leo for Debug End

		mSocketUtil.create(this, SERVER, PORT);
		mSocketUtil.startCommunication();
		mServerState = MsgConst.STATE_SERVER_CONNECTING;
		isInitSentence = true;
		Log.i(TAG, "connecting to server " + SERVER);
	}

	protected void switchToSpecialStatus(int status) {
		mSpecialStatus = status;
	}

	protected void initWifiLocation() {
		mWifiLocation = WifiLocation.getInstance(this);
		//mWifiLocation.init(this);
	}

	protected void initVoiceEngine() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initRecognizer();
	}

	protected void initRecognizer() {
		if(mSpeechRecognizer != null)
		{
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();			
			}			

			mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}
		
		mRecognizerListener = new IRecognizeListener() {
			@Override
			public void onResults(String result, String recordFileName) {
				if(IncomingCallShareState.isIncomgingCall()){
					String[] mAnswerCallArray = VoiceAssistantService.this.getResources().getStringArray(R.array.incoming_call_answer_call_commmand);
					String[] mEndCallArray = VoiceAssistantService.this.getResources().getStringArray(R.array.incoming_call_end_call_commmand);
					String action = null;
					for(String str:mEndCallArray)
					{
						if(result.contains(str))
						{
							action = "1";
						}
					}
					if(action == null)
					{
						for(String str:mAnswerCallArray)
						{
							if(result.contains(str))
							{
								action = "0";
							}
						}
					}
					if(action != null)
					{
					    Bundle b = new Bundle();
					    b.putString("action", action);
					    handleIncomingCall(b);
					}
					else
					{
						Intent intent = new Intent();
						intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING);
						sendBroadcast(intent);
					}
					return;
				}
				if (SmsReceiver.isReplySMS()) {
					String[] mSMSNoDataArr =  VoiceAssistantService.this.getResources().getStringArray(R.array.sms_not_reply_command);
					String[] mSMSReplyDataArr =  VoiceAssistantService.this.getResources().getStringArray(R.array.sms_reply_command);
					String[] mSMSPlayDataArr =  VoiceAssistantService.this.getResources().getStringArray(R.array.sms_play_content_command);
					int replyStatus = 0; //1, reply; 2, Play TTS, 3, not reply; 0 other VR command;
					
					for (String str: mSMSPlayDataArr) {
						if (result.contains(str)) {
							Log.i(TAG, "contains Play TTS:  " + str);
							replyStatus = 2;
							break;
						}
					}
					
					if(replyStatus != 2) {
						for (String str: mSMSReplyDataArr) {
							if (result.contains(str)) {
								Log.i(TAG, "contains Reply:  " + str);
								replyStatus = 1;
								break;
							}
						}
					}

					
					if (replyStatus != 0) {					
						for (String str: mSMSNoDataArr) {
							if (result.contains(str)) {
								Log.i(TAG, "contains Not Reply " + str);
								replyStatus = 3;
								break;
							}
						}
					}
					if (replyStatus == 1) {
						Intent intent = new Intent(
								VoiceAssistantService.this, GuideActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplicationContext().startActivity(intent);
						result = mSMSReplyDataArr[0];
						SavedData.setLockMessage(result);
						return;
					}else if (replyStatus == 2){
						SmsReceiver.playSMSContent();
						return;
					}else if (replyStatus == 3){
						SmsReceiver.setReplySMS(false);
						SavedData.setLockMessage(null);
						/*if(SmsReceiver.isServiceNeedStop()) {
							Log.i(TAG, "Need to close the services ");
							stopSelf();
							return;
						}*/
						mHandler.sendEmptyMessage(MSG_UI_INIT);
						return;
					}else {
						SmsReceiver.setReplySMS(false);
						SavedData.setLockMessage(null);
						/*if(SmsReceiver.isServiceNeedStop()) {
							Log.i(TAG, "Need to close the services ");
							stopSelf();
							return;
						}*/
					}
				}
				
				Message msg = mHandler.obtainMessage(MSG_RESULTS, result);
				mHandler.sendMessage(msg);
			}

			@Override
			public void onError(int errCode) {
				Message msg = mHandler.obtainMessage(MSG_ERROR, errCode, 0);
				mHandler.sendMessage(msg);
				mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
				if (SmsReceiver.isReplySMS()){
					SmsReceiver.setReplySMS(false);
					/*if(SmsReceiver.isServiceNeedStop()) {
						Log.i(TAG, "Need to close the services ");
						stopSelf();
						return;
					}*/
				}
				if(IncomingCallShareState.isIncomgingCall())
		        {
		        	Intent intent = new Intent();
					intent.setAction(com.viash.voicelib.data.AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD);
					intent.putExtra("startRecord", IncomingCallShareState.START_PLAY_TTS_WITHOUT_DELAY);
					VoiceAssistantService.this.sendBroadcast(intent);
		        }
			}

			@Override
			public void onEndOfSpeech() {
				mHandler.sendEmptyMessage(MSG_END_OF_SPEECH);
			}

			@Override
			public void onBeginningOfSpeech() {
				mHandler.sendEmptyMessage(MSG_BEGIN_OF_SPEECH);
			}

			@Override
			public void onCancel() {
				mHandler.sendEmptyMessage(MSG_UI_INIT);
				mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_END);
			}

			@Override
			public void onVolumeUpdate(int newVolume) {
				if (isControlFromWidget) {
					int imgSpeakBG;
					switch (newVolume) {
					case 1:
						imgSpeakBG = R.drawable.bg_speak_size01;
						break;
					case 2:
						imgSpeakBG = R.drawable.bg_speak_size02;
						break;
					case 3:
						imgSpeakBG = R.drawable.bg_speak_size03;
						break;
					case 4:
						imgSpeakBG = R.drawable.bg_speak_size04;
						break;
					case 5:
						imgSpeakBG = R.drawable.bg_speak_size05;
						break;
					case 6:
						imgSpeakBG = R.drawable.bg_speak_size06;
						break;
					case 7:
						imgSpeakBG = R.drawable.bg_speak_size07;
						break;
					case 8:
						imgSpeakBG = R.drawable.bg_speak_size08;
						break;
					case 9:
						imgSpeakBG = R.drawable.bg_speak_size09;
						break;
					case 10:
						imgSpeakBG = R.drawable.bg_speak_size10;
						break;
					case 11:
						imgSpeakBG = R.drawable.bg_speak_size11;
						break;
					case 12:
						imgSpeakBG = R.drawable.bg_speak_size12;
						break;
					default:
						imgSpeakBG = 0;
						break;
					}
					OlaAppWidgetProvider.setUpdateImage(R.id.image_volume,
							imgSpeakBG);
					OlaAppWidgetProvider.updateWidget();
				} else if (isFloatLogo) {
					application.getLogoFV().speak(newVolume);
				} else if (isThirdParty) {
					application.getThridVoiceFV().startSpeak();
					application.getThridVoiceFV().speak(newVolume);
				} else {
					Bundle bundle = new Bundle();
					bundle.putInt("volume", newVolume);

					sendBackToClient(
							MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME, bundle);
				}
			}

		};

		mSpeechRecognizer = SpeechRecognizer.getInstance(getApplicationContext());
		if (mSpeechRecognizer.isIsLogined())
			mSpeechRecognizer.setListener(mRecognizerListener);
		else {
			if (mSpeechRecognizer.create()){
				mSpeechRecognizer.setListener(mRecognizerListener);
			}
			else {
				mSpeechRecognizer = null;
			}
		}

		String ttsPath = TtsUtil.copyTtsData(this);
		if (ttsPath != null) {
			if (DEBUG)
				Log.d(TAG, "start to create tts.");
			Tts.create(ttsPath);
			// Tts.setSpeaker(Tts.ivTTS_ROLE_XIAOYAN);
			Tts.setSpeaker(SavedData.getVoiceType());
		}
		/*FilesCopyUtil filesCopy = new FilesCopyUtil(this);
		if (filesCopy.ExistSDCard()) {// SD exist
			String path = "asr";
			String sdPath = Environment.getExternalStorageDirectory() + "/asr";
			File file = new File(sdPath);
			if (file.exists() == false) {
				file.mkdirs();
			}
			if (file.isDirectory()) {
				if (file.listFiles().length < 10) {
					filesCopy.CopyAssets(path, sdPath);

				} else {
					File folamenu = new File(
							Environment.getExternalStorageDirectory()
									+ "/asr/olamenu.bnf");
					if ((folamenu.length() != 148) && (folamenu != null))
						filesCopy.CopyAssets(path, sdPath);
				}

			}
		}*/
	}

	protected void startCapture() {
		startCapture(0);
	}

	protected void startCapture(long waitTime) {
		if (mSpeechRecognizer != null) {
			mSpeechRecognizer.setListener(mRecognizerListener);
			if (!mSpeechRecognizer.isRecognizing()) {
				if (mSpeechRecognizer.startRecognize(waitTime,false)) {
					mHandler.sendMessageDelayed(
							mHandler.obtainMessage(MSG_BEGINNING_OF_RECORD), 0);
				}
			}
		}
	}

	protected void sendBluetoothList(List<BluetoothDevice> lstDevices) {
		JSONArray array = new JSONArray();
		if (lstDevices != null) {
			for (int i = 0; i < lstDevices.size(); i++) {
				JSONObject obj = mBlueTooth.getDeviceJsonObject((BluetoothDevice) lstDevices.get(i));
				array.put(obj);
			}
		}
		processQueryAnswer("bluetooth", array);
	}

	protected void processQueryAnswer(String type, JSONArray array) {
		JSONObject objAnswer = new JSONObject();
		try {
			objAnswer.put("type", type);
			if (array != null)
				objAnswer.put("result", array);
			processQueryAnswer(objAnswer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void processQueryAnswer(JSONObject objAnswer) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("data_type", "answer");
			jsonObj.put("data", objAnswer);

			Log.i(TAG, jsonObj.toString());

			MsgAsk msgAsk = new MsgAsk(jsonObj, MsgConst.TS_C_ANSWER);
			byte[] data = msgAsk.prepareRawData();
			if (data != null) {
				mSocketUtil.sendMessage(data, true);
				setWatiServerResponse(true);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	class PorcessServerqueryThread implements Runnable {
		String queryType;
		JSONObject obj;

		PorcessServerqueryThread(String type, JSONObject obj) {
			this.queryType = type;
			this.obj = obj;
		}

		@Override
		public void run() {
			JSONArray objArray = new JSONArray();

			if (queryType.equalsIgnoreCase("calendar_info")) {
				long start_time = obj.optLong("start_time");
				long end_time = obj.optLong("end_time");
				List<InstanceData> lstCalendar = CalendarUtil.queryCalendar(
						getApplicationContext(), start_time, end_time);
				if (lstCalendar != null && lstCalendar.size() > 0) {
					for (InstanceData instanceData : lstCalendar) {
						objArray.put(instanceData.toJsonObject());
					}
				}
			} else if (queryType.equalsIgnoreCase("sms")) {
				int sms_type = obj.optInt("sms_type");
				String contact_name = obj.optString("contact_id", null);
				String contact_number = obj.optString("contact_number", null);
				String[] contact_number_list = JsonUtil.optStringArray(obj,
						"contact_number_list");
				long start_time = obj.optLong("start_time");
				long end_time = obj.optLong("end_time");
				List<SmsData> lstSmsData = null;
				if (contact_number_list != null
						&& contact_number_list.length > 0) {
					lstSmsData = new ArrayList<SmsData>();
					List<SmsData> lstSmstemp = null;
					for (int i = 0; i < contact_number_list.length; i++) {
						lstSmstemp = CallUtil.querySms(getApplicationContext(),
								contact_name, contact_number_list[i],
								start_time, end_time, sms_type, 0);
						if (lstSmstemp != null) {
							lstSmsData.addAll(lstSmstemp);
						}
					}
				} else {
					lstSmsData = CallUtil.querySms(getApplicationContext(),
							contact_name, contact_number, start_time, end_time,
							sms_type, 0);
				}
				if (lstSmsData != null && lstSmsData.size() > 0) {
					for (SmsData smsData : lstSmsData) {
						objArray.put(smsData.toJSonObject());
					}
				}
			} else if (queryType.equalsIgnoreCase("calls")) {
				int call_type = obj.optInt("call_type");
				String contact_name = obj.optString("contact_name", null);
				String contact_number = obj.optString("contact_number", null);
				long start_time = obj.optLong("start_time");
				long end_time = obj.optLong("end_time");
				List<PhoneData> lstPhoneData = CallUtil.queryCalls(
						getApplicationContext(), call_type, contact_name,
						contact_number, start_time, end_time);
				if (lstPhoneData != null && lstPhoneData.size() > 0) {
					for (PhoneData phoneData : lstPhoneData) {
						objArray.put(phoneData.toJSonObject());
					}
				}
			} else if (queryType.equalsIgnoreCase("bluetooth")) {
				mBlueTooth.searchBluetooth();
				objArray = null;
				Message msg = mHandler.obtainMessage(MSG_BLUETOOTH_GETLIST);
				mHandler.sendMessageDelayed(msg, 1200);
			} else if (queryType.equalsIgnoreCase("new_email")) {

			} else if (queryType.equalsIgnoreCase("alarm_info")) {
				int startTime = obj.optInt("start_time");
				int endTime = obj.optInt("end_time");
				List<AlarmData> lstAlarm = AlarmUtil.queryAlarm(
						getApplicationContext(), startTime, endTime);
				if (lstAlarm != null && lstAlarm.size() > 0) {
					for (AlarmData alarm : lstAlarm) {
						objArray.put(alarm.toJSonObject());
					}
				}
			} else if (queryType.equalsIgnoreCase("memo")) {
				List<MemoData> lstMemo = MemoUtil
						.queryMemo(getApplicationContext());

				if (lstMemo != null && lstMemo.size() > 0) {
					for (MemoData memo : lstMemo) {
						objArray.put(memo.toJSonObject());
						Log.i(TAG, memo.toJSonObject().toString());
					}
				}
			} else if (queryType.equalsIgnoreCase("sina_weibo_token")) {
				objArray = null;
				Bundle bundle = new Bundle();
				bundle.putString("type", queryType);
				sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_WEIBO, bundle);
			} else if (queryType.equalsIgnoreCase("web_data")) {
				String webResponse = "";
				String method = obj.optString("request_method");
				String url = obj.optString("request_url");
				if ("get".equalsIgnoreCase(method)) {
					webResponse = HttpUtil.sendGetCommand(
							getApplicationContext(), url);
				} else if ("post".equalsIgnoreCase(method)) {
					int key_index = 1;
					List<NameValuePair> lstValue = null;
					while (true) {
						if (lstValue == null)
							lstValue = new ArrayList<NameValuePair>();
						String param_key = obj.optString("param_key_"
								+ key_index, null);
						String param_value = obj.optString("param_value_"
								+ key_index, null);
						if (param_key != null && param_value != null) {
							if (param_key.length() > 0
									&& param_value.length() > 0)
								lstValue.add(new BasicNameValuePair(param_key,
										param_value));
						} else {
							break;
						}
						key_index++;
					}
					int needCaptureImage = obj.optInt("need_capture_image");
					if (needCaptureImage == 1) {
						Message msg = mHandler
								.obtainMessage(MsgConst.SERVICE_ACTION_CAPTURE_VIEW);
						msg.obj = lstValue;
						Bundle bundle = new Bundle();
						bundle.putString("type", queryType);
						bundle.putString("url", url);
						msg.setData(bundle);
						sendBackToClient(msg);
						return;
					} else {
						webResponse = HttpUtil.sendPostCommand(
								getApplicationContext(), url, lstValue);
					}
					/*
					 * JSONArray arrayKey = obj.optJSONArray("param_key");
					 * JSONArray arrayValue = obj.optJSONArray("param_value");
					 * List<NameValuePair> lstValue = new
					 * ArrayList<NameValuePair>(); if (arrayKey != null &&
					 * arrayValue != null && arrayKey.length() ==
					 * arrayValue.length()) { for (int i = 0; i <
					 * arrayKey.length(); i++) { String key =
					 * arrayKey.optString(i, ""); String value =
					 * arrayValue.optString(i, ""); if (key.length() > 0 &&
					 * value.length() > 0) lstValue.add(new
					 * BasicNameValuePair(key, value)); } } webResponse =
					 * HttpUtil.sendPostCommand( getApplicationContext(), url,
					 * lstValue);
					 */
				}

				JSONObject objWebRet = new JSONObject();

				try {
					if (webResponse != null) {
						objWebRet.put("status", "0");
						objWebRet.put("return_data", webResponse);
					} else {
						objWebRet.put("status", "1");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				objArray.put(objWebRet);
			} else if (queryType.equalsIgnoreCase("video")) {

			} else if (queryType.equalsIgnoreCase("music")) {
				String title = obj.optString("title");
				String artist = obj.optString("artist");
				String album = obj.optString("album");

				List<AudioInfo> lstAudio = MediaUtil.queryMusic(
						getApplicationContext(), title, album, artist);
				if (lstAudio != null && lstAudio.size() > 0) {
					for (AudioInfo audioInfo : lstAudio) {
						objArray.put(audioInfo.toJsonObject());
					}
				}
			} else if (queryType.equalsIgnoreCase("music_playing")) {
				objArray = null;
				Bundle bundle = new Bundle();
				bundle.putString("type", queryType);
				sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_MUSIC, bundle);
			} else if (queryType.equalsIgnoreCase("position_alarm")) {
				objArray = null;
				Bundle bundle = new Bundle();
				bundle.putString("type", queryType);
				sendBackToClient(MsgConst.SERVICE_ACTION_QUERY_POSITION_ALARM,
						bundle);
			}

			if (objArray != null) {
				Message msg = mHandler
						.obtainMessage(MsgConst.MSG_PROCESS_SERVER_QUERY_SUCCESSED);
				msg.obj = objArray;
				Bundle bundle = new Bundle();
				bundle.putString("type", queryType);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			}
		}
	}

	protected void playVideo(int player_type, int startTime, String url) {
		if (player_type == 1) {
			Intent playIntent = new Intent("QvodPlayer.VIDEO_PLAY_ACTION");
			playIntent.setDataAndType(Uri.parse(url), "video/*");
			try {
				playIntent.putExtra("play_position", startTime * 1000);
				playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(playIntent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
				CustomToast.makeToast(this, getString(R.string.voiceassistantservice_no_qvodplayer));//, Toast.LENGTH_SHORT)
						//.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Intent playIntent = new Intent();
			playIntent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(url);
			playIntent.setData(uri);
			try {
				playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(playIntent);
			} catch (Exception e) {
				e.printStackTrace();
				CustomToast.makeToast(this, getString(R.string.voiceassistantservice_can_not_play));//, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setWatiServerResponse(boolean needWait) {
		if (needWait) {
			mServerResponsed = false;
			timeoutRunnable = new Runnable() {
				@Override
				public void run() {
					serverResonseTimeout();
				}
			};
			mHandler.postDelayed(timeoutRunnable, mServerTimeOut);
		} else {
			mServerResponsed = true;
			if (timeoutRunnable != null) {
				mHandler.removeCallbacks(timeoutRunnable);
				timeoutRunnable = null;
			}
		}
	}

	private void serverResonseTimeout() {
		Log.i(TAG, "server response timeout.");
		timeoutRunnable = null;
		serverIsDisconnected(false);
		// CustomToast.makeToast(VoiceAssistantService.this, R.string.net_reconnect,
		// Toast.LENGTH_SHORT).show();
		initCommunication();
	}

	public boolean checkNetConnection() {
		boolean ret = false;
		ret = NetWorkUtil.isNetConnected(this);
		if (!ret)
			CustomToast.makeToast(this, getString(R.string.net_not_available));//, Toast.LENGTH_SHORT).show();
		return ret;
	}

	protected void notifyClientState(int state) {
		if (mProcessState == 2 && state == 1) {
			Log.e("notifyClientState", "" + state);
		}

		Bundle bundle = new Bundle();
		bundle.putInt("state", state);
		sendBackToClient(MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE, bundle);

		mProcessState = state;
	}

	protected void notifyClientConnection(int state) {
		Bundle bundle = new Bundle();
		bundle.putInt("connection", state);
		sendBackToClient(MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE, bundle);
	}

	protected void checkServerConnection() {
		MsgRaw msgRaw = new MsgRaw(MsgConst.TS_C_CHECK_STATUS);
		byte[] data = msgRaw.prepareRawData();
		mSocketUtil.sendMessage(data, false);
		Message msg = mHandler.obtainMessage(MsgConst.MSG_SERVER_NO_RESPONSE);
		mHandler.sendMessageDelayed(msg, 2000);
	}

	protected void setWakeupByAudio(int status) {
		mWakeupByAudioEnable = status;
		if (mWakeupByAudioEnable == MsgConst.WAKEUP_AUDIO_ENABLE_WAIT)
			AiTalkShareData.setSpeechStartState(false);
		else
			AiTalkShareData.setSpeechStartState(true);
	}	
}
