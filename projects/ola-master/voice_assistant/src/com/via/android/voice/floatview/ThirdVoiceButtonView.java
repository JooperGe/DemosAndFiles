package com.via.android.voice.floatview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.Config;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.widget.VoiceButtonView;
import com.viash.voicelib.msg.MsgConst;

public class ThirdVoiceButtonView extends VoiceButtonView {
	private static final boolean DEBUG = true;
	private static final String TAG = "ThirdVoiceButtonView";

	private Context mContext;
	GestureDetector mGesture = null;  
	protected int mProcessingState = MsgConst.UI_STATE_INITED;
	
	protected float mLastX, mLastY;

	public ThirdVoiceButtonView(Context context) {
		super(context);

		mContext = context;
		initButton();
	//	startCapture();
		mGesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
		{			
			@Override  
	        public boolean onDown(MotionEvent e)  
	        {  
	            mLastX = e.getRawX();
	            mLastY = e.getRawY();
	    		
	            return super.onDown(e);  
	        }  

			 
			@Override  
	        public boolean onScroll(MotionEvent e1, MotionEvent e2,  
	                float distanceX, float distanceY)  
	        {  
				WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	        	
	    		WindowManager.LayoutParams voiceLP = (android.view.WindowManager.LayoutParams) ThirdVoiceButtonView.this.getLayoutParams();
	    		
	    		float x = e2.getRawX();
	    		float y = e2.getRawY();
	    		float delta = Math.abs(x - mLastX) + Math.abs(y - mLastY);
	    		if(delta >= 1.0f)
	    		{
		    		voiceLP.y += (y - mLastY);   
		    		voiceLP.x += (x - mLastX);  
		    		mLastX = x;
		    		mLastY = y;
		    		wm.updateViewLayout(ThirdVoiceButtonView.this, voiceLP);
	    		}
	    		
	            return true;
	        }


			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				onVoiceBtnClicked();
				return super.onSingleTapUp(e);
			}  
		}
		);  
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(Config.ShouldAlwaysShowTopVoiceBtn())
		{
			return false;
		}
		else
		{
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				switch(event.getKeyCode()){
				case KeyEvent.KEYCODE_BACK:
					closeThirdWindow();
					break;
				}
			}
			return super.dispatchKeyEvent(event);
		}
	}
	
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGesture.onTouchEvent(event);  
	}

	private void closeThirdWindow() {
		if (DEBUG) Log.d(TAG, "closeThirdWindow.");
		if(mProcessingState != MsgConst.UI_STATE_INITED) 
			return;
		startIntentWithAction(VoiceSdkServiceInterface.THIRDPARTY_CLOSED);
	}

	public void startCapture() {
		if (DEBUG)
			Log.i(TAG, "startCapture");
		btn_voiceSpeak.setBackgroundResource(R.drawable.voice_mic_pressed);
		startIntentWithAction(VoiceSdkServiceInterface.THIRDPARTY_START_CAPTURE_ACTION);
	}

	private void initButton() {
//		btn_voiceSpeak.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onVoiceBtnClicked();
//			}
//		});
		
		btn_voiceSpeak.setEnabled(false);
		btn_voiceSpeak.setClickable(false);
	}

	private void onVoiceBtnClicked() {
		if (DEBUG)
			Log.i(TAG, "onVoiceBtnClicked");
		
		
		if(mProcessingState == MsgConst.UI_STATE_SPEAKING){
			startIntentWithAction(VoiceSdkServiceInterface.THIRDPARTY_STOP_CAPTURE_ACTION);
		}else{
			// Start to record
			startCapture();
		}
	}
	
	public void initVoiceButton(){
		super.init();
		//isRecord = false;
		//btn_voiceSpeak.setEnabled(true);
		btn_voiceSpeak.setBackgroundResource(R.drawable.voice_mic_connected);
	}
	
	public void idleUI(){
		super.init();
		btn_voiceSpeak.setBackgroundResource(R.drawable.voice_mic_connected);
	}
	
	protected void startIntentWithAction(String action){
		Intent intent = null;
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
			intent = new Intent(mContext, VoiceSdkService.class);
		else
			intent = new Intent(mContext, VoiceAssistantService.class);
		intent.setAction(action);
		mContext.startService(intent);
	}
	
	public void setProcessState(int state)
	{
		mProcessingState = state;
		switch(state)
		{
		case MsgConst.UI_STATE_INITED:
			idleUI();
			break;
		case MsgConst.UI_STATE_SPEAKING:			
			startSpeak();
			break;
		case MsgConst.UI_STATE_RECOGNIZING:
			startLoading();
			break;
		}
	}
}
