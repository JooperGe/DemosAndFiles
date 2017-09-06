package com.viash.voice_assistant.component;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;

public class PopupDialog extends AlertDialog {

	public PopupDialog(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		dismiss();//hide();//
		return super.onTouchEvent(event);
	}

}
