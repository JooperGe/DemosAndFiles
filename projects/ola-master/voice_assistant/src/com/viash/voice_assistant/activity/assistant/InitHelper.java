package com.viash.voice_assistant.activity.assistant;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.SupportMapFragment;
import com.iflytek.aitalk4.AiTalkShareData;
import com.iflytek.tts.TtsService.Tts;
import com.nd.channel.NDChannel;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.AccountCenterActivity;
import com.viash.voice_assistant.activity.LoginActivity;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.activity.SettingsActivity;
import com.viash.voice_assistant.adapter.HelpExpandableAdapter;
import com.viash.voice_assistant.common.LogcatToFile;
import com.viash.voice_assistant.component.RecommendView;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voice_assistant.widget.RotateView;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.AlarmUtil;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.CustomToast;

/**
 * 
 * 拆分主activity功能
 * 
 * 初始化相关功能
 * 
 * @author fenglei
 *
 */
public class InitHelper {

	private static final boolean DEBUG = true;
	private static final String TAG = "InitHelper";
	
	private NewAssistActivity mainActivity;
	private static InitHelper _instance = null;

	private InitHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static InitHelper init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new InitHelper(main);
		return _instance;
	}

	public static InitHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init InitHelper");

		return _instance;
	}
	
	public String getCurrentChannel() {
		String channel = "";
		ApplicationInfo appInfo;
		try {
			appInfo = mainActivity.getPackageManager()
					.getApplicationInfo(
							mainActivity.getPackageName(),
							PackageManager.GET_META_DATA);
			channel = appInfo.metaData.getString("UMENG_CHANNEL");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return channel;
	}
	
	public class AsyncInitWorker implements Runnable {

		public void initUserData() {
			AlarmUtil.init(mainActivity);

			if (!VoiceAssistantService.SAVE_USER_PHONE_DATA) {
				ContactUtil.findAllContacts(mainActivity, true);
				AppUtil.findAllApp(mainActivity, true);
			}
		}
		
		public void initBroadcastReceiver() {
			if (mainActivity.mReceiver == null) {
				mainActivity.mReceiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
						} else if (intent.getAction().equals(
								Intent.ACTION_SCREEN_ON)) {
						}
					}
				};

				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_SCREEN_OFF);
				filter.addAction(Intent.ACTION_SCREEN_ON);
				mainActivity.registerReceiver(mainActivity.mReceiver, filter);
			}
		}
		
		@Override
		public void run() {
			initUserData();
			ServiceHolder.getInstantce().initVoiceAssistantService();

			initBroadcastReceiver();

			if (!SavedData.is91Registered()) {
				if (getCurrentChannel().equals("91zhushou")) {
					NDChannel.ndUploadChannelId(0x03050000,
							mainActivity,
							new NDChannel.NdChannelCallbackListener() {
								@Override
								public void callback(int arg0) {
									if (arg0 == 0)
										SavedData.set91Registered(true);
								}
							});
				}
			}

		}
	}
	
	
	@SuppressLint("SetJavaScriptEnabled")
	public void initControlName() {
		mainActivity.view_welcome = mainActivity.findViewById(R.id.layout_welcome);
		mainActivity.mLayoutMain = mainActivity.findViewById(R.id.layout_main);
		mainActivity.mLayoutText = mainActivity.findViewById(R.id.layout_txt);
		mainActivity.mLayoutText.setVisibility(View.GONE);
		mainActivity.layout_voice = mainActivity.findViewById(R.id.layout_voice);
		mainActivity.btn_voiceSpeak = (Button) mainActivity.findViewById(R.id.btn_voice_mic);
		mainActivity.btn_voiceHelp = (Button) mainActivity.findViewById(R.id.btn_voice_more);
		mainActivity.rotateview = (RotateView) mainActivity.findViewById(R.id.rotateview);
		mainActivity.rotateview.setImageDrawable(R.drawable.voice_load_rotate);
		mainActivity.mBtnSwitchToText = (Button) mainActivity.findViewById(R.id.btn_voice_edit);
		mainActivity.imgv_voice_volume = (ImageView) mainActivity.findViewById(R.id.imgv_volume);
		mainActivity.mBtnSwitchToVoice = (Button) mainActivity.mLayoutText.findViewById(R.id.btn_input_change);
		mainActivity.mBtnSendText = (Button) mainActivity.mLayoutText.findViewById(R.id.btn_confirm);
		mainActivity.mLstView = (ListView) mainActivity.mLayoutMain.findViewById(R.id.lst_communication);

		mainActivity.mLstMusicView = (ListView) mainActivity.mLayoutMain.findViewById(R.id.lv_music);
		
		mainActivity.mMapView = (RelativeLayout) mainActivity.mLayoutMain.findViewById(R.id.main_map_view);
		if (mainActivity.aMap == null) {
			mainActivity.aMap = ((SupportMapFragment) mainActivity.getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
		
		mainActivity.mEdtContent = (EditText) mainActivity.mLayoutText.findViewById(R.id.et_content);
		mainActivity.mWebView = (WebView) mainActivity.findViewById(R.id.main_web_view);
		mainActivity.layout_webView = mainActivity.findViewById(R.id.layout_webview);

		mainActivity.layout_selection_whole_View = (RelativeLayout)mainActivity.findViewById(R.id.layout_selection_view);		
		
		mainActivity.btn_last_page = (Button) mainActivity.findViewById(R.id.btn_last_page);
		mainActivity.btn_next_page = (Button) mainActivity.findViewById(R.id.btn_next_page);
		mainActivity.btn_refresh_page = (Button) mainActivity.findViewById(R.id.btn_refresh_page);

		mainActivity.mLayoutBeforeLogin = mainActivity.findViewById(R.id.layout_before_login);
		mainActivity.mLayoutLoginInfo = mainActivity.findViewById(R.id.layout_login_info);
		mainActivity.layout_login_center = (RelativeLayout) mainActivity.findViewById(R.id.layout_login_center);
		mainActivity.mBtn_Login = (ImageView) mainActivity.findViewById(R.id.btn_login);
		mainActivity.btn_goback = (ImageView) mainActivity.findViewById(R.id.btn_goback);
		mainActivity.mTv_Username = (TextView) mainActivity.findViewById(R.id.username);
		mainActivity.mTv_Score = (TextView) mainActivity.findViewById(R.id.score);
		mainActivity.mIcon_Medal = (ImageView) mainActivity.findViewById(R.id.icon_medal);
		mainActivity.mIcon_Arrow = (ImageView) mainActivity.findViewById(R.id.icon_arrow);
		mainActivity.icon_authenticate_user = (ImageView) mainActivity.findViewById(R.id.icon_authenticate_user);
		mainActivity.mLayoutBeforeLogin.setVisibility(View.VISIBLE);
		mainActivity.mLayoutLoginInfo.setVisibility(View.GONE);
		mainActivity.mLayoutHelp = this.mainActivity.findViewById(R.id.layout_help);
		
		mainActivity.mHelpView = (ExpandableListView) mainActivity.mLayoutHelp.findViewById(R.id.help);
		mainActivity.helpExpandableAdapter = new HelpExpandableAdapter(mainActivity,NotifyUiHandler.getInstantce());
		mainActivity.mHelpView.setAdapter(mainActivity.helpExpandableAdapter);
		mainActivity.mHelpView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
        
		DisplayMetrics dm = new DisplayMetrics();
		mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(dm.heightPixels >= 1024)
			mainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                                     WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		mainActivity.mBtn_Login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if(mainActivity.mLayoutHelp.getVisibility() == View.VISIBLE)
				{
					NotifyUiHandler.getInstantce().sendMessage(
							NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
					return;
				}
				if(mainActivity.layout_guide_help.getVisibility() == View.VISIBLE)
				{
					mainActivity.helpGuideDetailView = null;					
					NotifyUiHandler.getInstantce().sendMessage(
							NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
					return;
				}
				if (GlobalData.isUserLoggedin()) {//UserData.isLogin(NewAssistActivity.this)) {
					//isUserLoggedin = true;
					UIStateHelper.getInstantce().updateStatusView();
					return;
				}
				Intent intent = new Intent(mainActivity,
						LoginActivity.class);
				//startActivityForResult(intent, LOGIN_ACTIVITY_REQUESTCODE);
				mainActivity.startActivity(intent);
			}
		});
		
		mainActivity.btn_goback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if(mainActivity.mLayoutHelp.getVisibility() == View.VISIBLE)
				{
					NotifyUiHandler.getInstantce().sendMessage(
							NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
					return;
				}
				if(mainActivity.layout_guide_help.getVisibility() == View.VISIBLE)
				{
					mainActivity.helpGuideDetailView = null;					
					NotifyUiHandler.getInstantce().sendMessage(
							NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
					return;
				}				
			}
		});
		
		mainActivity.layout_login_center.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mainActivity,
						AccountCenterActivity.class);
				//startActivityForResult(intent,ACCOUNT_CENTER_ACTIVITY_REQUESTCODE);
				mainActivity.startActivity(intent);
			}
		});

		/*image_drawer = (ImageView) mainActivity.findViewById(R.id.image_drawer);
		image_drawer_icon_new = (ImageView) mainActivity.findViewById(R.id.image_drawer_icon_new);
		image_drawer_icon_new.setVisibility(View.INVISIBLE);*/
		
		mainActivity.image_help = (ImageView) mainActivity.findViewById(R.id.image_help);
		mainActivity.layout_guide_help = (LinearLayout) mainActivity.findViewById(R.id.layout_guide_help);
		mainActivity.layout_recommend = (RelativeLayout) mainActivity.findViewById(R.id.layout_recommend);
		mainActivity.mRecommendView = new RecommendView(mainActivity.getApplicationContext(), NotifyUiHandler.getInstantce());
		mainActivity.layout_recommend.addView(mainActivity.mRecommendView);
		mainActivity.layout_recommend.setVisibility(View.INVISIBLE);
		       
		mainActivity.mMusicPlayerView = (MusicPlayerView)mainActivity.mLayoutMain
				.findViewById(R.id.main_music_player);

		mainActivity.mMusicPlayerView.setVisibility(View.GONE);

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
		
		mainActivity.image_help.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				NotifyUiHandler.getInstantce().sendMessage(
						NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL));
				//NotifyUiHandler.getInstantce().sendMessage(NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE));
				NotifyUiHandler.getInstantce().sendMessage(
						NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));
			}			
		});

		mainActivity.mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("http://") || url.startsWith("https://")) {
					mainActivity.mWebView.loadUrl(url);
				}
				else {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					try	{
						mainActivity.startActivity(intent);
					}
					catch(ActivityNotFoundException e) {
						e.printStackTrace();
						return false;
					}
				}
				return true;
			}
		});
		mainActivity.mWebView.getSettings().setJavaScriptEnabled(true);
		mainActivity.mBtnSwitchToText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mainActivity.layout_voice.setVisibility(View.GONE);
				mainActivity.mLayoutText.setVisibility(View.VISIBLE);
				mainActivity.cancelRecognize();
			}
		});
		mainActivity.btn_last_page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainActivity.mWebView.canGoBack())
					mainActivity.mWebView.goBack();
			}
		});
		mainActivity.btn_next_page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainActivity.mWebView.canGoForward())
					mainActivity.mWebView.goForward();

			}
		});
		mainActivity.btn_refresh_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mainActivity.mWebView.reload();

			}
		});
		mainActivity.mBtnSwitchToVoice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mainActivity.layout_voice.setVisibility(View.VISIBLE);
				mainActivity.mLayoutText.setVisibility(View.GONE);
				mainActivity.hideSoftKeyboard();
				if (SavedData.isAllowWakeupByAudio()) {
					AiTalkShareData.setLeaveMainInterfaceFlag(false);
					try {
						if (mainActivity.mtServiceMessenger != null)
							mainActivity.mtServiceMessenger.send(Message
									.obtain(null,
											MsgConst.CLIENT_ACTION_REENTRY_WAKEUP_BY_AUDIO));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mainActivity.btn_voiceHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(mainActivity, SettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mainActivity.getApplicationContext().startActivity(intent);
				try {
					if (mainActivity.mtServiceMessenger != null)
						mainActivity.mtServiceMessenger.send(Message.obtain(
								null,// send message to stop VR
								MsgConst.CLIENT_ACTION_SET_WAKEUP_BY_AUDIO));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				AiTalkShareData.setLeaveMainInterfaceFlag(true);// Leave main screen flag.
				if(SavedData.isVoiceWakeUpOpen())
					MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE,0,0);
			}
		});

		mainActivity.mBtnSendText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.hideTopView(true);
				boolean bSpecial = false;
				String text = mainActivity.mEdtContent.getText().toString();
				if (GlobalData.getSoftwareMode() == GlobalData.SOFTWARE_MODE_DEBUG) {
					if (text.length() == 0) {
						long curTime = System.currentTimeMillis();
						if (curTime - mainActivity.mLastClickTime < 1000) {
							mainActivity.mLastClickTime = System.currentTimeMillis();
							mainActivity.onSettingIp();
						} else {
							mainActivity.mLastClickTime = curTime;
						}
						bSpecial = true;
					} else if (text.equalsIgnoreCase("dump")) {
						long curTime = System.currentTimeMillis();
						if (curTime - mainActivity.mLastClickTime < 1000) {
							mainActivity.mLastClickTime = System.currentTimeMillis();
							mainActivity.startDumpDialogText();
						} else {
							mainActivity.mLastClickTime = curTime;
						}
						bSpecial = true;
					} else if (text.equalsIgnoreCase("channel")) {
						String channel = getCurrentChannel();
						channel = "Channel:" + channel;
						CustomToast.makeToast(mainActivity,
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
								if (curTime - mainActivity.mLastClickTime < 1000) {
									mainActivity.mLastClickTime = System.currentTimeMillis();
									Tts.setSpeed(speed);
									Tts.setPitch(pitch);
									mainActivity.mMusicPlayerView.speakPause();
								} else {
									mainActivity.mLastClickTime = curTime;
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
						LogcatToFile.getInstance(mainActivity).start();
						CustomToast.makeToast(mainActivity,
								"Logcat Start.");//, Toast.LENGTH_SHORT).show();
						bSpecial = true;
					}
					else if (text.equalsIgnoreCase("stop")) {
						LogcatToFile.getInstance(mainActivity).stop();
						CustomToast.makeToast(mainActivity,
								"Logcat Stop.");//, Toast.LENGTH_SHORT).show();
						bSpecial = true;
					}
				}

				mainActivity.hideSoftKeyboard();
				if (text.length() > 0 && !bSpecial) {
					Message msg = NotifyUiHandler.getInstantce()
							.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
					msg.obj = text;
					NotifyUiHandler.getInstantce().sendMessage(msg);
					mainActivity.mEdtContent.setText("");
					NotifyUiHandler.getInstantce().sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
					//mMusicPlayerView.musicFormMinimize();
				}
			}
		});

		mainActivity.btn_voiceSpeak.setOnTouchListener(new Button.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mainActivity.mVoictBtnDownTime = System.currentTimeMillis();
					Log.i("CalculateTime", "Time down:"
							+ (mainActivity.mVoictBtnDownTime % 10000));
					mainActivity.onVoiceBtnClicked(0);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (System.currentTimeMillis() > mainActivity.mVoictBtnDownTime + 1200) {
						mainActivity.onVoiceBtnClicked(1);
					}
				}

					if (mainActivity.layout_selection_whole_View.getVisibility() == View.VISIBLE) {
						mainActivity.layout_selection_whole_View.removeAllViews();
						mainActivity.layout_selection_whole_View.setVisibility(View.GONE);
				
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

		mainActivity.view_welcome.setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View v) {
				SplashHelper.getInstantce().closeSplash();
			}
		});
		// bensonzhang Coupon Click Blank Collapse
		mainActivity.layout_voice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainActivity.layout_recommend.getVisibility() == View.VISIBLE) {
					mainActivity.layout_recommend.setVisibility(View.INVISIBLE);
				}
			}
		});
		mainActivity.mLstView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mainActivity.layout_recommend.getVisibility() == View.VISIBLE) {
					mainActivity.layout_recommend.setVisibility(View.INVISIBLE);
				}
				return false;
			}
		});// bensonzhang
		//tipsView = (TipsView) mainActivity.findViewById(R.id.main_tips_view);
	}
	
}
