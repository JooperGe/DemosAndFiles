package com.viash.voice_assistant.service.voicesdk;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.viash.voice_assistant.service.VoiceSdkService;
import com.viash.voicelib.msg.MsgConst;

/**
 * 
 * 拆分主 service
 * 
 * @author fenglei
 *
 */
public class CallClientHelper {

	private VoiceSdkService mainService;
	private static CallClientHelper _instance = null;
	
	private CallClientHelper(VoiceSdkService service){
		this.mainService = service;
	}
	public static CallClientHelper init(VoiceSdkService s){
		if(null == _instance)
			_instance = new CallClientHelper(s);
		return _instance;
	}
	
	public static CallClientHelper getInstance(){
		if(null == _instance)
			throw new RuntimeException("please init CallClientHelper");
		return _instance;
	}
	
	public void serverIsDisconnected(boolean showToast) {
		mainService.setWatiServerResponse(false);
		mainService.mServerState = MsgConst.STATE_SERVER_NOT_CONNECTED;

		sendBackToClient(MsgConst.SERVICE_ACTION_SERVER_DISCONNECTED);

	}

	public void sendBackToClient(Message msg) {
		if (mainService.mVoiceSdkUi != null && mainService.mVoiceSdkUi.isVoiceViewVisible()
				&& mainService.mVoiceSdkUi.isMsgSupport(msg)) {
			mainService.mVoiceSdkUi.sendMessage(msg);
		} else {
			for (Messenger messenger : mainService.cMessenger.values()) {

				if (messenger != null) {
					try {
						messenger.send(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void sendBackToClient(int aid) {
		Message msg = new Message();
		msg.what = aid;
		sendBackToClient(msg);
	}

	public void sendBackToClient(int aid, Bundle bundle) {
		Message msg = new Message();
		msg.what = aid;
		msg.setData(bundle);
		sendBackToClient(msg);
	}

	public void sendBackToClient(int aid, Object obj) {
		Message msg = new Message();
		msg.what = aid;
		msg.obj = obj;
		sendBackToClient(msg);
	}
	
	public void notifyClientState(int state) {
		if (mainService.mProcessState == 2 && state == 1) {
			Log.e("notifyClientState", "" + state);
		}

		Bundle bundle = new Bundle();
		bundle.putInt("state", state);
		sendBackToClient(MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE, bundle);
		mainService.mProcessState = state;
	}

	public void notifyClientConnection(int state) {
		Bundle bundle = new Bundle();
		bundle.putInt("connection", state);
		sendBackToClient(MsgConst.SERVICE_ACTION_SET_PROCESSING_STATE, bundle);
	}

}
