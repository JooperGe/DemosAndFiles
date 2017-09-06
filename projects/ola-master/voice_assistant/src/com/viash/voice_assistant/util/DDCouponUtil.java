package com.viash.voice_assistant.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import com.viash.voice_assistant.common.WifiLocation;
import com.viash.voice_assistant.entity.DrawEntity;
import com.viash.voicelib.utils.HttpUtil;

public class DDCouponUtil{

	private static final String TAG = "DDCouponUtil";
	private static DDCouponUtil mInstance = null;
	
	private String mDingdingApikey = "ol20130711ol";
	HttpResponse response;
	private DefaultHttpClient httpClient;
	private Context mContext = null;
	
	String promo_url = "http://www.ola.com.cn/clientinfo/promo.txt";

	private List<DrawEntity> drawerItemList_coupons = null; 
	private List<DrawEntity> drawerItemList_apps = null; 
	
	private JSONArray couponJsonArray = new JSONArray();
	private JSONArray appJsonArray = new JSONArray();
	
	private boolean mShouldNewIconShown = false;
	
	private IResultListener mListener = null;

	private DDCouponUtil(Context context) {
		mContext = context;
		drawerItemList_coupons = new ArrayList<DrawEntity>();
		drawerItemList_apps = new ArrayList<DrawEntity>();
		updatePromoData();
	}
	
	public static DDCouponUtil getInstance (Context context) {
		if (mInstance == null) {
			mInstance = new DDCouponUtil(context);
		}
		return mInstance;
	}
	
	public void setListener(IResultListener listener) {
		mListener = listener;
	}

	protected class GetCoupon extends AsyncTask<Void, Intent, String> {

		@Override
		protected String doInBackground(Void... params) {
			return downloadData();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				if (!result.contains("exception")) {
					String image_small_url = "http://img3.ddmapimg.com/city/images/small/";
					try {
						saveDDCouponData(result);
						JSONArray jArray = new JSONObject(result)
								.getJSONArray("resultList");
						drawerItemList_coupons.clear();
						for (int i = 0; i < jArray.length(); i++) {
							JSONObject ob = jArray.getJSONObject(i);
							drawerItemList_coupons.add(new DrawEntity(ob
									.getString("title"), image_small_url
									+ ob.getString("image_small"), null, null, ob
									.getString("id")));
						}
						if (mListener != null) {
							mListener.onReceiveNewListData();
						}
						//updateDrawerLayout();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					// TODO: do something for exception
	
					// if (couponJsonArray.length() > 0) {
					// for (int i = 0; i < couponJsonArray.length(); i++) {
					// JSONObject ob;
					// try {
					// ob = couponJsonArray.getJSONObject(i);
					// drawerItemList.add(new DrawEntity(ob
					// .getString("coupon_title"), ob
					// .getString("coupon_icon"), null, ob
					// .getString("coupon_image"), null));
					// } catch (JSONException e) {
					// e.printStackTrace();
					// updateDrawerLayout();
					// }
					// }
					// updateDrawerLayout();
					// }
				}
			} else {
				//drawerItemList_apps.clear();
				//if (0 == drawerItemList_apps.size()) {
					/*if (couponJsonArray.length() > 0) {
						for (int i = 0; i < couponJsonArray.length(); i++) {
							JSONObject ob;
							try {
								ob = couponJsonArray.getJSONObject(i);
								drawerItemList_apps.add(new DrawEntity(ob
										.getString("coupon_title"), ob
										.getString("coupon_icon"), null, ob
										.getString("coupon_image"), null));
							} catch (JSONException e) {
								e.printStackTrace();
								updateDrawerLayout();
							}
						}
					}*/
				//}
				//updateDrawerLayout();
			}
		}
	
		protected String downloadData() {
			String r = null;
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			String url = "http://coupon.api.ddmap.com/v2.0/nearbyCouponListService.do";
			Point point = WifiLocation.getInstance(mContext).getLocation();
			if (point == null) {
				return "";
			}
			String mLong = (point.x) / 1E6 + "";
			String mLat = (point.y) / 1E6 + "";
			params.add(new BasicNameValuePair("apikey", mDingdingApikey));
			params.add(new BasicNameValuePair("gps_longitude", mLong));
			params.add(new BasicNameValuePair("gps_latitude", mLat));
			String param = URLEncodedUtils.format(params, "UTF-8");
			HttpGet getMethod = new HttpGet(url + "?" + param);
			try {
				response = httpClient.execute(getMethod);
				r = EntityUtils.toString(response.getEntity(), "UTF-8");
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return r;
		}
	}
	
	private void saveDDCouponData(String str) {
		SharedPreferences dingdingPreferences = mContext.getSharedPreferences(
				"dingding", Context.MODE_PRIVATE);
		Editor editor = dingdingPreferences.edit();
		editor.putString("dingding_coupon", str);
		if (editor.commit()) {
			Log.i(TAG, "Save --OK" + str);
			return;
		}
		Log.i(TAG, "Save --Error");		
	}
	
	public String getDDCouponData() {
		SharedPreferences dingdingPreferences = mContext.getSharedPreferences(
				"dingding", Context.MODE_PRIVATE);
		return dingdingPreferences.getString("dingding_coupon", null);
	}
	
	public List<DrawEntity> getCouponList(){
		if (drawerItemList_coupons.size() == 0) {
			String result = getDDCouponData();
			if (null != result) {
				try {
					JSONArray jArray = new JSONObject(result)
							.getJSONArray("resultList");
					drawerItemList_coupons.clear();
					for (int i = 0; i < jArray.length(); i++) {
						JSONObject ob = jArray.getJSONObject(i);
						drawerItemList_coupons.add(new DrawEntity(ob
								.getString("title"), ""
								+ ob.getString("image_small"), null, null, ob
								.getString("id")));
					}
					// updateDrawerLayout();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			GetCoupon mGetCoupon = new GetCoupon();
			mGetCoupon.execute((Void[]) null);
		}
		return drawerItemList_coupons;
	}
	
	public List<DrawEntity> getAppList(){
		if (drawerItemList_apps.size() == 0) {
			if (appJsonArray.length() > 0) {
				for (int i = 0; i < appJsonArray.length(); i++) {
					JSONObject ob;
					try {
						ob = appJsonArray.getJSONObject(i);
						drawerItemList_apps.add(new DrawEntity(ob
								.getString("promo_title"), ob
								.getString("promo_icon"), null, ob
								.getString("promo_url"), null));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				//TODO there is no APPS.
				for (int i = 0; i < 8; i ++){
					drawerItemList_apps.add(new DrawEntity("虚位以待", null, null, null, null));
				}
			}
		}
		return drawerItemList_apps;
	}
	
	/**
	 * 
	 * @author p
	 * 
	 */
	protected class GetPromo extends AsyncTask<String, Integer, String> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			int server_id = 0;
			if (result != null) {
				try {
					int indexStart = result.indexOf('{');
					int indexEnd = result.lastIndexOf('}');
					if (indexStart >= 0 && indexEnd >= 0
							&& indexEnd > indexStart) {
						result = result.substring(indexStart, indexEnd + 1);
						JSONObject obj = new JSONObject(result);
						server_id = obj.getInt("id");
						couponJsonArray = obj.getJSONArray("coupons");
						appJsonArray = obj.getJSONArray("apps");
						drawerItemList_apps.clear();
						if (appJsonArray.length() > 0) {
							for (int i = 0; i < appJsonArray.length(); i++) {
								JSONObject ob;
								try {
									ob = appJsonArray.getJSONObject(i);
									drawerItemList_apps.add(new DrawEntity(ob
											.getString("promo_title"), ob
											.getString("promo_icon"), null, ob
											.getString("promo_url"), null));
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				server_id = 0;
				mShouldNewIconShown = true;
				//image_drawer_icon_new.setVisibility(View.VISIBLE);
			}
			if (server_id > 0) {
				SharedPreferences mPreferences = mContext.getSharedPreferences("promoinfo", Context.MODE_PRIVATE);
				int client_id = mPreferences.getInt("id", 0);
				if (server_id > client_id) {
					mShouldNewIconShown = true;
					//image_drawer_icon_new.setVisibility(View.VISIBLE);
					Editor editor = mPreferences.edit();
					editor.putInt("id", server_id);
					editor.commit();
				} else {
					mShouldNewIconShown = false;
					//image_drawer_icon_new.setVisibility(View.INVISIBLE);
				}
			}
			if (mListener != null) {
				mListener.onResultContainsNewPromo(mShouldNewIconShown);
				mListener.onReceiveNewListData();
			}

		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			return HttpUtil.sendGetCommand(mContext, url, "utf8");
		}
	}
	
	private void updatePromoData() {
/*		SharedPreferences mPreferences = mContext.getSharedPreferences("promoinfo",Context.MODE_PRIVATE);
		int promo_id = mPreferences.getInt("id", 0);
		if (promo_id <= 0) {
			mShouldNewIconShown = true;
			//image_drawer_icon_new.setVisibility(View.VISIBLE);
		} else {
			mShouldNewIconShown = false;
			//image_drawer_icon_new.setVisibility(View.INVISIBLE);
		}
		*/
		GetPromo mGetPromo = new GetPromo();
		mGetPromo.execute(promo_url);
	}
	
	public interface IResultListener {
		void onResultContainsNewPromo(boolean result);
		void onReceiveNewListData();
	}
}