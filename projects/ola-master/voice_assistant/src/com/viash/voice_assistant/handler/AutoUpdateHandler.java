package com.viash.voice_assistant.handler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.viash.voice_assistant.common.AutoUpdate;
import com.viash.voice_assistant.service.AutoUpdateNotificationService;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;

public class AutoUpdateHandler extends Handler{
	private final static boolean DEBUG = true;
	private final static String TAG = "AutoUpdate";
	
	private Context mContext;
	private boolean showToast = true;
	
	public AutoUpdateHandler(Context context){
		mContext = context;
	}
	public AutoUpdateHandler(Context context, boolean showToast){
		this.mContext = context;
		this.showToast = showToast;
	}
	
	@Override
	public void handleMessage(Message msg) {
		Intent intent = null;
		
		switch (msg.what) {	
		case MsgConst.MSG_UPDATE_VERSION_NONEWVERSION:
			if(showToast) CustomToast.makeToast(mContext, "沒有新版本");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "沒有新版本");
			break;
		case MsgConst.MSG_UPDATE_VERSION_NEWVERSION:
			if(showToast) CustomToast.makeToast(mContext, "有新版本可以下载更新");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "有新版本可以下载更新");
			//UpdateNotification.init(mContext);
			//UpdateNotification.showCanDownloadNotification();
			//DownloadNotification.addClickDownloadNotification(AutoUpdate.AutoUpdateID, "有新版本可以下载更新", "Click to start download update file.", R.drawable.icon_server_head);
			//AutoUpdate.download();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_ADD_DOWNLOAD);
			mContext.startService(intent);
			break;
		case MsgConst.MSG_UPDATE_VERSION_NOSDCARD:
			if(showToast) CustomToast.makeToast(mContext, "没有 SD-Card");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "没有 SD-Card");
			//DownloadNotification.cancel(AutoUpdate.AutoUpdateID);
			//UpdateNotification.cancel();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
			mContext.startService(intent);
			break;
		case MsgConst.MSG_UPDATE_VERSION_SERVERERROR:
			if(showToast) CustomToast.makeToast(mContext, "更新服务器连接失败");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "更新服务器连接失败");
			//DownloadNotification.cancel(AutoUpdate.AutoUpdateID);
			//UpdateNotification.cancel();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
			mContext.startService(intent);
			break;
		case MsgConst.MSG_UPDATE_VERSION_DOWNLOADSTOP:
			if(showToast) CustomToast.makeToast(mContext, "下载停止");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "下载停止");
			//DownloadNotification.cancel(AutoUpdate.AutoUpdateID);
			//UpdateNotification.cancel();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
			mContext.startService(intent);
			break;
		case MsgConst.MSG_UPDATE_VERSION_DOWNLOADUPDATE:
			int percent = msg.getData().getInt("percent");
			//DownloadNotification.updateNotification(AutoUpdate.AutoUpdateID, percent);
			//UpdateNotification.setProgressBar(percent);
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_UPDATE_PERCENT);
			intent.putExtra("percent", percent);
			mContext.startService(intent);
			break;
		case MsgConst.MSG_UPDATE_VERSION_DOWNLOADSUCCESS:
			if(showToast) CustomToast.makeToast(mContext, "下载完成");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "下载完成");
			//DownloadNotification.cancel(AutoUpdate.AutoUpdateID);
			//UpdateNotification.cancel();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
			mContext.startService(intent);
			
			AutoUpdate.update();
			break;
		case MsgConst.MSG_UPDATE_VERSION_HASUPDATE:
			//DownloadNotification.cancel(AutoUpdate.AutoUpdateID);
			//UpdateNotification.cancel();
			intent = new Intent(mContext, AutoUpdateNotificationService.class);
			intent.setAction(AutoUpdateNotificationService.ACTION_CANCEL);
			mContext.startService(intent);
			
			AutoUpdate.update();
			break;
		case MsgConst.MSG_UPDATE_VERSION_UPDATEFAIL:
			if(showToast) CustomToast.makeToast(mContext, "安装更新失败");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "安装更新失败");
			break;
		case MsgConst.MSG_UPDATE_VERSION_DIRECTLY:
			if(showToast) CustomToast.makeToast(mContext, "有新版本可以下载更新");//, Toast.LENGTH_SHORT).show();
			if(DEBUG) Log.i(TAG, "有新版本可以下载更新");
			AutoUpdate.download(mContext);			
			break;	
		default:
			break;
		}
	}
}
