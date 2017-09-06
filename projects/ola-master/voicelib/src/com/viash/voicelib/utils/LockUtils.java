package com.viash.voicelib.utils;

public class LockUtils {
	 private static long lastClickTime;   
     public static boolean isFastDoubleClick() {   
         long time = System.currentTimeMillis();   
         long timeD = time - lastClickTime;   
         if ( 0 < timeD && timeD < 500) {       //500毫秒内按钮无效 
         }      
         lastClickTime = time;      
         return false;      
     }   
}
