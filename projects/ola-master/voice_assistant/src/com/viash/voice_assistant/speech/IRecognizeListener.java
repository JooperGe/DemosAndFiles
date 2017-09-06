package com.viash.voice_assistant.speech;

public interface IRecognizeListener
{
	public void onError(int errCode);
	public void onEndOfSpeech();		
	public void onBeginningOfSpeech();
	public void onResults(String result, String wavFileName);
	public void onCancel();
	public void onVolumeUpdate(int newVolume);
}