package com.viash.voice_assistant.receiver;

import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.LockScreenService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenReceiver extends BroadcastReceiver {
	private final static String TAG = "LockScreenReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		SavedData.init(context);
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			SavedData.setSystemRestart(false);
			Log.i(TAG, "ACTION_BOOT_COMPLETED");
			context.startService(new Intent(context,LockScreenService.class)); 
		}
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)){
			SavedData.setSystemRestart(true);
			Log.i(TAG, "ACTION_SHUTDOWN");
		}
	}
}
