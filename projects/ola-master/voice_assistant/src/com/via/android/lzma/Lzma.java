package com.via.android.lzma;

public class Lzma {
	static {
		System.loadLibrary("lzma");
	}
	
	public synchronized static native byte[] compress(byte[] data);
	public synchronized static native byte[] decompress(byte[] data);
}
