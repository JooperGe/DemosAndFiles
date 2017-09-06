package com.viash.voice_assistant.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.ImageBrowerActivity;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voicelib.data.PreFormatData;
import com.viash.voicelib.data.PreFormatData.WeiboJsonData;
import com.viash.voicelib.data.PreFormatData.WeiboJsonData.WeiboData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ImageDownloadTaskUtil;
import com.viash.voicelib.utils.ImageLoaderUtil;
import com.viash.voicelib.utils.LogOutput;


/**
 * 新浪微博
 * 微博界面有单人微博和多人微博
 * @author Leo
 * @createDate 2014-01-07
 * @
 */
@SuppressLint("ViewConstructor")
public class SinaWeiBoView extends SelectionBaseView {
	private static final String TAG = "SinaWeiBoView";
	private List<WeiboData> weiboList;
	private WeiboData weiboData;
	private static int weiboViewType = -1;
	public SinaWeiBoView(Context context,PreFormatData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);		
	}
			
	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_weibo;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.weibo_num);
		mViewData.mSecondaryTitleText = null;
		
		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 1;
		mViewData.mTotleItemNumber = ((WeiboJsonData)mData).lstContent.size();
		mViewData.mHighLight = 0;		
	}

	@Override
	protected void initDataView(){		
		weiboList = ((WeiboJsonData)mData).lstContent;
		setListContentavaible(false);//Content view is not a list view.
		if(weiboList !=null && weiboList.size() > 0 && weiboList.get(0).sub_type != null)
		{ 
			String sub_type =weiboList.get(0).sub_type;
			if("weibo_content".equalsIgnoreCase(sub_type)){
				//关注的人的微博
				weiboViewType = 1;
				//loadWeiboAttention();
			}
			else if("weibo_content_oneperson".equalsIgnoreCase(sub_type)){
				//某个人的微博
				weiboViewType = 2;
				//loadWeiboSingle();
			}
			else if ("comment_add".equalsIgnoreCase(sub_type) || "comment_sent".equals(sub_type) || "comment_sent".equals(sub_type) || "comment_received".equals(sub_type)  ){
				//评论
				weiboViewType = 3;
				mViewData.mMinItemNumber = 2;
				//loadWeiboComment();
			}else if ("fans".equalsIgnoreCase(sub_type)){
				//粉丝
				weiboViewType = 4;
				mViewData.mMinItemNumber = 3;
				//loadWeiboFans();
			}else if ("myComment".equalsIgnoreCase(sub_type)){
				//加载不含原文的评论  loadOnlyComment();
				weiboViewType = 5;
				mViewData.mMinItemNumber = 2;
			}
		}
		else{
			LogOutput.e(TAG, "weibo data is null");
		}
		super.initDataView();
		setDataView();
	}
	
	private void setDataView()
	{
		View view = null;
		View viewTitle = null;
		if(!isFullScreen)
		{
			int count = 0;
			if(mViewData.mTotleItemNumber <= mViewData.mMinItemNumber)
			    count = mViewData.mTotleItemNumber;
			else
				count = mViewData.mMinItemNumber;
			
			if(weiboViewType == 3)
				count++;
			for(int i= 0; i<count; i++)
			{	
				switch(weiboViewType)
				{
				case 1:
					 view = getWeiboAttentionView(i);
					 break;
				case 2:
					 if(i == 0)
					   viewTitle = getWeiboSingleView(i);
					 else
					   viewTitle = null;	 
					 view = getWeiboSingleListItemView(i);
					 break;
				case 3:
					 if(i == 0)
					 {
					   //viewTitle = getWeiboCommentView(i);
					 }
					 else
					 {
					   viewTitle = null;	 
					   view = getWeiboCommentItemView(i);
					 }
					 break;
				case 4:
					 view = getWeiboFansView(i);
					 break;
				case 5:
					 view = getOnlyCommentView(i);
					 break;	 
	            default:
	            	 break;		 
				}
				
				if(viewTitle != null)
				{
					mNormalContent.addView(viewTitle);
					viewTitle = null;
				}
				if(view != null)
				    mNormalContent.addView(view);				
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				mNormalContent.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));				
			}
			
		}
		else
		{
			
			ScrollView sView = new ScrollView(mContext);
			LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			LinearLayout layout_content = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_bus, null).findViewById(R.id.layout_content);
			sView.setLayoutParams(param);
			
			for(int i= 0; i<mViewData.mTotleItemNumber; i++)
			{	
				switch(weiboViewType)
				{
				case 1:
					 view = getWeiboAttentionView(i);
					 break;
				case 2:
					 if(i == 0)
					   viewTitle = getWeiboSingleView(i);
					 else
					   viewTitle = null;	 
					 view = getWeiboSingleListItemView(i);
					 break;
				case 3:
					 if(i == 0)
					 {//viewTitle = getWeiboCommentView(i);
					 }
					 else
					 {
					   viewTitle = null;	 
					   view = getWeiboCommentItemView(i);
					 }
					 break;
				case 4:
					 view = getWeiboFansView(i);
					 break;
				case 5:
					 view = getOnlyCommentView(i);
					 break;	 
	            default:
	            	 break;		 
				}
			
				if(viewTitle != null)
				{
					layout_content.addView(viewTitle);
					viewTitle = null;
				}
				if(view != null)	
					layout_content.addView(view);				
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));			    
			}
			sView.addView(layout_content);
			mNormalContent.addView(sView);
		}
	}
	
	private View getWeiboAttentionView(int position) {
		weiboData = weiboList.get(position);
		View convertView = null;
		WeiboAttentionHolder holder = new WeiboAttentionHolder();		
	/*	if(convertView == null)
		{*/
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_list_item, null);
			holder = new WeiboAttentionHolder();
			holder.imgv_from = (ImageView) convertView.findViewById(R.id.imgv_from);
			holder.imgv_content = (ImageView) convertView.findViewById(R.id.imgv_content);
			holder.imgv_source = (ImageView) convertView.findViewById(R.id.imgv_source_content);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_content_source = (TextView)convertView.findViewById(R.id.tv_source_content);
			holder.tv_from = (TextView) convertView.findViewById(R.id.tv_from);
			holder.tv_like_num = (TextView) convertView.findViewById(R.id.tv_like_num);
			holder.tv_forward_num = (TextView) convertView.findViewById(R.id.tv_forward_num);
			holder.tv_comment_num = (TextView) convertView.findViewById(R.id.tv_comment_num);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.layout_forward = (LinearLayout) convertView.findViewById(R.id.layout_forward);			
			convertView.setTag(holder);
/*			
		}
		else
		{
			holder = (WeiboAttentionHolder) convertView.getTag();
		}*/
		if(weiboData.from_comment_num != 0)
			holder.tv_comment_num.setText(String.valueOf(weiboData.from_comment_num));
		if(weiboData.from_forward_num != 0)
			holder.tv_forward_num.setText(String.valueOf(weiboData.from_forward_num));
		if(weiboData.from_like_num != 0)
			holder.tv_like_num.setText(String.valueOf(weiboData.from_like_num));
		if(weiboData.source !=null )
			holder.tv_from.setText(getResources().getString(R.string.from_lable) + weiboData.source);
		String text_content = "";
		if(weiboData.text_content !=null)
			text_content += weiboData.text_content;
		if(weiboData.original_from != null)
			text_content += "\n\n" +weiboData.original_from;
		if(text_content.length() > 0)
			holder.tv_content.setText(text_content);
		else
			holder.tv_content.setVisibility(View.GONE);
		if(weiboData.image_thumb !=null){
			if(weiboData.image_larger != null){
				holder.imgv_content.setTag(weiboData.image_larger);
				holder.imgv_content.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String url = (String) v.getTag();
						Intent intent = new Intent(getContext(),ImageBrowerActivity.class);
						intent.putExtra("url", url);
						getContext().startActivity(intent);
					}
				});
			}
			ImageLoaderUtil.loadImageAsync(holder.imgv_content, weiboData.image_thumb, null, null,200,null);
		}else{
			holder.imgv_content.setVisibility(View.GONE);
		}
		if(weiboData.original_text_content !=null){
			holder.layout_forward.setVisibility(View.VISIBLE);
			holder.tv_content_source.setText(weiboData.original_text_content);
			if(weiboData.original_image_thumb !=null){
				if(weiboData.original_image_larger != null){
					holder.imgv_source.setTag(weiboData.original_image_larger);
					holder.imgv_source.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String url = (String) v.getTag();
							Intent intent = new Intent(getContext(),ImageBrowerActivity.class);
							intent.putExtra("url", url);
							getContext().startActivity(intent);
						}
					});
				}
				ImageLoaderUtil.loadImageAsync(holder.imgv_source, weiboData.original_image_thumb, null, null,100,null);
			}else{
				holder.imgv_source.setVisibility(View.GONE);
			}
				
		}else{
			holder.tv_content_source.setVisibility(View.GONE);
			holder.layout_forward.setVisibility(View.GONE);
		}
		if(weiboData.from_portrait !=null)
			ImageLoaderUtil.loadImageAsync(holder.imgv_from, weiboData.from_portrait, null, null,100,null);
		
		if(weiboData.from !=null){
			holder.tv_name.setText(weiboData.from);
		}
		
		if(weiboData.create_time != null)
		{
			try
			{
				Date date = new Date(new Long(weiboData.create_time));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				holder.tv_time.setText(dateFormat.format(date));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
	
				
		return convertView;
	}
    
	
	private View getWeiboSingleView(int position) {
		
		View convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_single,null);
		ImageView imgv_from=(ImageView) convertView.findViewById(R.id.imgv_from);
		ImageView imgv_sex=(ImageView) convertView.findViewById(R.id.imgv_sex);
		TextView tv_attention = (TextView)convertView.findViewById(R.id.tv_attention);
		TextView tv_funs = (TextView) convertView.findViewById(R.id.tv_funs);
		TextView  tv_weibonum = (TextView)convertView.findViewById(R.id.tv_weibo_num);
		TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
		new ImageDownloadTaskUtil().execute(weiboList.get(0).from_portrait, imgv_from);
		tv_attention.setText(String.valueOf(weiboList.get(0).from_care_num));
		tv_funs.setText(String.valueOf(weiboList.get(0).from_fan_num));
		tv_weibonum.setText(String.valueOf(weiboList.get(0).from_weibo_num));
		if(weiboList.get(0).from_sex !=null){
			if("m".equalsIgnoreCase(weiboList.get(0).from_sex)){
				imgv_sex.setImageResource(R.drawable.weibo_sex_boy);
			}else if("f".equalsIgnoreCase(weiboList.get(0).from_sex)){
				imgv_sex.setImageResource(R.drawable.weibo_sex_girl);
			}else{
				imgv_sex.setVisibility(View.GONE);
			}
		}else{
			imgv_sex.setVisibility(View.GONE);
		}
		if(weiboList.get(0).from != null)
			tv_name.setText(weiboList.get(0).from); 
		return convertView;
	}
	
	private View getWeiboSingleListItemView(int position) {
		weiboData = weiboList.get(position);
		View convertView = null;
		WeiboSingleHolder holder = new WeiboSingleHolder();		
		//holder = new WeiboSingleHolder();
		/*if(convertView == null)
		{*/
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_single_item, null);
			holder.imgv_original=(ImageView) convertView.findViewById(R.id.imgv_original);	
			holder.imgv_content=(ImageView) convertView.findViewById(R.id.imgv_content);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_original = (TextView) convertView.findViewById(R.id.tv_original);
			holder.tv_forward_num = (TextView) convertView.findViewById(R.id.tv_forward_num);
			holder.tv_comment_num = (TextView) convertView.findViewById(R.id.tv_comment_num);
			holder.tv_like_num = (TextView) convertView.findViewById(R.id.tv_like_num);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_from = (TextView) convertView.findViewById(R.id.tv_from);
			holder.layout_forward = (LinearLayout) convertView.findViewById(R.id.layout_forward);			
			convertView.setTag(holder);
	/*		
		}
		else
		{
			holder = (WeiboSingleHolder) convertView.getTag();
		}*/
		String text_content = "";
		if(weiboData.text_content !=null)
			text_content += weiboData.text_content;
		if(weiboData.original_from != null)
			text_content += "\n\n" +weiboData.original_from;
		if(text_content.length() > 0)
			holder.tv_content.setText(text_content);
		else
			holder.tv_content.setVisibility(View.GONE);
		
		if(weiboData.image_thumb !=null && !weiboData.image_thumb.equals("")){
			ImageLoaderUtil.loadImageAsync(holder.imgv_content, weiboData.image_thumb, null, null,100,null);
			if(weiboData.image_larger != null){
				holder.imgv_content.setTag(weiboData.image_larger);
				holder.imgv_content.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String url = (String) v.getTag();
						Intent intent = new Intent(getContext(),ImageBrowerActivity.class);
						intent.putExtra("url", url);
						getContext().startActivity(intent);
					}
				});
			}
		}
			
		else
			holder.imgv_content.setVisibility(View.GONE);
		
		if(weiboData.original_text_content !=null){
			holder.layout_forward.setVisibility(View.VISIBLE);
			holder.tv_original.setText(weiboData.original_text_content);
			if(weiboData.original_image_middle !=null){
				if(weiboData.original_image_larger != null){
					holder.imgv_original.setTag(weiboData.original_image_larger);
					holder.imgv_original.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String url = (String) v.getTag();
							Intent intent = new Intent(getContext(),ImageBrowerActivity.class);
							intent.putExtra("url", url);
							getContext().startActivity(intent);
						}
					});
				}
				ImageLoaderUtil.loadImageAsync(holder.imgv_original, weiboData.original_image_middle, null, null,100,null);
			}
			else{
				holder.imgv_original.setVisibility(View.GONE);
			}
				
		}else{
			holder.layout_forward.setVisibility(View.GONE);
		}
		if(weiboData.from_comment_num != 0)
			holder.tv_comment_num.setText(String.valueOf(weiboData.from_comment_num));
		if(weiboData.from_forward_num != 0)
			holder.tv_forward_num.setText(String.valueOf(weiboData.from_forward_num));
		if(weiboData.from_like_num != 0)
			holder.tv_like_num.setText(String.valueOf(weiboData.from_like_num));
		if(weiboData.source !=null )
			holder.tv_from.setText(getResources().getString(R.string.from_lable) + weiboData.source);
		if(weiboData.create_time != null){
			try
			{
				Date date = new Date(new Long(weiboData.create_time));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				holder.tv_time.setText(dateFormat.format(date));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
				
		return convertView;
	}
	
	private View getWeiboFansView(int position)
	{		
		weiboData = weiboList.get(position);
		View convertView = null;
		WeiboFansHolder holder = new WeiboFansHolder();		
	/*	if(convertView == null)
		{*/
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_fans_item, null);
					
			holder.imgv_fans_head = (ImageView) convertView.findViewById(R.id.imgv_head);
			holder.imgv_goto = (ImageView) convertView.findViewById(R.id.imgv_goto);
			holder.tv_fans_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_fans_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);						
			convertView.setTag(holder);			
/*		}
		else
		{
			holder = (WeiboFansHolder) convertView.getTag();
		}*/
        				
		holder.tv_index.setText(String.valueOf(position+1));		
		if(weiboData.from_portrait !=null)
			ImageLoaderUtil.loadImageAsync(holder.imgv_fans_head, weiboData.from_portrait, null, getResources().getDrawable(R.drawable.logo),100,null);
		if(weiboData.from !=null)
			holder.tv_fans_name.setText(weiboData.from);
		if(weiboData.text_content !=null)
			holder.tv_fans_content.setText(weiboData.text_content);
		holder.imgv_goto.setTag(holder);
		holder.imgv_goto.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isFullScreen)
				   mHandler.sendEmptyMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
				WeiboFansHolder holder = (WeiboFansHolder) v.getTag();
				String  str = holder.tv_fans_name.getText() + "的微博";
				Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT,str);
				mHandler.sendMessage(msg);
			}	
		});
		return convertView;
	}
	
    private View getOnlyCommentView(int position) 
    {		
		weiboData = weiboList.get(position);
		View convertView = null;
		OnlyCommentHolder holder = new OnlyCommentHolder();		
	/*	if(convertView == null)
		{*/
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_comment_item, null);
				
			holder.imgv_comment_head = (ImageView) convertView.findViewById(R.id.imgv_head);
			holder.tv_comment_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_comment_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_replay_content = (TextView) convertView.findViewById(R.id.tv_replay_content);
			holder.tv_comment_time = (TextView) convertView.findViewById(R.id.tv_time);				
			convertView.setTag(holder);			
	/*	}
		else
		{
			holder = (OnlyCommentHolder) convertView.getTag();
		}
		*/
		if(weiboData.from_portrait !=null)
			ImageLoaderUtil.loadImageAsync(holder.imgv_comment_head, weiboData.from_portrait, null, getResources().getDrawable(R.drawable.logo),100,null);
		if(weiboData.from !=null)
			holder.tv_comment_name.setText(weiboData.from);
		if(weiboData.text_content !=null)
			holder.tv_comment_content.setText(weiboData.text_content);
		
		if(weiboData.original_text_content !=null)
			holder.tv_replay_content.setText(weiboData.original_text_content);
		else
			holder.tv_replay_content.setVisibility(View.GONE);
		if(weiboData.time !=null)
		{
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("");
			sdf.applyPattern("MM/dd HH:mm");
			long time = Long.parseLong(weiboData.time);
			Date date = new Date(time);
			calendar.setTime(date);
			holder.tv_comment_time.setText(sdf.format(date));
		}
		else
		{
			holder.tv_comment_time.setVisibility(View.GONE);
		}
		
		return convertView;
	}
    
    private View getWeiboCommentView(int position) 
    {		
		weiboData = weiboList.get(position);
		View convertView = null;
		WeiboCommentHolder holder;		
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_comment, null);
			holder = new WeiboCommentHolder();			
			holder.imgv_from = (ImageView)convertView.findViewById(R.id.imgv_from);
			holder.imgv_content = (ImageView)convertView.findViewById(R.id.imgv_content);
			holder.imgv_source = (ImageView)convertView.findViewById(R.id.imgv_source_content);
			holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
			holder.tv_content = (TextView)convertView.findViewById(R.id.tv_content);
			holder.tv_content_source = (TextView)convertView.findViewById(R.id.tv_source_content);
			holder.tv_from = (TextView)convertView.findViewById(R.id.tv_from);
			holder.tv_like_num = (TextView) convertView.findViewById(R.id.tv_like_num);
			holder.tv_forward_num = (TextView)convertView.findViewById(R.id.tv_forward_num);
			holder.tv_comment_num = (TextView)convertView.findViewById(R.id.tv_comment_num);
			holder.layout_forward = (LinearLayout) convertView.findViewById(R.id.layout_forward);
			convertView.setTag(holder);			
		}
		else
		{
			holder = (WeiboCommentHolder) convertView.getTag();
		}
		
		//Load wei content
		if(weiboData != null){
			if(weiboData.from_comment_num != 0)
				holder.tv_comment_num.setText(String.valueOf(weiboData.from_comment_num));
			if(weiboData.from_forward_num != 0)
				holder.tv_forward_num.setText(String.valueOf(weiboData.from_forward_num));
			if(weiboData.from_like_num != 0)
				holder.tv_like_num.setText(String.valueOf(weiboData.from_like_num));
			if(weiboData.source !=null )
				holder.tv_from.setText(String.valueOf(weiboData.source));
			
			if(weiboData.text_content !=null)
				holder.tv_content.setText(weiboData.text_content);
			else
				holder.tv_content.setVisibility(View.GONE);
			
			if(weiboData.image_thumb !=null)
				ImageLoaderUtil.loadImageAsync(holder.imgv_content, weiboData.image_thumb, null, null,100,null);
			else
				holder.imgv_content.setVisibility(View.GONE);
			
			if(weiboData.original_text_content !=null){
				holder.layout_forward.setVisibility(View.VISIBLE);
				holder.tv_content_source.setText(weiboData.original_text_content);
				if(weiboData.original_image_middle !=null)
					ImageLoaderUtil.loadImageAsync(holder.imgv_source, weiboData.original_image_middle, null, null,100,null);
				else
					holder.imgv_source.setVisibility(View.GONE);
			}else{
				holder.tv_content_source.setVisibility(View.GONE);
				holder.layout_forward.setVisibility(View.GONE);
			}
			if(weiboData.from_portrait !=null)
				ImageLoaderUtil.loadImageAsync(holder.imgv_from, weiboData.from_portrait, null, null,100,null);
			
			if(weiboData.from !=null){
				holder.tv_name.setText(weiboData.from);
			}
		}						
		return convertView;
	}
        
    
    private View getWeiboCommentItemView(int position) 
    {	
    	if(position >= weiboList.size())
    		return null;
		weiboData = weiboList.get(position);
		View convertView = null;
		WeiboCommentItemHolder holder = new WeiboCommentItemHolder();			
/*		if(convertView == null)
		{*/
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_sina_weibo_comment_item, null);
			holder = new WeiboCommentItemHolder();			
			holder.imgv_comment_head = (ImageView) convertView.findViewById(R.id.imgv_head);
			holder.tv_comment_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_comment_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_replay_content = (TextView) convertView.findViewById(R.id.tv_replay_content);
			holder.tv_comment_time = (TextView) convertView.findViewById(R.id.tv_time);		
			convertView.setTag(holder);			
	/*	}
		else
		{
			holder = (WeiboCommentItemHolder) convertView.getTag();
		}*/
			
		if(weiboData.from_portrait !=null)
			ImageLoaderUtil.loadImageAsync(holder.imgv_comment_head, weiboData.from_portrait, null, getResources().getDrawable(R.drawable.logo),100,null);
		if(weiboData.from !=null)
			holder.tv_comment_name.setText(weiboData.from);
		if(weiboData.text_content !=null)
			holder.tv_comment_content.setText(weiboData.text_content);
		if(weiboData.create_time !=null)
			holder.tv_comment_time.setText(weiboData.create_time);
		if(weiboData.original_text_content != null)
			holder.tv_replay_content.setText(weiboData.original_text_content);
		else
			holder.tv_replay_content.setVisibility(View.GONE);
		return convertView;
	}
    
	private class WeiboAttentionHolder
	{
		TextView tv_name;
		TextView tv_content;
		TextView tv_content_source;
		TextView tv_from;
		TextView tv_like_num;
		TextView tv_forward_num;
		TextView tv_comment_num;
		TextView tv_time;
		ImageView imgv_from;
		ImageView imgv_content;
		ImageView imgv_source;
		LinearLayout layout_forward;
	}
    
	private class WeiboSingleHolder
	{
		TextView tv_content;
		TextView tv_original;		
		TextView tv_forward_num;
		TextView tv_comment_num;
		TextView tv_like_num;
		TextView tv_from;
		TextView tv_time;
		ImageView imgv_original;
		ImageView imgv_content;
		LinearLayout layout_forward;
	}

	private class WeiboFansHolder
	{
		TextView tv_index;
		TextView tv_fans_name;		
		TextView tv_fans_content;
		ImageView imgv_fans_head;
		ImageView imgv_goto;
	}
	
	private class OnlyCommentHolder
	{
		TextView tv_comment_name;
		TextView tv_comment_content;		
		TextView tv_comment_time;
		TextView tv_replay_content;
		ImageView imgv_comment_head;	
	}
	
	private class WeiboCommentHolder
	{
		TextView tv_name;
		TextView tv_content;
		TextView tv_content_source;
		TextView tv_from;
		TextView tv_like_num;
		TextView tv_forward_num;
		TextView tv_comment_num;
		TextView tv_time;
		ImageView imgv_from;
		ImageView imgv_content;
		ImageView imgv_source;
		LinearLayout layout_forward;
	}
	
	private class WeiboCommentItemHolder
	{
		TextView tv_comment_name;
		TextView tv_comment_content;		
		TextView tv_comment_time;
		TextView tv_replay_content;
		ImageView imgv_comment_head;	
	}
	
	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	} 
}
