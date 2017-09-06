package com.viash.voicelib.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import android.webkit.MimeTypeMap;

public class FileUtil {
	public static void saveJsonToFile(String fileName, JSONObject obj)
    {
    	FileOutputStream fOs;
		try {
			fOs = new FileOutputStream(new File(fileName));
			fOs.write(obj.toString().getBytes());
			fOs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public static String getMimeType(String name) {
		String mime = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(name);

		MimeTypeMap mMimeTypeMap = MimeTypeMap.getSingleton();
		mime = mMimeTypeMap.getMimeTypeFromExtension(extension);
		
		return mime;
	}

}
