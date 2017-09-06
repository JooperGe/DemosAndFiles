package com.viash.voice_assistant.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.via.android.voice.floatview.VIAApplication;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.service.VoiceAssistantService;

public class LogoView extends RelativeLayout implements OnLongClickListener, OnTouchListener, OnGestureListener {
	private static final boolean DEBUG = true;
	private static final String TAG = "LogoView";

	private static final int MIN_SCROLL_WIDTH = 50;
	private static final int MAX_SCROLL_WIDTH = 250;
	private static final int TRIGGER_SCROLL_WIDTH = 200;
	
	private GestureDetector mGestureDetector;
	private boolean isRecord = false;
	private boolean isDrag = false;
	private boolean isScroll = false;
	private float mTouchStartX;
	private float mTouchStartY;
	private float prevStartX;
	private float prevStartY;
	private float x;
	private float y;

	private WindowManager wm = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
	private WindowManager.LayoutParams logoParams = ((VIAApplication) getContext().getApplicationContext()).getWMParams(VIAApplication.LOGO_FLOAT_VIEW);
	
	private Context mContext;
	private ImageView logo;
	private RelativeLayout.LayoutParams logoLayout;
	private RelativeLayout.LayoutParams voiceButtonLayout;
	private View voiceButton;
	
	private ImageView imgv_voice_bg;
	private ImageView imgv_volume;
	private ImageView imgv_mike;
	private Handler handler;
	private static final int REFRESH_ALPHA =1;
	private static final int REFRESH_SET_IMG =2;
	private static final int REFRESH_INIT_VIEW =3;
	private static final int REFRESH_START_SPEAK_VIEW =4;
	private static final int REFRESH_LOAD_VIEW =5;
	private static final int REFRESH_MIKE_WHITE =6;
	private static final int REFRESH_MIKE_GRAY =7;
	private int imgSpeakBG =0;
	private Animation animation;
	
	public LogoView(Context context) {
		super(context);
		mContext = context;
		
		LayoutInflater.from(getContext()).inflate(R.layout.layout_logo, this, true);
		init();
		initHandler();
		animation = AnimationUtils.loadAnimation(getContext(), R.anim.voice_alpha);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float arg2, float arg3) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float arg2, float arg3) {
		if(DEBUG) Log.d(TAG, "onScroll");
		if(!isDrag){
			scrollViewPosition();
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		if(DEBUG) Log.d(TAG, "onSingleTapUp");
		Intent intent = new Intent(mContext, NewAssistActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		mContext.startActivity(intent);
		return true;
	}

	@Override
	public boolean onLongClick(View arg0) {
		if(!isScroll){
			if(DEBUG) Log.d(TAG, "onLongClick");
			isDrag = true;
			Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(100);
		}
		return false;
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		x = event.getRawX();
		y = event.getRawY() - 25;
		if(DEBUG) Log.i(TAG, "currX" + x + "====currY" + y);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchStartX = event.getX();
			mTouchStartY = event.getY();
			prevStartX = logoParams.x;
			prevStartY = logoParams.y;
			if(DEBUG) Log.i(TAG, "startX" + mTouchStartX + "====startY" + mTouchStartY);
			break;
		case MotionEvent.ACTION_MOVE:
			if(isDrag){
				moveViewPosition();
			}
			break;
		case MotionEvent.ACTION_UP:
			if(isDrag){
				fixedViewPosition();
				mTouchStartX = mTouchStartY = 0;
				isDrag = false;
			}else{
				if(isRecord) stopCapture();
				reverseViewPosition();
				prevStartX = prevStartY = 0;
				mTouchStartX = mTouchStartY = 0;
				isScroll = false;
			}
			break;
		}
		return this.mGestureDetector.onTouchEvent(event);
	}

	private void initHandler() {
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH_ALPHA:
					//imgv_voice_bg.setAlpha(loadVewAlpa);
					break;
				case REFRESH_SET_IMG:
					if(imgSpeakBG !=0)
						imgv_volume.setImageResource(imgSpeakBG);
					else
						imgv_volume.setImageBitmap(null);
					break;
				case REFRESH_INIT_VIEW:
					imgv_voice_bg.clearAnimation();
					imgv_voice_bg.setVisibility(View.GONE);
					imgv_volume.setVisibility(View.GONE);
					imgv_mike.setImageResource(R.drawable.voice_mike_white);
					break;
				case REFRESH_START_SPEAK_VIEW:
					imgv_volume.setVisibility(View.VISIBLE);
					imgv_mike.setImageResource(R.drawable.voice_mike_blue);
					break;
				case REFRESH_LOAD_VIEW:
					imgv_volume.setVisibility(View.GONE);
					imgv_voice_bg.setVisibility(View.VISIBLE);
					imgv_voice_bg.startAnimation(animation);
					break;
				case REFRESH_MIKE_WHITE:
					imgv_mike.setImageResource(R.drawable.voice_mike_white);
					break;
				case REFRESH_MIKE_GRAY:
					imgv_mike.setImageResource(R.drawable.voice_mike_gray);
					break;
				default:
					break;
				}
				
			}
		};
	}

	private void init() {
		logo = (ImageView) findViewById(R.id.logo);
		logoLayout = (LayoutParams) logo.getLayoutParams();
		voiceButton = (View) findViewById(R.id.logo_voice_button_layout);
		voiceButtonLayout = (LayoutParams) voiceButton.getLayoutParams();

		imgv_voice_bg = (ImageView) findViewById(R.id.logo_imgv_voice_bg);
		imgv_volume = (ImageView) findViewById(R.id.logo_imgv_volume);
		imgv_mike = (ImageView) findViewById(R.id.logo_imgv_mike);
		imgv_voice_bg.setVisibility(View.GONE);
		imgv_volume.setVisibility(View.GONE);
		
		mGestureDetector = new GestureDetector(this);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}

	private void moveViewPosition() {
		logoParams.x  = (int) (x - mTouchStartX);
		logoParams.y = (int) (y - mTouchStartY);
		voiceButtonLayout.width = 0;
		voiceButton.setLayoutParams(voiceButtonLayout);
		wm.updateViewLayout(this, logoParams);
	}

	private void scrollViewPosition() {
		int newX  = (int) (x - mTouchStartX);
		
		Display display = wm.getDefaultDisplay();
    	int width = display.getWidth();
    	int w;
		if(prevStartX > (width/2)){
			w = (int) (prevStartX - newX);
		}else{
			w = (int) (newX - prevStartX);
		}
		if(w < 0) return;
		
		if(w > MIN_SCROLL_WIDTH){
			isScroll = true;
		}
		if(w > TRIGGER_SCROLL_WIDTH && !isRecord){
			startCapture();
		}

    	if(w > MAX_SCROLL_WIDTH){
	    	if(prevStartX > (width/2)) newX  = (int) (prevStartX - MAX_SCROLL_WIDTH);
	    	else  newX  = (int) (prevStartX + MAX_SCROLL_WIDTH);
			voiceButtonLayout.width = MAX_SCROLL_WIDTH;
		}else{
			voiceButtonLayout.width = w;
		}
		
		if(prevStartX > (width/2)){
			logoParams.x = newX;
			logoLayout.addRule(RelativeLayout.RIGHT_OF, -1);
			logoLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
			logoLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
			voiceButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			voiceButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			voiceButtonLayout.addRule(RelativeLayout.RIGHT_OF, R.id.logo);
		}else{
			voiceButtonLayout.addRule(RelativeLayout.RIGHT_OF, -1);
			voiceButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
			voiceButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
			logoLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			logoLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			logoLayout.addRule(RelativeLayout.RIGHT_OF, R.id.logo_voice_button_layout);
		}
		
		logo.setLayoutParams(logoLayout);
		voiceButton.setLayoutParams(voiceButtonLayout);
		wm.updateViewLayout(this, logoParams);
	}

	private void reverseViewPosition() {
		logoParams.x  = (int) prevStartX;
		logoParams.y = (int) prevStartY;
		
		voiceButtonLayout.width = 0;
		voiceButton.setLayoutParams(voiceButtonLayout);
		wm.updateViewLayout(this, logoParams);
	}

	private void fixedViewPosition() {
		int finalX = (int) (x - mTouchStartX);
		int finalY = (int) (y - mTouchStartY);
		
		Display display = wm.getDefaultDisplay();
    	int width = display.getWidth();

	    if(finalX > (width/2)) finalX = width - logo.getWidth();
	    else finalX = 0;
    	
    	logoParams.x = finalX;
    	logoParams.y = finalY;
    	
		wm.updateViewLayout(this, logoParams);
	}

	private void startCapture() {
		if(DEBUG) Log.d(TAG, "start Capture");
		startSpeak();
		
		Intent intent = new Intent(mContext, VoiceAssistantService.class);
		intent.setAction(VoiceAssistantService.FLOATLOGO_START_CAPTURE_ACTION);
		mContext.startService(intent);
		isRecord = true;
	}

	private void stopCapture() {
		if(DEBUG) Log.d(TAG, "stop Capture");
		
		Intent intent = new Intent(mContext, VoiceAssistantService.class);
		intent.setAction(VoiceAssistantService.FLOATLOGO_STOP_CAPTURE_ACTION);
		mContext.startService(intent);
		isRecord = false;
		
		speakStop();
		handler.sendEmptyMessage(REFRESH_INIT_VIEW);
	}

	public void speak(int size) {
		switch (size) {
		case 1:
			imgSpeakBG = R.drawable.bg_speak_size01;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 2:
			imgSpeakBG = R.drawable.bg_speak_size02;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 3:
			imgSpeakBG = R.drawable.bg_speak_size03;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 4:
			imgSpeakBG = R.drawable.bg_speak_size04;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 5:
			imgSpeakBG = R.drawable.bg_speak_size05;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 6:
			imgSpeakBG = R.drawable.bg_speak_size06;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 7:
			imgSpeakBG = R.drawable.bg_speak_size07;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 8:
			imgSpeakBG = R.drawable.bg_speak_size08;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 9:
			imgSpeakBG = R.drawable.bg_speak_size09;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 10:
			imgSpeakBG = R.drawable.bg_speak_size10;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 11:
			imgSpeakBG = R.drawable.bg_speak_size11;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 12:
			imgSpeakBG = R.drawable.bg_speak_size12;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		default:
			imgSpeakBG = 0;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		}
	}

	private void startSpeak() {
		handler.sendEmptyMessage(REFRESH_START_SPEAK_VIEW);
	}

	private void speakStop() {
		handler.sendEmptyMessage(REFRESH_MIKE_GRAY);
	}
}
