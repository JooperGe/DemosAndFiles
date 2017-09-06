package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.SMSJsonData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CallUtil.SmsData;
import com.viash.voicelib.utils.ContactUtil;

/**
 * 短信息
 */
@SuppressLint("ViewConstructor")
public class SMSView extends SelectionBaseView implements SelectionBaseView.IYesNoListeners{
	protected List<SmsData> smsList;
	public SMSView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable,handler,showInMainScreen);
	}

	@Override
	protected void initDataView() {
		super.initDataView();
		if(smsList.size()==1){
			setListContentavaible(false);
			setOnYesNoListeners(this);
			layout_yes_no_btns.setVisibility(View.VISIBLE);
			btn_ok.setText("回复");
			btn_cancel.setText("转发");
			
			initTitle2();
		
			SmsData smsData=smsList.get(0);
		    View convertView=LayoutInflater.from(getContext()).inflate(R.layout.layout_reply_or_forward, null);
		    EditText tv_content_sms=(EditText) convertView.findViewById(R.id.sms_content);
			tv_content_sms.setText(smsData.getmContent());
			
			Bitmap bitmap = ContactUtil.getBitMapFromNumberAndName(mContext,smsData.getmContactPhone(),smsData.getmContactName());
			if(bitmap != null)
			  mIV_PrimaryTitle.setImageBitmap(bitmap);
			mNormalContent.addView(convertView);
			return ;
		}
		setListContentavaible(true);
		
		SmsAdapter adapter = new SmsAdapter();
		setAdapter(adapter);
		if (!isFullScreen) {
			setListViewHeight(mlsvContent);
		}
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_contact_us;
		mViewData.mPrimaryTitleText = "短信";
		mViewData.mSecondaryTitleText = null;

		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = "";
		
		this.smsList = ((SMSJsonData) ((PreFormatData)(mCommunicationData)).getJsonData()).mLstSms;
		
		mViewData.mMinItemNumber = 3;
		if (smsList != null) {
			if (smsList.size() == 1) {
				needConfirm = true;
				SmsData smsData=smsList.get(0);
				mViewData.mPrimaryTitleImg = R.drawable.default_contact_img;
				
				 
				if(smsData.getmContactName()!=null && !"".equals(smsData.getmContactName().trim())){
					mViewData.mPrimaryTitleText = smsData.getmContactName();
					mViewData.mSecondaryTitleText= smsData.getmContactPhone();
				}else{
					mViewData.mPrimaryTitleText=smsData.getmContactPhone();
					mViewData.mSecondaryTitleText="";
				}
				if(smsData.getmTime()!=0){
					Date date=new Date(smsData.getmTime());
					SimpleDateFormat sdf  = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
					sdf.applyPattern("MM/dd HH:mm");
					String time_str=sdf.format(date); 
					mViewData.mFilterOrCommFunText  = time_str;
				}else {
					mViewData.mFilterOrCommFunText = "";
				}
				mViewData.mFilters = null;
			}
			mViewData.mTotleItemNumber = smsList.size();
		} else {
			mViewData.mTotleItemNumber = 0;
		}
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
	private class SmsAdapter extends BaseAdapter {

		private class ViewHolder {
			TextView tv_index;
			TextView tv_name;
			TextView tv_content;
			TextView tv_time;
			TextView tv_type;
			//int  	 position;
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
			return smsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("UseValueOf")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;			
			SmsData smsData=smsList.get(position);
			final int current_position = position; 
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sms_item, null);
				holder.tv_index=(TextView) convertView.findViewById(R.id.tv_index);
				holder.tv_name=(TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_content=(TextView) convertView.findViewById(R.id.tv_content);
				holder.tv_time=(TextView) convertView.findViewById(R.id.tv_time);
				holder.tv_type=(TextView) convertView.findViewById(R.id.tv_type);
				//holder.position = position;
				convertView.setTag(holder);
			} else {				
				holder = (ViewHolder) convertView.getTag();
				//holder.position = position;
			}
			

			holder.tv_index.setText(String.valueOf(position + 1));
			if(smsData.getmContactName()!=null&&!"".equals(smsData.getmContactName().trim())){
				if(smsList.size()>1){
					holder.tv_name.setText(smsData.getmContactName());
				}else{
					holder.tv_name.setText(smsData.getmContactName());
				}
			}else{
				if(smsList.size()>1){
					holder.tv_name.setText(smsData.getmContactPhone());
				}else{
					holder.tv_name.setText(smsData.getmContactPhone());
				}
				
			}
			if(smsData.getmContent()!=null)
				holder.tv_content.setText(smsData.getmContent());
			if(smsData.getmTime()!=0){
				Date date=new Date(smsData.getmTime());
				SimpleDateFormat sdf  = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
				sdf.applyPattern("MM/dd HH:mm");
				String time_str=sdf.format(date); 
				holder.tv_time.setText(time_str);
			}
			
			if(smsData.getmType() == 1){
				holder.tv_type.setText(getResources().getString(R.string.sms_send));
				holder.tv_type.setTextColor(getResources().getColor(R.color.text_content_assist_color_1));
			}
			if(smsData.getmType() == 2){
				holder.tv_type.setText(getResources().getString(R.string.sms_unread));
				holder.tv_type.setTextColor(getResources().getColor(R.color.text_notice_content_color));
			}
			if(smsData.getmType() == 3){
				holder.tv_type.setText(getResources().getString(R.string.sms_read));
				holder.tv_type.setTextColor(getResources().getColor(R.color.text_content_main_color));
			}
			
			convertView.setOnClickListener(new OnClickListener () {

				@Override
				public void onClick(View v) {
					if (operationEnable) {
						//ViewHolder holder = (ViewHolder) v.getTag();
						int position = current_position;
						Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
						msg.obj = "第"+ (position + 1) +"个.";
						mHandler.sendMessage(msg);
					}
					mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
					mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				}
				
			});
			
			return convertView;
		}
	}
	@Override
	public void onYesButtonClickListener(View v) {
		if (operationEnable) {
			Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
			msg.obj = "回复";
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onNoButtonClickListener(View v) {
		if (operationEnable) {
			Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
			msg.obj = "转发";
			mHandler.sendMessage(msg);
		}
	}
}
