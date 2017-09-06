package com.viash.voice_assistant.component;

import com.viash.voice_assistant.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SecondStepDialog extends Dialog {
	public static boolean mbActive = false;
    Context context;
    public SecondStepDialog(Context context) {
        super(context);
        mbActive = false;
        this.context = context;
    }
    public SecondStepDialog(Context context, int theme){
        super(context, theme);
        mbActive = false;
        this.context = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.steptwodialog);
        mbActive = false;
        Button dialog_buttonoperate=(Button)findViewById(R.id.dialog_buttonoperate);
        dialog_buttonoperate.setTextColor(Color.BLACK);
        dialog_buttonoperate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startMain = new Intent(Intent.ACTION_MAIN);
	        	startMain.addCategory(Intent.CATEGORY_HOME);
	        	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	context.startActivity(startMain);
	        	mbActive = true;
	        	cancel();
			}
		});
    }

}
