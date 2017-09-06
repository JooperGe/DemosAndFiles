package com.viash.voice_assistant.widget;

import java.util.List;

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
import com.viash.voicelib.data.PreFormatData.RouteJsonData;
import com.viash.voicelib.data.PreFormatData.RouteJsonData.RouteData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 路线
 * @author Harlan Song
 * @createDate 2013-2-5
 */
@SuppressLint("ViewConstructor")
public class RouteView extends SelectionBaseView {
	private List<RouteData> mLstRouteData;
	public RouteView(Context context, PreFormatData data,boolean operationEnable, Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable,mHandler,showInMainScreen);
	}
		
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_navigation;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.navigation);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((RouteJsonData)mData).mLstRouteData.size();
		mViewData.mHighLight = 0;
		
	}

	@Override
	protected void initDataView(){
		super.initDataView();
		mLstRouteData = ((RouteJsonData)mData).mLstRouteData;
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
			int count = ((RouteJsonData)mData).mLstRouteData.size();
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
			RouteData routeData = mLstRouteData.get(position);
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_route_item, null);
				holder = new ViewHolder();
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_end_location = (TextView) convertView.findViewById(R.id.tv_end_location);
				holder.tv_end_address = (TextView) convertView.findViewById(R.id.tv_end_address);
				holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_end_distance);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_index.setText(Integer.toString(position+1));
			holder.tv_end_location.setText(routeData.to_name);
			if(routeData.distance != null){
				holder.tv_distance.setText("相距：" + routeData.distance);
			} else {
				holder.tv_distance.setVisibility(View.GONE);//convertView.findViewById(R.id.img_goto).setVisibility(View.GONE);	
			}
			
			if (routeData.address != null && !routeData.address.equals("")) {
				holder.tv_end_address.setText("地址：" + routeData.address);
			} else {
				
				if(routeData.way != null && !routeData.way.equals("")){
					holder.tv_end_address.setText("途经："+routeData.way);
				}else{
					holder.tv_end_address.setText("");
				}
			}

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					if (operationEnable) {
						String str = "导航到第"
								+ ((ViewHolder) v.getTag()).tv_index.getText()
								+ "个";
						Message msg = mHandler.obtainMessage(
								MsgConst.MSG_DATA_FROM_TEXT, str);
						mHandler.sendMessage(msg);
					}
				}
			});
			return convertView;
		}
		private class ViewHolder{
			TextView tv_index;
			TextView tv_end_location;
			TextView tv_end_address;
			TextView tv_distance;
		}
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
