package com.viash.voice_assistant.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.data.SavedData;
import com.viash.voice_assistant.data.UserData;
import com.viash.voicelib.utils.ClientPropertyUtil;
import com.viash.voicelib.utils.NetWorkUtil;

@SuppressLint("SimpleDateFormat")
public class AppCrashHandler implements UncaughtExceptionHandler {
	//private static String TAG = "AppCrashHandler";
	private Context mcontext;
	private Thread.UncaughtExceptionHandler UEHandler;
	private static AppCrashHandler crashHandler = new AppCrashHandler();
	private Map<String, String> appInfo = new HashMap<String, String>();
	private SimpleDateFormat timeF = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static final String filePath = Environment.getExternalStorageDirectory().getPath() + "/voice_assist" + "/crach";
	private AppCrashHandler() {

	}

	public static AppCrashHandler getInstance() {
		return crashHandler;
	}

	public void initUcExceptionHandler(Context context) {
		mcontext = context;
		
		
		UEHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exceptions) {
		// TODO Auto-generated method stub
		if (!handleException(exceptions) && UEHandler != null) {
			UEHandler.uncaughtException(thread, exceptions);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	@SuppressLint("ShowToast")
	private boolean handleException(Throwable exception) {
		
		if (exception == null)
			return false;
		new Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(mcontext,mcontext.getString(R.string.crash_toast),Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}.start();

		obtainDeviceInfo(mcontext);
		saveCrashInfo2File(exception);
		return true;
	}

	private void obtainDeviceInfo(Context context) {
		int version = ClientPropertyUtil.getVersionCode(context);
		String mode = SavedData.isHttpMode() ? "HTTP" : "SOCKET";
		String time = timeF.format(new Date());
		String connectType = null;
		String types = null;

		if (NetWorkUtil.isNetConnected(context)) {
			connectType = "Network_Type";
			if (NetWorkUtil.isWIFIConnected(context)) {
				types = "Wi-Fi";
			} else {
				types = "2G/3G";
			}

		} else {
			connectType = "NO_NETWORK";
			types = "NULL";
		}

		appInfo.put("Device_Name", android.os.Build.MANUFACTURER);
		appInfo.put("Android_Version", android.os.Build.VERSION.RELEASE);
		appInfo.put("client_version", String.valueOf(version));
		appInfo.put("User_Name", UserData.getUserName(context));
		appInfo.put("Server_Mode", mode);
		appInfo.put(connectType, types);
		appInfo.put("crash_time", time);
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
		String imei = telephonyManager.getDeviceId();
		if(imei != null)
		   appInfo.put("imei", imei);
		// TODO Anymore information should be added here.

	}

	private String saveCrashInfo2File(Throwable exception) {

		StringBuffer buffer = new StringBuffer();
		Iterator<String> iter = appInfo.keySet().iterator();

		while (iter.hasNext()) {
			String key = iter.next();
			String value = appInfo.get(key);
			buffer.append(key + "=" + value + "\r\n");
		}

		Writer writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);
		exception.printStackTrace(pwriter);
		Throwable cause = exception.getCause();

		while (cause != null) {
			cause.printStackTrace(pwriter);
			cause = cause.getCause();
		}
		pwriter.close();
		String result = writer.toString();
		buffer.append(result);

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				String time = timeF.format(new Date());
				long timetamp = System.currentTimeMillis();
				String fileName = "ola-" + time + "-" + timetamp + ".log";
								
				String path = filePath;
				System.out.println(path);
				File file = new File(path);
				File file2 = new File(path + File.separator+fileName);
				if (!file.exists()) {
					file.mkdirs();
				}
				if (!file2.exists()) {
					file2.createNewFile();
				}

				FileOutputStream fos = new FileOutputStream(file2);
				fos.write(buffer.toString().getBytes());
				fos.close();
				return file2.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
