package com.viash.voice_assistant.common;

import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.text.SimpleDateFormat;
import java.util.Date;
  
import android.annotation.SuppressLint;
import android.content.Context;  
import android.os.Environment;  
  
/** 
 * log日志统计保存 
 *  
 *  
 */  
  
public class LogcatToFile {  
  
    private static LogcatToFile INSTANCE = null;  
    private static String PATH_LOGCAT;  
    private LogDumperThread mLogThread = null;  
    private int mPId;  
  
    public void init(Context context) {  
        if (Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中  
            PATH_LOGCAT = Environment.getExternalStorageDirectory()  
                    .getAbsolutePath() + File.separator + "OlaLogcat";  
        } else {// 如果SD卡不存在，就保存到本应用的目录下  
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()  
                    + File.separator + "OlaLogcat";  
        }  
        File file = new File(PATH_LOGCAT);  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
    }  
  
    public static LogcatToFile getInstance(Context context) {  
        if (INSTANCE == null) {  
            INSTANCE = new LogcatToFile(context);  
        }  
        return INSTANCE;  
    }  
  
    private LogcatToFile(Context context) {  
        init(context);  
        mPId = android.os.Process.myPid();  
    }  
  
    public void start() {  
        if (mLogThread == null) {
        	mLogThread = new LogDumperThread(String.valueOf(mPId), PATH_LOGCAT);  
        }
        if (mLogThread.mRunning) {
        	return;
        }
        mLogThread.start();  
    }  
  
    public void stop() {  
        if (mLogThread != null) {  
            mLogThread.stopLogs();  
            mLogThread = null;  
        }  
    }  
  
    private class LogDumperThread extends Thread {  
  
        private Process logcatProc;  
        private BufferedReader mReader = null;  
        private boolean mRunning = false;  
        String cmds = null;  
        private String mPID;  
        private FileOutputStream out = null;  
  
        public LogDumperThread(String pid, String dir) {  
            mPID = pid;  
            try {  
                out = new FileOutputStream(new File(dir, "olalog" + MyDate.getFileName() +".log"));  
            } catch (FileNotFoundException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
  
            /** 
             *  
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s 
             *  
             * 显示当前mPID程序的 E和W等级的日志. 
             *  
             * */
  
             cmds = "logcat \"com.viash.voice_assistant:e\" | grep \"(" + mPID + ")\"";  
            // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息  
            // cmds = "logcat -s way";//打印标签过滤信息  
            //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";  
  
        }  
  
        public void stopLogs() {  
            mRunning = false;  
        }  
  
        @Override  
        public void run() {
        	mRunning = true;
            try {  
                logcatProc = Runtime.getRuntime().exec(cmds);  
                mReader = new BufferedReader(new InputStreamReader(  
                        logcatProc.getInputStream()), 1024);  
                String line = null;  
                while (mRunning && (line = mReader.readLine()) != null) {  
                    if (!mRunning) {  
                        break;  
                    }  
                    if (line.length() == 0) {  
                        continue;  
                    }  
                    if (out != null && line.contains(mPID)) {  
                        out.write((MyDate.getDateEN() + "  " + line + "\n")  
                                .getBytes());  
                    }  
                }  
  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                if (logcatProc != null) {  
                    logcatProc.destroy();  
                    logcatProc = null;  
                }  
                if (mReader != null) {  
                    try {  
                        mReader.close();  
                        mReader = null;  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                if (out != null) {  
                    try {  
                        out.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                    out = null;  
                }  
            }  
        }  
    } 
    
    
    @SuppressLint("SimpleDateFormat")
	public static class MyDate {  
        public static String getFileName() {  
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
            String date = format.format(new Date(System.currentTimeMillis()));  
            return date;// 2012年10月03日 23:41:31  
        }  
      
        public static String getDateEN() {  
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String date1 = format1.format(new Date(System.currentTimeMillis()));  
            return date1;// 2012-10-03 23:41:31  
        }
    }  
}  