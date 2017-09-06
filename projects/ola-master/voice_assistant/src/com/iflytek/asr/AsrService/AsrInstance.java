package com.iflytek.asr.AsrService;

import java.util.List;

import com.iflytek.asr.RecognitionResult;
import com.iflytek.asr.Recognizer;

import android.os.RemoteException;
import android.util.Log;

/**
 * 搴曞眰璇嗗埆浜や簰鎺ュ彛
 * @author yjzhao @ 2010.8.21
 *
 */
public class AsrInstance
{	
	private static final String TAG = "AITALK_AsrInstance";
	
	private static AsrInstance  mInstance = null;
	
	/**
	 * 渚涘閮ㄨ皟鐢�鍙敓鎴愬敮涓�殑瀵硅薄
	 * @return
	 */
	public static AsrInstance getInstance()
	{
		if(null == mInstance)
		{
			mInstance = new AsrInstance();
		}
		return mInstance;
	}
	
	/**
	 * 鏄惁宸茬粡鍒濆鍖�
	 * @return
	 */
	public static boolean isInitialized()
	{
		return mInstance != null;
	}
	
	private AsrInstance() 
	{
		Asr.init();		
		Log.d(TAG,"onCreate Ok ");
		initSettings(); 
		Log.d(TAG,"onSetting Ok ");
	}
	public void Destory()
	{
		if(mInstance != null)
		{
			Asr.Destory();
			mInstance = null;
		}
	}
	@Override
	protected void finalize() throws Throwable 
	{
		super.finalize();
		Log.d(TAG,"onDestroy Ok ");		
	};
	
	public void initSettings()
	{
		try{
			Asr.setParam(Asr.PARAM_ENHANCEVAD,1);
			Asr.setParam(Asr.PARAM_DISABLEVAD,0);
//			int nEVAD = Settings.System.getInt(this.getContentResolver(), 
//					Recognizer.KEY_ENHANCE_VAD);
//			
//			int nVAD = Settings.System.getInt(this.getContentResolver(), 
//					Recognizer.KEY_VAD);
//			
//			int nSens = Settings.System.getInt(this.getContentResolver(), 
//					Recognizer.KEY_SENSITIVITY_LEVEL);
//			
//			int nSpeech = Settings.System.getInt(this.getContentResolver(), 
//					Recognizer.KEY_SPEECH_TIMEOUT);
//			
//			int nResp = Settings.System.getInt(this.getContentResolver(), 
//					Recognizer.KEY_RESPONSE_TIMEOUT);
//			Asr.setParam(Asr.PARAM_ENHANCEVAD,nEVAD);
//			Asr.setParam(Asr.PARAM_DISABLEVAD,nVAD);
//			Asr.setParam(Asr.PARAM_SENSITIVITY,nSens);
//			Asr.setParam(Asr.PARAM_SPEECHTIMEOUT,nSpeech);
//			Asr.setParam(Asr.PARAM_RESPONSETIMEOUT,nResp);			
		}catch(Exception e){
			Log.e(TAG,e.toString());
		}		
	}

	/**
	 * 鍋滄璇嗗埆寮曟搸
	 * @throws RemoteException
	 */
	public void cancel()
	{
		Asr.exitService();
	}

	public List<RecognitionResult> getRecognitionResults(long key)
	{	
		return Asr.getRecognitionResults(key);
	}

	public int makeVoiceTag(String lexiconName, String word,
			byte[] pcmData, int dataLength)
	{
		return Asr.makeVoiceTag(lexiconName,word,pcmData,dataLength);
	}

	public int setAudioDiscard(long time)
	{			
		return Asr.setParam(Asr.PARAM_AUDIODISCARD,(int)time);
	}

	public int setEnhanceVAD(boolean isSetEnhanceVAD)
			throws RemoteException 
	{
		int value = 0;
		if (isSetEnhanceVAD) value = 1;
		
		return Asr.setParam(Asr.PARAM_ENHANCEVAD,value);
	}

	public int setResponseTimeout(long time) 
	{
		return Asr.setParam(Asr.PARAM_RESPONSETIMEOUT,(int)time);
	}

	public int setSensitivity(int sensitivityLevel)
	{
		return Asr.setParam(Asr.PARAM_SENSITIVITY,sensitivityLevel);
	}

	public int setSpeechTimeout(long time)
	{
		return Asr.setParam(Asr.PARAM_SPEECHTIMEOUT,(int)time);
	}

	public int setVAD(boolean isSetVAD) throws RemoteException 
	{
		int value = 0;
		if (isSetVAD) value = 1;
		return Asr.setParam(Asr.PARAM_DISABLEVAD,value);
	}
	
	public void startListening(Recognizer.IRecognitionListener listener)
	{
		//AsrRecord.startRecord();
		Asr.setListener(listener);
		Asr.start();
	}

	public int setScene(String sceneName)
	{
		return Asr.setScene(sceneName);
	}



	public int addLexiconItem(String name,String word, int id)
	{
		return Asr.addLexiconItem(name, word, id);
	}


	public int delLexiconItem(String name,String word)
	{
		return Asr.delLexiconItem(name, word);
	}

	public int createLexicon(String lexiconName)
	{
		return Asr.createLexicon(lexiconName);
	}


	public int updateLexicon(String name)
	{
		// TODO Auto-generated method stub
		return  Asr.updateLexicon(name);
	}

	public int buildGrammar(byte[] xmlText, int length)
	{			
		return Asr.buildGrammar(xmlText, length); 
	}	
}
