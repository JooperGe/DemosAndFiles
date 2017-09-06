package com.viash.voice_assistant.activity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.FirstStepDialog;
import com.viash.voice_assistant.component.SecondStepDialog;
import com.viash.voice_assistant.data.UserData;

public class LockHomeActivity extends Activity  implements OnClickListener{
	//private static final String TAG = "LockHomeAct";
	private TextView lockhome_one;
	private TextView lockhome_two;
	private RelativeLayout  setting_layout_lockhome_oneStep;
	private RelativeLayout  setting_layout_lockhome_twoStep;
    private String msCurrentPackageName;
    private boolean firstStepClicked = false;
	private TextView title_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		msCurrentPackageName = UserData.getDefaultPackage(this);
		
		setContentView(R.layout.activity_lockhome);	
		lockhome_one = (TextView)this.findViewById(R.id.setting_textview_lockhome_oneStep);
		lockhome_two = (TextView)this.findViewById(R.id.setting_textview_lockhome_twoStep);
		
		setting_layout_lockhome_oneStep=(RelativeLayout)findViewById(R.id.setting_layout_lockhome_oneStep);
		setting_layout_lockhome_oneStep.setOnClickListener(this);
		setting_layout_lockhome_twoStep=(RelativeLayout)findViewById(R.id.setting_layout_lockhome_twoStep);
		setting_layout_lockhome_twoStep.setOnClickListener(this);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		title_back.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.setting_layout_lockhome_oneStep:
			Dialog firstdialog = new FirstStepDialog(LockHomeActivity.this, R.style.MyDialog, msCurrentPackageName);
			firstdialog.show();
			firstStepClicked = true;
			break;
        case R.id.setting_layout_lockhome_twoStep:
        	Dialog seconddialog = new SecondStepDialog(LockHomeActivity.this, R.style.MyDialog);
        	seconddialog.show();
			break;
        case R.id.setting_title:
			this.finish();
			break;
		default:
			break;
		}
		
	}
	
    @Override
    public void onResume(){
    	super.onResume();
    	msCurrentPackageName  = UserData.getDefaultPackage(this);
		if (msCurrentPackageName != null ){
			if (msCurrentPackageName.equals("com.viash.voice_assistant")) {
				setting_layout_lockhome_oneStep.setClickable(false);
				setting_layout_lockhome_twoStep.setClickable(false);
				lockhome_one.setTextColor(Color.GRAY);
				lockhome_two.setTextColor(Color.GRAY);

			} else {
				setting_layout_lockhome_oneStep.setClickable(true);
				setting_layout_lockhome_twoStep.setClickable(false);
				lockhome_one.setTextColor(Color.WHITE);
				lockhome_two.setTextColor(Color.GRAY);
			}
		}
		else {
			//the first step should be enable, and the second step should be disable;
			setting_layout_lockhome_oneStep.setClickable(false);
			setting_layout_lockhome_twoStep.setClickable(true);
			lockhome_one.setTextColor(Color.GRAY);
			lockhome_two.setTextColor(Color.WHITE);
			if (firstStepClicked) {
				Dialog seconddialog = new SecondStepDialog(
						LockHomeActivity.this, R.style.MyDialog);
				seconddialog.show();
			}
		}		
    }
}
