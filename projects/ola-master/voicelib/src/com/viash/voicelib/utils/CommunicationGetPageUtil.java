package com.viash.voicelib.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;

public class CommunicationGetPageUtil {
	protected static final String SERVER_HEADER = "http://api.olavoice.com/OlaPushHtml/publish/getwelcomeres";
	public static final String PIC_PATH = Environment.getExternalStorageDirectory().toString()+"/voice_assist/welcomepage/";
	protected static final String PAGE_INFO = "page_info";
	protected static final String PAGE_VERSION = "page_version";
	protected static final String PAGE_START_TIME = "page_start_time";
	protected static final String PAGE_END_TIME = "page_end_time";
	protected static final String PAGE_PIC_NAME = "page_pic_name";
	private  Context mContext;
	private  Handler mHandler;
	protected String mServer;	
	protected static final int CONNECT_TIMEOUT = 5000;
	protected static final int SO_TIMEOUT = 20000;
	
	public CommunicationGetPageUtil(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	}
	
	public void getDataFromServer()
	{
		new Thread(new Runnable(){

			@Override
			public void run() {
				String jsonString = null;
				JSONObject jobj;
				boolean need_update = false;
				long start_time;
				long end_time;
				long version;
				String pic_name;
				try{
					
					HttpClient mHttpClient = HttpsSSLSocketFactory.createMyHttpClient(CONNECT_TIMEOUT,SO_TIMEOUT);									
					mServer = SERVER_HEADER + "?v="+getVersion(mContext);
					HttpGet httpGet = new HttpGet(mServer);					
					HttpResponse response = mHttpClient.execute(httpGet);
					if(response.getStatusLine().getStatusCode()==200)
					{
						InputStream in = response.getEntity().getContent(); 
						jsonString = readString(in);
						if(jsonString != null)
						{
							jobj = new JSONObject(jsonString);
							need_update = jobj.getBoolean("need_update");
							if(need_update)
							{
								String pic_url = jobj.getString("pic_url");
								pic_name = jobj.getString("pic_name");
								start_time = jobj.getLong("start_time");
								end_time = jobj.getLong("end_time");
								version = jobj.getLong("version");
								if((pic_url != null) &&(!pic_url.equals("")))
								{
								   Bitmap bmp = BitmapFactory.decodeStream(new URL(pic_url).openStream());
								   String type = "jpg";
								   if(pic_name.contains("jpg")||pic_name.contains("JPG")||pic_name.contains("jpeg")||pic_name.contains("JPEG"))
								   {
									   type = "jpg";
								   }else if(pic_name.contains("png")||pic_name.contains("PNG"))
								   {
									   type = "png";
								   }
								   if(bmp != null)
								   {
								     ImageLoaderUtil.saveBitmap(PIC_PATH+pic_name, bmp,type);
								     savePageInfo(start_time,end_time,pic_name,version);
								   }
								}
							}
						}
					}
					
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}).start();
	}
	
	private  long getVersion(Context context)
	{	
		long version = 0;
		try{
		    SharedPreferences pageInfo = context.getSharedPreferences(PAGE_INFO, 0);
		    version = pageInfo.getLong(PAGE_VERSION, version);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return version;		
	}
	
	private void savePageInfo(long start_time,long end_time,String pic_name,long version)
	{
		try{
		    SharedPreferences pageInfo = mContext.getSharedPreferences(PAGE_INFO, 0);
		    Editor edit = pageInfo.edit();
		    edit.putLong(PAGE_START_TIME, start_time);
		    edit.putLong(PAGE_END_TIME, end_time);
		    edit.putString(PAGE_PIC_NAME, pic_name);
		    edit.putLong(PAGE_VERSION, version);
		    edit.commit();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public  Bitmap isNeedShowWelcomePage(Context context)
	{
		Bitmap bitmap = null;
		long start_time = 0;
		long end_time = 0;
		String pic_name = null;
		try{
			SharedPreferences pageInfo = context.getSharedPreferences(PAGE_INFO, 0);
			start_time = pageInfo.getLong(PAGE_START_TIME, start_time);
			end_time = pageInfo.getLong(PAGE_END_TIME, end_time);
			pic_name = pageInfo.getString(PAGE_PIC_NAME, null);
			Date date_start = new Date(start_time);
			Date date_end = new Date(end_time);
			Date date = new Date();
			if(date.after(date_start) && date_end.after(date) && pic_name != null)
			{
				BitmapFactory.Options opt = new BitmapFactory.Options();
				bitmap = BitmapFactory.decodeFile(PIC_PATH + pic_name, opt);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return bitmap;	
	}
    public void setServer(String server, int port) {
		
		if (server.equals("api.olavoice.com") && port == 0) {
			mServer = SERVER_HEADER.replace("SERVER", "api.olavoice.com");
		}else if (server.equals("api.olavoice.com")){
			mServer = SERVER_HEADER.replace("SERVER", server + ":" + port);
		}else{
			mServer = SERVER_HEADER.replace("SERVER", server + ":" + port).replace("https", "http");;
		}
		
	}
   
    private  String readString(InputStream in) { 
       /*byte[]data = new byte[1024]; 
       int length = 0; 
       ByteArrayOutputStream bout = new ByteArrayOutputStream(); 
       while((length=in.read(data))!=-1){ 
           bout.write(data,0,length); 
       } 
       return new String(bout.toByteArray(),"UTF-8");*/
	   InputStreamReader isr;
	   StringBuffer out = new StringBuffer();
	   try {
			isr = new InputStreamReader(in, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
			String tempString = null;
			
			while ((tempString = br.readLine()) != null) {
				tempString += "\n";
				out.append(tempString);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		String result = out.toString();
		return result;
    }
}
