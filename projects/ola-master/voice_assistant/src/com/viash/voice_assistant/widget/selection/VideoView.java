package com.viash.voice_assistant.widget.selection;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContentUtil;
import com.viash.voicelib.utils.ImageLoaderUtil;

/**
 * 视频
 */
@SuppressLint("ViewConstructor")
public class VideoView extends SelectionBaseView {
	private List<String> videos;
	private GridView gv_video;
	private int verticalSpacing = 10;
	private int horizontalSpacing = 10;
	
	public VideoView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
	}
	
	protected void initSelectionViewData() {
		videos =  ((OptionData) mCommunicationData).getOptions();
		/*videos.remove(0);
		videos.remove(0);
		videos.remove(0);
		videos.remove(0);*/
		//videos.addAll(videos);
		mViewData.mPrimaryTitleImg = R.drawable.icons_video;
		mViewData.mPrimaryTitleText = "视频";
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 2;
		if (videos !=  null && videos.size() > 0 ){
			if (videos.size() % 3 == 0) {
				mViewData.mTotleItemNumber = (videos.size()) /3;
			} else {
				mViewData.mTotleItemNumber = (videos.size()) / 3 + 1;
			}
		}
		else {
			mViewData.mTotleItemNumber = 0 ;
		}
		mViewData.mHighLight = 0;	
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		setListContentavaible(false);
		View view = LayoutInflater.from(getContext()).inflate(R.layout.selection_video, null);
		gv_video = (GridView) view.findViewById(R.id.gv_video);
		verticalSpacing = ContentUtil.dip2px(getContext(), 5);
		horizontalSpacing = ContentUtil.dip2px(getContext(), 5);
		gv_video.setVerticalSpacing(verticalSpacing);
		gv_video.setHorizontalSpacing(horizontalSpacing);
		
		if(videos !=null && videos.size() > 0){
			gv_video.setAdapter(new VideoAdapter());
			setGridViewHeight(gv_video);			
		}
		mNormalContent.addView(view);
	}
	
	/*public VideoView(Context context,List<String> videos,boolean operationEnable,Handler mHandler) {
		super(context);
		this.videos =videos;
		this.operationEnable = operationEnable;
		this.mHandler = mHandler;
		LayoutInflater.from(getContext()).inflate(R.layout.selection_video, this,true);
		gv_video = (GridView) findViewById(R.id.gv_video);
		verticalSpacing = ContentUtil.dip2px(getContext(), 5);
		horizontalSpacing = ContentUtil.dip2px(getContext(), 5);
		gv_video.setVerticalSpacing(verticalSpacing);
		gv_video.setHorizontalSpacing(horizontalSpacing);
		
		if(videos !=null && videos.size() > 0){
			gv_video.setAdapter(new VideoAdapter());
			setGridViewHeight(gv_video);
			if(operationEnable){
				gv_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						int position=1<<arg2;
						Message msg = VideoView.this.mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_OPTION, position, 0);
						VideoView.this.mHandler.sendMessage(msg);
						
					}
				});
			}
		}
	}*/
	
	private void setGridViewHeight(GridView gridView){
		View listItem = gridView.getAdapter().getView(0, null, gridView);
		int widgetWidth = getResources().getDisplayMetrics().widthPixels;
		listItem.measure(MeasureSpec.makeMeasureSpec(widgetWidth/3, MeasureSpec.AT_MOST),
		        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int itemWith = listItem.getMeasuredWidth();
		int itemHeight = listItem.getMeasuredHeight();
		
		int lineNum = widgetWidth / itemWith;
		if(lineNum == 0)
			lineNum = 1;
		int count = 1;
		count = (gridView.getCount() - 1) / lineNum + 1;
		if(count == 0){
			count = 1;
		}
		int height = count * (itemHeight +verticalSpacing);
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = height;
		gridView.setLayoutParams(params);
	}
	
	 public static int dip2px(Context context, float dipValue){
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int)(dipValue * scale + 0.5f);
	 }
	
	private class VideoAdapter  extends BaseAdapter{

		@Override
		public int getCount() {
			if (isFullScreen) {
				return videos.size();
			} else if (mViewData.mMinItemNumber * 3 >= videos.size()) {
				return videos.size();
			}else {
				return mViewData.mMinItemNumber * 3;
			}
			
		}

		@Override
		public Object getItem(int position) {
			return videos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String itemContent = videos.get(position);
			if(itemContent!=null&&itemContent.indexOf("$$")>0){
				String[] array=itemContent.split("\\Q$$\\E");
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.selection_video_item, null);
				TextView tv_index = (TextView) convertView.findViewById(R.id.tv_index);
				TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				ImageView imgv_video = (ImageView) convertView.findViewById(R.id.imgv_video);
				convertView.setTag(position);
				if(array !=null && array.length > 0){
					tv_index.setText(String.valueOf((position +1)));
					if(array[0] !=null)
						tv_name.setText(array[0]);
					if(array.length > 1 && array[1] !=null){
						/*if (position != 4) {							
							ImageLoaderUtil.loadImageAsync(imgv_video, "http://pic.cnjuc.com/pic/uploadimg/2013-11/2013111321552820196.jpg", null, getResources().getDrawable(R.drawable.default_contact_img),ContentUtil.dip2px(getContext(),72,,null));
						}else */
						ImageLoaderUtil.loadImageAsync(imgv_video, array[1], null, getResources().getDrawable(R.drawable.default_contact_img),ContentUtil.dip2px(getContext(),72),null);
					}
				}
				
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (operationEnable) {
							int position = (Integer) v.getTag();
							position = 1<<position;
							Message msg = VideoView.this.mHandler
									.obtainMessage(
											MsgConst.MSG_DATA_FROM_OPTION,
											position, 0);
							VideoView.this.mHandler.sendMessage(msg);
						}
						mHandler.sendEmptyMessage(MsgConst.MSG_ON_VIEW_TOUCH);
					}
				});
			}
			return convertView;
		}
		
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}
