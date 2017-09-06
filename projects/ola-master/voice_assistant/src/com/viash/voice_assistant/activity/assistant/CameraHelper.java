package com.viash.voice_assistant.activity.assistant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voicelib.hardware.HCamera;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 拆分 主activity 功能
 * 
 * 相机相关功能
 * 
 * @author fenglei
 *
 */
public class CameraHelper {
	
	private static final String TAG = "CameraHelper";
	private static final boolean DEBUG = true;
	private Camera mCamera;
	private NewAssistActivity mainActivity;
	
	private static CameraHelper _instance = null;
	private CameraHelper(NewAssistActivity main){
		this.mainActivity = main;
	}
	
	public static void init(NewAssistActivity main){
		if(null == _instance)
			_instance = new CameraHelper(main);
	}
	public static CameraHelper getInstantce(){
		if(null == _instance)
			throw new RuntimeException("please init CameraHelper");
		
		return _instance;
	}
	
	
	private PictureCallback pictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (DEBUG)
				Log.d(TAG, "onPictureTaken");
			if (data == null) {
				if (DEBUG)
					Log.d(TAG, "onPictureTaken data is null");
				CustomToast.makeToast(mainActivity, mainActivity.getResources().getString(R.string.newassistactivity_take_pic_fail));//,
						//Toast.LENGTH_LONG).show();
				NotifyUiHandler.getInstantce().sendEmptyMessageDelayed(
						MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE, 200);
				return;
			}
				
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File path = new File(Environment.getExternalStorageDirectory(),
						"/dcim/");
				if (!path.exists())
					path.mkdirs();
				String pictureFile = path.getAbsolutePath() + "/OLA"
						+ System.currentTimeMillis() + ".jpeg";
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					String toastStr = mainActivity.getResources().getString(R.string.newassistactivity_take_pic_succ) + pictureFile;
					if (DEBUG)
						Log.d(TAG, toastStr);
					CustomToast.makeToast(mainActivity, toastStr);
							//Toast.LENGTH_LONG).show();
					NotifyUiHandler.getInstantce().sendEmptyMessageDelayed(
							MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE, 200);
				} catch (FileNotFoundException e) {
					Log.e(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "Error accessing file: " + e.getMessage());
				}
			} else {
				if (DEBUG)
					Log.d(TAG, mainActivity.getResources().getString(R.string.newassistactivity_no_sd_card));
				CustomToast.makeToast(mainActivity, mainActivity.getResources().getString(R.string.newassistactivity_no_sd_card));
						//Toast.LENGTH_SHORT).show();
			}
		}
	};

	public void restartCamera() {
		if (mCamera == null) {
			if (DEBUG)
				Log.d(TAG, "restartCamera");
			mCamera = HCamera.getCameraInstance();
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.set("orientation", "portrait");
			parameters.set("rotation", 90);
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.set("jpeg-quality", 100);
			DisplayMetrics dm = new DisplayMetrics();
		    mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		    int screenWidth = dm.widthPixels;
		    int screenHeight = dm.heightPixels;
		    try{
				List<Size> lsize = parameters.getSupportedPictureSizes();
				Size tempSize = lsize.get(0);
				if(lsize.size() > 1)
				{
					for(int i=1;i<lsize.size();i++)
					{
						if(tempSize.width < lsize.get(i).width)//width is larger than height
							tempSize = lsize.get(i);
					}				
				}
				
				int height = (tempSize.width*screenWidth)/screenHeight;
				for(int i=0;i<lsize.size();i++)
				{
					if(lsize.get(i).width == tempSize.width)
					{
						if(Math.abs(lsize.get(i).height - height) < 50)
						{
							tempSize = lsize.get(i);
							break;
						}
					}
				}
				if((tempSize.width > 0) && (tempSize.height > 0))
				   parameters.setPictureSize(tempSize.width, tempSize.height);
		    }catch(Exception e)
		    {
		    	e.printStackTrace();
		    }
			mainActivity.mPreview.setCamera(mCamera);
			if (mCamera != null) {
				mCamera.setParameters(parameters);				
				mCamera.startPreview();
			}
		}
	}

	public void releaseCamera() {
		if (mCamera != null) {
			if (DEBUG)
				Log.d(TAG, "releaseCamera");
			mainActivity.mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	public void takePictureByCamera(int preview) {
		if (DEBUG)
			Log.v(TAG, "takePictureByCamera preview:" + preview);
		// if preview is 1, open built-in camera app, now ignore
		// if preview is 0 then
		if (mCamera == null) {
			restartCamera();
		}
		if (mCamera != null) {
			NotifyUiHandler.getInstantce().removeMessages(MsgConst.MSG_CAMERA_RESTORE_TO_BEFORE);
			NotifyUiHandler.getInstantce().sendEmptyMessageDelayed(MsgConst.MSG_CAMERA_OPERATION, 100);
		} else {
			if (DEBUG)
				Log.v(TAG, "mCamera null");
		}
	}

	public void cameraOperation() {
		if (mCamera != null) {
			// before take pictures turn off the camera volume
			AudioManager audioManager = (AudioManager)mainActivity
					.getSystemService(Context.AUDIO_SERVICE);
			mainActivity.systemVolumeBeforeTaken = audioManager
					.getStreamVolume(AudioManager.STREAM_SYSTEM);
			if (mainActivity.systemVolumeBeforeTaken != 0) {
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			if (DEBUG)
				Log.v(TAG, "cameraOperation");
			mCamera.takePicture(null, null, pictureCallback);
		}
	}

	public void cameraRestoreToBefore() {
		releaseCamera();
		// The corresponding method of camera sound recovery
		if (mainActivity.systemVolumeBeforeTaken != 0) {
			AudioManager audioManager = (AudioManager)mainActivity
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
					mainActivity.systemVolumeBeforeTaken,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
	}

}
