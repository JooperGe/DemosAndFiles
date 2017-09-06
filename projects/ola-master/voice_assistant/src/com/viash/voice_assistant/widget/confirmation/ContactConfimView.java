package com.viash.voice_assistant.widget.confirmation;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;

public class ContactConfimView extends SelectionBaseView{


	public ContactConfimView(Context context, ConfirmData data,
			boolean operationEnable, Handler handler) {
		super(context, data, operationEnable, handler);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initDataView() {
		setListContentavaible(false);// Content view is NOT a list view.
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.confirm_contact, null);
		TextView confirmMessage = (TextView) itemView.findViewById(R.id.tv_remind);
		TextView mContactName = (TextView) itemView.findViewById(R.id.contact_name);
		TextView mContactNumber = (TextView) itemView.findViewById(R.id.contact_number);
		String text = ((ConfirmData)mCommunicationData).getTtsString();
		String name = ((ConfirmData)mCommunicationData).getContactData().getmName();
		String number = ((ConfirmData)mCommunicationData).getContactData().getmNumber();
		confirmMessage.setText(text);
		mContactName.setText(name);
		mContactNumber.setText(number);
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
