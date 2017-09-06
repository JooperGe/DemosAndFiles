package com.iflytek.asr.AsrService;

import java.util.ArrayList;

import android.media.AudioRecord;
import android.util.Log;

/**
 * ASR AudioRecord manager
 * @author zhangyun
 *
 */
public class AsrRecord {
	private static final String TAG = "AsrRecord";
	private static final int BUFF_SIZE = 64 * 320;           //Receive data buffer size
	private static final int FRAME_BUFF = 16 * 320;          //A frame buffer size
	private static final int SAMPLE_RATE = 16000;             //Sample rate
	private static final int READ_DELAY = 10;                //Read delay time
	private static final int BUFF_IGNORE = 4 * 320;          //Ignore audio data when begin record
	private static AudioRecord mRecord = null;
	private static boolean mCanAppendData = false;
	private static Thread mThreadRecord = null;
	private static ArrayList<byte[]> mList = null;
	private static int listSize = 0;
	
	/**
	 * 鍒涘缓褰曢煶瀵硅薄
	 * @author yjzhao
	 */
	public static int createRecord()
	{
		mCanAppendData = false;
		Log.d("AsrRecord", "createRecord()  mCanAppendData=" +mCanAppendData);	
		return 0;
	}
	
	/**寮�惎褰曢煶瀵硅薄
	 * @author yjzhao
	 * @return
	 */
	public static void initList(ArrayList<byte[]> list,int size)
	{
		mList = list;
		listSize = size;
		Log.d(TAG, "initList() listSize = " + listSize);
	}
	public static int  startRecord()
	{
		    mCanAppendData = true;
		    Log.d("AsrRecord", "startRecord()  mCanAppendData=" +mCanAppendData);
		    if (null != mThreadRecord)
			{
		    	try
			    {
		    		mThreadRecord.join();
			    }
			    catch (InterruptedException e)
			    {
			    	// TODO: handle exception
			    }
			}
		    Log.d(TAG, "before startRecording......." + System.currentTimeMillis());
						
			class ThreadRecord implements Runnable
			{			
				public void run() 
				{				
				    byte [] mBuff = new byte[4096];//FRAME_BUFF
				    int readSize = 4096;
				    
                    // modified by yjzhao @ 2010.8.12
					//mRecord.read(mBuff,0,BUFF_IGNORE );
					//Log.d(TAG," ignore audio data ...");
						
					//RecordFiler.ClearBuffer();
					//Log.d("WCDLog", "thread start" + System.currentTimeMillis());
					try{
					    
						int ret = 0;
						Log.d("AsrRecord", "startRecord() listSize = "+listSize);
					    if(listSize > 0) 
					    {
					    	Log.d("AsrRecord", "startRecord() 正在识别：listSize = "+listSize);
						    for(int i=0; i< listSize; i++)
						    {
	    						mBuff = mList.remove(0);
	    						Log.d("AsrRecord", "startRecord() i ="+i+" mCanAppendData = "+mCanAppendData);	        						
		        					//if(mCanAppendData)
		        						ret = Asr.appendData(mBuff,readSize);
		        					
		        					if (0 != ret){
		        					    	Log.e(TAG, "数据缓冲区满，ThreadRecord append data to ASR error! readSize = "+readSize+" ret = "+ret);
		        					    Asr.Destory();	
		        						break;
		        					}
		        					
		        					try {
			        					Thread.sleep(READ_DELAY);
			        				} catch (InterruptedException e) {
			        				    	Log.d(TAG,e.toString());		
			        					break;
			        				}
		        			  }
						    
	        				ret = 0;
	    					//离线识别文件末尾添加2秒数据0用于结束录音
	    					byte data1[] = new byte[4096];
	    					for(int i = 0; i< 12; i++)
							{
								ret = Asr.appendData(data1,4096);
								if (0 != ret)
								{
	    					    	Log.e(TAG, "数据缓冲区满，ThreadRecord append data to ASR error!");
	    					    	Asr.Destory();
	    						    break;
	    					    }
								try {
		        					Thread.sleep(READ_DELAY);
		        				} catch (InterruptedException e) {
		        				    	Log.d(TAG,e.toString());		
		        					break;
		        				}
							}
	    					mCanAppendData = false;
	    					mList = null;
	    					listSize = 0;
					    }
					}catch(Exception e)
					{ 
						e.printStackTrace();
					}
				}
			
			};
			mThreadRecord = (new Thread(new ThreadRecord()));
						
		return 0;
	}
	
	/**寮�褰曢煶锛岀敱璇嗗埆寮曟搸杩涜閫氱煡
	 * @author yjzhao
	 * @return
	 */
	public static int setCanAppendData() 
	{
	    startRecord();
	    Log.d("AsrRecord", "setCanAppendData()  mCanAppendData=" +mCanAppendData);	
    	mCanAppendData = true;
    	mThreadRecord.start();
    	return 0;
	}

	/**
	 * 鏆傚仠褰曢煶
	 * @author yjzhao
	 */
	public static void stopRecord() {
		mCanAppendData = false;
		Log.d("AsrRecord", "stopRecord()  mCanAppendData=" +mCanAppendData);
	}	
	
	/**閲婃斁褰曢煶瀵硅薄
	 * author yjzhao
	 * @return
	 */
	public static int releaseRecord() 
	{
	    mCanAppendData = false;
	    Log.d("AsrRecord", "releaseRecord()  mCanAppendData=" +mCanAppendData);
	    if (null != mThreadRecord)
		{
	    	try
		    {
	    	    mThreadRecord.join();
		    }
		    catch (InterruptedException e)
		    {
		    	// TODO: handle exception
		    }
		}
		if (null != mRecord && (mRecord.getState() == AudioRecord.STATE_INITIALIZED
			|| AudioRecord.RECORDSTATE_RECORDING == mRecord.getRecordingState()))
		{
		    Log.d(TAG,"stopRecord ");		
		    try {
			mRecord.stop();
			mRecord.release();
			mRecord = null;
		    }
		    catch (Exception e)
		    {
			Log.d(TAG,e.toString());
		    }
		}
		else
		{
		    Log.d(TAG,"stopRecord  error state ");
		}
	    	return 0;
	}
}


