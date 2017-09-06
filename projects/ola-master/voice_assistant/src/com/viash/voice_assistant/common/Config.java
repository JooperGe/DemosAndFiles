package com.viash.voice_assistant.common;

public class Config {
	public static boolean USING_HTTP_WITH_SERVER = true;
	public static final String HTTP_SERVER = "HttpServer";
	public static String WHICH_SERVER = HTTP_SERVER;
	public static boolean USE_SOFT_VOICE_KEY = false;
	
	public static boolean ShouldAlwaysShowTopVoiceBtn()
	{
		//return SavedData.ismShowTopVoiceButton();
		return USE_SOFT_VOICE_KEY;
	}
}
