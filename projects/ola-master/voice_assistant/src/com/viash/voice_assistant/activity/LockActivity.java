package com.viash.voice_assistant.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.iflytek.aitalk4.AiTalkShareData;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.DigitalClock;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.receiver.SmsReceiver;
import com.viash.voice_assistant.service.LockScreenService;
import com.viash.voice_assistant.speech.IRecognizeListener;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voice_assistant.widget.RotateView;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.LockUtils;

@SuppressWarnings("deprecation")
public class LockActivity extends Activity implements OnTouchListener {
	private static String TAG = "LockActivity" ;
	private  ImageView phone,message,camera,lock,voice,arrowanim,arrowanimpress,imgv_voice_volume;
	private  DigitalClock digitalClock;
	private  RotateView rotateview;
	
	private  int selectIcon = 0 ; 			            // 0, none, 1, Camera, 2, Message, 3, Phone, 4, Unlcok
	private  float lastY = 0;                    		// 记录移动时的Y坐标
	private  Vibrator mVibrator;
	private  AnimationDrawable animArrowDrawable = null;   
	private  AnimationDrawable animArrowDrawablePress = null; 
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private int imgSize;
	private int imgH1, imgH2, imgH3, imgH4, imgH5, imgH6, imgH7;
	private int imgW1, imgW2, imgW3;
	private int width,height;                                //手机屏幕宽高
	@SuppressWarnings("unused")
	private AbsoluteLayout layout;
	AbsoluteLayout.LayoutParams params;
	private AlphaAnimation voiceAA, voiceAABack;
	private boolean isClick = true;
	
	public static SpeechRecognizer mSpeechRecognizer = null;
	protected IRecognizeListener mRecognizerListener = null;
	//private static final int MSG_BEGINNING_OF_RECORD = 6;
	private static final int CHANGE_VOLUE=7;
	private static final int SHOW_MESSAGE=8;
	private static final int CANLE_REDISTER=9;
	private static final int SHOW_RESULT=10;
	private static final int SPEECH_END=11;
	protected int mServerState = MsgConst.SERVICE_ACTION_SERVER_CONNECTED;
	
	boolean flag = true; 
	
	public static boolean mbActive = false;
	   
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		int sdk_Version = android.os.Build.VERSION.SDK_INT;
		if (sdk_Version == 14) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		setContentView(R.layout.lock_screen);
		
		/*View view = View.inflate(getApplicationContext(), R.layout.lock_screen, null);
        WindowManager wm = (WindowManager) getApplicationContext()
                        .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = 2003;  //关键代码，设置view的类型为TYPE_SYSTEM_ALERT
        params.width = -1;
        params.height = -1;
        wm.addView(view, params);*/
		
		layout=(AbsoluteLayout)findViewById(R.id.locklayout);
		AiTalkShareData.setSpeechStartState(true);
		inits();
		startService(new Intent(LockActivity.this,LockScreenService.class));  
		initVoiceView();
		
		
		
		voiceAA = new AlphaAnimation (0.0f, 0.0f);
		voiceAA.setDuration(20);
		voiceAA.setFillAfter(true);
		voiceAABack = new AlphaAnimation (1.0f, 1.0f);
		voiceAABack.setDuration(20);
		initRecognizer();
		mbActive = true;
		//voiceAABack.setFillAfter(true);
		//this.startActivities(new Intent(this,))
	}
	private void getEachSize() {
		imgSize =   width * 100 / 1080;
		imgH1 = 100 * height / 1920; 
		imgH2 = 636 * height / 1920; 
		//imgH3 = 796 * height / 1920;
		imgH3 = 860 * height / 1920;
		//imgH4 = 956 * height / 1920;
		imgH4 = 1080 * height / 1920;
	       //imgH5 = 1147 * height / 1920;
		imgH5 = 1347 * height / 1920;
		//imgH6 = 1422 * height / 1920;
		imgH6 = 1622 * height / 1920;
		//imgH7 = 1564 * height / 1920;
		imgH7 = 1764 * height / 1920;
		
		imgW1 = width / 2 - imgSize;
		imgW2 = width / 2 - imgSize / 2;
		imgW3 = width / 2 - imgSize / 10;
	}

	public void inits() {
		mVibrator = (Vibrator) getApplication().getSystemService(
				Service.VIBRATOR_SERVICE);
		width = this.getWindowManager().getDefaultDisplay().getWidth();
		height = this.getWindowManager().getDefaultDisplay().getHeight();
		getEachSize();
		camera = (ImageView) findViewById(R.id.camera);
		message = (ImageView) findViewById(R.id.message);
		phone = (ImageView) findViewById(R.id.phone);
		digitalClock = (DigitalClock) findViewById(R.id.digitalClock);
		voice = (ImageView) findViewById(R.id.move_voice);

		imgv_voice_volume = (ImageView) findViewById(R.id.move_volume);
		rotateview = (RotateView) findViewById(R.id.rotateview);
		rotateview.setImageDrawable(R.drawable.voice_load_rotate);

		arrowanim = (ImageView) findViewById(R.id.getup_arrow);
		animArrowDrawable = (AnimationDrawable) arrowanim.getBackground();
		animArrowDrawable.start();

		arrowanimpress = (ImageView) findViewById(R.id.getup_arrow_press);
		animArrowDrawablePress = (AnimationDrawable) arrowanimpress
				.getBackground();

		lock = (ImageView) findViewById(R.id.lock);
		resetLayout();

		voice.setOnTouchListener(this);

	}
	
	public void inits(View view) {
		mVibrator = (Vibrator) getApplication().getSystemService(
				Service.VIBRATOR_SERVICE);
		width = this.getWindowManager().getDefaultDisplay().getWidth();
		height = this.getWindowManager().getDefaultDisplay().getHeight();
		getEachSize();
		camera = (ImageView) view.findViewById(R.id.camera);
		message = (ImageView) view.findViewById(R.id.message);
		phone = (ImageView) view.findViewById(R.id.phone);
		digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);
		voice = (ImageView) view.findViewById(R.id.move_voice);

		imgv_voice_volume = (ImageView) view.findViewById(R.id.move_volume);
		rotateview = (RotateView) view.findViewById(R.id.rotateview);
		rotateview.setImageDrawable(R.drawable.voice_load_rotate);

		arrowanim = (ImageView) view.findViewById(R.id.getup_arrow);
		animArrowDrawable = (AnimationDrawable) arrowanim.getBackground();
		animArrowDrawable.start();

		arrowanimpress = (ImageView) view.findViewById(R.id.getup_arrow_press);
		animArrowDrawablePress = (AnimationDrawable) arrowanimpress
				.getBackground();

		lock = (ImageView) view.findViewById(R.id.lock);
		resetLayout();

		voice.setOnTouchListener(this);

	}
	

	// init
	private void initVoiceView() {
		imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
		imgv_voice_volume.setVisibility(View.GONE);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);
		if (mServerState == MsgConst.STATE_SERVER_NOT_CONNECTED) {
			voice.setImageResource(R.drawable.lockscreen_voice_mic_disconnect);
		} else {
			voice.setImageResource(R.drawable.lockscreen_voice_mic_connected);
		}
		// voice.setImageResource(R.drawable.lockscreen_voice_mic_connected);
	}

	public void onClick() {
		if (SmsReceiver.isReplySMS()) {
			CustomToast.makeToast(this, this.getString(R.string.lockscreen_wait_vr));//, Toast.LENGTH_SHORT).show();
			return;
		}
		CustomToast.makeToast(this, this.getString(R.string.lockscreen_please_say));//, Toast.LENGTH_SHORT).show();
		mSpeechRecognizer.startRecognize(200,false);
		voice.setOnTouchListener(null);
		voice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {			
				flag =true;
				onStopSpeech();
				voice.setOnTouchListener(LockActivity.this);
				voice.setOnClickListener(null);
			}
			
		});
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);
		if (LockUtils.isFastDoubleClick()) {
			return;
		}
	}

	public void onStopSpeech() {
		if (SmsReceiver.isReplySMS()) {
			CustomToast.makeToast(LockActivity.this, this.getString(R.string.lockscreen_wait_vr));//, Toast.LENGTH_SHORT).show();
			return;
		}
		mSpeechRecognizer.stopRecognize();
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isClick = true;
			camera.setImageResource(R.drawable.big_camera_show);
			phone.setImageResource(R.drawable.big_phone_show);
			message.setImageResource(R.drawable.big_message_show);
			lock.setImageResource(R.drawable.lockscreen_icon_lock_press_small);

			lastY = (int) event.getRawY();

			voice.setImageResource(R.drawable.lockscreen_icon_press_highlight);
			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize * 4,
					v.getLeft() - imgSize, v.getTop() - imgSize);
			v.setLayoutParams(params);

			// layout.addView(v);
			arrowanim.setVisibility(View.INVISIBLE);
			arrowanimpress.setVisibility(View.VISIBLE);
			animArrowDrawablePress.start();
			break;
		// return handleDownMotionEvent(event);
		case MotionEvent.ACTION_MOVE:

			float distanceY1 = 0.0f;
			distanceY1 = lastY - imgSize - imgH5;
			if (Math.abs(distanceY1) > 50) {
				isClick = false;
			}
			lastY = event.getRawY();
			v.layout(v.getLeft(), (int) (lastY - 2 * imgSize), v.getRight(),
					(int) (lastY + 2 * imgSize));
			handleActionMoveEvent(event);
			break;
		case MotionEvent.ACTION_UP:
			float distanceY = 0.0f;
			distanceY = lastY - imgSize - imgH5;
			if (Math.abs(distanceY) > 50) {
				isClick = false;
			}
			return handleUpMotionEvent(event);
		}
		return true;
	}

	
	private boolean handleUpMotionEvent(MotionEvent event) {
		resetLayout();
		camera.setImageResource(R.drawable.camera_show);
		phone.setImageResource(R.drawable.phone_show);
		message.setImageResource(R.drawable.message_show);
		lock.setImageResource(R.drawable.lockscreen_icon_lock_normal);
		arrowanimpress.setVisibility(View.INVISIBLE);
		arrowanim.setVisibility(View.VISIBLE);
		voice.setImageResource(R.drawable.lockscreen_voice_mic_connected);
		voice.startAnimation(voiceAABack);
		resetLayout();

		switch (selectIcon) {
		case 0:
			voice.startAnimation(voiceAABack);
			break;
		case 1:
			Intent intent_camer = new Intent();
			intent_camer.setAction("android.media.action.STILL_IMAGE_CAMERA"); 	
			intent_camer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivity(intent_camer);
			finish();
			break; 
		case 2:
			Intent intent_sms = new Intent(Intent.ACTION_MAIN);
			intent_sms.addCategory(Intent.CATEGORY_DEFAULT);
			intent_sms.setType("vnd.android-dir/mms-sms");
		    intent_sms.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivity(intent_sms);
			finish();
			break;
		case 3:
			Intent intent_phone = new Intent();
			intent_phone.setAction(Intent.ACTION_DIAL);
			intent_phone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivity(intent_phone);
			finish();
			break;
		case 4:
			if (!UserData.doesContainHistoryApps(this) && UserData.isLockHomekeyEnable(this)) {
				Intent intent2 = new Intent();
				ComponentName comp = new ComponentName(LockScreenService.packagenameString, LockScreenService.ClassnameString);
				intent2.setComponent(comp);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(intent2);				
			}
			LockScreenService.presshome = 0;
			GuideActivity.mIsLock = false;
			finish();
			break;
		default:
			break;
		}
		
		if (isClick) {
			if(flag){
				flag = false;
				onClick();
			}else {
				flag =true;
			    onStopSpeech();
		   }
		}
		///voice.layout(voice.getLeft(), (int) (initTopLayoutY) + 100, voice.getRight(), (int) (initBottomLayoutY) + 100);
		return false;
	}
	private boolean handleActionMoveEvent(MotionEvent event) {

		if (imgH2+imgSize >= lastY &&  lastY >= imgH2 && selectIcon != 1) {
			mVibrator.vibrate(30);
			selectIcon = 1;
			
			camera.setImageResource(R.drawable.select_camera_show);
			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize* 4, imgW1 - imgSize, (int)( (imgH2 - ((int)imgSize* 1.5))));
			camera.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH3);
			message.setImageResource(R.drawable.big_message_show);
			message.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH4);
			phone.setImageResource(R.drawable.big_phone_show);
			phone.setLayoutParams(params);
			
			params = new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH7);
			lock.setImageResource(R.drawable.lockscreen_icon_lock_press_small);
			lock.setLayoutParams(params);
			
			voice.startAnimation(voiceAA);
			
		}
		else if (imgH3+imgSize >= lastY &&  lastY >= imgH3 && selectIcon != 2) {
			mVibrator.vibrate(30);
			selectIcon = 2;
			
			message.setImageResource(R.drawable.select_message_show);
			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize* 4, imgW1 - imgSize, (int)( (imgH3 - ((int)imgSize* 1.5))));
			message.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH2);
			camera.setImageResource(R.drawable.big_camera_show);
			camera.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH4);
			phone.setImageResource(R.drawable.big_phone_show);
			phone.setLayoutParams(params);
			
			params = new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH7);
			lock.setImageResource(R.drawable.lockscreen_icon_lock_press_small);
			lock.setLayoutParams(params);
			
			voice.startAnimation(voiceAA);
		}
		else if (imgH4+imgSize >= lastY &&  lastY >= imgH4 && selectIcon != 3) {
			mVibrator.vibrate(30);
			selectIcon = 3;

			phone.setImageResource(R.drawable.select_phone_show);
			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize* 4, imgW1 - imgSize, (int)( (imgH4 - ((int)imgSize* 1.5))));
			phone.setLayoutParams(params);			

			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH2);
			camera.setImageResource(R.drawable.big_camera_show);
			camera.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH3);
			message.setImageResource(R.drawable.big_message_show);
			message.setLayoutParams(params);
			
			params = new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH7);
			lock.setImageResource(R.drawable.lockscreen_icon_lock_press_small);
			lock.setLayoutParams(params);
			
			voice.startAnimation(voiceAA);			
		}		
		else if (imgH7+imgSize >= lastY &&  lastY >= imgH7 && selectIcon != 4) {
			mVibrator.vibrate(30);
			selectIcon = 4;

			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize* 4, imgW1 - imgSize, (int)( (imgH7 - ((int)imgSize* 1.5))));
			lock.setImageResource(R.drawable.select_lock_show);
			lock.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH2);
			camera.setImageResource(R.drawable.big_camera_show);
			camera.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH3);
			message.setImageResource(R.drawable.big_message_show);
			message.setLayoutParams(params);
			
			params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH4);
			phone.setImageResource(R.drawable.big_phone_show);
			phone.setLayoutParams(params);

			voice.startAnimation(voiceAA);
		}		
		else if (imgH7 >= lastY &&  lastY > imgH4 + imgSize && selectIcon != 0) {
			selectIcon = 0 ;
			voice.startAnimation(voiceAABack);
			camera.setImageResource(R.drawable.big_camera_show);
			phone.setImageResource(R.drawable.big_phone_show);
			message.setImageResource(R.drawable.big_message_show);
			lock.setImageResource(R.drawable.lockscreen_icon_lock_press_small);
			resetLayout();
			
			params = new AbsoluteLayout.LayoutParams(imgSize * 4, imgSize* 4, voice.getLeft(),voice.getTop());
			voice.setLayoutParams(params);
		}
		return false;
	}
	private void resetLayout() { 
		params= new AbsoluteLayout.LayoutParams(imgSize*10, imgSize*8, imgW1/8,imgH1);
		digitalClock.setLayoutParams(params);
				
		params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH2);
		camera.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH3);
		message.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH4);
		phone.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize*2, imgSize *2, imgW1, imgH5);
		voice.setLayoutParams(params);
		imgv_voice_volume.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize*2, imgSize *2, imgW1-imgSize/10, imgH5-imgSize/10);
		rotateview.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize/5, imgSize, imgW3, imgH6);
		arrowanim.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize/5, imgSize, imgW3, imgH6);
		arrowanimpress.setLayoutParams(params);
		
		params= new AbsoluteLayout.LayoutParams(imgSize, imgSize, imgW2, imgH7);
		lock.setLayoutParams(params);
	}
	private void updateMicImage(int volume) {
		switch (volume) {
		case 1:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_1);
			break;
		case 2:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_2);
			break;
		case 3:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_3);
			break;
		case 4:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_4);
			break;
		case 5:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_5);
			break;
		case 6:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_6);
			break;
		case 7:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_7);
			break;
		case 8:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_8);
			break;
		case 9:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_9);
			break;
		case 10:
			imgv_voice_volume.setBackgroundResource(R.drawable.lockscreen_volume_10);
			break;
		default:
			imgv_voice_volume.setImageResource(R.drawable.lockscreen_volume_1);
			break;
		}
	}
	
	Handler mHandler = new Handler()
	{
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHANGE_VOLUE:
				voice.setImageResource(R.drawable.lockscreen_voice_mic_pressed);
				imgv_voice_volume.setVisibility(View.VISIBLE);
				updateMicImage(msg.arg1);
				break;
			case SHOW_MESSAGE:
				rotateview.stopRotate();
				imgv_voice_volume.setVisibility(View.GONE);
				rotateview.setVisibility(View.GONE);
				voice.setImageResource(R.drawable.lockscreen_voice_mic_connected);
				CustomToast.makeToast(LockActivity.this, LockActivity.this.getString(R.string.voiceassistantservice_vr_error_1));//,Toast.LENGTH_SHORT).show();
				voice.setOnTouchListener(LockActivity.this);
				voice.setOnClickListener(null);
				break;
			case SHOW_RESULT:
				imgv_voice_volume.setVisibility(View.GONE);
				Intent intent = new Intent(LockActivity.this, NewAssistActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(intent);
				finish();
			case CANLE_REDISTER:
				voice.setOnTouchListener(LockActivity.this);
				voice.setOnClickListener(null);
				break;
			case SPEECH_END:
				rotateview.startRotate();
				rotateview.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	protected void initRecognizer() {
		if (mSpeechRecognizer != null) {
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();
			}

			//mSpeechRecognizer.destroy();
			mSpeechRecognizer = null;
		}
		
		mRecognizerListener = new IRecognizeListener() {
			@Override
			public void onResults(String result, String recordFileName) {
				if (result != null && !result.equals("")) {
					Message message = new Message();
					message.what = SHOW_RESULT;
					message.obj = result;
					mHandler.sendMessage(message);
					SavedData.setLockMessage(result);
				} else {

					Message message = new Message();
					message.what = SHOW_MESSAGE;
					mHandler.sendMessage(message);
				}
			}

			@Override
			public void onError(int errCode) {
				Message message = new Message();
				message.what = SHOW_MESSAGE;
				mHandler.sendMessage(message);
			}

			@Override
			public void onEndOfSpeech() {
				Message message = new Message();
				message.what = SPEECH_END;
				mHandler.sendMessage(message);
			}

			@Override
			public void onBeginningOfSpeech() {
			}

			@Override
			public void onCancel() {
				Message message = new Message();
				message.what = CANLE_REDISTER;
				mHandler.sendMessage(message);
			}

			@Override
			public void onVolumeUpdate(int newVolume) {
				Message message = new Message();
				message.what = CHANGE_VOLUE;
				message.arg1 = newVolume;
				mHandler.sendMessage(message);
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
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
			return true;
			}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onStart() {
		digitalClock.invalidate();
		super.onStart();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onResume() {
		super.onResume();
		digitalClock.updateDigitalClock();
		mbActive = true;
	}
	@Override
	protected void onDestroy() {
		if(mSpeechRecognizer != null)
		{
			if (mSpeechRecognizer.isRecognizing()) {
				mSpeechRecognizer.abort();			
			}			
			mSpeechRecognizer = null;
		}
		mbActive = false;
		if(GuideActivity.mIsLock)
		{
			mbActive = true;
		}
		Log.i("HOME","LockActivity() onDestroy  mbActive = "+mbActive);
		super.onDestroy();
	}
}
