package com.viash.voice_assistant.receiver;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract;
import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.TtsUtil;

public class SmsReceiver extends BroadcastReceiver{


	private static Context mContext;
	private static Handler mHandler;

	private static String mContactName = null;
	private static String mPhoneNumber = null;
	private static String mSMSContent = null;
	
	private static boolean isReplySMS = false;
	//private static boolean isServiceNeedClose = false;
	
	private static WakeLock wl;
	
	protected static AudioManager mAudioManager = null; 
	private static OnAudioFocusChangeListener mAudioFocusChangeListener = null;
	
	/*public static boolean isServiceNeedStop() {
		return isServiceNeedClose;
	}
	
	public static void setServiceNeedStop(boolean value) {
		isServiceNeedClose = value;
	}*/
	
	public static boolean isReplySMS() {
		return isReplySMS;
	}

	public static void setReplySMS(boolean value) {
		isReplySMS = value;
	}

	public static String getContactName() {
		if (mContactName == null) {
			return "";
		}
		return mContactName;
	}

	public static String getPhoneNumber() {
		if (mPhoneNumber == null) {
			return "";
		}
		return mPhoneNumber;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		
		if (intent.getAction().equals(AppData.RECEIVE_SMS_ACTION)) {
			if (!SavedData.isAutoRemindSMS()) {
				return;
			}
			
			if(IncomingCallShareState.getHandleCallState() != 2){
				return;
			}
			
			AudioManager audioManager =(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			Log.i("onReceive", audioManager.getMode() + "");
			if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT){
				return;
			}
			isReplySMS = true;
			Intent serviceIntent = null;
			if (!isServiceRun(context)) {
				//isServiceNeedClose = true;
				if(com.viash.voice_assistant.common.Config.WHICH_SERVER != null && com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
					serviceIntent = new Intent(context,VoiceSdkService.class);
				}
				else{
					serviceIntent = new Intent(context,VoiceAssistantService.class);
				}
				try{
					String ttsPath = TtsUtil.copyTtsData(context);
					if (ttsPath != null) {
						Tts.create(ttsPath);
						Tts.setSpeaker(SavedData.getVoiceType());
					}
					context.startService(serviceIntent);
				}catch(Exception e) {
					 e.printStackTrace();
				}
			}else {
				//isServiceNeedClose = false;
			}
			
			//SmsMessage[] messages = getMessagesFromIntent(intent);
			String[] message = (String[]) intent.getStringArrayExtra(AppData.RECEIVE_SMS_ACTION);
			if (message == null) {
				return;
			}
			if (message.length != 2) {
				return;
			}
			
			
			String str = mContext.getString(R.string.sms_auto_remind);
			String body = mContext.getString(R.string.sms_auto_remind_content_head);
			mContactName = mContext.getString(R.string.sms_auto_remind_unknown_sender);
			
			Log.i("SmsReceiver", message[0] + " : " + message[1]);

			mPhoneNumber = message[0];
			mSMSContent = message[1];
			
			if (mPhoneNumber != null) {
				mContactName = getContactNameFromPhoneBook(context,	mPhoneNumber);
			}
			body = body + mSMSContent;
			
			if (SavedData.isAutoRemindSMSNameOrNumber()) {
				if (!SavedData.isAutoRemindSMSContent()) {
					if (mContactName == null) {
						str = mPhoneNumber + mContext.getString(R.string.sms_auto_remind_with_name_and_content);
					}
					else {
						str = mContactName + mContext.getString(R.string.sms_auto_remind_with_name_and_content);
					}
				} else {
					if (mContactName == null) {
						str = mPhoneNumber + mContext.getString(R.string.sms_auto_remind_with_name_and_content) + body;
					}
					else {
						str = mContactName + mContext.getString(R.string.sms_auto_remind_with_name_and_content) + body;
					}
				}
				playTts(str + mContext.getString(R.string.sms_auto_remind_end), isReplySMS);
			} else {
				playTts(str, false);
			}
		}

		if(intent.getAction().equals(AppData.SENT_SMS_ACTION))
		{
			String format;
			String msg;
			String number = intent.getStringExtra(AppData.SENT_SMS_NUMBER);
			if(number != null && number.length() > 0)
			{
				switch (getResultCode()) 
				{
				case Activity.RESULT_OK:
					String content = intent.getStringExtra(AppData.SENT_SMS_CONTENT);
					if(content != null)
					{						
						format = context.getString(R.string.send_sms_success);
						msg = String.format(format, number);
						CustomToast.makeToast(context, msg);//, Toast.LENGTH_SHORT).show();
						CallUtil.addSmsToSendBox(context, number, content);
					}
					
					break;
				default:
					format = context.getString(R.string.send_sms_failed);
					msg = String.format(format, number);
					CustomToast.makeToast(context, msg);//, Toast.LENGTH_SHORT).show();
					break;
				}
			}				
		}
	}
	
	/*private final SmsMessage[] getMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra(AppData.RECEIVE_SMS_ACTION);
		byte[][] pduObjs = new byte[messages.length][];
		for (int i = 0; i < messages.length; i++) {
			pduObjs[i] = (byte[]) messages[i];
		}
		byte[][] pdus = new byte[pduObjs.length][];
		int pduCount = pdus.length;
		SmsMessage[] msgs = new SmsMessage[pduCount];
		for (int i = 0; i < pduCount; i++) {
			pdus[i] = pduObjs[i];
			msgs[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return msgs;
	}*/
	
	private String getContactNameFromPhoneBook(Context context, String phoneNum) {
		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };
		
		if (phoneNum.startsWith("+86")) {
			phoneNum = phoneNum.substring(3);
		}

		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, 
				ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%" + phoneNum + "%'",
				null, 
				null);

		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {  
	        contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));  
	        cursor.close();  
	    } 
	    return contactName;
	}
	
	public static void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	public static void playSMSContent() {
		if (mSMSContent != null) {
			playTts(mSMSContent + mContext.getString(R.string.sms_auto_remind_end), true);
		}
	}
	
	@SuppressLint("NewApi")
	private static boolean playTts(String str, final boolean needVR) {
		boolean ret = false;
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);  
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SmsReceiver");  
        wl.acquire();
		
		Tts.ITtsListener listener = new Tts.ITtsListener() {
			@Override
			public void onPlayEnd() {
				//TODO
				mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
				if (mHandler != null && needVR) {
					if(IncomingCallShareState.getHandleCallState() != 2){
						isReplySMS = false;
					}else {
						
						Message msg = mHandler.obtainMessage(MsgConst.MSG_START_CAPTURE);
						mHandler.sendMessage(msg);
					}
				}
				if (!needVR) {
					isReplySMS = false;
				}
				
				if (wl != null) {
					wl.release();
				}
			}
		};
		
		if (Build.VERSION.SDK_INT >= 8) {
			if (mAudioManager == null) {
				mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			}
			if(mAudioFocusChangeListener == null)
			{
				mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
					
				    @TargetApi(Build.VERSION_CODES.FROYO)
					public void onAudioFocusChange(int focusChange) {  
				        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				        	//TODO
				        	mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
				        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				        	/*if (Tts.isPlaying()) {
				        		Tts.stop(true);
				        	}*/
				        	mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
	//						Log.i("abandonAudioFocus", "onAudioFocusChange");
				        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
				        	//TODO
				        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) { 
				        	//TODO
				        }  
				    }  
				};
			}
		}
			
		
		if(IncomingCallShareState.getHandleCallState() != 2){
			isReplySMS = false;
			return false;
			//return directly.
		}else {//if (SavedData.getVoiceSetting()) { //check play Sms is on;//TODO
			
			if (mAudioManager != null && Build.VERSION.SDK_INT >= 8) {
				mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			}
			Tts.playText(mContext, str, listener, Tts.TTS_NORMAL_PRIORITY);
			if (mHandler != null) {
				mHandler.sendEmptyMessage(MsgConst.CLIENT_ACTION_ABORT_VR_BY_PHONE_OR_SMS);
				mHandler.sendEmptyMessage(MsgConst.SERVICE_ACTION_TTS_PLAY_START);
			}
		}
		ret = true;
		return ret;
	}
	
	public static boolean isServiceRun(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> list = am.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : list) {
			if(com.viash.voice_assistant.common.Config.WHICH_SERVER != null && com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
				if (info.service.getClassName().equals("com.viash.voice_assistant.service.VoiceSdkService")) {
					return true;
				}
			}
			else {
				if (info.service.getClassName().equals("com.viash.voice_assistant.service.VoiceAssistantService")) {
					return true;
				}
			}
		}
		return false;
	}
}
