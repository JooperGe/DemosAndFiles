package com.viash.voice_assistant.widget;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.NewsJsonData;
import com.viash.voicelib.data.PreFormatData.NewsJsonData.NewsData;
import com.viash.voicelib.utils.ImageLoaderUtil;

/**
 * 新闻
 * @author Harlan Song
 * @createDate 2013-3-28
 * @email:mark_dev@163.com
 */
@SuppressLint("ViewConstructor")
public class NewsView extends SelectionBaseView {
	private List<NewsData> newsDatas;
    private TextView tv_content;
    
	public NewsView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);		
	}
	
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_news;
		mViewData.mPrimaryTitleText = getResources().getString(R.string.news);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 1;
		mViewData.mTotleItemNumber = 2;
		mViewData.mHighLight = 0;				
	}
    
	@Override
	protected void initDataView(){
		super.initDataView();
		newsDatas = ((NewsJsonData)mData).mNewsData;
		setListContentavaible(false);//Content view is not a list view.
		setDataView();
	}
	
	private void setDataView()
	{	
		ScrollView sView = new ScrollView(mContext);
		LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		sView.setLayoutParams(param);
		sView.addView(getView());
		mNormalContent.addView(sView);		
	}
	
	private View getView()
	{
		int minCount = 6;
        int maxCount = 1000;
		View itemView = null;
		if (newsDatas != null && newsDatas.size() > 0) {
			for (int i = 0; i < newsDatas.size(); i++) {
				NewsData newsData = newsDatas.get(i);
				itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_news_item, null);
				TextView tv_name = (TextView) itemView.findViewById(R.id.tv_name);
				TextView tv_time = (TextView) itemView.findViewById(R.id.tv_time);
				TextView tv_source = (TextView) itemView.findViewById(R.id.tv_resource);
				ImageView imgv_news = (ImageView) itemView.findViewById(R.id.imgv_news);
				tv_content = (TextView) itemView.findViewById(R.id.tv_content);
				if(newsData.title != null)
					tv_name.setText(newsData.title);
				if(newsData.time != null)
					tv_time.setText(newsData.time);
				String newsDetail_str = newsData.detail;
				if (newsDetail_str != null && newsDetail_str.length() > 4) {
					if (!"    ".equals(newsDetail_str.substring(0, 4))) {
						newsDetail_str = "   " + newsDetail_str;
					}
					tv_content.setText(newsDetail_str);
					
					DisplayMetrics metric = getResources().getDisplayMetrics();
					
					int screenWidth = metric.widthPixels;
					float rowWidth = screenWidth - tv_content.getPaddingLeft()
							- tv_content.getPaddingRight();
					
					float textWidth = tv_content.getTextSize();
					int columnCount = (int) (rowWidth / textWidth);
					
					if (tv_content.getText().length() > minCount*columnCount) 
					{
						if(!isFullScreen)
						{
						  tv_content.setMaxLines(minCount);
						}
						else
						{
						  tv_content.setMaxLines(maxCount);
						}
						layout_bottom.setVisibility(View.VISIBLE);
						
					} 
					else 
					{
						tv_content.setMaxLines(maxCount);
						layout_bottom.setVisibility(View.GONE);
					}
				}
				if (newsData.source != null)
				{
					String[] array = newsData.source.split("\\Q\n\\E");
					String str = "";
					for(int j=0; j<array.length; j++)
					{
					  array[j] = array[j].trim();	
					  str += array[j] + " ";
					}
					tv_source.setText(str);
				}
				if (newsData.image_url != null && newsData.image_url.length() > 0)
					ImageLoaderUtil.loadImageAsync(imgv_news,
							newsData.image_url, null, null, 500,null);
				else
					imgv_news.setVisibility(View.GONE);										
			}
		}
		return itemView;		
	}
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
