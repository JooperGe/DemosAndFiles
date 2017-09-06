package com.viash.voice_assistant.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.CookingJsonData;
import com.viash.voicelib.msg.MsgConst;

/**
 * CookingDetailView
 */
@SuppressLint("ViewConstructor")
public class CookingDetailView extends SelectionBaseView{
	private TextView tv_cooking;
	private boolean isMeasured = false;
	
	public CookingDetailView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler,showInMainScreen);
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		setListContentavaible(false);
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_cooking_info, null);
		tv_cooking = (TextView)itemView.findViewById(R.id.tv_cooking_show);
		if (!isFullScreen) {
			tv_cooking.setLines(mViewData.mMinItemNumber);
		}
		else {
			tv_cooking.setMovementMethod(ScrollingMovementMethod.getInstance());
		}
		tv_cooking.setText(((CookingJsonData)mData).cooking);		
		itemView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
			}
			
		});
		ViewTreeObserver vto = itemView.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				if (!isMeasured) {
					isMeasured = true;
					tv_cooking.getMeasuredHeight();
					tv_cooking.getMeasuredWidth();
					if ( tv_cooking.getLineCount() <= mViewData.mMinItemNumber) {
						layout_bottom.setVisibility(View.GONE);
					}
				}
				return isMeasured;
			}
		});
		mNormalContent.addView(itemView);
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_cookbook;
		mViewData.mPrimaryTitleText = "菜谱";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mMinItemNumber = 8;
		mViewData.mTotleItemNumber = 100;
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
		
}
