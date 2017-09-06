package com.viash.voice_assistant.activity.assistant;

import java.io.File;
import java.util.List;

import org.json.JSONArray;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.data.GlobalData;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.service.LocationActionData;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voice_assistant.widget.MusicPlayerView;
import com.viash.voice_assistant.widget.NetworkWarningDialog;
import com.viash.voicelib.data.AppData;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.CommunicationData;
import com.viash.voicelib.data.DataConst;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.SilentInfoData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.LogOutput;
import com.viash.voicelib.utils.MusicUtil;
import com.viash.voicelib.utils.NetWorkUtil;
import com.viash.voicelib.utils.WeiboUtil;
import com.viash.voicelib.utils.MusicUtil.MusicInfo;

/**
 * 拆分主activity功能
 * 
 * 
 * 
 * @author fenglei
 *
 */
public class InComingHandler extends Handler {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "InComingHandler";
	
	private NewAssistActivity mainActivity;
	private static InComingHandler _instance = null;

	private InComingHandler(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static InComingHandler init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new InComingHandler(main);
		return _instance;
	}

	public static InComingHandler getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init InComingHandler");

		return _instance;
	}
	
	@Override
	public void handleMessage(final Message msg) {
		if (DEBUG)
			Log.d(TAG, "service action: " + msg.what);
/*			if (isPause) {
			return;
		}*/
		Message newMsg = new Message();
	
		switch (msg.what) {
		/*
		 * Service Command
		 */
		case MsgConst.SERVICE_ACTION_QUERY_WEIBO:
			WeiboUtil.queryWeiboToken(mainActivity, NotifyUiHandler.getInstantce(),
					NotifyUiHandler.MSG_WEIBO_TOKEN_RETURN);
			break;
		case MsgConst.SERVICE_ACTION_QUERY_MUSIC:
			if (MusicPlayerView.musicList != null
					&& MusicPlayerView.musicList.size() > 0) {
				JSONArray objArray = new JSONArray();

				MusicEntity musicEntity = MusicPlayerView.musicList
						.get(mainActivity.mMusicPlayerView.position);
				MusicInfo musicInfo = MusicUtil.getMusicPlaying(
						String.valueOf(musicEntity.getId()),
						musicEntity.getName(), musicEntity.getAuthor(),
						null, musicEntity.getUrl(), musicEntity.getPhoto());
				objArray.put(musicInfo.toJsonObject());

				MsgSender.getInstantce().sendMessageToService(
						MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
						msg.getData(), objArray);
			}else {
				//As music APP requested. when no music player, send back blank msg if server query music playing. 
				JSONArray objArray = new JSONArray();
				objArray.put("");
				MsgSender.getInstantce().sendMessageToService(
						MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
						msg.getData(), objArray);
			}
			break;
		case MsgConst.SERVICE_ACTION_QUERY_POSITION_ALARM:
			try {
				JSONArray objArray = new JSONArray();

				List<LocationActionData> result = ServiceHolder.getInstantce().getmMainService()
						.queryLocationAlert();
				if (result != null && result.size() > 0) {
					for (LocationActionData locationActionData : result) {
						objArray.put(locationActionData.toJsonObject());
					}
				}

				MsgSender.getInstantce().sendMessageToService(
						MsgConst.CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED,
						msg.getData(), objArray);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		case MsgConst.SERVICE_ACTION_CLOSE_SPLASH:
			if (DEBUG)
				Log.d(TAG, "closeSplash.");
			SplashHelper.getInstantce().closeSplash();
			break;
		case MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE:
			int state = msg.getData().getInt("state");
			if (DEBUG)
				Log.d(TAG, "setProcessingState: " + state);
			UIStateHelper.getInstantce().setProcessingState(state);
			break;
		case MsgConst.SERVICE_ACTION_UPDATE_ADAPTER_DATA:
			CommunicationData commData = (CommunicationData) msg.obj;
			if (commData != null) {
				Log.i("CalculateTime",
						"CommunicationData added:"
								+ System.currentTimeMillis() % 10000);
				
				if (!commData.isSilentInfoMsg()) {
					mainActivity.getMessageFromLock = SavedData.getLockMessage();
					if (mainActivity.getMessageFromLock != null
							&& !mainActivity.getMessageFromLock.equals("") && commData.getFrom() == DataConst.FROM_SERVER) {
						Message textmsg = NotifyUiHandler.getInstantce()
								.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
						textmsg.obj = mainActivity.getMessageFromLock;
						NotifyUiHandler.getInstantce().sendMessage(textmsg);
						SavedData.setLockMessage(null);
					}else {
						if(commData.getFrom() == DataConst.FROM_SERVER)
						{
							mainActivity.mServerResponsed = true;
						}
						else
						{
							mainActivity.mServerResponsed = false;
						}
						
						boolean showDialog = false;
						boolean isAppMusic = false;
						int whichKindRemind = NetworkWarningDialog.NONE_KIND_TO_REMIND;
						if(!NetWorkUtil.isWIFIConnected(mainActivity.getApplicationContext()) && SavedData.isNetworkTips())
						{
							for(BaseData item : commData.getLstData() ) {
								if(item instanceof OptionData) {
									int id = ((OptionData)item).getOptionId();
									if((id == OptionData.OPTION_VIDEO) && mainActivity.videoPlayRemind) {
										showDialog = true;
										whichKindRemind = NetworkWarningDialog.VIDEO_PLAY_TO_REMIND;
									}else if((id == OptionData.OPTION_APP_NAME) && mainActivity.downloadRemind)
									{
										showDialog = true;
										whichKindRemind = NetworkWarningDialog.DOWNLOAD_TO_REMIND;
									}
								}
								if (item instanceof AppData) {
									if (((AppData)(item)).getAppName().equals(AppData.APP_NAME_MUSIC) && mainActivity.musicPlayRemind) {
										showDialog = true;
										isAppMusic = true;
										whichKindRemind = NetworkWarningDialog.MUSIC_PLAY_TO_REMIND;
									}else if(((AppData)(item)).getAppName().equals(AppData.APP_NANE_DOWNLOAD) && mainActivity.downloadRemind)
									{
										showDialog = true;
										isAppMusic = false;
										whichKindRemind = NetworkWarningDialog.DOWNLOAD_TO_REMIND;
									}
									
								}
							}
							
						}
						if (showDialog) {
							mainActivity.networkWarningDialog = new NetworkWarningDialog(mainActivity);
							mainActivity.networkWarningDialog.setHandlerAndData(NotifyUiHandler.getInstantce(),commData,isAppMusic,whichKindRemind);
							mainActivity.networkWarningDialog.showDialog();								
						}else {
							mainActivity.mAdapter.addData(commData);
						}
					}
				}
				if(mainActivity.mMusicPlayerView != null)
					mainActivity.mMusicPlayerView.hideMusicList();
				if (commData.getFrom() == DataConst.FROM_SERVER) {						
					SilentInfoData silentData = commData
							.getSilentInfoData();
					if (silentData != null) {
						SavedData.setmUserInfo(silentData.getmLevel(),
								silentData.getmScore(),
								silentData.getmNextLevelScore(),
								silentData.getmSpecialTime());
						//isUserLoggedin = true;
					}

					UIStateHelper.getInstantce().updateStatusView();
				}
			}
			mainActivity.mLstView.setSelection(mainActivity.mLstView.getCount()-1);
			break;
		case MsgConst.SERVICE_ACTION_UPDATE_VOICE_VOLUME:
			int volume = msg.getData().getInt("volume");
			UIStateHelper.getInstantce().updateMicImage(volume);
			break;
		case MsgConst.SERVICE_ACTION_SERVER_RESPONSE:
			break;
		case MsgConst.SERVICE_ACTION_CAPTURE_VIEW:
			File file = ScreenCaptureHelper.getInstantce().startCaptureImage();
			newMsg.copyFrom(msg);
			newMsg.what = MsgConst.CLIENT_ACTION_CAPTURE_VIEW_OK;
			Bundle bundle = newMsg.getData();
			if (file != null) {
				bundle.putString("file", file.getAbsolutePath());
			} else {
				bundle.putString("file", null);
			}
			newMsg.setData(bundle);

			try {
				if (mainActivity.mtServiceMessenger != null) {
					mainActivity.mtServiceMessenger.send(newMsg);
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			break;
		/*
		 * Other commands
		 */
		case MsgConst.MSG_MUSIC_PLAY:
			if(!NetWorkUtil.isWIFIConnected(mainActivity.getApplicationContext()) && SavedData.isNetworkTips() && mainActivity.musicPlayRemind)
			{
				if(mainActivity.networkWarningDialog != null)
				{
					mainActivity.networkWarningDialog.setMusicData((List<String[]>)msg.obj);
				}					
			}else
			{
				@SuppressWarnings("unchecked")
				List<String[]> lstData = (List<String[]>) msg.obj;
				// requestMusicFocus(true);
				mainActivity.playMusicList(lstData);
			}
			break;
		case MsgConst.MSG_MUSIC_CONTROL:
			if (MusicPlayerView.musicList != null && !MusicPlayerView.musicList.isEmpty()) {
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
		case MsgConst.MSG_SHOW_ERROR:
			CustomToast.makeToast(mainActivity, (String) msg.obj);
					//Toast.LENGTH_SHORT).show();
			break;
		case MsgConst.MSG_SEND_TO_WEIXIN:
			int to = msg.arg1;
			int type = msg.arg2;
			@SuppressWarnings("unchecked")
			List<String> txtDatas = (List<String>) msg.obj;
			// txtDatas, it contains title,description,url
			if (txtDatas.size() < 3) {
				LogOutput.e(TAG, "MSG_SEND_TO_WEIXIN parameters error");
				return;
			}
			String title = txtDatas.get(0);
			String description = txtDatas.get(1);
			String url = txtDatas.get(2);
			ShareHelper.getInstantce().send2Weixin(to, type, title, description, url);
			break;
		case MsgConst.MSG_SEND_TO_RENREN:
			int pto = msg.arg1;
			int ptype = msg.arg2;
			@SuppressWarnings("unchecked")
			List<String> ptxtDatas = (List<String>) msg.obj;
			if (ptxtDatas.size() < 3) {
				LogOutput.e(TAG, "MSG_SEND_TO_RENREN parameters error");
				return;
			}
			String ptitle = ptxtDatas.get(0);
			String pdescription = ptxtDatas.get(1);
			String purl = ptxtDatas.get(2);
			ShareHelper.getInstantce().send2Renren(pto, ptype, ptitle, pdescription, purl);
			break;
		case MsgConst.MSG_TAKE_PHOTO:
			int preview = msg.arg1;
			CameraHelper.getInstantce().takePictureByCamera(preview);
			break;
		case MsgConst.MSG_POSITION_ALARM_ADDED:
			int longitude = msg.getData().getInt("longitude");
			int latitude = msg.getData().getInt("longitude");
			int longitude_range = msg.getData().getInt("longitude_range");
			int latitude_range = msg.getData().getInt("latitude_range");
			String position_name = msg.getData().getString("position_name");
			String alarm_title = msg.getData().getString("alarm_title");

			Parcel parcel = Parcel.obtain();
			parcel.writeInt(longitude);
			parcel.writeInt(latitude);
			parcel.writeInt(longitude_range);
			parcel.writeInt(latitude_range);
			parcel.writeString(position_name);
			parcel.writeInt(0);
			parcel.writeString(alarm_title);
			parcel.setDataPosition(0);
			LocationActionData action = new LocationActionData(parcel);

			try {
//				if (!mainActivity.mMainService.addLocationAlert(action)) {
				if (!ServiceHolder.getInstantce().getmMainService().addLocationAlert(action)) {
					Log.i(TAG, "mMainService.addLocationAlert failed.");
				}
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException: " + e.getMessage());
				e.printStackTrace();
			}
			break;
		case MsgConst.MSG_POSITION_ALARM_DELETED:
			if (msg.obj != null) {
				long[] result = (long[]) msg.obj;
				int[] ids = new int[result.length];
				for (int i = 0; i < result.length; i++) {
					ids[i] = (int) result[i];
				}
				try {
					if (!ServiceHolder.getInstantce().getmMainService().deleteLocationAlert(ids)) {
						Log.i(TAG,
								"mMainService.deleteLocationAlert failed.");
					}
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException: " + e.getMessage());
					e.printStackTrace();
				}
			}
			break;
		case MsgConst.SERVICE_ACTION_SERVER_CONNECTING:
			mainActivity.mServerState = MsgConst.STATE_SERVER_CONNECTING;
			break;
		case MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED:
		case MsgConst.SERVICE_ACTION_SERVER_BROKEN:
			mainActivity.mServerResponsed = true;
			mainActivity.mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;
			Message riddlegame = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_SHOW_WHOLE_SCREEN_CANCEL);
			riddlegame.obj =  "puzzlegame";
			NotifyUiHandler.getInstantce().sendMessage(riddlegame);
			break;
		case MsgConst.SERVICE_ACTION_SERVER_CONNECTED:
			mainActivity.mServerState = MsgConst.STATE_SERVER_CONNECTED;
			break;
		case MsgConst.SERVICE_ACTION_TTS_PLAY_END:
			if (SavedData.getmAutoStartRecord() && msg.arg1 == 1
					 && mainActivity.layout_voice.getVisibility() == View.VISIBLE) {
				MsgSender.getInstantce().sendMessageToService(MsgConst.CLIENT_ACTION_START_CAPTURE, 0, 0);
			}
			else {
				if(SavedData.isVoiceWakeUpOpen())
					MsgSender.getInstantce().sendMessageToService(MsgConst.MSG_START_CAPTURE_OFFLINE, 0, 0);
				mainActivity.mMusicPlayerView.speakRecover();
			}
			break;
		case MsgConst.SERVICE_ACTION_TTS_PLAY_START:
			mainActivity.mMusicPlayerView.speakPause();
			break;
		case MsgConst.MSG_CALL_START:
			mainActivity.isCalling = true;
			mainActivity.mMusicPlayerView.speakPause();
			break;
		case MsgConst.MSG_CALL_END:
			mainActivity.isCalling = false;
			mainActivity.mMusicPlayerView.speakRecover();
			break;
		case VoiceAssistantService.MSG_UI_SEARCHING_START:
			mainActivity.mSearchingDialog = new ProgressDialog(mainActivity);
			mainActivity.mSearchingDialog.setTitle(mainActivity.getResources().getString(R.string.bluetooth_searching_title));
			mainActivity.mSearchingDialog.setMessage(mainActivity.getResources().getString(R.string.bluetooth_searching_message));
			mainActivity.mSearchingDialog.show();
			break;
		case VoiceAssistantService.MSG_UI_SEARCHING_FOUND:
			mainActivity.mSearchingDialog.cancel();
			break;
		case MsgConst.CLIENT_ACTION_VIEW_HANDLER_MSG:
			AppData.ServerCommand cmd = (AppData.ServerCommand)msg.obj;
			mainActivity.mAdapter.handlerMsg(cmd);
		case MsgConst.SERVICE_ACTION_CLOSE_HELP_GUIDE_VIEW:
			if(mainActivity.mLayoutHelp.getVisibility() == View.VISIBLE)
			{
				mainActivity.mLayoutHelp.setVisibility(View.GONE);
				if(GlobalData.isUserLoggedin())
					mainActivity.btn_goback.setVisibility(View.INVISIBLE);						
				else
					mainActivity.mBtn_Login.setImageResource(R.drawable.statusbar_login_button);
				HelpStatisticsUtil.helpType = null;
			}
			if(mainActivity.layout_guide_help.getVisibility() == View.VISIBLE)
				NotifyUiHandler.getInstantce().sendMessage(NotifyUiHandler.getInstantce().obtainMessage(MsgConst.CLIENT_ACTION_HIDE_HELP_GUIDE));
			break;
		case MsgConst.CLIENT_ACTION_LISTVIEW_GOTO_LAST_POSITION:
			mainActivity.mLstView.setSelection(mainActivity.mLstView.getCount() -1);
			break;
		default:
			if (DEBUG)
				Log.d(TAG, "other msg id from service: " + msg.what);
			newMsg.copyFrom(msg);
			NotifyUiHandler.getInstantce().sendMessage(newMsg);
			break;
		}
	}
}