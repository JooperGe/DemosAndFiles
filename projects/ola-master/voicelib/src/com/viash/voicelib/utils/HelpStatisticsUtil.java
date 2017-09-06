package com.viash.voicelib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viash.voicelib.msg.MsgConst;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class HelpStatisticsUtil {

	public static final String filePath = Environment.getExternalStorageDirectory().toString()+"/voice_assist/help_icon/statistics.cfg";
	public static JSONObject jsonObj;
	public static String project_number = "";
	public static String currentType = null;
	public static String helpType = null;
	public static int touchIndex = -1;
	public static final String HELP_FIRST = "HelpFirst";
	public static final String HELP_SECOND = "HelpSecond";
	public static final String HELP_ALL = "HelpAll";
	public static final String HELP_INFO = "HelpInfo";
	public static final String PROJECT_NUMBER = "project_number";
	public static final String HTML_PUSH = "htmlPush";
	public static final String FLOAT_VIEW_GUIDE= "floatview_guide";
	public static final String FLOAT_VIEW_ON_DESK = "FloatViewOnDesk";
	public static final String FLOAT_VIEW_COUNT = "count";
	public static final String FLOAT_VIEW_COUNT_FOR_CALL = "count_for_call";

	public static final String helpDataFilePath = Environment.getExternalStorageDirectory().toString()+"/voice_assist/helpData_ola.cfg";
	public static JSONObject initJsonObjectFromFile()
	{
		String str = readFileToString(filePath);
		try
		{
			if(str == null)
				jsonObj = null;
			else	
			    jsonObj = new JSONObject(str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(jsonObj == null)
		{
			jsonObj = new JSONObject();
			JSONObject obj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONArray jArray = new JSONArray();
			JSONArray jArrayHtmlPush = new JSONArray();
			JSONObject jArrayFloatView = new JSONObject();
			try {
				obj.put(HELP_INFO, jsonArray);
				obj.put("project_number", project_number);
				jsonObj.put(HELP_FIRST, obj);
				jsonObj.put(HELP_ALL, jArray);
				jsonObj.put(HTML_PUSH, jArrayHtmlPush);
				jsonObj.put(FLOAT_VIEW_ON_DESK, jArrayFloatView);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonObj;
	}
	
	public static boolean isNeedToSendStatistics()
	{
		boolean ret = false;
		ret = isNeedToSendHelpFirstStatistics();
		if(ret)
		  return ret;
		ret = isNeedToSendHelpAllStatistics();
		if(ret)
		  return ret;
		ret = isNeedToSendHtmlPushStatistics();
		if(ret)
		  return ret;
		ret = isNeedToSendFloatViewStatistics();
		return ret;
	}
	
	public static boolean isNeedToSendHelpAllStatistics()
	{
		boolean ret = false;
		JSONArray jArray = new JSONArray();
		if(jsonObj != null)
		{
			try {
				  jArray = jsonObj.getJSONArray(HELP_ALL);
				  if(jArray != null)
				  {
					  if(jArray.length() > 0)
					  {
						  ret = true;
					  }
				  }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static boolean isNeedToSendHelpFirstStatistics()
	{
		boolean ret = false;
		JSONArray jArray = new JSONArray();
		if(jsonObj != null)
		{
			try {
				  jArray = jsonObj.getJSONObject(HELP_FIRST).getJSONArray(HELP_INFO);
				  if(jArray != null)
				  {
					  if(jArray.length() > 0)
					  {
						  ret = true;
					  }
				  }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static void putContentToJsonObject(String name,int value,boolean isHelpAll)
	{
		if(jsonObj != null)
		{			
			try {
				int count = 0;
				JSONObject obj = new JSONObject();
				JSONArray jArray = new JSONArray();
				if(isHelpAll == false)
				{	
					jArray = jsonObj.getJSONObject(HELP_FIRST).getJSONArray(HELP_INFO);
					for(int i=0; i<jArray.length(); i++)
					{
						obj = jArray.getJSONObject(i);
						if(obj.getString("type").equals(name))
						{
							count = obj.getInt("count") + value;
							obj.put("type", name);
							obj.put("count", count);
							break;
						}
						
					}				
					if(count == 0)
					{
						JSONObject jobj = new JSONObject();
						jobj.put("type", name);
						jobj.put("count", value);
						jArray.put(jobj);
					}
				}
				else{
					jArray = jsonObj.getJSONArray(HELP_ALL);
					for(int i=0; i<jArray.length(); i++)
					{
						obj = jArray.getJSONObject(i);
						if(obj.getString("type").equals(name))
						{
							count = obj.getInt("count") + value;
							obj.put("type", name);
							obj.put("count", count);
							break;
						}
						
					}				
					if(count == 0)
					{
						JSONObject jobj = new JSONObject();
						jobj.put("type", name);
						jobj.put("count", value);
						jArray.put(jobj);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void putContentToJsonObject(long msgId)
	{
		JSONObject obj = new JSONObject();
		JSONArray jArray = new JSONArray();
		if(jsonObj != null)
		{
			try {
				long currentTime = System.currentTimeMillis();
				jArray = jsonObj.getJSONArray(HTML_PUSH);
				obj.put(MsgConst.SERVER_MSG_ID, msgId);
				obj.put("click_time", currentTime);
				jArray.put(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isNeedToSendHtmlPushStatistics()
	{
		boolean ret = false;
		JSONArray jArray = new JSONArray();
		if(jsonObj != null)
		{
			try {
				  jArray = jsonObj.getJSONArray(HTML_PUSH);
				  if(jArray != null)
				  {
					  if(jArray.length() > 0)
					  {
						  ret = true;
					  }
				  }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static void putContentToJsonObject(String type ,int value)
	{
		JSONObject jObj = new JSONObject();
		int count = 0;
		if(jsonObj != null)
		{
			try {
				if(jsonObj.has(FLOAT_VIEW_ON_DESK) == false)
				  jsonObj.put(FLOAT_VIEW_ON_DESK, jObj);
				else
				  jObj = jsonObj.getJSONObject(FLOAT_VIEW_ON_DESK);
				if(type.equals(FLOAT_VIEW_COUNT))
				{
					count = jObj.optInt("count", 0) + value;
					jObj.put("count", count);
				}else if(type.equals(FLOAT_VIEW_COUNT_FOR_CALL))
				{
					count = jObj.optInt("count_for_call", 0) + value;
					jObj.put("count_for_call", count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isNeedToSendFloatViewStatistics()
	{
		boolean ret = false;
		JSONObject jObj = new JSONObject();
		if(jsonObj != null)
		{
			try {
				jObj = jsonObj.getJSONObject(FLOAT_VIEW_ON_DESK);
				  if(jObj != null)
				  {
					  if((jObj.optInt("count", 0) > 0) ||(jObj.optInt("count_for_call", 0) > 0))
					  {
						  ret = true;
					  }
				  }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static void deleteStatistics()
	{
		File file = new File(filePath);
		if(file.exists())
			file.delete();
		initJsonObjectFromFile();
	}
	
	public static void saveJsonObjectToFile()
	{
		if(jsonObj == null)
			return;
		try {
			 JSONObject obj = jsonObj.getJSONObject(HELP_FIRST);
			 String pronum = (String) obj.get(PROJECT_NUMBER);
			 if(((pronum.equals(""))||(pronum == null))&&(project_number != null))
			 {
				 obj.put(PROJECT_NUMBER, project_number);
			 }			 
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		String str = jsonObj.toString();
		File file = new File(filePath);
		if(file.exists())
			file.delete();
		try 
		{
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFileToString(String path)
	{
		String result = null;
		FileInputStream is = null;
		try 
		{
           
			File file = new File(path);
			if(!file.exists())
				return null;
			is = new FileInputStream(file) ;

			long contentLength = file.length();

			byte[] ba = new byte[(int)contentLength];

			is.read(ba);

			result = new String(ba);

		  }
		  catch(Exception e)
		  {
				e.printStackTrace();
		  }
		  finally 
		  {
			   if(is!=null) {try{is.close();} catch(Exception e){} }
		  }
		  return result;
	}
 
	public static JSONObject getHelpSecondJsonObject()
	{
		JSONObject obj = new JSONObject();
		try{
			if(touchIndex != -1)
			{
				obj.put("touchIndex", touchIndex);
			}
			if(helpType != null)
			  obj.put("type", helpType);	
			else	
			  obj.put("type", currentType);
			obj.put("project_number", project_number);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return obj;	
	}
	
	public static String getProjectNumber(Context context)
	{	
		try{
		    if(isHelpDataFileExist() == false)
		    	project_number = null;
		    else{
		    	String str = getHelpDataFromFile();
		    	JSONObject obj = new JSONObject(str);
		    	project_number = obj.optString(PROJECT_NUMBER,null);
		    }
		    //project_number = settings.getString(PROJECT_NUMBER, null);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return project_number;		
	}

	/*public static void setProjectNumber(Context context,String value)
	{
		SharedPreferences settings = context.getSharedPreferences(PROJECT_NUMBER, 0);
		String str = settings.getString(PROJECT_NUMBER, null);
		if((str == null)||(!str.equals(value)))
		{
			Editor editor = settings.edit();
			editor.putString(PROJECT_NUMBER, value);
			editor.commit();
			project_number = value;
		}
	}*/

	public static void saveHelpDataFile(String str)
	{
		File file = new File(helpDataFilePath);
		if(file.exists())
			file.delete();
		try 
		{
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getHelpDataFromFile()
	{
		String result = null;
		FileInputStream is = null;
		try 
		{           
			File file = new File(helpDataFilePath);
			if(!file.exists())
				return null;
			is = new FileInputStream(file) ;

			long contentLength = file.length();

			byte[] ba = new byte[(int)contentLength];

			is.read(ba);

			result = new String(ba);

		  }
		  catch(Exception e)
		  {
				e.printStackTrace();
		  }
		  finally 
		  {
			   if(is!=null) {try{is.close();} catch(Exception e){} }
		  }
		  return result;
	}
	
	public static boolean isHelpDataFileExist()
	{
		File file = new File(helpDataFilePath);
		if(file.exists())
			return true;
		else
			return false;
	}
	
	public static boolean isNeedShowFloatGuideView(Context context)
	{	
		int is_need_show = 0;
		try{
		    SharedPreferences settings = context.getSharedPreferences(PROJECT_NUMBER, 0);
		    is_need_show = settings.getInt(FLOAT_VIEW_GUIDE, 0);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(is_need_show == 0)
			return true;
		else
		    return false;		
	}
	
	public static void setFloatViewGuide(Context context,int value)
	{
		SharedPreferences settings = context.getSharedPreferences(PROJECT_NUMBER, 0);
		int is_need_show = settings.getInt(FLOAT_VIEW_GUIDE, 0);
		if(is_need_show == value)
			return;
		Editor editor = settings.edit();
		editor.putInt(FLOAT_VIEW_GUIDE, value);
		editor.commit();
	}
	
	/*public static boolean isNeedToRequestServer(Context context)
	{
		boolean ret = false;
		getProjectNumber(context);
		Long currentTime = System.currentTimeMillis();
		if(project_number != null)
		{
			try{
				Long lastTime = Long.parseLong(project_number);
				int day = (int) ((currentTime - lastTime)/(1000*60*60*24));
				if(day > 1)
					ret = true;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}*/
}
