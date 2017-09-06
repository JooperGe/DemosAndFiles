package com.viash.voice_assistant.activity;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.CustomSwitchButton;
import com.viash.voice_assistant.data.SavedData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TipsActivity extends Activity implements OnClickListener{
    
	private CustomSwitchButton CustomSwitchButtonHelpTips;
	private CustomSwitchButton CustomSwitchButtonNetWorkTips;
	private View help_tips;
	private View network_tips;
	private TextView title_back;
	
	//private static boolean isHelpTips = false;
	//private static boolean isNetworkTips = false; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tips);
		CustomSwitchButtonHelpTips = (CustomSwitchButton)findViewById(R.id.setting_swtichbutton_help_tips);
		CustomSwitchButtonNetWorkTips = (CustomSwitchButton) findViewById(R.id.setting_swtichbutton_network_tips);
		//isHelpTips = SavedData.isHelpTips();
		//CustomSwitchButtonHelpTips.setChecked(!SavedData.isHelpTips());
		//isNetworkTips = SavedData.isNetworkTips();
		CustomSwitchButtonNetWorkTips.setChecked(!SavedData.isNetworkTips());
		help_tips = (View) findViewById(R.id.setting_layout_help_tips);
		network_tips = (View) findViewById(R.id.setting_layout_network_tips);
		title_back = (TextView) this.findViewById(R.id.setting_title);

		initListener();
	}
	
	public void initListener()
	{
		help_tips.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomSwitchButtonHelpTips.performClick();
			}	
		});
		
		CustomSwitchButtonHelpTips.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				  //SavedData.setHelpTips(!isChecked);
			}
			
		});
		
		network_tips.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomSwitchButtonNetWorkTips.performClick();
			}	
		});
		
		CustomSwitchButtonNetWorkTips.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {				
				  SavedData.setNetworkTips(!isChecked);
			}
			
		});
		
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TipsActivity.this.finish();				
			}
			
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
