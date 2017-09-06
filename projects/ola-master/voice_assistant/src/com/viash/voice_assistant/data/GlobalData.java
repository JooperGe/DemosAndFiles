package com.viash.voice_assistant.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.viash.voice_assistant.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;


public class GlobalData {
	public static final int SOFTWARE_MODE_RELEASE = 0;
	public static final int SOFTWARE_MODE_DEBUG = 1;
	protected static int mSoftwareMode = SOFTWARE_MODE_RELEASE;
	
	protected static final String HEADER_FILE_NAME = "default_photo.png";
	protected static Bitmap userHeader;
	
	protected static final String WEATHER_DATABASE_NAME = "weatherdatabase.db";
	protected static File weatherDatabase = null;
	
	protected static Context mContext;
	private static boolean mIsNewAssistantActivityRun = false;
	private static String mServer_version = null;
	
	private static boolean mIsUserLoggedIn = false;
	
	
	public static String getmServer_version() {
		return mServer_version;
	}
	public static void setmServer_version(String mServer_version) {
		GlobalData.mServer_version = mServer_version;
	}
	
	// get weather database
	public static File getWeatherDatabase(){
		return weatherDatabase;
	}
	// get the header photo
	public static Bitmap getUserHeader()
	{
		return userHeader;
	}
	// set the header photo
	public static void setUserHeader(Bitmap bitmap){
		try {
			FileOutputStream fos = mContext.openFileOutput(HEADER_FILE_NAME, Context.MODE_PRIVATE);
			bitmap.compress(CompressFormat.PNG, 100, fos);
			userHeader = bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// modify the bitmap and save it to HEADER_FILE_NAME
	public static boolean createUserHeader(Bitmap bmpOriginal)
	{
		return false;
	}
	
	public static int getSoftwareMode()
	{
		return mSoftwareMode;
	}
	
	public static void init(Context context)
	{
		mContext = context;
		
		initSoftwareMode();
		initUserHeader();
		initWeatherDatabase();
	}
	
	private static void initWeatherDatabase() {
		weatherDatabase = mContext.getFileStreamPath(WEATHER_DATABASE_NAME);
		
		InputStream input = mContext.getResources().openRawResource(R.raw.weather);;
        OutputStream output = null;
        try {
			output = new FileOutputStream(weatherDatabase);
			byte[] buffer = new byte[2048];
			int length;
			while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
		} catch (FileNotFoundException e) {
			weatherDatabase = null;
			e.printStackTrace();
		} catch (IOException e) {
			weatherDatabase = null;
			e.printStackTrace();
		} finally {
			try {
                output.flush();
                output.close();
            } catch (IOException e) {
            }
			try {
                input.close();
            } catch (IOException e) {
            }
		}
	}
	private static void initUserHeader() {
		File file = mContext.getFileStreamPath(HEADER_FILE_NAME);
		if (file.exists()) {
			userHeader = BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			userHeader = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_user_default_head);
		}
		
	}
	protected static void initSoftwareMode()
	{
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aola_vip_0000";
		File file = new File(fileName);
		if(file.exists())
			mSoftwareMode = SOFTWARE_MODE_DEBUG;
			
	}
	
	public static void setNewAssistantAcitivityRunFlag(boolean value)
	{
		mIsNewAssistantActivityRun = value;		
	}
	
	public static boolean getNewAssistantAcitivityRunFlag()
	{
		return mIsNewAssistantActivityRun;
	}
	
	public static boolean isUserLoggedin() {
		return mIsUserLoggedIn;
	}
	
	public static void setUserLoggedin(boolean value) {
		if (mContext != null && ( UserData.getUserName(mContext) == null || UserData.getPwd(mContext) == null)) {
			mIsUserLoggedIn = false;
			return;
		}
		mIsUserLoggedIn = value;
	}
}
