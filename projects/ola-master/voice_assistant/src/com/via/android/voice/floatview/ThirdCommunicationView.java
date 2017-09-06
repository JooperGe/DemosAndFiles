package com.via.android.voice.floatview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.adapter.CommunicationAdapter;
import com.viash.voice_assistant.sdk.VoiceSdkServiceInterface;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voice_assistant.widget.CommunicationView;
import com.viash.voicelib.data.CommunicationData;

public class ThirdCommunicationView extends CommunicationView {
	private static final boolean DEBUG = true;
	private static final String TAG = "ThirdCommunicationView";

	private Context mContext;
	private Handler mHandler;
	private CommunicationAdapter mAdapter;
	private ListView mLstView;
	private ImageView exit;
	
	public ThirdCommunicationView(Context context) {
		super(context);
		mContext = context;
		mHandler = new Handler();
		initComponents();
	}

	public ThirdCommunicationView(Context context, Handler handler) {
		super(context);
		mContext = context;
		mHandler = handler;
		initComponents();
	}

	private void initComponents() {
		mAdapter = new CommunicationAdapter(mContext);
		mAdapter.setmHandler(mHandler);
		mLstView = (ListView) super.findViewById(R.id.lst_communication_view);
		mLstView.setAdapter(mAdapter);
		mLstView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		exit = (ImageView) super.findViewById(R.id.exit_communication_view);
		
		exit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "onExit");
				closeCommunicationView();
			}
		});
	}

	public void setData(CommunicationData commData){
		mAdapter.addData(commData);
	}
	
	public void clearData() {
		mAdapter.clearData();
	}
	
	private void closeCommunicationView() {
		if (DEBUG)
			Log.i(TAG, "closeCommunicationView");

		// Close communication view
		startIntentWithAction(VoiceSdkServiceInterface.THIRDPARTY_CLOSE_COMMUNICATIONVIEW_ACTION);
	}
	
	protected void startIntentWithAction(String action){
		Intent intent = null;
		if(com.viash.voice_assistant.common.Config.WHICH_SERVER.equals(com.viash.voice_assistant.common.Config.HTTP_SERVER))
			intent = new Intent(mContext, VoiceSdkService.class);
		else
			intent = new Intent(mContext, VoiceAssistantService.class);
		intent.setAction(action);
		mContext.startService(intent);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		switch(event.getKeyCode())
		{
		case KeyEvent.KEYCODE_BACK:
			if(event.getAction() == KeyEvent.ACTION_DOWN)
			{
				setVisibility(View.INVISIBLE);
			}
			return true;

		}
		return super.dispatchKeyEvent(event);
	}
}
