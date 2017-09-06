package com.viash.voice_assistant.observer;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.ContactUtil.ContactInfo;

public class ContactContentObserver extends ContentObserver {
	private Context mContext;
	// private Timer updateContactTimer;
	private Handler mHandler;
	private static boolean DEBUG = true;

	public ContactContentObserver(Handler handler, Context context) {
		super(handler);
		mHandler = handler;
		mContext = context;

	}

	@Override
	public void onChange(boolean selfChange) {
		Log.i("change", "ischange");
		UpdateContcat();
		// if (updateContactTimer == null) {
		// updateContactTimer = new Timer();
		// updateContactTimer.schedule(new UpdateContcatTask(mContext), 35000);
		// }
	}

	private void UpdateContcat() {
		List<ContactInfo> oldData = ContactUtil.getmLatestContactsInfo();
		List<ContactInfo> newData = ContactUtil.findAllContacts(mContext, true);

		// if (DEBUG) {
		// for (int i = 0; i < oldData.size(); i++) {
		// Log.i("List<ContactInfo>", "oldData.get(" + i + ")="
		// + oldData.get(i).toJsonObject().toString());
		// }
		// for (int i = 0; i < newData.size(); i++) {
		// Log.i("List<ContactInfo>", "newData.get(" + i + ")="
		// + newData.get(i).toJsonObject().toString());
		// }
		// }

		// Find modify and new data
		for (int i = 0; i < newData.size(); i++) {
			boolean hasData = false;
			for (int j = 0; j < oldData.size(); j++) {
				if (newData.get(i).getmId() == oldData.get(j).getmId()) {
					hasData = true;
					if (!Arrays.equals(newData.get(i).getMD5(), oldData.get(j)
							.getMD5())) {

						Message msg = mHandler
								.obtainMessage(MsgConst.MSG_CONTACT_ADDED);
						msg.arg1 = newData.get(i).getmId();
						mHandler.sendMessage(msg);
						Log.d("Tony", "modify: "
								+ newData.get(i).toJsonObject().toString());
					}

					oldData.remove(j);
					break;
				}
			}
			if (!hasData) {

				Message msg = mHandler
						.obtainMessage(MsgConst.MSG_CONTACT_ADDED);
				msg.arg1 = newData.get(i).getmId();
				mHandler.sendMessage(msg);
				Log.d("Tony", "new data: "
						+ newData.get(i).toJsonObject().toString());
			}
		}

		// Find delete data
		for (int i = 0; i < oldData.size(); i++) {

			Message msg = mHandler.obtainMessage(MsgConst.MSG_CONTACT_DELETED);
			msg.arg1 = oldData.get(i).getmId();
			mHandler.sendMessage(msg);

			Log.d("Tony", "delete data: "
					+ oldData.get(i).toJsonObject().toString());
		}

		// Refresh data
		// ContactUtil.setmLatestContactsInfo(newData);
		// ContactUtil.findAllContacts(mContext, false);

	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	// class UpdateContcatTask extends TimerTask {
	// private Context mContext;
	//
	// public UpdateContcatTask(Context context) {
	// mContext = context;
	// }
	//
	// @Override
	// public void run() {
	// List<ContactInfo> oldData = ContactUtil.getmLatestContactsInfo();
	// List<ContactInfo> newData = ContactUtil.findAllContacts(mContext,
	// false);
	//
	// // Find modify and new data
	// for (int i = 0; i < newData.size(); i++) {
	// boolean hasData = false;
	// for (int j = 0; j < oldData.size(); j++) {
	// if (newData.get(i).getmId() == oldData.get(j).getmId()) {
	// hasData = true;
	// if (!Arrays.equals(newData.get(i).getMD5(), oldData
	// .get(j).getMD5())) {
	// Log.d("Tony", "modify: "
	// + newData.get(i).toJsonObject().toString());
	// }
	//
	// oldData.remove(j);
	// break;
	// }
	// }
	// if (!hasData) {
	//
	// Log.d("Tony", "new data: "
	// + newData.get(i).toJsonObject().toString());
	// Message msg = mHandler
	// .obtainMessage(MsgConst.MSG_CONTACT_ADDED);
	// msg.arg1 = newData.get(i).getmId();
	// mHandler.sendMessage(msg);
	// }
	// }
	//
	// // Find delete data
	// for (int i = 0; i < oldData.size(); i++) {
	// Log.d("Tony", "delete data: "
	// + oldData.get(i).toJsonObject().toString());
	// }
	//
	// // Refresh data
	// ContactUtil.setmLatestContactsInfo(newData);
	// ContactUtil.findAllContacts(mContext, true);
	//
	// updateContactTimer.cancel();
	// updateContactTimer = null;
	// }
	// }
}
