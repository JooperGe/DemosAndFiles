package com.viash.voice_assistant.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.umeng.analytics.MobclickAgent;
import com.via.android.voice.floatview.FloatViewIdle;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.SecondStepDialog;
import com.viash.voice_assistant.data.AppData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voice_assistant.service.LockScreenService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.speech.SpeechRecognizer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.ScreenAndKeyguard;



/**
 * 引导界面
 * 
 * @author Harlan Song
 * @createDate 2013-3-22
 * @email:mark_dev@163.com
 */
public class GuideActivity extends Activity {
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ViewGroup viewPics;
	boolean isGride  = true;
	private long mMsgId = 0;
	private String mMsgTitle = null;
	private String mMsgUrl = null;
	private static final int MSG_UPDATE_HIGHLIGHT_IMAGEVIEW = 1;
    public static final int GUIDE_DISPLAY_DELAY = 1;
    public static final String DELAY = "delay";
    public static final String IS_BAIDU = "baiduyingyong";
    public static final String IS_START_FROM_BLUETOOTH = "isStartFromBluetooth";
    public static final String IS_NEED_UNLOCK = "isNeedUnlock";
    private boolean isBaiduyingyong = false;
    private boolean needAddCommondata = false;
    private boolean needStartCapture = false;
    private boolean isStartFromBluetooth = false;
    private boolean isNeedUnLock = false;
    private GuidePageChangeListener guidePageChangeListener;
    private RelativeLayout guide_go;
    public static boolean mIsLock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent != null)
		{
			mMsgId = intent.getLongExtra(MsgConst.SERVER_MSG_ID, 0);
			mMsgTitle = intent.getStringExtra(MsgConst.SERVER_MSG_TITLE);
			mMsgUrl = intent.getStringExtra(MsgConst.SERVER_MSG_URL);
			needAddCommondata = intent.getBooleanExtra(FloatViewIdle.START_FROM_FLOAT_VIEW, false);
			needStartCapture = intent.getBooleanExtra(SpeechRecognizer.START_FROM_OFFLINE_RECORD, false);
			String actionString = intent.getAction();
			if(actionString != null)
			  if(actionString.equals("android.intent.action.VOICE_COMMAND"))
			  {
				isStartFromBluetooth = true;
				//Log.e("ppp", "receive android.intent.action.VOICE_COMMAND");
				if(VoiceSdkService.mContext != null)
				{
					if(ScreenAndKeyguard.isScreenON(VoiceSdkService.mContext) == false)
						ScreenAndKeyguard.turnOnScreen(VoiceSdkService.mContext);
					if(ScreenAndKeyguard.isScreenLock(VoiceSdkService.mContext))
						ScreenAndKeyguard.unlockScreen(VoiceSdkService.mContext);
				}
				else
				{
					isNeedUnLock = true;
				}
			  }
		}
		
		MobclickAgent.onError(this);

		startService(new Intent(this,LockScreenService.class));
		SavedData.init(this);

		CustomToast.setToastAvailable(true);
		
		int currentAppVersion = ClientPropertyUtil.getVersionCode(GuideActivity.this);
		isBaiduyingyong = isMetaDataContainsValue("baiduyingyong");
		if(AppData.isAppVersionUpdated(GuideActivity.this, currentAppVersion) == true){
			SavedData.setSystemRestart(false);
			LayoutInflater inflater = getLayoutInflater();
			pageViews = new ArrayList<View>();
			ImageView viewPage1 = new ImageView(this);
			viewPage1.setImageResource(R.drawable.guide1);
			pageViews.add(viewPage1);
            			
			
			//ImageView viewPage2 = new ImageView(this);
			//viewPage2.setImageResource(R.drawable.guide2);
			guide_go = (RelativeLayout) LayoutInflater.from(GuideActivity.this).inflate(R.layout.guide_go, null);
			ImageView imgv_go = (ImageView) guide_go.findViewById(R.id.imgv_go);
			imgv_go.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					AppData.saveCurrentAppVersion(GuideActivity.this, ClientPropertyUtil.getVersionCode(GuideActivity.this));
					v.setVisibility(View.INVISIBLE);
					if(isBaiduyingyong)
						guide_go.setBackgroundResource(R.drawable.welcome_baidu);
					else
					    guide_go.setBackgroundResource(R.drawable.welcome);					
					startMainActivityDelay();
					finish();
				}		
			});
			pageViews.add(guide_go);
			
			View viewPage3 = inflater.inflate(R.layout.guide_page3, null);
			RelativeLayout layout_content = (RelativeLayout) viewPage3.findViewById(R.id.page3);			
			if(isBaiduyingyong)
			  layout_content.setBackgroundResource(R.drawable.welcome_baidu);
			pageViews.add(viewPage3);
	
			//View lastView = inflater.inflate(R.layout.guide_page2, null);
			//pageViews.add(lastView);
			//RelativeLayout page2 = (RelativeLayout)lastView.findViewById(R.id.page2);
			/*viewPage1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(isGride){
						AppData.saveCurrentAppVersion(GuideActivity.this, ClientPropertyUtil.getVersionCode(GuideActivity.this));
						startMainActivity();
					}
					finish();
				}
			});*/
			
			viewPics = (ViewGroup) inflater.inflate(R.layout.guide, null);
			viewPager = (ViewPager) viewPics.findViewById(R.id.guidePages);
			setContentView(viewPics);
			viewPager.setAdapter(new GuidePageAdapter());
			guidePageChangeListener = new GuidePageChangeListener();
			viewPager.setOnPageChangeListener(guidePageChangeListener);
			//add for yunzhisheng voice wake up
			Intent serviceSdk = new Intent();
			serviceSdk.setClass(GuideActivity.this,  VoiceSdkService.class);
			startService(serviceSdk);
		}else{
			Log.i("LockScreenReceiver", "" + LockScreenService.presshome);
			if (SavedData.isSystem_restart() && UserData.isLockHomekeyEnable(this)) {
				Log.i("LockScreenReceiver", "ACTION_BOOT_COMPLETED GUIDE" );
				SavedData.setSystemRestart(false);
				Intent intent2 = new Intent();
				intent2.setComponent(getDefaultComponentName());
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(intent2);
			}else if (LockScreenService.presshome == 1 && UserData.isLockHomekeyEnable(this)) {
				Log.i("HOME","GuideActivity LockActivity.mbActive = "+LockActivity.mbActive);
				if (!(LockActivity.mbActive)) {
					Intent intent2 = new Intent();
					//ComponentName comp = new ComponentName(LockScreenService.packagenameString, LockScreenService.ClassnameString);
					intent2.setComponent(getDefaultComponentName());
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					startActivity(intent2);
					finish();
				}else {
					Intent lockIntent = new Intent(this,LockActivity.class);
					lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					lockIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					startActivity(lockIntent);
					mIsLock = true;					
				}
			}else if (LockScreenService.presshome == 2 && UserData.isLockHomekeyEnable(this) && !UserData.doesContainHistoryApps(this)){
				if (!(LockActivity.mbActive)) {
					Intent intent2 = new Intent();
					//ComponentName comp = new ComponentName(LockScreenService.packagenameString, LockScreenService.ClassnameString);
					intent2.setComponent(getDefaultComponentName());
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					startActivity(intent2);
				}
			}
			else {
				if ( !SecondStepDialog.mbActive ){
					startMainActivity(); 
					LockScreenService.presshome = 0;
				}
				SecondStepDialog.mbActive = false;
			}
			//LockScreenService.presshome = 0;
			Log.i("HOME","GuideActivity LockScreenService.presshome = "+LockScreenService.presshome);
			finish();
		}
	}
	
	private void startMainActivity()
	{
		Intent intent = new Intent(GuideActivity.this,NewAssistActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(MsgConst.SERVER_MSG_ID, mMsgId);
		intent.putExtra(MsgConst.SERVER_MSG_TITLE, mMsgTitle);
		intent.putExtra(MsgConst.SERVER_MSG_URL, mMsgUrl);
		intent.putExtra(DELAY, 0);
		intent.putExtra(IS_BAIDU, isBaiduyingyong);
		intent.putExtra(FloatViewIdle.START_FROM_FLOAT_VIEW, needAddCommondata);
		intent.putExtra(SpeechRecognizer.START_FROM_OFFLINE_RECORD, needStartCapture);
		intent.putExtra(IS_START_FROM_BLUETOOTH, isStartFromBluetooth);
		intent.putExtra(IS_NEED_UNLOCK, isNeedUnLock);
		//intent.clearTaskOnLaunch();
		startActivity(intent);
	}
	
	private void startMainActivityDelay()
	{
		try{
			//Thread.sleep(500);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		Intent intent = new Intent(GuideActivity.this,NewAssistActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(MsgConst.SERVER_MSG_ID, mMsgId);
		intent.putExtra(MsgConst.SERVER_MSG_TITLE, mMsgTitle);
		intent.putExtra(MsgConst.SERVER_MSG_URL, mMsgUrl);
		intent.putExtra(DELAY, GUIDE_DISPLAY_DELAY);
		intent.putExtra(IS_BAIDU, isBaiduyingyong);
		intent.putExtra(FloatViewIdle.START_FROM_FLOAT_VIEW, needAddCommondata);
		//intent.clearTaskOnLaunch();
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}



	class GuidePageAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View v, int position, Object arg2) {
			((ViewPager) v).removeView(pageViews.get(position));

		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public Object instantiateItem(View v, int position) {
			((ViewPager) v).addView(pageViews.get(position));
			return pageViews.get(position);
		}

		@Override
		public boolean isViewFromObject(View v, Object arg1) {
			return v == arg1;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	class GuidePageChangeListener implements OnPageChangeListener {
		
		private int mCurrentpage = 0;

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
			if (mCurrentpage == (pageViews.size()-1) && arg2 == 0) {
				if(isGride){
					AppData.saveCurrentAppVersion(GuideActivity.this, ClientPropertyUtil.getVersionCode(GuideActivity.this));
					//startMainActivity();
					startMainActivityDelay();
				}
				finish();
			}
			
			
		}

		@Override
		public void onPageSelected(int position) {
			mCurrentpage = position;
			if (position == (pageViews.size()-1) ){
				/**/
			}
		}

	}
	
	private ComponentName getDefaultComponentName(){
		PackageManager manager = getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        ComponentName comp = null;
        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));        
        if (apps != null) {		        	
            final int count = apps.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                try {
					Field system = ResolveInfo.class.getDeclaredField("system");
					system.setAccessible(true);
					Object value = system.get(info);					
					if("true".equals(value.toString()))
					{
						Log.i("HOME","GuideActivity info.activityInfo.packageName = "+info.activityInfo.packageName);
						comp = new ComponentName(info.activityInfo.packageName,info.activityInfo.name);
	                	break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                /*Parcel  dest =  Parcel.obtain();
                info.writeToParcel(dest, 0);
                dest.setDataPosition( dest.dataSize()- 4);
                int first = dest.readInt();
     
                if (first == 1) {
                	comp = new ComponentName(info.activityInfo.packageName,info.activityInfo.name);
                	break;
                }*/
            }
        }
        return comp;
	} 
	
	private  boolean isMetaDataContainsValue(String name) {
        PackageManager packageManager = this.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(this
                    .getPackageName(), 128);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                Bundle bundle = applicationInfo.metaData;
                String str = bundle.getString("UMENG_CHANNEL");
                if(str.equals(name))
                {
               	return true; 
                }                 
            }
        } catch (Exception e) {
           e.printStackTrace();            
        }
        return false;
    }	
}
