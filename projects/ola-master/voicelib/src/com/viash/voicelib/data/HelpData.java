package com.viash.voicelib.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viash.voicelib.utils.HelpStatisticsUtil;


import android.content.Context;

public class HelpData extends BaseData{
	
	private ArrayList<HelpGuideData> mList;
	
	public HelpData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	@Override
	public boolean parseFromJson(Context context, JSONObject obj)
	{	
		/*try
		{
			String str = "{"+
					    "\"help_menu\":"+
						"["+
						   "{\"type\" : \"weather\",\"title\":\"天气\",\"description\" : \"今天天气怎么样\",\"icon_name\" : \"weather.png\",\"url\" : \"http://Images4.c-ctrip.com/target/hotel/125000/124634/0a388aa7e3e54721addaf2597cc50455_100_75.jpg\",\"contentArray\" : [\"今天天气怎么样\",\"今天天气怎么样\",\"上海天气怎么样\"],\"opacity\" : \"1\",\"color\" : \"#ffffff\"},"+

						   "{\"type\" : \"music\",\"title\":\"音乐\",\"description\" : \"我要听霍遵的歌\",\"icon_name\" : \"music.png\",\"url\" : \"http://Images4.c-ctrip.com/target/hotel/125000/124634/0a388aa7e3e54721addaf2597cc50455_100_75.jpg\",\"contentArray\" : [\"我要听歌\",\"播放刘德华的专辑\",\"随便放一首歌\"],\"opacity\" : \"0.5\",\"color\" : \"#00cbfa\"},"+
						   
                           "{\"type\" : \"music\",\"title\":\"音乐\",\"description\" : \"我要听霍遵的歌\",\"icon_name\" : \"music.png\",\"url\" : \"http://Images4.c-ctrip.com/target/hotel/125000/124634/0a388aa7e3e54721addaf2597cc50455_100_75.jpg\",\"contentArray\" : [\"我要听歌\",\"播放刘德华的专辑\",\"随便放一首歌\"],\"opacity\" : \"0.5\",\"color\" : \"#bcbcbc\"}"+
						 "],"+
						"\"project_number\" : \"1\""+
						"}";
			obj = new JSONObject(str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}*/
		
		try {
			mList = new ArrayList<HelpGuideData>();
			JSONArray jsonArray = obj.getJSONArray("help_menu");			
			JSONObject objTemp;
			JSONArray jsonArrayTemp;
			for(int i=0,count = jsonArray.length(); i<count; i++)
			{
				HelpGuideData  hData = new HelpGuideData();				
				objTemp = jsonArray.getJSONObject(i);
				
				hData.type =  objTemp.getString("type");
				hData.title =  objTemp.getString("title");
				hData.description =  objTemp.getString("description");
				hData.icon_name =  objTemp.getString("icon_name");
				hData.url =  objTemp.getString("url");
				hData.opacity = objTemp.optDouble("opacity");
				hData.colorRGB = objTemp.getString("color");
				if(objTemp.has("contentArray"))
				{
					jsonArrayTemp = objTemp.getJSONArray("contentArray");
					if(jsonArrayTemp != null)
					{
						int arraySize = jsonArrayTemp.length();
						hData.contentArray = new String[arraySize];
						for(int j=0; j<arraySize; j++)
						{
							hData.contentArray[j] = jsonArrayTemp.getString(j);
						}
					}
				}
				mList.add(hData);
			}
			/*String str = obj.getString(HelpStatisticsUtil.PROJECT_NUMBER);
			HelpStatisticsUtil.setProjectNumber(context,str);*/
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mParseResult = true;
		return mParseResult;
		
	}
	
	public ArrayList<HelpGuideData> getHelpGuideData()
	{
		return mList;
	}
	public static class HelpGuideData
    {
    	public String type;
    	public String title;
    	public String description;
    	public String icon_name;
    	public String url;
    	public String[] contentArray;
    	public double opacity;
    	public String colorRGB;
    }
}
