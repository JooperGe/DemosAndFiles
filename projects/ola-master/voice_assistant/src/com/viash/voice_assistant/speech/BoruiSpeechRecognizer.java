package com.viash.voice_assistant.speech;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.media.BeepPlayer;

public class BoruiSpeechRecognizer implements ISpeechRecognizer{
	private static String TAG = "BoruiSpeechRecognizer";
	
	private SpeechRecognizer recognizer = null;
	private Toast mToast;
	private int speechResult = -1;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private IRecognizeListener otherListener = null;
	private Context _context;
	
	private static BoruiSpeechRecognizer _instance = null;
	private BoruiSpeechRecognizer(Context ctx){
		_context = ctx;
		SpeechUtility.createUtility(ctx, "appid=57c3a3c7" );
		
		recognizer = SpeechRecognizer.createRecognizer(ctx, mInitListener);
		mToast = Toast.makeText(ctx, "", Toast.LENGTH_SHORT);
		
	}
	
	public static synchronized BoruiSpeechRecognizer getInstance(Context ctx){
		if(null == _instance){
			_instance = new BoruiSpeechRecognizer(ctx);
		}
		return _instance;
	}
	
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};
	
	private String printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		// mResultText.setText(resultBuffer.toString());
		String r = resultBuffer.toString(); 
		showTip(r);
		return r;
	}
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	private RecognizerListener recognizerListner = new RecognizerListener() {
		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
//			showTip("开始说话");
			BeepPlayer.play(_context, "start.mp3", false);
			
			if(otherListener != null)
				otherListener.onBeginningOfSpeech();
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			showTip(error.getPlainDescription(true));
			
			if(otherListener != null)
				otherListener.onError(error.getErrorCode());
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
//			showTip("结束说话");
			BeepPlayer.play(_context, "stop.mp3", false);
			if(otherListener != null)
				otherListener.onEndOfSpeech();
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			String result = printResult(results);
			
			if (isLast) {
				if(null != otherListener)
					otherListener.onResults(result, null);
				
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
//			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
			
			if(null != otherListener)
				otherListener.onVolumeUpdate(volume);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
			
		}
	};
	
	//-----------------------------------------------------------------------
	
	@Override
	public boolean isRecognizing() {
		return recognizer.isListening();
	}

	@Override
	public void stopRecognize() {
		recognizer.stopListening();
	}

	@Override
	public void abort() {
		recognizer.cancel();
	}

	@Override
	public boolean isRecognizeSuccess() {
		return false;
	}

	@Override
	public void setRecognizeSuccess(boolean suc) {
		
	}

	@Override
	public void destroy() {
		recognizer.destroy();
	}

	@Override
	public void setListener(IRecognizeListener listener) {
		otherListener = listener;
	}

	@Override
	public boolean startRecognize(long maxWait, boolean offLine) {
		if(Tts.isPlaying())
			Tts.stop();
		
		speechResult = recognizer.startListening(recognizerListner);
		
		if (speechResult == ErrorCode.SUCCESS) {
			showTip("开始听写.等待说话");
			return true;
		}else{
			showTip(""+speechResult);
			Log.d(TAG, "error:" + speechResult);
			return false;
		}
	}

	@Override
	public void stopWakeUp() {
		
	}

	@Override
	public boolean isIsLogined() {
		return _instance != null;
	}

	@Override
	public boolean create() {
		return _instance != null;
	}
}
