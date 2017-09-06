package com.viash.voice_assistant.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

import com.umeng.analytics.MobclickAgent;
import com.viash.voice_assistant.R;
import com.viash.voicelib.widget.MulitPointTouchListener;

public class ImageBrowerActivity  extends Activity{
	private static ImageView imgv_img;
	private static ProgressBar pb_loading;
	private Button btn_back;
	private View layout_title_bar;
	private View view_form;
	private boolean titlebarShow = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		setContentView(R.layout.activity_brower_img);
		imgv_img = (ImageView) findViewById(R.id.imgv_img);
		pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
		btn_back = (Button) findViewById(R.id.btn_back);
		layout_title_bar = findViewById(R.id.layout_title_bar);
		view_form = findViewById(R.id.layout_form);
		String url = getIntent().getStringExtra("url");
		if(url != null && url.length() > 0){
			pb_loading.setVisibility(View.VISIBLE);
			new ImageAsyncTask().execute(url);
		}else{
			pb_loading.setVisibility(View.GONE);
		}
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		view_form.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(titlebarShow){
					layout_title_bar.setVisibility(View.GONE);
					titlebarShow = false;
				}else{
					layout_title_bar.setVisibility(View.VISIBLE);
					titlebarShow = true;
				}
			}
		});
	}

	private static class ImageAsyncTask extends AsyncTask<Object, Object, Bitmap> {
		@Override
		protected Bitmap doInBackground(Object... params) {
			String url = (String) params[0];
			Bitmap bmp = null;
			try {
				bmp = BitmapFactory.decodeStream(new URL(url).openStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmp;
		}
		
		protected void onPostExecute(Bitmap result) {
			if(result != null ){
				imgv_img.setImageBitmap(result);
				imgv_img.setScaleType(ScaleType.MATRIX);
			    imgv_img.setOnTouchListener(new MulitPointTouchListener());
			}else{
				imgv_img.setImageResource(R.drawable.logo);
			}
			pb_loading.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}
	
	
}
