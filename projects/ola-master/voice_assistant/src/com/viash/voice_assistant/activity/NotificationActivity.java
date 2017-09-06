package com.viash.voice_assistant.activity;

import java.util.ArrayList;

import com.viash.voice_assistant.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends Activity {
	private TextView notification_information;
	private Button notification_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			ArrayList<String> title = bundle.getStringArrayList("title");
			if (title.size() != 0) {
				init();
				initInformation(title);
			} else {
				finish();
			}
		} else {
			finish();
		}
	}

	private void initInformation(ArrayList<String> title) {
		String information = "";
		for (int i = 0; i < title.size(); i++) {
			information += (title.get(i) + System.getProperty("line.separator"));
		}
		notification_information.setText(information);
	}

	private void init() {
		notification_information = (TextView) this
				.findViewById(R.id.notification_information);
		notification_ok = (Button) this.findViewById(R.id.notification_ok);
		notification_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
}
