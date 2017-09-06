package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.CalendarJsonData;
import com.viash.voicelib.data.PreFormatData.CalendarJsonData.CalendarData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CalendarUtil;
import com.viash.voicelib.utils.CalendarUtil.InstanceData;

/**
 * 日程
 * @author Leo Li
 * @createDate 2013-12-16
 */
@SuppressLint("ViewConstructor")
public class CalendarView extends SelectionBaseView implements SelectionBaseView.ISelectionViewListeners {
	private List<CalendarData> mCalendarData;
	private boolean isSystemCalendar = true;
	public CalendarView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
		setOnListeners(this);
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_calendar;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.calendar_remind);
		mViewData.mSecondaryTitleText = null;
		if (!needConfirm) {
			mViewData.mFilterOrCommFunText = mContext.getString(R.string.delete_all);
			mViewData.mCommFunImg = R.drawable.icons_delete_58;
		}
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 5;
		mViewData.mTotleItemNumber = ((CalendarJsonData)mData).mCalendarData.size();
		mViewData.mHighLight = 0;
		List<InstanceData> lstCalendar = CalendarUtil.queryCalendar(mContext, 0, 0);
		if(lstCalendar == null)
		{
			isSystemCalendar = false;
		}else if(lstCalendar.size() == 0)
		{
			isSystemCalendar = false;
		}
	}

	@Override
	protected void initDataView(){
		super.initDataView();
		//if (Datatype == PERFORMATED_DATA_TYPE) {
		mCalendarData = ((CalendarJsonData)mData).mCalendarData;
		/*} else {
			mCalendarData = transferStringDatatoCalenderData(((OptionData)mCommunicationData).getOptions());
		}*/
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}

    private class ContentAdapter extends BaseAdapter
    {

		@Override
		public int getCount() {
			int count = mCalendarData.size();
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
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("UseValueOf")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			CalendarData calendarData = mCalendarData.get(position);
			final int current_position = position; 
			
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_calendar_item, null);
				holder.tv_index = (TextView)convertView.findViewById(R.id.tv_index);
				holder.tv_time = (TextView)convertView.findViewById(R.id.tv_time);
				holder.tv_content = (TextView)convertView.findViewById(R.id.tv_content);
				holder.tv_date = (TextView)convertView.findViewById(R.id.tv_date);
				holder.tv_type = (TextView)convertView.findViewById(R.id.tv_type);
				//holder.position = position;
				convertView.setTag(holder);						
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			String titleString = "";
			for (String attende : calendarData.attendees) {
				titleString = titleString + attende + " ";
			}
			if (calendarData.location != null) {
				titleString = titleString + calendarData.location;
			}
			titleString = titleString + " " + calendarData.title;
			holder.tv_content.setText(titleString);
			SimpleDateFormat dateFormat = new SimpleDateFormat("",
					Locale.SIMPLIFIED_CHINESE);
			Date date ;
			if (!calendarData.start_time.equals(calendarData.end_time)) {
				date= new Date(new Long(calendarData.start_time));
				dateFormat.applyPattern("yyyy/MM/dd/");
				holder.tv_date.setText(dateFormat.format(date)  + getWeek(calendarData.start_time) + "/");
				dateFormat.applyPattern("HH:mm");
				holder.tv_time.setText(dateFormat.format(date));
				date= new Date(new Long(calendarData.end_time));
			} else {
				dateFormat.applyPattern("yyyy/MM/dd/");
				date= new Date(new Long(calendarData.start_time));
				holder.tv_date.setText(dateFormat.format(date)+getWeek(calendarData.start_time));
				dateFormat.applyPattern("HH:mm");
				holder.tv_time.setText(dateFormat.format(date));					
			}
			if (calendarData.selectable == 1) {
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (operationEnable) {
							int tempPosition = 1<<current_position;
							Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, tempPosition, 0);
							mHandler.sendMessage(msg);
						}
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					}
					
				});
			}
			if(isSystemCalendar)
				holder.tv_type.setText("系统");
			else
				holder.tv_type.setText("哦啦");
			return convertView;
		}
    	
		private class ViewHolder
		{
		   TextView tv_index;
		   TextView tv_time;
		   TextView tv_content;
		   TextView tv_date;
		   TextView tv_type;
		   //int	position;
		}
    }
	
    @SuppressLint("UseValueOf")
	public static String getWeek(String time) {
		Date date = new Date(new Long(time));
		String[] weekDaysName = { "周日", "周一", "周二", "周三", "周四", "周五","周六" };
		//String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return weekDaysName[intWeek];

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
			Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT , getResources().getString(R.string.delete_all));
			mHandler.sendMessage(msg);
		}
	}
	
}
