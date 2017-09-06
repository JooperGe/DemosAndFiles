package com.viash.voice_assistant.component;

import android.graphics.Bitmap;

public class DrawerItem {
	private Bitmap bitmap;
	private String url;

	DrawerItem(String url) {
		this.url = url;
	}

	DrawerItem(String url, Bitmap bitmap) {
		this.url = url;
		this.bitmap = bitmap;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}
}