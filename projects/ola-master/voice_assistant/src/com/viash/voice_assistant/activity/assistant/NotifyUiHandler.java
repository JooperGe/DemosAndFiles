package com.viash.voice_assistant.activity.assistant;

import java.util.List;

import org.json.JSONArray;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.route.RouteResult;
import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.adapter.CommunicationAdapter;
import com.viash.voice_assistant.common.AppDownloadManager;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.widget.HelpGuideDetailView;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voice_assistant.widget.NetworkWarningDialog;
import com.viash.voice_assistant.widget.WidgetViewFactory;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.AppData.MapInfo;
import com.viash.voicelib.data.AppData.RouteMapInfo;
import com.viash.voicelib.data.HelpData.HelpGuideData;
import com.viash.voicelib.data.PreFormatData.BusInfoJsonData;
import com.viash.voicelib.msg.MsgAnswer;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.WeiboUtil;

/**
 * 拆分 主activity
 * 
 * 传递到主activity 触发UI 改变相关的事件
 * 
 * @author fenglei
 *
 */
public class NotifyUiHandler extends Handler {

	private static final boolean DEBUG = true;
	private static final String TAG = "NotifyUiHandler";
	public static final int MSG_WEIBO_TOKEN_RETURN = 60;
	public static final int MSG_LISTVIEW_TO_LAST_PAGE = 61;
	
	
	private NewAssistActivity mainActivity;
	private static NotifyUiHandler _instance = null;

	private NotifyUiHandler(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static NotifyUiHandler init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new NotifyUiHandler(main);
		return _instance;
	}

	public static NotifyUiHandler getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init NotifyUiHandler");

		return _instance;
	}

	@Override
	public void handleMessage(Message msg) {
		if (DEBUG)
			Log.i(TAG, "msg id: " + msg.what);
		/*
		 * if (isPause) { return; }
		 */

		MapInfo mapInfo = null;
		
		switch (msg.what) {
		case MsgConst.MSG_ON_VIEW_TOUCH:
			if (mainActivity.layout_recommend.getVisibility() == View.VISIBLE) {
				mainActivity.layout_recommend.setVisibility(View.INVISIBLE);
			}
			break;

		case MsgConst.MSG_SHOW_WHOLE_SCREEN:
			
			if (mainActivity.layout_selection_whole_View.getVisibility() == View.GONE) {
				BaseData data = (BaseData) msg.obj;
				boolean operationable = false;
				if (msg.arg1 == 1) {
					operationable = true;
				}
				View view = WidgetViewFactory.getWidgetView(
						mainActivity, data, operationable, this,
						true, msg.arg2);
				if (view != null) {
					mainActivity.layout_selection_whole_View.setVisibility(View.VISIBLE);
					mainActivity.layout_selection_whole_View.addView(view);
				}
			}
			break;
		case MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL:
			if (mainActivity.layout_selection_whole_View.getVisibility() == View.VISIBLE) {
				mainActivity.layout_selection_whole_View.removeAllViews();
				mainActivity.layout_selection_whole_View.setVisibility(View.GONE);
			}

			break;

		case MsgConst.MSG_DATA_FROM_TEXT:
			
			if ((mainActivity.mMapView.getVisibility() == View.VISIBLE)
					|| (mainActivity.layout_webView.getVisibility() == View.VISIBLE)) {
				mainActivity.hideTopView(true);
			}
			MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_START_RECOGNITION,
					null, msg.obj);
			break;
		case MsgConst.MSG_DATA_FROM_TEXT_SHARE:
			Tts.stop(Tts.TTS_NORMAL_PRIORITY);

			break;
		case MSG_WEIBO_TOKEN_RETURN:
			JSONArray array = new JSONArray();
			array.put(msg.obj);
			Bundle bundle = new Bundle();
			bundle.putString("type", "sina_weibo_token");

			MsgSender.getInstantce().sendMessageToService(
					MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
					bundle, array);
			WeiboUtil.releaseWeibo();
			break;
		case MsgConst.MSG_DATA_FROM_OPTION:
			MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_SELECTION_ANSWER,
					msg.arg1, msg.arg2);
			if (mainActivity.layout_recommend.getVisibility() == View.VISIBLE) {
				mainActivity.layout_recommend.setVisibility(View.INVISIBLE);
			}
			break;
		case MsgConst.MSG_COPY_TEXT_FROM_ITEM:
			mainActivity.prepareSendText((String) msg.obj);
			break;
		case MsgConst.MSG_SHOW_MAP:
			mapInfo = (MapInfo) msg.obj;
			if (mapInfo != null)
				MapHelper.getInstantce().showMap(mapInfo);//mainActivity.showMap(mapInfo);
			break;
		case MsgConst.MSG_SHOW_MAP_BUSINFO:
			BusInfoJsonData data = (BusInfoJsonData) msg.obj;
			if (data != null) {
				MapHelper.getInstantce().showBusLineInfo(data, msg.arg1);
			}
			break;
		case MsgConst.MSG_ROUTE_SEARCH_RESULT:
			MapHelper.getInstantce().showRoute((RouteResult) msg.obj);
			break;
		case MsgConst.MSG_SHOW_ROUTE_MAP:
			RouteMapInfo routeInfo = (RouteMapInfo) msg.obj;
			if (routeInfo != null) {
				MapHelper.getInstantce().startNavigate((double) routeInfo.mRouteFromLatitude[0],
						(double) routeInfo.mRouteFromLongitude[0],
						(double) routeInfo.mRouteToLatitude[0],
						(double) routeInfo.mRouteToLongitude[0],
						(int) routeInfo.mRouteMode[0]);
			}
			break;
		case MsgConst.MSG_SEARCH_POS:
			mapInfo = (MapInfo) msg.obj;
			if (mapInfo != null)
				MapHelper.getInstantce().searchPos(mapInfo);
			break;
		case MsgConst.MSG_NAVIGATE_TO:
			if (msg.arg1 == 0) {
				LatLng point = (LatLng) msg.obj;
				double toLat = point.latitude;
				double toLong = point.longitude;
				if (toLat != 0 && toLong != 0)
					MapHelper.getInstantce().startNavigate(0, 0, toLat, toLong, 0);
			} else {
				MapHelper.getInstantce().startNavigate((String) msg.obj);
			}
			break;
		case MsgConst.MSG_SHOW_INTERNAL_WEB:
			mainActivity.showInternalWeb((String) msg.obj, ((msg.arg1 == 1) ? true : false));
			break;
		case MsgConst.MSG_SHOW_WEB:
			Intent intent = new Intent();
			Uri uri = Uri.parse((String) msg.obj);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			try {
				mainActivity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				CustomToast.makeToast(mainActivity, mainActivity.getResources()
						.getString(R.string.newassistactivity_can_not_open)
						+ "\"" + uri.getPath() + "\"");
				// Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			break;
		case MsgConst.MSG_FORCE_STOP_TTS:
			try {
				if (mainActivity.mtServiceMessenger != null)
					mainActivity.mtServiceMessenger.send(Message.obtain(null,
							MsgConst.CLIENT_ACTION_STOP_TTS));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mainActivity.mMusicPlayerView.speakRecover();
			break;
		case MsgConst.MSG_DOWNLOAD_APP:
			String title_str = msg.getData().getString("title");
			String logoPath = msg.getData().getString("logo");

			if (!AppDownloadManager.startDownload(mainActivity.getApplicationContext(),
					this, title_str, (String) msg.obj, logoPath)) {
				CustomToast.makeToast(
						mainActivity,
						mainActivity.getResources().getString(
								R.string.newassistactivity_sd_card_not_exist));
				// Toast.LENGTH_SHORT).show();
			}
			break;
		case MsgConst.MSG_SIM_VOICE_BUTTON:
			Log.i("Media", "MSG_SIM_VOICE_BUTTON");
			mainActivity.onVoiceBtnClicked(0);
			break;
		/*
		 * case MsgConst.MSG_CHECK_TTS: startTipsEvent(); break;
		 */
		// Minimize the music view
		case MSG_LISTVIEW_TO_LAST_PAGE:
			mainActivity.mLstView.setSelection(mainActivity.mLstView.getAdapter().getCount() - 1);
			break;
		// show tips
		/*
		 * case MsgConst.MSG_SHOW_TIPS: clearTipsEvent(true); if
		 * ((tipsView.getVisibility() != View.VISIBLE) &&
		 * SavedData.isHelpTips()) { if (tipsView.refreshTipData()) { //
		 * Log.d(TAG, "tips update success");
		 * tipsView.setVisibility(View.VISIBLE);
		 * mHandler.sendEmptyMessageDelayed( MsgConst.MSG_TIPS_TIMEOUT,
		 * tipsView.getDurationTime()); } else { Log.d(TAG,
		 * "tips not updated, as all tips was displayed in one week");
		 * tipsView.setVisibility(View.GONE); } } break; case
		 * MsgConst.MSG_TIPS_TIMEOUT: clearTipsEvent(false); if
		 * (tipsView.getVisibility() == View.VISIBLE) {
		 * tipsView.setVisibility(View.GONE); startTipsEvent(); } break;
		 */
		case MsgConst.MSG_DRAWER_UPDATE_SCROLLVIEW_LAYOUT:
			// updateDrawerLayout();
			break;
		case MsgConst.MSG_DRAWER_UPDATE_PAGE_LAYOUT:
			/*
			 * int totalPages = (int) msg.arg1; int currentPage = (int)
			 * msg.arg2;
			 */
			// updateDrawerPageLayout(totalPages, currentPage);
			break;
		case MsgConst.MSG_CAMERA_OPERATION:
			CameraHelper.getInstantce().cameraOperation();
			break;
		case MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE:
			CameraHelper.getInstantce().cameraRestoreToBefore();
			break;
		case VoiceAssistantService.MSG_UI_SEARCHING_START:
			mainActivity.mSearchingDialog = new ProgressDialog(mainActivity);
			mainActivity.mSearchingDialog.setTitle(mainActivity.getResources().getString(
					R.string.bluetooth_searching_title));
			mainActivity.mSearchingDialog.setMessage(mainActivity.getResources().getString(
					R.string.bluetooth_searching_message));
			mainActivity.mSearchingDialog.show();
			break;
		case VoiceAssistantService.MSG_UI_SEARCHING_FOUND:
			mainActivity.mSearchingDialog.cancel();
			break;
		case MsgConst.CLIENT_ACTION_REMOVE_DATA:
			if (msg.obj instanceof BaseData) {
				mainActivity.mAdapter.removeData((BaseData) msg.obj);
			}
			if (msg.obj instanceof CommunicationData) {
				mainActivity.mAdapter.removeData((CommunicationData) msg.obj);
			}
			break;
		case MsgConst.CLIENT_ACTION_ADD_DATA:
			mainActivity.mAdapter.addData((CommunicationData) msg.obj);
			if (msg.arg1 == NetworkWarningDialog.VIDEO_PLAY_TO_REMIND) {
				mainActivity.videoPlayRemind = false;
			} else if (msg.arg1 == NetworkWarningDialog.DOWNLOAD_TO_REMIND) {
				mainActivity.downloadRemind = false;
			}
			break;
		case MsgConst.CLIENT_ACTION_SHOW_MUSIC_LIST:
			if (msg.arg1 == NetworkWarningDialog.MUSIC_PLAY_TO_REMIND) {
				mainActivity.musicPlayRemind = false;
			}
			@SuppressWarnings("unchecked")
			List<String[]> lstData = (List<String[]>) msg.obj;
			// requestMusicFocus(true);
			if (lstData != null)
				mainActivity.playMusicList(lstData);
			break;
		/*
		 * case MsgConst.CLIENT_ACTION_SHOW_NEW_ICON_ON_PROMO_BUTTON:
		 * image_drawer_icon_new.setVisibility(View.VISIBLE); break;
		 */
		case MsgConst.CLIENT_ACTION_UPDATA_USER_LOG_STATUS:
			UIStateHelper.getInstantce().updateStatusView();
			break;
		case MsgConst.CLIENT_ACTION_REPORT_UI_INFO:
			Bundle newbundle = new Bundle();
			newbundle.putString("param", (String) msg.obj);
			MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_REPORT_UI_INFO,
					newbundle);
			break;
		case MsgConst.SERVICE_ACTION_SDKCOMMAND_RESPONSE:
			mainActivity.mServerResponsed = true;
			Bundle bundletemp = msg.getData();
			String cmd = bundletemp.getString("commandname");
			String param1 = bundletemp.getString("param1");
			String param2 = bundletemp.getString("param2");
			mainActivity.mAdapter.handlerMsg(cmd, param1, param2);
			UIStateHelper.getInstantce().setProcessingState(MsgConst.UI_STATE_INITED);
			break;
		case MsgConst.MSG_MUSIC_CONTROL:
			if (MusicPlayerView.musicList != null
					&& !MusicPlayerView.musicList.isEmpty()) {
				mainActivity.showTopView(mainActivity.mMusicPlayerView);
				if (mainActivity.mMusicPlayerView != null) {
					mainActivity.mMusicPlayerView.controlMusic((String) msg.obj, msg.arg1);
				}
			}
			break;
		case MsgConst.MSG_MUSIC_STOP:
			if (mainActivity.mMusicPlayerView != null) {
				mainActivity.mMusicPlayerView.closeMusicServer();
				mainActivity.mLstMusicView.setVisibility(View.GONE);
				mainActivity.mMusicPlayerView.setVisibility(View.GONE);
			}
			break;
		case MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE_DETAIL:
			mainActivity.helpGuideDetailView = new HelpGuideDetailView(
					mainActivity, this, (HelpGuideData) msg.obj);
			mainActivity.layout_guide_help.removeAllViews();
			mainActivity.mBtn_Login.setImageResource(R.drawable.go_back_button);
			mainActivity.layout_guide_help.setVisibility(View.VISIBLE);
			mainActivity.layout_guide_help.addView(mainActivity.helpGuideDetailView.initView());
			mainActivity.btn_goback.setVisibility(View.VISIBLE);
			break;
		case MsgConst.CLIENT_ACTION_CLOSE_HELP_GUIDE_DETAIL:
			if (mainActivity.layout_guide_help.getVisibility() == View.VISIBLE) {
				mainActivity.layout_guide_help.setVisibility(View.GONE);
				mainActivity.mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
				mainActivity.btn_goback.setVisibility(View.INVISIBLE);
			}
			break;
		case MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE:
			if (mainActivity.layout_guide_help.getVisibility() == View.VISIBLE) {
				mainActivity.layout_guide_help.setVisibility(View.GONE);
				mainActivity.mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
			}
			if (mainActivity.mLayoutHelp.getVisibility() == View.VISIBLE) {
				mainActivity.mLayoutHelp.setVisibility(View.GONE);
				mainActivity.mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
				HelpStatisticsUtil.helpType = null;
			}
			mainActivity.btn_goback.setVisibility(View.INVISIBLE);
			mainActivity.showHelpGuideView();
			break;
		case MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE:
			mainActivity.mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
			mainActivity.btn_goback.setVisibility(View.INVISIBLE);
			mainActivity.layout_guide_help.setVisibility(View.GONE);
			HelpStatisticsUtil.helpType = null;
			HelpStatisticsUtil.currentType = null;
			break;
		case MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW:
			if (mainActivity.mLayoutHelp.getVisibility() == View.VISIBLE) {
				mainActivity.mLayoutHelp.setVisibility(View.GONE);
				if (mainActivity.layout_guide_help.getVisibility() == View.VISIBLE) {
					mainActivity.mBtn_Login.setImageResource(R.drawable.go_back_button);
					mainActivity.btn_goback.setVisibility(View.VISIBLE);
				} else {
					mainActivity.mBtn_Login
							.setImageResource(R.drawable.statusbar_login_button);
					mainActivity.btn_goback.setVisibility(View.INVISIBLE);
				}
				HelpStatisticsUtil.helpType = null;
			} else {
				mainActivity.mLayoutHelp.setVisibility(View.VISIBLE);
				mainActivity.mBtn_Login.setImageResource(R.drawable.go_back_button);
				mainActivity.btn_goback.setVisibility(View.VISIBLE);
				HelpStatisticsUtil.helpType = "more";
			}
			break;
		case MsgConst.CLIENT_ACTION_START_CAPTURE:
			MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE, null);
			break;
		case MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING:
			msg = Message.obtain(null,
					MsgConst.CLIENT_ACTION_START_WITH_INDICATION_STRING);
			MsgAnswer msgAnswer = new MsgAnswer(
					"{\"Just Talk Dialog Outputs\":[{\"Question\":{\"Present\":\"3\",\"Content\":\"主人，有什么可以帮你的？\",\"tag\":\"login\"}}]}");
			msg.obj = msgAnswer;
			msg.arg1 = mainActivity.isNeedUnlock ? 1 : 0;
			MsgSender.getInstantce().sendMessageToService(msg);
			break;
		}
	}
}
