package com.viash.voice_assistant.activity.assistant;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.overlay.BusLineOverlay;
import com.amap.api.services.overlay.BusRouteOverlay;
import com.amap.api.services.overlay.DrivingRouteOverlay;
import com.amap.api.services.overlay.PoiOverlay;
import com.amap.api.services.overlay.WalkRouteOverlay;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.activity.NewAssistActivity;
import com.viash.voice_assistant.service.VoiceAssistantService;
import com.viash.voicelib.data.AppData.MapInfo;
import com.viash.voicelib.data.PreFormatData.BusInfoJsonData;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.CustomToast;

/**
 * 
 * 拆分主activity 相關功能
 * 
 * 地图及导航相关功能
 * 
 * @author fenglei
 *
 */
public class MapHelper implements OnBusLineSearchListener, OnPoiSearchListener, OnRouteSearchListener{

	private NewAssistActivity mainActivity;
	private static MapHelper _instance = null;

	private MapHelper(NewAssistActivity main) {
		this.mainActivity = main;
	}

	public static MapHelper init(NewAssistActivity main) {
		if(null == _instance)
			_instance = new MapHelper(main);
		return _instance;
	}

	public static MapHelper getInstantce() {
		if (null == _instance)
			throw new RuntimeException("please init MapHelper");

		return _instance;
	}
	
	
	/**
	 * Map
	 */
	public void showMap(MapInfo mapInfo) {
		mainActivity.showTopView(mainActivity.mMapView);
		mainActivity.aMap.clear();
		
		LatLng point = null;

		if (mapInfo.mLongitude != 0 || mapInfo.mLatitude != 0) {
			point = new LatLng(mapInfo.mLatitude, mapInfo.mLongitude);
		} else if (mapInfo.mAddress != null) {
			point = getPointFromAddress(mapInfo.mAddress, "");
		}

		if (point != null) {
			MarkerOptions marker = new MarkerOptions();
			marker.position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			mainActivity.aMap.addMarker(marker);
			changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
							point, 16, 0, 30)), null);
		}

		String poiId = "ola_pos";
		int index = 1;
		String poiSnippet = "";
		if (mapInfo.mPoiLatitude != null && mapInfo.mPoiLatitude.length > 0) {
			List<PoiItem> lstPoiItem = new ArrayList<PoiItem>();
			for (int i = 0; i < mapInfo.mPoiLatitude.length; i++) {
				LatLonPoint ptPoi = new LatLonPoint(mapInfo.mPoiLatitude[i],
						mapInfo.mPoiLongitude[i]);
				if (mapInfo.mPoiId != null)
					poiId = mapInfo.mPoiId[i];
				else
					poiId = "ola_pos" + (index++);
				if (mapInfo.mPoiSnippet != null)
					poiSnippet = mapInfo.mPoiSnippet[i];
				PoiItem poiItem = new PoiItem(poiId, ptPoi,
						mapInfo.mPoiTitle[i], poiSnippet);
				lstPoiItem.add(poiItem);
			}

			if (lstPoiItem.size() > 0) {
				point = new LatLng(lstPoiItem.get(0).getLatLonPoint().getLatitude(), lstPoiItem.get(0).getLatLonPoint().getLongitude());
				MarkerOptions marker = new MarkerOptions();
				marker.position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				mainActivity.aMap.addMarker(marker);
				CameraPosition position = new CameraPosition(point,16, 0, 30);
				changeCamera(CameraUpdateFactory.newCameraPosition(position), null);
			}
		}
	}
	
	public void showBusLineInfo(BusInfoJsonData data, int arg1) {
		mainActivity.showTopView(mainActivity.mMapView);
		mainActivity.aMap.clear();
		
		BusLineQuery busLineQuery = new BusLineQuery(data.mBusDescriptionData.bus_name,  SearchType.BY_LINE_NAME, data.mBusDescriptionData.city);
		busLineQuery.setPageSize(10);
		busLineQuery.setPageNumber(0);
		BusLineSearch busLineSearch = new BusLineSearch(mainActivity, busLineQuery);
		busLineSearch.searchBusLineAsyn();
		mainActivity.start_stop = data.mData.bus_start[arg1];
		mainActivity.end_stop = data.mData.bus_end[arg1];

		Message msg = NotifyUiHandler.getInstantce().obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_START);
		NotifyUiHandler.getInstantce().sendMessage(msg);
		busLineSearch.setOnBusLineSearchListener(this);
	}
	
	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if (rCode == 0) {
			List<BusLineItem> lineItems = result.getBusLines();
			if (lineItems != null && lineItems.size() > 0) {
				if (result.getQuery().getCategory() == SearchType.BY_LINE_NAME) {
					BusLineQuery busLineQuery = null;
					for( BusLineItem item : lineItems) {
						if (item.getOriginatingStation().equals(mainActivity.start_stop) 
								&& item.getTerminalStation().equals(mainActivity.end_stop)){
							String lineId = item.getBusLineId();
							busLineQuery = new BusLineQuery(lineId, SearchType.BY_LINE_ID,	item.getCityCode());
							BusLineSearch busLineSearch = new BusLineSearch(mainActivity, busLineQuery);
							busLineSearch.setOnBusLineSearchListener(this);
							busLineSearch.searchBusLineAsyn();
							break;
						}
					}
					if (null == busLineQuery) {
						Message msg = NotifyUiHandler.getInstantce().obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
						NotifyUiHandler.getInstantce().sendMessage(msg);
						CustomToast.makeToast(mainActivity, 
								mainActivity.getResources().getString(R.string.newassistactivity_can_not_find_bus_info));//,Toast.LENGTH_SHORT).show();
					}
				}
				else if (result.getQuery().getCategory() == SearchType.BY_LINE_ID) {
					Message msg = NotifyUiHandler.getInstantce().obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
					NotifyUiHandler.getInstantce().sendMessage(msg);
					lineItems = result.getBusLines();
					BusLineOverlay busLineOverlay = new BusLineOverlay(mainActivity,
							mainActivity.aMap, lineItems.get(0));
					busLineOverlay.removeFromMap();
					busLineOverlay.addToMap();
					busLineOverlay.zoomToSpan();

				}
			}
		}
		else {
			Message msg = NotifyUiHandler.getInstantce().obtainMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			NotifyUiHandler.getInstantce().sendMessage(msg);
			CustomToast.makeToast(mainActivity, 
					mainActivity.getResources().getString(R.string.newassistactivity_can_not_find_bus_info));//,Toast.LENGTH_SHORT).show();
		}
	}
	
	public LatLng getPointFromAddress(String mAddress, String city) {
		PoiSearch poiSearch;
		if (city == null) {
			city = "";
		}
		mainActivity.mPOIQuery = new PoiSearch.Query(mAddress, "", city);
		mainActivity.mPOIQuery.setPageSize(10);
		mainActivity.mPOIQuery.setPageNum(0);

		poiSearch = new PoiSearch(mainActivity, mainActivity.mPOIQuery);
		poiSearch.searchPOIAsyn();
		poiSearch.setOnPoiSearchListener(this);
		
		return null;
	}

	public void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint,
			final int mode) {
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
		mainActivity.mRouteSearch = new RouteSearch(mainActivity);
		mainActivity.mRouteSearch.setRouteSearchListener(this);
		NotifyUiHandler.getInstantce().sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_START);
		int localmode = mode;
		if (localmode < 10) {
			mainActivity.mBusRouteQuery = new BusRouteQuery(fromAndTo,
					RouteSearch.BusDefault, "", 0);
			mainActivity.mRouteSearch.calculateBusRouteAsyn(mainActivity.mBusRouteQuery);
		} else if (localmode < 20) {
			mainActivity.mDriveRouteQuery = new DriveRouteQuery(fromAndTo,
					RouteSearch.DrivingDefault, null, null, "");
			mainActivity.mRouteSearch.calculateDriveRouteAsyn(mainActivity.mDriveRouteQuery);
		} else {
			mainActivity.mWalkRouteQuery = new WalkRouteQuery(fromAndTo,
					RouteSearch.WalkDefault);
			mainActivity.mRouteSearch.calculateWalkRouteAsyn(mainActivity.mWalkRouteQuery);
		}
		mainActivity.showTopView(mainActivity.mMapView);
	}



	public void changeCamera(CameraUpdate newCameraPosition, CancelableCallback callback) {
		mainActivity.aMap.animateCamera(newCameraPosition, 1000, callback);
	}

	public void searchPos(MapInfo mapInfo) {
		if (mapInfo.mPoiSnippet != null && mapInfo.mPoiSnippet.length == 1) {
			MapThread thread = new MapThread();
			thread.setMapData(mapInfo);
			thread.start();
		}
	}

	class MapThread extends Thread {
		MapInfo mMapInfo = null;;
		String mAddress = null;
		boolean mIsNavigate = false;

		public void setMapData(MapInfo data) {
			mMapInfo = data;
			mAddress = mMapInfo.mPoiSnippet[0];
		}

		public void setNavigationAddress(String address) {
			mIsNavigate = true;
			mAddress = address;
		}

		@Override
		public void run() {
			Message msg = null;
			boolean found = false;
			GeocodeSearch geoCoder = new GeocodeSearch(mainActivity);
			int retry = 0;
			while (retry < 2) {
				try {
					List<GeocodeAddress> address = null;
					GeocodeQuery query = new GeocodeQuery(mAddress, "");
					address = geoCoder.getFromLocationName(query);
					
					if (address != null && address.size() > 0) {
						if (!mIsNavigate) {
							mMapInfo.mPoiLongitude[0] = address.get(0).getLatLonPoint().getLongitude();
							mMapInfo.mPoiLatitude[0] = address.get(0).getLatLonPoint().getLatitude();
							msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_SHOW_MAP);
							msg.obj = mMapInfo;
						} else {
							if (address.get(0).getLatLonPoint().getLatitude() != 0 &&
									address.get(0).getLatLonPoint().getLongitude() != 0) {
								LatLng point = new LatLng(address.get(0).getLatLonPoint().getLatitude(),
										address.get(0).getLatLonPoint().getLongitude());
								msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_NAVIGATE_TO, 0);
								msg.obj = point;
							}
							else {
								msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_NAVIGATE_TO, 1);
								msg.obj = mMapInfo.mPoiSnippet[0];
							}
						}

						found = true;
						break;
					}
				} catch (AMapException e) {
					e.printStackTrace();
				}
				retry++;
			}

			if (!found) {
				msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_SHOW_ERROR);
				msg.obj = mainActivity.getResources().getString(R.string.newassistactivity_can_not_find) + mAddress;
			}
			NotifyUiHandler.getInstantce().sendMessage(msg);
			super.run();
		}
	}

	public void showRoute(RouteResult routeResult) {
		if (routeResult != null) {
			if (routeResult instanceof BusRouteResult) {
				BusPath busPath = ((BusRouteResult) routeResult).getPaths().get(0);
				mainActivity.aMap.clear();
				BusRouteOverlay routeOverlay = new BusRouteOverlay(mainActivity, mainActivity.aMap,
						busPath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			}
			if (routeResult instanceof DriveRouteResult) {
				DrivePath drivePath = ((DriveRouteResult)routeResult).getPaths().get(0);
				mainActivity.aMap.clear();
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						mainActivity, mainActivity.aMap, drivePath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();
			}
			if (routeResult instanceof WalkRouteResult) {
				WalkPath walkPath = ((WalkRouteResult)routeResult).getPaths().get(0);
				mainActivity.aMap.clear();
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(mainActivity,
						mainActivity.aMap, walkPath, routeResult.getStartPos(),
						routeResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
			}
			mainActivity.mMapView.invalidate();
			mainActivity.showTopView(mainActivity.mMapView);
		}
	}
	

	public void startNavigate(double fromLatitude, double fromLongitude,
			double toLatitude, double toLongitude, int mode) {
		double latitude = toLatitude;
		double longitude = toLongitude;

		Intent i = new Intent("com.autonavi.xmgd.action.NAVIGATOR");
		i.setData(Uri.parse("GEONAVI:" + String.valueOf(longitude) + ","
				+ String.valueOf(latitude) + ","));

		try {
			mainActivity.startActivity(i);
		} catch (ActivityNotFoundException e) {
			i = new Intent(
					"android.intent.action.VIEW",
					android.net.Uri
							.parse("androidamap://navi?sourceApplication=S3&lat="
									+ latitude
									+ "&lon="
									+ longitude
									+ "&style=0&dev=0"));
			i.setPackage("com.autonavi.minimap");
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			try {
				mainActivity.startActivity(i);
			} catch (ActivityNotFoundException e1) {
				CustomToast.makeToast(mainActivity,
						mainActivity.getString(R.string.need_navi_software));//, Toast.LENGTH_LONG).show();
				if (fromLatitude == 0 || fromLongitude == 0) {
					if (VoiceAssistantService.mCurLocation != null) {
						fromLatitude = ((double)(VoiceAssistantService.mCurLocation.y))/1000000.0;
						fromLongitude = ((double)(VoiceAssistantService.mCurLocation.x))/1000000.0;
					}
				}

				if (fromLatitude != 0 && fromLongitude != 0) {
					LatLonPoint startPoint = new LatLonPoint(fromLatitude,
							fromLongitude);
					if  ( toLatitude != 0 && toLongitude != 0 ){
						LatLonPoint endPoint = new LatLonPoint(toLatitude, toLongitude);
						searchRouteResult(startPoint, endPoint, mode);
					}else {
						CustomToast.makeToast(mainActivity, mainActivity.getString(R.string.to_position_not_known));//, Toast.LENGTH_SHORT).show();
					}
				} else {
					CustomToast.makeToast(mainActivity, mainActivity.getString(R.string.position_not_known));
							//Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void startNavigate(String address) {
		if (address != null && address.length() > 0) {
			MapThread thread = new MapThread();
			thread.setNavigationAddress(address);
			thread.start();
		}
	}
	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPoiSearched(PoiResult result, int arg1) {
		if (result != null && result.getQuery() != null) {
			if (result.getQuery().equals(mainActivity.mPOIQuery)) {
				List<PoiItem> poiItems = result.getPois();

				if (poiItems != null && poiItems.size() > 0) {
					mainActivity.aMap.clear();
					mainActivity.mPoiOverLay = new PoiOverlay(mainActivity.aMap, poiItems);
					mainActivity.mPoiOverLay.removeFromMap();
					mainActivity.mPoiOverLay.addToMap();
					mainActivity.mPoiOverLay.zoomToSpan();
				}
			}
		}
		
	}

	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {
		RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				BusPath busPath = result.getPaths().get(0);
				mainActivity.aMap.clear();
				BusRouteOverlay routeOverlay = new BusRouteOverlay(mainActivity, mainActivity.aMap,
						busPath, result.getStartPos(),
						result.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();

				NotifyUiHandler.getInstantce().sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			} else {
				mainActivity.mDriveRouteQuery = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null,null,"");
				mainActivity.mRouteSearch.calculateDriveRouteAsyn(mainActivity.mDriveRouteQuery);
			}
		} else {
			mainActivity.mDriveRouteQuery = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null,null,"");
			mainActivity.mRouteSearch.calculateDriveRouteAsyn(mainActivity.mDriveRouteQuery);
		}
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		RouteSearch.FromAndTo fromAndTo = null;
		if (rCode == 0) {
			fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				DrivePath drivePath = result.getPaths().get(0);
				if (drivePath.getDistance() <= 500) {
					mainActivity.mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
					mainActivity.mRouteSearch.calculateWalkRouteAsyn(mainActivity.mWalkRouteQuery);
					return;
				}
				mainActivity.aMap.clear();
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						mainActivity, mainActivity.aMap, drivePath, result.getStartPos(),
						result.getTargetPos());
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();

				NotifyUiHandler.getInstantce().sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
			} else {
				mainActivity.mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
				mainActivity.mRouteSearch.calculateWalkRouteAsyn(mainActivity.mWalkRouteQuery);
			}
		} else {
			fromAndTo = new RouteSearch.FromAndTo(result.getStartPos(), result.getTargetPos());
			mainActivity.mWalkRouteQuery = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
			mainActivity.mRouteSearch.calculateWalkRouteAsyn(mainActivity.mWalkRouteQuery);
		}
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				WalkPath walkPath = result.getPaths().get(0);
				mainActivity.aMap.clear();
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(mainActivity,
						mainActivity.aMap, walkPath, result.getStartPos(),
						result.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
			} else {
				Message msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_ROUTE_SEARCH_RESULT);
				msg.obj = result;
				NotifyUiHandler.getInstantce().sendMessage(msg);
			}
		} else {
			Message msg = NotifyUiHandler.getInstantce().obtainMessage(MsgConst.MSG_ROUTE_SEARCH_RESULT);
			msg.obj = result;
			NotifyUiHandler.getInstantce().sendMessage(msg);
		}
		NotifyUiHandler.getInstantce().sendEmptyMessage(VoiceAssistantService.MSG_UI_SEARCHING_FOUND);
		
	}
}
