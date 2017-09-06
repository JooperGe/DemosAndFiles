package com.viash.voice_assistant.widget.selection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.TVJsonData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class TVView extends SelectionBaseView implements SelectionBaseView.ISelectionViewListeners {
	private static final String TAG = "TVView";
	
	public TVView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		setOnListeners(this);
	}
	@Override
	protected void initDataView(){
		super.initDataView();
		
		setListContentavaible(true);//Content view is a list view.
		setDataView();
	}
	
    private void setDataView()
    {
    	mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
    	mlsvContent.setFooterDividersEnabled(false);
		ContentAdapter adapter = new ContentAdapter();
		mlsvContent.setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
    }
	
	private class ContentAdapter extends BaseAdapter {
	
		@Override
		public int getCount() {
			//if (!mbIsDataReady) {return 0;}
			if (isFullScreen){
				return ((TVJsonData)mData).mTVProgramData.name.length;
			}
			else {
				
				if (((TVJsonData)mData).mTVProgramData.name.length >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else return  ((TVJsonData)mData).mTVProgramData.name.length;
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
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_selection_base_item, null);

				holder = new ViewHolder();
				holder.time = (TextView) convertView.findViewById(R.id.tv_time_item);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name_item);

				convertView.setTag(holder);
			} else {				
				holder = (ViewHolder) convertView.getTag();
			}

			holder.time.setText(((TVJsonData)mData).mTVProgramData.time[position + mIStartNumber]);
			holder.name.setText(((TVJsonData)mData).mTVProgramData.name[position + mIStartNumber]);
			
			long i = ((TVJsonData)mData).mTVProgramData.is_highlight;
			if (i != 0 && (((i >> (position + mIStartNumber)) & 1) == 1)) {
				holder.time.setTextColor(getResources().getColor(R.color.text_notice_content_color));
				holder.name.setTextColor(getResources().getColor(R.color.text_notice_content_color));
			}else {
				holder.time.setTextColor(getResources().getColor(R.color.text_content_color));
				holder.name.setTextColor(getResources().getColor(R.color.text_content_color));
			}
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					if (layout_filter_list.getVisibility() == View.VISIBLE) {
						layout_filter_list.setVisibility(View.GONE);
					}
					
				}
				
			});
			return convertView;
		}

		private class ViewHolder {
			TextView time;
			TextView name;
		}

	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_video;
		mViewData.mPrimaryTitleText = ((TVJsonData)mData).mTVDescriptionData.station_name;
		mViewData.mSecondaryTitleText = "酷控";
		mViewData.mSecondaryTitleImg = R.drawable.fyzb_logo_icon;
		
		/*mViewData.mFilterIndex = ((TVJsonData)mData).mTVDescriptionData.filter;
		mViewData.mFilterOrCommFunText = ((TVJsonData)mData).mTVDescriptionData.filters[mViewData.mFilterIndex];
		mViewData.mFilters = ((TVJsonData)mData).mTVDescriptionData.filters;*/
		
		//mViewData.mContentFunImg = R.drawable.icons_play_58;
		
		mViewData.mMinItemNumber = 6;
		mViewData.mTotleItemNumber = ((TVJsonData)mData).mTVProgramData.name.length;
		mViewData.mHighLight = ((TVJsonData)mData).mTVProgramData.is_highlight;
	}

	@Override
	public void onContentViewClickListener(View v) {
		Log.i(TAG, "Play button clicked");
		String url = ((TVJsonData)mData).mTVDescriptionData.url;
		Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_WEB, 1, 0);
		msg.obj=url;
		mHandler.sendMessage(msg);	
	}

	@Override
	public void onFilterItemClickListener(View v) {
		int tag = (Integer) v.getTag();
		String tagString = mViewData.mFilters[tag];
		String subString = tagString.substring(0, tagString.indexOf(" 星期")-1);
		String msgString = subString.replace('-', '月') + "日的节目预告。";
		
		Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
		msg.obj = ((TVJsonData)mData).mTVDescriptionData.station_name + msgString;
		mHandler.sendMessage(msg);
		//mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
	}
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onFilterTitleClickListener(View v) {
		// TODO Auto-generated method stub
		
	}
}
