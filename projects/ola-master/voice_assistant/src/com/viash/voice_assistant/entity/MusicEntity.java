package com.viash.voice_assistant.entity;

public class MusicEntity {
	private int id;
	private String name;
	private String author;
	private String url;
	private String photo;
	private double  time;
	private String album;
	
	public MusicEntity(int id,String name, String author, String url,String photo,double time,String album) {
		super();
		this.id = id;
		this.name = name;
		this.author = author;
		this.url = url;
		this.photo =photo;
		this.time = time;
		this.album = album;
	}
	public MusicEntity(){
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
}
