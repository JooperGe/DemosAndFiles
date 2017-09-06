package com.viash.voice_assistant.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.SavedData;

public class OtherSettingsView extends LinearLayout {
	protected CheckBox mCbxDisplayFloatView;

	public OtherSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public OtherSettingsView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		mCbxDisplayFloatView = (CheckBox) findViewById(R.id.cbx_display_floatview);
		mCbxDisplayFloatView.setChecked(SavedData.getmDisplayFloatView());
		super.onFinishInflate();
	}

	public boolean getDisplayFloatView() {
		return mCbxDisplayFloatView.isChecked();
	}
}
