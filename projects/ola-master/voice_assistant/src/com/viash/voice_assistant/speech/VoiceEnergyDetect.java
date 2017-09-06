package com.viash.voice_assistant.speech;

public class VoiceEnergyDetect {
	// Start energy detection
	static{
		System.loadLibrary("VoiceEnergyDetect");//Leo
	}
	public static native void startDetect();
	
	// Detect a segment of voice
	// The return value contains two values, the first is voice_start, the second is voice_end
	// if voice_start = 0 && voice_end = 0, it means there is no voice in this segment
	// if voice_start = 0 means the voice has been started
	// if voice_end == dataLen - 1 means the voice is ot ended yet
	public static native long addData(byte[] data, int dataLen);
	
	// Stop Detect
	public static native void stopDetect();
}
