package com.viash.voice_assistant.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.viash.voice_assistant.R;

public class VoiceView extends RelativeLayout {
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
	
	public VoiceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.layout_voice, this,true);
		imgv_voice_bg = (ImageView) findViewById(R.id.imgv_voice_bg);
		imgv_volume = (ImageView) findViewById(R.id.imgv_volume);
		imgv_mike = (ImageView) findViewById(R.id.imgv_mike);
		imgv_voice_bg.setVisibility(View.GONE);
		imgv_volume.setVisibility(View.GONE);
		initHandler();
		animation = AnimationUtils.loadAnimation(getContext(), R.anim.voice_alpha);	
	}
	private void initHandler(){
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
	public void speak(int size){
		switch (size) {
		case 1:
			imgSpeakBG =R.drawable.bg_speak_size01;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 2:
			imgSpeakBG =R.drawable.bg_speak_size02;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 3:
			imgSpeakBG =R.drawable.bg_speak_size03;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 4:
			imgSpeakBG =R.drawable.bg_speak_size04;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 5:
			imgSpeakBG =R.drawable.bg_speak_size05;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 6:
			imgSpeakBG =R.drawable.bg_speak_size06;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 7:
			imgSpeakBG =R.drawable.bg_speak_size07;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 8:
			imgSpeakBG =R.drawable.bg_speak_size08;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 9:
			imgSpeakBG =R.drawable.bg_speak_size09;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 10:
			imgSpeakBG =R.drawable.bg_speak_size10;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 11:
			imgSpeakBG =R.drawable.bg_speak_size11;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		case 12:
			imgSpeakBG =R.drawable.bg_speak_size12;
			handler.sendEmptyMessage(REFRESH_SET_IMG);
			break;
		default:
			imgSpeakBG = 0;
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
}
