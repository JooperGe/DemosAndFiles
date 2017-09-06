package com.viash.voicelib.widget;

import com.viash.voicelib.utils.ImageLoaderCacheUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class NetImageTextView extends TextView{

	public NetImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NetImageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NetImageTextView(Context context) {
		super(context);
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try
		{
			return super.onTouchEvent(event);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void setHtmlText(String html)
	{
		setMovementMethod(LinkMovementMethod.getInstance());
		this.setText(Html.fromHtml(html, new ImageGetter() {
			
			@Override
			public Drawable getDrawable(String source) {
				Drawable drawable = null;
				Bitmap bmp = ImageLoaderCacheUtil.getNetBitmap2(source);
				if(bmp != null)
				{
					drawable = new BitmapDrawable(bmp);
					drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
				}
				
				return drawable;
			}
		}, null
		));
	}

}
