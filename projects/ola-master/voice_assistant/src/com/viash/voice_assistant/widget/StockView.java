package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.StockJsonData;
import com.viash.voicelib.data.PreFormatData.StockJsonData.StockData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class StockView extends SelectionBaseView {
	private List<StockData> mStockData;
	//private ListView lv_stock;
	private LinearLayout layout_not_history;
	
	private TextView tv_name;
	private TextView tv_cur_price;
	private TextView tv_price_start;
	private TextView tv_price_end;
	private TextView tv_price_high;
	private TextView tv_price_low;
	private TextView tv_change_rate;
	private TextView tv_update_time;
	private TextView tv_amount;
	private TextView tv_volume;
	private TextView tv_percent;
	private TextView tv_favorites;
	private ImageView iv_favorites;
	
	private static boolean isFavorite = false; 
	
	
	public StockView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		
		StockJsonData mStockData = (StockJsonData)((PreFormatData)mCommunicationData).getJsonData();
		StockData stockData = null;
		if ( mStockData != null && mStockData.mStockData.size() > 0){
			if (mStockData.mStockData.size() == 1 ) {
				stockData = mStockData.mStockData.get(0);
				updateViewData(stockData);
			}			
		}
	}
	
	@SuppressLint("UseValueOf")
	private void updateViewData(StockData stockData) {
		if (stockData == null) {
			return;
		}		

		if(stockData.is_history == 0){
			layout_not_history.setVisibility(View.VISIBLE);
			tv_change_rate.setVisibility(View.VISIBLE);
			tv_percent.setVisibility(View.VISIBLE);
			if(stockData.change_amount !=null){
				tv_change_rate.setText(stockData.change_amount);
				double amount = Double.valueOf(stockData.change_amount);
				if( amount >= 0.0){
					tv_change_rate.setTextColor(getResources().getColor(R.color.stock_txt_red));
					tv_change_rate.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.stock_icon_price_up, 0);
				}else{
					tv_change_rate.setTextColor(getResources().getColor(R.color.text_security_content_color));
					tv_change_rate.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.stock_icon_price_down, 0);
				}
			}
			
			if(stockData.change_rate !=null){
				tv_percent.setText(stockData.change_rate);
			}
			tv_price_end.setText(stockData.price_end);
			tv_amount.setText(stockData.amount);
		}
		else {
			layout_not_history.setVisibility(View.GONE);
			tv_change_rate.setVisibility(View.INVISIBLE);
			tv_percent.setVisibility(View.INVISIBLE);
		}

		tv_cur_price.setText(stockData.cur_price);
		tv_volume.setText(stockData.volume);
		tv_name.setText(stockData.name +" " + stockData.id);
		
		if (isFavorite) {
			tv_favorites.setText("删除收藏");
			iv_favorites.setImageResource(R.drawable.icons_delete_58);
		}else {
			tv_favorites.setText("加入收藏");
			iv_favorites.setImageResource(R.drawable.icons_add_58);
		}
		
		
		tv_favorites.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (operationEnable) {
					mStockData = ((StockJsonData) ((PreFormatData) mCommunicationData)
							.getJsonData()).mStockData;
					if ((mStockData != null) && (mStockData.size() > 0)) {
						String str = null;
						if (isFavorite) {
							str = "删除" + mStockData.get(0).name;
						} else {
							str = "收藏" + mStockData.get(0).name;
						}
						Message msg = mHandler
								.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
						msg.obj = str;
						mHandler.sendMessage(msg);
					}
				}
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
			}

		});
		
		tv_cur_price.setText(stockData.cur_price);
		tv_price_start.setText(stockData.price_start);
		tv_price_high.setText(stockData.price_high);
		tv_price_low.setText(stockData.price_low);
		
		if(stockData.time!=null && stockData.time.length() > 0){
			Date date = new Date(new Long(stockData.time));
			SimpleDateFormat dateFormat = new SimpleDateFormat("", Locale.TRADITIONAL_CHINESE);
			dateFormat.applyPattern("yyyy-MM-dd HH:mm");
			tv_update_time.setText(dateFormat.format(date));
		}
	}
	
	@Override
	protected void initDataView(){
		if (mViewData.mMinItemNumber <= 0) {
			setListContentavaible(false);//Content view is a pre-formated view.
			View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_stock_item, null);
			layout_not_history = (LinearLayout)itemView.findViewById(R.id.layout_not_history);
			tv_name =(TextView) itemView.findViewById(R.id.tv_name);
			tv_cur_price =(TextView) itemView.findViewById(R.id.tv_cur_price);
			tv_price_start =(TextView) itemView.findViewById(R.id.tv_price_start);
			tv_price_end =(TextView) itemView.findViewById(R.id.tv_price_end);
			tv_price_high =(TextView) itemView.findViewById(R.id.tv_price_high);
			tv_price_low =(TextView) itemView.findViewById(R.id.tv_price_low);
			tv_change_rate =(TextView) itemView.findViewById(R.id.tv_change_rate);
			tv_amount =(TextView) itemView.findViewById(R.id.tv_amount);
			tv_update_time =(TextView) itemView.findViewById(R.id.tv_update_time);
			tv_volume =(TextView) itemView.findViewById(R.id.tv_volume);
			tv_percent =(TextView) itemView.findViewById(R.id.tv_percent);			
			tv_favorites = (TextView) itemView.findViewById(R.id.tv_favorites);
			iv_favorites = (ImageView) itemView.findViewById(R.id.iv_favorites);
			mNormalContent.addView(itemView);
		}
		else {
			super.initDataView();
			setListContentavaible(true);
			
			//mlsvContent.setDivider(getContext().getResources().getDrawable(R.drawable.bg_line));
			StockAdapter adapter = new StockAdapter();
			StockJsonData stockJsonData = (StockJsonData)((PreFormatData)mCommunicationData).getJsonData();
			if (stockJsonData != null) {
				mStockData = stockJsonData.mStockData;
			}
			setAdapter(adapter);
			if (!isFullScreen){
				setListViewHeight(mlsvContent);
			}
		}
		
	}
	
	private class StockAdapter extends BaseAdapter{
		
		class ViewHolder {
			TextView tv_index;
			TextView tv_name;
			TextView tv_cur_price;
			TextView tv_change_rate;
			TextView tv_percent;
			TextView tv_update_time;
			//int 	 position;
		}

		@Override
		public int getCount() {
			if (isFullScreen){
				return mViewData.mTotleItemNumber;
			}
			else {
				if (mViewData.mTotleItemNumber >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else return  mViewData.mTotleItemNumber;
			}
		}

		@Override
		public Object getItem(int position) {
			return mStockData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("UseValueOf")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder;
			final int current_position = position;
			
			StockData stockData = mStockData.get(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_stocklist_item, null);
				holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_name =(TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_cur_price =(TextView) convertView.findViewById(R.id.tv_cur_price);
				holder.tv_change_rate =(TextView) convertView.findViewById(R.id.tv_change_rate);
				holder.tv_percent =(TextView) convertView.findViewById(R.id.tv_percent);
				holder.tv_update_time =(TextView) convertView.findViewById(R.id.tv_update_time);
				//holder.position = position;
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_index.setText(String.valueOf((position + 1)));
			holder.tv_name.setText(stockData.name +" " + stockData.id);
			holder.tv_cur_price.setText(stockData.cur_price);
			
			if(stockData.time!=null && stockData.time.length() > 0){
				Date date = new Date(new Long(stockData.time));
				SimpleDateFormat dateFormat = new SimpleDateFormat("", Locale.TRADITIONAL_CHINESE);
				dateFormat.applyPattern("yyyy-MM-dd HH:mm");
				holder.tv_update_time.setText(dateFormat.format(date));
			}
			
			if(stockData.is_history == 0){
				if(stockData.change_amount !=null){
					holder.tv_change_rate.setText(stockData.change_amount);
					double amount = Double.valueOf(stockData.change_amount);
					if( amount >= 0.0){
						holder.tv_change_rate.setTextColor(getResources().getColor(R.color.stock_txt_red));
						holder.tv_change_rate.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.stock_icon_price_up, 0);
					}else{
						holder.tv_change_rate.setTextColor(getResources().getColor(R.color.text_security_content_color));
						holder.tv_change_rate.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.stock_icon_price_down, 0);
					}
				}
				if(stockData.change_rate !=null){
					holder.tv_percent.setText(stockData.change_rate);
				}
			}
			else {
				holder.tv_change_rate.setText("");
				holder.tv_change_rate.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
				holder.tv_percent.setText(stockData.change_rate);
			}
			if (operationEnable) {
				convertView.setOnClickListener(new OnClickListener () {
	
					@Override
					public void onClick(View v) {
						//ViewHolder holder = (ViewHolder) v.getTag();
						int position = current_position;
						StockData stockData = mStockData.get(position);
						String str = "";
						if(stockData.time!=null && stockData.time.length() > 0){
							Date date = new Date(new Long(stockData.time));
							SimpleDateFormat dateFormat = new SimpleDateFormat("", Locale.TRADITIONAL_CHINESE);
							dateFormat.applyPattern("yyyy年MM月dd日");
							str = dateFormat.format(date);
						}
						str = stockData.name + str + "的行情。";
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
						msg.obj = str;
						mHandler.sendMessage(msg);
						mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);					
					}
					
				});
			}
			return convertView;
		}
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_stocks;
		mViewData.mPrimaryTitleText = "股票";
		mViewData.mSecondaryTitleText = "新浪股票";
		
		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = "";
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = "";
		
		mViewData.mMinItemNumber = 0;
		mViewData.mTotleItemNumber = 0;
		mViewData.mHighLight = 0;
	
		StockJsonData stockData = (StockJsonData)((PreFormatData)mCommunicationData).getJsonData();
		if ( stockData != null && stockData.mStockData.size() > 0){
			if (stockData.mStockData.size() > 1 ) {
				mViewData.mMinItemNumber = 3;
				mViewData.mTotleItemNumber = stockData.mStockData.size();
			}
			else if (stockData.mStockData.size() == 1) {
				StockData item = stockData.mStockData.get(0);
				if (item.favorite == 0) {
					isFavorite = false;
				}
				else {
					isFavorite = true;
				}
			}
		}
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
