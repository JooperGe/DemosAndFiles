package com.viash.voice_assistant.component;

import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class DrawerHScrollView extends HorizontalScrollView {
	private static final String TAG = "DrawerHScrollView";

	private IDrawerPresenter drawerPresenter = null;
	private int currentPage = 0;
	private int totalPages = 1;
	private boolean hasFlinged = false;
	private static Hashtable<Integer, Integer> positionLeftTopOfPages = null;// this

	public DrawerHScrollView(Context context) {
		super(context);
	}

	public DrawerHScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawerHScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void cleanup() {
		currentPage = 0;
		totalPages = 1;
		drawerPresenter = null;
		if (positionLeftTopOfPages != null) {
			positionLeftTopOfPages.clear();
		}
	}

	public void setParameters(int totalPages, int currentPage, int scrollDisX) {
		Log.d(TAG, "~~~~~setParameters totalPages:" + totalPages
				+ ",currentPage:" + currentPage + ",scrollDisX:" + scrollDisX);
		this.totalPages = totalPages;
		this.currentPage = currentPage;
		if (positionLeftTopOfPages == null) {
			positionLeftTopOfPages = new Hashtable<Integer, Integer>();
		}
		positionLeftTopOfPages.clear();
		for (int i = 0; i < totalPages; i++) {
			int posx = (scrollDisX) * i;
			positionLeftTopOfPages.put(i, posx);
			Log.d(TAG, "~~~~~setParameters i:" + i + ",posx:" + posx);
		}
		smoothScrollTo(0, 0);
	}

	public void setPresenter(IDrawerPresenter drawerPresenter) {
		this.drawerPresenter = drawerPresenter;
	}

	@Override
	public void fling(int velocityX) {
		if (positionLeftTopOfPages == null) {
			return;
		}
		boolean change_flag = false;
		if (velocityX > 0 && (currentPage < totalPages - 1)) {
			currentPage++;
			change_flag = true;
		} else if (velocityX < 0 && (currentPage > 0)) {
			currentPage--;
			change_flag = true;
		}
		if (change_flag) {
			hasFlinged = true;
			smoothScrollTo(positionLeftTopOfPages.get(currentPage), 0);
			if (drawerPresenter != null) {
				drawerPresenter.dispatchEvent(totalPages, currentPage);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (positionLeftTopOfPages == null) {
			return false;
		}
		final boolean ret = super.onTouchEvent(ev);
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL) {
			if (hasFlinged || (totalPages == 0)) {
			} else {
				hasFlinged = true;
				smoothScrollTo(positionLeftTopOfPages.get(currentPage), 0);
				if (drawerPresenter != null){
					drawerPresenter.dispatchEvent(totalPages, currentPage);
				}
			}
		} else {
			hasFlinged = false;
		}
		return ret;
	}
		
	public interface IDrawerPresenter {
		IDrawerPresenter getInstance();
		void dispatchEvent(int totalPages, int currentPage);
	}
}
