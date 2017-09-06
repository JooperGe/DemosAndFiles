package com.viash.voice_assistant.widget.confirmation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.ConfirmData;

/**
 * 确定和取消
 */
@SuppressLint("ViewConstructor")
public class ConfirmCancelView extends SelectionBaseView {
	
	public ConfirmCancelView(Context context, ConfirmData confirmData,boolean operationEnable, Handler mHandler) {
		super(context, confirmData, operationEnable, mHandler);
	}
	
	@Override
	protected void initDataView() {
		setListContentavaible(false);// Content view is NOT a list view.
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.confirm_text, null);
		TextView confirmMessage = (TextView) itemView.findViewById(R.id.tv_confirm);
		String text = ((ConfirmData)mCommunicationData).getDisplayString();
		confirmMessage.setText(text);
		mNormalContent.addView(itemView);
	}
	
	@Override
	protected void initSelectionViewData() {
		super.initDataView();
		mViewData.mPrimaryTitleImg = R.drawable.icons_remaind;
		mViewData.mPrimaryTitleText = "确认";
		mViewData.mSecondaryTitleText = null;

		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;

		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;

		mViewData.mMinItemNumber = 0;
		mViewData.mTotleItemNumber = 0;
		mViewData.mHighLight = 0;		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
