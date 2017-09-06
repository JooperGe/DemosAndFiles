package com.viash.voicelib.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class TtsUtil {

	

	public static String copyTtsData(Context context) {  
		String path = null;
		InputStream is = null;
		OutputStream os = null;
		File dataRoot = context.getFilesDir();
		path  = dataRoot.getAbsolutePath() + "/Resource.irf";
		File file = new File(path);
    	if(file.length() != 11629844)
    	{
    		try {
    			is = context.getAssets().open("Resource.mp3");  
				os = new FileOutputStream(path);
				
				byte[] buffer = new byte[1024 * 512];  
				int length;  
				while ((length = is.read(buffer)) > 0) {  
					os.write(buffer, 0, length);  
				}  

				os.flush();  
			} 
	        catch (FileNotFoundException e) {
				path = null;
				e.printStackTrace();
			} catch (IOException e) {
				path = null;
				e.printStackTrace();
			}  
	        finally
	        {
	        	if(os != null)
	        	{
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	if(is != null)
	        	{
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }   
    	}

        return path;
	}
}
