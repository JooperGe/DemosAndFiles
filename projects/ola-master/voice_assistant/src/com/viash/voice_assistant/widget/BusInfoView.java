package com.viash.voice_assistant.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.BusInfoJsonData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 公交信息
 * @author Leo 
 * @createDate 2014-01-10
 */
@SuppressLint("ViewConstructor")
public class BusInfoView extends SelectionBaseView implements SelectionBaseView.ISelectionViewListeners  {	
	
	private TextView tv_start;
	private TextView tv_end;
	private TextView tv_first;
	private TextView tv_last;
	private TextView tv_mils;
	private ImageView imgv_map;
	private TextView tv_start_station;
	private TextView tv_pass;	
	
	private BusInfoJsonData.BusInfoData mBusinfodata = null; 
		
	public BusInfoView(Context context, PreFormatData data, boolean operationAble, Handler handler, boolean isFullScreen) {
		super(context, data, operationAble, handler, isFullScreen);
		setOnListeners(this);
		BusInfoJsonData jsondata = (BusInfoJsonData)data.getJsonData();
		if ( jsondata != null && jsondata.mData != null) {
			mBusinfodata = jsondata.mData;
		}
		
		updateFilterView();
	}
	
	@Override
	protected void initDataView(){
		setListContentavaible(false);//Content view is NOT a list view.		
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bus_info_item, null);
		tv_start = (TextView) itemView.findViewById(R.id.tv_start);
		tv_end = (TextView) itemView.findViewById(R.id.tv_end);
		tv_first = (TextView) itemView.findViewById(R.id.tv_first);
		tv_last = (TextView) itemView.findViewById(R.id.tv_last);
		tv_mils = (TextView) itemView.findViewById(R.id.tv_mils);
		imgv_map = (ImageView) itemView.findViewById(R.id.imgv_map);
		tv_start_station = (TextView) itemView.findViewById(R.id.tv_start_station);
		tv_pass = (TextView) itemView.findViewById(R.id.tv_pass);
		mNormalContent.addView(itemView);
	}

	@Override
	protected void initSelectionViewData() {
		super.initDataView();
		mViewData.mPrimaryTitleImg = R.drawable.icons_bus;
		mViewData.mPrimaryTitleText = ((BusInfoJsonData)mData).mBusDescriptionData.bus_name;
		mViewData.mSecondaryTitleText = null;
		
		/*mViewData.mFilterIndex = ((BusInfoJsonData)mData).mDescriptionData.filter;
		mViewData.mFilterOrCommFunText = ((BusInfoJsonData)mData).mDescriptionData.filters[mViewData.mFilterIndex];
		mViewData.mFilters = ((BusInfoJsonData)mData).mDescriptionData.filters;*/
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 0;
		mViewData.mTotleItemNumber = 0;
		mViewData.mHighLight = 0;
	}

	@Override
	public void onContentViewClickListener(View v) {}

	@Override
	public void onFilterItemClickListener(View v) {
		mViewData.mFilterIndex = (Integer) v.getTag();
		updateFilterView();
	}

	private void updateFilterView() {
		int index = mViewData.mFilterIndex;
		tv_start.setText(mBusinfodata.bus_start[index]);
		tv_end.setText(mBusinfodata.bus_end[index]);
		tv_first.setText(this.getResources().getString(R.string.businfo_early_bus) + mBusinfodata.early_bus[index]);
		tv_last.setText(this.getResources().getString(R.string.businfo_last_bus) + mBusinfodata.last_bus[index]);
		tv_mils.setText(mBusinfodata.mileage[index]);
		tv_start_station.setText("始发："+mBusinfodata.bus_start[index]);
		tv_pass.setText(mBusinfodata.stop_names[index]);
		imgv_map.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_MAP_BUSINFO);
				msg.arg1 =  mViewData.mFilterIndex;
				msg.obj = mData;
				mHandler.sendMessage(msg);
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
			}			
		});
	}
	
	/*private SpannableString getClickableSpan(int upDownValue) {
		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_MAP_BUSINFO);
				msg.arg1 =  mViewData.mFilterIndex;
				msg.obj = mData;
				mHandler.sendMessage(msg);
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
			}
		};
		SpannableString spanableInfo; 
			spanableInfo = new SpannableString(this.getResources().getString(R.string.businfo_mileage) 
					+ mBusinfodata.mileage[upDownValue] + this.getResources().getString(R.string.businfo_show_map));
		
		int start = spanableInfo.length() - 7;
		int end = spanableInfo.length()-1;
		spanableInfo.setSpan(new Clickable(l), start, end,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spanableInfo;
	}
	
	class Clickable extends ClickableSpan implements OnClickListener {
		private final View.OnClickListener mListener;

		public Clickable(View.OnClickListener l) {
			mListener = l;
		}

		@Override
		public void onClick(View v) {
			mListener.onClick(v);
		}
	}*/

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		/*mViewData.mFilterIndex = 1;
		updateFilterView();*/
	}

	@Override
	public void onFilterTitleClickListener(View v) {
		// TODO Auto-generated method stub
		
	}
}
