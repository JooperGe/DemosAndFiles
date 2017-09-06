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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.HotelJsonData;
import com.viash.voicelib.data.PreFormatData.HotelJsonData.HotelData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ImageLoaderUtil;

/**
 * 酒店
 */
@SuppressLint("ViewConstructor")
public class HotelView extends SelectionBaseView {
	private List<HotelData> hotels;

	public HotelView(Context context, PreFormatData data,
			boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
	}

	@Override
	protected void initDataView() {
		super.initDataView();
		setListContentavaible(true);

		//mlsvContent.setDivider(getContext().getResources().getDrawable(R.drawable.bg_line));
		HotelAdapter adapter = new HotelAdapter();
		setAdapter(adapter);
		if (!isFullScreen) {
			setListViewHeight(mlsvContent);
		}
	}


	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_hotel;
		mViewData.mPrimaryTitleText = "酒店";
		mViewData.mSecondaryTitleText = "携程";
        mViewData.mSecondaryTitleImg = R.drawable.icon_xiecheng_logo;
        
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = "";

		HotelJsonData hotelJsonData = (HotelJsonData) ((PreFormatData) mCommunicationData)
				.getJsonData();
		hotels = hotelJsonData.hotels;

		mViewData.mMinItemNumber = 2;
		if (hotels != null) {
			mViewData.mTotleItemNumber = hotels.size();
		} else {
			mViewData.mTotleItemNumber = 0;
		}
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub

	}

	private class HotelAdapter extends BaseAdapter {

		class ViewHolder {
			TextView tv_index;
			ImageView imgv_hotel;
			TextView tv_title;
			TextView tv_address;
			RatingBar rb_rating;
			TextView tv_price;
			TextView tv_des;
			TextView tv_user_rating;
			//String  url;
		}

		@Override
		public int getCount() {
			if (isFullScreen) {
				return mViewData.mTotleItemNumber;
			} else {
				if (mViewData.mTotleItemNumber >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				} else
					return mViewData.mTotleItemNumber;
			}
		}

		@Override
		public Object getItem(int position) {
			return hotels.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("UseValueOf")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			HotelData data = hotels.get(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_hotel_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.imgv_hotel = (ImageView) convertView.findViewById(R.id.imgv_hotel);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
				holder.rb_rating = (RatingBar) convertView.findViewById(R.id.rb_rating);
				holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
				holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
				holder.tv_user_rating = (TextView) convertView.findViewById(R.id.tv_user_rating);
				//holder.url = null;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv_index.setText(String.valueOf((position + 1)));
			
			if (data.name != null)
				holder.tv_title.setText(data.name);
			if (data.floor_price != null) {
				if (!"未知".equals(data.floor_price)) {
					int floor_price = (int) Float.parseFloat(data.floor_price);
					int max_price = (int) Float.parseFloat(data.max_price);
					holder.tv_price.setText("￥" + floor_price + " ~ ￥" + max_price);
				}
			}
			if (data.address != null)
				holder.tv_address.setText(data.address);
			if (data.description != null) {
				String des = data.description;
				des = des.replaceAll("<br/>", "");
				des = des.replaceAll("1.", "");
				des = des.replaceAll("2.", " ");
				holder.tv_des.setText(des);
			}
			if (data.image != null)
				ImageLoaderUtil.loadImageAsync(holder.imgv_hotel, data.image, null,
						getResources().getDrawable(R.drawable.logo), 50,null);
			if (data.user_rating != null)
				holder.tv_user_rating.setText(data.user_rating);
			if (data.ctrip_rating != null) {
				float star = 1.0f;
				try {
					star = Float.valueOf(data.ctrip_rating);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (star >= 0.0 && star <= 10)
					holder.rb_rating.setRating(star);

			}
			if (data.description_url != null && operationEnable) {
				final String url = data.description_url;
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//ViewHolder holder = (ViewHolder) v.getTag();
						Message msg = mHandler.obtainMessage(MsgConst.MSG_SHOW_WEB,	1, 0);
						msg.obj = url;
						mHandler.sendMessage(msg);
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					}
				});
			}

			return convertView;
		}
	}

}
