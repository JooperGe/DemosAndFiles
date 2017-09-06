package com.viash.voice_assistant.alarm;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.alarm.Alarm;
import com.viash.voicelib.utils.alarm.AlarmUtil;
public class AlarmReceiver extends BroadcastReceiver {
	private final static int STALE_WINDOW = 30 * 60 * 1000;
	private static final String TAG = "AlarmReceiver";
	Alarm alarm = null;
	private static Handler mHandler;
	protected byte[] mRemindStart ;

	public AlarmReceiver() {
		// TODO Auto-generated constructor stub
		super();
	}

	public AlarmReceiver(Handler handler) {
		// TODO Auto-generated constructor stub
		super();
		AlarmReceiver.mHandler = handler; 
	}
	public static void setHandler(Handler handler){
		AlarmReceiver.mHandler = handler;
	}

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("AlarmReceiver",intent.getAction());
		if (AlarmUtil.ALARM_KILLED.equals(intent.getAction())) {
			updateNotification(context,
					(Alarm) intent
					.getParcelableExtra(AlarmUtil.ALARM_INTENT_EXTRA),
					intent.getIntExtra(AlarmUtil.ALARM_KILLED_TIMEOUT, -1));
			return;
		} else if (AlarmUtil.CANCEL_SNOOZE.equals(intent.getAction())) {
			AlarmUtil.saveSnoozeAlert(context, -1, -1);
			return;
		} else if (!AlarmUtil.ALARM_ALERT_ACTION.equals(intent.getAction())) {
			return;
		}


		final byte[] data = intent.getByteArrayExtra(AlarmUtil.ALARM_RAW_DATA);
		if (data != null) {
			Parcel in = Parcel.obtain();
			in.unmarshall(data, 0, data.length);
			in.setDataPosition(0);
			alarm = Alarm.CREATOR.createFromParcel(in);
		}

		if (alarm == null) {
			AlarmUtil.setNextAlert(context);
			return;
		}
		//判断当前闹钟是否是之前被推迟的闹钟，若是的话，则将被保存的推迟闹钟信息给清除。
		AlarmUtil.disableSnoozeAlert(context, alarm.id);

		if (!alarm.daysOfWeek.isRepeatSet()) {
			//闹钟开始后，会更新闹钟的状态（db）
			AlarmUtil.enableAlarm(context, alarm.id, false);
		} else {
			AlarmUtil.setNextAlert(context);
		}
		long now = System.currentTimeMillis();
		if (now > alarm.time + STALE_WINDOW) {
			Log.v("Spencer", "Ignoring stale alarm");
			return;
		}
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.sendBroadcast(closeDialogs);
		Class c = AlarmAlert.class;
		KeyguardManager km = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		if (km.inKeyguardRestrictedInputMode()) {
			c = AlarmAlertFullScreen.class;
		}
		int tab = alarm.tab;
		Log.i(TAG, "-------->>开始选择");
		//闹钟触发的时候
		if(tab == 1){
			Log.i(TAG, "-------->>选择闹钟");
			AlarmAlertFullScreen.setHandle(mHandler);
			AlarmKlaxon.setHandle(mHandler);
			Intent playAlarm = new Intent(AlarmUtil.ALARM_ALERT_ACTION);
			playAlarm.putExtra(AlarmUtil.ALARM_INTENT_EXTRA, alarm);
			context.startService(playAlarm);

			Intent notify = new Intent(context, AlarmAlert.class);
			notify.putExtra(AlarmUtil.ALARM_INTENT_EXTRA, alarm);
			notify.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//context.startActivity(notify);
			PendingIntent pendingNotify = PendingIntent.getActivity(context,
					alarm.id, notify, 0);
			String label = alarm.getLabelOrDefault(context);
			Notification n = new Notification(R.drawable.stat_notify_alarm, label,
					alarm.time);
			n.setLatestEventInfo(context, label,
					context.getString(R.string.alarm_notify_text), pendingNotify);
			n.flags |= Notification.FLAG_SHOW_LIGHTS
					| Notification.FLAG_ONGOING_EVENT;
			n.defaults |= Notification.DEFAULT_LIGHTS;
			Intent alarmAlert = new Intent(context, c);
			alarmAlert.putExtra(AlarmUtil.ALARM_INTENT_EXTRA, alarm);
			alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			n.fullScreenIntent = PendingIntent.getActivity(context, alarm.id,
					alarmAlert, 0);
			NotificationManager nm = getNotificationManager(context);
			nm.notify(alarm.id, n);
		}else if(tab == 2){
			//提醒触发的时候
			Log.i(TAG, "-------->>选择提醒");
			AssetManager manager = context.getAssets();
			try {
				AssetFileDescriptor fd = manager.openFd("remind.wav");
				if(fd != null){
					int len = (int)fd.getLength();
					FileInputStream stream = fd.createInputStream();
					stream.skip(44);
					mRemindStart = new byte[len - 44];
					stream.read(mRemindStart);
					stream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	AECSystem.getInstance().addUrgentSound(mRemindStart);
			//BeepPlayer.play(context, "189.wav", false);
			//ThreadUtil.sleep(2000);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(alarm.time);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minutes = calendar.get(Calendar.MINUTE);
			String speech = "主人，现在"+hour+"点"+minutes+"分了，是不是该"+alarm.label+"了。";
			//Tts.playText(context, speech, new MyTtsListener());
			if(mHandler != null)
			{
				mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_START_TTS, speech));
			}
		}
	}

	private NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void updateNotification(Context context, Alarm alarm, int timeout) {
		NotificationManager nm = getNotificationManager(context);
		if (alarm == null) {
			if (true) {
			}
			return;
		}
	}

	public class MyTtsListener implements Tts.ITtsListener{
		@Override
		public void onPlayEnd() {
			// TODO Auto-generated method stub
			/*	JSONObject object = new JSONObject();
			try {
				object.put("id",alarm.id);
				object.put("content",alarm.label);
				object.put("hour",alarm.hour);
				object.put("minutes",alarm.minutes);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			/*Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMIND_WARNING);
			//msg.obj= object.toString();
			msg.arg1 = 2 ;
			mHandler.sendMessage(msg);*/
		}
	}
}
