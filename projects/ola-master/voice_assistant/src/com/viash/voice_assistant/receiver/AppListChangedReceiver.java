package com.viash.voice_assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.viash.voicelib.msg.MsgConst;

public class AppListChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "AppListChangedReceiver";
	private static Handler handler = null;
//	private static AppListChangedReceiver instance = null;

	public AppListChangedReceiver() {
		super();
	}

	public AppListChangedReceiver(Context context, Handler mHandler) {
		super();
		AppListChangedReceiver.handler = mHandler;
	}

//	public static AppListChangedReceiver getInstance(Context context,
//			Handler mHandler) {
//		if (instance == null) {
//			instance = new AppListChangedReceiver(context, mHandler);
//		}
//		return instance;
//	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接收安装广播
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String packageName = intent.getDataString();
			Log.i(TAG, "安装了:" + packageName + "包名的程序");

			if (handler!=null) {
				Message message = handler
						.obtainMessage(MsgConst.MSG_APPLIST_CHANGED);
				message.obj = packageName;
				message.arg1 = 1;//add an App
				handler.sendMessage(message);
			}
		}
		// 接收卸载广播
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			String packageName = intent.getDataString();
			Log.i(TAG, "卸载了:" + packageName + "包名的程序");
			
			if (handler!=null) {
				Message message = handler
						.obtainMessage(MsgConst.MSG_APPLIST_CHANGED);
				message.obj = packageName;
				message.arg1 = 2;//remove an App
				handler.sendMessage(message);
			}
		}
	}
}
