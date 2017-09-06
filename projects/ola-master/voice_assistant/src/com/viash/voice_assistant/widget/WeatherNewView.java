package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.WeatherJsonData;
import com.viash.voicelib.data.PreFormatData.WeatherJsonData.WeatherData;
import com.viash.voicelib.utils.LogOutput;

/**
 * 天气
 * @author Leo
 * @date 2014-01-08
 */
@SuppressLint("ViewConstructor")
public class WeatherNewView extends SelectionBaseView {
	public List<WeatherData> weatherDatas;
	private ArrayList<View> lView = null;
	private int queryIndex = 0;
	private static final String TAG = "WeatherNewView";
	String[] weatherNameArray = null;
	int[] weatherImageArray = null;

	public WeatherNewView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);		
	}

	private void setWeatherImage(ImageView weatherImg, int weather1,
			int weather2) {
		if (weather1 == weather2) {
			if (weather1 < weatherImageArray.length) {
				weatherImg.setImageResource(weatherImageArray[weather1]);
			} else {
				weatherImg.setImageResource(R.drawable.icon_weather00);
				LogOutput.e(TAG, "没有相应的天气图片weather1：" + weather1);
			}
		} else {
			if (weather1 > weather2) {
				if (weather1 < weatherImageArray.length) {
					weatherImg.setImageResource(weatherImageArray[weather1]);
				} else {
					weatherImg.setImageResource(R.drawable.icon_weather00);
					LogOutput.e(TAG, "没有相应的天气图片weather1：" + weather1);
				}
			} else {
				if (weather1 < weatherImageArray.length) {
					weatherImg.setImageResource(weatherImageArray[weather2]);
				} else {
					weatherImg.setImageResource(R.drawable.icon_weather00);
					LogOutput.e(TAG, "没有相应的天气图片weather1：" + weather2);
				}
			}
		}
	}

	protected String getWeatherName(int weather1, int weather2) {
		String weatherName = "";
		if (weather1 == weather2) {
			if (weather1 < weatherNameArray.length) {
				weatherName = weatherNameArray[weather1];
			} else {
				weatherName = "未知天气";
				LogOutput.e(TAG, "有未知天气-->weather1:" + weather1 + " weather2:"
						+ weather2);
			}
		} else {
			if (weather1 < weatherNameArray.length) {
				weatherName += weatherNameArray[weather1];
			} else {
				LogOutput.e(TAG, "有未知天气-->weather1:" + weather1);
			}
			if (weather2 < weatherNameArray.length) {
				weatherName += "转" + weatherNameArray[weather2];
			} else {
				LogOutput.e(TAG, "有未知天气-->weather2:" + weather2);
			}
			if ("".equals(weatherName.trim()))
				weatherName = "未知天气";
		}
		return weatherName;
	}

	public String getWeek(int day, long time) {
		String[] dayOfWeek = { "", "日", "一", "二", "三", "四", "五", "六" };
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("");
		sdf.applyPattern("MM-dd");
		Date dt = new Date(time);
		calendar.setTime(dt);
		// calendar.add(Calendar.DAY_OF_YEAR, day);
		return " 周" + dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];
	}

	public String getDate(int day, long time) {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("");
		sdf.applyPattern("MM/dd");
		Date dt = new Date(time);
		calendar.setTime(dt);
		// calendar.add(Calendar.DAY_OF_YEAR, day);
		return sdf.format(dt);
	}

	public String getWeek(int day) {
		String[] dayOfWeek = { "", "日", "一", "二", "三", "四", "五", "六" };
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("",
				Locale.SIMPLIFIED_CHINESE);
		sdf.applyPattern("MM-dd");
		calendar.add(Calendar.DAY_OF_YEAR, day);
		return " 周" + dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];
	}

	public String getDate(int day) {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("",
				Locale.SIMPLIFIED_CHINESE);
		sdf.applyPattern("MM/dd");
		calendar.add(Calendar.DAY_OF_YEAR, day);
		Date date = calendar.getTime();
		return sdf.format(date);
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_weather;
		mViewData.mPrimaryTitleText = "天气";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 4;
		mViewData.mTotleItemNumber = ((WeatherJsonData)mData).mWeatherData.size();
		mViewData.mHighLight = 0;
		init();
	}
    
	private void init()
	{
		int []ImageArray = { R.drawable.icon_weather01,
				R.drawable.icon_weather10, R.drawable.icon_weather02,
				R.drawable.icon_weather03, R.drawable.icon_weather06,
				R.drawable.icon_weather06, R.drawable.icon_weather04,
				R.drawable.icon_weather05, R.drawable.icon_weather05,
				R.drawable.icon_weather06, R.drawable.icon_weather06,
				R.drawable.icon_weather06, R.drawable.icon_weather06,
				R.drawable.icon_weather06, R.drawable.icon_weather06,
				R.drawable.icon_weather06, R.drawable.icon_weather06,
				R.drawable.icon_weather06, R.drawable.icon_weather08,
				R.drawable.icon_weather09, R.drawable.icon_weather09,
				R.drawable.icon_weather09, R.drawable.icon_weather09,
				R.drawable.icon_weather09, R.drawable.icon_weather09,
				R.drawable.icon_weather09, R.drawable.icon_weather09,
				R.drawable.icon_weather09, R.drawable.icon_weather12,
				R.drawable.icon_weather12, R.drawable.icon_weather12,
				R.drawable.icon_weather12, R.drawable.icon_weather10 };
		weatherImageArray = ImageArray;
		weatherNameArray =  this.getResources().getStringArray(R.array.weather_names_array);
	}
	@Override
	protected void initDataView(){
		super.initDataView();
		weatherDatas = ((WeatherJsonData)mData).mWeatherData;
		if(weatherDatas.size() == 0)
			return;
		setListContentavaible(false);//Content view is not a list view.
		setDataView();
	}
	
	private void setDataView()
	{
		lView = new ArrayList<View>();
		for(int j=0; j<weatherDatas.size(); j++)
		{
			if(weatherDatas.get(j).is_querying == 1)
			{
				queryIndex = j;
				break;
			}
		}
		if(!isFullScreen)
		{
			int count = 0;
			if(mViewData.mTotleItemNumber <= mViewData.mMinItemNumber)
			    count = mViewData.mTotleItemNumber;
			else
				count = mViewData.mMinItemNumber;
					
			mNormalContent.addView(getView(queryIndex));
			ImageView viewbg = new ImageView(getContext());
			viewbg.setImageResource(R.drawable.bg_line);
			viewbg.setScaleType(ScaleType.FIT_XY);
			mNormalContent.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			for(int i= 0; i<count; i++)
			{	
				if(i == queryIndex)
					continue;				
				mNormalContent.addView(getView(i));
				ImageView viewbg1 = new ImageView(getContext());
				viewbg1.setImageResource(R.drawable.bg_line);
				viewbg1.setScaleType(ScaleType.FIT_XY);
				mNormalContent.addView(viewbg1,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		else
		{
			ScrollView sView = new ScrollView(mContext);
			LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			LinearLayout layout_content = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_bus, null).findViewById(R.id.layout_content);
			sView.setLayoutParams(param);
			
			layout_content.addView(getView(queryIndex));
			ImageView viewbg = new ImageView(getContext());
			viewbg.setImageResource(R.drawable.bg_line);
			viewbg.setScaleType(ScaleType.FIT_XY);
			layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			
			for(int i= 0; i< mViewData.mTotleItemNumber; i++)
			{
				if(i == queryIndex)
					continue;
				layout_content.addView(getView(i));
				ImageView viewbg1 = new ImageView(getContext());
				viewbg1.setImageResource(R.drawable.bg_line);
				viewbg1.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg1,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
			sView.addView(layout_content);
			mNormalContent.addView(sView);
		}
	}
	
	private View getView(int position) {
		WeatherData weatherData = weatherDatas.get(position);
		View convertView = null;
		if(position == queryIndex)
		{
			ViewHighlightHolder holder;
			if(lView != null)
			  if(lView.size() >= 1)
			  {
			    convertView = lView.get(0);//queryIndex position is 0
			  }
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_weather_item_highlight, null);
				holder = new ViewHighlightHolder();
				holder.imgv_history = (ImageView) convertView.findViewById(R.id.imgv_history);
				holder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				holder.tv_temperature = (TextView) convertView.findViewById(R.id.tv_temperature);
				holder.tv_pm = (TextView) convertView.findViewById(R.id.tv_pm);
				holder.tv_week = (TextView) convertView.findViewById(R.id.tv_week);
				holder.tv_weather = (TextView) convertView.findViewById(R.id.tv_weather);
				holder.tv_wind = (TextView) convertView.findViewById(R.id.tv_wind);				
				convertView.setTag(holder);
				lView.add(convertView);
			}
			else
			{
				holder = (ViewHighlightHolder) convertView.getTag();
			}
			setWeatherImage(holder.imgv_history, weatherData.weather1, weatherData.weather2);
			holder.tv_city.setText(weatherData.city);
			if (weatherData.real_date != 0) {
				holder.tv_date.setText(getDate(weatherData.date, weatherData.real_date));
				holder.tv_week.setText(getWeek(weatherData.date, weatherData.real_date));
			} else {
				holder.tv_date.setText(getDate(weatherData.date));
				holder.tv_week.setText(getWeek(weatherData.date));
			}
			holder.tv_temperature.setText(weatherData.temperature_low.replaceAll("℃", "") + "~"
					+ weatherData.temperature_high.replaceAll("℃", "") + "℃");
			if (weatherData.pm25 != 0) {
				holder.tv_pm.setText("PM2.5(" + weatherData.pm25 + ")");
			} else {
				holder.tv_pm.setVisibility(View.GONE);
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);				
				params.addRule(ALIGN_PARENT_RIGHT);
				params.rightMargin = (int) getResources().getDimension(R.dimen.whole_content_margin_right);
				holder.tv_temperature.setLayoutParams(params);
			}
			holder.tv_weather.setText(getWeatherName(weatherData.weather1, weatherData.weather2));
			if(weatherData.wind.contains("级"))
			  holder.tv_wind.setText(weatherData.wind);	
			else	
			  holder.tv_wind.setText(weatherData.wind+weatherData.wind_level);
		}
		else
		{			
			ViewHolder holder;
			if(lView != null)
			  if(lView.size() >= position+2)
			  {
			    convertView = lView.get(position+1);//skip queryIndex 
			  }
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_weather_item, null);
				holder = new ViewHolder();
				holder.imgv_history = (ImageView) convertView.findViewById(R.id.imgv_history);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				holder.tv_weather = (TextView) convertView.findViewById(R.id.tv_weather);	
				holder.tv_temperature = (TextView) convertView.findViewById(R.id.tv_temperature);
				holder.tv_pm = (TextView) convertView.findViewById(R.id.tv_pm);
				holder.tv_week = (TextView) convertView.findViewById(R.id.tv_week);				
				convertView.setTag(holder);
				lView.add(convertView);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			setWeatherImage(holder.imgv_history, weatherData.weather1, weatherData.weather2);
			if (weatherData.real_date != 0) {
				holder.tv_date.setText(getDate(weatherData.date, weatherData.real_date));
				holder.tv_week.setText(getWeek(weatherData.date, weatherData.real_date));
			} else {
				holder.tv_date.setText(getDate(weatherData.date));
				holder.tv_week.setText(getWeek(weatherData.date));
			}
			holder.tv_weather.setText(getWeatherName(weatherData.weather1, weatherData.weather2));
			holder.tv_temperature.setText(weatherData.temperature_low.replaceAll("℃", "") + "~"
					+ weatherData.temperature_high.replaceAll("℃", "") + "℃");
			if (weatherData.pm25 != 0) {
				holder.tv_pm.setText("PM2.5(" + weatherData.pm25 + ")");
			} else {
				holder.tv_pm.setVisibility(View.GONE);
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);				
				params.addRule(ALIGN_PARENT_RIGHT);
				params.addRule(ALIGN_TOP, R.id.tv_week);
				params.rightMargin = (int) getResources().getDimension(R.dimen.whole_content_margin_right);
				holder.tv_temperature.setLayoutParams(params);
			}			
		}
		return convertView;
	}

	private class ViewHighlightHolder
	{
		ImageView imgv_history;
		TextView tv_city;
		TextView tv_date;
		TextView tv_temperature;
		TextView tv_pm;
		TextView tv_week;
		TextView tv_weather;
		TextView tv_wind;
	}
	private class ViewHolder
	{
		ImageView imgv_history;
		TextView tv_date;
		TextView tv_week;
		TextView tv_weather;
		TextView tv_temperature;
		TextView tv_pm;	
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

}
