package com.viash.voice_assistant.activity.assistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.service.IMainService;
import com.viash.voice_assistant.service.MainService;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.msg.MsgConst;


/**
 * 
 * 拆分 主activity功能
 * 
 * @author fenglei
 *
 */
public class ServiceHolder {
	private static final boolean DEBUG = true;
	private static final String TAG = "ServiceHolder";
	
	private NewAssistActivity mainActivity;
	private static ServiceHolder _instance = null;

	private IMainService mMainService = null;
	private MainServiceConnection mMainServiceConnection;
	
	private VoiceAssistantServiceConnection mServiceConnection;
	
	
	private ServiceHolder(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static ServiceHolder init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new ServiceHolder(main);
		return _instance;
	}

	public static ServiceHolder getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init ServiceHelper");

		return _instance;
	}
	
	/**
	 * Main Service
	 */
	public void initMainService() {
		mMainServiceConnection = new MainServiceConnection();
		mainActivity.bindService(new Intent(mainActivity, MainService.class),
				mMainServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void stopMainService() {
		if (mMainService != null) {
			mainActivity.unbindService(mMainServiceConnection);
			mMainService = null;
			mMainServiceConnection = null;
		}
	}

	class MainServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMainService = IMainService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMainService = null;
		}
	}

	
	/**
	 * VoiceAssistantService
	 */
	public void initVoiceAssistantService() {
		if (DEBUG)
			Log.d(TAG, "initVoiceAssistantService");

		mServiceConnection = new VoiceAssistantServiceConnection();
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
		{
			mainActivity.bindService(new Intent(mainActivity, VoiceSdkService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
		}
		else
		{
			mainActivity.bindService(new Intent(mainActivity, VoiceAssistantService.class),
					mServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	public void stopVoiceAssistantService() {
		if (mServiceConnection != null) {
			if (DEBUG)
				Log.d(TAG, "stopVoiceAssistantService");

			//unregisterClient();

			mainActivity.unbindService(mServiceConnection);
			mainActivity.mtServiceMessenger = null;
			mServiceConnection = null;
		}
		//the following code is: force stop the service//it may started by SMSReceiver incomingCallReceiver.. 
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
		{
		// Do not need to stop VoiceSdkService
		//	this.stopService(new Intent(this, VoiceSdkService.class));
		}
		else
		{
			mainActivity.stopService(new Intent(mainActivity, VoiceAssistantService.class));
		}
	}

	class VoiceAssistantServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DEBUG)
				Log.d(TAG, "VoiceAssistantService on Connected.");

			if (mainActivity.mtServiceMessenger == null){
				mainActivity.mtServiceMessenger = new Messenger(service);
				UIStateHelper.getInstantce().setProcessingState(MsgConst.UI_STATE_INITED);
				SplashHelper.getInstantce().closeSplash();				
			}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (DEBUG)
				Log.d(TAG, "onServiceDisconnected.");
			mainActivity.mtServiceMessenger = null;
		}
	}


	public IMainService getmMainService() {
		return mMainService;
	}
}
