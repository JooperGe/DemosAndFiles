package com.viash.voice_assistant.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;

public class VoiceSdkServiceInterface {
	public static final String THIRDPARTY_START_CAPTURE_ACTION = "com.viash.voice_assistant.thridparty.STARTCAPTURE";
	public static final String THIRDPARTY_STOP_CAPTURE_ACTION = "com.viash.voice_assistant.thridparty.STOPCAPTURE";
	public static final String THIRDPARTY_CLOSE_COMMUNICATIONVIEW_ACTION = "com.viash.voice_assistant.thridparty.CLOSECOMMUNICATIONVIEW";
	public static final String THIRDPARTY_CLOSED = "com.viash.voice_assistant.thridparty.closed";
	public static final String THIRDPARTY_NAME = "third-part";
	public static final String TOGGLE_VOICE_UI = "com.viash.voice_assistant.toggle_voice_ui";
	
	public static final String THIRDPARTY_START_TTS_ACTION = "com.viash.voice_assistant.thridparty.start_tts";
	public static final String THIRDPARTY_STOP_TTS_ACTION = "com.viash.voice_assistant.thridparty.stop_tts";
	public static final String ACTION_SHOW_RECORD_UI = "com.viash.voice.sdk.service.show_record_ui";
	

	public static void bindToVoiceService(Context context, ServiceConnection connection)
	{
		if(SavedData.isHttpMode())
			context.bindService(new Intent(context, VoiceSdkService.class), connection, Context.BIND_AUTO_CREATE);
		else
			context.bindService(new Intent(context, VoiceAssistantService.class), connection, Context.BIND_AUTO_CREATE);
	}
}
