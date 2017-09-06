package com.viash.voice_assistant.receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.viash.voice_assistant.common.AppDownloadManager;
import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voice_assistant.component.DownloadNotification;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.CheckServerMsgService;
import com.viash.voicelib.utils.CustomToast;

public class MessageReceiver extends BroadcastReceiver {
	private static final boolean DEBUG = true;
	private static final String TAG = "MessageReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			SavedData.init(context);
			
			Intent service = new Intent();
			service.setClass(context, CheckServerMsgService.class);
			context.startService(service);
			
		//	FloatView.bindToFloatViewService(context.getApplicationContext());
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			if (DEBUG)
				Log.d(TAG, "add: " + intent.getData().toString());
			
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
			if (DEBUG)
				Log.d(TAG, "remove: " + intent.getData().toString());
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			if (DEBUG)
				Log.d(TAG, "replace: " + intent.getData().toString());
		} else if (intent.getAction().equals(
				DownloadNotification.NOTIFICATION_CANCEL_ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String id = bundle.getString("id");
				if (id == null) {
					if (DEBUG)
						Log.i(TAG, "stop auto update download");
					AutoUpdate.stop();
				} else {
					if (DEBUG)
						Log.i(TAG, "stop download apk");
					AppDownloadManager.stopDownload(context, id);
				}
			} else {
				if (DEBUG)
					Log.i(TAG, "stop auto update download");
				AutoUpdate.stop();
			}
			// UpdateNotification.cancel();
		} else if (intent.getAction().equals(
				DownloadNotification.NOTIFICATION_STARTDOWNLOAD_ACTION)) {
			if (DEBUG)
				Log.i(TAG, "start auto update download");
			CustomToast.makeToast(context, "开始下载更新");//, Toast.LENGTH_SHORT).show();
			AutoUpdate.download(context);
		}
	}

}
