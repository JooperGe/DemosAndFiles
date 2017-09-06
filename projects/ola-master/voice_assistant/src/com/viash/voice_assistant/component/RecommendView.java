package com.viash.voice_assistant.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.adapter.DrawerListAdapter;
import com.viash.voice_assistant.adapter.DrawerListAdapter.DrawerItemClickListener;
import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.component.DrawerHScrollView.IDrawerPresenter;
import com.viash.voice_assistant.util.DDCouponUtil;
import com.viash.voice_assistant.util.DDCouponUtil.IResultListener;
import com.viash.voicelib.msg.MsgConst;

@SuppressLint("ViewConstructor")
public class RecommendView extends RelativeLayout implements IDrawerPresenter , IResultListener{
	private static final String TAG = "RecommendView";

	private TabHost mTabhost;

	private String[] mSubtabs = {"coupon", "app"};
	private DrawerHScrollView recommend_hscrollview = null;
	private GridView recommend_GridView = null;
	private LinearLayout layout_pagenumber = null;
	
	/**
	 * content view;
	 */
	private RelativeLayout layout_base;

	private Handler mHandler;
	private Context mContext;
	
	private DrawerListAdapter adapter;
	private DrawerItemClickListener mListener;

	private boolean isCouponLast = false;
	private boolean hasMeasured = false;
	
	@SuppressLint("HandlerLeak")
	private Handler mOwenHandler = new Handler () {
		@Override
		public void handleMessage(final Message msg) {
			updateDrawerLayout();
		}
	};
	
	
	public RecommendView(Context context, Handler handler) {
		super(context);
		this.mContext = context;
		this.mHandler = handler;
		layout_base = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.recommend_layout,this,true);
		recommend_hscrollview = (DrawerHScrollView)layout_base.findViewById(R.id.recommend_hscrollview);
		recommend_GridView = (GridView)layout_base.findViewById(R.id.recommend_gridView);
		layout_pagenumber = (LinearLayout)layout_base.findViewById(R.id.layout_pagenumber);
		initViews();
		DDCouponUtil.getInstance(context).setListener(this);
		
		ViewTreeObserver vto = recommend_hscrollview.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (hasMeasured == false) {
					int wid = recommend_hscrollview.getWidth();
					int hei = recommend_hscrollview.getHeight();
					if (wid != 0 && hei != 0) {
						hasMeasured = true;
						mTabhost.setCurrentTab(0);	
						updateDrawerLayout();
						recommend_hscrollview.getViewTreeObserver().removeOnPreDrawListener(this);
					}
				}
				return true;
			}
		});
		mTabhost.setCurrentTab(0);	
	}

	private void initViews() {
		mTabhost = (TabHost) findViewById(android.R.id.tabhost);
		mTabhost.setup();

		//Coupon View.
		/*View subtabview_left = LayoutInflater.from(getContext()).inflate(R.layout.recommend_title, null);
		final TextView left_title = (TextView) subtabview_left.findViewById(R.id.tv_recommend_title);
		final TextView left_bar = (TextView) subtabview_left.findViewById(R.id.tv_recommend_title_bar);
		left_title.setText(getResources().getString(R.string.str_coupon));
		left_bar.setVisibility(View.VISIBLE);
		mTabhost.addTab(mTabhost.newTabSpec(mSubtabs[0])
				.setIndicator(subtabview_left)
				.setContent(recommend_hscrollview.getId()));*/
		//App View;
		View subtabview_right = LayoutInflater.from(getContext()).inflate(R.layout.recommend_title, null);
		final TextView right_title = (TextView) subtabview_right.findViewById(R.id.tv_recommend_title);
		final TextView right_bar = (TextView) subtabview_right.findViewById(R.id.tv_recommend_title_bar);
		right_title.setText(getResources().getString(R.string.str_app));
		right_title.setTextColor(getResources().getColor(R.color.alarm_txt_blue));
		right_bar.setVisibility(View.VISIBLE);
		mTabhost.addTab(mTabhost.newTabSpec(mSubtabs[1])
				.setIndicator(subtabview_right)
				.setContent(recommend_hscrollview.getId()));
		
		recommend_hscrollview.setVisibility(View.VISIBLE);
		isCouponLast = false;
	/*	mTabhost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {

				switch (mTabhost.getCurrentTab()) {
				case 0://coupon
					left_title.setTextColor(getResources().getColor(
							R.color.alarm_txt_blue));
					right_title.setTextColor(getResources().getColor(
							R.color.alarm_txt_gray));
					left_bar.setVisibility(View.VISIBLE);
					right_bar.setVisibility(View.INVISIBLE);
					recommend_hscrollview.setVisibility(View.VISIBLE);
					isCouponLast = true;
					updatePromoData();
					break;
				case 1://app
					right_title.setTextColor(getResources().getColor(
							R.color.alarm_txt_blue));
					left_title.setTextColor(getResources().getColor(
							R.color.alarm_txt_gray));
					right_bar.setVisibility(View.VISIBLE);
					left_bar.setVisibility(View.INVISIBLE);
					recommend_hscrollview.setVisibility(View.VISIBLE);
					isCouponLast = false;
					updatePromoData();
					break;
				default:
					break;
				}

			}
		});*/

		recommend_hscrollview.setPresenter(this);
		updatePromoData();		
	}
	
	
	private void updatePromoData() {
		//App Data has already been gotten when Application start. so it only need to update the Coupon data.
		if (isCouponLast ) {
			if (WifiLocation.getInstance(mContext).getBDLocation() == null || WifiLocation.getInstance(mContext).getLocation() == null) {
				//TODO can't get your location. show message
				return;
			} else {
				DDCouponUtil.getInstance(mContext).getCouponList();
			}
		}
		else {
			
		}
		updateDrawerLayout();
	}
	
	public void updateDrawerLayout() {
		if (isCouponLast && 0 == DDCouponUtil.getInstance(mContext).getCouponList().size()) {
			Log.d(TAG, "Coupon itemList is null or empty");
			//TODO
			recommend_hscrollview.setVisibility(View.INVISIBLE);
			return;
		}else if (!isCouponLast && 0 == DDCouponUtil.getInstance(mContext).getAppList().size()){
			Log.d(TAG, "App itemList is null or empty");
			//TODO
			recommend_hscrollview.setVisibility(View.INVISIBLE);
			return;
		}

		recommend_hscrollview.setVisibility(View.VISIBLE);
		
		if (!hasMeasured) {
			mTabhost.setCurrentTab(1);
			mOwenHandler.sendMessageDelayed(mOwenHandler.obtainMessage(0), 500);
			Log.d(TAG, "hasMeasured is false");
			return;
		}
		int scrollWid = recommend_hscrollview.getWidth();
		int scrollHei = recommend_hscrollview.getHeight();
		if (scrollWid <= 0 || scrollHei <= 0) {
			Log.d(TAG, "scrollWid or scrollHei is less than 0");
			return;
		}
		if (recommend_GridView == null) {
			Log.e(TAG, "GridView init failed");
			return;
		}
		
		if (isCouponLast) {
			int spaceing = scrollWid / 100;
			int colWid = (scrollWid - spaceing * 3) / 3;
			int colHei = (scrollHei - spaceing * 2) / 2;
			int numCols = (DDCouponUtil.getInstance(mContext).getCouponList().size() - 1) / 2 + 1;
			int gridViewWid = numCols * colWid + (numCols + 1) * spaceing;
			// if numCols is odd (like 5), add blank space
			if (numCols % 2 == 1) {
				gridViewWid += colWid + spaceing;
			}
			Log.v(TAG, "~~~~~colWid:" + colWid + ",colHei:" + colHei);
			Log.v(TAG, "~~~~~numCols:" + numCols + ",gridViewWid:"
					+ gridViewWid);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(gridViewWid, scrollHei);
			recommend_GridView.setLayoutParams(params);
			recommend_GridView.setColumnWidth(colWid);
			recommend_GridView.setHorizontalSpacing(spaceing);
			recommend_GridView.setVerticalSpacing(spaceing);
			recommend_GridView.setStretchMode(GridView.NO_STRETCH);
			recommend_GridView.setNumColumns(numCols);
			recommend_GridView.setPadding(0, 0, 0, 0);

			adapter = new DrawerListAdapter(mContext, colWid, colHei, isCouponLast);
			mListener = adapter.new DrawerItemClickListener();
			recommend_GridView.setAdapter(adapter);
			recommend_GridView.setOnItemClickListener(mListener);

			int pageNum = (DDCouponUtil.getInstance(mContext).getCouponList().size() - 1) / 6 + 1;
			recommend_hscrollview.setParameters(pageNum, 0, gridViewWid);
			updateDrawerPageLayout(pageNum, 0);
		} else {
			int spaceing = 30;
			int colWid = (scrollWid - spaceing * 3 - 60) / 4;
			int colHei = (scrollHei - spaceing * 3 - 80) / 2;
			int numCols = (DDCouponUtil.getInstance(mContext).getAppList().size() - 1) / 2 + 1;
			int gridViewWid = numCols * colWid + (numCols + 1) * spaceing;
			// if numCols is odd (like 5), add blank space
			// if (numCols % 2 == 1) {
			// gridViewWid += colWid + spaceing;
			// }
			Log.v(TAG, "~~~~~colWid:" + colWid + ",colHei:" + colHei);
			Log.v(TAG, "~~~~~numCols:" + numCols + ",gridViewWid:"
					+ gridViewWid);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(gridViewWid, scrollHei);
			recommend_GridView.setLayoutParams(params);
			recommend_GridView.setColumnWidth(colWid);
			recommend_GridView.setHorizontalSpacing(spaceing);
			recommend_GridView.setVerticalSpacing(spaceing);
			recommend_GridView.setStretchMode(GridView.NO_STRETCH);
			recommend_GridView.setNumColumns(numCols);
			recommend_GridView.setPadding(20, 20, 20, 20);
			adapter = new DrawerListAdapter(mContext, colWid, colHei, isCouponLast);
			mListener = adapter.new DrawerItemClickListener();
			recommend_GridView.setAdapter(adapter);
			recommend_GridView.setOnItemClickListener(mListener);

			int pageNum = (DDCouponUtil.getInstance(mContext).getAppList().size() - 1) / 8 + 1;
			recommend_hscrollview.setParameters(pageNum, 0, scrollWid - spaceing);
			updateDrawerPageLayout(pageNum, 0);
		}
	}
	
	public void updateDrawerPageLayout(int total_pages, int sel_page) {
		Log.e(TAG, "~~~updateBooksPageLayout total_pages:" + total_pages
				+ ",sel_page:" + sel_page);
		layout_pagenumber.removeAllViews();
		if (total_pages <= 0 || sel_page < 0 || sel_page >= total_pages) {
			Log.e(TAG, "total_pages or sel_page is outofrange.");
			return;
		}
		if (total_pages > 1) {
			for (int i = 0; i < total_pages; i++) {
				if (i != 0) {
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.setMargins(5, 0, 0, 0);
					layout_pagenumber.addView(new PageItemImageView(mContext),	params);
				} else {
					layout_pagenumber.addView(new PageItemImageView(mContext));
				}
			}
			PageItemImageView selItem = (PageItemImageView) layout_pagenumber
					.getChildAt(sel_page);
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon_page_selected);
			selItem.setImageBitmap(bitmap);
		}
	}
	
	public class PageItemImageView extends ImageView {
		public PageItemImageView(Context context) {
			super(context);
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon_page_normal);
			this.setImageBitmap(bitmap);
		}
	}
	
	@Override
	public IDrawerPresenter getInstance() {
		return this;
	}

	@Override
	public void dispatchEvent(int totalPages, int currentPage) {
		Log.i(TAG, "~~~~dispatchEvent currentPage:" + currentPage);
		Message msg = Message.obtain();
		msg.what = MsgConst.MSG_DRAWER_UPDATE_PAGE_LAYOUT;
		msg.arg1 = totalPages;
		msg.arg2 = currentPage;
		if(mHandler != null)
		  mHandler.sendMessage(msg);
		updateDrawerPageLayout(totalPages,currentPage);
	}

	@Override
	public void onResultContainsNewPromo(boolean result) {
		if (result) {
			if(mHandler != null)
			  mHandler.sendEmptyMessage(MsgConst.CLIENT_ACTION_SHOW_NEW_ICON_ON_PROMO_BUTTON);
		}
		// TODO Auto-generated method stub
	}

	@Override
	public void onReceiveNewListData() {
		// TODO Auto-generated method stub
		mOwenHandler.sendEmptyMessage(0);
	}

}
