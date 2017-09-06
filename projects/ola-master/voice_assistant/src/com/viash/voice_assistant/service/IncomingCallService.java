package com.viash.voice_assistant.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.viash.voice_assistant.receiver.IncomingCallReceiver;
import com.viash.voicelib.utils.CustomToast;


public class IncomingCallService extends Service{

	private IncomingCallReceiver  callReceiver;
	@Override
	public void onCreate()
	{
		super.onCreate();
		CustomToast.makeToast(getApplicationContext(), "entry IncomingCallService");//, Toast.LENGTH_LONG);
		callReceiver = new IncomingCallReceiver();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}