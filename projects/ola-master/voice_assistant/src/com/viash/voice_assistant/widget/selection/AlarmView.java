package com.viash.voice_assistant.widget.selection;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.msg.MsgConst;

/**
 * 闹钟选择
 * @author Harlan Song
 * @createDate 2013-3-16
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class AlarmView extends RelativeLayout{
	private List<String> alarms;
	protected boolean operationEnable;
	protected Handler mHandler;
	private LinearLayout layout_content;
	public AlarmView(Context context,List<String> alarms,boolean operationEnable,Handler mHandler) {
		super(context);
		this.alarms =alarms;
		this.operationEnable = operationEnable;
		this.mHandler = mHandler;
		LayoutInflater.from(getContext()).inflate(R.layout.selection_alarm,this,true);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		if(alarms !=null && alarms.size() > 0 ){
			setData();
		}
		
	}
	private void setData(){
		for (int i = 0; i <alarms.size(); i++) {
			String musicContent=alarms.get(i);
			if(musicContent!=null&&musicContent.indexOf("$$")>0){
				String[] alarmArray=musicContent.split("\\Q$$\\E");
				View alarmView =LayoutInflater.from(getContext()).inflate(R.layout.selection_calendar_item, null);
				if(operationEnable){
					alarmView.setTag(i);
					alarmView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							int position=(Integer) v.getTag();
							position=1<<position;
							Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
							mHandler.sendMessage(msg);
						}
					});
				}
				
				TextView tv_timeLeft = (TextView) alarmView.findViewById(R.id.tv_time_left);
				TextView tv_timeRight = (TextView) alarmView.findViewById(R.id.tv_time_right);
				TextView tv_title = (TextView) alarmView.findViewById(R.id.tv_title);
				TextView tv_week0 = (TextView) alarmView.findViewById(R.id.tv_week0);
				TextView tv_week1 = (TextView) alarmView.findViewById(R.id.tv_week1);
				TextView tv_week2 = (TextView) alarmView.findViewById(R.id.tv_week2);
				TextView tv_week3 = (TextView) alarmView.findViewById(R.id.tv_week3);
				TextView tv_week4 = (TextView) alarmView.findViewById(R.id.tv_week4);
				TextView tv_week5 = (TextView) alarmView.findViewById(R.id.tv_week5);
				TextView tv_week6 = (TextView) alarmView.findViewById(R.id.tv_week6);
				ImageView imgv_am_or_pm = (ImageView) alarmView.findViewById(R.id.imgv_am_or_pm);
				TextView tv_status = (TextView) alarmView.findViewById(R.id.tv_status);
				layout_content.addView(alarmView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				if(i<alarms.size()-1){
					View viewLine = new View(getContext());
					layout_content.addView(viewLine,new LayoutParams(LayoutParams.FILL_PARENT,(int)getResources().getDimension(R.dimen.main_listview_divider_height)));
				}
				
				if(alarmArray.length > 3){
					if("1".equals(alarmArray[3])){
						tv_status.setText("已开启");
					}else{
						tv_status.setText("已关闭");
					}
				}
				if(alarmArray.length > 1)
					tv_title.setText(alarmArray[1]);
				if(alarmArray.length > 0 && alarmArray[0] != null){
					Long long_time =Long.valueOf(alarmArray[0]);
					Date date= new Date(long_time);
					int timeHour = 0;
					if(date.getHours() >= 0 && date.getHours() < 12){
						imgv_am_or_pm.setImageResource(R.drawable.alarm_icon_am);
						timeHour = date.getHours();
					}else if(date.getHours() == 24){
						timeHour = 0;
						imgv_am_or_pm.setImageResource(R.drawable.alarm_icon_am);
					}else{
						imgv_am_or_pm.setImageResource(R.drawable.alarm_icon_pm);
						timeHour = date.getHours() % 12;
						if(timeHour == 0)
							timeHour = 12;
					}
					String time_str = "";
					if(timeHour >9)
						time_str = String.valueOf(timeHour);
					else
						time_str = "0" + timeHour;
					tv_timeLeft.setText(time_str);
					if(date.getMinutes() >9)
						time_str = String.valueOf(date.getMinutes());
					else
						time_str = "0" + date.getMinutes();
					tv_timeRight.setText(time_str);
				}
				if(isInteger(alarmArray[2]) && alarmArray.length >2){
					Map<String, Integer>  map = com.viash.voice_assistant.widget.AlarmView.getShowWeek(Integer.valueOf(alarmArray[2]));
					if(map.get("week0") == 1)
						tv_week0.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week0.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week1") == 1)
						tv_week1.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week1.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week2") == 1)
						tv_week2.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week2.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week3") == 1)
						tv_week3.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week3.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week4") == 1)
						tv_week4.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week4.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week5") == 1)
						tv_week5.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week5.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
					if(map.get("week6") == 1)
						tv_week6.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
					else
						tv_week6.setTextColor(getResources().getColor(R.color.alarm_txt_gray));
				}
			}
		}
	}
	/**
	  * 判断字符串是否是整数
	  */
	 public static boolean isInteger(String value) {
		  try {
		      Integer.parseInt(value);
		      return true;
		   } catch (NumberFormatException e) {
			  return false;
		  }
	 }
}
