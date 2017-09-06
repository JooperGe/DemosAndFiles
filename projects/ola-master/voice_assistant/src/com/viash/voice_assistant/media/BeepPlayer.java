package com.viash.voice_assistant.media;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.viash.voicelib.utils.ThreadUtil;

public class BeepPlayer {
	private static boolean mIsPlayFinished = false;
	protected static MediaPlayer mPlayer = new MediaPlayer();
	public static void play(Context context,String file, boolean wait) {		
		AssetFileDescriptor afd = null;
		try {
			afd = context.getAssets().openFd(file);
		} catch (Exception e) { 
			e.printStackTrace();
		}
		try {
			mPlayer.reset();
			mPlayer.setLooping(false);
			mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.prepare();	
			afd.close();
			
			if(wait)
			{
				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {					
						mIsPlayFinished = true;
					}
				});
				mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						mIsPlayFinished = true;
						return false;
					}
				});
			}
			
			mIsPlayFinished = false;
			mPlayer.start();
			
			if(wait)
			{
				while(!mIsPlayFinished)
				{			
					ThreadUtil.sleep(20);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
}
