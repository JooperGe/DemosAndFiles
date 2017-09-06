package com.viash.voice_assistant.activity.assistant;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

import com.viash.voice_assistant.activity.NewAssistActivity;

/**
 * 
 * 主activity功能拆分
 * 
 * @author fenglei
 *
 */
public class MsgSender {
	
	private NewAssistActivity mainActivity;
	private static MsgSender _instance = null;

	private MsgSender(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static MsgSender init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new MsgSender(main);
		return _instance;
	}

	public static MsgSender getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init MsgSender");

		return _instance;
	}
	

	public boolean sendMessageToService(int what, Bundle data) {
		return sendMessageToService(what, 0, 0, data, null);
	}

	public boolean sendMessageToService(int what, Bundle data, Object obj) {
		return sendMessageToService(what, 0, 0, data, obj);
	}

	public boolean sendMessageToService(int what, int arg1, int arg2) {
		return sendMessageToService(what, arg1, arg2, null, null);
	}

	public boolean sendMessageToService(int what, int arg1, int arg2,
			Bundle data, Object obj) {
		Message msg = Message.obtain(null, what);
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		if (data != null)
			msg.setData(data);
		if (obj != null)
			msg.obj = obj;
		return sendMessageToService(msg);
	}

	public boolean sendMessageToService(Message msg) {
		boolean ret = false;
		try {
			if (mainActivity.mtServiceMessenger != null) {
				mainActivity.mtServiceMessenger.send(msg);
				ret = true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return ret;
	}

}
