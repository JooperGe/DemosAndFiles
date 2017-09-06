package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.viash.voicelib.utils.ImageLoaderUtil;
/*
 * 选择新闻
 * @author Leo Li
 * @createDate 2013-12-16
 */
@SuppressLint("ViewConstructor")
public class NewsView extends SelectionBaseView{
	private List<String> mOptionData;
	public NewsView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_news;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.news);
		mViewData.mSecondaryTitleText = null;
		
		//mViewData.mFilterOrCommFunText = mContext.getString(R.string.all_news);
		//mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 2;
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
	
/*	@Override
	protected void initFilterView(){
		layout_filter.setVisibility(View.VISIBLE);
		mTv_filter.setText(getResources().getString(R.string.all_news));
		mTv_filter.setTextColor(Color.parseColor("#fefefe"));
		ImageView iv_filter = (ImageView) layout_selection_base.findViewById(R.id.iv_filter);
		iv_filter.setImageDrawable(getResources().getDrawable(R.drawable.selection_filter_icon));
		if(operationEnable)
		{	
			layout_filter.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
					msg.obj = mCommunicationData;
					mHandler.sendMessage(msg);
					msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,getResources().getString(R.string.all_news));
					mHandler.sendMessage(msg);
				}
				
			});
		}
	}*/
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		/*public int getCount() {
			int count = mOptionData.size();
			if (isFullScreen)
			{
				return 9;
			}else{
				if (count >= mViewData.mMinItemNumber) {
					return 3;
				}
				else {
					return 3;
				}
			} 
		}*/
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
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.selection_news_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_news = (ImageView) convertView.findViewById(R.id.imgv_news);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_index.setText(Integer.toString(position+1));
			String itemContent = mOptionData.get(position);
			if(itemContent!=null && itemContent.indexOf("$$")>0)
			{
				final String[] newsArray=itemContent.split("\\Q$$\\E");
				if(newsArray !=null && newsArray.length > 0)
				{
					if(newsArray[0] != null)
						holder.tv_title.setText(newsArray[0]);
					ImageLoaderUtil.loadImageAsync(holder.imgv_news, newsArray[2], null, getResources().getDrawable(R.drawable.aola_img_default),100,null);
					if(newsArray.length > 3 && newsArray[3] !=null){
						String newsItem = newsArray[3].trim();
						newsItem = newsItem.replaceAll("　", "");
						newsItem = newsItem.replaceAll(" ", "");
						newsItem = newsItem.replaceAll("\\n", "");
						holder.tv_des.setText(newsItem);
					}
				}				
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						if(operationEnable)
						{
							int position = Integer.parseInt((String) ((ViewHolder)v.getTag()).tv_index.getText())-1;
							position =1<<position;
							Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
							mHandler.sendMessage(msg);
							if(isFullScreen)
							   mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
						}
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						mHandler.sendEmptyMessage(MsgConst.MSG_FORCE_STOP_TTS);
					}					
				});
				
			}						
			return convertView;
		}
		
		private class ViewHolder{
			TextView tv_index;
			ImageView imgv_news;
			TextView tv_title;
			TextView tv_des;
		}
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
