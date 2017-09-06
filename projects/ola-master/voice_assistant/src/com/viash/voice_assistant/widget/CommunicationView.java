package com.viash.voice_assistant.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.viash.voice_assistant.R;

public class CommunicationView extends RelativeLayout {
	protected static final String TAG = "CommunicationView";

	public CommunicationView(Context context) {
		super(context);
		LayoutInflater.from(getContext()).inflate(
				R.layout.layout_communication, this, true);
		initControls();
	}

	public void initControls() {

	}

}
