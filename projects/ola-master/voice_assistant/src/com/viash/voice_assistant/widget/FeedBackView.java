package com.viash.voice_assistant.widget;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.FeedBackActivity;
import com.viash.voice_assistant.component.SendDataIndicationDialog;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voicelib.msg.MsgConst;

public class FeedBackView extends LinearLayout{

	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected VoiceAssistantServiceConnection mVoiceAssistantServiceConnection = null;
	private static EditText editText_content;
	private static EditText editText_contact;
	private TextView tv_input_num;
	private TextView setting_title;
	private static Button   button_send;
	private int screenHeight;
	private Context mContext;
	private View view;
	public static FeedBackActivity instance = null;
	public FeedBackView(Context context,FeedBackActivity feedBackActivity) {
		super(context);
		mContext = context;
		instance = feedBackActivity;
		initVoiceAssistantService();
		initView();
	}
   
	public FeedBackView(Context context,AttributeSet attrs) {
		super(context,attrs);
		mContext = context;
	}
	
	private void initView() 
	{
		DisplayMetrics dm = new DisplayMetrics();
		instance.getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight = dm.heightPixels;
		if(screenHeight <= 800)
		{
			view = LayoutInflater.from(mContext).inflate(R.layout.layout_feedback_small_screen_item, this);
		}
		else
		{
		   view = LayoutInflater.from(mContext).inflate(R.layout.layout_feedback_item, this);
		}
		setting_title = (TextView) view.findViewById(R.id.setting_title);
		editText_content = (EditText) view.findViewById(R.id.editText_content);
		editText_contact = (EditText) view.findViewById(R.id.editText_contact);
		tv_input_num = (TextView) view.findViewById(R.id.tv_input_num);
		button_send = (Button) view.findViewById(R.id.button_send);
				    
	    setting_title.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				instance.finish();
			}	    	
	    });
	    	    
		editText_content.setFocusable(true);

		editText_content.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					editText_content.requestFocus();
					editText_content.setEnabled(true);					
					int length = editText_content.getText().toString().length();
					tv_input_num.setText(length+"/500");
				}
				return false;
			}
			
		});
		editText_content.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = editText_content.getText().toString().length();
				tv_input_num.setText(length+"/500");
			}	
		});
		
		button_send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				button_send.setClickable(false);
				String content = editText_content.getText().toString();
				String contact = editText_contact.getText().toString();
				JSONObject jsonObj = new JSONObject();
				if(content != null)
				{
					if(!content.equals(""))
					{
						try {
							jsonObj.put("type", "feedback");
							jsonObj.put("content", content);
							if(contact != null)
								jsonObj.put("contact", contact);
							if(mVoiceAssistantServiceMessenger != null)
							{
								Message msg = Message.obtain(null, MsgConst.CLIENT_ACTION_SEND_FEEDBACK, jsonObj);
								mVoiceAssistantServiceMessenger.send(msg);
							}
						} catch (Exception e) {						
							e.printStackTrace();
						}
					}
				}
			}
			
		});
	}
	
	protected void initVoiceAssistantService() {
		mVoiceAssistantServiceConnection = new VoiceAssistantServiceConnection();
		VoiceSdkServiceInterface.bindToVoiceService(mContext, mVoiceAssistantServiceConnection);
	}
	
	class VoiceAssistantServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (mVoiceAssistantServiceMessenger == null){
				mVoiceAssistantServiceMessenger = new Messenger(service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mVoiceAssistantServiceMessenger = null;
		}
	}
		
	@Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) 
	{
		super.onLayout(changed, l, t, r, b); 
	}
	
	public static void showDialog(String content)
	{
		if(content.equals(SendDataIndicationDialog.SEND_SUCCESS)){
			editText_content.setText("");
			editText_contact.setText("");
		}else if(content.equals(SendDataIndicationDialog.SEND_FAILED)){
			button_send.setClickable(true);
		}
		
		SendDataIndicationDialog sendDataIndicationDialog = new SendDataIndicationDialog(instance,content,null);
		sendDataIndicationDialog.showDialog();
	}
}
