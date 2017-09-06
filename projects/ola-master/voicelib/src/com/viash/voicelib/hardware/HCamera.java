package com.viash.voicelib.hardware;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

public class HCamera {
	private final static String TAG = "HCamera";
	
	public static Camera getCameraInstance() {
		Camera c = null;
		int camIdx = -1;
		try {
			if (Build.VERSION.SDK_INT <= 8) {
				c = Camera.open();
			}else{
				int cameraCount = Camera.getNumberOfCameras();
				Log.v(TAG, "cameraCount:" + cameraCount);
				if (cameraCount <= 0) {
					Log.e(TAG, "have no any camera");
					return null;
				}
				camIdx = getCameraIndex(Camera.CameraInfo.CAMERA_FACING_BACK);
				if (camIdx <= -1) {
					camIdx = getCameraIndex(Camera.CameraInfo.CAMERA_FACING_FRONT);
				}
				c = Camera.open(camIdx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			c = null;
		}
		return c;
	}
	
	private static int getCameraIndex(int cameraFacing) {
		int cameraCount = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int camIdx = -1;
		int index = 0;
		switch (cameraFacing) {
		case Camera.CameraInfo.CAMERA_FACING_FRONT:
			for (index = 0; index < cameraCount; index++) {
				Camera.getCameraInfo(index, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camIdx = index;
					Log.v(TAG, "front camIdx:" + camIdx);
					break;
				}
			}
			break;
		case Camera.CameraInfo.CAMERA_FACING_BACK:
			for (index = 0; index < cameraCount; index++) {
				Camera.getCameraInfo(index, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camIdx = index;
					Log.v(TAG, "back camIdx:" + camIdx);
					break;
				}
			}
			break;
		default:
			Log.e(TAG, "cameraFacing:" + cameraFacing + " is outofrange.");
			break;
		}

		return camIdx;
	}

}
