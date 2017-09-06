package com.viash.voice_assistant.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.overlay.BusLineOverlay;
import com.amap.api.services.overlay.BusRouteOverlay;
import com.amap.api.services.overlay.DrivingRouteOverlay;
import com.amap.api.services.overlay.PoiOverlay;
import com.amap.api.services.overlay.WalkRouteOverlay;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.aitalk4.FilesCopyUtil;
import com.iflytek.tts.TtsService.Tts;
import com.nd.channel.NDChannel;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.umeng.analytics.MobclickAgent;
import com.via.android.voice.floatview.FloatViewIdle;
import com.via.android.voice.floatview.FloatViewService;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.adapter.CommunicationAdapter;
import com.viash.voice_assistant.adapter.HelpExpandableAdapter;
import com.viash.voice_assistant.common.AppDownloadManager;
import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voice_assistant.common.CameraPreview;
import com.viash.voice_assistant.common.Corpus;
import com.viash.voice_assistant.common.LogcatToFile;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.component.DownloadNotification;
import com.viash.voice_assistant.component.RecommendView;
import com.viash.voice_assistant.component.SettingsView;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.handler.AppCrashHandler;
import com.viash.voice_assistant.renren.PhotoServiceActivity;
import com.viash.voice_assistant.service.CheckServerMsgService;
import com.viash.voice_assistant.service.FloatViewIdleService;
import com.viash.voice_assistant.service.IMainService;
import com.viash.voice_assistant.service.LocationActionData;
import com.viash.voice_assistant.service.LockScreenService;
import com.viash.voice_assistant.service.MainService;
import com.viash.voice_assistant.service.UploadService;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.widget.HelpGuideDetailView;
import com.viash.voice_assistant.widget.HelpGuideView;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voice_assistant.widget.NetworkWarningDialog;
import com.viash.voice_assistant.widget.RotateView;
import com.viash.voice_assistant.widget.WidgetViewFactory;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.data.AppData.MapInfo;
import com.viash.voicelib.data.AppData.RouteMapInfo;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.HelpData.HelpGuideData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData.BusInfoJsonData;
import com.viash.voicelib.data.SilentInfoData;
import com.viash.voicelib.hardware.HCamera;
import com.viash.voicelib.hardware.HScreenBrightness;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.msg.MsgRaw;
import com.viash.voicelib.utils.AlarmUtil;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.CommunicationGetPageUtil;
import com.viash.voicelib.utils.CommunicationUpdateUtil;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.LogOutput;
import com.viash.voicelib.utils.MusicUtil;
import com.viash.voicelib.utils.MusicUtil.MusicInfo;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.WeiboUtil;
import com.viash.voicelib.utils.WeixinUtil;

public class CopyOfNewAssistActivity extends FragmentActivity implements
	CancelableCallback, OnBusLineSearchListener, OnPoiSearchListener, OnRouteSearchListener{
	/**
	 * Define
	 */
	private static final boolean DEBUG = true;
	private static final String TAG = "NewAssistActivity";
	private static final int MSG_WEIBO_TOKEN_RETURN = 60;
	

	/**
	 * Variablesal
	 */
	protected List<Bitmap> mVolumeBitmap = new ArrayList<Bitmap>();
	protected BroadcastReceiver mReceiver = null;
	private Corpus mCorpus;
	protected SettingsView mSettingsView;

	protected int mProcessingState = MsgConst.UI_STATE_UNINIT;
	//protected boolean mNeedAnswer = false;

	private boolean isExit = false;

	//private boolean isPause = false;
	/**
	 * Components
	 */
	protected MusicPlayerView mMusicPlayerView = null;
	protected RelativeLayout mMapView = null;
	private AMap aMap;
	protected ListView mLstView;
	protected ListView mLstMusicView;
	protected View mLayoutMain;
	protected View mLayoutText;
	protected Button mBtnSwitchToText;
	protected Button mBtnSwitchToVoice;
	protected Button mBtnSendText;
	protected EditText mEdtContent;
	protected Bitmap speakUpdateBitMap;
	protected CommunicationData mLatestServerData = null;
	private View view_welcome;
	protected WebView mWebView = null;
	protected View layout_webView = null;
	//add for basic selection whole screen show; 
	protected RelativeLayout layout_selection_whole_View = null;

	

	
	protected Button btn_last_page;
	protected Button btn_next_page;
	protected Button btn_refresh_page;
	protected CommunicationAdapter mAdapter;
	protected View mLayoutBeforeLogin;
	protected View mLayoutLoginInfo;
	protected RelativeLayout layout_login_center;
	protected ImageView mBtn_Login;
	protected ImageView btn_goback; 
	protected TextView mTv_Username;
	protected TextView mTv_Score;
	protected ImageView mIcon_Medal;
	protected ImageView mIcon_Arrow;
	protected ImageView icon_authenticate_user;

	// for drawer
	//private ImageView image_drawer;
	//private ImageView image_drawer_icon_new;
	private ImageView image_help;
	private LinearLayout layout_guide_help;
	private View mLayoutHelp;
	protected ExpandableListView mHelpView;
	private RelativeLayout layout_recommend;
	private RecommendView mRecommendView;

	boolean mMusicRequested = false;
	OnAudioFocusChangeListener mAudioListener = null;

	private Button btn_voiceSpeak;
	private Button btn_voiceHelp;
	protected ComponentName mMonitorMediaButton = null;

	private long mMsgId = 0;
	private String mMsgTitle = null;
	private String mMsgUrl = null;
	private long mVoictBtnDownTime = 0;
	private boolean mFirstRegister = true;
	protected boolean mServerResponsed = true;

	/**
	 * Map
	 */
	protected PoiOverlay mPoiOverLay = null;
	
	/**
	 * Handler
	 */
	protected Handler mHandler;

	/**
	 * Service
	 */
	protected IMainService mMainService = null;
	protected MainServiceConnection mMainServiceConnection = null;

	protected boolean isRegister = false;
	protected Messenger mtServiceMessenger = null;
	protected VoiceAssistantServiceConnection mServiceConnection = null;

	protected long mLastClickTime = 0;
	protected int mClickTimes = 0;

	private RotateView rotateview;
	private ImageView imgv_voice_volume;
	public static View layout_voice;
	protected int mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

	//private TipsView tipsView;

	WifiLocation mWifiLocation = null;

	private boolean isCalling = false;

	/**
	 * silent camera
	 */
	private Camera mCamera = null;
	private CameraPreview mPreview;
	private static int systemVolumeBeforeTaken = 0;

	public static final int MSG_LISTVIEW_TO_LAST_PAGE = 61;

	private String getMessageFromLock = null;

	//public static boolean isUserLoggedin = false;

	//for bluetooth
	private ProgressDialog mSearchingDialog;
	
	/**
	 * for busline 
	 */
	private String start_stop = null;
	private String end_stop = null;
	
	/**
	 * for POI search and route search
	 */
	private PoiSearch.Query mPOIQuery;
	private BusRouteQuery mBusRouteQuery;
	private DriveRouteQuery mDriveRouteQuery;
	private WalkRouteQuery mWalkRouteQuery;
	private RouteSearch mRouteSearch;
    private NetworkWarningDialog networkWarningDialog = null;
    private static boolean musicPlayRemind = true;
    private static boolean videoPlayRemind = true;
    private static boolean downloadRemind = true;
    private HelpGuideView helpGuideView;
    private HelpGuideDetailView helpGuideDetailView; 
    private HelpExpandableAdapter helpExpandableAdapter;
    private int guideDisplayDelay = 0;
    private boolean isBaidu = false;
    private boolean needAddCommondata = false;
    private boolean needStartCapture = false;
    private boolean isStartFromBluetooth = false;
    private boolean isNeedUnlock = false;
    private Bitmap  welcomePageBitmap = null;
    private BitmapDrawable  welcomePageDrawable = null;
   
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SavedData.init(getApplicationContext());
		if(SavedData.isHttpMode())
			mServerState = MsgConst.STATE_SERVER_CONNECTED;
		MobclickAgent.onError(this);
		Intent service = new Intent();
		service.setClass(this, CheckServerMsgService.class);
		startService(service);
		
		if(SavedData.isHttpMode())
		{
			Intent serviceSdk = new Intent();
			serviceSdk.setClass(this,  VoiceSdkService.class);
			startService(serviceSdk);
		}
			
		mWifiLocation = WifiLocation.getInstance(this);

		long time = SystemClock.currentThreadTimeMillis() - 60 * 60000;
//		SystemClock.setCurrentTimeMillis(time);
		parseDataFromIntent(getIntent());

		if (DEBUG)
			Log.d(TAG, "onCreate()");
		saveDebugToFile(TAG, "onCreate------begin");
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		GlobalData.init(this);
		super.onCreate(savedInstanceState);
		saveDebugToFile(TAG, "onCreate------middle");
		setContentView(R.layout.activity_assist);
		
		initHandler();
		initControlName();
		openSplash();

		initUI();
		initNetWorkRemindVariable();
		mHandler.postDelayed(new InitData(), 100);
		saveDebugToFile(TAG, "onCreate------end");
		// Auto update
		DownloadNotification.init(this, this);

		/*if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
			AutoUpdateHandler autoUpdateHandler = new AutoUpdateHandler(
					getApplicationContext(), false);
			// UpdateNotification.init(this);
			AutoUpdate.init(getApplicationContext(), autoUpdateHandler);

			if (!AutoUpdate.start()) {
				// start fail...
			}
		} else {
			UmengUpdateAgent.update(this);			
		}*/

		mPreview = new CameraPreview(this, null);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview, new LayoutParams(1, 1));// preview.addView(mPreview);//
		if(FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE == false)
		  startService(new Intent(CopyOfNewAssistActivity.this,FloatViewIdleService.class));	
		
//		new Thread(){
//			public void run() {
//				// initBoruiTTS
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				Tts.playText(NewAssistActivity.this, "欢迎使用小简", null);
//			};
//		}.start();
		
		Tts.initTTS(this);
		
	}

	private PictureCallback pictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (DEBUG)
				Log.d(TAG, "onPictureTaken");
			if (data == null) {
				if (DEBUG)
					Log.d(TAG, "onPictureTaken data is null");
				CustomToast.makeToast(CopyOfNewAssistActivity.this, getResources().getString(R.string.newassistactivity_take_pic_fail));//,
						//Toast.LENGTH_LONG).show();
				mHandler.sendEmptyMessageDelayed(
						MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE, 200);
				return;
			}
				
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File path = new File(Environment.getExternalStorageDirectory(),
						"/dcim/");
				if (!path.exists())
					path.mkdirs();
				String pictureFile = path.getAbsolutePath() + "/OLA"
						+ System.currentTimeMillis() + ".jpeg";
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					String toastStr = getResources().getString(R.string.newassistactivity_take_pic_succ) + pictureFile;
					if (DEBUG)
						Log.d(TAG, toastStr);
					CustomToast.makeToast(CopyOfNewAssistActivity.this, toastStr);
							//Toast.LENGTH_LONG).show();
					mHandler.sendEmptyMessageDelayed(
							MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE, 200);
				} catch (FileNotFoundException e) {
					Log.e(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "Error accessing file: " + e.getMessage());
				}
			} else {
				if (DEBUG)
					Log.d(TAG, getResources().getString(R.string.newassistactivity_no_sd_card));
				CustomToast.makeToast(CopyOfNewAssistActivity.this, getResources().getString(R.string.newassistactivity_no_sd_card));
						//Toast.LENGTH_SHORT).show();
			}
		}
	};

	public void restartCamera() {
		if (mCamera == null) {
			if (DEBUG)
				Log.d(TAG, "restartCamera");
			mCamera = HCamera.getCameraInstance();
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.set("orientation", "portrait");
			parameters.set("rotation", 90);
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.set("jpeg-quality", 100);
			DisplayMetrics dm = new DisplayMetrics();
		    getWindowManager().getDefaultDisplay().getMetrics(dm);
		    int screenWidth = dm.widthPixels;
		    int screenHeight = dm.heightPixels;
		    try{
				List<Size> lsize = parameters.getSupportedPictureSizes();
				Size tempSize = lsize.get(0);
				if(lsize.size() > 1)
				{
					for(int i=1;i<lsize.size();i++)
					{
						if(tempSize.width < lsize.get(i).width)//width is larger than height
							tempSize = lsize.get(i);
					}				
				}
				
				int height = (tempSize.width*screenWidth)/screenHeight;
				for(int i=0;i<lsize.size();i++)
				{
					if(lsize.get(i).width == tempSize.width)
					{
						if(Math.abs(lsize.get(i).height - height) < 50)
						{
							tempSize = lsize.get(i);
							break;
						}
					}
				}
				if((tempSize.width > 0) && (tempSize.height > 0))
				   parameters.setPictureSize(tempSize.width, tempSize.height);
		    }catch(Exception e)
		    {
		    	e.printStackTrace();
		    }
			mPreview.setCamera(mCamera);
			if (mCamera != null) {
				mCamera.setParameters(parameters);				
				mCamera.startPreview();
			}
		}
	}

	public void releaseCamera() {
		if (mCamera != null) {
			if (DEBUG)
				Log.d(TAG, "releaseCamera");
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	public void takePictureByCamera(int preview) {
		if (DEBUG)
			Log.v(TAG, "takePictureByCamera preview:" + preview);
		// if preview is 1, open built-in camera app, now ignore
		// if preview is 0 then
		if (mCamera == null) {
			restartCamera();
		}
		if (mCamera != null) {
			mHandler.removeMessages(MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE);
			mHandler.sendEmptyMessageDelayed(MsgConst.MSG_CAMERA_OPERATION, 100);
		} else {
			if (DEBUG)
				Log.v(TAG, "mCamera null");
		}
	}

	public void cameraOperation() {
		if (mCamera != null) {
			// before take pictures turn off the camera volume
			AudioManager audioManager = (AudioManager) CopyOfNewAssistActivity.this
					.getSystemService(Context.AUDIO_SERVICE);
			systemVolumeBeforeTaken = audioManager
					.getStreamVolume(AudioManager.STREAM_SYSTEM);
			if (systemVolumeBeforeTaken != 0) {
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			if (DEBUG)
				Log.v(TAG, "cameraOperation");
			mCamera.takePicture(null, null, pictureCallback);
		}
	}

	public void cameraRestoreToBefore() {
		releaseCamera();
		// The corresponding method of camera sound recovery
		if (systemVolumeBeforeTaken != 0) {
			AudioManager audioManager = (AudioManager) CopyOfNewAssistActivity.this
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
					systemVolumeBeforeTaken,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
	}

	@Override
	protected void onNewIntent(Intent arg0) {
		parseDataFromIntent(arg0);
		checkServerNotification();
		hideTopView(true);
		super.onNewIntent(arg0);
	}

	protected void parseDataFromIntent(Intent intent) {
		if (intent != null) {
			mMsgId = intent.getLongExtra(MsgConst.SERVER_MSG_ID, 0);
			mMsgTitle = intent.getStringExtra(MsgConst.SERVER_MSG_TITLE);
			mMsgUrl = intent.getStringExtra(MsgConst.SERVER_MSG_URL);
			guideDisplayDelay = intent.getIntExtra(GuideActivity.DELAY, 0);
			isBaidu = intent.getBooleanExtra(GuideActivity.IS_BAIDU, false);
			needAddCommondata = intent.getBooleanExtra(FloatViewIdle.START_FROM_FLOAT_VIEW, false);
			needStartCapture = intent.getBooleanExtra(SpeechRecognizer.START_FROM_OFFLINE_RECORD, false);
			isStartFromBluetooth = intent.getBooleanExtra(GuideActivity.IS_START_FROM_BLUETOOTH, false);
			isNeedUnlock = intent.getBooleanExtra(GuideActivity.IS_NEED_UNLOCK, false);
		}
	}

	protected void checkServerNotification() {
		if (mMsgId > 0) {
			CommunicationData data = new CommunicationData(
					DataConst.FROM_NOTIFY);
			data.setNotifyInfo(mMsgTitle,mMsgUrl);
			mAdapter.addData(data);
			HelpStatisticsUtil.putContentToJsonObject(mMsgId);
			mMsgId = 0;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG)
			Log.d(TAG, "onRestart()");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		if (DEBUG)
			Log.d(TAG, "onDestroy()");
		saveDebugToFile(TAG, "onDestroy------begin");

		// requestMusicFocus(false);
		
		sendMessageToService(
				MsgConst.CLIENT_ACTION_CLEAR_TALK_HISTORY, 0, 0);

		uninitBroadcastReceiver();
		//unregeisterMusicControlBroadcastReceiver();
		mMusicPlayerView.closeMusicServer();

		// stopFloatView();
		// if (FloatView.getStatus() != 0)
		// FloatView.unBindFromMediaPlayerservice(this);

		stopVoiceAssistantService();

		if (UserData.isLockHomekeyEnable(this)) {
			Intent intent2 = new Intent();
			if(LockScreenService.packagenameString != null)
			{
				ComponentName comp = new ComponentName(LockScreenService.packagenameString, LockScreenService.ClassnameString);
				intent2.setComponent(comp);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(intent2);
			}
		}
		
		saveDebugToFile(TAG, "onDestroy------middle");
		super.onDestroy();
		saveDebugToFile(TAG, "onDestroy------end");
		// System.runFinalizersOnExit(true);
		// System.exit(0);
		releaseCamera();
		CustomToast.setToastAvailable(false);
		if(NetWorkUtil.isWIFIConnected(CopyOfNewAssistActivity.this))
		{
		   if(FilesCopyUtil.isHaveFileInDirectory(AppCrashHandler.filePath))	
		      startService(new Intent(CopyOfNewAssistActivity.this,UploadService.class));
		}
		FloatViewIdle.IS_START_FROM_FLOAT_VIEW_IDLE = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: requestCode(" + requestCode + ")"
				+ " resultCode(" + resultCode + ")");
	/*	if (requestCode == LOGIN_ACTIVITY_REQUESTCODE && resultCode == LOGIN_ACTIVITY_RESULTCODE_LOGIN) {
			updateStatusView();
		} else if (requestCode == ACCOUNT_CENTER_ACTIVITY_REQUESTCODE && resultCode == ACCOUNT_CENTER_ACTIVITY_RESULTCODE_LOGOUT) {
			sendMessageToService(MsgConst.CLIENT_ACTION_USER_LOGOUT, 0, 0);
		}*/
		WeiboUtil.onActivityResult(requestCode, resultCode, data);
	}

	void onSettingIp() {
		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
			ColorDrawable drawable = new ColorDrawable(0);
			drawable.setBounds(0, 0, 1, 1);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle(R.string.settings)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String ipTemp = SavedData.getmIP();
									boolean server_temp = SavedData.isHttpMode();
									boolean server = mSettingsView.getAndSaveServerMode();
									String ip = mSettingsView
											.getAndSaveIPAddress();
									int port = mSettingsView.getAndSavePort();
									if (server != server_temp) {
										if(Tts.isPlaying())
											Tts.stop(Tts.TTS_NORMAL_PRIORITY);
										Intent intent = new Intent();
										intent.setAction(AppData.COM_VIASH_VOICE_ASSISTANT_REBOOT);										
										stopVoiceAssistantService();
										if(!server) {
											SavedData.setmPort(80);
											stopService(new Intent(CopyOfNewAssistActivity.this,VoiceSdkService.class));
										}
										else {
											stopService(new Intent(CopyOfNewAssistActivity.this,VoiceAssistantService.class));
											SavedData.setmPort(0);
										}
										sendBroadcast(intent);
										CopyOfNewAssistActivity.this.finish();
										return;
									}
									
									/*if((!ip.equals(ipTemp))&&((ip.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
											    ||(ipTemp.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))))
									{
										
									}*/
									else if(!server)
									{
										if (!ip.equals(VoiceAssistantService.SERVER)
												|| port != VoiceAssistantService.PORT
												|| VoiceAssistantService.mServerState != MsgConst.STATE_SERVER_CONNECTED) {
											sendMessageToService(
													MsgConst.CLIENT_ACTION_INIT_COMMUNICATION,
													1, 0);
										}
									}else {
										if (!ip.equals(VoiceSdkService.SERVER)
												|| port != VoiceSdkService.PORT
												|| VoiceSdkService.mServerState != MsgConst.STATE_SERVER_CONNECTED) {
											sendMessageToService(
													MsgConst.CLIENT_ACTION_INIT_COMMUNICATION,
													1, 0);
										}
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							}).setIcon(drawable);

			mSettingsView = (SettingsView) this.getLayoutInflater().inflate(
					R.layout.dlg_settings, null);

			builder.setView(mSettingsView);
			builder.show();
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
	protected void onStart() {
		if (DEBUG)
			Log.d(TAG, "onStart()");
		saveDebugToFile(TAG, "onStart------begin");
		super.onStart();
		saveDebugToFile(TAG, "onStart------middle");
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
		saveDebugToFile(TAG, "onStart------end");
	}

	@Override
	protected void onResume() {
		if (DEBUG)
			Log.d(TAG, "onResume()");
		//isPause = false;
		MobclickAgent.onResume(this);
		GlobalData.setNewAssistantAcitivityRunFlag(true);
		
		HScreenBrightness.setActivity(this);
		saveDebugToFile(TAG, "onResume------begin");
		super.onResume();
		saveDebugToFile(TAG, "onResume------end");
		registerClient();
		updateStatusView();

		setProcessingState(MsgConst.UI_STATE_INITED);
		if(mMusicPlayerView != null) {
			mMusicPlayerView.speakRecover();
			mMusicPlayerView.updateMusicViewStatus();
		}
		isCalling = false;
		// restartCamera();
		/*if (mFirstRegister) {
			return;
		}*/
		
		getMessageFromLock = SavedData.getLockMessage();
		if (getMessageFromLock != null && !getMessageFromLock.equals("") ) {
			if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
				Message textmsg = mHandler
						.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
				textmsg.obj = getMessageFromLock;
				mHandler.sendMessageDelayed(textmsg, 2000);
				getMessageFromLock = null;
				SavedData.setLockMessage(null);
			}
			else if (VoiceAssistantService.mServerState  == MsgConst.STATE_SERVER_CONNECTED && !mFirstRegister) {
				Message textmsg = mHandler
						.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
				textmsg.obj = getMessageFromLock;
				mHandler.sendMessage(textmsg);
				getMessageFromLock = null;
				SavedData.setLockMessage(null);
			}
		}						
	}

	@Override
	protected void onPause() {
		if (DEBUG)
			Log.d(TAG, "onPause()");
		MobclickAgent.onPause(this);
		//isPause = true;

		//clearTipsEvent(true);

		saveDebugToFile(TAG, "onPause------begin");
		if (!isCalling) {
			mMusicPlayerView.speakRecover();
		}
		cancelRecognize();
		super.onPause();
		saveDebugToFile(TAG, "onPause------middle");
		releaseCamera();
		saveDebugToFile(TAG, "onPause------end");
	}

	@Override
	protected void onStop() {
		if (DEBUG)
			Log.d(TAG, "onStop()");
		HScreenBrightness.setActivity(null);
		saveDebugToFile(TAG, "onStop------begin");
		super.onStop();
		unregisterClient();
		saveDebugToFile(TAG, "onStop------middle");
		if (mWifiLocation != null)
		 mWifiLocation.close();

		Tts.stop();
		saveDebugToFile(TAG, "onStop------end");
		GlobalData.setNewAssistantAcitivityRunFlag(false);
		HelpStatisticsUtil.saveJsonObjectToFile();
	}

	@Override
	public void finish() {
		if (DEBUG)
			Log.d(TAG, "finish()");
		Tts.destroy();
		
		// getContentResolver().unregisterContentObserver(myContentObserver);
		// if (mSpeechRecognizer != null)
		// mSpeechRecognizer.destroy();

		super.finish();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int actionType = ev.getAction();
		switch (actionType) {
		case MotionEvent.ACTION_UP:
			// Log.d(TAG, "dispatchTouchEvent ACTION_UP");
			/*if (tipsView.getVisibility() == View.VISIBLE) {
				tipsView.setVisibility(View.GONE);
			}

			startTipsEvent();*/
			break;
		}

		return super.dispatchTouchEvent(ev);
	}

	protected boolean cancelRecognize() {
		boolean ret = false;
		
			if(mProcessingState == MsgConst.UI_STATE_SPEAKING)
			{
				sendMessageToService(MsgConst.CLIENT_ACTION_CANCEL_RECORD, 0, 0);
				ret = true;
			}

		return ret;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (layout_recommend.getVisibility() == View.VISIBLE) {
				layout_recommend.setVisibility(View.INVISIBLE);
			}
			else  if((mMapView.getVisibility() == View.VISIBLE)||(layout_webView.getVisibility() == View.VISIBLE))
			{
				hideTopView(true);
			}
			else if(mLayoutHelp.getVisibility() == View.VISIBLE)
			{
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
			}
			else if(layout_guide_help.getVisibility() == View.VISIBLE)
			{
				helpGuideDetailView = null;
				mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
			}			
			else if (layout_selection_whole_View.getVisibility() == View.VISIBLE) {
				layout_selection_whole_View.removeAllViews();
				layout_selection_whole_View.setVisibility(View.GONE);
		
			}else {
				if (cancelRecognize()) {
					return true;
				} else if (hideTopView(false)) {
					return true;
				}
				Tts.stop(Tts.TTS_NORMAL_PRIORITY);
				if(SavedData.isVoiceWakeUpOpen())
					sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE,0,0);
				if (isExit) {
					finish();
				} else {
					isExit = true;
					CustomToast.showShortText(CopyOfNewAssistActivity.this,
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
			// return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK)
		{
			onVoiceBtnClicked(0);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	protected void startDumpDialogText() {
		MsgRaw msgRaw = new MsgRaw();
		msgRaw.setmId(MsgConst.TS_C_DUMP);
		byte[] data = msgRaw.prepareRawData();
		if (data != null) {
			// mSocketUtil.sendMessage(data, true);
			Bundle bundle = new Bundle();
			bundle.putByteArray("data", data);

			sendMessageToService(MsgConst.CLIENT_ACTION_SEND_DATA_TO_SERVER,
					bundle);
		}
	}
	
	protected void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (DEBUG)
					Log.i(TAG, "msg id: " + msg.what);
				/*if (isPause) {
					return;
				}*/
				
				MapInfo mapInfo = null;

				switch (msg.what) {
				case MsgConst.MSG_ON_VIEW_TOUCH:
					if (layout_recommend.getVisibility() == View.VISIBLE) {
						layout_recommend.setVisibility(View.INVISIBLE);
					}
					break;
					
				case MsgConst.MSG_SHOW_WHOLE_SCREEN:
					if (layout_selection_whole_View.getVisibility() == View.GONE) {
						BaseData data = (BaseData)msg.obj;
						boolean operationable = false;
						if (msg.arg1 == 1) {
							operationable = true;
						}
						View view = WidgetViewFactory.getWidgetView(CopyOfNewAssistActivity.this, data, operationable, mHandler, true, msg.arg2);
						if (view != null ) {
							layout_selection_whole_View.setVisibility(View.VISIBLE);
							layout_selection_whole_View.addView(view);	
						}					
					}					
					break;
				case MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL:					
					if (layout_selection_whole_View.getVisibility() == View.VISIBLE) 
					{
						layout_selection_whole_View.removeAllViews();
						layout_selection_whole_View.setVisibility(View.GONE);
					}
			
					break;
				
				case MsgConst.MSG_DATA_FROM_TEXT:
					if((mMapView.getVisibility() == View.VISIBLE)||(layout_webView.getVisibility() == View.VISIBLE))
					{
						hideTopView(true);
					}
					sendMessageToService(
							MsgConst.CLIENT_ACTION_START_RECOGNITION, null,
							msg.obj);
					break;
				case MsgConst.MSG_DATA_FROM_TEXT_SHARE:
					Tts.stop(Tts.TTS_NORMAL_PRIORITY);
			
					break;
				case MSG_WEIBO_TOKEN_RETURN:
					JSONArray array = new JSONArray();
					array.put(msg.obj);
					Bundle bundle = new Bundle();
					bundle.putString("type", "sina_weibo_token");

					sendMessageToService(
							MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
							bundle, array);
					WeiboUtil.releaseWeibo();
					break;
				case MsgConst.MSG_DATA_FROM_OPTION:
					sendMessageToService(
							MsgConst.CLIENT_ACTION_SELECTION_ANSWER, msg.arg1,
							msg.arg2);
					if (layout_recommend.getVisibility() == View.VISIBLE) {
						layout_recommend.setVisibility(View.INVISIBLE);
					}
					break;
				case MsgConst.MSG_COPY_TEXT_FROM_ITEM:
					prepareSendText((String) msg.obj);
					break;
				case MsgConst.MSG_SHOW_MAP:
					mapInfo = (MapInfo) msg.obj;
					if (mapInfo != null)
						showMap(mapInfo);
					break;
				case MsgConst.MSG_SHOW_MAP_BUSINFO:
					BusInfoJsonData data = (BusInfoJsonData) msg.obj;
					if(data != null) {
						showBusLineInfo(data, msg.arg1);
					}
					break;
				case MsgConst.MSG_ROUTE_SEARCH_RESULT:
					showRoute((RouteResult) msg.obj);
					break;
				case MsgConst.MSG_SHOW_ROUTE_MAP:
					RouteMapInfo routeInfo = (RouteMapInfo) msg.obj;
					if (routeInfo != null) {
						startNavigate((double) routeInfo.mRouteFromLatitude[0],
								(double) routeInfo.mRouteFromLongitude[0],
								(double) routeInfo.mRouteToLatitude[0],
								(double) routeInfo.mRouteToLongitude[0],
								(int) routeInfo.mRouteMode[0]);
					}
					break;
				case MsgConst.MSG_SEARCH_POS:
					mapInfo = (MapInfo) msg.obj;
					if (mapInfo != null)
						searchPos(mapInfo);
					break;
				case MsgConst.MSG_NAVIGATE_TO:
					if (msg.arg1 == 0) {
						LatLng point = (LatLng) msg.obj;
						double toLat = point.latitude;
						double toLong = point.longitude;
						if (toLat != 0 && toLong != 0)
							startNavigate(0, 0, toLat, toLong, 0);
					}
					else {
						startNavigate((String) msg.obj);
					}
					break;
				case MsgConst.MSG_SHOW_INTERNAL_WEB:
					showInternalWeb((String) msg.obj, ((msg.arg1 == 1) ? true
							: false));
					break;
				case MsgConst.MSG_SHOW_WEB:
					Intent intent = new Intent();
					Uri uri = Uri.parse((String) msg.obj);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(uri);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						CustomToast.makeToast(CopyOfNewAssistActivity.this,
								getResources().getString(R.string.newassistactivity_can_not_open) +	"\"" + uri.getPath() + "\"");
								//Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					break;
				case MsgConst.MSG_FORCE_STOP_TTS:
					try {
						if (mtServiceMessenger != null)
							mtServiceMessenger
									.send(Message
											.obtain(null,
													MsgConst.CLIENT_ACTION_STOP_TTS));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					mMusicPlayerView.speakRecover();
					break;
				case MsgConst.MSG_DOWNLOAD_APP:
					String title_str = msg.getData().getString("title");
					String logoPath = msg.getData().getString("logo");
					
					if (!AppDownloadManager.startDownload(
							getApplicationContext(), mHandler, title_str,
							(String) msg.obj, logoPath)) {
						CustomToast.makeToast(CopyOfNewAssistActivity.this, getResources().getString(R.string.newassistactivity_sd_card_not_exist));
								//Toast.LENGTH_SHORT).show();
					}
					break;
				case MsgConst.MSG_SIM_VOICE_BUTTON:
					Log.i("Media", "MSG_SIM_VOICE_BUTTON");
					onVoiceBtnClicked(0);
					break;
				/*case MsgConst.MSG_CHECK_TTS:
					startTipsEvent();
					break;*/
				// Minimize the music view
				case MSG_LISTVIEW_TO_LAST_PAGE:
					mLstView.setSelection(mLstView.getAdapter().getCount() - 1);
					break;
				// show tips				
				/*case MsgConst.MSG_SHOW_TIPS:
					clearTipsEvent(true);
					if ((tipsView.getVisibility() != View.VISIBLE) && SavedData.isHelpTips()) {
						if (tipsView.refreshTipData()) {
							// Log.d(TAG, "tips update success");
							tipsView.setVisibility(View.VISIBLE);
							mHandler.sendEmptyMessageDelayed(
									MsgConst.MSG_TIPS_TIMEOUT,
									tipsView.getDurationTime());
						} else {
							Log.d(TAG,
									"tips not updated, as all tips was displayed in one week");
							tipsView.setVisibility(View.GONE);
						}
					}
					break;
				case MsgConst.MSG_TIPS_TIMEOUT:
					clearTipsEvent(false);
					if (tipsView.getVisibility() == View.VISIBLE) {
						tipsView.setVisibility(View.GONE);
						startTipsEvent();
					}
					break;*/
				case MsgConst.MSG_DRAWER_UPDATE_SCROLLVIEW_LAYOUT:
					//updateDrawerLayout();
					break;
				case MsgConst.MSG_DRAWER_UPDATE_PAGE_LAYOUT:
					/*int totalPages = (int) msg.arg1;
					int currentPage = (int) msg.arg2;*/
					//updateDrawerPageLayout(totalPages, currentPage);
					break;
				case MsgConst.MSG_CAMERA_OPERATION:
					cameraOperation();
					break;
				case MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE:
					cameraRestoreToBefore();
					break;
				case VoiceAssistantService.MSG_UI_SEARCHING_START:
					mSearchingDialog = new ProgressDialog(CopyOfNewAssistActivity.this);
					mSearchingDialog.setTitle(getResources().getString(R.string.bluetooth_searching_title));
					mSearchingDialog.setMessage(getResources().getString(R.string.bluetooth_searching_message));
					mSearchingDialog.show();
					break;
				case VoiceAssistantService.MSG_UI_SEARCHING_FOUND:
					mSearchingDialog.cancel();
					break;
				case MsgConst.CLIENT_ACTION_REMOVE_DATA:
					if (msg.obj instanceof BaseData) {
						mAdapter.removeData((BaseData)msg.obj);
					}
					if (msg.obj instanceof CommunicationData) {
						mAdapter.removeData((CommunicationData)msg.obj);
					}
					break;
				case MsgConst.CLIENT_ACTION_ADD_DATA:
					mAdapter.addData((CommunicationData)msg.obj);
					if(msg.arg1 == NetworkWarningDialog.VIDEO_PLAY_TO_REMIND)
					{
						videoPlayRemind = false;
					}else if(msg.arg1 == NetworkWarningDialog.DOWNLOAD_TO_REMIND)
					{
						downloadRemind = false;
					}
					break;
				case MsgConst.CLIENT_ACTION_SHOW_MUSIC_LIST:
					if(msg.arg1 == NetworkWarningDialog.MUSIC_PLAY_TO_REMIND)
					{
						musicPlayRemind = false;
					}
					@SuppressWarnings("unchecked")
					List<String[]> lstData = (List<String[]>) msg.obj;
					// requestMusicFocus(true);
					if(lstData != null)
					  playMusicList(lstData);
					break;
				/*case MsgConst.CLIENT_ACTION_SHOW_NEW_ICON_ON_PROMO_BUTTON:
					image_drawer_icon_new.setVisibility(View.VISIBLE);
					break;*/
				case MsgConst.CLIENT_ACTION_UPDATA_USER_LOG_STATUS:
					updateStatusView();
					break;
				case MsgConst.CLIENT_ACTION_REPORT_UI_INFO:				
					Bundle newbundle = new Bundle();
					newbundle.putString("param", (String)msg.obj);
					sendMessageToService(MsgConst.CLIENT_ACTION_REPORT_UI_INFO,newbundle);
					break;
				case MsgConst.SERVICE_ACTION_SDKCOMMAND_RESPONSE:
					mServerResponsed = true;
					Bundle bundletemp = msg.getData();
					String cmd = bundletemp.getString("commandname");
					String param1 = bundletemp.getString("param1");
					String param2 = bundletemp.getString("param2");
					mAdapter.handlerMsg(cmd, param1, param2);
					setProcessingState(MsgConst.UI_STATE_INITED);	
					break;
				case MsgConst.MSG_MUSIC_CONTROL:
					if (MusicPlayerView.musicList != null && !MusicPlayerView.musicList.isEmpty()) {
						showTopView(mMusicPlayerView);
						if (mMusicPlayerView != null) {
							mMusicPlayerView.controlMusic((String) msg.obj, msg.arg1);
						}
					}				
					break;
				case MsgConst.MSG_MUSIC_STOP:
					if (mMusicPlayerView != null) {
						mMusicPlayerView.closeMusicServer();
						mLstMusicView.setVisibility(View.GONE);
						mMusicPlayerView.setVisibility(View.GONE);
					}
					break;
				case MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE_DETAIL:
					helpGuideDetailView = new HelpGuideDetailView(CopyOfNewAssistActivity.this,mHandler,(HelpGuideData)msg.obj);
					layout_guide_help.removeAllViews();
					mBtn_Login.setImageResource(R.drawable.go_back_button);
					layout_guide_help.setVisibility(View.VISIBLE);
					layout_guide_help.addView(helpGuideDetailView.initView());
					btn_goback.setVisibility(View.VISIBLE);
					break;
				case MsgConst.CLIENT_ACTION_CLOSE_HELP_GUIDE_DETAIL:
					if(layout_guide_help.getVisibility() == View.VISIBLE)
					{
					   layout_guide_help.setVisibility(View.GONE);
					   mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
					   btn_goback.setVisibility(View.INVISIBLE);
					}
					break;
				case MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE:
					if(layout_guide_help.getVisibility() == View.VISIBLE)
					{
					   layout_guide_help.setVisibility(View.GONE);
					   mBtn_Login.setImageResource(R.drawable.statusbar_login_button);					   
					}
					if(mLayoutHelp.getVisibility() == View.VISIBLE)
					{
						mLayoutHelp.setVisibility(View.GONE);						
						mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
						HelpStatisticsUtil.helpType = null;
					}
					btn_goback.setVisibility(View.INVISIBLE);
					showHelpGuideView();
					break;
				case MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE:
					mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
					btn_goback.setVisibility(View.INVISIBLE);
					layout_guide_help.setVisibility(View.GONE);					
					HelpStatisticsUtil.helpType = null;
					HelpStatisticsUtil.currentType = null;
					break;
				case MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW:
					if(mLayoutHelp.getVisibility() == View.VISIBLE)
					{
						mLayoutHelp.setVisibility(View.GONE);
						if(layout_guide_help.getVisibility() == View.VISIBLE)
						{
						   mBtn_Login.setImageResource(R.drawable.go_back_button);
						   btn_goback.setVisibility(View.VISIBLE);
						}
						else
						{
						   mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
						   btn_goback.setVisibility(View.INVISIBLE);
						}
						HelpStatisticsUtil.helpType = null;
					}
					else
					{
					    mLayoutHelp.setVisibility(View.VISIBLE);
					    mBtn_Login.setImageResource(R.drawable.go_back_button);
					    btn_goback.setVisibility(View.VISIBLE);
					    HelpStatisticsUtil.helpType = "more";
					}
					break;
				case MsgConst.CLIENT_ACTION_START_CAPTURE:
					sendMessageToService(MsgConst.MSG_START_CAPTURE,null);
					break;
				case MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING:
					msg = Message.obtain(null, MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING);
					MsgAnswer msgAnswer = new MsgAnswer("{\"Just Talk Dialog Outputs\":[{\"Question\":{\"Present\":\"3\",\"Content\":\"主人，有什么可以帮你的？\",\"tag\":\"login\"}}]}");
					msg.obj = msgAnswer;
					msg.arg1 = isNeedUnlock ? 1: 0;
					sendMessageToService(msg);
					break;
				}
			}

		};
	}

	protected void prepareSendText(String text) {
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

	@SuppressLint("SetJavaScriptEnabled")
	protected void initControlName() {
		view_welcome = findViewById(R.id.layout_welcome);
		mLayoutMain = findViewById(R.id.layout_main);
		mLayoutText = findViewById(R.id.layout_txt);
		mLayoutText.setVisibility(View.GONE);
		layout_voice = findViewById(R.id.layout_voice);
		btn_voiceSpeak = (Button) findViewById(R.id.btn_voice_mic);
		btn_voiceHelp = (Button) findViewById(R.id.btn_voice_more);
		rotateview = (RotateView) findViewById(R.id.rotateview);
		rotateview.setImageDrawable(R.drawable.voice_load_rotate);
		mBtnSwitchToText = (Button) findViewById(R.id.btn_voice_edit);
		imgv_voice_volume = (ImageView) findViewById(R.id.imgv_volume);
		mBtnSwitchToVoice = (Button) mLayoutText
				.findViewById(R.id.btn_input_change);
		mBtnSendText = (Button) mLayoutText.findViewById(R.id.btn_confirm);
		mLstView = (ListView) mLayoutMain.findViewById(R.id.lst_communication);

		mLstMusicView = (ListView) mLayoutMain.findViewById(R.id.lv_music);
		
		mMapView = (RelativeLayout) mLayoutMain.findViewById(R.id.main_map_view);
		if (aMap == null) {
			aMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
		
		mEdtContent = (EditText) mLayoutText.findViewById(R.id.et_content);
		mWebView = (WebView) findViewById(R.id.main_web_view);
		layout_webView = findViewById(R.id.layout_webview);

		layout_selection_whole_View = (RelativeLayout)findViewById(R.id.layout_selection_view);		
		
		btn_last_page = (Button) findViewById(R.id.btn_last_page);
		btn_next_page = (Button) findViewById(R.id.btn_next_page);
		btn_refresh_page = (Button) findViewById(R.id.btn_refresh_page);

		mLayoutBeforeLogin = findViewById(R.id.layout_before_login);
		mLayoutLoginInfo = findViewById(R.id.layout_login_info);
		layout_login_center = (RelativeLayout) findViewById(R.id.layout_login_center);
		mBtn_Login = (ImageView) findViewById(R.id.btn_login);
		btn_goback = (ImageView) findViewById(R.id.btn_goback);
		mTv_Username = (TextView) findViewById(R.id.username);
		mTv_Score = (TextView) findViewById(R.id.score);
		mIcon_Medal = (ImageView) findViewById(R.id.icon_medal);
		mIcon_Arrow = (ImageView) findViewById(R.id.icon_arrow);
		icon_authenticate_user = (ImageView) findViewById(R.id.icon_authenticate_user);
		mLayoutBeforeLogin.setVisibility(View.VISIBLE);
		mLayoutLoginInfo.setVisibility(View.GONE);
		mLayoutHelp = this.findViewById(R.id.layout_help);
		
        mHelpView = (ExpandableListView) mLayoutHelp.findViewById(R.id.help);
        helpExpandableAdapter = new HelpExpandableAdapter(this,mHandler);
        mHelpView.setAdapter(helpExpandableAdapter);
        mHelpView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
        
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(dm.heightPixels >= 1024)
		    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                                     WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		mBtn_Login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if(mLayoutHelp.getVisibility() == View.VISIBLE)
				{
					mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
					return;
				}
				if(layout_guide_help.getVisibility() == View.VISIBLE)
				{
					helpGuideDetailView = null;					
					mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
					return;
				}
				if (GlobalData.isUserLoggedin()) {//UserData.isLogin(NewAssistActivity.this)) {
					//isUserLoggedin = true;
					updateStatusView();
					return;
				}
				Intent intent = new Intent(CopyOfNewAssistActivity.this,
						LoginActivity.class);
				//startActivityForResult(intent, LOGIN_ACTIVITY_REQUESTCODE);
				startActivity(intent);
			}
		});
		
		btn_goback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if(mLayoutHelp.getVisibility() == View.VISIBLE)
				{
					mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
					return;
				}
				if(layout_guide_help.getVisibility() == View.VISIBLE)
				{
					helpGuideDetailView = null;					
					mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
					return;
				}				
			}
		});
		
		layout_login_center.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CopyOfNewAssistActivity.this,
						AccountCenterActivity.class);
				//startActivityForResult(intent,ACCOUNT_CENTER_ACTIVITY_REQUESTCODE);
				startActivity(intent);
			}
		});

		/*image_drawer = (ImageView) findViewById(R.id.image_drawer);
		image_drawer_icon_new = (ImageView) findViewById(R.id.image_drawer_icon_new);
		image_drawer_icon_new.setVisibility(View.INVISIBLE);*/
		
		image_help = (ImageView) findViewById(R.id.image_help);
		layout_guide_help = (LinearLayout) findViewById(R.id.layout_guide_help);
		layout_recommend = (RelativeLayout) findViewById(R.id.layout_recommend);
		mRecommendView = new RecommendView(this.getApplicationContext(), mHandler);
		layout_recommend.addView(mRecommendView);
		layout_recommend.setVisibility(View.INVISIBLE);
		       
		mMusicPlayerView = (MusicPlayerView) mLayoutMain
				.findViewById(R.id.main_music_player);

		mMusicPlayerView.setVisibility(View.GONE);

		/*image_drawer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layout_recommend.getVisibility() == View.VISIBLE) {
					layout_recommend.setVisibility(View.INVISIBLE);
				} else {
					layout_recommend.setVisibility(View.VISIBLE);
					image_drawer_icon_new.setVisibility(View.INVISIBLE);
					mRecommendView.updateDrawerLayout();
				}
			}
		});*/
		
		//CheckNewAppAndCoupons();//startCheckApps();remove temporary
		
		image_help.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL));
				//mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE));
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
			}			
		});

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("http://") || url.startsWith("https://")) {
					mWebView.loadUrl(url);
				}
				else {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					try	{
						startActivity(intent);
					}
					catch(ActivityNotFoundException e) {
						e.printStackTrace();
						return false;
					}
				}
				return true;
			}
		});
		mWebView.getSettings().setJavaScriptEnabled(true);
		mBtnSwitchToText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				layout_voice.setVisibility(View.GONE);
				mLayoutText.setVisibility(View.VISIBLE);
				cancelRecognize();
			}
		});
		btn_last_page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mWebView.canGoBack())
					mWebView.goBack();
			}
		});
		btn_next_page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mWebView.canGoForward())
					mWebView.goForward();

			}
		});
		btn_refresh_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mWebView.reload();

			}
		});
		mBtnSwitchToVoice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				layout_voice.setVisibility(View.VISIBLE);
				mLayoutText.setVisibility(View.GONE);
				hideSoftKeyboard();
				if (SavedData.isAllowWakeupByAudio()) {
					AiTalkShareData.setLeaveMainInterfaceFlag(false);
					try {
						if (mtServiceMessenger != null)
							mtServiceMessenger.send(Message
									.obtain(null,
											MsgConst.CLIENT_ACTION_REENTRY_WAKEUP_BY_AUDIO));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btn_voiceHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(CopyOfNewAssistActivity.this, SettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(intent);
				try {
					if (mtServiceMessenger != null)
						mtServiceMessenger.send(Message.obtain(
								null,// send message to stop VR
								MsgConst.CLIENT_ACTION_SET_WAKEUP_BY_AUDIO));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				AiTalkShareData.setLeaveMainInterfaceFlag(true);// Leave main screen flag.
				if(SavedData.isVoiceWakeUpOpen())
					sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE,0,0);
			}
		});

		mBtnSendText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideTopView(true);
				boolean bSpecial = false;
				String text = mEdtContent.getText().toString();
				if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
					if (text.length() == 0) {
						long curTime = System.currentTimeMillis();
						if (curTime - mLastClickTime < 1000) {
							mLastClickTime = System.currentTimeMillis();
							onSettingIp();
						} else {
							mLastClickTime = curTime;
						}
						bSpecial = true;
					} else if (text.equalsIgnoreCase("dump")) {
						long curTime = System.currentTimeMillis();
						if (curTime - mLastClickTime < 1000) {
							mLastClickTime = System.currentTimeMillis();
							startDumpDialogText();
						} else {
							mLastClickTime = curTime;
						}
						bSpecial = true;
					} else if (text.equalsIgnoreCase("channel")) {
						String channel = getCurrentChannel();
						channel = "Channel:" + channel;
						CustomToast.makeToast(CopyOfNewAssistActivity.this,
								(String) channel);//, Toast.LENGTH_SHORT).show();
						bSpecial = true;
					} else if (text.startsWith("tts:")) {
						String subString = text.substring(4);
						int index = subString.indexOf(',');
						if (index > 0) {
							try {
								short speed = (short) Integer
										.parseInt(subString.substring(0, index));
								short pitch = (short) Integer
										.parseInt(subString
												.substring(index + 1));

								long curTime = System.currentTimeMillis();
								if (curTime - mLastClickTime < 1000) {
									mLastClickTime = System.currentTimeMillis();
									Tts.setSpeed(speed);
									Tts.setPitch(pitch);
									mMusicPlayerView.speakPause();
								} else {
									mLastClickTime = curTime;
								}
								bSpecial = true;
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					}
					else if (text.startsWith("voicebutton:")) {
						String subString = text.substring(12);
						if(subString.length() > 0)
						{
							int value = 0;
							try
							{
								value = Integer.parseInt(subString);
								bSpecial = true;
							}
							catch(NumberFormatException e)
							{
								e.printStackTrace();
							}
							
							if(value != 0)
								SavedData.setmShowTopVoiceButton(true);
							else
								SavedData.setmShowTopVoiceButton(false);							
						}
					}
					else if (text.equalsIgnoreCase("logcat")) {
						LogcatToFile.getInstance(CopyOfNewAssistActivity.this).start();
						CustomToast.makeToast(CopyOfNewAssistActivity.this,
								"Logcat Start.");//, Toast.LENGTH_SHORT).show();
						bSpecial = true;
					}
					else if (text.equalsIgnoreCase("stop")) {
						LogcatToFile.getInstance(CopyOfNewAssistActivity.this).stop();
						CustomToast.makeToast(CopyOfNewAssistActivity.this,
								"Logcat Stop.");//, Toast.LENGTH_SHORT).show();
						bSpecial = true;
					}
				}

				hideSoftKeyboard();
				if (text.length() > 0 && !bSpecial) {
					Message msg = mHandler
							.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
					msg.obj = text;
					mHandler.sendMessage(msg);
					mEdtContent.setText("");
					mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
					//mMusicPlayerView.musicFormMinimize();
				}
			}
		});

		btn_voiceSpeak.setOnTouchListener(new Button.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mVoictBtnDownTime = System.currentTimeMillis();
					Log.i("CalculateTime", "Time down:"
							+ (mVoictBtnDownTime % 10000));
					onVoiceBtnClicked(0);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (System.currentTimeMillis() > mVoictBtnDownTime + 1200) {
						onVoiceBtnClicked(1);
					}
				}

					if (layout_selection_whole_View.getVisibility() == View.VISIBLE) {
						layout_selection_whole_View.removeAllViews();
						layout_selection_whole_View.setVisibility(View.GONE);
				
				}

				
				return true;
			}
		});

		// btn_voiceSpeak.setOnClickListener(new Button.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// onVoiceBtnClicked();
		// }
		// });

		view_welcome.setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeSplash();
			}
		});
		// bensonzhang Coupon Click Blank Collapse
		layout_voice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layout_recommend.getVisibility() == View.VISIBLE) {
					layout_recommend.setVisibility(View.INVISIBLE);
				}
			}
		});
		mLstView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (layout_recommend.getVisibility() == View.VISIBLE) {
					layout_recommend.setVisibility(View.INVISIBLE);
				}
				return false;
			}
		});// bensonzhang
		//tipsView = (TipsView) findViewById(R.id.main_tips_view);
	}

	protected void openSplash() {
		if ((view_welcome != null)&&(guideDisplayDelay != GuideActivity.GUIDE_DISPLAY_DELAY)) {
			if(VoiceAssistantService.mServerState != MsgConst.STATE_SERVER_CONNECTED){	
				/*if(isBaidu)
				{
					view_welcome.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.welcome_baidu));
				}*/
				try{
					CommunicationGetPageUtil communicationGetPageUtil = new CommunicationGetPageUtil(CopyOfNewAssistActivity.this,null);
					welcomePageBitmap = communicationGetPageUtil.isNeedShowWelcomePage(this);
					if(welcomePageBitmap != null)
					{
					   welcomePageDrawable = new BitmapDrawable(welcomePageBitmap);
					   view_welcome.setBackgroundDrawable(welcomePageDrawable);					   
					}
					else if(isBaidu)
					{
						view_welcome.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.welcome_baidu));
					}
					communicationGetPageUtil = null;
				}catch(Exception e)
				{
					e.printStackTrace();
					if(isBaidu)
					{
						view_welcome.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.welcome_baidu));
					}
				}
				view_welcome.setVisibility(View.VISIBLE);
			}
		}
	}

	protected void closeSplash() {
		if ((view_welcome != null)&&(guideDisplayDelay != GuideActivity.GUIDE_DISPLAY_DELAY)) {
			try{				
				Thread.sleep(700);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			view_welcome.setVisibility(View.GONE);
			if(welcomePageBitmap != null)
			{
				welcomePageBitmap.recycle();
				welcomePageBitmap = null;
			}
			if(welcomePageDrawable != null)
			{
				welcomePageDrawable = null;
			}
		}
	}
	     
	protected boolean hideTopView(boolean all) {
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
				setProcessingState(MsgConst.UI_STATE_INITED);
			}
		}
		
		if (layout_recommend.getVisibility() == View.VISIBLE) {
			layout_recommend.setVisibility(View.INVISIBLE);
		}
		return ret;
	}

	protected void showTopView(View topView) {
		mMapView.setVisibility(View.GONE);
		layout_webView.setVisibility(View.GONE);
		topView.setVisibility(View.VISIBLE);
	}

	protected void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEdtContent.getWindowToken(), 0);
	}

	protected void updateStatusView() {
		if (GlobalData.isUserLoggedin()){// && isUserLoggedin) {
			mLayoutBeforeLogin.setVisibility(View.GONE);
			mLayoutLoginInfo.setVisibility(View.VISIBLE);
			String[] userInfo = UserData.getUserInfo(this);
			String username = "";
			if (userInfo.length > 0) {
				username = userInfo[0];
			}

			mTv_Username.setText(username);

			int resId = 0;
			int userLevel = SavedData.getmUserLevel();
			switch (userLevel) {
			case 1:
				resId = R.drawable.statusbar_bronze_1;
				break;
			case 2:
				resId = R.drawable.statusbar_bronze_2;
				break;
			case 3:
				resId = R.drawable.statusbar_bronze_3;
				break;
			case 4:
				resId = R.drawable.statusbar_bronze_4;
				break;
			case 5:
				resId = R.drawable.statusbar_silver_1;
				break;
			case 6:
				resId = R.drawable.statusbar_silver_2;
				break;
			case 7:
				resId = R.drawable.statusbar_silver_3;
				break;
			case 8:
				resId = R.drawable.statusbar_silver_4;
				break;
			case 9:
				resId = R.drawable.statusbar_gold_1;
				break;
			case 10:
				resId = R.drawable.statusbar_gold_2;
				break;
			case 11:
				resId = R.drawable.statusbar_gold_3;
				break;
			case 12:
				resId = R.drawable.statusbar_gold_4;
				break;
			default:
				resId = R.drawable.statusbar_gold_4;
				break;
			}
			if (userLevel <= 0) {
				mIcon_Medal.setVisibility(View.INVISIBLE);
			} else {
				mIcon_Medal.setVisibility(View.VISIBLE);
				mIcon_Medal.setImageResource(resId);
			}

			mTv_Score.setText(String.valueOf(SavedData.getmUserScore()));
			if (SavedData.getmUserSpecialTime() == 0) {
				mIcon_Arrow.setVisibility(View.GONE);
			} else {
				mIcon_Arrow.setVisibility(View.VISIBLE);
				if (userLevel / 4 == 0) {
					mIcon_Arrow.setImageResource(R.drawable.statusbar_up_1);
					mTv_Score.setTextColor(getResources().getColor(
							R.color.statusbar_score_bronze));
				} else if (userLevel / 4 == 1) {
					mIcon_Arrow.setImageResource(R.drawable.statusbar_up_2);
					mTv_Score.setTextColor(getResources().getColor(
							R.color.statusbar_score_silver));
				} else if (userLevel / 4 == 2) {
					mIcon_Arrow.setImageResource(R.drawable.statusbar_up_3);
					mTv_Score.setTextColor(getResources().getColor(
							R.color.statusbar_score_gold));
				}
			}
			

			long currentDate = System.currentTimeMillis();
			long expireDate = UserData.getVCodeExpireDate(this);
			if (UserData.isPhoneBinded(this)) {
				if (currentDate < expireDate) {
					icon_authenticate_user.setVisibility(View.GONE);
				} else {
					icon_authenticate_user.setVisibility(View.VISIBLE);
				}
			} else {
				icon_authenticate_user.setVisibility(View.GONE);
			}
		} else {
			mLayoutBeforeLogin.setVisibility(View.VISIBLE);
			mLayoutLoginInfo.setVisibility(View.GONE);
		}

	}

	class InitData implements Runnable {
		@Override
		public void run() {
			saveDebugToFile(TAG, "InitData------begin");
			initUserData();
			//SmsReceiver.setServiceNeedStop(false);
//			initSms();

			// initFloatView();
			// initMainService();
			initVoiceAssistantService();
			initBroadcastReceiver();
			//initMusicControlBroadcastReceiver();

			if (!SavedData.is91Registered()) {
				if (getCurrentChannel().equals("91zhushou")) {
					NDChannel.ndUploadChannelId(0x03050000,
							CopyOfNewAssistActivity.this,
							new NDChannel.NdChannelCallbackListener() {
								@Override
								public void callback(int arg0) {
									if (arg0 == 0)
										SavedData.set91Registered(true);
								}
							});
				}
			}

			// AppUtil.launchFixedApp(AssistActivity.this, "Email");
			saveDebugToFile(TAG, "InitData------end");

			// initVoiceVolumeImage();
		}
	}


	protected void initUI() {
		mAdapter = new CommunicationAdapter(this);
		mAdapter.setmHandler(mHandler);
		mLstView.setAdapter(mAdapter);

		updateStatusView();

		checkServerNotification();
		mLstView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		//mMapView.setBuiltInZoomControls(true);
//		mMapView.setClickable(true);
		//mMapView.setMapMoveEnable(true);
		//mMapController = mMapView.getController();
		setProcessingState(MsgConst.UI_STATE_UNINIT);
	}

	private void initVoiceView() {
		imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
		imgv_voice_volume.setVisibility(View.GONE);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);
		
		if(!com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))	
		{
			if (VoiceAssistantService.mServerState  == MsgConst.STATE_SERVER_NOT_CONNECTED)
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
			else if (VoiceAssistantService.mServerState  == MsgConst.STATE_SERVER_CONNECTING)
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
			else
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_connected);
		}
		else
		{
			btn_voiceSpeak
			.setBackgroundResource(R.drawable.voice_mic_connected);
		}

	}
    private void initNetWorkRemindVariable()
    {
    	if(SavedData.isNetworkTips())
    	{
    		musicPlayRemind = true;
    		videoPlayRemind = true;
    		downloadRemind = true;
    	}
    	else
    	{
    		musicPlayRemind = false;
    		videoPlayRemind = false;
    		downloadRemind = false;
    	}
    	
    }
	protected void setProcessingState(int state) {

		Log.i("notifyClientState", "aa" + state);
		mProcessingState = state;

		if (VoiceAssistantService.mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED && 
			!(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))) {
			// mBtnSendText.setEnabled(true);
			mBtnSwitchToText.setEnabled(true);
			mBtnSwitchToVoice.setEnabled(true);
			mBtnSendText.setEnabled(true);
			btn_voiceSpeak.setEnabled(true);

			imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			imgv_voice_volume.setVisibility(View.INVISIBLE);
			rotateview.stopRotate();
			rotateview.setVisibility(View.GONE);

			initVoiceView();
			// voiceView.init(); // Clear animation if server is disconnected.
		} else {
			switch (state) {
			case MsgConst.UI_STATE_UNINIT:
				//clearTipsEvent(false);
				mBtnSendText.setEnabled(false);
				btn_voiceSpeak.setEnabled(false);
				mBtnSwitchToText.setEnabled(false);
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_disconnect);
				break;
			case MsgConst.UI_STATE_INITED:
				Log.i(TAG, "UI_STATE_INITED");
				// init state is OK
				//clearTipsEvent(false);
				//startTipsEvent();
				if (isServerResponsed()) {
					mBtnSendText.setEnabled(true);
					btn_voiceSpeak.setEnabled(true);
					initVoiceView();
				} else {
					mBtnSendText.setEnabled(false);
					btn_voiceSpeak.setEnabled(false);
				}
				mBtnSwitchToText.setEnabled(true);
				mBtnSwitchToVoice.setEnabled(true);
				break;
			case MsgConst.UI_STATE_SPEAKING:
				// Start speaking
				Log.i(TAG, "UI_STATE_SPEAKING");
				//clearTipsEvent(true);
				mBtnSendText.setEnabled(false);
				mBtnSwitchToVoice.setEnabled(false);
				// mMusicPlayerView.speakPause();
				// voiceView.startSpeak();
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_pressed);
				imgv_voice_volume.setVisibility(View.VISIBLE);
				break;
			case MsgConst.UI_STATE_RECOGNIZING:
				// start to recogine
				//clearTipsEvent(true);
				// mMusicPlayerView.speakRecoer();
				mBtnSendText.setEnabled(false);
				btn_voiceSpeak.setEnabled(false);
				mBtnSwitchToVoice.setEnabled(false);
				// voiceView.startLoading();
				btn_voiceSpeak
						.setBackgroundResource(R.drawable.voice_mic_loading);
				rotateview.setVisibility(View.VISIBLE);
				imgv_voice_volume.setVisibility(View.GONE);
				rotateview.startRotate();
				break;
			}
		}
	}

	protected void initUserData() {
		AlarmUtil.init(this);

		if (!VoiceAssistantService.SAVE_USER_PHONE_DATA) {
			ContactUtil.findAllContacts(this, true);
			AppUtil.findAllApp(this, true);
		}
	}


	protected void uninitBroadcastReceiver() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	protected void initBroadcastReceiver() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
						saveDebugToFile(TAG, Intent.ACTION_SCREEN_OFF);
					} else if (intent.getAction().equals(
							Intent.ACTION_SCREEN_ON)) {
						saveDebugToFile(TAG, Intent.ACTION_SCREEN_ON);
					}
				}
			};

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			registerReceiver(mReceiver, filter);
		}
	}

	protected void saveDebugToFile(String msg1, String msg2) {
		if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
			Date date = new Date();
			String fileName = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/voice_assist/debug/";
			File fileParent = new File(fileName);
			if (!fileParent.exists())
				fileParent.mkdirs();

			String sDate = String.format("%04d%02d%02d_",
					date.getYear() + 1900, date.getMonth() + 1, date.getDate());
			String time = String.format("%02d:%02d:%02d", date.getHours(),
					date.getMinutes(), date.getSeconds());

			FileOutputStream fOs = null;
			try {
				fOs = new FileOutputStream(fileName + sDate + "state.txt", true);
				try {
					fOs.write(time.getBytes());

					if (msg1 != null) {
						fOs.write("\t".getBytes());
						fOs.write(msg1.getBytes());
					}

					if (msg2 != null) {
						fOs.write("\t".getBytes());
						fOs.write(msg2.getBytes());
					}

					fOs.write("\r\n".getBytes());

				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (fOs != null)
					try {
						fOs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	/**
	 * Music
	 */
	protected void playMusicList(List<String[]> lstData) {
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
				mMusicPlayerView.setPlayList(lstMusic, mHandler);
				mMusicPlayerView.showMusicList();
			} else {
				LogOutput.e(TAG, "lstMusic is null");
			}
		}
	}

	/**
	 * Map
	 */
	protected void showMap(MapInfo mapInfo) {
		showTopView(mMapView);
		aMap.clear();
		
		LatLng point = null;

		if (mapInfo.mLongitude != 0 || mapInfo.mLatitude != 0) {
			point = new LatLng(mapInfo.mLatitude, mapInfo.mLongitude);
		} else if (mapInfo.mAddress != null) {
			point = getPointFromAddress(mapInfo.mAddress, "");
		}

		if (point != null) {
			MarkerOptions marker = new MarkerOptions();
			marker.position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			aMap.addMarker(marker);
			changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
							point, 16, 0, 30)), null);
		}

		String poiId = "ola_pos";
		int index = 1;
		String poiSnippet = "";
		if (mapInfo.mPoiLatitude != null && mapInfo.mPoiLatitude.length > 0) {
			List<PoiItem> lstPoiItem = new ArrayList<PoiItem>();
			for (int i = 0; i < mapInfo.mPoiLatitude.length; i++) {
				LatLonPoint ptPoi = new LatLonPoint(mapInfo.mPoiLatitude[i],
						mapInfo.mPoiLongitude[i]);
				if (mapInfo.mPoiId != null)
					poiId = mapInfo.mPoiId[i];
				else
					poiId = "ola_pos" + (index++);
				if (mapInfo.mPoiSnippet != null)
					poiSnippet = mapInfo.mPoiSnippet[i];
				PoiItem poiItem = new PoiItem(poiId, ptPoi,
						mapInfo.mPoiTitle[i], poiSnippet);
				lstPoiItem.add(poiItem);
			}

			if (lstPoiItem.size() > 0) {
				point = new LatLng(lstPoiItem.get(0).getLatLonPoint().getLatitude(), lstPoiItem.get(0).getLatLonPoint().getLongitude());
				MarkerOptions marker = new MarkerOptions();
				marker.position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				aMap.addMarker(marker);
				CameraPosition position = new CameraPosition(point,16, 0, 30);
				changeCamera(CameraUpdateFactory.newCameraPosition(position), null);
			}
		}
	}
	
	private void showBusLineInfo(BusInfoJsonData data, int arg1) {
		showTopView(mMapView);
		aMap.clear();
		
		BusLineQuery busLineQuery = new BusLineQuery(data.mBusDescriptionData.bus_name,  SearchType.BY_LINE_NAME, data.mBusDescriptionData.city);
		busLineQuery.setPageSize(10);
		busLineQuery.setPageNumber(0);
		BusLineSearch busLineSearch = new BusLineSearch(this, busLineQuery);
		busLineSearch.searchBusLineAsyn();
		start_stop = data.mData.bus_start[arg1];
		end_stop = data.mData.bus_end[arg1];

		Message msg = mHandler.obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_START);
		mHandler.sendMessage(msg);
		busLineSearch.setOnBusLineSearchListener(this);
	}
	
	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if (rCode == 0) {
			List<BusLineItem> lineItems = result.getBusLines();
			if (lineItems != null && lineItems.size() > 0) {
				if (result.getQuery().getCategory() == SearchType.BY_LINE_NAME) {
					BusLineQuery busLineQuery = null;
					for( BusLineItem item : lineItems) {
						if (item.getOriginatingStation().equals(start_stop) && item.getTerminalStation().equals(end_stop)){
							String lineId = item.getBusLineId();
							busLineQuery = new BusLineQuery(lineId, SearchType.BY_LINE_ID,	item.getCityCode());
							BusLineSearch busLineSearch = new BusLineSearch(this, busLineQuery);
							busLineSearch.setOnBusLineSearchListener(this);
							busLineSearch.searchBusLineAsyn();
							break;
						}
					}
					if (null == busLineQuery) {
						Message msg = mHandler.obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
						mHandler.sendMessage(msg);
						CustomToast.makeToast(CopyOfNewAssistActivity.this, getResources().getString(R.string.newassistactivity_can_not_find_bus_info));//,Toast.LENGTH_SHORT).show();
					}
				}
				else if (result.getQuery().getCategory() == SearchType.BY_LINE_ID) {
					Message msg = mHandler.obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
					mHandler.sendMessage(msg);
					lineItems = result.getBusLines();
					BusLineOverlay busLineOverlay = new BusLineOverlay(this,
							aMap, lineItems.get(0));
					busLineOverlay.removeFromMap();
					busLineOverlay.addToMap();
					busLineOverlay.zoomToSpan();

				}
			}
		}
		else {
			Message msg = mHandler.obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			mHandler.sendMessage(msg);
			CustomToast.makeToast(CopyOfNewAssistActivity.this, getResources().getString(R.string.newassistactivity_can_not_find_bus_info));//,Toast.LENGTH_SHORT).show();
		}
	}
	
	private LatLng getPointFromAddress(String mAddress, String city) {
		PoiSearch poiSearch;
		if (city == null) {
			city = "";
		}
		mPOIQuery = new PoiSearch.Query(mAddress, "", city);
		mPOIQuery.setPageSize(10);
		mPOIQuery.setPageNum(0);

		poiSearch = new PoiSearch(this, mPOIQuery);
		poiSearch.searchPOIAsyn();
		poiSearch.setOnPoiSearchListener(this);
		
		return null;
	}

	protected void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint,
			final int mode) {
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
		mRouteSearch = new RouteSearch(CopyOfNewAssistActivity.this);
		mRouteSearch.setRouteSearchListener(CopyOfNewAssistActivity.this);
		mHandler.sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_START);
		int localmode = mode;
		if (localmode < 10) {
			mBusRouteQuery = new BusRouteQuery(fromAndTo,
					RouteSearch.BusDefault, "", 0);
			mRouteSearch.calculateBusRouteAsyn(mBusRouteQuery);
		} else if (localmode < 20) {
			mDriveRouteQuery = new DriveRouteQuery(fromAndTo,
					RouteSearch.DrivingDefault, null, null, "");
			mRouteSearch.calculateDriveRouteAsyn(mDriveRouteQuery);
		} else {
			mWalkRouteQuery = new WalkRouteQuery(fromAndTo,
					RouteSearch.WalkDefault);
			mRouteSearch.calculateWalkRouteAsyn(mWalkRouteQuery);
		}
		showTopView(mMapView);
	}



	private void changeCamera(CameraUpdate newCameraPosition, CancelableCallback callback) {
		aMap.animateCamera(newCameraPosition, 1000, callback);
	}

	private void searchPos(MapInfo mapInfo) {
		if (mapInfo.mPoiSnippet != null && mapInfo.mPoiSnippet.length == 1) {
			MapThread thread = new MapThread();
			thread.setMapData(mapInfo);
			thread.start();
		}
	}

	class MapThread extends Thread {
		MapInfo mMapInfo = null;;
		String mAddress = null;
		boolean mIsNavigate = false;

		public void setMapData(MapInfo data) {
			mMapInfo = data;
			mAddress = mMapInfo.mPoiSnippet[0];
		}

		public void setNavigationAddress(String address) {
			mIsNavigate = true;
			mAddress = address;
		}

		@Override
		public void run() {
			Message msg = null;
			boolean found = false;
			GeocodeSearch geoCoder = new GeocodeSearch(CopyOfNewAssistActivity.this);
			int retry = 0;
			while (retry < 2) {
				try {
					List<GeocodeAddress> address = null;
					GeocodeQuery query = new GeocodeQuery(mAddress, "");
					address = geoCoder.getFromLocationName(query);
					
					if (address != null && address.size() > 0) {
						if (!mIsNavigate) {
							mMapInfo.mPoiLongitude[0] = address.get(0).getLatLonPoint().getLongitude();
							mMapInfo.mPoiLatitude[0] = address.get(0).getLatLonPoint().getLatitude();
							msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_MAP);
							msg.obj = mMapInfo;
						} else {
							if (address.get(0).getLatLonPoint().getLatitude() != 0 &&
									address.get(0).getLatLonPoint().getLongitude() != 0) {
								LatLng point = new LatLng(address.get(0).getLatLonPoint().getLatitude(),
										address.get(0).getLatLonPoint().getLongitude());
								msg = mHandler.obtainMessage(MsgConst.MSG_NAVIGATE_TO, 0);
								msg.obj = point;
							}
							else {
								msg = mHandler.obtainMessage(MsgConst.MSG_NAVIGATE_TO, 1);
								msg.obj = mMapInfo.mPoiSnippet[0];
							}
						}

						found = true;
						break;
					}
				} catch (AMapException e) {
					e.printStackTrace();
				}
				retry++;
			}

			if (!found) {
				msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_ERROR);
				msg.obj = getResources().getString(R.string.newassistactivity_can_not_find) + mAddress;
			}
			mHandler.sendMessage(msg);
			super.run();
		}
	}

	protected void showRoute(RouteResult routeResult) {
		if (routeResult != null) {
			if (routeResult instanceof BusRouteResult) {
				BusPath busPath = ((BusRouteResult) routeResult).getPaths().get(0);
				aMap.clear();
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, aMap,
						busPath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			}
			if (routeResult instanceof DriveRouteResult) {
				DrivePath drivePath = ((DriveRouteResult)routeResult).getPaths().get(0);
				aMap.clear();
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						this, aMap, drivePath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();
			}
			if (routeResult instanceof WalkRouteResult) {
				WalkPath walkPath = ((WalkRouteResult)routeResult).getPaths().get(0);
				aMap.clear();
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						aMap, walkPath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
			}
			mMapView.invalidate();
			showTopView(mMapView);
		}
	}

	/**
	 * Web
	 */
	@SuppressLint("SetJavaScriptEnabled")
	protected void showInternalWeb(String url, boolean disableJavaScript) {
		mWebView.getSettings().setJavaScriptEnabled(!disableJavaScript);
		mWebView.loadUrl(url);
		showTopView(layout_webView);
	}

	/**
	 * Main Service
	 */
	protected void initMainService() {
		mMainServiceConnection = new MainServiceConnection();
		this.bindService(new Intent(this, MainService.class),
				mMainServiceConnection, Context.BIND_AUTO_CREATE);
	}

	protected void stopMainService() {
		if (mMainService != null) {
			this.unbindService(mMainServiceConnection);
			mMainService = null;
			mMainServiceConnection = null;
		}
	}

	class MainServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMainService = IMainService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMainService = null;
		}
	}

	/**
	 * VoiceAssistantService
	 */
	protected void initVoiceAssistantService() {
		if (DEBUG)
			Log.d(TAG, "initVoiceAssistantService");

		mServiceConnection = new VoiceAssistantServiceConnection();
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
		{
			this.bindService(new Intent(this, VoiceSdkService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
		}
		else
		{
			this.bindService(new Intent(this, VoiceAssistantService.class),
					mServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	protected void stopVoiceAssistantService() {
		if (mServiceConnection != null) {
			if (DEBUG)
				Log.d(TAG, "stopVoiceAssistantService");

			//unregisterClient();

			this.unbindService(mServiceConnection);
			mtServiceMessenger = null;
			mServiceConnection = null;
		}
		//the following code is: force stop the service//it may started by SMSReceiver incomingCallReceiver.. 
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
		{
		// Do not need to stop VoiceSdkService
		//	this.stopService(new Intent(this, VoiceSdkService.class));
		}
		else
		{
			this.stopService(new Intent(this, VoiceAssistantService.class));
		}
	}

	class VoiceAssistantServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DEBUG)
				Log.d(TAG, "VoiceAssistantService on Connected.");

			if (mtServiceMessenger == null){
				mtServiceMessenger = new Messenger(service);
				setProcessingState(MsgConst.UI_STATE_INITED);
				closeSplash();				
			}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (DEBUG)
				Log.d(TAG, "onServiceDisconnected.");
			mtServiceMessenger = null;
		}
	}

	private void unregisterClient() {
		// remove unregister
		if (mtServiceMessenger == null)
			return;
		Bundle bundle = new Bundle();
		bundle.putString("key", getApplicationContext().getPackageName());
		Message msg = Message.obtain(null,
				MsgConst.CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER);
		msg.replyTo = mMessenger;
		msg.setData(bundle);
		sendMessageToService(msg);
		/*if (!sendMessageToService(msg)) {
			//stopVoiceAssistantService();
		}*/
		isRegister = false;
	}

	private void showHelpGuideView()
	{
		if(HelpStatisticsUtil.isHelpDataFileExist() == false)
		{
			String  str = "{\"Just Talk Dialog Outputs\":[{"+ 
    		"\"Help\":"
    		+"{"+
		    "\"help_menu\":"+
			"["+
			   "{\"type\" : \"weather\",\"title\":\"天气\",\"description\" : \"明天天气怎么样\",\"icon_name\" : \"icn_weather.png\",\"url\" : \"local\",\"contentArray\" : [\"上海的天气\",\"空气质量\",\"天气预报\",\"明天天气怎么样\",\"下一周的天气\"],\"opacity\" : \"1\",\"color\" : \"#33b8e3\"},"+

			   "{\"type\" : \"music\",\"title\":\"音乐\",\"description\" : \"我要听陈奕迅的歌\",\"icon_name\" : \"icn_music.png\",\"url\" : \"local\",\"contentArray\" : [\"听歌\",\"听音乐\",\"听陈奕迅的歌\",\"随便放首歌\",\"我要听青花瓷\"],\"opacity\" : \"1\",\"color\" : \"#7dcf00\"},"+
			   
               "{\"type\" : \"joke\",\"title\":\"笑话\",\"description\" : \"我想听冷笑话\",\"icon_name\" : \"icn_joke.png\",\"url\" : \"local\",\"contentArray\" : [\"讲笑话\",\"我要听笑话\",\"来个笑话\",\"来个段子\",\"我想听冷笑话\"],\"opacity\" : \"1\",\"color\" : \"#d338fe\"},"+
               
               "{\"type\" : \"POI\",\"title\":\"周边\",\"description\" : \"我饿了\",\"icon_name\" : \"icn_poi.png\",\"url\" : \"local\",\"contentArray\" : [\"我饿了\",\"我在哪\",\"我的位置\",\"附近的餐厅\",\"附近的厕所\"],\"opacity\" : \"1\",\"color\" : \"#fe3e67\"},"+

               "{\"type\" : \"TV_Guide\",\"title\":\"节目预告\",\"description\" : \"晚上有什么电视节目\",\"icon_name\" : \"icn_tv.png\",\"url\" : \"local\",\"contentArray\" : [\"爸爸去哪儿哪个台放\",\"湖南卫视的节目列表\",\"妈妈咪呀什么时候放\",\"今晚有古剑奇谭吗\",\"晚上有什么电视节目\"],\"opacity\" : \"1\",\"color\" : \"#ff9434\"},"+

               "{\"type\" : \"more\",\"title\":\"更多用途\",\"description\" : \"看视频，查公交，设提醒等\",\"icon_name\" : \"icn_more.png\",\"url\" : \"local\",\"opacity\" : \"1\",\"color\" : \"#0cc19d\"}"+
			 "],"+
			"\"project_number\" : \"1405322466697\""+
			"}"+ 
    		"}]}";
			Message message = Message.obtain(null,MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE);
			message.obj = str;
			sendMessageToService(message);
		}
		else
		{
			String str = HelpStatisticsUtil.getHelpDataFromFile();
			Message message = Message.obtain(null,MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE);
			message.obj = str;
			sendMessageToService(message);
		}
	}
	private void registerClient() {
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

					sendMessageToService(msg);
						//stopVoiceAssistantService();
					if (mFirstRegister) {
						/*msg = Message.obtain(null, MsgConst.CLIENT_ACTION_WELCOME);
						MsgAnswer msgAnswer = new MsgAnswer("{\"Just Talk Dialog Outputs\":[{\"Question\":{\"Present\":\"3\",\"Content\":\"我是你最亲爱的小助手，需要我做什么吗？\",\"tag\":\"login\"}}]}");
						msg.obj = msgAnswer;*/
						
						showHelpGuideView();
						msg =  Message.obtain(null, MsgConst.CLIENT_ACTION_WELCOME);
						sendMessageToService(msg);
						if (SavedData.isHttpMode())
							sendMessageToService(MsgConst.CLIENT_ACTION_INIT_COMMUNICATION, 1, 0);
						if(needAddCommondata)
						{
						   sendMessageToService(MsgConst.CLIENT_ACTION_ADD_COMMONDATA,null);
						   needAddCommondata = false;
						}
						if(needStartCapture)
						{							
							mHandler.sendEmptyMessageDelayed(MsgConst.CLIENT_ACTION_START_CAPTURE, 500);
							needStartCapture = false;
						}
						if(AutoUpdate.isDownloading() == false)
						{
							CommunicationUpdateUtil communicationUpdateUtil = new CommunicationUpdateUtil(CopyOfNewAssistActivity.this);
							communicationUpdateUtil.getDataFromServer();
						}
						mFirstRegister = false;						
					}
					else
					{
						if(needAddCommondata)
						{
						   sendMessageToService(MsgConst.CLIENT_ACTION_ADD_COMMONDATA,null);
						   needAddCommondata = false;
						}
						if(needStartCapture)
						{
							sendMessageToService(MsgConst.MSG_START_CAPTURE,null);
							needStartCapture = false;
						}
					}
					if(isStartFromBluetooth)
					{						
						mHandler.sendEmptyMessageDelayed(MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING, 500);
						isStartFromBluetooth = false;
					}	
					/*else
					{
						if(HelpStatisticsUtil.isNeedToRequestServer(NewAssistActivity.this))
						{
							msg =  Message.obtain(null, MsgConst.CLIENT_ACTION_WELCOME);
							sendMessageToService(msg);
							mFirstRegister = false;
						}
					}*/
					isRegister = true;
				}
			}).start();
		} else {
			if (DEBUG)
				Log.d(TAG, "is registered.");
			if(needStartCapture)
			{
				sendMessageToService(MsgConst.MSG_START_CAPTURE,null);
				needStartCapture = false;
			}
			if(isStartFromBluetooth)
			{
				mHandler.sendEmptyMessageDelayed(MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING, 500);
				isStartFromBluetooth = false;
			}	
		}
	}

	/**
	 * Voice Assistant Service Callback
	 */
	private Messenger mMessenger = new Messenger(new InComingHandler());

	@SuppressLint("HandlerLeak")
	class InComingHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			if (DEBUG)
				Log.d(TAG, "service action: " + msg.what);
/*			if (isPause) {
				return;
			}*/
			Message newMsg = new Message();
		
			switch (msg.what) {
			/*
			 * Service Command
			 */
			case MsgConst.SERVICE_ACTION_QUERY_WEIBO:
				WeiboUtil.queryWeiboToken(CopyOfNewAssistActivity.this, mHandler,
						MSG_WEIBO_TOKEN_RETURN);
				break;
			case MsgConst.SERVICE_ACTION_QUERY_MUSIC:
				if (MusicPlayerView.musicList != null
						&& MusicPlayerView.musicList.size() > 0) {
					JSONArray objArray = new JSONArray();

					MusicEntity musicEntity = MusicPlayerView.musicList
							.get(mMusicPlayerView.position);
					MusicInfo musicInfo = MusicUtil.getMusicPlaying(
							String.valueOf(musicEntity.getId()),
							musicEntity.getName(), musicEntity.getAuthor(),
							null, musicEntity.getUrl(), musicEntity.getPhoto());
					objArray.put(musicInfo.toJsonObject());

					sendMessageToService(
							MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
							msg.getData(), objArray);
				}else {
					//As music APP requested. when no music player, send back blank msg if server query music playing. 
					JSONArray objArray = new JSONArray();
					objArray.put("");
					sendMessageToService(
							MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
							msg.getData(), objArray);
				}
				break;
			case MsgConst.SERVICE_ACTION_QUERY_POSITION_ALARM:
				try {
					JSONArray objArray = new JSONArray();

					List<LocationActionData> result = mMainService
							.queryLocationAlert();
					if (result != null && result.size() > 0) {
						for (LocationActionData locationActionData : result) {
							objArray.put(locationActionData.toJsonObject());
						}
					}

					sendMessageToService(
							MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
							msg.getData(), objArray);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case MsgConst.SERVICE_ACTION_CLOSE_SPLASH:
				if (DEBUG)
					Log.d(TAG, "closeSplash.");
				closeSplash();
				break;
			case MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE:
				int state = msg.getData().getInt("state");
				if (DEBUG)
					Log.d(TAG, "setProcessingState: " + state);
				setProcessingState(state);
				break;
			case MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA:
				CommunicationData commData = (CommunicationData) msg.obj;
				if (commData != null) {
					Log.i("CalculateTime",
							"CommunicationData added:"
									+ System.currentTimeMillis() % 10000);
					
					if (!commData.isSilentInfoMsg()) {
						getMessageFromLock = SavedData.getLockMessage();
						if (getMessageFromLock != null
								&& !getMessageFromLock.equals("") && commData.getFrom() == DataConst.FROM_SERVER) {
							Message textmsg = mHandler
									.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
							textmsg.obj = getMessageFromLock;
							mHandler.sendMessage(textmsg);
							SavedData.setLockMessage(null);
						}else {
							if(commData.getFrom() == DataConst.FROM_SERVER)
							{
								mServerResponsed = true;
							}
							else
							{
								mServerResponsed = false;
							}
							
							boolean showDialog = false;
							boolean isAppMusic = false;
							int whichKindRemind = NetworkWarningDialog.NONE_KIND_TO_REMIND;
							if(!NetWorkUtil.isWIFIConnected(getApplicationContext()) && SavedData.isNetworkTips())
							{
								for(BaseData item : commData.getLstData() ) {
									if(item instanceof OptionData) {
										int id = ((OptionData)item).getOptionId();
										if((id == OptionData.OPTION_VIDEO) && videoPlayRemind) {
											showDialog = true;
											whichKindRemind = NetworkWarningDialog.VIDEO_PLAY_TO_REMIND;
										}else if((id == OptionData.OPTION_APP_NAME) && downloadRemind)
										{
											showDialog = true;
											whichKindRemind = NetworkWarningDialog.DOWNLOAD_TO_REMIND;
										}
									}
									if (item instanceof AppData) {
										if (((AppData)(item)).getAppName().equals(AppData.APP_NAME_MUSIC) && musicPlayRemind) {
											showDialog = true;
											isAppMusic = true;
											whichKindRemind = NetworkWarningDialog.MUSIC_PLAY_TO_REMIND;
										}else if(((AppData)(item)).getAppName().equals(AppData.APP_NANE_DOWNLOAD) && downloadRemind)
										{
											showDialog = true;
											isAppMusic = false;
											whichKindRemind = NetworkWarningDialog.DOWNLOAD_TO_REMIND;
										}
										
									}
								}
								
							}
							if (showDialog) {
								networkWarningDialog = new NetworkWarningDialog(CopyOfNewAssistActivity.this);
								networkWarningDialog.setHandlerAndData(mHandler,commData,isAppMusic,whichKindRemind);
								networkWarningDialog.showDialog();								
							}else {
								mAdapter.addData(commData);
							}
						}
					}
					if(mMusicPlayerView != null)
						  mMusicPlayerView.hideMusicList();
					if (commData.getFrom() == DataConst.FROM_SERVER) {						
						SilentInfoData silentData = commData
								.getSilentInfoData();
						if (silentData != null) {
							SavedData.setmUserInfo(silentData.getmLevel(),
									silentData.getmScore(),
									silentData.getmNextLevelScore(),
									silentData.getmSpecialTime());
							//isUserLoggedin = true;
						}

						updateStatusView();
					}
				}
				mLstView.setSelection(mLstView.getCount()-1);
				break;
			case MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME:
				int volume = msg.getData().getInt("volume");
				updateMicImage(volume);
				break;
			case MsgConst.SERVICE_ACTION_SERVER_RESPONSE:
				break;
			case MsgConst.SERVICE_ACTION_CAPTURE_VIEW:
				File file = startCaptureImage();
				newMsg.copyFrom(msg);
				newMsg.what = MsgConst.CLIENT_ACTION_CAPTURE_VIEW_OK;
				Bundle bundle = newMsg.getData();
				if (file != null) {
					bundle.putString("file", file.getAbsolutePath());
				} else {
					bundle.putString("file", null);
				}
				newMsg.setData(bundle);

				try {
					if (mtServiceMessenger != null) {
						mtServiceMessenger.send(newMsg);
					}
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				break;
			/*
			 * Other commands
			 */
			case MsgConst.MSG_MUSIC_PLAY:
				if(!NetWorkUtil.isWIFIConnected(getApplicationContext()) && SavedData.isNetworkTips() && musicPlayRemind)
				{
					if(networkWarningDialog != null)
					{
						networkWarningDialog.setMusicData((List<String[]>)msg.obj);
					}					
				}else
				{
					@SuppressWarnings("unchecked")
					List<String[]> lstData = (List<String[]>) msg.obj;
					// requestMusicFocus(true);
					playMusicList(lstData);
				}
				break;
			case MsgConst.MSG_MUSIC_CONTROL:
				if (MusicPlayerView.musicList != null && !MusicPlayerView.musicList.isEmpty()) {
					showTopView(mMusicPlayerView);
					if (mMusicPlayerView != null) {
						mMusicPlayerView.controlMusic((String) msg.obj, msg.arg1);
					}
				}				
				break;
			case MsgConst.MSG_MUSIC_STOP:
				if (mMusicPlayerView != null) {
					mMusicPlayerView.closeMusicServer();
					mLstMusicView.setVisibility(View.GONE);
					mMusicPlayerView.setVisibility(View.GONE);
				}
				break;
			case MsgConst.MSG_SHOW_ERROR:
				CustomToast.makeToast(CopyOfNewAssistActivity.this, (String) msg.obj);
						//Toast.LENGTH_SHORT).show();
				break;
			case MsgConst.MSG_SEND_TO_WEIXIN:
				int to = msg.arg1;
				int type = msg.arg2;
				@SuppressWarnings("unchecked")
				List<String> txtDatas = (List<String>) msg.obj;
				// txtDatas, it contains title,description,url
				if (txtDatas.size() < 3) {
					LogOutput.e(TAG, "MSG_SEND_TO_WEIXIN parameters error");
					return;
				}
				String title = txtDatas.get(0);
				String description = txtDatas.get(1);
				String url = txtDatas.get(2);
				send2Weixin(to, type, title, description, url);
				break;
			case MsgConst.MSG_SEND_TO_RENREN:
				int pto = msg.arg1;
				int ptype = msg.arg2;
				@SuppressWarnings("unchecked")
				List<String> ptxtDatas = (List<String>) msg.obj;
				if (ptxtDatas.size() < 3) {
					LogOutput.e(TAG, "MSG_SEND_TO_RENREN parameters error");
					return;
				}
				String ptitle = ptxtDatas.get(0);
				String pdescription = ptxtDatas.get(1);
				String purl = ptxtDatas.get(2);
				send2Renren(pto, ptype, ptitle, pdescription, purl);
				break;
			case MsgConst.MSG_TAKE_PHOTO:
				int preview = msg.arg1;
				takePictureByCamera(preview);
				break;
			case MsgConst.MSG_POSITION_ALARM_ADDED:
				int longitude = msg.getData().getInt("longitude");
				int latitude = msg.getData().getInt("longitude");
				int longitude_range = msg.getData().getInt("longitude_range");
				int latitude_range = msg.getData().getInt("latitude_range");
				String position_name = msg.getData().getString("position_name");
				String alarm_title = msg.getData().getString("alarm_title");

				Parcel parcel = Parcel.obtain();
				parcel.writeInt(longitude);
				parcel.writeInt(latitude);
				parcel.writeInt(longitude_range);
				parcel.writeInt(latitude_range);
				parcel.writeString(position_name);
				parcel.writeInt(0);
				parcel.writeString(alarm_title);
				parcel.setDataPosition(0);
				LocationActionData action = new LocationActionData(parcel);

				try {
					if (!mMainService.addLocationAlert(action)) {
						Log.i(TAG, "mMainService.addLocationAlert failed.");
					}
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException: " + e.getMessage());
					e.printStackTrace();
				}
				break;
			case MsgConst.MSG_POSITION_ALARM_DELETED:
				if (msg.obj != null) {
					long[] result = (long[]) msg.obj;
					int[] ids = new int[result.length];
					for (int i = 0; i < result.length; i++) {
						ids[i] = (int) result[i];
					}
					try {
						if (!mMainService.deleteLocationAlert(ids)) {
							Log.i(TAG,
									"mMainService.deleteLocationAlert failed.");
						}
					} catch (RemoteException e) {
						Log.e(TAG, "RemoteException: " + e.getMessage());
						e.printStackTrace();
					}
				}
				break;
			case MsgConst.SERVICE_ACTION_SERVER_CONNECTING:
				mServerState = MsgConst.STATE_SERVER_CONNECTING;
				break;
			case MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED:
			case MsgConst.SERVICE_ACTION_SERVER_BROKEN:
				mServerResponsed = true;
				mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;
				Message riddlegame = mHandler.obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
				riddlegame.obj =  "puzzlegame";
				mHandler.sendMessage(riddlegame);
				break;
			case MsgConst.SERVICE_ACTION_SERVER_CONNECTED:
				mServerState = MsgConst.STATE_SERVER_CONNECTED;
				break;
			case MsgConst.SERVICE_ACTION_TTS_PLAY_END:
				if (SavedData.getmAutoStartRecord() && msg.arg1 == 1
						 && layout_voice.getVisibility() == View.VISIBLE) {
					sendMessageToService(MsgConst.CLIENT_ACTION_START_CAPTURE, 0, 0);
				}
				else {
					if(SavedData.isVoiceWakeUpOpen())
						sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE, 0, 0);
					mMusicPlayerView.speakRecover();
				}
				break;
			case MsgConst.SERVICE_ACTION_TTS_PLAY_START:
				mMusicPlayerView.speakPause();
				break;
			case MsgConst.MSG_CALL_START:
				isCalling = true;
				mMusicPlayerView.speakPause();
				break;
			case MsgConst.MSG_CALL_END:
				isCalling = false;
				mMusicPlayerView.speakRecover();
				break;
			case VoiceAssistantService.MSG_UI_SEARCHING_START:
				mSearchingDialog = new ProgressDialog(CopyOfNewAssistActivity.this);
				mSearchingDialog.setTitle(getResources().getString(R.string.bluetooth_searching_title));
				mSearchingDialog.setMessage(getResources().getString(R.string.bluetooth_searching_message));
				mSearchingDialog.show();
				break;
			case VoiceAssistantService.MSG_UI_SEARCHING_FOUND:
				mSearchingDialog.cancel();
				break;
			case MsgConst.CLIENT_ACTION_VIEW_HANDLER_MSG:
				AppData.ServerCommand cmd = (AppData.ServerCommand)msg.obj;
				mAdapter.handlerMsg(cmd);
			case MsgConst.SERVICE_ACTION_CLOSE_HELP_GUIDE_VIEW:
				if(mLayoutHelp.getVisibility() == View.VISIBLE)
				{
					mLayoutHelp.setVisibility(View.GONE);
					if(GlobalData.isUserLoggedin())
						btn_goback.setVisibility(View.INVISIBLE);						
					else
					    mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
					HelpStatisticsUtil.helpType = null;
				}
				if(layout_guide_help.getVisibility() == View.VISIBLE)
					mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
				break;
			case MsgConst.CLIENT_ACTION_LISTVIEW_GOTO_LAST_POSITION:
				mLstView.setSelection(mLstView.getCount() -1);
				break;
			default:
				if (DEBUG)
					Log.d(TAG, "other msg id from service: " + msg.what);
				newMsg.copyFrom(msg);
				mHandler.sendMessage(newMsg);
				break;
			}
		}
	}

	private void updateMicImage(int volume) {
		switch (volume) {
		case 1:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			break;
		case 2:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume02);
			break;
		case 3:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume03);
			break;
		case 4:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume04);
			break;
		case 5:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume05);
			break;
		case 6:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume06);
			break;
		case 7:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume07);
			break;
		case 8:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume08);
			break;
		case 9:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume09);
			break;
		case 10:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume10);
			break;
		case 11:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume11);
			break;
		case 12:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume12);
			break;
		default:
			imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
			break;
		}
	}

	protected boolean checkAppExist(Context context, String packageName) {
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
	protected void initFloatView() {
		// FloatView.bindToFloatViewService(this);
		Intent intent = new Intent(this, FloatViewService.class);
		intent.setAction(FloatViewService.START_LOGO_VIEW);
		this.startService(intent);
	}

	protected void releaseFloatView() {
		Intent intent = new Intent(this, FloatViewService.class);
		intent.setAction(FloatViewService.STOP_LOGO_VIEW);
		this.startService(intent);
	}

	protected void stopFloatView() {
		if (FloatViewService.serverIsStart) {
			FloatViewService.serverIsStart = false;
			Intent intent = new Intent(this, FloatViewService.class);
			this.stopService(intent);
		}
	}

	/**
	 * 既然延时线程
	 */
	private void exitThread() {
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

	protected void startNavigate(double fromLatitude, double fromLongitude,
			double toLatitude, double toLongitude, int mode) {
		double latitude = toLatitude;
		double longitude = toLongitude;

		Intent i = new Intent("com.autonavi.xmgd.action.NAVIGATOR");
		i.setData(Uri.parse("GEONAVI:" + String.valueOf(longitude) + ","
				+ String.valueOf(latitude) + ","));

		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			i = new Intent(
					"android.intent.action.VIEW",
					android.net.Uri
							.parse("androidamap://navi?sourceApplication=S3&lat="
									+ latitude
									+ "&lon="
									+ longitude
									+ "&style=0&dev=0"));
			i.setPackage("com.autonavi.minimap");
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			try {
				startActivity(i);
			} catch (ActivityNotFoundException e1) {
				CustomToast.makeToast(CopyOfNewAssistActivity.this,
						this.getString(R.string.need_navi_software));//, Toast.LENGTH_LONG).show();
				if (fromLatitude == 0 || fromLongitude == 0) {
					if (VoiceAssistantService.mCurLocation != null) {
						fromLatitude = ((double)(VoiceAssistantService.mCurLocation.y))/1000000.0;
						fromLongitude = ((double)(VoiceAssistantService.mCurLocation.x))/1000000.0;
					}
				}

				if (fromLatitude != 0 && fromLongitude != 0) {
					LatLonPoint startPoint = new LatLonPoint(fromLatitude,
							fromLongitude);
					if  ( toLatitude != 0 && toLongitude != 0 ){
						LatLonPoint endPoint = new LatLonPoint(toLatitude, toLongitude);
						searchRouteResult(startPoint, endPoint, mode);
					}else {
						CustomToast.makeToast(this, this.getString(R.string.to_position_not_known));//, Toast.LENGTH_SHORT).show();
					}
				} else {
					CustomToast.makeToast(this, this.getString(R.string.position_not_known));
							//Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	protected void startNavigate(String address) {
		if (address != null && address.length() > 0) {
			MapThread thread = new MapThread();
			thread.setNavigationAddress(address);
			thread.start();
		}
	}

	private File startCaptureImage() {
		File file = null;
		Bitmap cachefile = loadBitmapFromView(mLstView, true);
		if (cachefile != null) {
			try {
				file = this.getFileStreamPath("cachefile.png");
				FileOutputStream fos = new FileOutputStream(file);
				cachefile.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				file = null;
				e.printStackTrace();
			} catch (IOException e) {
				file = null;
				e.printStackTrace();
			}
		}
		return file;
	}

	private Bitmap loadBitmapFromView(View view, boolean addWaterMark) {
		Bitmap bitmap = null;
		try {
			int width = view.getWidth();
			int height = view.getHeight();
			if (width != 0 && height != 0) {
				bitmap = Bitmap.createBitmap(width, height,
						Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);

				view.layout(0, 0, width, height);
				view.draw(canvas);

				if (addWaterMark) {
					Bitmap watermark = BitmapFactory.decodeResource(
							getResources(), R.drawable.ola_icon_watermark);
					int left = width - watermark.getWidth() - 1;
					int top = height - watermark.getHeight() - 1;
					if (left < 0)
						left = 0;
					if (top < 0)
						top = 0;

					canvas.drawBitmap(watermark, left, top, null);
				}
			}
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}

	private Bitmap addWatermark(Bitmap src, Bitmap watermark) {
		if (src == null) {
			Log.d(TAG, "src is null");
			return null;
		}
		if (watermark == null) {
			Log.d(TAG, "watermark is null");
			return src;
		}

		int sWid = src.getWidth();
		int sHei = src.getHeight();
		int wWid = watermark.getWidth();
		int wHei = watermark.getHeight();
		if (sWid == 0 || sHei == 0) {
			// Log.d(TAG, "src's width or height is 0");
			return null;
		}

		if (sWid < wWid || sHei < wHei) {
			// Log.d(TAG, "src is small than watermark");
			return src;
		}

		Bitmap bitmap = Bitmap.createBitmap(sWid, sHei, Config.ARGB_8888);
		try {
			Canvas cv = new Canvas(bitmap);
			cv.drawBitmap(src, 0, 0, null);
			cv.drawBitmap(watermark, sWid - wWid - 5, sHei - wHei - 5, null);
			cv.save(Canvas.ALL_SAVE_FLAG);
			cv.restore();
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}

	private boolean send2Weixin(int to, int type, String title,
			String description, Bitmap bm) {
		LogOutput.d(TAG, "send2Weixin bm");
		IWXAPI api = WeixinUtil.register2Weixin(this);

		return WeixinUtil.sendScreenCapture(this, api, false, bm, title,
				description);
	}

	private boolean send2Weixin(int to, int type, String title,
			String description, String url) {
		LogOutput.d(TAG, "send2Weixin");
		IWXAPI api = WeixinUtil.register2Weixin(this);
		boolean isToFriend = (to == 1) ? false : true;
		switch (type) {
		case 0:// Text
			WeixinUtil.sendText(this, api, isToFriend, description);
			break;
		case 1:// image
			break;
		case 2:// video
			break;
		case 3:// music
			break;
		case 4:// webpage
			break;
		case 5:// screen_capture
			Bitmap screenBmp = loadBitmapFromView(mLstView, true);
			if (screenBmp != null) {
				WeixinUtil.sendScreenCapture(this, api, isToFriend, screenBmp,
						title, description);
			}
			break;
		default:
			break;
		}

		return true;
	}

	private boolean send2Renren(int to, int type, String title,
			String description, String url) {
		LogOutput.d(TAG, "send2Renren");
		switch (type) {
		case 0:// Text
			break;
		case 1:// image
			break;
		case 2:// video
			break;
		case 3:// music
			break;
		case 4:// webpage
			break;
		case 5:// screen_capture
			startCaptureImage();
			Intent intent = new Intent(this, PhotoServiceActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}

	private void onVoiceBtnClicked(int isUp) {
		hideTopView(true);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);
		mLstView.setSelection(mLstView.getAdapter().getCount() - 1);
//		mMusicPlayerView.musicFormMinimize();
		
		mMusicPlayerView.speakPause();

		sendMessageToService(MsgConst.CLIENT_ACTION_START_CAPTURE, isUp, 0);
	}
	
	/*protected void clearTipsEvent(boolean closeTips) {
		mHandler.removeMessages(MsgConst.MSG_SHOW_TIPS);
		mHandler.removeMessages(MsgConst.MSG_CHECK_TTS);

		if (closeTips) {
			mHandler.removeMessages(MsgConst.MSG_TIPS_TIMEOUT);
			tipsView.setVisibility(View.GONE);
		}
	}

	protected void startTipsEvent() {
		clearTipsEvent(true);
		if (mProcessingState == MsgConst.UI_STATE_UNINIT
				|| mProcessingState == MsgConst.UI_STATE_INITED) {
			if (Tts.isPlaying()) {
				mHandler.sendEmptyMessageDelayed(MsgConst.MSG_CHECK_TTS,
						TipsView.TIPS_TIME_DELAY_SHORT);
			} else {
				mHandler.sendEmptyMessageDelayed(MsgConst.MSG_SHOW_TIPS,
						TipsView.TIPS_TIME_DELAY);
			}
		}
	}*/
	
	protected boolean sendMessageToService(int what, Bundle data) {
		return sendMessageToService(what, 0, 0, data, null);
	}

	protected boolean sendMessageToService(int what, Bundle data, Object obj) {
		return sendMessageToService(what, 0, 0, data, obj);
	}

	protected boolean sendMessageToService(int what, int arg1, int arg2) {
		return sendMessageToService(what, arg1, arg2, null, null);
	}

	protected boolean sendMessageToService(int what, int arg1, int arg2,
			Bundle data, Object obj) {
		Message msg = Message.obtain(null, what);
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		if (data != null)
			msg.setData(data);
		if (obj != null)
			msg.obj = obj;
		return sendMessageToService(msg);
	}

	protected boolean sendMessageToService(Message msg) {
		boolean ret = false;
		try {			
			if (mtServiceMessenger != null) {
				mtServiceMessenger.send(msg);
				ret = true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return ret;
	}

	protected String getCurrentChannel() {
		String channel = "";
		ApplicationInfo appInfo;
		try {
			appInfo = CopyOfNewAssistActivity.this.getPackageManager()
					.getApplicationInfo(
							CopyOfNewAssistActivity.this.getPackageName(),
							PackageManager.GET_META_DATA);
			channel = appInfo.metaData.getString("UMENG_CHANNEL");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return channel;
	}


	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPoiSearched(PoiResult result, int arg1) {
		if (result != null && result.getQuery() != null) {
			if (result.getQuery().equals(mPOIQuery)) {
				List<PoiItem> poiItems = result.getPois();

				if (poiItems != null && poiItems.size() > 0) {
					aMap.clear();
					mPoiOverLay = new PoiOverlay(aMap, poiItems);
					mPoiOverLay.removeFromMap();
					mPoiOverLay.addToMap();
					mPoiOverLay.zoomToSpan();
				}
			}
		}
		
	}

	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {
		RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				BusPath busPath = result.getPaths().get(0);
				aMap.clear();
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, aMap,
						busPath, result.getStartPos(),
						result.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();

				mHandler.sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			} else {
				mDriveRouteQuery = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null,null,"");
				mRouteSearch.calculateDriveRouteAsyn(mDriveRouteQuery);
			}
		} else {
			mDriveRouteQuery = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null,null,"");
			mRouteSearch.calculateDriveRouteAsyn(mDriveRouteQuery);
		}
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		RouteSearch.FromAndTo fromAndTo = null;
		if (rCode == 0) {
			fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				DrivePath drivePath = result.getPaths().get(0);
				if (drivePath.getDistance() <= 500) {
					mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
					mRouteSearch.calculateWalkRouteAsyn(mWalkRouteQuery);
					return;
				}
				aMap.clear();
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						this, aMap, drivePath, result.getStartPos(),
						result.getTargetPos());
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();

				mHandler.sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			} else {
				mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
				mRouteSearch.calculateWalkRouteAsyn(mWalkRouteQuery);
			}
		} else {
			fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
			mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
			mRouteSearch.calculateWalkRouteAsyn(mWalkRouteQuery);
		}
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				WalkPath walkPath = result.getPaths().get(0);
				aMap.clear();
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						aMap, walkPath, result.getStartPos(),
						result.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
			} else {
				Message msg = mHandler.obtainMessage(MsgConst.MSG_ROUTE_SEARCH_RESULT);
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		} else {
			Message msg = mHandler.obtainMessage(MsgConst.MSG_ROUTE_SEARCH_RESULT);
			msg.obj = result;
			mHandler.sendMessage(msg);
		}
		mHandler.sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
		
	}
	
	protected boolean isServerResponsed()
	{
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
			return mServerResponsed;
		else 
			return VoiceAssistantService.mServerResponsed;
	}
}
