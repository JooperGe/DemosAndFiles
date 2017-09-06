package com.viash.voice_assistant.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.AlarmJsonData;
import com.viash.voicelib.data.PreFormatData.AlarmJsonData.AlarmData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.AlarmUtil;

/**
 * 闹钟
 */
@SuppressLint("ViewConstructor")
public class AlarmView extends SelectionBaseView implements SelectionBaseView.ISelectionViewListeners{
	private List<AlarmData> mAlarmData;
	public AlarmView(Context context, PreFormatData data,
			boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		setOnListeners(this);
	}

	@Override
	protected void initDataView() {
		super.initDataView();
		setListContentavaible(true);

		//mlsvContent.setDivider(getContext().getResources().getDrawable(R.drawable.bg_line));
		AlarmAdapter adapter = new AlarmAdapter();
		setAdapter(adapter);
		if (!isFullScreen) {
			setListViewHeight(mlsvContent);
		}
	}
	
	private class AlarmAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = mAlarmData.size();
			if (isFullScreen)
			{
				return count;
			}else{
				if (count >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else {
					return count;
				}
			}
		}

		@Override
		public Object getItem(int position) {
			return mAlarmData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView =  LayoutInflater.from(getContext()).inflate(R.layout.layout_alarm_item,null);
			TextView tv_index = (TextView) convertView.findViewById(R.id.tv_index);
			TextView tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			TextView tv_week0 = (TextView) convertView.findViewById(R.id.tv_week0);
			TextView tv_week1 = (TextView) convertView.findViewById(R.id.tv_week1);
			TextView tv_week2 = (TextView) convertView.findViewById(R.id.tv_week2);
			TextView tv_week3 = (TextView) convertView.findViewById(R.id.tv_week3);
			TextView tv_week4 = (TextView) convertView.findViewById(R.id.tv_week4);
			TextView tv_week5 = (TextView) convertView.findViewById(R.id.tv_week5);
			TextView tv_week6 = (TextView) convertView.findViewById(R.id.tv_week6);
			tv_index.setText(Integer.toString(position+1));
			//CustomSwitchButton customWitchOpen = (CustomSwitchButton) convertView.findViewById(R.id.switchbtn_open);
			final AlarmData alarmData = mAlarmData.get(position);
			final ImageButton  btnOn = (ImageButton) convertView.findViewById(R.id.btn_alarm_on);
			final  ImageButton btnOff = (ImageButton) convertView.findViewById(R.id.btn_alarm_off);
	
			if(alarmData.enabled == 1){
				btnOn.setBackgroundResource(R.drawable.btn_alarm_on_selected);
				btnOff.setBackgroundResource(R.drawable.btn_alarm_off_normal);
			}else{
				btnOn.setBackgroundResource(R.drawable.btn_alarm_on_normal);
				btnOff.setBackgroundResource(R.drawable.btn_alarm_off_selected);
			}
			btnOff.setEnabled(false);
			btnOn.setEnabled(false);
		/*	btnOn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(alarmData.enabled == 0){
						AlarmUtil.modifyAlarm(mContext, alarmData.id, alarmData.time,Integer.parseInt(alarmData.repeat) , alarmData.title, (Integer.parseInt(alarmData.vibrate) != 0 ? true:false), true);
						alarmData.enabled = 1;
						btnOn.setBackgroundResource(R.drawable.btn_alarm_on_selected);
						btnOff.setBackgroundResource(R.drawable.btn_alarm_off_normal);
					}
				}
			});
			btnOff.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(alarmData.enabled == 1){
						AlarmUtil.modifyAlarm(mContext, alarmData.id, alarmData.time,Integer.parseInt(alarmData.repeat) , alarmData.title, (Integer.parseInt(alarmData.vibrate) != 0 ? true:false),false);
						alarmData.enabled = 0;
						btnOn.setBackgroundResource(R.drawable.btn_alarm_on_normal);
						btnOff.setBackgroundResource(R.drawable.btn_alarm_off_selected);
					}
				}
			});*/
			/*if(operationEnable){
				customWitchOpen.setTag(alarmData);
				customWitchOpen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						AlarmData alarmItem = (AlarmData) buttonView.getTag();
						if(alarmItem.id != 0){
							AlarmUtil.updateEnableState(getContext(), alarmItem.id, !isChecked);
						}else{
							int alarmId = AlarmUtil.queryAlarmAtTime(getContext(),Integer.valueOf(alarmItem.time), alarmItem.title, Integer.valueOf(alarmItem.repeat));
							if( alarmId  != 0 ){
								alarmItem.id =alarmId;
								AlarmUtil.updateEnableState(getContext(), alarmItem.id, !isChecked);
							}
						}
						alarmItem.enabled = isChecked? 0:1;
						buttonView.setTag(alarmItem);
						Message msg = mHandler.obtainMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						mHandler.sendMessage(msg);
					}
				});
			}else{
				customWitchOpen.setEnabled(false);
			}
			if(alarmData.enabled ==0){
				customWitchOpen.setChecked(true);
			}else{
				customWitchOpen.setChecked(false);
			}*/
			int time = alarmData.time;
			if(alarmData.title != null && !"".equals(alarmData.title.trim()))
				tv_title.setText(alarmData.title);
			else
				tv_title.setText(null);
			if(time >= 0 && time < 1440){
				tv_time.setText(getLeftTime(time) +":" + getRightTime(time) );
			}else{
				tv_time.setText(getLeftTime(alarmData.time / 2) + ":" + getRightTime(alarmData.time / 2 ));
			}
			if(alarmData.repeat !=null && !"0".equals(alarmData.repeat)){
				Map<String, Integer>  map = getShowWeek(Integer.valueOf(alarmData.repeat));
				if(map.get("week0") == 1)
					tv_week0.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week0.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week1") == 1)
					tv_week1.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week1.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week2") == 1)
					tv_week2.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week2.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week3") == 1)
					tv_week3.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week3.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week4") == 1)
					tv_week4.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week4.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week5") == 1)
					tv_week5.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week5.setTextColor(getResources().getColor(R.color.text_content_color));
				if(map.get("week6") == 1)
					tv_week6.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
				else
					tv_week6.setTextColor(getResources().getColor(R.color.text_content_color));
			}
			if (alarmData.selectable == 1) {
				convertView.setTag(position);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (operationEnable) {
							int tempPosition = 1<<((Integer)v.getTag());
							Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, tempPosition, 0);
							mHandler.sendMessage(msg);
						}
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					}
					
				});
			}
			return convertView;
		}
	}
	public static  Map<String, Integer> getShowWeek(int repeat){
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("week0", 0);
		map.put("week1", 0);
		map.put("week2", 0);
		map.put("week3", 0);
		map.put("week4", 0);
		map.put("week5", 0);
		map.put("week6", 0);
		if(repeat != 0){			
			if(repeat == 31)
			{
				map.put("week1", 1);
				map.put("week2", 1);
				map.put("week3", 1);
				map.put("week4", 1);
				map.put("week5", 1);
			}
			else if(repeat == 96)
			{
				map.put("week0", 1);
				map.put("week6", 1);
			}
			else if(repeat == 127)
			{
				map.put("week0", 1);
				map.put("week1", 1);
				map.put("week2", 1);
				map.put("week3", 1);
				map.put("week4", 1);
				map.put("week5", 1);
				map.put("week6", 1);
			}
			else
			{			
				for(int i = 0; i < 7; i++)
				{
					if((repeat & ( 1 << i)) != 0)
					{
						map.put("week" + ((i + 1)% 7), 1);						
					}
				}
			}		
		}else{
			
		}
		return map;
		
	}
	
	public static  String getWeek(int repeat){
		String result="";
		if(repeat != 0){			
			String[] weekArray ={"一","二","三","四","五","六","日"};
			if(repeat == 31)
			{
				result ="工作日";
			}
			else if(repeat == 96)
			{
				result ="周末";
			}
			else if(repeat == 127)
			{
				result ="每天";
			}
			else
			{			
				result += "周";
				for(int i = 0; i < 7; i++)
				{
					if((repeat & ( 1 << i)) != 0)
					{
						result += weekArray[i];
					}
				}
			}		
		}else{
			result ="单次";
		}
		return result;
	}
	
	public static String getLeftTime(int time){
		String time_str ="";
		int hours = time / 60;
		if(hours<10){
			time_str = "0" + hours ;
		}else{
			time_str = "" + hours ;
		}
		return time_str;
	}
	
	public static String getRightTime(int time){
		String time_str ="";
		int minutes = (time % 60);
		if(minutes < 10){
			time_str = "0" + minutes;
		}else{
			time_str = "" + minutes;
		}
		return time_str;
	}
	
	
	public static String getTime(String time){
		int time_int=Integer.valueOf(time);
		String time_str ="";
		if(time_int / 60 == 0){
			time_str += "00";
		}else{
			int hours = time_int / 60;
			if(hours<10){
				time_str += "0" + hours ;
			}else{
				time_str += hours ;
			}
		}
		time_str +=":";
		if(time_int % 60 == 0){
			time_str += "00";
		}else if(time_int %60 < 10){
			time_str +="0" + time_int % 60;
		}else{
			time_str += time_int % 60;
		}
		return time_str;
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_alarm;
		mViewData.mPrimaryTitleText = "闹钟";
		mViewData.mSecondaryTitleText = "";

		if (!needConfirm) {
			mViewData.mFilterOrCommFunText = mContext.getString(R.string.delete_all);
			mViewData.mCommFunImg = R.drawable.icons_delete_58;
		}

		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = "";

		AlarmJsonData alarmJsonData = (AlarmJsonData) ((PreFormatData) mCommunicationData).getJsonData();
		mAlarmData = alarmJsonData.mAlarmData;

		mViewData.mMinItemNumber = 3;
		if (mAlarmData != null) {
			mViewData.mTotleItemNumber = mAlarmData.size();
		} else {
			mViewData.mTotleItemNumber = 0;
		}
		mViewData.mHighLight = 0;		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContentViewClickListener(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFilterItemClickListener(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFilterTitleClickListener(View v) {
		if(operationEnable)
		{			
			String str = getResources().getString(R.string.delete_all);
			Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,str);
			mHandler.sendMessage(msg);	
		}
	}
}
