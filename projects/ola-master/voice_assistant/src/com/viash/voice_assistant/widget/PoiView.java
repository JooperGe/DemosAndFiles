package com.viash.voice_assistant.widget;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.AppData.MapInfo;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.PoiJsonData;
import com.viash.voicelib.data.PreFormatData.PoiJsonData.PoiItemData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;


/**
 * 兴趣点
 * @author Leo Li
 */
@SuppressLint("ViewConstructor")
public class PoiView extends SelectionBaseView {
	private List<PoiItemData> poiDataList;
	private int[]mExpanded;
	public PoiView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
	
	public static void setTextViewLeftIcon (TextView textview,String text,Drawable icon){
		BitmapDrawable bd = (BitmapDrawable) icon;
		Bitmap bm = bd.getBitmap();
		icon.setBounds(0, 0,bm.getWidth(), bm.getHeight());
		textview.setCompoundDrawables(icon, null, null, null);
		textview.setText(text);
		textview.setPadding(0, 5, 10, 5);
		textview.setGravity(Gravity.CENTER_VERTICAL);
	}
	
	protected void showMap(PoiItemData data)
	{
		PoiItemData itemData = data;
		MapInfo info= new MapInfo();
		info.mPoiId = new String[]{itemData.poi_id};
		//info.mAddress = itemData.address;
		//info.mLatitude = itemData.latitude;
		//info.mLongitude = itemData.longitude;
		info.mPoiLatitude = new double[]{itemData.poi_latitude};
		info.mPoiLongitude = new double[]{itemData.poi_longitude};
		info.mPoiSnippet = new String[]{itemData.poi_snippet};
		info.mPoiTitle = new String[]{itemData.poi_title};
		
		if(itemData.poi_latitude == 0 && itemData.poi_longitude == 0)
		{
			Message msg = mHandler.obtainMessage(MsgConst.MSG_SEARCH_POS);
			msg.obj=info;
			mHandler.sendMessage(msg);
		}
		else
		{
			Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_MAP);
			msg.obj=info;
			mHandler.sendMessage(msg);
		}
	}
	
	protected void navigate(PoiItemData data)
	{
		Message msg;
		if (data.poi_latitude != 0 && data.poi_longitude != 0){
			LatLng point = new LatLng(data.poi_latitude,data.poi_longitude);
			msg = mHandler.obtainMessage(MsgConst.MSG_NAVIGATE_TO, 0);
			msg.obj = point;
		}else {
			msg = mHandler.obtainMessage(MsgConst.MSG_NAVIGATE_TO, 1);
			msg.obj = data.poi_snippet;
		} 
		mHandler.sendMessage(msg);
	}
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_near;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.poi_title);
		poiDataList = ((PoiJsonData)mData).mLstPoi;
		if(poiDataList != null)
		{
		   PoiItemData poiItemData = poiDataList.get(0);
		   
		   if(poiItemData.source == 1)
		   {
			   mViewData.mSecondaryTitleText = "高德地图";
			   mViewData.mSecondaryTitleImg = R.drawable.poi_logo_gaode;
		   }
		   else if(poiItemData.source == 2)
		   {
			   mViewData.mSecondaryTitleText = "大众点评";
			   mViewData.mSecondaryTitleImg = R.drawable.poi_logo_dzdp;
		   }
		}		
		/*DescriptionData descriptionData= ((PoiJsonData)mData).mDescriptionData;
		if (((PoiJsonData)mData).mDescriptionData!= null) {
			mViewData.mFilterIndex = 
			mViewData.mFilterOrCommFunText = mContext.getString(R.string.default_sort);
			mViewData.mFilters = null;
		}*/
		
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((PoiJsonData)mData).mLstPoi.size();
		mViewData.mHighLight = 0;
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		poiDataList = ((PoiJsonData)mData).mLstPoi;
		setListContentavaible(true);//Content view is a list view.
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		mExpanded = new int[mViewData.mTotleItemNumber];
		ContentAdapter adapter = new ContentAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
	}
	
	private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int count = poiDataList.size();
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
			PoiItemData poiItemData = poiDataList.get(position);
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_poi_item, null);
				holder.layout_item = (RelativeLayout) convertView.findViewById(R.id.layout_item);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_youhui = (TextView) convertView.findViewById(R.id.tv_youhui);
				holder.rb = (RatingBar) convertView.findViewById(R.id.rb);
				holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);
				holder.tv_telephone = (TextView) convertView.findViewById(R.id.tv_telephone);
				holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
				holder.img_more = (ImageView) convertView.findViewById(R.id.img_more);
				holder.layout_item_onclick = (RelativeLayout) convertView.findViewById(R.id.layout_item_onclick);
				holder.img_more_onclick = (ImageView) convertView.findViewById(R.id.img_more_onclick);
				holder.img_navigation = (ImageView) convertView.findViewById(R.id.img_navigation);
				holder.img_telephone = (ImageView) convertView.findViewById(R.id.img_telephone);
				holder.img_detail = (ImageView) convertView.findViewById(R.id.img_detail);
				holder.img_map = (ImageView) convertView.findViewById(R.id.img_map);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_index.setText(Integer.toString(position+1));
			holder.tv_title.setText(poiItemData.poi_title);
			
			if(poiItemData.coupon_url !=null && poiItemData.coupon_url.length() > 0){
				holder.tv_youhui.setVisibility(View.VISIBLE);
				holder.tv_youhui.setTag(poiItemData.coupon_url);
				if(operationEnable)
				{	
					holder.tv_youhui.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String url = (String) v.getTag();
							mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
							Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_INTERNAL_WEB);
							msg.obj=url;
							mHandler.sendMessage(msg);
						}
					});
				}
			}else{
				holder.tv_youhui.setVisibility(View.GONE);
			}
			
			if(poiItemData.poi_avg_rating != 0)
			{
				float rating_db=(float)poiItemData.poi_avg_rating;
				rating_db = poiItemData.poi_avg_rating / 2.0f;
				holder.rb.setRating(rating_db);
			}
			else
			{
				holder.rb.setVisibility(View.INVISIBLE);
			}
			
			if(poiItemData.poi_distance!=null){
				double distance = Double.valueOf(poiItemData.poi_distance);
				if(distance>=1000){
					//double distance_db=(double)distance;
					distance /=1000;
					String distance_str=String.valueOf(distance);
					int tag_str=distance_str.indexOf(".");
					distance_str=distance_str.substring(0, tag_str+2);
					holder.tv_distance.setText(getResources().getString(R.string.apart)+distance_str + "km");
				}else{
					int distance_int = (int) distance;
					holder.tv_distance.setText(getResources().getString(R.string.apart)+distance_int + "m");
				}
			}
			else
			{
				holder.tv_distance.setText(getResources().getString(R.string.apart)+getResources().getString(R.string.unknow));
			}
			
			holder.tv_telephone.setText(poiItemData.poi_telephone);
			holder.tv_address.setText(poiItemData.poi_snippet);
			
			if(mExpanded[position] == 1)
				holder.layout_item_onclick.setVisibility(View.VISIBLE);
			else
				holder.layout_item_onclick.setVisibility(View.GONE);
			//if(operationEnable)
			{	
				holder.layout_item.setTag(holder);
				holder.layout_item.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							ViewHolder holderTemp = (ViewHolder) v.getTag();
							if(holderTemp.layout_item_onclick.getVisibility() == View.GONE)
							{
								/*int position = Integer.parseInt(holderTemp.tv_index.getText().toString())-1;
								PoiItemData poiData = poiDataList.get(position);
								String url = poiData.poi_url;
								mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
								if(url != null)
								{
									Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_INTERNAL_WEB);
									msg.obj=url;
									mHandler.sendMessage(msg);
								}
								else
								{
									CustomToast.showShortText(getContext(), getResources().getString(R.string.poi_no_detail));
								}*/
								Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_right_to_left);
								holderTemp.layout_item_onclick.startAnimation(animation);
								holderTemp.layout_item_onclick.setVisibility(View.VISIBLE);
								mExpanded[Integer.parseInt(holderTemp.tv_index.getText().toString())-1] = 1;
							}
						}
					}
					
				});
				
				holder.img_more.setTag(holder);
				holder.img_more.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							ViewHolder holderTemp = (ViewHolder) v.getTag();
							if(holderTemp.layout_item_onclick.getVisibility() == View.GONE)
							{
								Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_right_to_left);
								holderTemp.layout_item_onclick.startAnimation(animation);
								holderTemp.layout_item_onclick.setVisibility(View.VISIBLE);
								mExpanded[Integer.parseInt(holderTemp.tv_index.getText().toString())-1] = 1;
							}
						}
					}
					
				});
				
				holder.img_more_onclick.setTag(holder);
				holder.img_more_onclick.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							ViewHolder holderTemp = (ViewHolder) v.getTag();
							if(holderTemp.layout_item_onclick.getVisibility() == View.VISIBLE)
							{
								Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_left_to_right);
								holderTemp.layout_item_onclick.startAnimation(animation);
								holderTemp.layout_item_onclick.setVisibility(View.GONE);
								mExpanded[Integer.parseInt(holderTemp.tv_index.getText().toString())-1] = 0;
							}
						}
					}
					
				});
				
				holder.img_navigation.setTag(position);
				holder.img_navigation.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							int position = (Integer) v.getTag();
							navigate(poiDataList.get(position));
						}
					}				
				});
				
				holder.img_telephone.setTag(position);
				holder.img_telephone.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							int position = (Integer) v.getTag();
							PoiItemData poiData = poiDataList.get(position);
							mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
							String telephone = poiData.poi_telephone;
							if(telephone.indexOf(";") > 0)
								telephone =telephone.split(";")[0];
							if(telephone.indexOf(",") > 0)
								telephone =telephone.split(",")[0];
							if(telephone.indexOf("，") > 0)
								telephone =telephone.split("，")[0];
							if(telephone.indexOf("；") > 0)
								telephone =telephone.split("；")[0];
							Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+telephone));
							getContext().startActivity(intent);
						}
					}				
				});
				
				holder.img_detail.setTag(position);
				holder.img_detail.setOnClickListener(new OnClickListener(){
					
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							int position = (Integer) v.getTag();
							PoiItemData poiData = poiDataList.get(position);
							String url = poiData.poi_url;
							mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
							if(url != null)
							{
								Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_INTERNAL_WEB);
								msg.obj=url;
								mHandler.sendMessage(msg);
							}
							else
							{
								CustomToast.showShortText(getContext(), getResources().getString(R.string.poi_no_detail));
							}
						}
					}				
				});
				
				holder.img_map.setTag(position);
				holder.img_map.setOnClickListener(new OnClickListener(){
					
					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
						if(operationEnable)
						{
							int position = (Integer) v.getTag();
							PoiItemData poiData = poiDataList.get(position);
							mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
							if((poiData.poi_latitude != 0 && poiData.poi_longitude != 0) || (poiData.poi_snippet != null && poiData.poi_snippet.length() > 0))
								showMap(poiData);
							else
								CustomToast.showShortText(getContext(), getResources().getString(R.string.no_address_info));
						}
					}				
				});
			}
			return convertView;
		}	
	}
	
	private class ViewHolder
	{
		RelativeLayout layout_item;
		TextView tv_index;
		TextView tv_title;
		TextView tv_youhui;
		RatingBar rb;
		TextView tv_distance;
		TextView tv_telephone;
		TextView tv_address;
		ImageView img_more;
		RelativeLayout layout_item_onclick;
		ImageView img_more_onclick;
		ImageView img_navigation;
		ImageView img_telephone;
		ImageView img_detail;
		ImageView img_map;	
	}
	
	/*@Override
	protected void initFilterView(){
		super.initFilterView();
		layout_filter.setVisibility(View.VISIBLE);
		mTv_filter.setText(getResources().getString(R.string.default_sort));
		mTv_filter.setTextColor(Color.parseColor("#fefefe"));
		mTv_filter.setTextSize(15);
		ImageView iv_filter = (ImageView) layout_selection_base.findViewById(R.id.iv_filter);
		iv_filter.setImageDrawable(getResources().getDrawable(R.drawable.selection_filter_icon));
		if(operationEnable)
		{	
			layout_filter.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					
				}
				
			});
		}
	}*/

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}	
}
