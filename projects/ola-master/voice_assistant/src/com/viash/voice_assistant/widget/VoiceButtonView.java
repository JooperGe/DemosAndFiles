package com.viash.voice_assistant.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.viash.voice_assistant.R;

public class VoiceButtonView extends RelativeLayout {
	private ImageView imgv_voice_volume;
	private RotateView rotateview;
	protected Button btn_voiceSpeak;
	private Handler handler;
	private static final int REFRESH_ALPHA =1;
	private static final int REFRESH_SET_IMG =2;
	private static final int REFRESH_INIT_VIEW =3;
	private static final int REFRESH_START_SPEAK_VIEW =4;
	private static final int REFRESH_LOAD_VIEW =5;
	private static final int REFRESH_MIKE_WHITE =6;
	private static final int REFRESH_MIKE_GRAY =7;
	private int imgSpeakBG =0;
	
	public VoiceButtonView(Context context) {
		super(context);
		LayoutInflater.from(getContext()).inflate(R.layout.layout_voice_button, this, true);
		imgv_voice_volume = (ImageView) findViewById(R.id.third_imgv_volume);
		rotateview = (RotateView) findViewById(R.id.third_rotateview);
		btn_voiceSpeak = (Button) super.findViewById(R.id.third_btn_voice);
		initVoiceView();
		initHandler();
	}
	private void initHandler(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH_SET_IMG:
					if(imgSpeakBG !=0)
						imgv_voice_volume.setImageResource(imgSpeakBG);
					else
						imgv_voice_volume.setImageBitmap(null);
					break;
				case REFRESH_INIT_VIEW:
					rotateview.stopRotate();
					rotateview.setVisibility(View.GONE);
					imgv_voice_volume.setVisibility(View.GONE);
					break;
				case REFRESH_START_SPEAK_VIEW:
					imgv_voice_volume.setVisibility(View.VISIBLE);
					btn_voiceSpeak.setBackgroundResource(R.drawable.voice_mic_pressed);
					break;
				case REFRESH_LOAD_VIEW:
					rotateview.setVisibility(View.VISIBLE);
					imgv_voice_volume.setVisibility(View.GONE);
					rotateview.startRotate();
					break;
				default:
					break;
				}				
			}
		};
	}
	public void speak(int size){
		switch (size) {
		case 1:
			imgSpeakBG =R.drawable.voice_volume01;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 2:
			imgSpeakBG =R.drawable.voice_volume02;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 3:
			imgSpeakBG =R.drawable.voice_volume03;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 4:
			imgSpeakBG =R.drawable.voice_volume04;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 5:
			imgSpeakBG =R.drawable.voice_volume05;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 6:
			imgSpeakBG =R.drawable.voice_volume06;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 7:
			imgSpeakBG =R.drawable.voice_volume07;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 8:
			imgSpeakBG =R.drawable.voice_volume08;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 9:
			imgSpeakBG =R.drawable.voice_volume09;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 10:
			imgSpeakBG =R.drawable.voice_volume10;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 11:
			imgSpeakBG =R.drawable.voice_volume11;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 12:
			imgSpeakBG =R.drawable.voice_volume12;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		default:
			imgSpeakBG =R.drawable.voice_volume01;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		}
	}
	
	public void init(){
		handler.sendEmptyMessage(REFRESH_INIT_VIEW);
	}
	
	public void startSpeak(){
		handler.sendEmptyMessage(REFRESH_START_SPEAK_VIEW);
	}
	
	public void startLoading(){
		handler.sendEmptyMessage(REFRESH_LOAD_VIEW);
	}
	public void speakStop(){
		handler.sendEmptyMessage(REFRESH_MIKE_GRAY);
	}
	
	private void initVoiceView(){
		imgv_voice_volume.setImageResource(R.drawable.voice_volume01);
		imgv_voice_volume.setVisibility(View.GONE);
		rotateview.setImageDrawable(R.drawable.voice_load_rotate);
		rotateview.stopRotate();
		rotateview.setVisibility(View.GONE);		
	}
}
