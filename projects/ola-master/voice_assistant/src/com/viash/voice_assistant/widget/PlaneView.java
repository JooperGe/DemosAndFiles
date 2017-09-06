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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.PlaneJsonData;
import com.viash.voicelib.data.PreFormatData.PlaneJsonData.PlaneData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;

/**
 * 机票
 */
@SuppressLint("ViewConstructor")
public class PlaneView extends SelectionBaseView {
	private List<PlaneData> planes;
	private View layout_view;
	
	public PlaneView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
	}

	@Override
	protected void initDataView() {
		super.initDataView();
		setListContentavaible(true);
		
		initOwnSubTitle();
		//mlsvContent.setDivider(getContext().getResources().getDrawable(R.drawable.bg_line));
		PlaneAdapter adapter = new PlaneAdapter();
		setAdapter(adapter);
		
		if (!isFullScreen) {
			setListViewHeight(mlsvContent);
		}
	}
	
	@SuppressLint({ "UseValueOf", "SimpleDateFormat" })
	private void initOwnSubTitle() {
		layout_view = LayoutInflater.from(getContext()).inflate(R.layout.layout_plane,null);
		TextView tv_city_from = (TextView) layout_view.findViewById(R.id.tv_city_from);
		TextView tv_city_to = (TextView) layout_view.findViewById(R.id.tv_city_to);
		TextView tv_date = (TextView) layout_view.findViewById(R.id.tv_date);
		
		if(planes !=null && planes.size() > 0){
			if(planes.get(0).city_from != null)
				tv_city_from.setText(planes.get(0).city_from);
			if(planes.get(0).city_to != null)
				tv_city_to.setText(planes.get(0).city_to);
			if(planes.get(0).departure_time != null){
				Date date = new Date(new Long(planes.get(0).departure_time));
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
				tv_date.setText(dateFormat.format(date));
			}
		}
		setSubTitle(layout_view);
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_plantickets;
		mViewData.mPrimaryTitleText = "飞机票";
		mViewData.mSecondaryTitleText = "携程";
		mViewData.mSecondaryTitleImg = R.drawable.icon_xiecheng_logo;

		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = "";

		PlaneJsonData planeJsonData = (PlaneJsonData) ((PreFormatData) mCommunicationData).getJsonData();
		planes = planeJsonData.mPlaneData;

		mViewData.mMinItemNumber = 2;
		if (planes != null) {
			mViewData.mTotleItemNumber = planes.size();
		} else {
			mViewData.mTotleItemNumber = 0;
		}
		mViewData.mHighLight = 0;
	}
	
	private class PlaneAdapter extends BaseAdapter {

		class ViewHolder {
			TextView tv_index;
			TextView tv_title;
			TextView tv_time_begin;
			TextView tv_time_end;
			TextView tv_air_begin;
			TextView tv_air_end;
			TextView tv_price;
			TextView tv_rebate;
			//String  url;
		}

		@Override
		public int getCount() {
			if (isFullScreen) {
				return mViewData.mTotleItemNumber;
			} else {
				if (mViewData.mTotleItemNumber >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				} else
					return mViewData.mTotleItemNumber;
			}
		}

		@Override
		public Object getItem(int position) {
			return planes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("UseValueOf")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			Log.i("getView", "Current position is " + position);
			PlaneData  data=planes.get(position);
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_plane_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_time_begin = (TextView) convertView.findViewById(R.id.tv_time_begin);
				holder.tv_time_end = (TextView) convertView.findViewById(R.id.tv_time_end);
				holder.tv_air_begin = (TextView) convertView.findViewById(R.id.tv_air_begin);
				holder.tv_air_end = (TextView) convertView.findViewById(R.id.tv_air_end);
				holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
				holder.tv_rebate = (TextView) convertView.findViewById(R.id.tv_rebate);
				//holder.url = null;

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv_index.setText(String.valueOf((position + 1)));

			String title = "";

			if(data.air_company !=null){
				title += data.air_company;
			}
			if(data.flight_no !=null){
				title += " " + data.flight_no;
			}
			holder.tv_title.setText(title);
			
			if(data.departure_time != null ){
				holder.tv_time_begin.setText(ContentUtil.formatTime(data.departure_time, "HH:mm"));
			}
			if(data.arrive_time != null){
				holder.tv_time_end.setText(ContentUtil.formatTime(data.arrive_time, "HH:mm"));
			}
			if(data.airport_from !=null){ 
				holder.tv_air_begin.setText(data.airport_from);
			}
			if(data.airport_to !=null) {
				holder.tv_air_end.setText(data.airport_to);
			}
			if(data.price !=null){
				holder.tv_price.setText("￥" + data.price);
			}
			if(data.discount !=null && data.discount.trim().length() > 0 ){
				double discount = Double.valueOf(data.discount);
				if(discount < 1.0){
					discount = discount * 10;
					holder.tv_rebate.setText( new java.text.DecimalFormat("#.0").format(discount) + " " +getResources().getString(R.string.rebate));
				}else{
					holder.tv_rebate.setVisibility(View.GONE);
				}
			}else{
				holder.tv_rebate.setVisibility(View.GONE);
			}
			
			if(data.url != null && data.url.length() > 0)
			{
				final String url = data.url;
				convertView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						//ViewHolder holder = (ViewHolder) v.getTag();
						Intent intent = new Intent();
						Uri uri = Uri.parse(url);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction(Intent.ACTION_VIEW);					
						intent.setData(uri);
						try
						{
							v.getContext().startActivity(intent);
						}
						catch(ActivityNotFoundException e)
						{
							e.printStackTrace();
						}
					}
				});			
			}
			
			return convertView;
		}
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
