package com.viash.voice_assistant.widget;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.viash.voice_assistant.R;
import com.viash.voice_assistant.widget.selection.AppDownloadView;
import com.viash.voice_assistant.widget.selection.BluetoothView;
import com.viash.voice_assistant.widget.selection.CallNumView;
import com.viash.voice_assistant.widget.selection.CallPeopleView;
import com.viash.voice_assistant.widget.selection.CookingView;
import com.viash.voice_assistant.widget.selection.DefaultView;
import com.viash.voice_assistant.widget.selection.ExchangeRateView;
import com.viash.voice_assistant.widget.selection.NewsView;
import com.viash.voice_assistant.widget.selection.PeopleAddressView;
import com.viash.voice_assistant.widget.selection.PersionEncyclopedia_SelectionView;
import com.viash.voice_assistant.widget.selection.SMSNumberView;
import com.viash.voice_assistant.widget.selection.SelectionBaseView;
import com.viash.voice_assistant.widget.selection.TVView;
import com.viash.voice_assistant.widget.selection.VideoView;
import com.viash.voicelib.data.BaseData;
import com.viash.voicelib.data.OptionData;
import com.viash.voicelib.data.PreFormatData;
public class WidgetViewFactory {
	
	public static View getWidgetView(Context context, BaseData originalData, boolean operationable, Handler handler, boolean isFullScreen, int dataType) {
		if (dataType == SelectionBaseView.PERFORMATED_DATA_TYPE)  {
			try {
				PreFormatData data = (PreFormatData)originalData;
				return getWidgetView(context, data, operationable, handler, isFullScreen);
			}catch (Exception e) {
				e.getStackTrace();
				return null;
			}
		}else if (dataType == SelectionBaseView.OPTION_DATA_TYPE)  {
			try {
				OptionData data = (OptionData)originalData;
				return getWidgetView(context, data, operationable, handler, isFullScreen);
			}catch (Exception e) {
				e.getStackTrace();
				return null;
			}
		}
		return null;
	}

	
	public static View getWidgetView(Context context, PreFormatData originalData, boolean operationable, Handler handler, boolean isFullScreen) {
		View returnView = null;
		RelativeLayout.LayoutParams layoutwhole_View = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		layoutwhole_View.leftMargin = (int) context.getResources().getDimension(
				R.dimen.main_listview_divider_height);
		layoutwhole_View.rightMargin = (int) context.getResources().getDimension(
				R.dimen.main_listview_divider_height);
		
		if (originalData == null) {
			return null;
		}
		switch (originalData.getmDataType()) {
		case PreFormatData.JSON_TV:
			TVView tvView = new TVView(context, originalData, operationable,
					handler, isFullScreen);
			returnView = tvView;
			break;
		case PreFormatData.JSON_CALL_DELAY:
			CallDelayView callDelayView = new CallDelayView(context, originalData, operationable, handler, isFullScreen);
			returnView = callDelayView;
			break;
		case PreFormatData.JSON_EXCHANGE_RATE:
			ExchangeRateView exchangeRageView = new ExchangeRateView(context,
					originalData, operationable, handler, isFullScreen);
			returnView = exchangeRageView;
			break;
		case PreFormatData.JSON_LYRICS_INFO:
			LyricsView lyricsView = new LyricsView(context, originalData,
					operationable, handler, isFullScreen);
			returnView = lyricsView;
			break;
		case PreFormatData.JSON_MEMO:
			MemoView memoView = new MemoView(context, originalData,
					operationable, handler, isFullScreen);
			returnView = memoView;
			break;
		case PreFormatData.JSON_BUS_INFO:
			BusInfoView busInfoView = new BusInfoView(context, originalData, operationable, handler, isFullScreen);
		    returnView = busInfoView;
		    break;
		case PreFormatData.JSON_ROUTE:
			RouteView routeView = new RouteView(context, originalData, operationable, handler, isFullScreen);
		    returnView = routeView;
		    break;
		case PreFormatData.JSON_CALENDAR:
			CalendarView calendarView = new CalendarView(context, originalData, operationable, handler, isFullScreen);
		    returnView = calendarView;	    
			break;
		case PreFormatData.JSON_CONTACT:
			ContactDetailView contactDetailView = new ContactDetailView(context, 
					originalData, operationable, handler,isFullScreen);
			returnView = contactDetailView;	    
			break;
		case PreFormatData.JSON_BUS:
			BusView busView = new BusView(context, originalData, operationable, handler, isFullScreen);
		    returnView = busView;	        
			break;
		case PreFormatData.JSON_POI:
			PoiView poiView = new PoiView(context, originalData, operationable, handler, isFullScreen);
			returnView = poiView;
			break;	
		case PreFormatData.JSON_STOCK:
			StockView stockView = new StockView(context, originalData, operationable, handler, isFullScreen);
			returnView = stockView;
			break;
		case PreFormatData.JSON_HOTEL:
			HotelView hotelView = new HotelView(context,originalData, operationable, handler, isFullScreen);
			returnView = hotelView;
			break;
		case PreFormatData.JSON_PLANE:
			PlaneView planeView = new PlaneView(context,originalData, operationable, handler, isFullScreen);
			returnView = planeView;
			break;
		case PreFormatData.JSON_ALARM:
			AlarmView alarmView = new AlarmView(context,originalData, operationable, handler, isFullScreen);
			returnView = alarmView; 
			break;
		case PreFormatData.JSON_CALL:
			CallRecordView callRecordView = new CallRecordView(context, originalData, operationable, handler, isFullScreen);
			returnView = callRecordView;
			break;
		case PreFormatData.JSON_POI_REFERENCE:
			POIReferenceView poiRefView = new POIReferenceView(context, originalData, operationable, handler, isFullScreen);
			returnView = poiRefView;
			break;
		case PreFormatData.JSON_JOKE:
			JokeView jokeView = new JokeView(context, originalData, operationable, handler, isFullScreen);
			returnView = jokeView;
			break;
		case PreFormatData.JSON_COOKING:
			CookingDetailView cookView = new CookingDetailView(context, originalData, operationable, handler, isFullScreen);
			returnView = cookView;
			break;
		case PreFormatData.JSON_SMS:
			SMSView smsView = new SMSView(context, originalData, operationable, handler, isFullScreen);
			returnView = smsView;
			break;
		case PreFormatData.JSON_PERSON:	
		case PreFormatData.JSON_BAIKE_OTHER:
			BaikeView baikeView = new BaikeView(context, originalData, operationable, handler, isFullScreen);
			returnView = baikeView;
			break;
		case PreFormatData.JSON_TRAFFIC:
			TrafficView trafficView = new TrafficView(context, originalData, operationable, handler, isFullScreen);
			returnView = trafficView;
			break;
		case PreFormatData.JSON_NEWS:
			com.viash.voice_assistant.widget.NewsView newsView = new com.viash.voice_assistant.widget.NewsView(
					context, originalData, operationable, handler, isFullScreen);
			returnView = newsView;
			break;
		case PreFormatData.JSON_WEIBO:
			SinaWeiBoView sinaWeiBoView = new SinaWeiBoView(context, originalData, operationable, handler, isFullScreen);
			returnView = sinaWeiBoView;
			break;
		case PreFormatData.JSON_WEATHER:
			WeatherNewView weatherNewView = new WeatherNewView(context, originalData, operationable, handler, isFullScreen);
			returnView = weatherNewView;
			break;			
		}
		if (isFullScreen) {
			returnView.setLayoutParams(layoutwhole_View);
		}
		
		return returnView;
	}

	public static View getWidgetView(Context context, OptionData originalData, boolean operationable, Handler handler, boolean isFullScreen) {
		View returnView = null;
		RelativeLayout.LayoutParams layoutwhole_View = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		layoutwhole_View.leftMargin = (int) context.getResources().getDimension(
				R.dimen.main_listview_divider_height);
		layoutwhole_View.rightMargin = (int) context.getResources().getDimension(
				R.dimen.main_listview_divider_height);
		
		
		if (originalData == null) {
			return null;
		}
		switch (originalData.getOptionId()) {
		case OptionData.OPTION_PERSON_ENCYCLOPEDIA:
			PersionEncyclopedia_SelectionView persionEncyclopediaView = new PersionEncyclopedia_SelectionView(
					context, originalData, operationable, handler, isFullScreen);
			returnView = persionEncyclopediaView;
	    	break;
		case OptionData.OPTION_PEOPLE_ADDRESS:
		case OptionData.OPTION_POI:
	    	PeopleAddressView peopleAddressView = new PeopleAddressView(context,originalData,operationable,handler,isFullScreen);
	    	returnView = peopleAddressView;
	    	break;
		case OptionData.OPTION_MOBILE_SETTING_BLUETOOTH:
			BluetoothView bluetoothView = new BluetoothView(context,originalData,operationable,handler,isFullScreen);
        	returnView = bluetoothView;
        	break;
		case OptionData.OPTION_NEWS_NAME:
			NewsView newsView = new NewsView(context,originalData,operationable,handler,isFullScreen);
        	returnView = newsView;
        	break;
		case OptionData.OPTION_SMS_NUMBER:
			SMSNumberView smsNumberView = new SMSNumberView(context,originalData,operationable,handler,isFullScreen);
			returnView = smsNumberView;
			break;	
		case OptionData.OPTION_CALL_NUMBER:
			CallNumView callNumView = new CallNumView(context,originalData,operationable,handler,isFullScreen);
			returnView = callNumView;
			break;
		case OptionData.OPTION_APP_NAME:
			AppDownloadView appDownloadView = new AppDownloadView(context,originalData,operationable,handler,isFullScreen);
			returnView = appDownloadView;
			break;	
		case OptionData.OPTION_COOKING:
			CookingView cookingView = new CookingView(context,originalData,operationable,handler,isFullScreen);
			returnView = cookingView;
			break;
		case OptionData.OPTION_POEM_TITLE:
			com.viash.voice_assistant.widget.selection.PoemView poemView = new com.viash.voice_assistant.widget.selection.PoemView(
					context,originalData,operationable,handler,isFullScreen);
			returnView = poemView;
			break;
		case OptionData.OPTION_CALL_PEOPLE:
		case OptionData.OPTION_SMS_PEOPLE:
		case OptionData.OPTION_CONTACT_PEOPLE:
			CallPeopleView callPeopleView = new CallPeopleView(context,originalData,operationable,handler,isFullScreen);
			returnView = callPeopleView;
			break;
		case OptionData.OPTION_MUSIC_ALBUM:
			com.viash.voice_assistant.widget.selection.MusicAlbumView musicAlbumView = new com.viash.voice_assistant.widget.selection.MusicAlbumView(
					context,originalData,operationable,handler,isFullScreen);
			returnView = musicAlbumView;
			break;		
		case OptionData.OPTION_VIDEO:
			VideoView videoView = new VideoView(context, originalData, operationable, handler, isFullScreen);
			returnView = videoView;
			break;
		default:
			DefaultView defaultView = new DefaultView(context,originalData,operationable,handler,isFullScreen);
			returnView = defaultView;
			break;
		}
		
		if (isFullScreen) {
			returnView.setLayoutParams(layoutwhole_View);
		}
		return returnView;
	}
}
