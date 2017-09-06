package com.viash.voice_assistant.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.CustomSwitchButton;
import com.viash.voice_assistant.data.SavedData;

public class SmsIncomingCallSettingActivity extends Activity implements OnClickListener{
   
	private View autoRemind_incomingCall;
	private View autoRemind_incomingCall_name;
	private View autoRemind_sms;
	private View autoRemind_sms_name_or_number;
	private View autoRemind_sms_content;
	
	private TextView setting_textview_autoRemind_incomingCall_name;
	private TextView setting_textview_autoRemind_sms_name_or_number;
	private TextView setting_textview_autoRemind_sms_content;
	
	private CustomSwitchButton switchButton_incomingCall;
	private CustomSwitchButton switchButton_incomingCallName;
	private CustomSwitchButton switchButton_sms;
	private CustomSwitchButton switchButton_sms_name_or_number;
	private CustomSwitchButton switchButton_sms_content;
	private TextView title_back;

	private AlertDialog.Builder builder;
	private Context mContext = null;
	private boolean bTemp = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_incoming_call);
		mContext = SmsIncomingCallSettingActivity.this;
		autoRemind_incomingCall = findViewById(R.id.setting_layout_autoRemind_incomingCall);
		autoRemind_incomingCall_name = findViewById(R.id.setting_layout_autoRemind_incomingCall_name);
		
		setting_textview_autoRemind_incomingCall_name = (TextView)findViewById(R.id.setting_textview_autoRemind_incomingCall_name);
		 
		autoRemind_sms = findViewById(R.id.setting_layout_autoRemind_sms);
		autoRemind_sms_name_or_number = findViewById(R.id.setting_layout_autoRemind_sms_name_or_number);
		autoRemind_sms_content = findViewById(R.id.setting_layout_autoRemind_sms_content);
		
		setting_textview_autoRemind_sms_name_or_number = (TextView)findViewById(R.id.setting_textview_autoRemind_sms_name_or_number);
		setting_textview_autoRemind_sms_content = (TextView)findViewById(R.id.setting_textview_autoRemind_sms_content);
		
		
		switchButton_incomingCall = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_autoRemind_incomingCall);
		switchButton_incomingCall.setChecked(!SavedData.isAutoRemindIncomingCall());
		if(!SavedData.isAutoRemindIncomingCall())
			SavedData.setAutoRemindIncomingCallName(false);
		switchButton_incomingCallName = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_autoRemind_incomingCall_name);
		switchButton_incomingCallName.setChecked(!SavedData.isAutoRemindIncomingCallName());
		switchButton_sms = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_autoRemind_sms);
		switchButton_sms.setChecked(!SavedData.isAutoRemindSMS());
		if(!SavedData.isAutoRemindSMS())
		{
			SavedData.setAutoRemindSMSNameOrName(false);
			SavedData.setAutoRemindSMSContent(false);			
		}
		else
		{
			if(!SavedData.isAutoRemindSMSNameOrNumber())
				SavedData.setAutoRemindSMSContent(false);	
		}
		switchButton_sms_name_or_number = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_autoRemind_sms_name_or_number);
		switchButton_sms_name_or_number.setChecked(!SavedData.isAutoRemindSMSNameOrNumber());
		switchButton_sms_content = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_autoRemind_sms_content);
		switchButton_sms_content.setChecked(!SavedData.isAutoRemindSMSContent());
		
		if (!SavedData.isAutoRemindSMS()) {
			switchButton_sms_name_or_number.setEnabled(false);
			switchButton_sms_content.setEnabled(false);
			autoRemind_sms_name_or_number.setEnabled(false);
			autoRemind_sms_content.setEnabled(false);

			setting_textview_autoRemind_sms_name_or_number.setTextColor(Color.GRAY);
			setting_textview_autoRemind_sms_content.setTextColor(Color.GRAY);
			switchButton_sms_name_or_number.setTextColor(Color.GRAY);
			switchButton_sms_content.setTextColor(Color.GRAY);
		}
		else {
			
			switchButton_sms_name_or_number.setEnabled(true);
			switchButton_sms_content.setEnabled(true);
			autoRemind_sms_name_or_number.setEnabled(true);
			autoRemind_sms_content.setEnabled(true);			

			setting_textview_autoRemind_sms_name_or_number.setTextColor(Color.rgb(255, 255, 255));
			setting_textview_autoRemind_sms_content.setTextColor(Color.rgb(255, 255, 255));
			switchButton_sms_name_or_number.setTextColor(Color.rgb(255, 255, 255));
			switchButton_sms_content.setTextColor(Color.rgb(255, 255, 255));
			
			if(!SavedData.isAutoRemindSMSNameOrNumber())
			{
				SavedData.setAutoRemindSMSContent(false);
				switchButton_sms_content.setEnabled(false);
				autoRemind_sms_content.setEnabled(false);
				
				setting_textview_autoRemind_sms_content.setTextColor(Color.GRAY);
				switchButton_sms_content.setTextColor(Color.GRAY);
			}
		}
		
		if (!SavedData.isAutoRemindIncomingCall()) {
			autoRemind_incomingCall_name.setEnabled(false);
			switchButton_incomingCallName.setEnabled(false);

			setting_textview_autoRemind_incomingCall_name.setTextColor(Color.GRAY);
			switchButton_incomingCallName.setTextColor(Color.GRAY);
			
		}else {
			autoRemind_incomingCall_name.setEnabled(true);
			switchButton_incomingCallName.setEnabled(true);

			setting_textview_autoRemind_incomingCall_name.setTextColor(Color.rgb(255, 255, 255));
			switchButton_incomingCallName.setTextColor(Color.rgb(255, 255, 255));
		}
		
		title_back = (TextView) this.findViewById(R.id.setting_title);

		
		initListener();
	}

	private void initListener()
	{
		autoRemind_incomingCall.setOnClickListener(new OnClickListener(){
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switchButton_incomingCall.performClick();
			}		
		});
		
		switchButton_incomingCall.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
								
				if (isChecked) {
					SavedData.setAutoRemindIncomingCall(!isChecked);
					SavedData.setAutoRemindIncomingCallName(!isChecked);
					switchButton_incomingCallName.setChecked(true);
					
					autoRemind_incomingCall_name.setEnabled(false);
					switchButton_incomingCallName.setEnabled(false);

					setting_textview_autoRemind_incomingCall_name.setTextColor(Color.GRAY);
					switchButton_incomingCallName.setTextColor(Color.GRAY);
				} else {
					bTemp = !isChecked;					
					builder = new Builder(mContext);
					builder.setMessage(mContext.getResources().getString(
							R.string.setting_remind_incoming_call_prompt));
					builder.setTitle(mContext.getResources().getString(R.string.setting_prompt));
					builder.setNegativeButton(mContext.getResources().getString(R.string.cancel), 
							new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which)  {
							// TODO Auto-generated method stub
							switchButton_incomingCall.setChecked(bTemp);
							dialog.dismiss();
						}
					});
					
					builder.setPositiveButton(mContext.getResources().getString(R.string.dialog_continue), 
							new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which)  {
							SavedData.setAutoRemindIncomingCall(bTemp);
							autoRemind_incomingCall_name.setEnabled(true);
							switchButton_incomingCallName.setEnabled(true);					

							setting_textview_autoRemind_incomingCall_name.setTextColor(Color.rgb(255, 255, 255));
							switchButton_incomingCallName.setTextColor(Color.rgb(255, 255, 255));
							dialog.dismiss();
						}
					});
					builder.show();
				}
			}			
		});
		
		autoRemind_incomingCall_name.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindIncomingCall())
				  switchButton_incomingCallName.performClick();	
			}		
		});
		
		switchButton_incomingCallName.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindIncomingCall())
				  SavedData.setAutoRemindIncomingCallName(!isChecked);
			}			
		});
		
		autoRemind_sms.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switchButton_sms.performClick();
			}			
		});
		
		switchButton_sms.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				SavedData.setAutoRemindSMS(!isChecked);
				if (isChecked) {
					SavedData.setAutoRemindSMS(!isChecked);					
					SavedData.setAutoRemindSMSNameOrName(!isChecked);
					SavedData.setAutoRemindSMSContent(!isChecked);
					switchButton_sms_name_or_number.setChecked(true);
					switchButton_sms_content.setChecked(true);
					
					switchButton_sms_name_or_number.setEnabled(false);
					switchButton_sms_content.setEnabled(false);
					autoRemind_sms_name_or_number.setEnabled(false);
					autoRemind_sms_content.setEnabled(false);
					
					setting_textview_autoRemind_sms_name_or_number.setTextColor(Color.GRAY);
					setting_textview_autoRemind_sms_content.setTextColor(Color.GRAY);
					switchButton_sms_name_or_number.setTextColor(Color.GRAY);
					switchButton_sms_content.setTextColor(Color.GRAY);
				}else {
					bTemp = !isChecked;					
					builder = new Builder(mContext);
					builder.setMessage(mContext.getResources().getString(
							R.string.setting_remind_sm_prompt));
					builder.setTitle(mContext.getResources().getString(R.string.setting_prompt));
					builder.setNegativeButton(mContext.getResources().getString(R.string.cancel), 
							new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which)  {
							// TODO Auto-generated method stub
							switchButton_sms.setChecked(bTemp);
							dialog.dismiss();
						}
					});
					
					builder.setPositiveButton(mContext.getResources().getString(R.string.dialog_continue), 
							new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which)  {
							SavedData.setAutoRemindIncomingCall(bTemp);
							switchButton_sms_name_or_number.setEnabled(true);
							switchButton_sms_content.setEnabled(true);
							autoRemind_sms_name_or_number.setEnabled(true);
							autoRemind_sms_content.setEnabled(true);					

							setting_textview_autoRemind_sms_name_or_number.setTextColor(Color.rgb(255, 255, 255));
							setting_textview_autoRemind_sms_content.setTextColor(Color.rgb(255, 255, 255));
							switchButton_sms_name_or_number.setTextColor(Color.rgb(255, 255, 255));
							switchButton_sms_content.setTextColor(Color.rgb(255, 255, 255));
							
							if(!SavedData.isAutoRemindSMSNameOrNumber())
							{
								SavedData.setAutoRemindSMSContent(false);
								switchButton_sms_content.setEnabled(false);
								autoRemind_sms_content.setEnabled(false);
								
								setting_textview_autoRemind_sms_content.setTextColor(Color.GRAY);
								switchButton_sms_content.setTextColor(Color.GRAY);
							}
							dialog.dismiss();
						}
					});
					builder.show();					
				}
			}			
		});
		
		autoRemind_sms_name_or_number.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindSMS())
				  switchButton_sms_name_or_number.performClick();
			}			
		});
		
		switchButton_sms_name_or_number.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindSMS())
				  SavedData.setAutoRemindSMSNameOrName(!isChecked);
				if(isChecked)
				{
					SavedData.setAutoRemindSMSContent(!isChecked);
					switchButton_sms_content.setChecked(true);
					switchButton_sms_content.setEnabled(false);
					autoRemind_sms_content.setEnabled(false);
					
					setting_textview_autoRemind_sms_content.setTextColor(Color.GRAY);
					switchButton_sms_content.setTextColor(Color.GRAY);
				}
				else
				{
					switchButton_sms_content.setEnabled(true);
					autoRemind_sms_content.setEnabled(true);					

					setting_textview_autoRemind_sms_content.setTextColor(Color.rgb(255, 255, 255));
					switchButton_sms_content.setTextColor(Color.rgb(255, 255, 255));
				}
			}			
		});
		
		autoRemind_sms_content.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindSMS() && SavedData.isAutoRemindSMSNameOrNumber())
				  switchButton_sms_content.performClick();
			}			
		});
		
		switchButton_sms_content.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(SavedData.isAutoRemindSMS() && SavedData.isAutoRemindSMSNameOrNumber())
				  SavedData.setAutoRemindSMSContent(!isChecked);
			}			
		});
		
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SmsIncomingCallSettingActivity.this.finish();				
			}
			
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
