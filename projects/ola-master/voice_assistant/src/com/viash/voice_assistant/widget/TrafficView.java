package com.viash.voice_assistant.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.util.DensityUtil;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.TrafficJsonData;
import com.viash.voicelib.data.PreFormatData.TrafficJsonData.TrafficData;

/**
 * 公交
 * @author Leo
 * @createDate 2013-12-30
 * @
 */
@SuppressLint("ViewConstructor")
public class TrafficView extends SelectionBaseView {
	private List<TrafficData> traffics;
	private static int highlight = 0;
	private static View highlightView = null;
	private List<View> lView = new ArrayList<View>();	
	public TrafficView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);		
	}
			
	private  String formatTime(int minte){
		String time ="";
		if(minte >0 && minte < 60){
			time = minte + getResources().getString(R.string.minute);
		}else if (minte >= 60 && minte < 1440){
			time = (minte / 60) + getResources().getString(R.string.hour) + (minte % 60) + getResources().getString(R.string.minute);
		} else if(minte >= 1440 ){
			time = (minte / 1440) + getResources().getString(R.string.day) + (minte *24 / 60) + getResources().getString(R.string.hour) + (minte % 1440) + getResources().getString(R.string.minute);
		}
		return time;
	}
	
	private String formatDistance(int distance){
		String distance_str = "";
		if(distance > 0 && distance < 1000){
			distance_str = distance + getResources().getString(R.string.meter);
		}else if( distance >= 1000){
			distance_str = ((double)distance / 1000) + getResources().getString(R.string.kilometer);
		}
		return distance_str;
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_bus;
		mViewData.mPrimaryTitleText = "乘车路线";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((TrafficJsonData)mData).trafficDatas.size();
		mViewData.mHighLight = 0;		
	}
    
	@Override
	protected void initDataView(){
		super.initDataView();
		traffics = ((TrafficJsonData)mData).trafficDatas;
		setListContentavaible(false);//Content view is not a list view.
		setDataView();
	}
	
	private void setDataView()
	{
		lView = new ArrayList<View>();
		if(!isFullScreen)
		{
			int count = 0;
			if(mViewData.mTotleItemNumber <= mViewData.mMinItemNumber)
			    count = mViewData.mTotleItemNumber;
			else
				count = mViewData.mMinItemNumber;
			for(int i= 0; i<count; i++)
			{
				mNormalContent.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				mNormalContent.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		else
		{
			ScrollView sView = new ScrollView(mContext);
			LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			LinearLayout layout_content = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_bus, null).findViewById(R.id.layout_content);
			sView.setLayoutParams(param);
			for(int i= 0; i< mViewData.mTotleItemNumber; i++)
			{
				layout_content.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
			sView.addView(layout_content);
			mNormalContent.addView(sView);
		}
	}
	
	private View getView(int position) {
		
		View convertView = null;
		ViewHolder holder;
		TrafficData trafficData = traffics.get(position);
		if(lView != null)
		  if(lView.size() >= position+1)
		  {
		    convertView = lView.get(position);
		  }
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_traffic_item, null);
			holder = new ViewHolder();
			holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_time_distance = (TextView) convertView.findViewById(R.id.tv_time_distance);
			holder.imgv_arrows = (ImageView) convertView.findViewById(R.id.imgv_arrows);
			holder.layout_child = (LinearLayout) convertView.findViewById(R.id.layout_child);
			convertView.setTag(holder);
			lView.add(convertView);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tv_index.setText(Integer.toString(position+1));
		if(trafficData.segment_title !=null)
		{
			String[] title = trafficData.segment_title;
			String title_str ="";
			for(String str:title)
				title_str +=str ;	
			holder.tv_title.setText(title_str);
		}
		
		String timeAndDistance = "";
		if(trafficData.total_time != 0 )
			timeAndDistance += formatTime(trafficData.total_time);
		if(trafficData.total_distance != 0)
			timeAndDistance += "/" +formatDistance(trafficData.total_distance);
		holder.tv_time_distance.setText(timeAndDistance);
		
		if(trafficData.segment_description !=null && trafficData.segment_description.length > 0)
		{
			String des ="";
			for(String str :trafficData.segment_description){
				des += str +"\n";
			}
			des = des.substring(0,des.length() -1);
			TextView textView = new TextView(getContext());
			int size = DensityUtil.px2dip(mContext, getResources().getDimension(R.dimen.content_second_text_height));
			textView.setTextSize(size);
			textView.setTextColor(getResources().getColor(R.color.list_item_text));
			LinearLayout.LayoutParams param= new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			textView.setLayoutParams(param);
			textView.setText(des);
			holder.layout_child.addView(textView);
			holder.imgv_arrows.setBackgroundResource(R.drawable.icon_bus_enter);
		}
		holder.layout_child.setVisibility(View.GONE);
		
		if(operationEnable)
		{
			if(holder.layout_child.getChildCount() > 0)
			{
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ViewHolder holderTemp = (ViewHolder) v.getTag();
						if(holderTemp.layout_child.getVisibility() == View.GONE)
						{	
							holderTemp.layout_child.setVisibility(View.VISIBLE);
							holderTemp.imgv_arrows.setBackgroundResource(R.drawable.icon_bus_back);
						}
						else
						{
							holderTemp.layout_child.setVisibility(View.GONE);
							holderTemp.imgv_arrows.setBackgroundResource(R.drawable.icon_bus_enter);
						}
						
						int currentSelect = Integer.parseInt(holderTemp.tv_index.getText().toString()) - 1;
						if(highlight != currentSelect)
						{	
							  if(highlightView != null)
								  highlightView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
							  v.setBackgroundResource(R.drawable.background_press);
							  highlight = currentSelect;
							  highlightView = v;
						}
						else
						{
							v.setBackgroundResource(R.drawable.background_press);
							highlightView = v;
						}
					}			
				});
			}
		}
		return convertView;	
	}
	
	private class ViewHolder
	{
		TextView tv_index;
		TextView tv_title;
		TextView tv_time_distance;
		ImageView imgv_arrows;
		LinearLayout layout_child;
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
