package com.viash.voice_assistant.alarm;
import java.io.FileInputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.util.CommunicationUtil;
import com.viash.voicelib.msg.MsgAsk;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.alarm.Alarm;
import com.viash.voicelib.utils.alarm.AlarmUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.media.AudioManager.OnAudioFocusChangeListener;

@TargetApi(Build.VERSION_CODES.FROYO)
@SuppressLint("NewApi")
public class AlarmKlaxon extends Service {

	private static final int ALARM_TIMEOUT_SECONDS = 10 * 60;
	private static final long[] sVibratePattern = new long[] { 500, 500 };
	private boolean mPlaying = false;
	private Vibrator mVibrator;
	private MediaPlayer mMediaPlayer;
	private Alarm mCurrentAlarm;
	private long mStartTime;
	private TelephonyManager mTelephonyManager;
	private int mInitialCallState;
	private AudioManager mAudioManager = null;
	private boolean mCurrentStates = true;

	private static Handler handler;
	private static final int KILLER = 1;
	private static final int FOCUSCHANGE = 2;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case KILLER:	
				sendKillBroadcast((Alarm) msg.obj);
				stopSelf();
				break;
			case FOCUSCHANGE:
				switch (msg.arg1) {
				case AudioManager.AUDIOFOCUS_LOSS:

					if (!mPlaying && mMediaPlayer != null) {
						stop();
					}
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

					if (!mPlaying && mMediaPlayer != null) {
						mMediaPlayer.pause();
						mCurrentStates = false;
					}
					break;
				case AudioManager.AUDIOFOCUS_GAIN:

					if (mPlaying && !mCurrentStates) {
						play(mCurrentAlarm);
					}
					break;
				default:

					break;
				}
			default:
				break;

			}
		}
	};

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			if (state != TelephonyManager.CALL_STATE_IDLE
					&& state != mInitialCallState) {
				sendKillBroadcast(mCurrentAlarm);
				stopSelf();
			}
		}
	};

	public static void setHandle(Handler handlers){
		handler = handlers;
	}
	@Override
	public void onCreate() {
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
		AssetManager manager = getApplicationContext().getAssets();
		try {	
			AssetFileDescriptor fd = manager.openFd("fallbackring.wav");
			if(fd != null && mRingtong == null)
			{
				int len = (int) fd.getLength();
				FileInputStream is = fd.createInputStream();
				is.skip(44);
				mRingtong = new byte[len - 44];
				is.read(mRingtong);	
				is.close();
			}
		}catch(Exception e) {
			
		}
	}

	@Override
	public void onDestroy() {
		stop();
		mTelephonyManager.listen(mPhoneStateListener, 0);
		AlarmAlertWakeLock.releaseCpuLock();
		mAudioManager.abandonAudioFocus(mAudioFocusListener);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		final Alarm alarm = intent
				.getParcelableExtra(AlarmUtil.ALARM_INTENT_EXTRA);

		if (alarm == null) {
			Log.v("wang",
					"AlarmKlaxon failed to parse the alarm from the intent");
			stopSelf();
			return START_NOT_STICKY;
		}

		if (mCurrentAlarm != null) {
			sendKillBroadcast(mCurrentAlarm);
		}
		
		JSONObject object = new JSONObject();
		try {
			object.put("id",alarm.id);
			object.put("content",alarm.label);
			object.put("hour",alarm.hour);
			object.put("minutes",alarm.minutes);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*Message msg = handler.obtainMessage(MsgConst.CLIENT_ACTION_ALARM_WARNING);
		msg.obj= object.toString();
		msg.arg1 = 1 ;
		handler.sendMessage(msg);*/
		play(alarm);
		mCurrentAlarm = alarm;
		mInitialCallState = mTelephonyManager.getCallState();

		return START_STICKY;
	}

	private void sendKillBroadcast(Alarm alarm) {
		long millis = System.currentTimeMillis() - mStartTime;
		int minutes = (int) Math.round(millis / 60000.0);
		Intent alarmKilled = new Intent(AlarmUtil.ALARM_KILLED);
		alarmKilled.putExtra(AlarmUtil.ALARM_INTENT_EXTRA, alarm);
		alarmKilled.putExtra(AlarmUtil.ALARM_KILLED_TIMEOUT, minutes);
		sendBroadcast(alarmKilled);
	}

	private static final float IN_CALL_VOLUME = 0.125f;

	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			mHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
		}
	};

	protected byte[] mRingtong;
	private void play(Alarm alarm) {
		mAudioManager.requestAudioFocus(mAudioFocusListener,
				AudioManager.STREAM_ALARM,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		stop();
		mPlaying = true;
		/*new Thread() {
			public void run() {
				while (mPlaying) {
					AECSystem.getInstance().addUrgentSound(mRingtong);
					try {
						Thread.sleep(1900);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}.start();*/
		

		mMediaPlayer = new MediaPlayer();
		Uri  alert = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.v("wangxianming", "Error occurred while playing audio.");
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				return true;
			}
		});

		try {
			if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
				Log.v("wangxianming", "Using the in-call alarm");
				mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
				setDataSourceFromResource(getResources(), mMediaPlayer,
						R.raw.fallbackring);
			} else {
				mMediaPlayer.setDataSource(this, alert);
			}
			startAlarm(mMediaPlayer);
		} catch (Exception ex) {
			Log.v("wangxianming", "Using the fallback ringtone");
			try {
				mMediaPlayer.reset();
				setDataSourceFromResource(getResources(), mMediaPlayer,
						R.raw.fallbackring);
				startAlarm(mMediaPlayer);
			} catch (Exception ex2) {
				Log.v("wangxianming", "Failed to play fallback ringtone"
						+ ex2);
			}
		}


		if (alarm.vibrate) {

			//mVibrator.vibrate(sVibratePattern, 0);
		} else {
			mVibrator.cancel();
		}

		enableKiller(alarm);
		
		mStartTime = System.currentTimeMillis();
	}

	private void startAlarm(MediaPlayer player) throws java.io.IOException,
	IllegalArgumentException, IllegalStateException {
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
		}
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}

	public void stop() {
		Log.v("wang", "AlarmKlaxon.stop()");
		if (mPlaying) {
			mPlaying = false;

			Intent alarmDone = new Intent(AlarmUtil.ALARM_DONE_ACTION);
			sendBroadcast(alarmDone);
			//AECSystem.getInstance().clearUrgentSound();
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

			//mVibrator.cancel();
		}
		disableKiller();
	}

	private void enableKiller(Alarm alarm) {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
				1000 * ALARM_TIMEOUT_SECONDS);
	}

	private void disableKiller() {
		mHandler.removeMessages(KILLER);
	}

}
