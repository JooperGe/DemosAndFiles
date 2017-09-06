package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.CallJsonData;
import com.viash.voicelib.data.PreFormatData.CallJsonData.CallData;

/**
 * Call record view
 * 
 * @author Leo
 * @createDate 2013-12-19
 * 
 */
@SuppressLint("ViewConstructor")
public class CallRecordView extends SelectionBaseView {

    private List<CallData> callDatas;
	public CallRecordView(Context context, PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_contact;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.call_record);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 5;
		mViewData.mTotleItemNumber = ((CallJsonData)mData).mCallData.size();
		mViewData.mHighLight = 0;	
	}

	@Override
	protected void initDataView(){
		super.initDataView();
		callDatas = ((CallJsonData)mData).mCallData;
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){			
			setListViewHeight(mlsvContent);			 
		}
	}
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = ((CallJsonData)mData).mCallData.size();
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
			CallData callData = callDatas.get(position);
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_call_record_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_call_type = (ImageView) convertView.findViewById(R.id.imgv_call_type);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_num = (TextView) convertView.findViewById(R.id.tv_num);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_index.setText(Integer.toString(position+1));
			if(callData.contact_name.equals("")){
				holder.tv_name.setVisibility(INVISIBLE);
			}else{
				holder.tv_name.setText(callData.contact_name);
			}
			holder.tv_num.setText(callData.contact_number);
			
			if ("1".equals(callData.call_type)) {
				holder.imgv_call_type.setImageResource(R.drawable.icon_call_to);
			} else if ("2".equals(callData.call_type)) {
				holder.imgv_call_type.setImageResource(R.drawable.icon_call_connect);
			} else if ("3".equals(callData.call_type)) {
				holder.imgv_call_type.setImageResource(R.drawable.icon_call_no_connect);
			}
			
			if (callData.time != null && !"".equals(callData.time)) {
				Date date = new Date(Long.valueOf(callData.time));
				SimpleDateFormat sdf = new SimpleDateFormat("",
						Locale.SIMPLIFIED_CHINESE);
				sdf.applyPattern("MM-dd HH:mm");
				holder.tv_time.setText(sdf.format(date));
			}
			else
			{
				holder.tv_time.setVisibility(View.GONE);
			}
			
			if(operationEnable)
			{
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						TextView call_num = (TextView) v.findViewById(R.id.tv_num);
						Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
								+ call_num.getText().toString()));
						Activity a = (Activity) mContext;
						a.startActivity(intent);
					}				
				});
			}
			return convertView;
		}
		
		private class ViewHolder{
			TextView tv_index;
			ImageView imgv_call_type;
			TextView tv_name;
			TextView tv_num;
			TextView tv_time;		
		}
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
