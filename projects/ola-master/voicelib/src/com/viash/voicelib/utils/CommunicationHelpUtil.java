package com.viash.voicelib.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.Handler;

import com.viash.voicelib.msg.MsgConst;

public class CommunicationHelpUtil {
	protected static final String SERVER_HEADER = "https://SERVER/olaweb/webservice/v1/wizardhelp";
	private  Context mContext;
	private  Handler mHandler;
	protected String mServer;	
	protected static final int CONNECT_TIMEOUT = 5000;
	protected static final int SO_TIMEOUT = 20000;
	public CommunicationHelpUtil(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
	}
	public void getHelpDataFromServer(Context context,Handler handler)
	{	
		mContext = context;
		mHandler = handler;
		
		new Thread(new Runnable(){

			@Override
			public void run() {
			   String jsonString = null;
			   try {				   				  				    
				   HttpClient mHttpClient = HttpsSSLSocketFactory.createMyHttpClient(CONNECT_TIMEOUT,SO_TIMEOUT);
				   String project_number = HelpStatisticsUtil.getProjectNumber(mContext);
				   if((project_number != null)&&(!project_number.equals("")))
					{
						if(HelpStatisticsUtil.isHelpDataFileExist())
						{
						   //param.setParameter("v", project_number);
						   //httpGet.setParams(param);
							mServer += "?v="+project_number;							
						}
					}
				   HttpGet httpGet = new HttpGet(mServer);
								
				   HttpResponse response = mHttpClient.execute(httpGet);					
					if(response.getStatusLine().getStatusCode()==200)
					{
						InputStream in = response.getEntity().getContent(); 
						jsonString = readString(in);
						if(jsonString != null)
						{
							if(jsonString.contains(HelpStatisticsUtil.PROJECT_NUMBER))
							{
							   HelpStatisticsUtil.saveHelpDataFile(jsonString);
							}
							/*else
							{
							   if(HelpStatisticsUtil.isHaveHelpData)
								   return;
							   jsonString = HelpStatisticsUtil.getHelpDataFromFile();
							   isHelpData = true;
							}*/
						}
						/*else
						{
							if(HelpStatisticsUtil.isHaveHelpData)
							   return;
							jsonString = HelpStatisticsUtil.getHelpDataFromFile();
							isHelpData = true;
						}
						if(isHelpData)
						{
						  mHandler.sendMessage(mHandler.obtainMessage(MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE, jsonString));
						  isHelpData = false;
						}*/
					}
					/*else
					{
						jsonString = HelpStatisticsUtil.getHelpDataFromFile();
						if(jsonString != null)
						{
						  if((jsonString.length() > 3)&& jsonString.contains(HelpStatisticsUtil.PROJECT_NUMBER))
						  {
						     mHandler.sendMessage(mHandler.obtainMessage(MsgConst.SERVICE_ACTION_SHOW_HELP_GUIDE, jsonString));
						     isHelpData = false;
						  }
						}
					}*/
			  } catch (Exception e) {
					e.printStackTrace();
			}
		  }		
		}).start();
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
