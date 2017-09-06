package com.viash.voice_assistant.widget.confirmation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.ConfirmData;
import com.viash.voicelib.data.ConfirmData.SendSmsData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;

@SuppressLint("ViewConstructor")
public class SendSMSView extends SelectionBaseView implements
		SelectionBaseView.IYesNoListeners {
	private EditText et_sms_content;
	private TextView tv_sms_name;
	private TextView tv_sms_phone;
	private TextView tv_input_num;
	private final int inputNum = 70;
    private float x0=0;
	public SendSMSView(Context context, ConfirmData confirmData,
			boolean operationEnable, Handler mHandler) {
		super(context, confirmData, operationEnable, mHandler);
	}

	@Override
	protected void initDataView() {
		setListContentavaible(false);// Content view is NOT a list view.
		setOnYesNoListeners(this);
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sms_message, null);
		et_sms_content = (EditText) itemView.findViewById(R.id.et_content);
		tv_sms_name = (TextView) itemView.findViewById(R.id.tv_name);
		tv_sms_phone = (TextView) itemView.findViewById(R.id.tv_phone);
		tv_input_num = (TextView) itemView.findViewById(R.id.tv_input_num);
		setData();
		mNormalContent.addView(itemView);
		loading();
	}

	private void loading() {
		et_sms_content.setFocusable(true);
		et_sms_content.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int linetext=et_sms_content.getText().toString().length()/et_sms_content.getLineCount();
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					x0=event.getX();
					new Handler().postDelayed(new Runnable(){    
					    public void run() {    
					    	et_sms_content.requestFocus();    
					    }    
					 }, 300);   
				}
				if(event.getAction()==MotionEvent.ACTION_MOVE){		
					if(event.getX()-x0>0){
					if(et_sms_content.getSelectionStart()<et_sms_content.getText().toString().length()-linetext){
					 et_sms_content.setSelection(et_sms_content.getSelectionStart()+linetext);
					 x0=event.getX();
					}
				}}
				if(event.getX()-x0<0){
					if(et_sms_content.getSelectionStart()>linetext){
						 et_sms_content.setSelection(et_sms_content.getSelectionStart()-linetext);
						 x0=event.getX();
					}
				}
			
				return false;
			}
		});
		et_sms_content.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
				ConfirmData confirmData = (ConfirmData) mCommunicationData;
				if (confirmData.getType() == ConfirmData.TYPE_SMS) {
					confirmData.getSmsData()
							.setContent(charsequence.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence charsequence, int i,	int j, int k) {}

			@Override
			public void afterTextChanged(Editable editable) {
				int intputCount = et_sms_content.getText().toString().length();
				String sInputNumString = intputCount + "/" + inputNum;
				int iSmsCount = intputCount / inputNum + 1;
				if (iSmsCount >= 2) {
					sInputNumString = sInputNumString + "(" + iSmsCount + ")";
				}
				tv_input_num.setText(sInputNumString);
			}
		});
	}

	public void setData() {
		ConfirmData confirmData = (ConfirmData) mCommunicationData;
		SendSmsData sendSmsData = confirmData.getSmsData();
		if (sendSmsData != null) {
			tv_sms_name.setText(sendSmsData.getTo());
			tv_sms_phone.setText(sendSmsData.getToPhone());
			et_sms_content.setText(sendSmsData.getContent());
			resetContactName(sendSmsData.getTo(),sendSmsData.getToPhone());
		}
		if (operationEnable) {
			et_sms_content.setEnabled(true);

		} else {			
			et_sms_content.setEnabled(false);
		}

		int intputCount = sendSmsData.getContent().length();
		String sInputNumString = intputCount + "/" + inputNum;
		int iSmsCount = intputCount / inputNum + 1;
		if (iSmsCount >= 2) {
			sInputNumString = sInputNumString + "(" + iSmsCount + ")";
		}
		tv_input_num.setText(sInputNumString);
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
			   tv_sms_name.setText(contactName);
		}
	}
	
	@Override
	protected void initSelectionViewData() {
		super.initDataView();
		mViewData.mPrimaryTitleImg = R.drawable.icons_message;
		mViewData.mPrimaryTitleText = "短信";
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

	@Override
	public void onYesButtonClickListener(View v) {
		Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, 1, 1);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onNoButtonClickListener(View v) {
		Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, 0, 1);
		mHandler.sendMessage(msg);
	}
    
	public void setOperationEnableDisable()
	{
		operationEnable = false;
	}
}
