package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;
/**
 * 蓝牙
 * @author Leo Li
 */
@SuppressLint("ViewConstructor")
public class BluetoothView extends SelectionBaseView {
	private List<String> mOptionData;
	public BluetoothView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icon_bluetooth;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.bluetooth_title);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = mContext.getString(R.string.bluetooth_new_search);
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 6;
		mViewData.mTotleItemNumber = ((OptionData) mCommunicationData).getOptions().size();
		mViewData.mHighLight = 0;
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		mOptionData = ((OptionData) mCommunicationData).getOptions();
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}
	
	@Override
	protected void initFilterView(){
		layout_filter.setVisibility(View.VISIBLE);
		mTv_filter.setText(getResources().getString(R.string.bluetooth_new_search));
		mTv_filter.setTextColor(Color.parseColor("#fefefe"));
		mTv_filter.setTextSize(15);
		ImageView iv_filter = (ImageView) layout_selection_base.findViewById(R.id.iv_filter);
		iv_filter.setImageDrawable(getResources().getDrawable(R.drawable.icon_map));
		if(operationEnable)
		{	
			layout_filter.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,getResources().getString(R.string.bluetooth_searching));
					mHandler.sendMessage(msg);
				}
				
			});
		}
	}
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = mOptionData.size();
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
			final int current_position = position;
			
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bluetooth_item, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_ispaired = (TextView) convertView.findViewById(R.id.tv_ispaired);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			String blueToothInfo = mOptionData.get(position);
			if((blueToothInfo != null) && (blueToothInfo.indexOf("$$") > 0))
			{
				String[] itemArray = blueToothInfo.split("\\Q$$\\E");
				holder.tv_name.setText(itemArray[0]);
				if(itemArray.length > 1)
				{
					if(itemArray[1].equals("1"))
						holder.tv_ispaired.setText(getResources().getString(R.string.blue_connected));
					else
						holder.tv_ispaired.setText(getResources().getString(R.string.blue_disconnected));
				}
			 }
			if(operationEnable)
			{
				//holder.index = position;
				convertView.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View v) {
						int tempPosition = 1<<current_position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, tempPosition, 0);
						mHandler.sendMessage(msg);
					}
					
				});
			}
			return convertView;
		}
		
		private class ViewHolder{
			TextView  tv_name;
			TextView  tv_ispaired;
			//int index;
		}
	}
		
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

}
