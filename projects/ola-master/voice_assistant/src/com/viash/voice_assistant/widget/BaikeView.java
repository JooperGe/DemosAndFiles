package com.viash.voice_assistant.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.mm.sdk.platformtools.Log;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.util.DensityUtil;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.OtherBaikeJsonData;
import com.viash.voicelib.data.PreFormatData.OtherBaikeJsonData.OtherBaikeData;
import com.viash.voicelib.data.PreFormatData.PersonJsonData;
import com.viash.voicelib.data.PreFormatData.PersonJsonData.PersonData;

/**
 * 
 * @author Benson Zhang
 * @description : update WebView to solve the memory issue;
 *
 */
@SuppressLint("ViewConstructor")
public class BaikeView extends SelectionBaseView {
	private OtherBaikeData baikeDataOther = null;
	private PersonData baikeDataPerson = null;
	private String photo_url = null;
	private String description = null;
	private static WebView webView;
	private boolean isDrawn = false;
	public BaikeView(Context context, PreFormatData data,
			boolean operationEnable, Handler mHandler, boolean showInMainScreen) {
		super(context, data, operationEnable, mHandler, showInMainScreen);
	}
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_baike;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.encyclopedia);
		mViewData.mSecondaryTitleText = null;
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		mViewData.mMinItemNumber = 6;
		mViewData.mTotleItemNumber = 100;
		mViewData.mHighLight = 0;
	}
	@Override
	protected void initDataView() {
		super.initDataView();
		if (mData instanceof OtherBaikeJsonData) {
			baikeDataOther = ((OtherBaikeJsonData) mData).mOtherBaikeData;
			photo_url = baikeDataOther.photo_url;
			description = baikeDataOther.description;
		} else if (mData instanceof PersonJsonData) {
			baikeDataPerson = ((PersonJsonData) mData).mPersonData;
			photo_url = baikeDataPerson.photo_url;
			description = baikeDataPerson.description;
		}
		while(description != null && description.startsWith("\n") ){
			description=description.substring(2);
		}
		while (description != null && description.endsWith("\n")) {
			description = description.substring(0, description.length() - 1);
		}
		Log.e("eeeeee", description);
		setListContentavaible(false);	
		// Content view is not a list view.	
		setDataView();
	}
	
	private void setDataView() {
		RelativeLayout.LayoutParams oldParams=(LayoutParams) mNormalContent.getLayoutParams() ;
		int pix = DensityUtil.dip2px(mContext, 160);
		Log.e("EEEEEEE", ""+ pix);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,pix);
		mNormalContent.setLayoutParams(params);
		View tempView = LayoutInflater.from(mContext).inflate(
				R.layout.layout_other_baike_new, null);
		webView = (WebView) tempView.findViewById(R.id.layout_baike);
		final TextView take_position = (TextView) tempView.findViewById(R.id.take_position);	
		webView.setBackgroundColor(0);		
		webView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
		String content = null;
		if (isFullScreen) {			
			mNormalContent.setLayoutParams(oldParams);
			content = "<div>";
		} else {
			content = "<div style=\"max-height:9em;overflow-y: hidden;line-height: 1.5em; margin-top:6px\">"; 
		}
		content = content + "<img src =\"" + photo_url + "\"style=\"float:left; margin-right:10px; max-width:45%; max-height:9em;line-height: 1.5em;\" BORDER=\"0\" ALT=\"\">" 
						+ "<font color=\"white\" style=\"line-height: 1.5em;\">" + description + "</font></div>";
		String property = "";
		if (description != null && photo_url != null) {
			property = "<table border=\"0\" width=\"100%\" cellspacing=\"0px\" cellpadding = \"0px\" style=\" margin-top:6px\" >";
		}
		else {
			property = "<table border=\"0\" width=\"100%\" cellspacing=\"0px\" cellpadding = \"0px\" style=\" margin-top:0px\" >";
		}
		int highLight = baikeDataOther.hightlight_item;
		String color = null;
		for (int i = 0; i < baikeDataOther.field_name.length; i++) {
			if (((highLight >> i) & 1) == 1) {
				color= "red";
			} else {
				color = "white";
			}
			property = property + "<tr align=\"left\" valign=\"top\"> "
					+ "<td nowrap style=\"white-space:nowrap;word-break:nowrap; border:0;\" align=\"left\"><font color=\"" + color + "\" style=\"line-height: 1.5em;\">" +  baikeDataOther.field_name[i] + ":</font></td>"
					+ "<td style = \"word-break:break-all; word-wrap:break-word; border:0;\"><font color=\"" + color + "\" style=\"line-height: 1.565em;\">" +baikeDataOther.field_value[i] + "</font></td>"
					+ "</tr>";
		}
		property = property + "</table>";
		
		webView.getSettings().setDefaultTextEncodingName("utf-8") ;
		webView.setScrollBarStyle(0);
		if (description == null && photo_url == null) {
			webView.loadDataWithBaseURL(null, property , "text/html", "utf-8", null);
		}else {
			webView.loadDataWithBaseURL(null, content + property , "text/html", "utf-8", null);
		}	
		
		ViewTreeObserver viewTreeObserver = webView.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {   
			@Override
			public boolean onPreDraw() {
				if (!isDrawn) {		
					if (!isFullScreen) {								
						int wid = webView.getWidth();
						int hei = webView.getContentHeight();
						float scale=webView.getScale();					
						if (wid != 0 && hei != 0) {
							isDrawn = true;
							int hi = take_position.getHeight();
							int post=(int)(hi/scale);			
							Log.e("eee"+hei,""+post+"ss"+scale);						
							if (hei-10< post) {
								layout_bottom.setVisibility(View.GONE);
							}else {
								RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) mNormalContent.getLayoutParams();
								para.height = hi;
								mNormalContent.setLayoutParams(para);
							}
						}
					}else {
						isDrawn = true;
					}
				}
				return isDrawn;
			}
		});
		
		webView.invalidate();
		mNormalContent.addView(tempView);
		tempView.invalidate();		
	}		
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
	}	
}
