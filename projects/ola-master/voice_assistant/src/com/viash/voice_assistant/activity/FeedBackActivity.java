package com.viash.voice_assistant.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Messenger;
import android.widget.LinearLayout;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.FeedBackView;

public class FeedBackActivity extends Activity {
	protected Messenger mVoiceAssistantServiceMessenger = null;
	protected static final int UPDATE_EDITTEXT_CONTENT = 1;
	public static FeedBackActivity instance = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.layout_feedback);
		FeedBackView view = new FeedBackView(FeedBackActivity.this,FeedBackActivity.this);
		LinearLayout layout_content = (LinearLayout) findViewById(R.id.layout_content);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
		layout_content.addView(view,params);
		instance = FeedBackActivity.this;
	}	
}
