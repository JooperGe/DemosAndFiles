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
import com.viash.voicelib.data.PreFormatData.LyricsJsonData;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class LyricsView extends SelectionBaseView {
	private TextView tv_lyrics;
	private boolean isMeasured = false;
		
	public LyricsView(Context context, PreFormatData data, boolean operationEnable, Handler handler, boolean showInMainScreen) {
		super(context, data, operationEnable, handler, showInMainScreen);
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		setListContentavaible(false);//Content view is a normal view.
		View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_lyrics_info, null);
		tv_lyrics = (TextView)itemView.findViewById(R.id.tv_lyrics_show);
		if (!isFullScreen) {
			tv_lyrics.setLines(mViewData.mMinItemNumber);
		}
		else {
			tv_lyrics.setMovementMethod(ScrollingMovementMethod.getInstance());
		}
		tv_lyrics.setText(((LyricsJsonData)mData).lyrics);
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
					tv_lyrics.getMeasuredHeight();
					tv_lyrics.getMeasuredWidth();
					if ( tv_lyrics.getLineCount() <= mViewData.mMinItemNumber) {
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
		mViewData.mPrimaryTitleImg = R.drawable.icons_music;
		mViewData.mPrimaryTitleText = "音乐";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterIndex = 0;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 13;
		mViewData.mTotleItemNumber = 100;
		mViewData.mHighLight = 0;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
