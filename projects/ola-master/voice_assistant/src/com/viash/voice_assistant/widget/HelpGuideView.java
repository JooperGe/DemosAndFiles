package com.viash.voice_assistant.widget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.mm.sdk.platformtools.Log;
import com.viash.voice_assistant.R;
import com.viash.voicelib.data.HelpData;
import com.viash.voicelib.data.HelpData.HelpGuideData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CallBackInterface;
import com.viash.voicelib.utils.HelpStatisticsUtil;
import com.viash.voicelib.utils.ImageLoaderUtil;

public class HelpGuideView extends LinearLayout implements CallBackInterface{
    private Context mContext;
    private ArrayList<HelpGuideData> mList;
    private ListView layout_content_list;
    private BaseAdapter mContentAdapter;
    private HelpGuideData mHelpGuideData;
    public static final String iconSDPath = Environment.getExternalStorageDirectory().toString()+"/voice_assist/help_icon/";
    private ArrayList<String> iconNameForSave = new ArrayList<String>();
    private Handler mHandler;
    private boolean operationEnable;
    private HelpData mHelpData;
	/*public HelpGuideView(Context context,JSONObject obj,Handler handler) 
	{
		super(context);
		mContext = context;
		mHandler = handler;
		try
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
		}
		
		initData(obj);
	}
	private void initData(JSONObject obj)
	{
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
				jsonArrayTemp = objTemp.getJSONArray("contentArray");
				int arraySize = jsonArrayTemp.length();
				hData.contentArray = new String[arraySize];
				for(int j=0; j<arraySize; j++)
				{
					hData.contentArray[j] = jsonArrayTemp.getString(j);
				}
				mList.add(hData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}*/
	
	public HelpGuideView(Context context, HelpData helpData,boolean operationEnable, Handler handler)
	{
		super(context);
		mContext = context;
		mHandler = handler;
		mHelpData = helpData;
		this.operationEnable = operationEnable;		
	}
    public View initView()
    {
    	mList = (ArrayList<HelpGuideData>)mHelpData.getHelpGuideData();
    	View view = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_help, null);
    	layout_content_list = (ListView) view.findViewById(R.id.layout_content_list);
    	layout_content_list.setVisibility(View.VISIBLE);
    	mContentAdapter = new ContentAdapter();    	   	
    	layout_content_list.setAdapter(mContentAdapter);
    	
    	setListViewHeight(layout_content_list);
    	
    	layout_content_list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showHelpGuideDetailView(position);
			}
    		
    	});
    	return view;
    }
    
    private class ContentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			return mList.size();
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			mHelpGuideData = mList.get(position);
			if(convertView == null)
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_help_item, null);
				holder.imgv_title = (ImageView) convertView.findViewById(R.id.imgv_title);
				holder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.imgv_go = (ImageView) convertView.findViewById(R.id.imgv_go);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
            if(mHelpGuideData.url.equals("local"))
            {
            	switch(position)
            	{
            	case 0:
            		holder.imgv_title.setImageResource(R.drawable.icn_weather);
            		break;
            	case 1:
            		holder.imgv_title.setImageResource(R.drawable.icn_music);
            		break;
            	case 2:
            		holder.imgv_title.setImageResource(R.drawable.icn_joke);
            		break;
            	case 3:
            		holder.imgv_title.setImageResource(R.drawable.icn_poi);
            		break;
            	case 4:
            		holder.imgv_title.setImageResource(R.drawable.icn_tv);
            		break;	
            	case 5:
            		holder.imgv_title.setImageResource(R.drawable.icn_more);
            		break;	
            	}            	
            }
            else
            {
				String fileName = iconSDPath+mHelpGuideData.icon_name;
				if(!fileName.equals(iconSDPath))
				{
					Bitmap bitmap = getLocalBitmap(fileName);				
					if(bitmap != null)
					   holder.imgv_title.setImageBitmap(bitmap);
					//holder.imgv_title.setImageDrawable(getResources().getDrawable(R.drawable.icn_weather));
					else
					{
						iconNameForSave.add(fileName);
						if((mHelpGuideData.url != null) &&(!mHelpGuideData.url.equals("")))
						{
							ImageLoaderUtil.downloadCount++;
							ImageLoaderUtil.loadImageAsync(holder.imgv_title, mHelpGuideData.url, null, getResources().getDrawable(R.drawable.icn_default),100,HelpGuideView.this);	
						}
					}
				}
            }
			holder.tv_description.setText(mHelpGuideData.description);
			holder.tv_title.setText(mHelpGuideData.title);
			if(android.os.Build.VERSION.SDK_INT > 13)
			   holder.tv_title.setAlpha((float) mHelpGuideData.opacity);
			if(mHelpGuideData.colorRGB.length() != 7)
				mHelpGuideData.colorRGB = "#FFFFFF";
			holder.tv_title.setTextColor(Color.parseColor(mHelpGuideData.colorRGB));			
			//holder.tv_subTitle.setText(mHelpGuideData.type);
			holder.imgv_go.setTag(position);
			holder.imgv_go.setClickable(true);
			holder.imgv_go.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int index = (Integer) v.getTag();
					showHelpGuideDetailView(index);
				}			
			});
			
			/*convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					ViewHolder holder = (ViewHolder)v.getTag();
					int index = (Integer) holder.imgv_go.getTag();
					showHelpGuideDetailView(index);
				}				
			});*/
			return convertView;
		}
		
		class ViewHolder{
			ImageView  imgv_title;
			TextView   tv_description;
			TextView   tv_title;
			ImageView  imgv_go;	
		}
    }
    
    private void showHelpGuideDetailView(int index)
    {
    	mHelpGuideData = mList.get(index);
		if(mHelpGuideData.title.equals("更多用途"))
		{
			mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_GOTO_HELP_VIEW));						
		}
		else
		{
		   mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_SHOW_HELP_GUIDE_DETAIL, mHelpGuideData));
		}
		if(HelpStatisticsUtil.jsonObj == null)
			HelpStatisticsUtil.initJsonObjectFromFile();
		HelpStatisticsUtil.putContentToJsonObject(mHelpGuideData.type, 1,false);
    }
    
    public static Bitmap getLocalBitmap(String url) {
    	FileInputStream fis = null;
    	Bitmap bitmap = null;
        try {
        	 if(url.equals(iconSDPath))
        		 return bitmap;
             fis = new FileInputStream(url);
             bitmap = BitmapFactory.decodeStream(fis);                        
            } catch (Exception e) {
             e.printStackTrace();
        }
        if(fis != null)
        {
        	try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	fis = null;
        }
        return bitmap;
   }
    
    protected void setListViewHeight(ListView listView){
    	View listItem = listView.getAdapter().getView(0, null, listView);		
		listItem.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int itemHeight = listItem.getMeasuredHeight();
		int height = 0; 
		int dividerHeight = listView.getDividerHeight();
	
	    height = listView.getAdapter().getCount() * (itemHeight + dividerHeight);
		
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = height;
		listView.setLayoutParams(params);
	}
	@Override
	public void callBack(Object obj, int param1, int param2) {
		String path = iconNameForSave.remove(0);
		if(path != null)
		{
			Log.e("HelpGuideView", path);
			ImageLoaderUtil.saveBitmap(path, (Bitmap)obj,"png");
			ImageLoaderUtil.downloadCount--;
			if(ImageLoaderUtil.downloadCount == 0)
				ImageLoaderUtil.setCallBackInterfaceFree();
		}	
	}    
}
