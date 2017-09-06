package com.viash.voice_assistant.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.via.android.voice.floatview.FloatViewService;
import com.via.android.voice.floatview.IFloatViewService;

public class FloatView {
	private static final boolean DEBUG = true;
	private static final String TAG = "FloatView";
	
	private static int status = 0;
	private static IFloatViewService floatviewService = null;
	private static FloatViewServiceConnection floatviewServiceConn = null;
	
	public static IFloatViewService getFloatViewService(){
		return floatviewService;
	}

	public static int getStatus(){
		return status;
	}
	public static void bindToFloatViewService(Context context) {
		status = 1;
		if(DEBUG) Log.d(TAG, "bindToMediaPlayerService()");
		if(floatviewService != null) return;
		context.startService(new Intent(context, FloatViewService.class));
		floatviewServiceConn = new FloatViewServiceConnection();
		context.bindService(new Intent(context, FloatViewService.class), floatviewServiceConn, 0);
	}
	public static void unBindFromMediaPlayerservice(Context context) {
		if(DEBUG) Log.d(TAG, "unBindFromMediaPlayerservice()");
		
		int MAX_WAIT_TIME = 50;
		int wait_time = 0;
		while(status == 1){
			if(wait_time >= MAX_WAIT_TIME){
				floatviewService = null;
				floatviewServiceConn = null;
				status = 0;
				return;
			}
			wait_time++;
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		context.unbindService(floatviewServiceConn);
		floatviewService = null;
		floatviewServiceConn = null;
		status = 0;
	}
	
	static class FloatViewServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if(DEBUG) Log.d(TAG, "onServiceConnected()");
			floatviewService = IFloatViewService.Stub.asInterface(service);
			status = 2;
			try {
				floatviewService.initFloatView();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(DEBUG) Log.d(TAG, "onServiceDisconnected()");
			floatviewService = null;
			status = 0;
		}
		
	};
}
