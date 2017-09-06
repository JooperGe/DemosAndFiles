package com.viash.voice_assistant.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;

public class DigitalClock extends LinearLayout {

	private static SimpleDateFormat sdf_hour = new SimpleDateFormat("HH");
	private static SimpleDateFormat sdf_minute = new SimpleDateFormat(":mm");
	private static SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar cal = Calendar.getInstance();
	
	private TextView textViewTime,textViewTimeminute, textViewDate, textViewWeek;

	public DigitalClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 使用layoutinflater把布局加载到本ViewGroup
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_date, this);

		textViewTime = (TextView) findViewById(R.id.textViewTime);
		textViewTimeminute = (TextView) findViewById(R.id.textViewTimeminute);
		textViewDate = (TextView) findViewById(R.id.textViewDate);
		textViewWeek = (TextView) findViewById(R.id.textViewWeek);

		startThread();

	}

	public static String getCurrentTime(Date date) {

		sdf_hour.format(date);
		
		return sdf_hour.format(date);
	}
	public static String getCurrentTime2(Date date) {

		sdf_minute.format(date);
		
		return sdf_minute.format(date);
	}

	public static String getCurrentDate(Date date) {

		sdf_date.format(date);
		return sdf_date.format(date);
	}

	public static String getCurrentWeekDay(Date dt) {
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		cal.setTime(dt);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;

		return weekDays[w];
	}

	private void startThread() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					handler.sendEmptyMessage(12);
					try {
						Thread.sleep(1000 * 60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		}).start();
	}

	@SuppressLint("HandlerLeak") 
	Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 12) {
				Date date=new Date();
				textViewTime.setText(getCurrentTime(date));
				textViewTimeminute.setText(getCurrentTime2(date));
				textViewDate.setText(getCurrentDate(date));
				textViewWeek.setText(getCurrentWeekDay(date));
			}

		}
	};

	public void updateDigitalClock()
	{
		handler.sendEmptyMessage(12);
	}
}