package com.viash.voice_assistant.sdk;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.via.android.voice.floatview.ThirdCommunicationView;
import com.via.android.voice.floatview.ThirdVoiceButtonView;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.Config;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;

public class VoiceSdkUI {
	private static final String TAG = "VoiceSdkUI";
	private Context mContext;
	protected ThirdVoiceButtonView mVoiceBtn = null;
	protected ThirdCommunicationView mCommView = null;
	protected SdkUiHandler mHandler;
	
	
	public VoiceSdkUI(Context context)
	{
		mContext = context;
	}
	
	public void init()
	{
		mHandler = new SdkUiHandler();		
		mVoiceBtn = new ThirdVoiceButtonView(mContext);
		mCommView = new ThirdCommunicationView(mContext, mHandler);
		mVoiceBtn.setVisibility(View.INVISIBLE);
		mCommView.setVisibility(View.INVISIBLE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		Display display = wm.getDefaultDisplay();
    	int width = display.getWidth();
    	int height = display.getHeight();
    	
		WindowManager.LayoutParams voiceLP = new WindowManager.LayoutParams();
		voiceLP.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		voiceLP.type = LayoutParams.TYPE_PHONE;
		voiceLP.format = PixelFormat.RGBA_8888;
		//voiceLP.gravity = Gravity.CENTER_HORIZONTAL;
		voiceLP.y = height - mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_voice_btn_size) - 150;    
		voiceLP.width = LayoutParams.WRAP_CONTENT;
		voiceLP.height = LayoutParams.WRAP_CONTENT;
		wm.addView(mVoiceBtn, voiceLP);    	
		Log.d(TAG, "VoiceButton: [" + voiceLP.x + "," + voiceLP.y + "] (" + voiceLP.width + "x" + voiceLP.height + ")");
		
		WindowManager.LayoutParams commViewLP = new WindowManager.LayoutParams();
		
    	commViewLP.type = LayoutParams.TYPE_PHONE;
    	commViewLP.format = PixelFormat.RGBA_8888;
    	commViewLP.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
    	//commViewLP.gravity = Gravity.CENTER_HORIZONTAL;   
    	commViewLP.y = height / 3 - mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_voice_btn_size) - 180;    	
    	commViewLP.width = width - 100;
    	commViewLP.height = height / 2;
    	wm.addView(mCommView, commViewLP);
    	
		Log.d(TAG, "CommunicationView: [" + commViewLP.x + "," + commViewLP.y + "] (" + commViewLP.width + "x" + commViewLP.height + ")");
	}
	
	public void destroy()
	{
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		if(mCommView != null)
		{
			wm.removeView(mCommView);
			mCommView = null;
		}
		
		if(mVoiceBtn != null)
		{
			wm.removeView(mVoiceBtn);
			mVoiceBtn = null;
		}
	}
	
	public void setProcessState(int state)
	{
		mVoiceBtn.setProcessState(state);
	}

	public void updateVolume(int volume)
	{		
		mVoiceBtn.speak(volume);
	}
	
	public boolean isVoiceViewVisible()
	{
		return (mVoiceBtn.getVisibility() == View.VISIBLE);
	}
	
	public void newVoiceData(CommunicationData data)
	{
		mCommView.clearData();
		mCommView.setData(data);
		mCommView.setVisibility(View.VISIBLE);
	}
	
	public void newServerData(CommunicationData data)
	{
		mCommView.setData(data);
		mCommView.setVisibility(View.VISIBLE);
	}
	
	public void showVoiceView(int state)
	{
		mVoiceBtn.setVisibility(View.VISIBLE);
		mVoiceBtn.setProcessState(state);
	}

	public void hide()
	{
		mVoiceBtn.setVisibility(View.INVISIBLE);
		mCommView.setVisibility(View.INVISIBLE);
	}
	
	public void sendMessage(Message msg)
	{
		mHandler.sendMessage(msg);
	}
	
	public boolean isMsgSupport(Message msg)
	{
		boolean ret = false;
		int[] msgId = new int[]{
			MsgConst.MSG_SHOW_INTERNAL_WEB,
			MsgConst.MSG_SHOW_WEB,
			MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME,
			MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE,
			MsgConst.MSG_JUMP_TO_NEW_APP,
			MsgConst.MSG_CALL_START
		};
		
		for(int i = 0; i < msgId.length; i++)
		{
			if(msg.what == msgId[i])
			{
				ret = true;
				break;
			}
		}
		
		return ret;
	}
	
	public void hideUI(boolean force)
	{
		mCommView.setVisibility(View.INVISIBLE);
		if(!Config.ShouldAlwaysShowTopVoiceBtn() || force)
		{
			mVoiceBtn.setVisibility(View.INVISIBLE);
		}
	}
	
	class SdkUiHandler extends Handler
	{

		@Override
		public void handleMessage(Message msg) {
			boolean leaveSdkUi = false;
			switch(msg.what)
			{
			case MsgConst.MSG_SHOW_INTERNAL_WEB:
			case MsgConst.MSG_SHOW_WEB:
			{
				Intent intent = new Intent();
				Uri uri = Uri.parse((String) msg.obj);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(uri);
				try {
					mContext.startActivity(intent);
					leaveSdkUi = true;
				} catch (ActivityNotFoundException e) {
					CustomToast.makeToast(mContext, mContext.getResources().getString(R.string.newassistactivity_can_not_open) +	"\"" + uri.getPath() + "\"");//,
							//Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			break;
			}
			case MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME:
				int volume = msg.getData().getInt("volume");
				updateVolume(volume);
				break;
			case MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE:
				int state = msg.getData().getInt("state");
				setProcessState(state);
				break;
			case MsgConst.MSG_JUMP_TO_NEW_APP:
				leaveSdkUi = true;
				break;
			case MsgConst.MSG_CALL_START:
			{
				String phoneNum = (String) msg.obj;
				if(phoneNum != null)
				{
					Uri uri = Uri.parse("tel:" + phoneNum);
					Intent intent = new Intent(Intent.ACTION_CALL, uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	
					if (intent != null) {
						try {
							mContext.startActivity(intent);
							leaveSdkUi = true;
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			}
			}
		
			if(leaveSdkUi)
				hideUI(false);
		}
		
	}
}
