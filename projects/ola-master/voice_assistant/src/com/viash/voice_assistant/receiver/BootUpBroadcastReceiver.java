package com.viash.voice_assistant.receiver;

import com.viash.voice_assistant.common.Config;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.data.AppData;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
public class BootUpBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		
		if(action.equals("android.intent.action.BOOT_COMPLETED"))
		{
			if(Config.USE_SOFT_VOICE_KEY || SavedData.isVoiceWakeUpOpen())
			{
				Intent serviceSdk = new Intent();
				serviceSdk.setClass(context,  VoiceSdkService.class);
				context.startService(serviceSdk);
			}
		}
		else if(action.equals(AppData.COM_VIASH_VOICE_ASSISTANT_REBOOT))
		{
			try{
				 Thread.sleep(1000);
				 Intent intent1 = new Intent();
				 ComponentName comp = new ComponentName("com.viash.voice_assistant","com.viash.voice_assistant.activity.GuideActivity");
				 intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 intent1.setComponent(comp);
				 context.startActivity(intent1);
			}catch(Exception e)
			{
			   e.printStackTrace();	
			}
		}
	}

}