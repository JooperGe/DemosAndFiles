package com.viash.voice_assistant.alarm;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voicelib.utils.alarm.Alarm;
import com.viash.voicelib.utils.alarm.AlarmUtil;
public class AlarmAlertFullScreen extends Activity {
    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";
    
    private static Handler mHandle;
    protected Alarm mAlarm;
    private int mVolumeBehavior;
    private static final String TAG = "AlarmAlertFullScreen";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AlarmUtil.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(AlarmUtil.ALARM_DISMISS_ACTION)) {
                dismiss(false);
               // sendMsgToServer();
            }else if(action.equals(AlarmUtil.ALARM_DISMISS_ALARM_WARN)){
            	dismissAlarmWarn();
            }else {
                Alarm alarm = intent.getParcelableExtra(AlarmUtil.ALARM_INTENT_EXTRA);
                if (alarm != null && mAlarm.id == alarm.id) {
                    dismiss(true);
                }
            }
        }
    };
    
    public static void setHandle(Handler handler){
    	mHandle = handler;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAlarm = getIntent().getParcelableExtra(AlarmUtil.ALARM_INTENT_EXTRA);
        mAlarm = AlarmUtil.getAlarm(getContentResolver(), mAlarm.id);
        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString("volume_button_setting",
                        DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        updateLayout();
        IntentFilter filter = new IntentFilter(AlarmUtil.ALARM_KILLED);
        filter.addAction(AlarmUtil.ALARM_SNOOZE_ACTION);
        filter.addAction(AlarmUtil.ALARM_DISMISS_ACTION);
        filter.addAction(AlarmUtil.ALARM_DISMISS_ALARM_WARN);
        registerReceiver(mReceiver, filter);
        
    }

    private void sendMsgToServer(){
    	boolean isFA = AlarmUtil.isFirstAlarm(mAlarm.id,getApplicationContext());
    	if(isFA){
    		AlarmUtil.sendMsgToServer();
    	}
    }
    
/*    private boolean isFirstAlarm(){
    	
    	Alarm alarm = AlarmUtil.calculateFirstAlarm(getApplicationContext());
    	return mAlarm.id == alarm.id;
    	
    }
    */
    private void setTitle() {
        String label = mAlarm.getLabelOrDefault(this);
        TextView title = (TextView) findViewById(R.id.alertTitle);
        title.setText(label);
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflater.inflate(R.layout.alarm_alert, null));

        Button snooze = (Button) findViewById(R.id.snooze);
        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });

        /* dismiss button: close notification */
        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                       // sendMsgToServer();
                    }
                });
        setTitle();
    }
    private void snooze() {
    	
    	if (!findViewById(R.id.snooze).isEnabled()) {
    		dismiss(false);
    		return;
    	}
    	AlarmUtil.snooze(getApplicationContext(),mAlarm.id, 5,1);
    	finish();
    	
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void dismiss(boolean killed) {
    	AlarmUtil.stop(mAlarm.id, killed);
    	SavedData.setIsDismissAlarmByHand(true);
        finish();
        
    }

    private void dismissAlarmWarn(){
    	finish();
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mAlarm = intent.getParcelableExtra(AlarmUtil.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AlarmUtil.getAlarm(getContentResolver(), mAlarm.id) == null) {
            Button snooze = (Button) findViewById(R.id.snooze);
            snooze.setEnabled(false);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public void onBackPressed() {
        return;
    }
}
