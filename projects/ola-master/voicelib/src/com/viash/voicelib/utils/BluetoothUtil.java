package com.viash.voicelib.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class BluetoothUtil {
	protected List<BluetoothDevice> mLstDevices = new ArrayList<BluetoothDevice>();
	Handler mHandler;
	int mMsgId;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mLstDevices.add(device);
			}
		}
	};

	public static void enableBluetooth(Context context, boolean enable) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (null != adapter) {
			if (!adapter.isEnabled()) {
				Intent intent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);

			}
		}
	}
	
	public static void setBluetoothVisible(Context context, boolean visible)
	{
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static boolean enableBluetoothSilent(Context context, boolean enable) {
		boolean ret = false;
		try {
			Object bluetoothManager = context.getSystemService("bluetooth");
			Method methodEnable;
			Method methodDisable;

			if (bluetoothManager != null) {
				Class<? extends Object> classManager = bluetoothManager.getClass();
				if (enable) {
					methodEnable = classManager.getMethod("enable",
							new Class[0]);
					if (methodEnable != null) {
						methodEnable.setAccessible(true);
						methodEnable.invoke(bluetoothManager, new Object[0]);
					}
				} else {
					methodDisable = classManager.getMethod("disable",
							new Class[0]);
					if (methodDisable != null) {
						methodDisable.setAccessible(true);
						methodDisable.invoke(bluetoothManager, new Object[0]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	public boolean searchDevices(Context context, Handler handler, int msgId) {
		boolean ret = false;
		mLstDevices.clear();
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		mMsgId = msgId;

		if (null != adapter) {
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			context.registerReceiver(mReceiver, filter);
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mHandler.obtainMessage(mMsgId, mLstDevices);
					mHandler = null;
				}
			}, 12000);
			adapter.startDiscovery();
			ret = true;
		}

		return ret;
	}
}
