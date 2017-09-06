package com.viash.voicelib.hardware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.viash.voicelib.msg.MsgConst;

/**
 * 手机蓝牙相关操作
 * 
 * @author Harlan
 * @createDate 2012-12-4
 */
public class HBluetooth {
	Context activity;
	/* 取得默认的蓝牙适配器 */
	static BluetoothAdapter mBluetoothAdapter;
	/* 用来存储搜索到的蓝牙设备 */
	Handler handler;
	private static List<BluetoothDevice> list;
	
	/**
	 * BroadcastReceiver 下面注释有例子
	 * 
	 * @param activity
	 * @param discoveryReceiver
	 *            完成搜索时触发
	 * @param foundReceiver
	 *            当找到一个设备时触发
	 */
	public HBluetooth(Context activity, Handler handler) {
		this.activity = activity;
		this.handler = handler;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		list = new ArrayList<BluetoothDevice>();
	}

	/**
	 * 打开蓝牙
	 */
	public static void openBluetooth() {
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
	}

	/**
	 * 关闭蓝牙
	 */
	public static void closeBluetooth() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.disable();
			}
		}
	}

	public void searchBluetooth() {
		if (mBluetoothAdapter != null) {
			list.clear();
			
			Message startmessage = handler.obtainMessage();
			startmessage.what = MsgConst.MSG_BLUETOOTH_FOUND_START;
			startmessage.sendToTarget();
			
			openBluetooth();
			while (!mBluetoothAdapter.isEnabled()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Message message = handler.obtainMessage();
					message.obj = list;
					message.what = MsgConst.MSG_BLUETOOTH_FOUND;
					message.sendToTarget();
				}
			}
			IntentFilter discoveryFilter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			activity.registerReceiver(discoveryReceiver, discoveryFilter);
			IntentFilter foundFilter = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
			activity.registerReceiver(foundReceiver, foundFilter);
			mBluetoothAdapter.startDiscovery();
		}
	}

	/**
	 * 蓝牙是否打开
	 * 
	 * @return
	 */
	public boolean isOpen() {
		if (mBluetoothAdapter != null) {
			return mBluetoothAdapter.isEnabled();
		} else {
			return false;
		}

	}

	/*
	*//**
	 * 接收器 当搜索蓝牙设备完成时调用
	 */
	private BroadcastReceiver foundReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// 从intent中取得搜索结果数据
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			list.add(device);
		}
	};

	public BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 卸载注册的接收器
			context.unregisterReceiver(foundReceiver);
			context.unregisterReceiver(this);
			Message message = handler.obtainMessage();
			message.obj = list;
			message.what = MsgConst.MSG_BLUETOOTH_FOUND;
			message.sendToTarget();
		}
	};

	/**
	 * 得到已经配对的蓝牙
	 * 
	 * @return
	 */
	public List<BluetoothDevice> getAlreadyLinkBluetooth() {
		openBluetooth();
		List<BluetoothDevice> listLocal = new ArrayList<BluetoothDevice>();
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		if (devices.size() > 0) {
			for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext();) {
				BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator
						.next();
				listLocal.add(bluetoothDevice);
				// 得到远程蓝牙的地址
				/*
				 * String bluetooothAddress=bluetoothDevice.getAddress(); String
				 * bluetooothName=bluetoothDevice.getName();
				 * System.out.println(bluetooothAddress);
				 * System.out.println(bluetooothName);
				 */
			}
		}
		return listLocal;
	}
	
	public JSONObject getDeviceJsonObject(BluetoothDevice device)
	{
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", device.getName());
			obj.put("paired", (device.getBondState()==BluetoothDevice.BOND_BONDED) ? "1" : "0");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
				
		return obj;
	}
	
	public List<BluetoothDevice> getFoundDevices()
	{
		return list;
	}

	/**
	 * 设置设备可见时间
	 * 
	 * @param m
	 *            秒
	 */
	public static void setDisplayTime(Context context, int m) {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, m);
		discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(discoverableIntent);
	}
	
	public static void setVisible(Context context, boolean visible)
	{
		if(visible)
			setDisplayTime(context, 120);
		else
			; // to do
	}

	/**
	 * 蓝牙发送配对请求
	 */
	public static void requestPair(BluetoothDevice bluetoothDevice) {
		if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				ClsUtils.setPin(bluetoothDevice.getClass(), bluetoothDevice, "0"); // 手机和蓝牙采集器配对
				ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
				//ClsUtils.cancelPairingUserInput(bluetoothDevice.getClass(),
						//bluetoothDevice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
				ClsUtils.setPin(bluetoothDevice.getClass(), bluetoothDevice, "0"); // 手机和蓝牙采集器配对
				ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
				//ClsUtils.cancelPairingUserInput(bluetoothDevice.getClass(),
						//bluetoothDevice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static boolean requestPair(String deviceName)
	{
		boolean ret = false;
		BluetoothDevice device = null;
		
		for(int i = 0; i < list.size(); i++)
		{
			BluetoothDevice deviceTemp = list.get(i);			
			if(deviceName.equals(deviceTemp.getName()))
			{
				if(deviceTemp.getBondState() == BluetoothDevice.BOND_NONE)
				{
					device = deviceTemp;
					break;
				}				
			}
		}
		if(device != null)
		{
			requestPair(device);
			ret = true;
		}
		
		return ret;
	}
	public void release(Context context)
	{
	
	}
}
