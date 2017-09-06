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

import com.viash.voicelib.msg.MsgConst;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class CommunicationUpdateUtil {
	protected static final String SERVER= "http://www.ola.com.cn/update/ola/update.txt";
	//public static final String PIC_PATH = Environment.getExternalStorageDirectory().toString()+"/voice_assist/welcomepage/";
	//protected static final String PAGE_INFO = "page_info";
	//protected static final String PAGE_VERSION = "page_version";
	//protected static final String PAGE_START_TIME = "page_start_time";
	//protected static final String PAGE_END_TIME = "page_end_time";
	//protected static final String PAGE_PIC_NAME = "page_pic_name";
	private  Context mContext;
	private  static Handler mHandler;
	protected String mServer;	
	protected static final int CONNECT_TIMEOUT = 5000;
	protected static final int SO_TIMEOUT = 20000;
	
	private int build_version = 0;
	private String version;
	private String file_name;
	private String update_url;
	private String description;
	private long file_size;
	private boolean is_from_setting_menu = false;
	
	public CommunicationUpdateUtil(Context context)
	{
		mContext = context;
	}
	
	public static void setHandler(Handler handler)
	{
		mHandler = handler;
	}
	
	public void setIsFromSettingMenu(boolean value)
	{
		is_from_setting_menu = value;
	}
	
	public void getDataFromServer()
	{
		new Thread(new Runnable(){

			@Override
			public void run() {
				String jsonString = null;
				JSONObject jobj;				
						
				try{
					
					HttpClient mHttpClient = HttpsSSLSocketFactory.createMyHttpClient(CONNECT_TIMEOUT,SO_TIMEOUT);														
					HttpGet httpGet = new HttpGet(SERVER);					
					HttpResponse response = mHttpClient.execute(httpGet);
					if(response.getStatusLine().getStatusCode()==200)
					{
						InputStream in = response.getEntity().getContent(); 
						jsonString = readString(in);
						if(jsonString != null)
						{
							jobj = new JSONObject(jsonString);
							if(jobj != null)
							{
								build_version = jobj.getInt("build_version");
								version = jobj.getString("version");
								file_name = jobj.getString("file_name");
								update_url = jobj.getString("update_url");
								description = jobj.getString("description");
								file_size = jobj.getLong("file_size");
								if(ClientPropertyUtil.getVersionCode(mContext) < build_version)
								{
									Message msg =  Message.obtain(null, MsgConst.CLIENT_ACTION_DISPLAY_UPDATE_DIALOG);
									Bundle bundle = new Bundle();
									bundle.putInt("build_version", build_version);
									bundle.putString("version", version);
									bundle.putString("description", description);
									bundle.putString("update_url", update_url);
									bundle.putLong("file_size", file_size);
									bundle.putString("file_name", file_name);
									msg.setData(bundle);
									mHandler.sendMessage(msg);
									mHttpClient.getConnectionManager().shutdown();
								}
								else
								{
									if((mHandler != null) && is_from_setting_menu)
										mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_DISPLAY_VERSION_UPDATE, "没有新版本更新！"));							
								}
							}
							
						}
					}
					else
					{
						if((mHandler != null) && is_from_setting_menu)
							mHandler.sendMessage(mHandler.obtainMessage(MsgConst.CLIENT_ACTION_DISPLAY_VERSION_UPDATE, "网络状态异常!"));
					}
					
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				is_from_setting_menu = false;
			}
			
		}).start();
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
