package com.viash.voice_assistant.widget.selection;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.AppDownloadManager;
import com.viash.voice_assistant.component.DownloadNotification;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.ImageLoaderUtil;
import com.viash.voicelib.utils.LocalPathUtil;
import com.viash.voicelib.utils.LogOutput;
/**
 * 选择音乐
 * @author Leo
 * @createDate 2013-12-25
 * @
 */
@SuppressLint("ViewConstructor")
public class AppDownloadView extends SelectionBaseView {
	private static final String TAG = "AppDownloadView";
	
	private List<Boolean> isDownloading = new ArrayList<Boolean>();
	private String stop_id = null;
	private List<String> mOptionData;
	private ArrayList<View> lView = null;
	public AppDownloadView(Context context, OptionData data,boolean operationEnable,Handler mHandler,boolean showInMainScreen) {
		super(context,data,operationEnable, mHandler, showInMainScreen);
		for(int i = 0; i < mOptionData.size(); i++) {
			isDownloading.add(false);
		}
		// add receive broadcast
		//IntentFilter filter = new IntentFilter();
		//filter.addAction(AppDownloadManager.APP_DOWNLOAD_FAIL);
		//filter.addAction(DownloadNotification.NOTIFICATION_CANCEL_ACTION);
		//context.registerReceiver(new Receiver(), filter);
	}
		
/*	private void setBtnDownload(Button btn_download, int position, String stop_id) {
		if(isDownloading.get(position)){
			btn_download.setText(getResources().getString(R.string.downloading));
			btn_download.setEnabled(false);
		}
		
		DownloadInfoHolder dholder = (DownloadInfoHolder) btn_download.getTag();
		String[] array = dholder.array; 
		if(array !=null && array.length > 6 ){
			String url_str=null;
			if(array.length > 6)
				 url_str = array[6];
			if(url_str != null && url_str.equals(stop_id)){
				btn_download.setText(getResources().getString(R.string.download));
				btn_download.setEnabled(true);
				isDownloading.set(position, false);
			}
		}
	}*/

	class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DownloadNotification.NOTIFICATION_CANCEL_ACTION)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					final String id = bundle.getString("id");
					if (id != null) {
						Log.i(TAG, "stop download apk: " + id);
						mHandler.post(new Runnable(){
							@Override
							public void run() {
								stop_id = id;
								mNormalContent.removeAllViews();
								setDataView();
								
							}
						});
					}
				}
			} else if (intent.getAction().equals(AppDownloadManager.APP_DOWNLOAD_FAIL)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					final String id = bundle.getString("id");
					if (id != null) {
						Log.i(TAG, "download apk fail: " + id);
						mHandler.post(new Runnable(){
							@Override
							public void run() {
								stop_id = id;
								mNormalContent.removeAllViews();
								setDataView();
							}
						});
					}
				}
			}
		}
	}

	@Override
	protected void initSelectionViewData() {
		mViewData.mPrimaryTitleImg = R.drawable.icons_download;
		mViewData.mPrimaryTitleText = mContext.getString(R.string.download);
		mViewData.mSecondaryTitleText = null;
		
/*		mViewData.mFilterOrCommFunText = null;
		mViewData.mFilters = null;*/
		
		mViewData.mContentFunImg = 0;
		mViewData.mContentFunText = null;
		
		mViewData.mMinItemNumber = 3;
		mViewData.mTotleItemNumber = ((OptionData) mCommunicationData).getOptions().size();
		mViewData.mHighLight = 0;		
	}
	
	@Override
	protected void initDataView(){
		super.initDataView();
		mOptionData = ((OptionData) mCommunicationData).getOptions();
		setListContentavaible(false);//Content view is not a list view.
		setDataView();
	}
	
	private void setDataView()
	{
		lView = new ArrayList<View>();
		if(!isFullScreen)
		{
			int count = 0;
			if(mViewData.mTotleItemNumber <= mViewData.mMinItemNumber)
			    count = mViewData.mTotleItemNumber;
			else
				count = mViewData.mMinItemNumber;
			for(int i= 0; i<count; i++)
			{
				mNormalContent.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				mNormalContent.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		else
		{
			ScrollView sView = new ScrollView(mContext);
			LayoutParams param= new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			LinearLayout layout_content = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_bus, null).findViewById(R.id.layout_content);
			sView.setLayoutParams(param);
			for(int i= 0; i< mViewData.mTotleItemNumber; i++)
			{
				layout_content.addView(getView(i));
				ImageView viewbg = new ImageView(getContext());
				viewbg.setImageResource(R.drawable.bg_line);
				viewbg.setScaleType(ScaleType.FIT_XY);
				layout_content.addView(viewbg,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
			sView.addView(layout_content);
			mNormalContent.addView(sView);
		}
	}
			
	public View getView(int position) 
	{
		View convertView = null;
		ViewHolder holder;
		String itemContent = mOptionData.get(position);
		if(lView != null)
		{
		  if(lView.size() >= position+1)
		  {
		    convertView = lView.get(position);
		  }
		}
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.selection_download_app_item, null);
			holder.tv_index = (TextView) convertView.findViewById(R.id.tv_index);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
			holder.tv_content_summary = (TextView) convertView.findViewById(R.id.tv_content_summary);
			holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
			holder.imgv_logo = (ImageView) convertView.findViewById(R.id.imgv_logo);
			holder.imgv_download = (ImageView) convertView.findViewById(R.id.imgv_download);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_index.setText(Integer.toString(position+1));
		String[] itemArray=itemContent.split("\\Q$$\\E");
		if(itemContent!=null && itemContent.indexOf("$$")>0)
		{				
			if(itemArray !=null && itemArray.length > 0)
			{
				holder.tv_name.setText(itemArray[0]);
				if(itemArray.length > 1 )
					holder.tv_size.setText(itemArray[1]);
				if(itemArray.length > 4)
					ImageLoaderUtil.loadImageAsync(holder.imgv_logo, itemArray[4], LocalPathUtil.IMAGE_APP_LOGO, getResources().getDrawable(R.drawable.logo), 100,null);
				if(itemArray.length > 3 && itemArray[3] !=null && itemArray[3].length() > 0)
				{
					holder.tv_des.setText(itemArray[3].trim());
					holder.tv_des.setVisibility(View.GONE);
				}
			 }
		}
		
		holder.tv_content_summary.setVisibility(View.GONE);		
		if(operationEnable)
		{			
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ViewHolder holder = (ViewHolder) v.getTag();
					if(holder.tv_des.getVisibility() == View.GONE)
					{
						holder.tv_content_summary.setVisibility(View.VISIBLE);
						holder.tv_des.setVisibility(View.VISIBLE);
					}
					else
					{
						holder.tv_content_summary.setVisibility(View.GONE);
						holder.tv_des.setVisibility(View.GONE);
					}
				}
				
			});
			
			if(itemArray.length > 5){
				DownloadInfoHolder downloadInfoHolder = new DownloadInfoHolder();
				downloadInfoHolder.index = position;
				downloadInfoHolder.array = itemArray;
				holder.imgv_download.setTag(downloadInfoHolder);
				if(stop_id != null)
				{
					//setBtnDownload(holder.imgv_download, position, stop_id);
				}
				holder.imgv_download.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//Button btn=(Button) v;
						//btn.setText(getResources().getString(R.string.downloading));
						DownloadInfoHolder dholder = (DownloadInfoHolder) v.getTag();
						
						String[] array = dholder.array; 
						if(array !=null && array.length > 6 ){
							String title_str = array[0];
							String url_str=null;
							if(array.length > 6)
								 url_str =array[6];
							if(title_str!=null && !"".equals(title_str) && url_str !=null && !"".equals(url_str)){
								if(!AppDownloadManager.isUrlDownloading(url_str)){
									String logo =null;
									if(array.length > 4)
										logo= array[4];
									if(logo != null && logo.length() > 0)
										logo =LocalPathUtil.IMAGE_APP_LOGO + ImageLoaderUtil.MD5(logo);
									Message msg = mHandler.obtainMessage(MsgConst.MSG_DOWNLOAD_APP);
									msg.obj = url_str;
									Bundle bundle = new Bundle();
									bundle.putString("title", title_str);
									bundle.putString("logo", logo);
									msg.setData(bundle);
									mHandler.sendMessage(msg);
									//btn.setEnabled(false);
									isDownloading.set(dholder.index, true);
									CustomToast.showShortText(getContext(), title_str + " 正在下载中...");
									CustomToast.makeToast(mContext, title_str + " 正在下载中...");//, Toast.LENGTH_SHORT).show();
								}else{
									CustomToast.showShortText(getContext(), "该文件正在下载！");
									CustomToast.makeToast(mContext, "该文件正在下载！");//, Toast.LENGTH_SHORT).show();
								}
							}else{
								LogOutput.w(TAG, "file title or url is null");
								CustomToast.makeToast(mContext, "未找到下载链接");//, Toast.LENGTH_SHORT).show();
							}
						}/*else{
							CustomToast.showShortText(getContext(), "下载失败，请重试！");
						}*/
						
					}
				});
			}
		}else
		{
			//holder.imgv_download.setEnabled(false);			
		}
		return convertView;
	}
	
	private class ViewHolder{
		TextView tv_index;
		TextView tv_name;
		TextView tv_size;
		TextView tv_content_summary;
		TextView tv_des;
		ImageView imgv_logo;
		ImageView imgv_download;
	}
	

	private class DownloadInfoHolder
	{
		int index;
		String[] array;
	}

	@Override
	public void handleServerCmd() {
		// TODO Auto-generated method stub
		
	}
}