package com.via.android.voice.floatview;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.handler.AppCrashHandler;

import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class VIAApplication extends Application {
	private static final String TAG = "VIAApplication";
	private static final boolean DEBUG = true;
	
	public static final int LOGO_FLOAT_VIEW = 1;
	public static final int THIRD_VOICE_BUTTON_VIEW = 2;
	public static final int THIRD_COMMUNICATION_VIEW = 3;
	
	private WindowManager.LayoutParams logoParams = new WindowManager.LayoutParams();
	private WindowManager.LayoutParams thirdVoiceButtonParams = new WindowManager.LayoutParams();
	private WindowManager.LayoutParams thirdCommunicationParams = new WindowManager.LayoutParams();
	
	private LogoFloatView logoFV = null;
	private ThirdVoiceButtonView thirdVoiceButtonFV = null;
	private ThirdCommunicationView thirdCommunicationView = null;
	@Override
	public void onCreate() {
		super.onCreate();
		AppCrashHandler handler = AppCrashHandler.getInstance();
		handler.initUcExceptionHandler(this);
	}
	public WindowManager.LayoutParams getWMParams(int vid){
		switch(vid){
		case LOGO_FLOAT_VIEW:
			return logoParams;
		case THIRD_VOICE_BUTTON_VIEW:
			return thirdVoiceButtonParams;
		case THIRD_COMMUNICATION_VIEW:
			return thirdCommunicationParams;
		}
		return null;
	}
	
	public void releaseView(int vid){
		switch(vid){
		case LOGO_FLOAT_VIEW:
			releaseLogoView();
			break;
		case THIRD_VOICE_BUTTON_VIEW:
			releaseThirdVoiceButtonView();
			break;
		case THIRD_COMMUNICATION_VIEW:
			releaseThirdCommunicationView();
			break;
		}
	}

	public void createView(int vid){
		switch(vid){
		case LOGO_FLOAT_VIEW:
			createLogoView();
			break;
		case THIRD_VOICE_BUTTON_VIEW:
			createThirdVoiceButtonView();
			break;
		case THIRD_COMMUNICATION_VIEW:
			createThirdCommunicationView();
			break;
		}
	}
	
	public void updateView(int vid){
		switch(vid){
		case LOGO_FLOAT_VIEW:
			// updateLogoView();
			break;
		case THIRD_VOICE_BUTTON_VIEW:
			// updateThirdVoiceButtonView();
			break;
		}
	}
	
	/**
	 * Release Floating Windows
	 */
	private void releaseLogoView(){
		if(logoFV == null) return;
    	WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    	wm.removeView(logoFV);
    	logoFV = null;
    	System.gc();
	}
	private void releaseThirdVoiceButtonView() {
		if(thirdVoiceButtonFV == null) return;
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    	wm.removeView(thirdVoiceButtonFV);
    	thirdVoiceButtonFV = null;
    	System.gc();
    	
    	// also release communication view
    	releaseThirdCommunicationView();
	}
	private void releaseThirdCommunicationView() {
		if(thirdCommunicationView == null) return;
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    	wm.removeView(thirdCommunicationView);
    	thirdCommunicationView = null;
    	System.gc();
	}
	
	/**
	 * Create Floating Windows
	 */
	private void createLogoView(){
		if(logoFV != null) return;
		logoFV = new LogoFloatView(this);
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
    	int width = display.getWidth();
    	
		//logoFV.setImageResource(R.drawable.ic_launcher);

		logoParams.type = LayoutParams.TYPE_PHONE;
		logoParams.format = PixelFormat.RGBA_8888;
		
		/*
		 * flags
		 * LayoutParams.FLAG_NOT_TOUCH_MODAL
		 * LayoutParams.FLAG_NOT_FOCUSABLE
		 * LayoutParams.FLAG_NOT_TOUCHABLE
		 */
		logoParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		logoParams.gravity = Gravity.LEFT | Gravity.TOP;
		logoParams.x = width - 100;
		logoParams.y = 0;
		logoParams.width = LayoutParams.WRAP_CONTENT;
		logoParams.height = LayoutParams.WRAP_CONTENT;
    	
		wm.addView(logoFV, logoParams);
	}
	private void createThirdVoiceButtonView() {
		if(thirdVoiceButtonFV != null) return;
		thirdVoiceButtonFV = new ThirdVoiceButtonView(this);
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    	
    	thirdVoiceButtonParams.type = LayoutParams.TYPE_PHONE;
		thirdVoiceButtonParams.format = PixelFormat.RGBA_8888;
		/*
		 * flags
		 * LayoutParams.FLAG_NOT_TOUCH_MODAL
		 * LayoutParams.FLAG_NOT_FOCUSABLE
		 * LayoutParams.FLAG_NOT_TOUCHABLE
		 */
		//thirdVoiceButtonParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		thirdVoiceButtonParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
		//wmParams.x = width - 100;
		//wmParams.y = 0;
		thirdVoiceButtonParams.width = LayoutParams.WRAP_CONTENT;
		thirdVoiceButtonParams.height = LayoutParams.WRAP_CONTENT;
    	
		Log.d(TAG, "VoiceButton: [" + thirdVoiceButtonParams.x + "," + thirdVoiceButtonParams.y + "] (" + thirdVoiceButtonParams.width + "x" + thirdVoiceButtonParams.height + ")");
		
		wm.addView(thirdVoiceButtonFV, thirdVoiceButtonParams);
	}
	private void createThirdCommunicationView() {
		if(thirdCommunicationView != null) return;
		if(thirdVoiceButtonFV == null){
			Log.d(TAG, "thirdVoiceButtonFV is null.");
			return;
		}
		
		thirdCommunicationView = new ThirdCommunicationView(this);
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
    	int width = display.getWidth();
    	int height = display.getHeight() / 2;

    	thirdCommunicationParams.type = LayoutParams.TYPE_PHONE;
    	thirdCommunicationParams.format = PixelFormat.RGBA_8888;
		/*
		 * flags
		 * LayoutParams.FLAG_NOT_TOUCH_MODAL
		 * LayoutParams.FLAG_NOT_FOCUSABLE
		 * LayoutParams.FLAG_NOT_TOUCHABLE
		 */
    	thirdCommunicationParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
    	thirdCommunicationParams.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
    	//thirdCommunicationParams.x = 0;
    	
    	if(thirdVoiceButtonFV != null)
    	{
    		thirdCommunicationParams.y = height / 2 - thirdVoiceButtonFV.getHeight() - 30;
    	}
    	else
    	{
    		thirdCommunicationParams.y = (height - (height/2)) - thirdVoiceButtonParams.height - 30;
    	}
    	
    	thirdCommunicationParams.width = width - 100;
		thirdCommunicationParams.height = height;
    	
		Log.d(TAG, "CommunicationView: [" + thirdCommunicationParams.x + "," + thirdCommunicationParams.y + "] (" + thirdCommunicationParams.width + "x" + thirdCommunicationParams.height + ")");

		wm.addView(thirdCommunicationView, thirdCommunicationParams);
	}

	/**
	 * Third-party Voice Button
	 */
	public ThirdVoiceButtonView getThridVoiceFV(){
		return thirdVoiceButtonFV;
	}
	
	/**
	 * Third-party Communication View
	 */
	public ThirdCommunicationView getThridCommunicationFV(){
		return thirdCommunicationView;
	}
	
	/**
	 * Logo Floating Window
	 */
	public LogoFloatView getLogoFV(){
		return logoFV;
	}
}
