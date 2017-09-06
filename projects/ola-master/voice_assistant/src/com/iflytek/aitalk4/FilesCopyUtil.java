package com.iflytek.aitalk4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;

public class FilesCopyUtil {
	
	private Context mContext = null;
	
	public FilesCopyUtil(Context context)
	{
		mContext = context;
	}
	public boolean ExistSDCard() 
	{  
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) 
		   return true;  
        else  
		   return false;  
	} 
	
	public int getVersion(String assetDir)
	{
		int version = 1;
		 try {
			 byte[] buffer = new byte[32];
			InputStream flInput = mContext.getResources().getAssets().open(assetDir+"/version.txt");
			int len = flInput.read(buffer);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(buffer,0,len);
			version = Integer.parseInt(byteArrayOutputStream.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public void CopyAssets(String assetDir, String dir) {   
        String[] files;   
        try {   
            files = mContext.getResources().getAssets().list(assetDir);   
        } catch (IOException e1) {   
            return;   
        }   
        File mWorkingPath = new File(dir);   
        // if this directory does not exists, make one.   
        if (!mWorkingPath.exists()) {   
            if (!mWorkingPath.mkdirs()) {   
  
            }   
        }   
        for (int i = 0; i < files.length; i++) {   
            try {   
                String fileName = files[i];   
                // we make sure file name not contains '.' to be a folder.   
                if (!fileName.contains(".")) {   
                    if (0 == assetDir.length()) {   
                        CopyAssets(fileName, dir + fileName + "/");   
                    } else {   
                        CopyAssets(assetDir + "/" + fileName, dir + fileName   
                                + "/");   
                    }   
                    continue;   
                }   
                File outFile = new File(mWorkingPath, fileName);   
                if (outFile.exists())   
                    outFile.delete();   
                InputStream in = null;   
                if (0 != assetDir.length()) {   
                    in = mContext.getAssets().open(assetDir + "/" + fileName);   
                } else {   
                    in = mContext.getAssets().open(fileName);   
                }   
                OutputStream out = new FileOutputStream(outFile);   
  
                // Transfer bytes from in to out   
                byte[] buf = new byte[1024*4];   
                int len;   
                while ((len = in.read(buf)) > 0) {   
                    out.write(buf, 0, len);   
                }   
                out.close();
                in.close();
            } catch (Exception e) {   
                e.printStackTrace();   
            }   
  
        }   
    }  

	public static List<File> getFileSort(String path) {
		 
        List<File> list = getFiles(path, new ArrayList<File>());
 
        if (list != null && list.size() > 0) {
 
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() > newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }
 
                }
            });
 
        }
 
        return list;
    }
	
	 public static List<File> getFiles(String realpath, List<File> files) {
		 
	        File realFile = new File(realpath);
	        if (realFile.isDirectory()) {
	            File[] subfiles = realFile.listFiles();
	            for (File file : subfiles) {
	                if (file.isDirectory()) {
	                    getFiles(file.getAbsolutePath(), files);
	                } else {
	                    files.add(file);
	                }
	            }
	        }
	        return files;
	}
	 
	public static boolean isHaveFileInDirectory(String path)
	{
		
		List<File> list = getFiles(path, new ArrayList<File>());
		if(list != null)
		{
			if(list.size() > 0)
				return true;
		}
		return false;
	}
}
