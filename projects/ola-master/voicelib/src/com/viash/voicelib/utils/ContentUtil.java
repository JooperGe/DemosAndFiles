package com.viash.voicelib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class ContentUtil {
	/**
	 * Format data
	 * 
	 * @param time
	 * @param format
	 *            HH:mm
	 * @return
	 */
	public static String formatTime(String time, String format) {
		Date date = new Date(new Long(time));
		SimpleDateFormat sdf = new SimpleDateFormat("",
				Locale.SIMPLIFIED_CHINESE);
		sdf.applyPattern(format);
		return sdf.format(date);
	}

	public static boolean isAlpha(char c) {
		return ((('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z')));
	}

	public static boolean isDigit(char c) {
		return (('0' <= c) && (c <= '9'));
	}

	// 登录时用此函数检查而不用正则表达式检查，以免已注册的会员无法登录
	public static boolean checkUserName(String passwordStr) {
		for (int nIndex = 0; nIndex < passwordStr.length(); nIndex++) {
			char cCheck = passwordStr.charAt(nIndex);
			if (nIndex == 0 && (cCheck == '-' || cCheck == '_')) {
				return false;
			}
			if (!(isDigit(cCheck) || isAlpha(cCheck) || cCheck == '-' || cCheck == '_')) {
				return false;
			}
		}
		return true;
	}
	
	// 用户名规则：6-15个字符，字母开头，可使用字母、数字、下划线
	public static boolean checkUserNameByRegex(String content) {
		boolean result = true;
		String pat = "^[a-zA-Z][a-zA-Z0-9_]{5,14}$";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(content);
		if (m.matches())
			result = true;
		else
			result = false;
		return result;
	}
	
	// 密码规则：6-15个字符
	public static boolean checkPasswordByRegex(String content) {
		boolean result = true;
		String pat = "^[a-zA-Z0-9_]{6,15}$";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(content);
		if (m.matches())
			result = true;
		else
			result = false;
		return result;
	}

	public static boolean checkEmail(String content) {
		boolean result = true;
		String pat = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(content);
		if (m.matches())
			result = true;
		else
			result = false;
		return result;
	}
	
	// 手机号规则: 11位数字,13或15或18打头的数串
	public static boolean checkMobileCN(String content) {
		boolean result = true;
		String pat = "^1[358][0-9]{9}$";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(content);
		if (m.matches())
			result = true;
		else
			result = false;
		return result;
	}
	
	
	/**
	 * dp转px
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	/**
	 * px转dp
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue/scale+0.5f);
	}

}
