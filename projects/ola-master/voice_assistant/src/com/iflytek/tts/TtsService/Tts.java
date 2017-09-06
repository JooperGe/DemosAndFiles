package com.iflytek.tts.TtsService;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public final class Tts{
	//TTS 部分请自行开发，原来的代码因为商务的关系，不能够提供。
	public static int TTS_HIGH_PRIORITY = 10;// no one can stop this tts.
	public static int TTS_NORMAL_PRIORITY = 5; //for NEWS, story, poem TTS play, this TTS is still playing when screen off.
	public static int TTS_LOW_PRIORITY = 0; // each operation can Stop this TTS.
	
	
	private static TextToSpeech boruiTTS = null;
	
	public interface ITtsListener
	{
		public void onPlayEnd();		
	}

	static {
			
	}
	
	
	public static void initTTS(final Context context){
		if(null == boruiTTS){
			boruiTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					Toast.makeText(context, "正在初始化TTS...", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	private synchronized static TextToSpeech getTTS(final Context context){
		return boruiTTS;
	}
	
	public synchronized static boolean setSpeaker(int id)
	{
		return true;
	}
	
	public synchronized static int setSpeed(short speed)
	{
		return 0;
	}
	
	public synchronized static int setPitch(short pitch)
	{
		return 0;
	}
	
	public synchronized static int setStyle(short style)
	{
		return 0;
	}
	
	public synchronized static boolean isPlaying()
	{
		if(null != boruiTTS)
			return boruiTTS.isSpeaking();
		return false;
	}
	
	public synchronized static void playText(Context context, String text, ITtsListener listener)
	{
//		Toast.makeText(context, "toSpeak:"+text, Toast.LENGTH_SHORT).show();
		
		getTTS(context).speak(text, TextToSpeech.QUEUE_FLUSH, null);
		listener.onPlayEnd(); 
	}
	
	public synchronized static void playText(Context context, String text, ITtsListener listener, int priority)
	{
//		Toast.makeText(context, "toSpeak:"+text, Toast.LENGTH_SHORT).show();
		
		getTTS(context).speak(text, TextToSpeech.QUEUE_FLUSH, null);
		listener.onPlayEnd(); 
	}
	
	public synchronized static int setParam(int paramId,int value)
	{
		return 0;
	}
	
	public synchronized static int getParam(int paramId)
	{
		return 0;
	}
	
	//if input TTS priority is bigger than current TTS, stop current TTS.
	//Stop all TTS which priority lower or equal input value
	public static void stop(int priority)
	{
		if(boruiTTS!= null)
			boruiTTS.stop();
	}
	
	public static void stop()
	{
		if(boruiTTS!= null)
			boruiTTS.stop();
	}
	
	public static int create(String resFilename)
	{
		return 0;
	}
	
	public static int destroy()
	{
		return 0;
	}

	protected static class PlayThread extends Thread{		
		
	}
	
}
