package com.viash.voice_assistant.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.GuideActivity;
import com.viash.voice_assistant.entity.MusicEntity;
import com.viash.voice_assistant.media.Playlist;

public class MusicNotificationService extends Service {
	private static final boolean DEBUG = true;
	private static final String TAG = "MusicNotificationService";
	private static int NOTIFICATION_ID = 19180439;	
	public static boolean isStarted = false;

	public static final String NOTIFICATION_MUSIC_CANCEL_ACTION = "com.viash.voice_assistant.MUSIC_CANCEL";
	public static final String NOTIFICATION_MUSIC_PLAYPAUSE_ACTION = "com.viash.voice_assistant.MUSIC_PLAYPAUSE";
	public static final String NOTIFICATION_MUSIC_NEXT_ACTION = "com.viash.voice_assistant.MUSIC_NEXT";

	private static NotificationManager mNotificationManager;
	private Notification musicNotification;

	@Override
	public void onCreate() {
		super.onCreate();

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		isStarted = true;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			if (DEBUG) Log.i(TAG, "action: " + intent.getAction());
			if (MusicService.STOP.equals(intent.getAction())) {
				cancel_music();
				return;
			}
			if (MusicService.PLAY.equals(intent.getAction())) {
				updateMusicNotification(0);
				return;
			}
			if (MusicService.PAUSE.equals(intent.getAction())) {
				updateMusicNotification(1);
				return;
			}
			if (MusicService.NEXT.equals(intent.getAction())) {
				updateMusicNotification(2);
				return;
			}
			if (MusicService.UPDATE_ARTWORK.equals(intent.getAction())) {
				updateMusicNotification(3);
				return;
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		mNotificationManager = null;
		isStarted = false;
		super.onDestroy();
	}

	private void updateMusicNotification(int type) {
		MusicEntity entity = Playlist.Instance().getSelectedTrack();
		
		String title = entity.getName();
		if (title == null) {
			title = "未知";
		}
		String artistAlbum = getMusicInfo (entity);
		if (artistAlbum == null) {
			artistAlbum = "未知";
		}
		
		if (musicNotification == null) {
			musicNotification = new Notification(
				R.drawable.icons_music, title, System.currentTimeMillis());
		}else {
			musicNotification.tickerText = title;
			musicNotification.when = System.currentTimeMillis();
		}
		musicNotification.contentView = new RemoteViews("com.viash.voice_assistant",
				R.layout.music_notification);
		musicNotification.flags = Notification.FLAG_NO_CLEAR;
		

		musicNotification.contentView.setTextViewText(R.id.title, title);
		musicNotification.contentView.setTextViewText(R.id.artist_album, artistAlbum);
		musicNotification.contentView.setTextColor(R.id.artist_album,Color.GRAY);
		Bitmap bm = Playlist.getCurrentEntryArtWork();
		if (bm != null) {
			musicNotification.contentView.setImageViewBitmap(R.id.artwork, bm);
		}else {
			musicNotification.contentView.setImageViewResource(R.id.artwork, R.drawable.icon_defalut_music_author);
		}
		
		switch (type){
		case 0:
		case 2:
			musicNotification.contentView.setImageViewResource(R.id.btn_music_play, R.drawable.icon_play);
			break;
		case 1:
			musicNotification.contentView.setImageViewResource(R.id.btn_music_play, R.drawable.icon_pause);
			break;		
		}
		
		Intent cancelIntent = new Intent();
		cancelIntent.setAction(NOTIFICATION_MUSIC_CANCEL_ACTION);
		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,
				NOTIFICATION_ID , cancelIntent, 0);
		musicNotification.contentView.setOnClickPendingIntent(
				R.id.btn_cancel, cancelPendingIntent);
		
		Intent playPauseIntent = new Intent();
		playPauseIntent.setAction(NOTIFICATION_MUSIC_PLAYPAUSE_ACTION);
		PendingIntent playPauseIntentPendingIntent = PendingIntent.getBroadcast(this,
				NOTIFICATION_ID, playPauseIntent, 0);
		musicNotification.contentView.setOnClickPendingIntent(
				R.id.btn_music_play, playPauseIntentPendingIntent);
		
		Intent nextIntent = new Intent();
		nextIntent.setAction(NOTIFICATION_MUSIC_NEXT_ACTION);
		PendingIntent nextIntentPendingIntent = PendingIntent.getBroadcast(this,
				NOTIFICATION_ID, nextIntent, 0);
		musicNotification.contentView.setOnClickPendingIntent(
				R.id.btn_next, nextIntentPendingIntent);

		Intent intent = new Intent(this, GuideActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		musicNotification.contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        
		mNotificationManager.notify(NOTIFICATION_ID , musicNotification);
	}
	
	public void cancel_music() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID );
		}
		this.stopSelf();
	}
	
	private String getMusicInfo(MusicEntity mEntry){
		if (mEntry != null ) {
			String artistAlbum = null;
			if (mEntry.getAuthor() != null && !mEntry.getAuthor().equals("")){
				artistAlbum = mEntry.getAuthor();					
			}
			if (mEntry.getAlbum() != null && !mEntry.getAlbum().equals("")){
				if (artistAlbum != null) {
					artistAlbum = artistAlbum + " - " + mEntry.getAlbum();
				}
				else {
					artistAlbum = mEntry.getAlbum();
				}
			}
			if (artistAlbum != null) {
				return artistAlbum;
			}else {
				return "未知";
			}				
		} else {
			return "未知";
		}
	}
}
