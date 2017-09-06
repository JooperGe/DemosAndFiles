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
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class PersionEncyclopedia_SelectionView extends SelectionBaseView {
	private static final String TAG = "PersionEncyclopedia_SelectionView";
	
	public PersionEncyclopedia_SelectionView(Context context, OptionData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		
		setListContentavaible(true);//Content view is a list view.
		
		//mlsvContent.setDivider(mContext.getResources().getDrawable(R.drawable.bg_line));
		ContextAdapter adapter = new ContextAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}
	
	private class ContextAdapter extends BaseAdapter {
				
		@Override
		public int getCount() {
			if (isFullScreen){
				return ((OptionData)mCommunicationData).getOptions().size();
			}
			else {
				if (((OptionData)mCommunicationData).getOptions().size() >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else{
					return  ((OptionData)mCommunicationData).getOptions().size();
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
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_person_encyclopedia_item, null);

				holder = new ViewHolder();
				holder.index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.name = (TextView) convertView.findViewById(R.id.tv_person_name);
				holder.info = (TextView) convertView.findViewById(R.id.tv_person_info);
				holder.periods = (TextView) convertView.findViewById(R.id.tv_periods);
				//holder.position = position;

				convertView.setTag(holder);
			} else {				
				holder = (ViewHolder) convertView.getTag();
			}
			String item = ((OptionData)mCommunicationData).getOptions().get(position);
			String[] array = null;
			if(item!=null && item.indexOf("$$")>0){
				array = item.split("\\Q$$\\E");
				if (array == null) {
					Log.e(TAG, "Message format error");
					return convertView;
				} 
				/*if (array.length != 6) {
					Log.e(TAG, "Message format error");
					return convertView;
				}*/
			}

			holder.index.setText(String.valueOf(position + 1));
			holder.name.setText(array[0]);
			
			if (array.length != 6) {
				holder.periods.setVisibility(View.GONE);
				String text = "";
				for (int i = 1; i <array.length; i ++) {
					text = text + array[i] + " ";
				}
				holder.info.setText(text);
			}
			else {
				holder.info.setText(array[2] + " " + array[5] + " " +array[1]);
				holder.periods.setText(array[3] + "--" + array[4]);
			}
			//holder.position = position;
			
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					if (operationEnable) {
						//ViewHolder holder=( ViewHolder) v.getTag();
						int position =1<<current_position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						mHandler.sendMessage(msg);
						mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
					}
				}
				
			});
			return convertView;
		}

		private class ViewHolder {
			//int position;
			TextView index;
			TextView name;
			TextView info;
			TextView periods;
		}

	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_baike;
		mViewData.mPrimaryTitleText = getResources().getString(R.string.encyclopedia);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber =  ((OptionData)mCommunicationData).getOptions().size();;
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
}
