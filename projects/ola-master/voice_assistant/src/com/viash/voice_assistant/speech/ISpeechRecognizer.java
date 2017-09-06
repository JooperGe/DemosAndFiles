package com.viash.voice_assistant.speech;


public interface ISpeechRecognizer {

	public boolean isRecognizing();
	public void stopRecognize();
	public void abort();
	
	public boolean isIsLogined();
	public boolean create();
	
	public boolean isRecognizeSuccess();
	public void setRecognizeSuccess(boolean suc);
	
	public void destroy();
	
	public void setListener(IRecognizeListener listener);
	public boolean startRecognize(long maxWait, boolean offLine);
	
	public void stopWakeUp();
	
}
