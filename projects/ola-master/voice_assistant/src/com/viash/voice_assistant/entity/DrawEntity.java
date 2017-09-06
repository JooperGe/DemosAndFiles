package com.viash.voice_assistant.entity;

import android.graphics.Bitmap;

public class DrawEntity {
	private String title;
	private String image_small_url;
	private Bitmap image_small;
	private String url;
	private String id;

	public DrawEntity(String title, String image_small_url, Bitmap image_small,
			String url, String id) {
		super();
		this.title = title;
		this.image_small_url = image_small_url;
		this.setImage_small(image_small);
		this.url = url;
		this.id = id;
	}


	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the image_small_url
	 */
	public String getImage_small_url() {
		return image_small_url;
	}


	/**
	 * @param image_small_url the image_small_url to set
	 */
	public void setImage_small_url(String image_small_url) {
		this.image_small_url = image_small_url;
	}


	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}


	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	public Bitmap getImage_small() {
		return image_small;
	}


	public void setImage_small(Bitmap image_small) {
		this.image_small = image_small;
	}

}
