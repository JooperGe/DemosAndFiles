package com.viash.voice_assistant.activity;

import com.viash.voice_assistant.R;
import com.viash.voicelib.utils.CustomToast;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ContactUsActivity extends Activity {
	private TextView official_website;
	private TextView customer_service;
	private TextView title_back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contactus);
		
		initView();
	}

	private void initView() {
		official_website = (TextView) this.findViewById(R.id.official_website);
		customer_service = (TextView) this.findViewById(R.id.customer_service);
		
		// set value
		final String official_website_url = "www.ola.com.cn";
		//String text = "<a href='http://" + official_website_url + "'>" + official_website_url + "</a>";
		official_website.setText(official_website_url);
		official_website.setPaintFlags(customer_service.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		final String customer_service_url = "service@olavoice.com";
		//text = "<a href='mailto:" + customer_service_url + "'>" + customer_service_url + "</a>";
		customer_service.setText(customer_service_url);
		customer_service.setPaintFlags(customer_service.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		//customer_service.setText(Html.fromHtml(text));
		//customer_service.setMovementMethod(LinkMovementMethod.getInstance());

		// Listener
		official_website.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW);  
				intent.setData(Uri.parse("http://" + official_website_url));  
				try
				{
					startActivity(intent);
				}catch(ActivityNotFoundException e)
				{
					e.printStackTrace();
					CustomToast.makeToast(ContactUsActivity.this, getResources().getString(R.string.open_web_error));//, Toast.LENGTH_LONG).show();
				}
			}
		});
		customer_service.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);  
				intent.setData(Uri.parse("mailto:" + customer_service_url));  
				try
				{
					startActivity(intent);
				}catch(ActivityNotFoundException e)
				{
					e.printStackTrace();
					CustomToast.makeToast(ContactUsActivity.this, getResources().getString(R.string.open_mail_error));//, Toast.LENGTH_LONG).show();
				}
			}
		});
		

		title_back = (TextView) this.findViewById(R.id.setting_title);
		title_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContactUsActivity.this.finish();				
			}
			
		});
	}
}
