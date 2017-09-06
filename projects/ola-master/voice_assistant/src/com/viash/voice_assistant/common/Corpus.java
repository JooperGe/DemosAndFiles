package com.viash.voice_assistant.common;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.util.EncodingUtils;

import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import com.viash.voicelib.msg.MsgConst;

public class Corpus {

	private String[] mCorpus;
	private int mCorpusIndex = 0;
	private Handler mHandler;
	
	public boolean mRunningCorpus = false;
	public boolean mSupport = true;	

	public Corpus(Handler messagehandle)
	{
		mHandler = messagehandle;
	}
	
	// 执行一条有效的corpus，并且将指针移到下一条corpus.
    public void runCorpus()
    {
    	String corpus = getCorpus();
    	if(mRunningCorpus && (corpus.length() > 0) )
    	{
			new Handler().postDelayed(new Runnable()
			{   
			    public void run()
			    {
					Message msg = mHandler.obtainMessage(MsgConst.MSG_DATA_FROM_TEXT);
					msg.obj = mCorpus[mCorpusIndex++];
					mHandler.sendMessage(msg);
			    } 
			}, 500);
			return;
    	}
    }
    
    // 返回当前的那条corpus.
    public String getCorpus()
    {
    	// 运行一条非空corpus
		if(mCorpus.length > 0)
		{
			while( mCorpusIndex < mCorpus.length ) 
			{
				if( mCorpus[mCorpusIndex].length() > 0 )
				{
					break;
				}
				else
				{
					mCorpusIndex++;
				}
			}
			if(mCorpusIndex < mCorpus.length)
			{
				return(mCorpus[mCorpusIndex]);
			}
		}    	
		mRunningCorpus = false;
		return("");
    }
    
    // 把corpus加载到内存中。
    public void loadCorpus(MenuItem item)
	{
		String menutext = item.getTitle().toString();
		if(menutext.toLowerCase().contains("corpus.txt.autotest"))
		{
			// 加载corpus文件
			String filename = "mnt/sdcard/Download/" + menutext.substring(2);
			try
			{
    			FileInputStream fin = new FileInputStream(filename);
    			byte[] buffer = new byte[fin.available()];
    			fin.read(buffer);
    			String content = EncodingUtils.getString(buffer, "UTF-8");
    			mCorpus = content.split( "\r\n" );
    			if( mCorpus != null )
    			{
        			for(int i = 0; i < mCorpus.length; i++)
        			{
        				mCorpus[i] = mCorpus[i].replace("//", "");
        				mCorpus[i] = mCorpus[i].trim();
        			}
        			
        			if(mCorpus.length > 0)
        			{
        				mCorpus[0] = mCorpus[0].substring(1);
        			}
    			}
    			fin.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			

			// 执行第一条非空corpus语句
			mRunningCorpus = true;
			mCorpusIndex = 0;
		}
	}
	
    // 根据mnt/sdcard/Download中的*corpus.txt.autotest, 在菜单中添加菜单。
    public void addCorpusMenu(Menu menu)
	{
		// 如果SD卡的/Download下面有*corpus.txt.autotest; 那么可以让用户选择测试*corpus.txt.autotest
    	boolean hasCorpus = false;
		File file = new File("mnt/sdcard/Download");  
		File[] files = file.listFiles();
		if(files != null)
		{
	    	for(int i = 0; i < files.length; i++)
	    	{
	    		String filename = files[i].getName();
	    		if(filename.toLowerCase().contains("corpus.txt.autotest"))
	    		{
	    			hasCorpus = true;
	            	menu.addSubMenu("测试"+files[i].getName());
	    		}
	    	}
		}
		
		if(hasCorpus)
		{
			menu.addSubMenu("停止corpus测试");
		}
	}
    
    public void stopRun()
    {
    	mRunningCorpus = false;
    }
    
    public boolean isCorpusMenu(String menutext)
    {
    	if( menutext == "停止corpus测试" )
    	{
    		return(true);
    	}
    	else if(menutext.toLowerCase().contains("corpus.txt.autotest"))
    	{
    		return(true);
    	}
    	else
    	{
    		return(false);
    	}
    }

    public boolean isStopMenu(String menutext)
    {
    	if( menutext == "停止corpus测试" )
    	{
    		return(true);
    	}
    	else
    	{
    		return(false);
    	}
    }
}
