package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.MemoJsonData;
import com.viash.voicelib.data.PreFormatData.MemoJsonData.MemoData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 备忘
 * @author Harlan Song
 * 2013-4-12
 */
@SuppressLint("ViewConstructor")
public class MemoView extends SelectionBaseView  implements SelectionBaseView.ISelectionViewListeners {
	private List<MemoData> memoDatas;
	private TextView tvLastScroll = null;
	public MemoView(Context context, PreFormatData data,boolean operationEnable, Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable,mHandler,showInMainScreen);
		setOnListeners(this);
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_note;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.memo);
		mViewData.mSecondaryTitleText = null;
		
		if (!needConfirm) {
			mViewData.mFilterOrCommFunText = mContext.getString(R.string.delete_all);
			mViewData.mCommFunImg = R.drawable.icons_delete_58;
		}
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 5;
		mViewData.mTotleItemNumber = ((MemoJsonData)mData).mMemoData.size();
		mViewData.mHighLight = 0;
		
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		memoDatas = ((MemoJsonData)mData).mMemoData;
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = ((MemoJsonData)mData).mMemoData.size();
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			MemoData memoData = memoDatas.get(position);
			final int current_position = position; 
			final int selectable = memoData.selectable;
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_memo_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				holder.tv_week = (TextView) convertView.findViewById(R.id.tv_week);
				//holder.position = position;
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_index.setText(Integer.toString(position+1));
			String[] array = memoData.date.split("\\Q \\E");
			if(array.length > 1)
			{
				holder.tv_date.setText(array[0]);
				holder.tv_time.setText(array[1]);				
				holder.tv_week.setText(getWeek(array[0]));
			}
			
			holder.tv_content.setText(memoData.content);
			
			convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (operationEnable) {
							if (selectable == 1)
							{
								int tempPosition = 1<<current_position;
								Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, tempPosition, 0);
								mHandler.sendMessage(msg);
							}
							ViewHolder holder = (ViewHolder) v.getTag();
							holder.tv_content.setSingleLine(true);
							holder.tv_content.setEllipsize(TruncateAt.MARQUEE);
							holder.tv_content.setSelected(true);
							if(tvLastScroll != null)
							{
								tvLastScroll.setSelected(false);
								tvLastScroll.setEllipsize(TruncateAt.END);
							}
							tvLastScroll = holder.tv_content;
						}
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						
					}
					
			});
			
			
			return convertView;
			
		}
		
		private class ViewHolder{
			TextView tv_index;
			TextView tv_time;
			TextView tv_content;
			TextView tv_date;
			TextView tv_week;
			//int position;
		}
	}

	public String getWeek(String date) {
		String[] dayOfWeek = { "", "日", "一", "二", "三", "四", "五", "六" };
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("");
		sdf.applyPattern("yy/MM/dd");
		Date dt = new Date(date);
		calendar.setTime(dt);
		return "星期" + dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];
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
