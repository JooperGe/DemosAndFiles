package com.viash.voice_assistant.widget.selection;


public class SelectionViewData {
	public int mPrimaryTitleImg 		= 0;    //Main icon, in top left of the view.
	public String mPrimaryTitleText 	= null; //main title text.
	public String mSecondaryTitleText	= null; //title info after the main title or below the main title text
	public int mSecondaryTitleImg		= 0; 	//if mSecondaryTitleText is not null, this value will be ignored.
	
	public String mFilters[];					//filters if common function is available. this Filter should be set only one item
	public int mFilterIndex				= 0;	//Current filter index	
	public String mFilterOrCommFunText	= null;	//Filter text or common function text.
	public String mDefaultFilterClickString = null;
	
	public int mCommFunImg				= 0;	//common function image, only when common function is available, this value is not 0;
	
	
	public int mContentFunImg			= 0;
	public String mContentFunText		= null;
	
	public int mTotleItemNumber 		= 0;
	public int mMinItemNumber			= 3;
	
	public long mHighLight				= 0;
}
