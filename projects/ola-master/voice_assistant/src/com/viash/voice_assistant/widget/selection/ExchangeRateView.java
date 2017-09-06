package com.viash.voice_assistant.widget.selection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.ExchangeRateJsonData;
import com.viash.voicelib.msg.MsgConst;

/**
 * 汇率信息
 * @author Loneway 
 * @createDate 2013-10-25
 */
@SuppressLint("ViewConstructor")
public class ExchangeRateView extends SelectionBaseView {

	public ExchangeRateView(Context context, PreFormatData data,
			boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initDataView(){
		setListContentavaible(true);//Content view is a list view.
		
		//mlsvContent.setDivider(getResources().getDrawable(R.drawable.bg_line));
		//mlsvContent.setFooterDividersEnabled(true);
		ContextAdapter adapter = new ContextAdapter();
		setAdapter(adapter);
		if (!isFullScreen){
			setListViewHeight(mlsvContent);
		}
		
		super.initDataView();
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_money;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.exchange_rate);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 5;
		mViewData.mTotleItemNumber = ((ExchangeRateJsonData)mData).mtargets.length;
		mViewData.mHighLight = 0;
	}
	
	private class ContextAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			if (isFullScreen){
				return ((ExchangeRateJsonData)mData).mtargets.length;
			}
			else {
				if (((ExchangeRateJsonData)mData).mtargets.length >= mViewData.mMinItemNumber) {
					return mViewData.mMinItemNumber;
				}
				else return  ((ExchangeRateJsonData)mData).mtargets.length;
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_exchange_rate_item, null);

				holder = new ViewHolder();
				holder.tv_target = (TextView) convertView.findViewById(R.id.tv_exchange_target);
				holder.tv_source = (TextView) convertView.findViewById(R.id.tv_exchange_source);

				convertView.setTag(holder);
			} else {				
				holder = (ViewHolder) convertView.getTag();
			}
			if (((ExchangeRateJsonData)mData).mSource != null ||((ExchangeRateJsonData)mData).mtargets != null) {
				holder.tv_source.setText(((ExchangeRateJsonData)mData).mSource);
				holder.tv_target.setText(((ExchangeRateJsonData)mData).mtargets[position]);
			}
			else {
				holder.tv_source.setText("");
				holder.tv_target.setText("");
			}
			
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);					
				}
				
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_source;
			TextView tv_target;
		}

	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}

}
