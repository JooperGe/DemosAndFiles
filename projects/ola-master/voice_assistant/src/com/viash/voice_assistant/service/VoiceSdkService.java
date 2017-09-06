package com.viash.voice_assistant.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.provider.ContactsContract;
import android.util.Log;

import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.tts.TtsService.Tts;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.alarm.AlarmReceiver;
import com.viash.voice_assistant.common.Config;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.common.LogRecordData;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.component.VoiceWakeUpIndicationDialog;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.observer.ContactContentObserver;
import com.viash.voice_assistant.receiver.AppListChangedReceiver;
import com.viash.voice_assistant.receiver.IncomingCallReceiver;
import com.viash.voice_assistant.receiver.SmsReceiver;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.sdk.VoiceSdkUI;
import com.viash.voice_assistant.service.voicesdk.BRRecognizerListener;
import com.viash.voice_assistant.service.voicesdk.CallClientHelper;
import com.viash.voice_assistant.service.voicesdk.InComingHandler;
import com.viash.voice_assistant.service.voicesdk.MainHandler;
import com.viash.voice_assistant.service.voicesdk.CallServerHelper;
import com.viash.voice_assistant.service.voicesdk.ParamHelper;
import com.viash.voice_assistant.service.voicesdk.ServerMsgProcessor;
import com.viash.voice_assistant.speech.BoruiSpeechRecognizer;
import com.viash.voice_assistant.speech.IRecognizeListener;
import com.viash.voice_assistant.speech.ISpeechRecognizer;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.util.CommunicationUtil;
import com.viash.voicelib.data.AppData.PhoneCommand;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.SdkActionData;
import com.viash.voicelib.data.SdkCommandData;
import com.viash.voicelib.hardware.HBluetooth;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.BatteryUtil;
import com.viash.voicelib.utils.CommunicationUpdateUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.MachineUtil;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.ScreenAndKeyguard;
import com.viash.voicelib.utils.alarm.AlarmUtil;
//import com.viash.voice_assistant.LogRecordData;

@TargetApi(Build.VERSION_CODES.FROYO)
public class VoiceSdkService extends Service {
	/**
	 * Define
	 */
	public static final boolean DEBUG = true;
	public static final String TAG = "VoiceSdkService";
	public static final String VOICE_ASSISTANT_PACKAGE = "com.viash.voice_assistant";

	public static final int MSG_BEGINNING_OF_RECORD = 6;
	public static final int MSG_END_OF_RECORD = 7;
	public static final int MSG_END_OF_UPLOAD = 8;
	public static final int MSG_UPLOAD_ERROR = 9;
	public static final int MSG_WAKEN = 10;
	public static final int MSG_UI_IN_RECORDING = 17;
	public static final int MSG_UI_STOP_RECORDING = 18;
	//public static final int MSG_UI_IDLE = 19;
	public static final int MSG_TTS_PLAY_END = 30;

	public static final int MSG_USER_DATA_REFRESH = 40;

	public static final int MSG_BLUETOOTH_GETLIST = 50;
	public static final int MSG_UI_SEARCHING_START = 51;
	public static final int MSG_UI_SEARCHING_FOUND = 52;

	public static final int SPECIAL_STATUS_NORMAL = 0;
	public static final int SPECIAL_STATUS_SMS = 1;

	public static String SERVER;// = "10.27.27.228";
	public static int PORT = 0;

	public static final int TTS_END_TIME = 200;
	public static final int SERVER_RESPONSE_TIMEOUT = 15000;
	public static int mServerTimeOut = SERVER_RESPONSE_TIMEOUT;

	public static final boolean SAVE_USER_PHONE_DATA = true;

	public Handler mHandler;
	public static boolean mServerResponsed = false;
	public static int mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

	public boolean mNeedAnswer = false;
	public CommunicationData mLatestServerData = null;

	public int mSpecialStatus = SPECIAL_STATUS_NORMAL;

	public boolean isControlFromWidget = false;

	// Voice
	public int mTtsIndex = 0;
	public List<String> mLstTts = new ArrayList<String>();
	public List<String> mLstTtsFlag = new ArrayList<String>();

	/**
	 * Socket
	 */
	public CommunicationUtil mSocketUtil = null;
	public List<byte[]> msgToServerQueue = new ArrayList<byte[]>();
	public Runnable timeoutRunnable = null;

	/**
	 * Messenger
	 */
	public Messenger mMessenger = new Messenger(new InComingHandler(this));
	public HashMap<String, Messenger> cMessenger = new HashMap<String, Messenger>();
	public List<Message> msgQueue = new ArrayList<Message>();

	public String mKeyClient = null;
	public boolean mNeedRequestUserLevel = false;
	public int mProcessState = MsgConst.UI_STATE_INITED;
	public boolean mAskingUserLevel = false;

	public boolean mIsCharging = false;
	public int mWakeupByAudioEnable = MsgConst.WAKEUP_AUDIO_DISABLE;
	ContactContentObserver myContentObserver;

	public VoiceSdkUI mVoiceSdkUi;
	public byte[] mSendingPrompt = null;

	// Loneway
	public int mInputType = 0;

	/**
	 * Audio
	 */
	public static final int MSG_BEGIN_OF_SPEECH = 1;
	public static final int MSG_BUFFER_RECEIVERD = 2;
	public static final int MSG_END_OF_SPEECH = 3;
	public static final int MSG_ERROR = 4;
	public static final int MSG_RESULTS = 5;
	public static final int MSG_UI_INIT = 16;

	public AudioManager mAudioManager = null;
	public static OnAudioFocusChangeListener mAudioFocusChangeListener = null;
//	public static SpeechRecognizer mSpeechRecognizer = null;
	public static ISpeechRecognizer mSpeechRecognizer = null;
	
	public IRecognizeListener mRecognizerListener = null;

	/**
	 * Location
	 */
	public WifiLocation mWifiLocation;
	public static Point mCurLocation = new Point(0, 0);

	/**
	 * Bluetooth
	 */
	public HBluetooth mBlueTooth;

	BroadcastReceiver mReceiver = null;

	public static String mLastUIInfo = "";
	public static String mCurUIInfo = "";

	public static boolean mStarted = false;
	public static boolean mStartCommunication = false;
	public static boolean isApplistUpLoad = false;
	public FloatViewIdle floatViewIdle;
	public static Context mContext;


	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate() {
		mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;
		mServerResponsed = false;
		mIsCharging = BatteryUtil.isCharging(this);
		Log.i(TAG, "IsCharging:" + mIsCharging);

		SavedData.init(getApplicationContext());
		initHandler();

		//----------------------------------------
		ServerMsgProcessor.init(this);
		ParamHelper.init(this);
		CallServerHelper.init(this);
		CallClientHelper.init(this);
		//----------------------------------------
		
		mHandler.postDelayed(new InitData(), 100);

		// Add an Observer for Contacts
		myContentObserver = new ContactContentObserver(mHandler, this);
		this.getContentResolver().registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, myContentObserver);

		LogRecordData.init(MachineUtil.getMachineId(this)); // LeoLi

		initBroadcastReceiver();
		new AppListChangedReceiver(this, mHandler);
		// initCommunication();

		IncomingCallReceiver.setHandler(mHandler);
		SmsReceiver.setHandler(mHandler);
		// initSms();

		mVoiceSdkUi = new VoiceSdkUI(this);
		mVoiceSdkUi.init();

		AlarmReceiver.setHandler(mHandler);
		AlarmUtil.setHandler(mHandler);
		CommunicationUpdateUtil.setHandler(mHandler);
		initHelpStatistics();
		initContext();
		
		if(Config.USE_SOFT_VOICE_KEY)
		{
			Notification notification = new Notification(R.drawable.voice_mic_pressed, getText(R.string.all), System.currentTimeMillis()); 
			Intent notificationIntent = new Intent(this, VoiceSdkService.class);
			notificationIntent.setAction(VoiceSdkServiceInterface.TOGGLE_VOICE_UI);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(this, getText(R.string.top_voice_button_title), getText(R.string.top_voice_button_desc), pendingIntent); 
			startForeground(2000, notification);
		}
		
		this.registerReceiver(mHomeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		if (Build.VERSION.SDK_INT >= 8) {
			if (mAudioManager == null) {
				mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			}
			mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

				@TargetApi(Build.VERSION_CODES.FROYO)
				public void onAudioFocusChange(int focusChange) {
					if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
						// TODO
					} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
						/*
						 * if (Tts.isPlaying()) { Tts.stop(true); }
						 */
						mHandler.sendEmptyMessageDelayed(
								MsgConst.CLIENT_ACTION_STOP_TTS, 2000);
						mAudioManager
								.abandonAudioFocus(mAudioFocusChangeListener);
						// Log.i("abandonAudioFocus", "onAudioFocusChange");
					} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
						// TODO
					} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
						// TODO
					}
				}
			};
		}
		if(SavedData.isVoiceWakeUpOpen())
		{		  
			mHandler.postDelayed(new InitVoiceWakeUp(), 500);
		}
		mStarted = true;
	}

	public void notifyChargeStatus(boolean isCharging) {
		mIsCharging = isCharging;
	}

	public void initBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(VoiceWakeUpIndicationDialog.NOTIFICATION_CLOSE_VOICE_WAKE_UP);
		filter.addAction(VoiceWakeUpIndicationDialog.NOTIFICATION_START_CAPTURE_OFFLINE);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

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

						}
					}
				} else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
					notifyChargeStatus(true);
				} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
					notifyChargeStatus(false);
				} else if (action
						.equals(VoiceWakeUpIndicationDialog.NOTIFICATION_START_CAPTURE_OFFLINE)) {
					mHandler.sendMessage(mHandler
							.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE));
				} else if (action
						.equals(VoiceWakeUpIndicationDialog.NOTIFICATION_CLOSE_VOICE_WAKE_UP)) {
					cancelVoiceWakeUpNotification();
				}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
				{
					if(SavedData.isVoiceWakeUpOpen()&&ScreenAndKeyguard.isScreenLock(VoiceSdkService.this))
					{
						if(mSpeechRecognizer != null)
//							if(mSpeechRecognizer.recognizeSuccess)
							if(mSpeechRecognizer.isRecognizeSuccess())
							{
//								mSpeechRecognizer.recognizeSuccess = false;
								mSpeechRecognizer.setRecognizeSuccess(false);
								ScreenAndKeyguard
										.unlockScreen(VoiceSdkService.this);
								Intent intent1 = new Intent(context,
										GuideActivity.class);
								intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent1.putExtra(
										SpeechRecognizer.START_FROM_OFFLINE_RECORD,
										true);
								context.startActivity(intent1);
							}
					}else if(SavedData.isVoiceWakeUpOpen()){
						if(mSpeechRecognizer != null)
							if(mSpeechRecognizer.isRecognizeSuccess())
							{
								mSpeechRecognizer.setRecognizeSuccess(false);														
								Intent intent1 = new Intent(context,GuideActivity.class);
								intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent1.putExtra(SpeechRecognizer.START_FROM_OFFLINE_RECORD, true);
								context.startActivity(intent1);
							}
					}
							
				}else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				{					
					ScreenAndKeyguard.lockScreen(VoiceSdkService.this);
				}
			}

		};
		registerReceiver(mReceiver, filter);
	}

	public void unInitBroadcastReceiver() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	public void thirdStopCapture() {
		if (mSpeechRecognizer != null && mSpeechRecognizer.isRecognizing()) {
			mSpeechRecognizer.stopRecognize();
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MSG_END_OF_RECORD), 0);

			if (DEBUG)
				Log.i(TAG, "stop recognize by third-party");
		}
	}

	public void thridStartCapture() {
		mVoiceSdkUi.showVoiceView(mProcessState);
		if (mSpeechRecognizer != null && !mSpeechRecognizer.isRecognizing()) {
			if (Tts.isPlaying()) {
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				startCapture(TTS_END_TIME);
			} else {
				startCapture();
			}

			CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_SDK_QUERY_STATE);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (DEBUG)
			Log.d(TAG, "onBind) package name: "
					+ intent.getComponent().getPackageName());
		cMessenger.clear();
		mStarted = true;
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

		if (mSocketUtil != null)
			mSocketUtil.destroy();

		if (mWifiLocation != null)
			mWifiLocation.close();

		if (mSpeechRecognizer != null) {
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();
			}

			mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}

		if (mVoiceSdkUi != null) {
			mVoiceSdkUi.destroy();
			mVoiceSdkUi = null;
		}

		/*
		 * if (mSmsReceiver != null) unregisterReceiver(mSmsReceiver);
		 */

		cMessenger.clear();
		LogRecordData.unInit();
		this.unregisterReceiver(mHomeKeyReceiver);
		// startService(new Intent(VoiceSdkService.this,
		// VoiceSdkService.class));//keep our service alive.
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@SuppressLint("HandlerLeak")
	public void initHandler() {
		mHandler = new MainHandler(this);
	}

	public void handleIncomingCall(Bundle bundle) {
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

	

	public boolean playTts(int index) {
		return playTts(index, Tts.TTS_LOW_PRIORITY);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public boolean playTts(int index, int priority) {
		boolean ret = false;
		if (SavedData.getLockMessage() != null) {
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
				if (IncomingCallShareState.isIncomgingCall()) {
				} else if (SavedData.getVoiceSetting()) {
					if (mSpeechRecognizer != null
							&& mSpeechRecognizer.isRecognizing()) {
						Tts.playText(this, "", listener, priority);
						mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
						if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
							mAudioManager.requestAudioFocus(
									mAudioFocusChangeListener,
									AudioManager.STREAM_VOICE_CALL,
									AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
						}
						return true;
					}
					Tts.playText(this, mLstTts.get(index), listener, priority);
					mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
					if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
						mAudioManager.requestAudioFocus(
								mAudioFocusChangeListener,
								AudioManager.STREAM_VOICE_CALL,
								AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
					}
				} else {
					if (SavedData.isVoiceWakeUpOpen())
						mHandler.sendMessage(mHandler
								.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE));
				}
				ret = true;
			}
		}

		return ret;
	}



	class InitData implements Runnable {
		@Override
		public void run() {
			if (DEBUG)
				Log.d(TAG, "InitData");
			initVoiceEngine();
			initUserData();
		}
	}

	public void initUserData() {
		mBlueTooth = new HBluetooth(this, mHandler);
	}

	public boolean mForceConnect = false;

	public void initCommunication() {
		initWifiLocation();
		switchToSpecialStatus(SPECIAL_STATUS_NORMAL);
		mCurLocation = new Point(0, 0);
		if (mSocketUtil == null) {
			mSocketUtil = new CommunicationUtil();
			mSocketUtil.setCallbackHandler(mHandler);
			mSocketUtil.init(this);
		}

		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE) {
			SERVER = SavedData.INTERNET_SERVER_IP_HTTP;
			PORT = 0;
		} else {
			SERVER = SavedData.getmIP();
			PORT = SavedData.getmPort();
		}

		mSocketUtil.setServer(SERVER, PORT);
		if (!mStartCommunication || mForceConnect) {
			mSocketUtil.startCommunication();
			// mHandler.sendMessage();
			Log.i(TAG, "Connect to " + SERVER + PORT);
			CallServerHelper.getInstance().sendUserDataToServer();
			mForceConnect = false;
		}
		Log.i(TAG, "initCommunication");
		mStartCommunication = true;
	}

	public void switchToSpecialStatus(int status) {
		mSpecialStatus = status;
	}

	public void initWifiLocation() {
		mWifiLocation = WifiLocation.getInstance(this);
		// mWifiLocation.init(this);
	}

	public void initVoiceEngine() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initRecognizer();
	}

	public void initRecognizer() {
		if (mSpeechRecognizer != null) {
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();
			}

			mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}

		mRecognizerListener = new BRRecognizerListener(this);

//		mSpeechRecognizer = SpeechRecognizer.getInstance(getApplicationContext());
		mSpeechRecognizer = BoruiSpeechRecognizer.getInstance(getApplicationContext());
		if (mSpeechRecognizer.isIsLogined())
			mSpeechRecognizer.setListener(mRecognizerListener);
		else {
			if (mSpeechRecognizer.create()) {
				mSpeechRecognizer.setListener(mRecognizerListener);
			} else {
				mSpeechRecognizer = null;
			}
		}
		
//		String ttsPath = TtsUtil.copyTtsData(this);
//		if (ttsPath != null) {
//			if (DEBUG)
//				Log.d(TAG, "start to create tts.");
//			Tts.create(ttsPath);
//			// Tts.setSpeaker(Tts.ivTTS_ROLE_XIAOYAN);
//			Tts.setSpeaker(SavedData.getVoiceType());
//		}
	}

	public void startCapture() {
		startCapture(0);
	}

	public void startCapture(boolean duringPhone) {
		if (duringPhone) {
			if (mSpeechRecognizer != null) {
				mSpeechRecognizer.setListener(mRecognizerListener);
				if (mSpeechRecognizer.isRecognizing()) {
					mSpeechRecognizer.abort();
				}
				AiTalkShareData.setSpeechStartState(false);
				mSpeechRecognizer.startRecognize(0, true);
			}
		} else {
			startCapture(0);
		}
	}

	public void startCapture(long waitTime) {
		// mCurUIInfo = "";
		if (mSpeechRecognizer != null) {
			mSpeechRecognizer.setListener(mRecognizerListener);
			if (!mSpeechRecognizer.isRecognizing()) {
				if (mSpeechRecognizer.startRecognize(waitTime, false)) {
					mHandler.sendMessageDelayed(
							mHandler.obtainMessage(MSG_BEGINNING_OF_RECORD), 0);
				}
			}
		}
	}

	public void sendBluetoothList(List<?> lstDevices) {
		JSONArray array = new JSONArray();
		if (lstDevices != null) {
			for (int i = 0; i < lstDevices.size(); i++) {
				JSONObject obj = mBlueTooth
						.getDeviceJsonObject((BluetoothDevice) lstDevices
								.get(i));
				array.put(obj);
			}
		}
		ServerMsgProcessor.getInstance().processQueryAnswer("bluetooth", array, lstDevices.size());
	}

	public List getSubList(List list, int require_num, int start_record_id) {
		if ((list != null) && (require_num != 0)) {
			int size = list.size();
			int needReturnCount;
			if ((start_record_id + require_num) <= size)
				needReturnCount = require_num;
			else
				needReturnCount = size - start_record_id;
			if (needReturnCount > 0)
				list = list.subList(start_record_id, start_record_id
						+ needReturnCount);
		}
		return list;
	}

	public void playVideo(int player_type, int startTime, String url) {
		if (player_type == 1) {
			Intent playIntent = new Intent("QvodPlayer.VIDEO_PLAY_ACTION");
			playIntent.setDataAndType(Uri.parse(url), "video/*");
			try {
				playIntent.putExtra("play_position", startTime * 1000);
				playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(playIntent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
				CustomToast
						.makeToast(
								this,
								getString(R.string.voiceassistantservice_no_qvodplayer));// ,
																							// Toast.LENGTH_SHORT).show();
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
				CustomToast.makeToast(this,
						getString(R.string.voiceassistantservice_can_not_play));// ,
																				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void setWatiServerResponse(boolean needWait) {
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

	public void serverResonseTimeout() {
		Log.i(TAG, "server response timeout.");
		timeoutRunnable = null;
		CallClientHelper.getInstance().serverIsDisconnected(false);
		// CustomToast.makeToast(VoiceAssistantService.this,
		// R.string.net_reconnect,
		// Toast.LENGTH_SHORT).show();
	}

	public boolean checkNetConnection() {
		boolean ret = false;
		ret = NetWorkUtil.isNetConnected(this);
		if (!ret)
			CustomToast.makeToast(this, getString(R.string.net_not_available));// ,
																				// Toast.LENGTH_SHORT).show();
		return ret;
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "on StartCommand");
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {
				if (VoiceSdkServiceInterface.ACTION_SHOW_RECORD_UI
						.equals(action)) {
					thridStartCapture();
				} else if (VoiceSdkServiceInterface.THIRDPARTY_START_CAPTURE_ACTION
						.equals(action)) {
					if (DEBUG)
						Log.i(TAG, "THIRDPARTY_START_CAPTURE_ACTION");
					thridStartCapture();
				} else if (VoiceSdkServiceInterface.THIRDPARTY_STOP_CAPTURE_ACTION
						.equals(action)) {
					if (DEBUG)
						Log.i(TAG, "THIRDPARTY_STOP_CAPTURE_ACTION");
					thirdStopCapture();
				} else if (VoiceSdkServiceInterface.THIRDPARTY_CLOSE_COMMUNICATIONVIEW_ACTION
						.equals(action)) {
					if (mVoiceSdkUi != null) {
						mVoiceSdkUi.hideUI(false);
					}
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				} else if (VoiceSdkServiceInterface.THIRDPARTY_CLOSED
						.equals(action)) {
					if (mVoiceSdkUi != null) {
						mVoiceSdkUi.hideUI(false);
					}
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				} else if (VoiceSdkServiceInterface.TOGGLE_VOICE_UI
						.equals(action)) {
					if (mVoiceSdkUi != null
							&& !mVoiceSdkUi.isVoiceViewVisible())
						mVoiceSdkUi.showVoiceView(mProcessState);
					else if (mVoiceSdkUi != null) {
						mVoiceSdkUi.hideUI(true);
					}
				} else if (VoiceSdkServiceInterface.THIRDPARTY_START_TTS_ACTION
						.equals(action)) {
					String ttsText = (String) intent.getStringExtra("tts_text");
					if (ttsText != null && ttsText.length() > 0)
						Tts.playText(VoiceSdkService.this, ttsText, null);
				} else if (VoiceSdkServiceInterface.THIRDPARTY_STOP_TTS_ACTION
						.equals(action)) {
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				}
			}
		}


		Tts.setSpeaker(SavedData.getVoiceType());
		initFloatViewIdle();		
		return START_STICKY;
	}

	public void initFloatViewIdle() {
		floatViewIdle = FloatViewIdle.getInstance(VoiceSdkService.this);
		floatViewIdle.setHandler(mHandler);
	}

	public void addDataToCommondata(String str, CommunicationData data) {
		CommunicationData commData = new CommunicationData(DataConst.FROM_MIC);
		commData.setDisplayText(str);
		CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA, commData);
		CallClientHelper.getInstance().sendBackToClient(MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA, data);
		CallClientHelper.getInstance().sendBackToClient(MsgConst.CLIENT_ACTION_LISTVIEW_GOTO_LAST_POSITION);
	}

	public void playTtsDataWakeUpByFloatView(CommunicationData commData) {
		if (commData != null) {
			boolean isSdkCommand = false;
			boolean isActionCommand = false;
			List<BaseData> lstData = commData.getLstData();

			if (lstData != null) {
				for (BaseData baseData : lstData) {
					if (baseData instanceof SdkCommandData) {
						isSdkCommand = true;
						ServerMsgProcessor.getInstance().processSpecialSdkCommandData((SdkCommandData) baseData);
					}

					if (baseData instanceof SdkActionData) {
						isActionCommand = true;
						ServerMsgProcessor.getInstance().processSpecialSdkActionData((SdkActionData) baseData);
					}
				}

				if (isActionCommand) {
					if (mVoiceSdkUi != null) {
						mVoiceSdkUi.hideUI(false);
					}
				} else if (isSdkCommand) {
					if (mVoiceSdkUi != null) {
						mVoiceSdkUi.hideUI(false);
					}
				} else {
					String tts = "";
					mNeedAnswer = false;
					boolean isSilentInfoData = false;
					boolean isNeedUsePriority = false;
					for (BaseData baseData : lstData) {
						if (!mNeedAnswer) {
							if (commData.isSilentInfoMsg()) {
								mNeedAnswer = true;
								isSilentInfoData = true;
							} else {
								mNeedAnswer = baseData.isDataNeedAnswer();
							}
						}
						if (baseData.getTtsString() != null)
							tts += baseData.getTtsString();

						baseData.doAction(this, mHandler);
						String actionResult = baseData.getActionResult(this);
						if (actionResult != null) {
							tts += actionResult;
						}

						if (baseData instanceof ConfirmData) {
							ServerMsgProcessor.getInstance().processSpecialConfirmData((ConfirmData) baseData);
						}

						if (baseData instanceof PreFormatData) {
							ServerMsgProcessor.getInstance().processSpecialPreformatData((PreFormatData) baseData);
							int type = ((PreFormatData) baseData)
									.getmDataType();
							if ((type == PreFormatData.TYPE_HTML)
									|| (type == PreFormatData.JSON_POEM)
									|| (type == PreFormatData.JSON_JOKE)
									|| (type == PreFormatData.JSON_BAIKE_OTHER)
									|| (type == PreFormatData.JSON_NEWS))
								isNeedUsePriority = true;
						}

						if (baseData instanceof OptionData) {
							int type = ((OptionData) baseData).getOptionId();
							if (type == OptionData.OPTION_NEWS_NAME)
								isNeedUsePriority = true;
						}
					}

					if (tts.length() > 0) {
						ServerMsgProcessor.getInstance().parseTtsData(tts);
						if (isNeedUsePriority)
							playTts(0, 1);
						else
							playTts(0);
					}
				}
			}
		}
	}

	public boolean isRunAppDirect(CommunicationData commData) {
		boolean ret = false;
		if (commData != null) {
			List<BaseData> lstData = commData.getLstData();

			if (lstData != null) {
				for (BaseData baseData : lstData) {
					if (baseData instanceof com.viash.voicelib.data.AppData) {
						com.viash.voicelib.data.AppData appData = (com.viash.voicelib.data.AppData) baseData;
						if (appData.getServerCommand() instanceof com.viash.voicelib.data.AppData.PhoneCommand) {
							PhoneCommand phoneCommand = (PhoneCommand) appData
									.getServerCommand();
							String phoneNumber = phoneCommand.getPhoneNumber();
							if (phoneNumber != null) {
								Uri uri = Uri.parse("tel:" + phoneNumber);
								Intent intent = new Intent(Intent.ACTION_CALL,
										uri);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								if (intent != null) {
									try {
										HelpStatisticsUtil
												.putContentToJsonObject(
														HelpStatisticsUtil.FLOAT_VIEW_COUNT_FOR_CALL,
														1);
										startActivity(intent);
										ret = true;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}

	public BroadcastReceiver mHomeKeyReceiver = new BroadcastReceiver() {
		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";// home key

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, intent.getAction());

			if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {
						Log.i(TAG, SYSTEM_HOME_KEY);
						if (Tts.isPlaying()) {
							Tts.stop(Tts.TTS_NORMAL_PRIORITY);
							if (SavedData.isVoiceWakeUpOpen())
								mHandler.sendMessage(mHandler
										.obtainMessage(MsgConst.MSG_START_CAPTURE_OFFLINE));
						}
					}
				}
			}
		}
	};
	public String displayString;

	public void initHelpStatistics() {
		HelpStatisticsUtil.initJsonObjectFromFile();
	}

	public void cancelVoiceWakeUpNotification() {
		SavedData.setVoiceWakeUp(false);
		if (mSpeechRecognizer != null)
			mSpeechRecognizer.stopWakeUp();
		mHandler.sendMessage(mHandler
				.obtainMessage(MsgConst.CLIENT_ACTION_CANCEL_RECORD));
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.wake_voice_64,
				"哦啦语音唤醒功能已关闭", System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(VoiceSdkService.this, "哦啦语音唤醒功能已关闭",
				"请进入哦啦语音设置菜单开启该功能", null);
		Intent intent = new Intent(VoiceSdkService.this, GuideActivity.class);
		notification.contentIntent = PendingIntent.getActivity(
				VoiceSdkService.this, 0, intent, 0);
		mNotificationManager.notify(
				VoiceWakeUpIndicationDialog.NOTIFICATION_ID, notification);
		mNotificationManager
				.cancel(VoiceWakeUpIndicationDialog.NOTIFICATION_ID);
	}

	public void initContext() {
		mContext = VoiceSdkService.this;
	}
	
	class InitVoiceWakeUp implements Runnable {
		@Override
		public void run() {			
			if (!mStartCommunication) {
				initCommunication();
			}
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);	
//			mSpeechRecognizer.recognizeSuccess = false;
			mSpeechRecognizer.setRecognizeSuccess(false);
			startCapture(true);
		}
	}
}
