package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MemoUtil {
	
	public static class MemoData
	{
		String Content = null;
		public String getContent() {
			return Content;
		}
		public void setContent(String Content) {
			this.Content = Content;
			}
		
		public JSONObject toJSonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("content", "" + Content);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;			
		}
	}
	
	public static List<MemoData> queryMemo(Context context)
	{
		List<MemoData> lstMemo = new ArrayList<MemoUtil.MemoData>();
		SharedPreferences settings = context.getSharedPreferences("MemoIndex" , Context.MODE_PRIVATE);
        Map<String, ?> items = settings.getAll(); 
        Iterator<?> it = items.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemoData tempData = new MemoData();
            tempData.setContent(pairs.getValue().toString());            
            lstMemo.add(tempData);
        }		
		return lstMemo;
	}
	
	
	public static boolean addMemo(Context context, String memo)
	{
		boolean ret = false;	
		try {				                   
            SharedPreferences sp = context.getSharedPreferences("MemoIndex" , Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putString(memo, memo);
            editor.commit();          
            ret = true; 
		} catch (Exception e) {e.printStackTrace();}	
		return ret;
	}

	
	public static boolean deleteMemo(Context context, String memoDelete)
	{
		boolean ret = false;
		try {
            SharedPreferences sp = context.getSharedPreferences("MemoIndex" , Context.MODE_PRIVATE);
            Editor editor = sp.edit();		
			if( sp.contains(memoDelete) ) {
	            editor.remove(memoDelete);
	            editor.commit();				
				ret = true;
			} 
			else {
			}		            
		} catch (Exception e) {e.printStackTrace();}		
		return ret;
	}
}
