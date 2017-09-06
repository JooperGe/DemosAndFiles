package com.viash.voice_assistant.activity;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.component.RecommendView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecommendActivity extends Activity {
	private RelativeLayout layout_recommend;
	private RecommendView mRecommendView;
	private TextView title_back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_recommend);
		
		initView();
	}
	public void initView()
	{
		layout_recommend = (RelativeLayout) findViewById(R.id.layout_recommend);
		mRecommendView = new RecommendView(this.getApplicationContext(), null);
		layout_recommend.addView(mRecommendView);
		title_back = (TextView) this.findViewById(R.id.setting_title);
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecommendActivity.this.finish();				
			}
			
		});
	}
}
