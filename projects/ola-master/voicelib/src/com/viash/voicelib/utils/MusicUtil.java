package com.viash.voicelib.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class MusicUtil {
	
	public static  class MusicInfo{
		public String id;
		public String title;
		public String artist;
		public String album;
		public String url;
		public String photo;
		
		public MusicInfo(String id,String title,String artist,String album,String url,String photo){
			this.id = id;
			this.title = title;
			this.artist = artist;
			this.album = album;
			this.url = url;
			this.photo = photo;
		}
		public JSONObject toJsonObject(){
			JSONObject obj = new JSONObject();
			try{
				if(id != null )
					obj.put("id", id);
				if(title !=null)
					obj.put("title", title);
				if(artist !=null)
					obj.put("artist", artist);
				if(album !=null)
					obj.put("album", album);
				if(url !=null)
					obj.put("url", url);
				if(photo != null)
					obj.put("photo", photo);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return obj;
		}
	}
	
	
	public static MusicInfo getMusicPlaying(String id,String title,String artist,String album,String url,String photo){
		MusicInfo musicInfo = new MusicInfo(id, title, artist, album, url, photo);
		return musicInfo;
	}
	
	

}
