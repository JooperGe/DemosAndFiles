package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData.TrainJsonData.TrainData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;

/**
 * 机票
 * 
 * @author Harlan Song
 * @createDate 2013-3-18
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class TrainView extends RelativeLayout {
	private List<TrainData> trains;
	private LinearLayout layout_content;
	private TextView tv_city_from;
	private TextView tv_city_to;
	private TextView tv_date;
	private ImageView imgv_logo;
	private Handler mHandler;

	public TrainView(Context context, List<TrainData> trains, Handler handler) {
		super(context);
		this.trains = trains;
		this.mHandler = handler;
		LayoutInflater.from(getContext()).inflate(R.layout.layout_train, this,
				true);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		tv_city_from = (TextView) findViewById(R.id.tv_city_from);
		tv_city_to = (TextView) findViewById(R.id.tv_city_to);
		tv_date = (TextView) findViewById(R.id.tv_date);
		imgv_logo = (ImageView) findViewById(R.id.imgv_source_logo);
		if (trains != null && trains.size() > 0) {
			if (trains.get(0).city_from != null)
				tv_city_from.setText(trains.get(0).city_from);
			if (trains.get(0).city_to != null)
				tv_city_to.setText(trains.get(0).city_to);
			if (trains.get(0).provider_id == 0) {
				imgv_logo.setImageResource(R.drawable.icon_xiecheng_logo);
			} else if (trains.get(0).provider_id == 1) {
				imgv_logo.setImageResource(R.drawable.logo_quna);
			} else {
				imgv_logo.setVisibility(View.GONE);
			}
			if (trains.get(0).departure_time != null) {
				Date date = new Date(new Long(trains.get(0).departure_time));
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
				tv_date.setText(dateFormat.format(date));
			}
			setData();
		}
	}

	private void setData() {
		if (trains != null && trains.size() > 0) {
			if (trains.size() > 3) {
				for (int i = 0; i < 3; i++) {
					addItemView(i);
				}
				addMoreView();
			} else {
				for (int i = 0; i < trains.size(); i++) {
					addItemView(i);
				}
			}
		}
	}

	private void addMoreView() {
		View moreView = LayoutInflater.from(getContext()).inflate(
				R.layout.layout_plane_more, null);
		moreView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				layout_content.removeAllViews();
				for (int i = 0; i < trains.size(); i++) {
					addItemView(i);
				}
			}
		});
		layout_content.addView(moreView);

	}

	private void addItemView(int position) {
		TrainData data = trains.get(position);
		View itemView = LayoutInflater.from(getContext()).inflate(
				R.layout.layout_train_item, null);
		ImageView imgv_begin = (ImageView) itemView
				.findViewById(R.id.imgv_begin);
		ImageView imgv_end = (ImageView) itemView.findViewById(R.id.imgv_end);
		TextView tv_index = (TextView) itemView.findViewById(R.id.tv_index);
		TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);
		TextView tv_time_begin = (TextView) itemView
				.findViewById(R.id.tv_time_begin);
		TextView tv_time_end = (TextView) itemView
				.findViewById(R.id.tv_time_end);
		TextView tv_station_begin = (TextView) itemView
				.findViewById(R.id.tv_station_begin);
		TextView tv_station_end = (TextView) itemView
				.findViewById(R.id.tv_station_end);
		// TextView tv_last_time = (TextView)
		// itemView.findViewById(R.id.tv_last_time);
		TextView tv_price = (TextView) itemView.findViewById(R.id.tv_price);
		TextView tv_seat_type = (TextView) itemView
				.findViewById(R.id.tv_seat_type);
		layout_content.addView(itemView,
				android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (position < trains.size() - 1) {
			View viewLineBG = new View(getContext());
			viewLineBG.setBackgroundResource(R.drawable.bg_line);
			layout_content.addView(viewLineBG, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
		}
		tv_index.setText(String.valueOf(position + 1));
		
		if (data.is_start==1) {
			imgv_begin.setVisibility(View.VISIBLE);
		}else {
			imgv_begin.setVisibility(View.INVISIBLE);
		}
		if (data.is_end==1) {
			imgv_end.setVisibility(View.VISIBLE);
		}else {
			imgv_end.setVisibility(View.INVISIBLE);
		}
		
		if (data.train_no != null)
			tv_title.setText(data.train_type + " " + data.train_no);
		if (data.departure_time != null)
			tv_time_begin.setText(ContentUtil.formatTime(data.departure_time,
					"HH:mm"));
		if (data.arrive_time != null)
			tv_time_end.setText(ContentUtil.formatTime(data.arrive_time,
					"HH:mm"));
		if (data.station_from != null)
			tv_station_begin.setText(data.station_from);
		if (data.station_to != null)
			tv_station_end.setText(data.station_to);
		// tv_last_time.setVisibility(View.GONE);
		// if(data.departure_time !=null && data.arrive_time != null)
		// tv_last_time.setText(TimeUtil.timeDif(new Long(data.departure_time),
		// new Long(data.arrive_time)));
		if (data.price != null)
			tv_price.setText("￥" + data.price);
		if (data.seat_class != null)
			tv_seat_type.setText(data.seat_class);

		if (data.url != null && data.url.length() > 0) {
			itemView.setTag(data.url);
			itemView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					String url = (String) v.getTag();
					Intent intent = new Intent();
					Uri uri = Uri.parse(url);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(uri);
					try {
						v.getContext().startActivity(intent);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

}
