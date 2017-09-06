package com.viash.voice_assistant.widget;

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
import com.viash.voicelib.data.PreFormatData.PoiReferenceJsonData;
import com.viash.voicelib.data.PreFormatData.PoiReferenceJsonData.PoiReferenceData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class POIReferenceView extends SelectionBaseView {
	//private static final String TAG = "POIReferenceView";
	
	public POIReferenceView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
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
				return ((PoiReferenceJsonData)(((PreFormatData)mCommunicationData).getJsonData())).mPoiReferenceDatas.size();
			}
			else {
				if (((PoiReferenceJsonData)(((PreFormatData)mCommunicationData).getJsonData())).mPoiReferenceDatas.size() >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else{
					return  ((PoiReferenceJsonData)(((PreFormatData)mCommunicationData).getJsonData())).mPoiReferenceDatas.size();
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
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_poi_reference_item, null);

				holder = new ViewHolder();
				holder.index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.distance = (TextView) convertView.findViewById(R.id.tv_distance_info);
				holder.address = (TextView) convertView.findViewById(R.id.tv_address);
				//holder.position = position;

				convertView.setTag(holder);
			} else {				
				holder = (ViewHolder) convertView.getTag();
			}
			PoiReferenceData data = ((PoiReferenceJsonData)(((PreFormatData)mCommunicationData).getJsonData())).mPoiReferenceDatas.get(position);

			holder.index.setText(String.valueOf(position + 1));
			holder.name.setText(data.ref_name);
			holder.distance.setText("相距" + data.ref_distance + "米");
			holder.address.setText(data.ref_address);
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
			TextView distance;
			TextView address;
		}

	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_near;
		mViewData.mPrimaryTitleText = "周边";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber =  ((PoiReferenceJsonData)(((PreFormatData)mCommunicationData).getJsonData())).mPoiReferenceDatas.size();
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	
}
