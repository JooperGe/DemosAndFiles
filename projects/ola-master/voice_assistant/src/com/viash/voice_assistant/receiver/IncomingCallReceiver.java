package com.viash.voice_assistant.receiver;

import java.lang.reflect.Method;
import java.util.List;

import com.android.internal.telephony.ITelephony;
import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.IncomingCallShareState;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.MusicService;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.SocketUtil;
import com.viash.voicelib.utils.TtsUtil;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class IncomingCallReceiver extends BroadcastReceiver {

	private Context context;
	private TelephonyManager tm;
	private static String phoneNumber = null;
	private static String name = IncomingCallShareState.getName();
	private final String TAG = "IncomingCallReceiver";
	protected SocketUtil mSocketUtil;
	private ITelephony iTelephony = null;
	public static String SERVER;// = "10.27.27.228";
	public static int PORT = 80;
	private boolean isOpenAutoRemind = false;// true:open auto remind incoming
												// call,false:do not open
	// private static int strRingerMode = -1;
	private static int viberateRinger = -1;
	protected static Handler mHandler = null;
	protected static Handler mPlayTTsHandler;
	private static CountDownTimer countDownTimer = null;
	// private static final int MSG_ERROR = 4;
	public static final int MSG_CAPUTRE_START_COUNT = 1;
	public static final int MSG_PLAY_TTS_NOT_SUPPORT_ANSWER_BY_MANUAL = 2;
	
	private static int volumeRingBak = 0;
	// private static MediaPlayer mMediaPlayer = null;
	private static boolean isServiceAlreadyRun = false;
	private static Intent intentWhichService = null;
	private static boolean isSilence = false;
	private static int volumeMusicBak = 0;
	private static boolean isNeedRestoreRing = false;
	private static AudioManager audioManager = null;
	private static Tts.ITtsListener listener = null;
	private static boolean isIncomingCall = false;
	private static boolean pauseMusicBecauseCall = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		this.context = context;
		isOpenAutoRemind = SavedData.isAutoRemindIncomingCall();
		if (intent.getAction().equals(
				AppData.COM_VIASH_VOICE_ASSISTANT_START_RECORD)) {// play tts of inComingCall
			if (isOpenAutoRemind) {
				/*
				 * int msgCtrl = intent.getIntExtra("startRecord", -1);
				 * if(msgCtrl ==
				 * IncomingCallShareState.START_PLAY_TTS_DELAY_SECONDS) { Thread
				 * mThread = new Thread() {
				 * 
				 * @Override public void run() { try{ Thread.sleep(2000);
				 * if(needPlayTTs && IncomingCallShareState.isIncomgingCall())
				 * PlayTTSIncomingCall(IncomingCallShareState.getName());
				 * }catch(Exception e) { e.printStackTrace(); } } };
				 * mThread.start();
				 * 
				 * } else {
				 * PlayTTSIncomingCall(IncomingCallShareState.getName()); }
				 */
				replayRing();
				restoreRing();
				listener = new Tts.ITtsListener() {
					@Override
					public void onPlayEnd() {
						audioManager.setStreamVolume(AudioManager.STREAM_RING,
								volumeRingBak, 0);
					}
				};
				Tts.playText(IncomingCallReceiver.this.context, context
						.getString(R.string.incoming_call_answer_by_manual),
						listener, Tts.TTS_HIGH_PRIORITY);
			}
		} else if (intent.getAction().equals(
				AppData.COM_VIASH_VOICE_ASSISTANT_START_ANSWER_ACTION)) {// answer the msg of handling the call
			if (isOpenAutoRemind) {
				int msgAnswer = intent.getIntExtra("answerAction", -1);
				iTelephony = getITelephony();
				if (msgAnswer == IncomingCallShareState.ANSWER_RING) {// answer the all
					try {
						playTTsAutoAnswerForYou();
					} catch (Exception e) {
						e.printStackTrace();
						/*
						 * if(Tts.isPlaying()) Tts.stop(true); AudioManager
						 * audioManager
						 * =(AudioManager)context.getSystemService(Context
						 * .AUDIO_SERVICE);;
						 * audioManager.setRingerMode(AudioManager
						 * .RINGER_MODE_NORMAL); if(viberateRinger != -1)
						 * audioManager
						 * .setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						 * viberateRinger);
						 */
						// audioManager.setStreamMute(AudioManager.STREAM_RING,
						// false);
						/*
						 * (IncomingCallShareState.setName(null);
						 * IncomingCallShareState.setNumber(null); replayRing();
						 * restoreRing();
						 */
						// answerRingingCall();
					}
				} else if (msgAnswer == IncomingCallShareState.END_CALL) {// end call
					try {
						if (countDownTimer != null)
							countDownTimer.cancel();
						IncomingCallShareState.setName(null);
						iTelephony.endCall();

						/*
						 * if(Tts.isPlaying()) Tts.stop(true); AudioManager
						 * audioManager
						 * =(AudioManager)context.getSystemService(Context
						 * .AUDIO_SERVICE);
						 * audioManager.setRingerMode(AudioManager
						 * .RINGER_MODE_NORMAL); if(viberateRinger != -1)
						 * audioManager
						 * .setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						 * viberateRinger);
						 * audioManager.setStreamMute(AudioManager.STREAM_RING,
						 * false); IncomingCallShareState.setName(null);
						 * IncomingCallShareState.setNumber(null);
						 */

					} catch (Exception e) {
						e.printStackTrace();
						replayRing();
						restoreRing();
					}
				}
			}
		} else if (intent.getAction().equals(
				AppData.COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING)) {// restore ring when the net is broken
			if (isOpenAutoRemind) {
				/*
				 * if(Tts.isPlaying()) Tts.stop(true); AudioManager audioManager
				 * =
				 * (AudioManager)context.getSystemService(Context.AUDIO_SERVICE)
				 * ;
				 * audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				 * //Log.i(TAG," restoreRing strRingerMode = "+strRingerMode);
				 * //if(strRingerMode != -1)
				 * //audioManager.setRingerMode(strRingerMode);
				 * if(viberateRinger != -1)
				 * audioManager.setVibrateSetting(AudioManager
				 * .VIBRATE_TYPE_RINGER, viberateRinger);
				 */
				// audioManager.setStreamMute(AudioManager.STREAM_RING, false);
				// IncomingCallShareState.setName(null);
				// IncomingCallShareState.setNumber(null);
				replayRing();
				restoreRing();
				if (mHandler != null) {
					Message msg = mHandler
							.obtainMessage(MsgConst.MSG_STOP_CAPTURE);
					mHandler.sendMessage(msg);
				}
				/*if ((isServiceAlreadyRun == false)
						|| SmsReceiver.isServiceNeedStop())
					context.stopService(intentWhichService);*/
				listener = new Tts.ITtsListener() {
					@Override
					public void onPlayEnd() {
						audioManager.setStreamVolume(AudioManager.STREAM_RING,
								volumeRingBak, 0);
					}
				};
				Tts.playText(IncomingCallReceiver.this.context, context
						.getString(R.string.incoming_call_answer_by_manual),
						listener, Tts.TTS_HIGH_PRIORITY);
			}
		} else {
			tm = (TelephonyManager) context
					.getSystemService(Service.TELEPHONY_SERVICE);
			audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			if ((com.viash.voice_assistant.common.Config.WHICH_SERVER != null)
					&& com.viash.voice_assistant.common.Config.WHICH_SERVER
							.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
				intentWhichService = new Intent(context, VoiceSdkService.class);
			else
				intentWhichService = new Intent(context,
						VoiceAssistantService.class);

			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (pauseMusicBecauseCall && isMusicServiceRun(context)) {
					new CountDownTimer(1500, 1000) {
						@Override
						public void onTick(long millisUntilFinished) {
						}

						@Override
						public void onFinish() {
							if (!MusicService.isPlaying()) {
								MusicService.resumeMusicPlay();
								pauseMusicBecauseCall = false;
							}
						}
					}.start();
				}
				/*
				 * Intent intent2 = new Intent();
				 * intent2.setAction(AppData.COM_VIASH_VOICE_ASSISTANT_END_CALL
				 * ); this.context.sendBroadcast(intent2);//send msg "endCall"
				 */
				IncomingCallShareState.setHandleCallState(2);
				if (isSilence)
					return;
				if (isOpenAutoRemind && isIncomingCall) {
					Tts.stop(Tts.TTS_HIGH_PRIORITY);
					if (countDownTimer != null)
						countDownTimer.cancel();					
					IncomingCallShareState.setName(null);
					IncomingCallShareState.setNumber(null);
					/*
					 * audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL
					 * ); //if(strRingerMode != -1)
					 * //audioManager.setRingerMode(strRingerMode);
					 * if(viberateRinger != -1)
					 * audioManager.setVibrateSetting(AudioManager
					 * .VIBRATE_TYPE_RINGER, viberateRinger);
					 * //audioManager.setStreamMute(AudioManager.STREAM_RING,
					 * false);
					 */
					if (mHandler != null) {
						Message msg = mHandler
								.obtainMessage(MsgConst.MSG_STOP_CAPTURE);
						mHandler.sendMessage(msg);
					}
					restoreRing();
					audioManager.setStreamVolume(AudioManager.STREAM_RING,
							volumeRingBak, 0);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							volumeMusicBak, 0);
					stopReplayRing();
					/*if ((isServiceAlreadyRun == false)
							|| SmsReceiver.isServiceNeedStop())
						context.stopService(intentWhichService);*/
					isIncomingCall = false;
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:

				if (isMusicServiceRun(context)) {
					if (MusicService.isPlaying()) {
						MusicService.pauseMusic();
						pauseMusicBecauseCall = true;
					}
				}
				/*
				 * Intent intent3 = new Intent();
				 * intent3.setAction(AppData.COM_VIASH_VOICE_ASSISTANT_INCOMING_CALL
				 * );//send msg "incomingCall"
				 * this.context.sendBroadcast(intent3);
				 */
				IncomingCallShareState.setHandleCallState(0);
				if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
					isSilence = true;
					return;
				}
				if (isOpenAutoRemind) {					
					IncomingCallShareState.setRecordingFlag(false);
					SmsReceiver.setReplySMS(false);
					isServiceAlreadyRun = isServiceRun(context);
					CustomPhoneStateListener customPhoneListener = new CustomPhoneStateListener();
					tm.listen(customPhoneListener,
							PhoneStateListener.LISTEN_CALL_STATE);
					Bundle bundle = intent.getExtras();
					phoneNumber = bundle.getString("incoming_number");
					if (phoneNumber.startsWith("+86")) {
						phoneNumber = phoneNumber.substring(3);
					} else if (phoneNumber.startsWith("+")) {
						phoneNumber = phoneNumber.substring(1);
					}
					IncomingCallShareState.setNumber(phoneNumber);
					Log.v(TAG, "phoneNumber = " + phoneNumber);
					getPeople();

					// strRingerMode = audioManager.getRingerMode();
					// Log.i(TAG," TelephonyManager.CALL_STATE_RINGING strRingerMode = "+strRingerMode);
					/*
					 * audioManager.setStreamMute(AudioManager.STREAM_RING,
					 * false); viberateRinger =
					 * audioManager.getVibrateSetting(AudioManager
					 * .VIBRATE_TYPE_RINGER);
					 * audioManager.setVibrateSetting(AudioManager
					 * .VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
					 * try{ Thread.sleep(1000); }catch(Exception e){
					 * 
					 * }
					 * 
					 * audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT
					 * ); //audioManager.setStreamMute(AudioManager.STREAM_RING,
					 * true);
					 */
					volumeRingBak = audioManager
							.getStreamVolume(AudioManager.STREAM_RING);
					volumeMusicBak = audioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					audioManager
							.setStreamVolume(AudioManager.STREAM_RING, 2, 0);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							volumeRingBak, 0);
					isNeedRestoreRing = true;
					isIncomingCall = true;
					IncomingCallShareState.setName(name);

					try {
						initHandler();
						if (!isServiceAlreadyRun) {
							prepareToPlayTTS();
							context.startService(intentWhichService);
						}
						PlayTTSIncomingCall(name);
					} catch (Exception e) {
						/*
						 * audioManager.setRingerMode(AudioManager.
						 * RINGER_MODE_NORMAL); if(viberateRinger != -1)
						 * audioManager
						 * .setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						 * viberateRinger);
						 * //audioManager.setStreamMute(AudioManager
						 * .STREAM_RING, false);
						 */
						restoreRing();
						e.printStackTrace();
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (isMusicServiceRun(context)) {
					if (MusicService.isPlaying()) {
						MusicService.pauseMusic();
						pauseMusicBecauseCall = true;
					}
				}
				IncomingCallShareState.setHandleCallState(1);
				if (isSilence)
					return;
				if (isOpenAutoRemind && isIncomingCall) {
					Tts.stop(Tts.TTS_HIGH_PRIORITY);					
					IncomingCallShareState.setName(null);
					IncomingCallShareState.setNumber(null);
					// if(strRingerMode != -1)
					// audioManager.setRingerMode(strRingerMode);
					/*
					 * audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL
					 * ); if(viberateRinger != -1)
					 * audioManager.setVibrateSetting
					 * (AudioManager.VIBRATE_TYPE_RINGER, viberateRinger);
					 * //audioManager.setStreamMute(AudioManager.STREAM_RING,
					 * false);
					 */
					if (mHandler != null) {
						Message msg = mHandler
								.obtainMessage(MsgConst.MSG_STOP_CAPTURE);
						mHandler.sendMessage(msg);
					}
					restoreRing();
					audioManager.setStreamVolume(AudioManager.STREAM_RING,
							volumeRingBak, 0);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							volumeMusicBak, 0);
					stopReplayRing();
					/*if ((isServiceAlreadyRun == false)
							|| SmsReceiver.isServiceNeedStop())
						context.stopService(intentWhichService);*/
					break;
				}
			default:
				break;
			}
		}
	}

	public static void setHandler(Handler handler) {
		mHandler = handler;
	}

	private void prepareToPlayTTS() {
		String ttsPath = TtsUtil.copyTtsData(context);
		if (ttsPath != null) {
			Tts.create(ttsPath);
			Tts.setSpeaker(SavedData.getVoiceType());
		}
	}

	private void PlayTTSIncomingCall(String strName) {
		// Log.v(TAG, "PlayTTSIncomingCall() " + strName);
		Thread mThread = new Thread() {
			@Override
			public void run() {
				listener = new Tts.ITtsListener() {
					@Override
					public void onPlayEnd() {// send msg to start record

						if (mHandler != null) {
							audioManager.setStreamVolume(
									AudioManager.STREAM_RING, 0, 0);
							Message msg = mHandler
									.obtainMessage(MsgConst.MSG_START_CAPTURE);
							mHandler.sendMessage(msg);
							Message msg1 = mPlayTTsHandler
									.obtainMessage(MSG_CAPUTRE_START_COUNT);
							mPlayTTsHandler.sendMessage(msg1);
						} else {
							restoreRing();
						}
					}
				};
				if (mHandler != null) {
					mHandler.sendEmptyMessage(MsgConst.CLIENT_ACTION_ABORT_VR_BY_PHONE_OR_SMS);
				}
				if (SavedData.isAutoRemindIncomingCallName()) {
					if (IncomingCallShareState.getName().equals(
							IncomingCallShareState.UNKNOWN_NAME)
							|| IncomingCallShareState.getName() == null)
						Tts.playText(
								IncomingCallReceiver.this.context,
								phoneNumber
										+ ","
										+ context
												.getString(R.string.incoming_call_to_you),
								listener, Tts.TTS_HIGH_PRIORITY);
					else
						Tts.playText(
								IncomingCallReceiver.this.context,
								IncomingCallShareState.getName()
										+ ","
										+ context
												.getString(R.string.incoming_call_to_you),
								listener, Tts.TTS_HIGH_PRIORITY);
				} else {
					Tts.playText(IncomingCallReceiver.this.context,
							context.getString(R.string.incoming_call),
							listener, Tts.TTS_HIGH_PRIORITY);
				}
			}
		};
		mThread.start();
	}

	private void playTTsAutoAnswerForYou() {
		Tts.ITtsListener listener = new Tts.ITtsListener() {
			@Override
			public void onPlayEnd() {
				try {
					if (countDownTimer != null)
						countDownTimer.cancel();
					IncomingCallShareState.setName(null);
					iTelephony.answerRingingCall();
				} catch (Exception e) {
					answerRingingCall();
				}
			}
		};
		restoreRing();
		Tts.playText(IncomingCallReceiver.this.context,
				context.getString(R.string.incoming_call_auto_answer_for_you),
				listener, Tts.TTS_HIGH_PRIORITY);
	}

	@SuppressLint("HandlerLeak")
	protected void initHandler() {
		mPlayTTsHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_CAPUTRE_START_COUNT:
					countDownTimer = new CountDownTimer(6000, 5000) {
						@Override
						public void onTick(long millisUntilFinished) {
						}

						@Override
						public void onFinish() {
							if (mHandler != null) {
								Message msg = mHandler
										.obtainMessage(MsgConst.MSG_STOP_CAPTURE);
								mHandler.sendMessage(msg);
								/*
								 * msg = mHandler.obtainMessage(MSG_ERROR);
								 * msg.arg1 = 1; mHandler.sendMessage(msg);
								 */
							} else {
								restoreRing();
							}
						}
					};
					countDownTimer.start();
					break;
				case MSG_PLAY_TTS_NOT_SUPPORT_ANSWER_BY_MANUAL:
					countDownTimer = new CountDownTimer(3000, 1000) {
						@Override
						public void onTick(long millisUntilFinished) {

						}

						@Override
						public void onFinish() {
							if (IncomingCallShareState.getHandleCallState() == 0) {
								restoreRing();
								listener = new Tts.ITtsListener() {
									@Override
									public void onPlayEnd() {
										audioManager.setStreamVolume(
												AudioManager.STREAM_RING,
												volumeRingBak, 0);
									}
								};
								Tts.playText(
										IncomingCallReceiver.this.context,
										context.getString(R.string.incoming_call_not_support_answer_by_manual),
										listener, Tts.TTS_HIGH_PRIORITY);
							}
						}
					};
					countDownTimer.start();
					break;
				default:
					break;
				}
			}
		};
	}

	private void restoreRing() {

		Tts.stop(Tts.TTS_HIGH_PRIORITY);
		if (countDownTimer != null)
			countDownTimer.cancel();
		if (isNeedRestoreRing) {
			/*
			 * audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			 * //if(strRingerMode != -1)
			 * //audioManager.setRingerMode(strRingerMode);
			 */
			if (viberateRinger != -1)
				audioManager.setVibrateSetting(
						AudioManager.VIBRATE_TYPE_RINGER, viberateRinger);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					volumeRingBak, 0);
			audioManager.setStreamVolume(AudioManager.STREAM_RING, 2, 0);
			audioManager.setStreamMute(AudioManager.STREAM_RING, false);
			isNeedRestoreRing = false;
		}
	}

	private void replayRing() {
		/*
		 * Uri alert =
		 * RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		 * mMediaPlayer = new MediaPlayer(); try{
		 * if(IncomingCallShareState.getHandleCallState() == 0) {
		 * mMediaPlayer.setDataSource(context, alert);
		 * mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		 * mMediaPlayer.setLooping(true); mMediaPlayer.prepare();
		 * mMediaPlayer.start(); CountDownTimer cdt = new CountDownTimer(2000,
		 * 1500){
		 * 
		 * @Override public void onTick(long millisUntilFinished) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onFinish() { // TODO Auto-generated method stub
		 * if(mMediaPlayer != null) mMediaPlayer.stop(true); }
		 * 
		 * }; cdt.start(); } } catch (Exception e1) {
		 * 
		 * }
		 */

	}

	private void stopReplayRing() {
		/*
		 * if(mMediaPlayer != null) { mMediaPlayer.stop(true); mMediaPlayer =
		 * null; }
		 */
	}

	class CustomPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			}
		}
	}

	private void getPeople() {
		String nameTemp = ContactUtil.getContactName(this.context,phoneNumber);	
        if((nameTemp == null)||(nameTemp.equals("")))
        	nameTemp = IncomingCallShareState.UNKNOWN_NAME;
		name = nameTemp;
	}

	private ITelephony getITelephony() {
		ITelephony iTelephony = null;
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Service.TELEPHONY_SERVICE);
		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony",
					(Class[]) null);
			getITelephonyMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		try {
			iTelephony = (ITelephony) getITelephonyMethod.invoke(tm,
					(Object[]) null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iTelephony;
	}

	public static boolean isServiceRun(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> list = am
				.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : list) {
			if ((com.viash.voice_assistant.common.Config.WHICH_SERVER != null)
					&& com.viash.voice_assistant.common.Config.WHICH_SERVER
							.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER)) {
				if (info.service.getClassName().equals(
						"com.viash.voice_assistant.service.VoiceSdkService")) {
					return true;
				}
			} else {
				if (info.service
						.getClassName()
						.equals("com.viash.voice_assistant.service.VoiceAssistantService")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isMusicServiceRun(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> list = am
				.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : list) {
			if (info.service.getClassName().equals(
					"com.viash.voice_assistant.service.MusicService")) {
				return true;
			}
		}
		return false;
	}

	private void answerRingingCall() {
		try {
			Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
			buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
					KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
			context.sendOrderedBroadcast(buttonDown,
					"android.permission.CALL_PRIVILEGED");

			// froyo and beyond trigger on buttonUp instead of buttonDown
			Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
			buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
					KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
			context.sendOrderedBroadcast(buttonUp,
					"android.permission.CALL_PRIVILEGED");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mPlayTTsHandler != null) {
			Message msg1 = mPlayTTsHandler
					.obtainMessage(MSG_PLAY_TTS_NOT_SUPPORT_ANSWER_BY_MANUAL);
			mPlayTTsHandler.sendMessage(msg1);
		}
	}
}
