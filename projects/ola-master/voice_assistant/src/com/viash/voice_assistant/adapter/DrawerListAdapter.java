package com.viash.voice_assistant.adapter;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity.ImageCallback;
import com.viash.voice_assistant.activity.ShowCouponPicActivity;
import com.viash.voice_assistant.common.CustomeAsyncTask;
import com.viash.voice_assistant.entity.DrawEntity;
import com.viash.voice_assistant.util.DDCouponUtil;
import com.viash.voicelib.utils.CustomToast;

public class DrawerListAdapter extends BaseAdapter {
	private static final String TAG = "DrawerListAdapter";
	
	private LayoutInflater mInflater;
	private RelativeLayout layout_item;
	private TextView tvTitle;
	private ImageView ivIcon;
	private int colWid;
	private int colHei;
	private boolean mIsCoupon = true;
	private Context mContext = null;
	
	private HashMap<String, SoftReference<Drawable>> mImageCache;
	CustomeAsyncTask mCouponAsyncTask = new CustomeAsyncTask();
	CustomeAsyncTask mAppAsyncTask = new CustomeAsyncTask();
	private couponDetail mDetail = null;
	private final long mUpdatedTime = 1000 * 60 * 60 * 12;

	public DrawerListAdapter(Context context, int colWid, int colHei, boolean isCoupon) {
		this.colWid = colWid;
		this.colHei = colHei;
		this.mIsCoupon = isCoupon;
		this.mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if (mIsCoupon) {
			return DDCouponUtil.getInstance(mContext).getCouponList().size();
		} else {
			return DDCouponUtil.getInstance(mContext).getAppList().size();
		}
	}

	public Object getItem(int position) {
		if (mIsCoupon) {
			return DDCouponUtil.getInstance(mContext).getCouponList().get(position);
		} else {
			return DDCouponUtil.getInstance(mContext).getAppList().get(position);
		}

	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("DefaultLocale")
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawEntity item;
		if (mIsCoupon) {
			item = DDCouponUtil.getInstance(mContext).getCouponList().get(position);
		} else {
			item = DDCouponUtil.getInstance(mContext).getAppList().get(position);
			Log.i(TAG, "URL: " + item.getImage_small_url() + "  title: " + item.getTitle());
		}
		if (convertView == null) {
			if (mIsCoupon) {
				convertView = mInflater.inflate(R.layout.drawer_item, null);
			} else {
				convertView = mInflater.inflate(R.layout.drawer_item_app,
						null);
			}
			layout_item = (RelativeLayout) convertView
					.findViewById(R.id.layout_item);
			ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
			tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			if (colHei != 0 && colWid != 0) {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						colWid, colWid);
				ivIcon.setLayoutParams(params);
			}
			convertView.setTag(layout_item);
		} else {
			layout_item = (RelativeLayout) convertView.getTag();
		}
		if (mIsCoupon) {
			long currentTiem;
			tvTitle.setText(item.getTitle());
			String imageUrl = item.getImage_small_url();
			ivIcon.setTag(imageUrl);
			mImageCache = mCouponAsyncTask.getImageCache();
			Drawable drawable_temp = null;
			if (mImageCache.containsKey(imageUrl)) {
				SoftReference<Drawable> temp = mImageCache.get(imageUrl);
				drawable_temp = temp.get();
			}
				
			if (drawable_temp == null) {
				currentTiem = System.currentTimeMillis();
				long updatetime = getUpdateTime();
				if (updatetime > 0 && currentTiem - updatetime < mUpdatedTime) {
					drawable_temp = mCouponAsyncTask.getDrawableFromSDcard(imageUrl);
				}
			}
					
			if (drawable_temp == null) {
				drawable_temp = getDrawable(mCouponAsyncTask, imageUrl, ivIcon);
				saveUpdateTime();
			}
			
			if (drawable_temp != null) {
				ivIcon.setImageDrawable(drawable_temp);
			} else {
				ivIcon.setImageResource(R.drawable.ola);
			}
		} else {
			tvTitle.setText(item.getTitle());
			String app_icon_url = item.getImage_small_url();
			Drawable drawable_temp = null;
			if (app_icon_url != null) {
				ivIcon.setTag(app_icon_url);
				mImageCache = mAppAsyncTask.getImageCache();
				Log.i(TAG, "mImageCache sise is " + mImageCache.size());
				Log.i(TAG, app_icon_url);
				if (mImageCache.containsKey(app_icon_url)) {
					SoftReference<Drawable> temp = mImageCache.get(app_icon_url);
					drawable_temp = temp.get();
				}
				
				if (drawable_temp == null) {
					String fileNa = app_icon_url.substring(app_icon_url.lastIndexOf("/") + 1,
							app_icon_url.length()).toLowerCase();
					drawable_temp = mCouponAsyncTask.getDrawableFromSDcard(fileNa);
				}
				
				if (drawable_temp == null) {
					drawable_temp = getDrawable(mAppAsyncTask, app_icon_url, ivIcon);
				}
			}
			
			if (drawable_temp != null) {
				ivIcon.setImageDrawable(drawable_temp);
			} else {
				ivIcon.setImageResource(R.drawable.ola);
			}
		}
		return convertView;
	}
	
	public class DrawerItemClickListener implements OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.v(TAG, "~~~~~~onItemClick position:" + position);
			if (mIsCoupon) {
				DrawEntity dobj = (DrawEntity) parent
						.getItemAtPosition(position);
				if (dobj.getUrl() != null) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Bundle bundle = new Bundle();
					bundle.putString("image_url", dobj.getUrl());
					intent.setClass(mContext,
							ShowCouponPicActivity.class);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				}
				mDetail = new couponDetail();
				mDetail.execute(dobj.getId());
			} else {
				Uri uri;
				DrawEntity drawEntity_temp = (DrawEntity) parent
						.getItemAtPosition(position);
				if (drawEntity_temp.getTitle() == null || drawEntity_temp.getTitle().equals("")) {
					return;
				}
				if (drawEntity_temp.getUrl() != null) {
					uri = Uri.parse(drawEntity_temp.getUrl());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			}
		}
	}

	private Drawable getDrawable(CustomeAsyncTask mytask, String imageUrl,
			final ImageView imageView) {
		Drawable drawable = mytask.loadDrawable(imageUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				if (imageDrawable != null)
					imageView.setImageDrawable(imageDrawable);
				else
					imageView.setImageResource(R.drawable.ola);
			}
		});
		return drawable;
	}
	
	/**
	 * save time
	 * 
	 * @return
	 */
	private boolean saveUpdateTime() {
		SharedPreferences dingdingPreferences = mContext.getSharedPreferences(
				"dingding", Context.MODE_PRIVATE);
		Editor editor = dingdingPreferences.edit();
		long time = System.currentTimeMillis();
		Log.i(TAG, "system time=" + time);
		editor.putLong("updatetime", time);
		if (editor.commit()) {
			return true;
		}
		return false;
	}

	private long getUpdateTime() {
		SharedPreferences dingdingPreferences = mContext.getSharedPreferences(
				"dingding", Context.MODE_PRIVATE);
		return dingdingPreferences.getLong("updatetime", 0);
	}
	
	/**
	 * 获取优惠�?	 * 
	 * @author p
	 * 
	 */
	public class couponDetail extends AsyncTask<String, integer, String> {
		private String mDingdingApikey = "ol20130711ol";
		HttpResponse response;
		private DefaultHttpClient httpClient;
		private String image_large_url;

		@Override
		protected String doInBackground(String... params) {
			return downloadImage_large(params[0]);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					JSONObject obj = new JSONObject(result);
					image_large_url = obj.getString("citypic");
					if (image_large_url != null && image_large_url != "") {
						Intent intent = new Intent();
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Bundle bundle = new Bundle();
						bundle.putString("image_url", image_large_url);
						intent.setClass(mContext,
								ShowCouponPicActivity.class);
						intent.putExtras(bundle);
						mContext.startActivity(intent);
					} else {
						CustomToast.makeToast(mContext, mContext.getResources().getString(R.string.newassistactivity_can_not_get_details_temp));
								//Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.i(TAG, "abtain the data failed");
				CustomToast.makeToast(mContext, "get data faile");//, Toast.LENGTH_SHORT).show();
			}
		}

		protected String downloadImage_large(String image_id) {
			String r = null;
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			String url = "http://coupon.api.ddmap.com/v2.0/couponDetailsService.do";
			params.add(new BasicNameValuePair("apikey", mDingdingApikey));
			params.add(new BasicNameValuePair("id", image_id));
			String param = URLEncodedUtils.format(params, "UTF-8");
			HttpGet getMethod = new HttpGet(url + "?" + param);
			try {
				response = httpClient.execute(getMethod);
				r = EntityUtils.toString(response.getEntity());
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return r;
		}
	}
}

