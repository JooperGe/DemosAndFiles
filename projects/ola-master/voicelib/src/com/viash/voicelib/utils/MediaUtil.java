package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class MediaUtil {
	private static int MAX_QUERY_COUNT = 100;
	protected static List<AudioInfo> mLstAudio = new ArrayList<AudioInfo>();
	protected static List<VideoInfo> mLstVideo = new ArrayList<VideoInfo>();
	
	public static class AudioInfo
	{
		protected long mId = 0;
		protected int mYear = 0;
		protected long mDuration = 0;
		protected long mSize = 0;
		protected String mTitle = null;
		protected String mAlbum = null;
		protected String mArtist = null;
		protected String mUrl = null;
		protected String mComposer = null;
		
		public AudioInfo(long mId, int mYear, long mDuration, long mSize,
				String mTitle, String mAlbum, String mArtist, String mUrl,
				String mComposer) {
			super();
			this.mId = mId;
			this.mYear = mYear;
			this.mDuration = mDuration;
			this.mSize = mSize;
			this.mTitle = mTitle;
			this.mAlbum = mAlbum;
			this.mArtist = mArtist;
			this.mUrl = mUrl;
			this.mComposer = mComposer;
		}
		
		public JSONObject toJsonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", mId);
			
//				if(mYear != 0)
//					obj.put("year", mYear);
//				
//				if(mDuration != 0)
//					obj.put("duration", mDuration);
//				
//				if(mSize != 0)
//					obj.put("size", mSize);
				
				if(mTitle != null)
					obj.put("title", mTitle);
				
				if(mAlbum != null)
					obj.put("album", mAlbum);
				
				if(mArtist != null)
					obj.put("artist", mArtist);
				
				if(mUrl != null)
					obj.put("url", mUrl);
				
//				if(mComposer != null)
//					obj.put("composer", mComposer);
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
	}
	
	public static class VideoInfo
	{
		protected long mId = 0;
		protected int mDate = 0;
		protected long mDuration = 0;
		protected long mSize = 0;
		protected String mTitle = null;
		protected String mAlbum = null;
		protected String mArtist = null;
		protected String mUrl = null;
		protected String mCategory = null;
		protected String mDescription = null;
		protected String mLanguage = null;
		protected String mResolution = null;		
		
		public VideoInfo(long mId, int mDate, long mDuration, long mSize,
				String mTitle, String mAlbum, String mArtist, String mUrl,
				String mCategory, String mDescription, String mLanguage,
				String mResolution) {
			super();
			this.mId = mId;
			this.mDate = mDate;
			this.mDuration = mDuration;
			this.mSize = mSize;
			this.mTitle = mTitle;
			this.mAlbum = mAlbum;
			this.mArtist = mArtist;
			this.mUrl = mUrl;
			this.mCategory = mCategory;
			this.mDescription = mDescription;
			this.mLanguage = mLanguage;
			this.mResolution = mResolution;
		}



		public JSONObject toJsonObject()
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", mId);
			
//				if(mDate != 0)
//					obj.put("date", mDate);
//				
//				if(mDuration != 0)
//					obj.put("duration", mDuration);
//				
//				if(mSize != 0)
//					obj.put("size", mSize);
				
				if(mTitle != null)
					obj.put("title", mTitle);
				
//				if(mAlbum != null)
//					obj.put("album", mAlbum);
//				
				if(mArtist != null)
					obj.put("artist", mArtist);
				
				if(mUrl != null)
					obj.put("url", mUrl);
				
//				if(mCategory != null)
//					obj.put("category", mCategory);
//				
//				if(mDescription != null)
//					obj.put("description", mDescription);
//				
//				if(mLanguage != null)
//					obj.put("language", mLanguage);
//				
//				if(mResolution != null)
//					obj.put("resolution", mResolution);
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}
	}
		
	public static JSONObject getJsonObjectOfAudios(List<AudioInfo> lstAudio)
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonAudios = new JSONArray();
		
		if(lstAudio != null)
		{
			for(AudioInfo info : lstAudio)
			{			
				jsonAudios.put(info.toJsonObject());
			}
		}
		
		try {
			jsonObject.put("music", jsonAudios);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	public static JSONObject getJsonObjectOfVideos()
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonVideos = new JSONArray();
		
		for(VideoInfo info : mLstVideo)
		{			
			jsonVideos.put(info.toJsonObject());
		}
		try {
			jsonObject.put("video", jsonVideos);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	public static List<AudioInfo> queryMusic(Context context, String title, String album, String artist)
	{		
		List<AudioInfo> lstAudioInfo =new ArrayList<MediaUtil.AudioInfo>();
		ContentResolver resolver = context.getContentResolver();
		String selection = "";		
		if(title != null && title.length() > 0)
		{
			if(selection.length() > 0)
				selection += " and ";
			selection += MediaStore.Audio.Media.TITLE + "=\'" + title + "\'";
		}
		if(album != null && album.length() > 0)
		{
			if(selection.length() > 0)
				selection += " and ";
			selection += MediaStore.Audio.Media.ALBUM + "=\'" + album + "\'";
		}
		if(artist != null && artist.length() > 0)
		{
			if(selection.length() > 0)
				selection += " and ";
			selection += MediaStore.Audio.Media.ARTIST + "=\'" + artist + "\'";
		}
		if(selection.length() > 0)
			selection +=" and " ;
		selection += MediaStore.Audio.Media.SIZE + " > " +(5*1024);
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
		if(cursor != null)
		{
			int colId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
			int colTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int colAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);  
			int colArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST); 
			int colUrl = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			
			if(cursor.moveToFirst())
			{
				do
				{
					long id = 0;
					String titleNew = null;
					String albumNew = null;
					String artistNew = null;
					String url = null;

					if(colId != -1)
						id = cursor.getLong(colId);
					
					if(colTitle != -1)
						titleNew = cursor.getString(colTitle);
					if(colAlbum != -1)
						albumNew = cursor.getString(colAlbum);
					if(colArtist != -1)
						artistNew = cursor.getString(colArtist);
					if(colUrl != -1)
						url = cursor.getString(colUrl);
					
					AudioInfo audioInfo = new AudioInfo(id, 0, 0, 0, titleNew, albumNew, artistNew, url, null);
					lstAudioInfo.add(audioInfo);
				}while(cursor.moveToNext() && lstAudioInfo.size() < MAX_QUERY_COUNT);
			}
			
			cursor.close();			
		}
		return lstAudioInfo;
	}
	
	/*
	public static void initContext(Context context)
	{		
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
		if(cursor != null)
		{
			int colId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
			int colTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int colAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);  
			int colArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST); 
			int colUrl = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			int colDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
			int colSize = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
			int colComposer = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
			int colYear = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
			
			if(cursor.moveToFirst())
			{
				do
				{
					long id = 0;
					int year = 0;
					long duration = 0;
					long size = 0;
					String title = null;
					String album = null;
					String artist = null;
					String url = null;
					String composer = null;
					
					if(colId != -1)
						id = cursor.getLong(colId);
					if(colYear != -1)
						year = cursor.getInt(colYear);
					if(colDuration != -1)
						duration = cursor.getLong(colDuration);
					if(colSize != -1)
						size = cursor.getLong(colSize);
					
					if(colTitle != -1)
						title = cursor.getString(colTitle);
					if(colAlbum != -1)
						album = cursor.getString(colAlbum);
					if(colArtist != -1)
						artist = cursor.getString(colArtist);
					if(colComposer != -1)
						composer = cursor.getString(colComposer);
					if(colUrl != -1)
						url = cursor.getString(colUrl);
					
					AudioInfo audioInfo = new AudioInfo(id, year, duration, size, title, album, artist, url, composer);
					mLstAudio.add(audioInfo);
				}while(cursor.moveToNext());
			}
			
			cursor.close();			
		}
		
		cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);  
		if(cursor != null)
		{
			int colId = cursor.getColumnIndex(MediaStore.Video.Media._ID);
			int colTitle = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
			int colAlbum = cursor.getColumnIndex(MediaStore.Video.Media.ALBUM);  
			int colArtist = cursor.getColumnIndex(MediaStore.Video.Media.ARTIST); 
			int colUrl = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
			int colDuration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
			int colSize = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
			int colCategory= cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY);
			int colDate = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
			int colDescription = cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION);
			int colLanguage = cursor.getColumnIndex(MediaStore.Video.Media.LANGUAGE);
			int colResolution = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);
			
			if(cursor.moveToFirst())
			{
				do
				{
					long id = 0;
					int date = 0;
					long duration = 0;
					long size = 0;
					String title = null;
					String album = null;
					String artist = null;
					String url = null;
					String category = null;
					String description = null;
					String language = null;
					String resolution = null;
					
					if(colId != -1)
						id = cursor.getLong(colId);
					if(colDate != -1)
						date = cursor.getInt(colDate);
					if(colDuration != -1)
						duration = cursor.getLong(colDuration);
					if(colSize != -1)
						size = cursor.getLong(colSize);
					
					if(colTitle != -1)
						title = cursor.getString(colTitle);
					if(colAlbum != -1)
						album = cursor.getString(colAlbum);
					if(colArtist != -1)
						artist = cursor.getString(colArtist);
	
					if(colUrl != -1)
						url = cursor.getString(colUrl);
					
					if(colCategory != -1)
						category = cursor.getString(colCategory);
					
					if(colDescription != -1)
						description = cursor.getString(colDescription);
					
					if(colLanguage != -1)
						language = cursor.getString(colLanguage);
					
					if(colResolution != -1)
						resolution = cursor.getString(colResolution);
					
					VideoInfo videoInfo = new VideoInfo(id, date, duration, size, title, album, artist, url, category, description, language, resolution);
					mLstVideo.add(videoInfo);
				}while(cursor.moveToNext());
			}
			cursor.close();			
		}
	}*/
}
