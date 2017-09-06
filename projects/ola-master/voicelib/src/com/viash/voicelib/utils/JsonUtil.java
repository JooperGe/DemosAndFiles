package com.viash.voicelib.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {
	public static int optInt(JSONObject obj, String name, int fallback) {
		int ret = fallback;
		JSONArray array = obj.optJSONArray(name);
		if(array == null)
		{
			ret = obj.optInt(name, fallback);
		}
		else
		{
			if(array.length() > 0)
				ret = array.optInt(0, fallback);
		}
		return ret;
	}

	public static long optLong(JSONObject obj, String name, long fallback) {
		long ret = fallback;
		JSONArray array = obj.optJSONArray(name);
		if(array == null)
		{
			ret = obj.optLong(name, fallback);
		}
		else
		{
			if(array.length() > 0)
				ret = array.optLong(0, fallback);
		}
		return ret;
	}
	
	public static JSONObject optJsonObj(JSONObject obj, String name) {
		JSONObject ret = null;
		JSONArray array = obj.optJSONArray(name);
		if(array == null)
		{
			ret = obj.optJSONObject(name);
		}
		else
		{
			if(array.length() > 0)
				ret = array.optJSONObject(0);
		}
		return ret;
	}

	public static String optString(JSONObject obj, String name, String fallback) {
		String ret = fallback;
		JSONArray array = obj.optJSONArray(name);
		if(array == null)
		{
			ret = obj.optString(name, fallback);
		}
		else
		{
			if(array.length() > 0)
				ret = array.optString(0, fallback);
		}
		return ret;
	}
	
	
	public static String[] optStringArray(JSONObject obj, String key)
	{
		String[] ret = null;
		
		JSONArray array = obj.optJSONArray(key);
		if(array != null && array.length() > 0)
		{
			ret = new String[array.length()];
			for(int i = 0; i < array.length(); i++)
			{
				ret[i] = array.optString(i);
			}
		}
		return ret;
	}
	
	public static long[] optLongArray(JSONObject obj, String key)
	{
		long[] ret = null;
		
		JSONArray array = obj.optJSONArray(key);
		if(array != null && array.length() > 0)
		{
			ret = new long[array.length()];
			for(int i = 0; i < array.length(); i++)
			{
				ret[i] = array.optLong(i, 0);
			}
		}
		return ret;
	}
}
