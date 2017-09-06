package com.viash.voice_assistant.activity;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.CustomSwitchButton;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.service.LockScreenService;
import com.viash.voicelib.utils.CustomToast;

public class LockScreenSettingActivity extends Activity  {
	private static final String TAG = "LockHomeAct";
	private CustomSwitchButton voiceLock_button;
	private View lockbuttonlayout;
	private View lockhome;
	private TextView lockhome_text;
	private TextView setting_lockhome;
	private TextView lockhome_tips;
  	private View closeSystemLock; 
  	private View closethree;
  	private TextView title_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		
		setContentView(R.layout.activity_lockset);
		voiceLock_button = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_lock);
		lockbuttonlayout = (View)this.findViewById(R.id.setting_layout_lock);
		lockhome = (View) this.findViewById(R.id.setting_layout_lockhome);
		lockhome_text=(TextView)this.findViewById(R.id.setting_textview_lockhome);
		setting_lockhome=(TextView)this.findViewById(R.id.setting_lockhome);
		lockhome_tips=(TextView)this.findViewById(R.id.setting_textview_basicsetting_lockhome);
		closeSystemLock = (View) this.findViewById(R.id.setting_layout_closelock);
		closethree = (View) this.findViewById(R.id.setting_layout_closethree);
		voiceLock_button.setChecked(!SavedData.isAllowLock());
		if(!SavedData.isAllowLock()){
			lockhome_text.setTextColor(Color.GRAY);
			setting_lockhome.setTextColor(Color.GRAY);
			lockhome_tips.setTextColor(Color.GRAY);
		}else{
		    lockhome_text.setTextColor(Color.rgb(255, 255, 255));
			setting_lockhome.setTextColor(Color.rgb(255, 255, 255));
			lockhome_tips.setTextColor(Color.rgb(255, 255, 255));
		}

		title_back = (TextView) this.findViewById(R.id.setting_title);

		initListener();
	}
	

	
	private void initListener() {
		lockhome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SavedData.isAllowLock()) {
					Intent intent = new Intent(LockScreenSettingActivity.this,
							LockHomeActivity.class);
					startActivity(intent);
				}
		}
	});
	
		closethree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				Intent intent = new Intent(LockScreenSettingActivity.this,
						ThirdHomeScreenActivity.class);
				startActivity(intent);
				}	
		});
		
		closeSystemLock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					Intent intent = new Intent();
					ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.ChooseLockGeneric");
					intent.setComponent(comp);
					intent.setAction("android.intent.action.VIEW");
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
					startActivity( intent );
				} catch (ActivityNotFoundException e) {
					CustomToast.makeToast(LockScreenSettingActivity.this, "您使用的Android为非标准系统，请从（系统设置->安全->屏幕保护）中关闭系统屏保。");//,Toast.LENGTH_LONG).show();					
				} catch (java.lang.SecurityException se) {
					CustomToast.makeToast(LockScreenSettingActivity.this, "您使用的Android为非标准系统，请从（系统设置->安全->屏幕保护）中关闭系统屏保。");//,Toast.LENGTH_LONG).show();
				}
			}
		});
		
		voiceLock_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//SavedData.setVoiceSetting(!isChecked);
				SavedData.setAllowLock(!SavedData.isAllowLock());
				if (SavedData.isAllowLock()) {
					startService(new Intent(LockScreenSettingActivity.this,
							LockScreenService.class));
					lockhome_text.setTextColor(Color.rgb(255, 255, 255));
					setting_lockhome.setTextColor(Color.rgb(255, 255, 255));
					lockhome_tips.setTextColor(Color.rgb(255, 255, 255));
				} else {
					// stopService(new
					// Intent(LockScreenSet.this,LockScreenService.class));
					lockhome_text.setTextColor(Color.GRAY);
					setting_lockhome.setTextColor(Color.GRAY);
					lockhome_tips.setTextColor(Color.GRAY);
				}
			}
		});

		lockbuttonlayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voiceLock_button.performClick();
			}
		});
		
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LockScreenSettingActivity.this.finish();				
			}
			
		});
	}
	
    private String getDefaultPackage(){
	   	Intent startMain = new Intent(Intent.ACTION_MAIN);
	   	startMain.addCategory(Intent.CATEGORY_HOME);
	   	PackageManager manager = getPackageManager();
	   	ActivityInfo aInfo = startMain.resolveActivityInfo(manager, PackageManager.GET_SHARED_LIBRARY_FILES);
	   	    	       	
	   	final List<ResolveInfo> apps = manager.queryIntentActivities(startMain, 0);
	   	Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
   	 
	   	if (apps != null) {        	
	           final int count = apps.size();
	           for (int i = 0; i < count; i++) {
	               ResolveInfo info = apps.get(i);
	               if (aInfo != null) {  
		               	if (aInfo.applicationInfo.packageName.equals(info.activityInfo.packageName) ) {
		               		return info.activityInfo.packageName;
		               	}                	
	               }
	               continue;
	           }
	   	}
	   	return null;
   }
    @Override
    public void onResume() {
    	super.onResume();
		if (getDefaultPackage() != null && getDefaultPackage().equals("com.viash.voice_assistant")) {
			lockhome_tips.setText(R.string.setting_basicsetting_lock);
			setting_lockhome.setText(R.string.setting_basicsetting_lock);
		}else {
			lockhome_tips.setText(R.string.setting_basicsetting_unlock);
			setting_lockhome.setText(R.string.setting_basicsetting_unlock);
		}
   }
}


