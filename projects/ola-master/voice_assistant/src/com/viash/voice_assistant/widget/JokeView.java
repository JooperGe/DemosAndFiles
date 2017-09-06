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
import com.viash.voicelib.data.PreFormatData.JokeJsonData;
import com.viash.voicelib.msg.MsgConst;

/**
 * JokeView
 */
@SuppressLint("ViewConstructor")
public class JokeView extends SelectionBaseView{
	private TextView tv_joke;
	private boolean isMeasured = false;
	
	public JokeView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler,showInMainScreen);
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		setListContentavaible(false);
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_cooking_info, null);
		tv_joke = (TextView)itemView.findViewById(R.id.tv_cooking_show);
		if (!isFullScreen) {
			tv_joke.setLines(mViewData.mMinItemNumber);
		}
		else {
			tv_joke.setMovementMethod(ScrollingMovementMethod.getInstance());
		}
		tv_joke.setText(((JokeJsonData)mData).joke);
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
					tv_joke.getMeasuredHeight();
					tv_joke.getMeasuredWidth();
					if ( tv_joke.getLineCount() <= mViewData.mMinItemNumber) {
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
		mViewData.mPrimaryTitleImg = R.drawable.icon_joy;
		mViewData.mPrimaryTitleText = "笑话故事";
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
