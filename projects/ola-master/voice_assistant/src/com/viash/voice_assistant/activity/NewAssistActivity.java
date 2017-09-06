package com.viash.voice_assistant.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.services.overlay.PoiOverlay;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.aitalk4.FilesCopyUtil;
import com.iflytek.tts.TtsService.Tts;
import com.nd.channel.NDChannel;
import com.umeng.analytics.MobclickAgent;
import com.via.android.voice.floatview.FloatViewIdle;
import com.via.android.voice.floatview.FloatViewService;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.assistant.CameraHelper;
import com.viash.voice_assistant.activity.assistant.InComingHandler;
import com.viash.voice_assistant.activity.assistant.InitHelper;
import com.viash.voice_assistant.activity.assistant.MapHelper;
import com.viash.voice_assistant.activity.assistant.MsgSender;
import com.viash.voice_assistant.activity.assistant.NotifyUiHandler;
import com.viash.voice_assistant.activity.assistant.ScreenCaptureHelper;
import com.viash.voice_assistant.activity.assistant.ServiceHolder;
import com.viash.voice_assistant.activity.assistant.ShareHelper;
import com.viash.voice_assistant.activity.assistant.SplashHelper;
import com.viash.voice_assistant.activity.assistant.UIStateHelper;
import com.viash.voice_assistant.activity.assistant.InitHelper.AsyncInitWorker;
import com.viash.voice_assistant.adapter.CommunicationAdapter;
import com.viash.voice_assistant.adapter.HelpExpandableAdapter;
import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voice_assistant.common.CameraPreview;
import com.viash.voice_assistant.common.Corpus;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.component.DownloadNotification;
import com.viash.voice_assistant.component.RecommendView;
import com.viash.voice_assistant.component.SettingsView;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.handler.AppCrashHandler;
import com.viash.voice_assistant.service.CheckServerMsgService;
import com.viash.voice_assistant.service.FloatViewIdleService;
import com.viash.voice_assistant.service.LockScreenService;
import com.viash.voice_assistant.service.UploadService;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.widget.HelpGuideDetailView;
import com.viash.voice_assistant.widget.HelpGuideView;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voice_assistant.widget.NetworkWarningDialog;
import com.viash.voice_assistant.widget.RotateView;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.hardware.HScreenBrightness;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.utils.AlarmUtil;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.CommunicationUpdateUtil;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.LogOutput;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.WeiboUtil;

public class NewAssistActivity extends FragmentActivity implements
		CancelableCallback {
	/**
	 * Define
	 */
	public static final boolean DEBUG = true;
	public static final String TAG = "NewAssistActivity";

	/**
	 * Variablesal
	 */
	public List<Bitmap> mVolumeBitmap = new ArrayList<Bitmap>();
	public BroadcastReceiver mReceiver = null;
	public Corpus mCorpus;
	public SettingsView mSettingsView;

	public int mProcessingState = MsgConst.UI_STATE_UNINIT;
	// public boolean mNeedAnswer = false;

	public boolean isExit = false;

	// public boolean isPause = false;
	/**
	 * Components
	 */
	public MusicPlayerView mMusicPlayerView = null;
	public RelativeLayout mMapView = null;
	public AMap aMap;
	public ListView mLstView;
	public ListView mLstMusicView;
	public View mLayoutMain;
	public View mLayoutText;
	public Button mBtnSwitchToText;
	public Button mBtnSwitchToVoice;
	public Button mBtnSendText;
	public EditText mEdtContent;
	public Bitmap speakUpdateBitMap;
	public CommunicationData mLatestServerData = null;
	public View view_welcome;
	public WebView mWebView = null;
	public View layout_webView = null;
	// add for basic selection whole screen show;
	public RelativeLayout layout_selection_whole_View = null;

	public Button btn_last_page;
	public Button btn_next_page;
	public Button btn_refresh_page;
	public CommunicationAdapter mAdapter;
	public View mLayoutBeforeLogin;
	public View mLayoutLoginInfo;
	public RelativeLayout layout_login_center;
	public ImageView mBtn_Login;
	public ImageView btn_goback;
	public TextView mTv_Username;
	public TextView mTv_Score;
	public ImageView mIcon_Medal;
	public ImageView mIcon_Arrow;
	public ImageView icon_authenticate_user;

	// for drawer
	// public ImageView image_drawer;
	// public ImageView image_drawer_icon_new;
	public ImageView image_help;
	public LinearLayout layout_guide_help;
	public View mLayoutHelp;
	public ExpandableListView mHelpView;
	public RelativeLayout layout_recommend;
	public RecommendView mRecommendView;

	boolean mMusicRequested = false;
	OnAudioFocusChangeListener mAudioListener = null;

	public Button btn_voiceSpeak;
	public Button btn_voiceHelp;
	public ComponentName mMonitorMediaButton = null;

	public long mMsgId = 0;
	public String mMsgTitle = null;
	public String mMsgUrl = null;
	public long mVoictBtnDownTime = 0;
	public boolean mFirstRegister = true;
	public boolean mServerResponsed = true;

	/**
	 * Map
	 */
	public PoiOverlay mPoiOverLay = null;

	/**
	 * Service
	 */

	public boolean isRegister = false;
	public Messenger mtServiceMessenger = null;

	public long mLastClickTime = 0;
	public int mClickTimes = 0;

	public RotateView rotateview;
	public ImageView imgv_voice_volume;
	public static View layout_voice;
	public int mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

	// public TipsView tipsView;

	WifiLocation mWifiLocation = null;

	public boolean isCalling = false;

	/**
	 * silent camera
	 */
	public Camera mCamera = null;
	public CameraPreview mPreview;
	public static int systemVolumeBeforeTaken = 0;

	public String getMessageFromLock = null;

	// public static boolean isUserLoggedin = false;

	// for bluetooth
	public ProgressDialog mSearchingDialog;

	/**
	 * for busline
	 */
	public String start_stop = null;
	public String end_stop = null;

	/**
	 * for POI search and route search
	 */
	public PoiSearch.Query mPOIQuery;
	public BusRouteQuery mBusRouteQuery;
	public DriveRouteQuery mDriveRouteQuery;
	public WalkRouteQuery mWalkRouteQuery;
	public RouteSearch mRouteSearch;
	public NetworkWarningDialog networkWarningDialog = null;
	public static boolean musicPlayRemind = true;
	public static boolean videoPlayRemind = true;
	public static boolean downloadRemind = true;
	public HelpGuideView helpGuideView;
	public HelpGuideDetailView helpGuideDetailView;
	public HelpExpandableAdapter helpExpandableAdapter;
	public int guideDisplayDelay = 0;
	public boolean isBaidu = false;
	public boolean needAddCommondata = false;
	public boolean needStartCapture = false;
	public boolean isStartFromBluetooth = false;
	public boolean isNeedUnlock = false;
	public Bitmap welcomePageBitmap = null;
	public BitmapDrawable welcomePageDrawable = null;

	/**
	 * Voice Assistant Service Callback
	 */
	public Messenger mMessenger = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SavedData.init(getApplicationContext());
		if (SavedData.isHttpMode())
			mServerState = MsgConst.STATE_SERVER_CONNECTED;
		MobclickAgent.onError(this);
		Intent service = new Intent();
		service.setClass(this, CheckServerMsgService.class);
		startService(service);

		if (SavedData.isHttpMode()) {
			Intent serviceSdk = new Intent();
			serviceSdk.setClass(this, VoiceSdkService.class);
			startService(serviceSdk);
		}

		mWifiLocation = WifiLocation.getInstance(this);

		long time = SystemClock.currentThreadTimeMillis() - 60 * 60000;
		SystemClock.setCurrentTimeMillis(time);
		parseDataFromIntent(getIntent());

		if (DEBUG)
			Log.d(TAG, "onCreate()");
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		GlobalData.init(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assist);

		// 初始化 handler
		// ----------------------------------------------
		NotifyUiHandler.init(this);
		CameraHelper.init(this);
		InComingHandler.init(this);
		mMessenger = new Messenger(InComingHandler.getInstantce());
		MapHelper.init(this);
		ServiceHolder.init(this);
		InitHelper.init(this);
		InitHelper.getInstantce().initControlName();
		ShareHelper.init(this);
		ScreenCaptureHelper.init(this);
		SplashHelper.init(this);
		SplashHelper.getInstantce().openSplash();
		MsgSender.init(this);
		UIStateHelper.init(this);
		// ----------------------------------------------

		initUI();
		initNetWorkRemindVariable();
		
		NotifyUiHandler.getInstantce().postDelayed(InitHelper.getInstantce().new AsyncInitWorker(), 100);

		// Auto update
		DownloadNotification.init(this, this);

		mPreview = new CameraPreview(this, null);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview, new LayoutParams(1, 1));// preview.addView(mPreview);//
		if (FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE == false)
			startService(new Intent(NewAssistActivity.this,
					FloatViewIdleService.class));

		Tts.initTTS(this);

	}

	@Override
	public void onNewIntent(Intent arg0) {
		parseDataFromIntent(arg0);
		checkServerNotification();
		hideTopView(true);
		super.onNewIntent(arg0);
	}

	public void parseDataFromIntent(Intent intent) {
		if (intent != null) {
			mMsgId = intent.getLongExtra(MsgConst.SERVER_MSG_ID, 0);
			mMsgTitle = intent.getStringExtra(MsgConst.SERVER_MSG_TITLE);
			mMsgUrl = intent.getStringExtra(MsgConst.SERVER_MSG_URL);
			guideDisplayDelay = intent.getIntExtra(GuideActivity.DELAY, 0);
			isBaidu = intent.getBooleanExtra(GuideActivity.IS_BAIDU, false);
			needAddCommondata = intent.getBooleanExtra(
					FloatViewIdle.START_FROM_FLOAT_VIEW, false);
			needStartCapture = intent.getBooleanExtra(
					SpeechRecognizer.START_FROM_OFFLINE_RECORD, false);
			isStartFromBluetooth = intent.getBooleanExtra(
					GuideActivity.IS_START_FROM_BLUETOOTH, false);
			isNeedUnlock = intent.getBooleanExtra(GuideActivity.IS_NEED_UNLOCK,
					false);
		}
	}

	public void checkServerNotification() {
		if (mMsgId > 0) {
			CommunicationData data = new CommunicationData(
					DataConst.FROM_NOTIFY);
			data.setNotifyInfo(mMsgTitle, mMsgUrl);
			mAdapter.addData(data);
			HelpStatisticsUtil.putContentToJsonObject(mMsgId);
			mMsgId = 0;
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (DEBUG)
			Log.d(TAG, "onRestart()");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_CLEAR_TALK_HISTORY, 0, 0);

		uninitBroadcastReceiver();
		mMusicPlayerView.closeMusicServer();

		ServiceHolder.getInstantce().stopVoiceAssistantService();

		if (UserData.isLockHomekeyEnable(this)) {
			Intent intent2 = new Intent();
			if (LockScreenService.packagenameString != null) {
				ComponentName comp = new ComponentName(
						LockScreenService.packagenameString,
						LockScreenService.ClassnameString);
				intent2.setComponent(comp);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent2);
			}
		}

		super.onDestroy();

		CameraHelper.getInstantce().releaseCamera();
		CustomToast.setToastAvailable(false);
		if (NetWorkUtil.isWIFIConnected(NewAssistActivity.this)) {
			if (FilesCopyUtil.isHaveFileInDirectory(AppCrashHandler.filePath))
				startService(new Intent(NewAssistActivity.this,
						UploadService.class));
		}
		FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: requestCode(" + requestCode + ")"
				+ " resultCode(" + resultCode + ")");
		WeiboUtil.onActivityResult(requestCode, resultCode, data);
	}

	public void onSettingIp() {
		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menu_dump) {
			startDumpDialogText();
		} else if (item.getItemId() == R.id.menu_settings) {
			onSettingIp();
		} else {
			if ((mCorpus != null) && mCorpus.mSupport) {
				if (mCorpus.isCorpusMenu(item.getTitle().toString())) {
					if (mCorpus.isStopMenu((item.getTitle().toString()))) {
						mCorpus.stopRun();
					} else {
						mCorpus.loadCorpus(item);
						mCorpus.runCorpus();
					}
				}
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = false;
		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
			getMenuInflater().inflate(R.menu.activity_main, menu);
			if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_RELEASE) {
				menu.removeItem(R.id.menu_dump);
			} else {
				if ((mCorpus != null) && mCorpus.mSupport) {
					mCorpus.addCorpusMenu(menu);
				}
			}

			ret = true;
		}

		return ret;
	}

	@Override
	public void onStart() {
		if (DEBUG)
			Log.d(TAG, "onStart()");
		super.onStart();
		if (AiTalkShareData.getLeaveMainInterfaceFlag()
				&& SavedData.isAllowWakeupByAudio()) {
			AiTalkShareData.setLeaveMainInterfaceFlag(false);
			try {
				if (mtServiceMessenger != null)
					mtServiceMessenger.send(Message.obtain(null,
							MsgConst.CLIENT_ACTION_REENTRY_WAKEUP_BY_AUDIO));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume() {
		if (DEBUG)
			Log.d(TAG, "onResume()");
		// isPause = false;
		MobclickAgent.onResume(this);
		GlobalData.setNewAssistantAcitivityRunFlag(true);

		HScreenBrightness.setActivity(this);
		super.onResume();
		registerClient();
		UIStateHelper.getInstantce().updateStatusView();

		UIStateHelper.getInstantce().setProcessingState(MsgConst.UI_STATE_INITED);
		if (mMusicPlayerView != null) {
			mMusicPlayerView.speakRecover();
			mMusicPlayerView.updateMusicViewStatus();
		}
		isCalling = false;

		getMessageFromLock = SavedData.getLockMessage();
		if (getMessageFromLock != null && !getMessageFromLock.equals("")) {
			if (com.viash.voice_assistant.common.Config.WHICH_SERVER
					.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
				Message textmsg = NotifyUiHandler.getInstantce().obtainMessage(
						MsgConst.MSG_DATA_FROM_TEXT);
				textmsg.obj = getMessageFromLock;
				NotifyUiHandler.getInstantce()
						.sendMessageDelayed(textmsg, 2000);
				getMessageFromLock = null;
				SavedData.setLockMessage(null);
			} else if (VoiceAssistantService.mServerState == MsgConst.STATE_SERVER_CONNECTED
					&& !mFirstRegister) {
				Message textmsg = NotifyUiHandler.getInstantce().obtainMessage(
						MsgConst.MSG_DATA_FROM_TEXT);
				textmsg.obj = getMessageFromLock;
				NotifyUiHandler.getInstantce().sendMessage(textmsg);
				getMessageFromLock = null;
				SavedData.setLockMessage(null);
			}
		}
	}

	@Override
	public void onPause() {
		if (DEBUG)
			Log.d(TAG, "onPause()");
		MobclickAgent.onPause(this);

		if (!isCalling) {
			mMusicPlayerView.speakRecover();
		}
		cancelRecognize();
		super.onPause();
		CameraHelper.getInstantce().releaseCamera();
	}

	@Override
	public void onStop() {
		if (DEBUG)
			Log.d(TAG, "onStop()");
		HScreenBrightness.setActivity(null);
		super.onStop();
		unregisterClient();
		if (mWifiLocation != null)
			mWifiLocation.close();

		Tts.stop();
		GlobalData.setNewAssistantAcitivityRunFlag(false);
		HelpStatisticsUtil.saveJsonObjectToFile();
	}

	@Override
	public void finish() {
		if (DEBUG)
			Log.d(TAG, "finish()");
		Tts.destroy();

		super.finish();
	}

	public boolean cancelRecognize() {
		boolean ret = false;
		if (mProcessingState == MsgConst.UI_STATE_SPEAKING) {
			MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_CANCEL_RECORD, 0, 0);
			ret = true;
		}

		return ret;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (layout_recommend.getVisibility() == View.VISIBLE) {
				layout_recommend.setVisibility(View.INVISIBLE);
			} else if ((mMapView.getVisibility() == View.VISIBLE)
					|| (layout_webView.getVisibility() == View.VISIBLE)) {
				hideTopView(true);
			} else if (mLayoutHelp.getVisibility() == View.VISIBLE) {
				NotifyUiHandler.getInstantce().sendMessage(
						NotifyUiHandler.getInstantce().obtainMessage(
								MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
			} else if (layout_guide_help.getVisibility() == View.VISIBLE) {
				helpGuideDetailView = null;
				mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
				NotifyUiHandler.getInstantce().sendMessage(
						NotifyUiHandler.getInstantce().obtainMessage(
								MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
			} else if (layout_selection_whole_View.getVisibility() == View.VISIBLE) {
				layout_selection_whole_View.removeAllViews();
				layout_selection_whole_View.setVisibility(View.GONE);

			} else {
				if (cancelRecognize()) {
					return true;
				} else if (hideTopView(false)) {
					return true;
				}
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				if (SavedData.isVoiceWakeUpOpen())
					MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE, 0,
							0);
				if (isExit) {
					finish();
				} else {
					isExit = true;
					CustomToast.showShortText(NewAssistActivity.this,
							getResources()
									.getString(R.string.exit_please_again));
					exitThread();
				}
			}
		}
		AudioManager audioManager = null;
		audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			return true;
		} else {
			return true;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
			onVoiceBtnClicked(0);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void startDumpDialogText() {
		MsgRaw msgRaw = new MsgRaw();
		msgRaw.setmId(MsgConst.TS_C_DUMP);
		byte[] data = msgRaw.prepareRawData();
		if (data != null) {
			Bundle bundle = new Bundle();
			bundle.putByteArray("data", data);

			MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER,
					bundle);
		}
	}

	public void prepareSendText(String text) {
		boolean allowEdit = false;
		if (mLayoutText.getVisibility() != View.VISIBLE) {
			allowEdit = (mProcessingState == MsgConst.UI_STATE_INITED);
		} else {
			allowEdit = true;
		}

		if (allowEdit) {
			mLayoutText.setVisibility(View.VISIBLE);
			layout_voice.setVisibility(View.GONE);
			mEdtContent.setText(text);
		}
	}

	public boolean hideTopView(boolean all) {
		boolean ret = false;

		if (mMapView.getVisibility() == View.VISIBLE) {
			mMapView.setVisibility(View.GONE);
			ret = true;
		}

		if (all || !ret) {
			if (layout_webView.getVisibility() == View.VISIBLE) {
				layout_webView.setVisibility(View.GONE);
				ret = true;

				// reset
				UIStateHelper.getInstantce().setProcessingState(MsgConst.UI_STATE_INITED);
			}
		}

		if (layout_recommend.getVisibility() == View.VISIBLE) {
			layout_recommend.setVisibility(View.INVISIBLE);
		}
		return ret;
	}

	public void showTopView(View topView) {
		mMapView.setVisibility(View.GONE);
		layout_webView.setVisibility(View.GONE);
		topView.setVisibility(View.VISIBLE);
	}

	public void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEdtContent.getWindowToken(), 0);
	}

	public void initUI() {
		mAdapter = new CommunicationAdapter(this);
		mAdapter.setmHandler(NotifyUiHandler.getInstantce());
		mLstView.setAdapter(mAdapter);

		UIStateHelper.getInstantce().updateStatusView();

		checkServerNotification();
		mLstView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		UIStateHelper.getInstantce().setProcessingState(MsgConst.UI_STATE_UNINIT);
	}

	public void initVoiceView() {
		imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
		imgv_voice_volume.setVisibility(View.GONE);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);

		if (!com.viash.voice_assistant.common.Config.WHICH_SERVER
				.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
			if (VoiceAssistantService.mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED)
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
			else if (VoiceAssistantService.mServerState == MsgConst.STATE_SERVER_CONNECTING)
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
			else
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_connected);
		} else {
			btn_voiceSpeak
					.setBackgroundResource(R.drawable.voice_mic_connected);
		}

	}

	public void initNetWorkRemindVariable() {
		if (SavedData.isNetworkTips()) {
			musicPlayRemind = true;
			videoPlayRemind = true;
			downloadRemind = true;
		} else {
			musicPlayRemind = false;
			videoPlayRemind = false;
			downloadRemind = false;
		}

	}

	
	public void uninitBroadcastReceiver() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}


	/**
	 * Music
	 */
	public void playMusicList(List<String[]> lstData) {
		if (lstData != null && lstData.size() > 0) {
			List<MusicEntity> lstMusic = new ArrayList<MusicEntity>();
			for (int i = 0; lstData.get(1) != null && i < lstData.get(1).length; i++) {
				MusicEntity music = new MusicEntity();
				if (lstData.get(0) != null)
					music.setId(Integer.valueOf(lstData.get(0)[i]));
				if (lstData.get(1) != null)
					music.setName(lstData.get(1)[i]);
				if (lstData.get(2) != null)
					music.setAuthor(lstData.get(2)[i]);
				if (lstData.get(3) != null)
					music.setUrl(lstData.get(3)[i]);
				if (lstData.get(4) != null)
					music.setPhoto(lstData.get(4)[i]);
				if (lstData.size() > 5 && lstData.get(5) != null
						&& lstData.get(5)[i].length() > 0)
					music.setTime(Double.valueOf(lstData.get(5)[i]));
				if (lstData.size() > 6 && lstData.get(6) != null
						&& lstData.get(6)[i].length() > 0)
					music.setAlbum(lstData.get(6)[i]);
				lstMusic.add(music);
			}
			if (lstMusic != null && lstMusic.size() > 0) {
				String[] temp = lstData.get(7);
				int play_type = Integer.parseInt(temp[0]);
				if (play_type == 0) {
					mMusicPlayerView.setAutoplay(true);
				} else {
					mMusicPlayerView.setAutoplay(false);
				}
				mMusicPlayerView.setVisibility(View.VISIBLE);
				mMusicPlayerView.initMusicListView(mLstMusicView);
				mMusicPlayerView.setPlayList(lstMusic,
						NotifyUiHandler.getInstantce());
				mMusicPlayerView.showMusicList();
			} else {
				LogOutput.e(TAG, "lstMusic is null");
			}
		}
	}

	/**
	 * Web
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void showInternalWeb(String url, boolean disableJavaScript) {
		mWebView.getSettings().setJavaScriptEnabled(!disableJavaScript);
		mWebView.loadUrl(url);
		showTopView(layout_webView);
	}

	public void unregisterClient() {
		// remove unregister
		if (mtServiceMessenger == null)
			return;
		Bundle bundle = new Bundle();
		bundle.putString("key", getApplicationContext().getPackageName());
		Message msg = Message.obtain(null,
				MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER);
		msg.replyTo = mMessenger;
		msg.setData(bundle);
		MsgSender.getInstantce().sendMessageToService(msg);
		isRegister = false;
	}

	public void showHelpGuideView() {
		if (HelpStatisticsUtil.isHelpDataFileExist() == false) {
			String str = "{\"Just Talk Dialog Outputs\":[{"
					+ "\"Help\":"
					+ "{"
					+ "\"help_menu\":"
					+ "["
					+ "{\"type\" : \"weather\",\"title\":\"天气\",\"description\" : \"明天天气怎么样\",\"icon_name\" : \"icn_weather.png\",\"url\" : \"local\",\"contentArray\" : [\"上海的天气\",\"空气质量\",\"天气预报\",\"明天天气怎么样\",\"下一周的天气\"],\"opacity\" : \"1\",\"color\" : \"#33b8e3\"},"
					+

					"{\"type\" : \"music\",\"title\":\"音乐\",\"description\" : \"我要听陈奕迅的歌\",\"icon_name\" : \"icn_music.png\",\"url\" : \"local\",\"contentArray\" : [\"听歌\",\"听音乐\",\"听陈奕迅的歌\",\"随便放首歌\",\"我要听青花瓷\"],\"opacity\" : \"1\",\"color\" : \"#7dcf00\"},"
					+

					"{\"type\" : \"joke\",\"title\":\"笑话\",\"description\" : \"我想听冷笑话\",\"icon_name\" : \"icn_joke.png\",\"url\" : \"local\",\"contentArray\" : [\"讲笑话\",\"我要听笑话\",\"来个笑话\",\"来个段子\",\"我想听冷笑话\"],\"opacity\" : \"1\",\"color\" : \"#d338fe\"},"
					+

					"{\"type\" : \"POI\",\"title\":\"周边\",\"description\" : \"我饿了\",\"icon_name\" : \"icn_poi.png\",\"url\" : \"local\",\"contentArray\" : [\"我饿了\",\"我在哪\",\"我的位置\",\"附近的餐厅\",\"附近的厕所\"],\"opacity\" : \"1\",\"color\" : \"#fe3e67\"},"
					+

					"{\"type\" : \"TV_Guide\",\"title\":\"节目预告\",\"description\" : \"晚上有什么电视节目\",\"icon_name\" : \"icn_tv.png\",\"url\" : \"local\",\"contentArray\" : [\"爸爸去哪儿哪个台放\",\"湖南卫视的节目列表\",\"妈妈咪呀什么时候放\",\"今晚有古剑奇谭吗\",\"晚上有什么电视节目\"],\"opacity\" : \"1\",\"color\" : \"#ff9434\"},"
					+

					"{\"type\" : \"more\",\"title\":\"更多用途\",\"description\" : \"看视频，查公交，设提醒等\",\"icon_name\" : \"icn_more.png\",\"url\" : \"local\",\"opacity\" : \"1\",\"color\" : \"#0cc19d\"}"
					+ "]," + "\"project_number\" : \"1405322466697\"" + "}"
					+ "}]}";
			Message message = Message.obtain(null,
					MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE);
			message.obj = str;
			MsgSender.getInstantce().sendMessageToService(message);
		} else {
			String str = HelpStatisticsUtil.getHelpDataFromFile();
			Message message = Message.obtain(null,
					MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE);
			message.obj = str;
			MsgSender.getInstantce().sendMessageToService(message);
		}
	}

	public void registerClient() {
		if (!isRegister) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (mtServiceMessenger == null) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					Bundle bundle = new Bundle();
					bundle.putString("key", getApplicationContext()
							.getPackageName());
					Message msg = Message.obtain(null,
							MsgConst.CLIENT_ACTION_REGISTER_CLIENT_MESSENGER);
					msg.replyTo = mMessenger;
					msg.setData(bundle);
					msg.arg1 = (mFirstRegister ? 1 : 0);
					msg.arg2 = (GlobalData.isUserLoggedin() ? 1 : 0);

					MsgSender.getInstantce().sendMessageToService(msg);
					if (mFirstRegister) {

						showHelpGuideView();
						msg = Message.obtain(null,
								MsgConst.CLIENT_ACTION_WELCOME);
						MsgSender.getInstantce().sendMessageToService(msg);
						if (SavedData.isHttpMode())
							MsgSender.getInstantce().sendMessageToService(
									MsgConst.CLIENT_ACTION_INIT_COMMUNICATION,
									1, 0);
						if (needAddCommondata) {
							MsgSender.getInstantce().sendMessageToService(
									MsgConst.CLIENT_ACTION_ADD_COMMONDATA, null);
							needAddCommondata = false;
						}
						if (needStartCapture) {
							NotifyUiHandler
									.getInstantce()
									.sendEmptyMessageDelayed(
											MsgConst.CLIENT_ACTION_START_CAPTURE,
											500);
							needStartCapture = false;
						}
						if (AutoUpdate.isDownloading() == false) {
							CommunicationUpdateUtil communicationUpdateUtil = new CommunicationUpdateUtil(
									NewAssistActivity.this);
							communicationUpdateUtil.getDataFromServer();
						}
						mFirstRegister = false;
					} else {
						if (needAddCommondata) {
							MsgSender.getInstantce().sendMessageToService(
									MsgConst.CLIENT_ACTION_ADD_COMMONDATA, null);
							needAddCommondata = false;
						}
						if (needStartCapture) {
							MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE,
									null);
							needStartCapture = false;
						}
					}
					if (isStartFromBluetooth) {
						NotifyUiHandler
								.getInstantce()
								.sendEmptyMessageDelayed(
										MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING,
										500);
						isStartFromBluetooth = false;
					}
					isRegister = true;
				}
			}).start();
		} else {
			if (DEBUG)
				Log.d(TAG, "is registered.");
			if (needStartCapture) {
				MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE, null);
				needStartCapture = false;
			}
			if (isStartFromBluetooth) {
				NotifyUiHandler.getInstantce().sendEmptyMessageDelayed(
						MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING,
						500);
				isStartFromBluetooth = false;
			}
		}
	}

	

	public boolean checkAppExist(Context context, String packageName) {
		boolean ret = false;
		if (packageName != null && !"".equals(packageName)) {
			final PackageManager packageManager = context.getPackageManager();
			List<PackageInfo> mPackageInfo = packageManager
					.getInstalledPackages(0);
			for (int i = 0; i < mPackageInfo.size(); i++) {
				String tempName = mPackageInfo.get(i).packageName;
				if (tempName != null && tempName.equals(packageName)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Floating Windows Service
	 */
	public void initFloatView() {
		Intent intent = new Intent(this, FloatViewService.class);
		intent.setAction(FloatViewService.START_LOGO_VIEW);
		this.startService(intent);
	}

	public void releaseFloatView() {
		Intent intent = new Intent(this, FloatViewService.class);
		intent.setAction(FloatViewService.STOP_LOGO_VIEW);
		this.startService(intent);
	}

	public void stopFloatView() {
		if (FloatViewService.serverIsStart) {
			FloatViewService.serverIsStart = false;
			Intent intent = new Intent(this, FloatViewService.class);
			this.stopService(intent);
		}
	}

	/**
	 * 既然延时线程
	 */
	public void exitThread() {
		new Thread() {
			public void run() {
				try {
					sleep(2000);
					isExit = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	public void onVoiceBtnClicked(int isUp) {
		hideTopView(true);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);
		mLstView.setSelection(mLstView.getAdapter().getCount() - 1);
		mMusicPlayerView.speakPause();

		MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_START_CAPTURE, isUp, 0);
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	public boolean isServerResponsed() {
		if (com.viash.voice_assistant.common.Config.WHICH_SERVER
				.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
			return mServerResponsed;
		else
			return VoiceAssistantService.mServerResponsed;
	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onFinish() {

	}

}
