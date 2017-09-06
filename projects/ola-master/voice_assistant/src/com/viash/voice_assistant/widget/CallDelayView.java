package com.viash.voice_assistant.widget;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.CallDelayJsonData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;


@SuppressLint("ViewConstructor")
public class CallDelayView extends SelectionBaseView implements SelectionBaseView.ISelectionViewListeners{

	private static ProgressBar progresssbar = null;
    private static CountDownTimer countDownTimer = null;
    private static CountDownTimer shortTimer = null;
    private static View cancelDialView = null;
    private static View cancelView = null;
    private static PreFormatData oldData = null;

	public CallDelayView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
		if (oldData == null ) {
			oldData = data;
		}else if (!oldData.equals(data)){
			Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
			msg.obj = oldData;
			mHandler.sendMessage(msg);
			oldData = data;
		}
		reSetTitle();
	}

	@Override
	protected void initSelectionViewData() {
		
		mViewData.mPrimaryTitleImg = R.drawable.default_contact_img;
		mViewData.mPrimaryTitleText = ((CallDelayJsonData)mData).mCallDelayData.contact_name;
		mViewData.mSecondaryTitleText = ((CallDelayJsonData)mData).mCallDelayData.contact_number;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 1;
		mViewData.mTotleItemNumber = 1;
		mViewData.mHighLight = 0;
		resetContactName(mViewData.mPrimaryTitleText,mViewData.mSecondaryTitleText);
	}
	
	private void resetContactName(String name,String number)
	{
		Boolean isNeedRest = false;
		if(name == null)
		{
			isNeedRest = true;
		}
		else if(name.equals(""))
		{
			isNeedRest = true;
		}
		if(isNeedRest)
		{
		   String contactName = ContactUtil.getContactName(mContext,number);
		   if(contactName != null)
			   mViewData.mPrimaryTitleText = contactName;
		}
	}
	
	private void reSetTitle()
	{
		int contact_id = ((CallDelayJsonData)mData).mCallDelayData.contact_id;
		if(contact_id != 0)
		{
			byte[] photo=ContactUtil.getPhoto(getContext(), String.valueOf(contact_id));
			if(photo!=null && photo.length>0){
				Bitmap bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
				mIV_PrimaryTitle.setImageResource(0);
				mIV_PrimaryTitle.setImageBitmap(bitmapPhoto);
			}	
		}
	}
	@Override
	protected void initDataView(){
		//super.initDataView();
		initTitle2();
		if (countDownTimer != null ) {
			countDownTimer.cancel();
		}
		
		cancelView =LayoutInflater.from(getContext()).inflate(R.layout.layout_cancel_dial, null);
		progresssbar = (ProgressBar)cancelView.findViewById(R.id.progressbar_cancel);
		progresssbar.setVisibility(View.VISIBLE);
		progresssbar.setProgress(0);
		cancelDialView = cancelView.findViewById(R.id.cancel_dial);
		
		progresssbar.setProgress(100);
		
		countDownTimer = new CountDownTimer(3000,20){
            int count = 150;
			@Override
			public void onFinish() {
				progresssbar.setProgress(0);
				Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
				msg.obj = mCommunicationData;
				mHandler.sendMessage(msg);
				Uri uri = Uri.parse("tel:" + mViewData.mSecondaryTitleText);
				Intent intent = new Intent(Intent.ACTION_CALL, uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				if (intent != null) {
					try {
						getContext().startActivity(intent);
						
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}

			@Override
			public void onTick(long millisUntilFinished) {
				count--;
				progresssbar.setProgress(count*100/150);						
			}
			
		};
		
		//if(operationEnable)
		//{
		countDownTimer.start();
		//}
		cancelDialView.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
				v.setBackgroundColor(getResources().getColor(R.color.weather_bg1));
				countDownTimer.cancel();
				shortTimer = new CountDownTimer(200,200){

					@Override
					public void onFinish() {
						cancelDialView.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_bg_bluetooth));
						progresssbar.setProgress(0);
						Message msg = mHandler.obtainMessage(MsgConst.CLIENT_ACTION_REMOVE_DATA);
						msg.obj = mCommunicationData;
						oldData = null;
						mHandler.sendMessage(msg);
					}

					@Override
					public void onTick(long millisUntilFinished) {}
					
				};
				shortTimer.start();
			}
			
		});
		//layout_Content = (RelativeLayout) findViewById(R.id.layout_content_base_parent); //
		mNormalContent.setVisibility(View.VISIBLE);
		View bgline = layout_selection_base.findViewById(R.id.base_separate1);
		bgline.setVisibility(View.GONE);
		bgline = layout_selection_base.findViewById(R.id.base_separate2);
		bgline.setVisibility(View.GONE);
		mNormalContent.addView(cancelView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
	}
	
	@Override
	public void onContentViewClickListener(View v) {}

	@Override
	public void onFilterItemClickListener(View v) {}

	@Override
	public void handleServerCmd() {
		cancelDialView.performClick();
	}

	@Override
	public void onFilterTitleClickListener(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
