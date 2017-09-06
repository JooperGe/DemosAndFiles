package com.iflytek.aitalk4;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iflytek.asr.RecognitionResult;
import com.iflytek.asr.Recognizer.IRecognitionListener;
import com.iflytek.asr.AsrService.Asr;
import com.iflytek.asr.AsrService.AsrInstance;
import com.viash.voicelib.utils.CustomToast;

public class Aitalk4 extends Thread implements IRecognitionListener {
    private static final String TAG = "Asr_Aitalk4";
    
	
	/**
	 * 定义识别消息类型
	 */
	private static final int MSG_BEGIN_OF_SPEECH = 1;

	private static final int MSG_BUFFER_RECEIVERD = 2;

	private static final int MSG_END_OF_SPEECH = 3;

	private static final int MSG_ERROR = 4;

	private static final int MSG_RESULTS = 5;

	private static final int MSG_BEGINNING_OF_RECORD = 6;

	private static final int MSG_END_OF_RECORD = 7;
	
	private int mLastErrorId = 0;
	private List<RecognitionResult> mLastResults = null;  //识别结果

	private boolean mBindOk = false;
	private Context mContext;
	public Handler mMsgReciver;
    private boolean state_init = false;
	InputStream voicePcm;
	
	@Override
	public void run() 
	{
		try{
	    Looper.prepare();
	  /**
		 * 处理各种消息的Handler类	 */
		  mMsgReciver = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case MSG_BEGIN_OF_SPEECH:
					break;
				case MSG_BUFFER_RECEIVERD:
					break;
				case MSG_END_OF_SPEECH:				 		
					break;
				case MSG_ERROR:		 
					onErrorMsg();
					break;
				case MSG_RESULTS: 
					onResultsMsg();
					break;
				case MSG_BEGINNING_OF_RECORD:	
					onBeginRecord();			
					break;
				case MSG_END_OF_RECORD:
					onEndRecord();
					break;
				}			
			}
		};
	   Looper.loop();
	 }catch(Exception e)
	 {
		e.printStackTrace(); 
	 }
	}
	public Aitalk4(Context context)
	{
		mContext = context;
		this.start();
		if(init() == false)
		  state_init = false;
		else
		  state_init = true;
	}
	public void Create()
	{		
		if(state_init)
		{
		  AsrInstance.getInstance().setScene("olamenu");	
		  AsrInstance.getInstance().startListening(this);
		  //AsrRecord.setCanAppendData();
		}
		else
		{
			int count = 0;
			Log.d(TAG,"初始化失败 ");
			state_init = init();
			while(state_init == false)
			{
				try{
					Thread.sleep(20);
					Log.d(TAG,"初始化失败  count = "+count);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				count++;
				if(count > 3)
				   break;
				state_init = init();
			}
			if(state_init == false)
			{	
			  AiTalkShareData.setIdentificationFlag(2);//识别结果出错
			  Log.d(TAG,"初始化最终还是失败  count = "+count);
			}
			else
			{
			  Log.d(TAG,"初始化成功了 count = "+count);
			  AsrInstance.getInstance().setScene("olamenu");
			  AsrInstance.getInstance().startListening(this);
			  //AsrRecord.setCanAppendData();
			}
		}	
		//init();
		/*super.onCreate();
        Asr.init(); //创建ESR
        onAsrInit(); //初始语法
                     
        try {
        	byte[] buffer = null;
            int bufferLen = 0;
			InputStream voicePcm = this.mContext.getAssets().open("voice.pcm");
			bufferLen = voicePcm.available();
			buffer = new byte[bufferLen];
			voicePcm.read(buffer);
			voicePcm.close();
//			String soltName = "<songs>";
//			String itemName = "119";
			int ret = Asr.makeVoiceTag("<songs>","119",buffer,bufferLen);
			if (0 != ret){
				Log.d(TAG,"makeVoiceTag error return=" + ret);
			}
			buffer = null;
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
 
        Asr.setParam(7,1);
        onButtonRetryClick();*/
	}

	public boolean  init() 
	{
		boolean ret = true;
        if(Asr.init() == false)//创建ESR
        {
        	Log.d(TAG,"Asr.init() 返回错误");
        	ret = false;
        	return ret;
        }
        onAsrInit(); //初始语法
                     
        /*try {
        	byte[] buffer = null;
            int bufferLen = 0;
			InputStream voicePcm = this.mContext.getAssets().open("voice.pcm");
			bufferLen = voicePcm.available();
			buffer = new byte[bufferLen];
			voicePcm.read(buffer);
			voicePcm.close();
//			String soltName = "<songs>";
//			String itemName = "119";
			int ret = Asr.makeVoiceTag("<songs>","119",buffer,bufferLen);
			if (0 != ret){
				Log.d(TAG,"makeVoiceTag error return=" + ret);
			}
			buffer = null;
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}*/
 
        AsrInstance.getInstance().setScene("olamenu");		     		      
		Asr.setParam(7, 1);
		Asr.setParam(1, 60);//识别结果得分低于80则不返回
        return ret;
	}
	public void finish() {
		
		AsrInstance.getInstance().cancel();
		//AsrInstance.getInstance().Destory(); //销毁ESR
		//super.finish();
	}
 
	

	//@Override
	public void onAsrInit() { 
		//构建语法;语法文件存在assets目录中;
		try {
			InputStream is = this.mContext.getAssets().open("asr/olamenu.bnf");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();                       
			int ret = Asr.buildGrammar(buffer, size);
			if (0 != ret){
				Log.d(TAG,"buildGrammar error return=" + ret);
			}		

			buffer = null;
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//Toast.makeText(this,this.getString(R.string.init_ok),Toast.LENGTH_SHORT).show();
		mBindOk  = true;
    }
	
	/**
	 * 识别错误
	 */
	private void onErrorMsg() {
		String msg = "识别出错";
		switch (mLastErrorId){		 
		case RecognitionResult.NETWORK_TIMEOUT:// Network operation timed out.
			msg = "res net timeout";//this.getString(R.string.res_net_timeout);
			break;
		case RecognitionResult.NETWORK_ERROR: // Other network related errors.
			msg = "res net error";//this.getString(R.string.res_net_error);
			break;
		case RecognitionResult.AUDIO_ERROR: // Audio recording error.
			msg = "res recode error";//this.getString(R.string.res_recode_error);
			break;
		case RecognitionResult.CLIENT_ERROR:// Other client side errors.
			msg = "res client error";//this.getString(R.string.res_client_error);
			break;			
		case RecognitionResult.SPEECH_TIMEOUT: // No speech input
			msg = "res speech timeout";//this.getString(R.string.res_speech_timeout);
			return;
		case RecognitionResult.NO_MATCH: // No recognition result matched.
			msg = "res res no match";//this.getString(R.string.res_no_match);
			break;
		case RecognitionResult.RESPONSE_TIMEOUT: // User response timeout.
			msg = "res response timeout";//this.getString(R.string.res_response_timeout);
			break;
		} 	
	}	
	
	/**
	 * 结束录音
	 */
	protected void onEndRecord() {
		//Toast.makeText(this.mContext,"录音结束",Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 开始录音
	 */
	protected void onBeginRecord() {
		//Toast.makeText(this.mContext,"开始录音",Toast.LENGTH_LONG).show();
		//mImgMic.setImageResource(R.drawable.ico_mic_on);
		
	}

	/**
	 * 识别结果返回
	 */
	private void onResultsMsg() {
		if (mLastResults.size() == 0){
			//Log.i(TAG, "onResultsMsg() 没有识别出来 ");
			AiTalkShareData.setIdentificationFlag(2);
			//CustomToast.makeToast(this.mContext,"没识别出来，你可以说："+AiTalkShareData.recognize_words);//,Toast.LENGTH_SHORT).show();
			return;
		}else{
			//处理识别结果
			
			RecognitionResult result = mLastResults.get(0);
			
			//Log.d(TAG,"Confidence" + result.toString());
			int nSlots = result.mSlotList.size();
			String resMsg = "";
			for (int i = 0; i < nSlots; i ++){
				resMsg += result.mSlotList.get(i).mItemTexts[0];
			}
			for (int i = 0; i < AiTalkShareData.recognize_words.length; i ++) {
				if(resMsg.equals(AiTalkShareData.recognize_words[i]))
				{
				  AiTalkShareData.setIdentificationFlag(1);	
				  //Log.i(TAG, "onResultsMsg() 识别成功 " );//+ AiTalkShareData.getIdentificationFlag());	
				  //CustomToast.makeToast(this.mContext,"识别成功:"+resMsg);//,Toast.LENGTH_SHORT).show();
				  break;
				}
				else
				{
					AiTalkShareData.setIdentificationFlag(2);
					//CustomToast.makeToast(this.mContext,"识别出错:"+resMsg);
				}
			}
		}			
	}
	
	/**
	 * 实现服务的回调接口 IRecognitionListener
	 */
	public void onBeginningOfRecord() {
		Log.d(TAG,"onBeginningOfRecord" );
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BEGINNING_OF_RECORD), 0);
	}

	public void onBeginningOfSpeech() {

		Log.d(TAG,"onBeginningOfSpeech");
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BEGIN_OF_SPEECH), 0);

	}

	public void onBufferReceived(byte[] buffer) {
		Log.d(TAG,"onBufferReceived length=" + buffer.length);
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BUFFER_RECEIVERD), 0);
	}

	public void onEndOfRecord() {
		Log.d(TAG,"onEndOfRecord" );	
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_END_OF_RECORD), 0);
	}

	public void onEndOfSpeech() {
		Log.d(TAG,"onEndOfSpeech");
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_END_OF_SPEECH), 0);	
	}

	public void onError(int error) {
		mLastErrorId = error;
		Log.d(TAG,"on error=" + error);
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_ERROR), 0);
	}

	public void onResults(List<RecognitionResult> results, long key) {
		Log.d(TAG,"on results =" + results.size());
		mLastResults = results;
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_RESULTS), 0);			

	}
	

}