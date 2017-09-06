package com.viash.voicelib.utils;

import android.util.Log;

/**
 * LOG打印的公共类
 * @author Harlan
 * @createDate 2012-12-14
 */
public class LogOutput {
	

	public class Debug {
	    /**
	     * Indicated whether logging functionality is enabled.
	     */
	    public final static boolean ON = true;
	    
	    /**
	     * Protectes Dubug class of instantiation
	     */
	    private Debug() {
	    } // constructor
	}
	
	private static final String TAG = "SPORTQ";
	private static final String FILE_NAME = "data/log.txt";
	private static final int MaxBufferSize = 8 * 1024;

	// Debug Info
	public static void d(String sMessage) {
		if (Debug.ON) {
			d(TAG, sMessage);
		}
	}

	public static void d(String sTag, String sMessage) {
		if (Debug.ON) {
			if (null != sMessage) {
				Log.d(sTag, sMessage);
			}
		}
	}

	// Warning Info
	public static void w(String sTag, String sMessage) {
		if (Debug.ON) {
			if (null != sMessage) {
				Log.w(sTag, sMessage);
			}
		}
	}

	// Error Info
	public static void e(String sMessage) {
		if (Debug.ON) {
			if (null != sMessage) {
				e(TAG, sMessage);
			}
		}
	}

	public static void e(String sTag, String sMessage) {
		if (Debug.ON) {
			if (null != sMessage) {
				Log.e(sTag, sMessage);
			}
		}
	}
	
	public static void i(String sTag, String sMessage) {
		if (Debug.ON) {
			if (null != sMessage) {
				Log.i(sTag, sMessage);
			}
		}
	}

	/**
	 * 可以将网络获取的内容写入文件
	 * @param traceInfo
	 */
	public static void toFile(byte[] traceInfo) {
		/*if (Debug.ON && PhoneUtil.sdcard()) {
			File file = new File(FILE_NAME);
			try {
				file.createNewFile();
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						new FileOutputStream(file, true), MaxBufferSize);
				bufferedOutputStream.write(traceInfo);
				traceInfo = null;
				bufferedOutputStream.close();
			} catch (IOException e) {
				LogOutput.d(e.getMessage());
			}
		}*/
	}

}
