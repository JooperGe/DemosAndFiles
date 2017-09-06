package com.viash.voicelib.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.CallUtil.SmsData;
import com.viash.voicelib.utils.JsonUtil;

public class PreFormatData extends BaseData {
	public static final int TYPE_HTML = 0;
	public static final int JSON_ALARM = 1;
	public static final int JSON_CALENDAR = 2;
	public static final int JSON_SMS = 3;
	public static final int JSON_CALL = 4;
	public static final int JSON_MEMO = 5;
	public static final int JSON_BLUETOOTH = 6;
	public static final int JSON_WEATHER = 7;
	public static final int JSON_POEM = 8;
	public static final int JSON_WEIBO = 9;
	public static final int JSON_NEWS = 10;
	public static final int JSON_STOCK = 11;
	public static final int JSON_CONTACT = 12;
	public static final int JSON_TRAIN = 13;
	public static final int JSON_PLANE = 14;
	public static final int JSON_WIKIPEDIA = 15;
	public static final int JSON_POI = 16;
	public static final int JSON_POEM_TITLE = 17;
	public static final int JSON_ROUTE = 18;
	public static final int JSON_ARTIST_ALBUM = 19;
	public static final int JSON_MUSIC_ALBUM = 20;
	public static final int JSON_SHOPPING_ITEM = 21;
	public static final int JSON_HOTEL = 22;
	public static final int JSON_TRAFFIC = 23;
	public static final int JSON_BUS = 24;
	public static final int JSON_PERSON = 25;
	public static final int JSON_BAIKE_OTHER=26;
	//Add by Loneway for Game;
	public static final int JSON_GAME = 27;
	public static final int JSON_TV = 28; //for TV program.
	public static final int JSON_BUS_INFO = 29;
    public static final int JSON_CALL_DELAY = 30;//added by LeoLi for dialing delay
    public static final int JSON_EXCHANGE_RATE = 31;//added by Loneway for Exchange rate
    public static final int JSON_LYRICS_INFO = 32;//added by Loneway for music lyrics
    public static final int JSON_POI_REFERENCE = 34;
    public static final int JSON_JOKE = 35;
    public static final int JSON_COOKING = 36;
    
    
	// protected List<ItemData> mLstData = null;
	protected int mDataType;
	protected JsonData mJsonData = null;
	protected DescriptionData mDescriptionData = null;
	private boolean isConfirmedData = false;

	public boolean isConfirmedData() {
		return isConfirmedData;
	}

	public void setConfirmedData(boolean isConfirmedData) {
		this.isConfirmedData = isConfirmedData;
	}

	public DescriptionData getDescriptionData() {
		return mDescriptionData;
	}

	public PreFormatData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	
	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;
		String type = obj.optString("Type");
		if (type.equalsIgnoreCase("html")) {
			String str = obj.optString("Content", null);
			if (str != null) {
				mDisplayStr = str;
				mParseResult = true;
			}
		}
		//JsonData jsonDescriptionData = null;
		if (type.equalsIgnoreCase("json")) {
			JSONObject objAppendix = obj.optJSONObject("json_data");
			if (objAppendix != null) {
				String dataType = JsonUtil.optString(objAppendix, "type", "");
				if (dataType.equalsIgnoreCase("alarm")) {
					mDataType = JSON_ALARM;
					mJsonData = new AlarmJsonData();
				} else if (dataType.equalsIgnoreCase("calendar")) {
					mDataType = JSON_CALENDAR;
					mJsonData = new CalendarJsonData();
				} else if (dataType.equalsIgnoreCase("sms")) {
					mDataType = JSON_SMS;
					mJsonData = new SMSJsonData();
					//check sms size. change the selectionable in bottom of this method. 					
				} else if (dataType.equalsIgnoreCase("call")) {
					mDataType = JSON_CALL;
					mJsonData = new CallJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("memo")) {
					mDataType = JSON_MEMO;
					mJsonData = new MemoJsonData();
				} else if (dataType.equalsIgnoreCase("bluetooth")) {
					mDataType = JSON_BLUETOOTH;
					mJsonData = new BluetoothJsonData();
				} else if (dataType.equalsIgnoreCase("weather")) {
					mDataType = JSON_WEATHER;
					mJsonData = new WeatherJsonData();
				} else if (dataType.equalsIgnoreCase("poem")) {
					mDataType = JSON_POEM;
					mJsonData = new PoemJsonData();
				} else if (dataType.equalsIgnoreCase("weibo")) {
					mDataType = JSON_WEIBO;
					mJsonData = new WeiboJsonData();
				} else if (dataType.equalsIgnoreCase("news")) {
					mDataType = JSON_NEWS;
					mJsonData = new NewsJsonData();
				} else if (dataType.equalsIgnoreCase("stock")) {
					mDataType = JSON_STOCK;
					mJsonData = new StockJsonData();
					//check stock size. change the selectionable in bottom of this method. 
				} else if (dataType.equalsIgnoreCase("contact")) {
					mDataType = JSON_CONTACT;
					mJsonData = new ContactJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("train")) {
					mDataType = JSON_TRAIN;
					mJsonData = new TrainJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("plane")) {
					mDataType = JSON_PLANE;
					mJsonData = new PlaneJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("wikipedia")) {
					mDataType = JSON_WIKIPEDIA;
					mJsonData = new WikipediaJsonData();
				} else if (dataType.equalsIgnoreCase("poi")) {
					mDataType = JSON_POI;
					mJsonData = new PoiJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("route")) {
					mDataType = JSON_ROUTE;
					mJsonData = new RouteJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("poem_title")) {
					mDataType = JSON_POEM_TITLE;
					mJsonData = new PoemtitleJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("artist_album")) {
					mDataType = JSON_ARTIST_ALBUM;
					mJsonData = new ArtistAlbumJsonData();
				} else if (dataType.equalsIgnoreCase("music_album")) {
					mDataType = JSON_MUSIC_ALBUM;
					mJsonData = new MusicAlbumJsonData();
				} else if (dataType.equalsIgnoreCase("shopping_item")) {
					mDataType = JSON_SHOPPING_ITEM;
					mJsonData = new ShoppingItemJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("hotel")) {
					mDataType = JSON_HOTEL;
					mJsonData = new HotelJsonData();
					setIsSelectionData(true);
				} else if (dataType.equalsIgnoreCase("traffic")) {
					mDataType = JSON_TRAFFIC;
					mJsonData = new TrafficJsonData();
				} else if (dataType.equalsIgnoreCase("bus")) {
					mDataType = JSON_BUS;
					mJsonData = new BusJsonData();
				} else if (dataType.equals("person")) {
					mDataType = JSON_PERSON;
					mJsonData = new PersonJsonData();
				}else if (dataType.equals("encyclopedia")) {
					mDataType=JSON_BAIKE_OTHER;
					mJsonData=new OtherBaikeJsonData();
				}else if (dataType.equals("game")) {
					mDataType=JSON_GAME;
					mJsonData=new GameJsonData();
				}else if (dataType.equals("tv_program")) {
					mDataType=JSON_TV;
					mJsonData=new TVJsonData();
				}else if (dataType.equalsIgnoreCase("bus_info")) {
					mDataType = JSON_BUS_INFO;
					mJsonData = new BusInfoJsonData();
				}
                else if (dataType.equalsIgnoreCase("call_info")) {
					mDataType = JSON_CALL_DELAY;
					mJsonData = new CallDelayJsonData();
					setIsSelectionData(false);
				}
                else if (dataType.equalsIgnoreCase("exchange_rate")) {
					mDataType = JSON_EXCHANGE_RATE;
					mJsonData = new ExchangeRateJsonData();
				}
                else if (dataType.equalsIgnoreCase("lyrics")) {
					mDataType = JSON_LYRICS_INFO;
					mJsonData = new LyricsJsonData();
				}
                else if (dataType.equalsIgnoreCase("poi_reference")) {
					mDataType = JSON_POI_REFERENCE;
					mJsonData = new PoiReferenceJsonData();
					setIsSelectionData(true);
				}
                else if (dataType.equalsIgnoreCase("joke")) {
					mDataType = JSON_JOKE;
					mJsonData = new JokeJsonData();
				}
                else if (dataType.equalsIgnoreCase("cooking")) {
					mDataType = JSON_COOKING;
					mJsonData = new CookingJsonData();
				}
				if (mJsonData != null) {
					try {
						JSONArray objData = objAppendix.getJSONArray("data_obj");
						if (objData!= null) {
							mParseResult = mJsonData.parse(context, objData);
						}
						
						JSONObject objDescription = objAppendix.optJSONObject("description_obj");
						if (objDescription != null){
							mJsonData.parseDescription(context,objDescription);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (mJsonData instanceof StockJsonData) {
					if (((StockJsonData)mJsonData).mStockData.size() > 1) {
						setIsSelectionData(true);
					}
				}
				else if (mJsonData instanceof SMSJsonData) {
					if (((SMSJsonData)mJsonData).mLstSms.size() > 1) {
						setIsSelectionData(true);
					}
				}
			}
		}

		return mParseResult;
	}

	public int getmDataType() {
		return mDataType;
	}

	public JsonData getJsonData() {
		return mJsonData;
	}

	public class JsonData {
		
		public boolean parse(Context context, JSONArray obj) {
			return false;
		}

		public boolean parseDescription(Context context, JSONObject obj) {
			if (mDescriptionData == null) {
				mDescriptionData = new DescriptionData();
			}
			try {
				mDescriptionData.filter = JsonUtil.optInt(obj, "filter", 0);
				mDescriptionData.filters = JsonUtil.optStringArray(obj, "filters");
				mDescriptionData.default_input = JsonUtil.optString(obj, "default_input", null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public class AlarmJsonData extends JsonData {
		public List<AlarmData> mAlarmData = new ArrayList<AlarmData>();

		public class AlarmData {
			public int id;
			public int time;
			public String repeat;
			public String title;
			public String vibrate;
			public int enabled;
			public int selectable;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				AlarmData data = new AlarmData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.id = JsonUtil.optInt(objdata, "id", 0);
					data.time = JsonUtil.optInt(objdata, "time", 0);
					data.repeat = JsonUtil.optString(objdata, "repeat", null);
					data.title = JsonUtil.optString(objdata, "title", null);
					data.vibrate = JsonUtil.optString(objdata, "vibrate", null);
					data.enabled = JsonUtil.optInt(objdata, "enabled", 0);
					data.selectable = JsonUtil.optInt(objdata, "selectable", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mAlarmData.add(data);
			}
			return true;
		}
	}

	public class CalendarJsonData extends JsonData {
		public List<CalendarData> mCalendarData = new ArrayList<CalendarData>();

		public class CalendarData {
			public long id;
			public String start_time;
			public String end_time;
			public String title;
			public String location;
			public String description;
			public List<String> attendees = new ArrayList<String>();
			public String all_day;
			public String has_alarm;
			public String advance_time;
			public String repeat_rule;
			public int selectable;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				CalendarData data = new CalendarData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.id = JsonUtil.optLong(objdata, "id", 0);
					data.start_time = JsonUtil.optString(objdata, "start_time",
							null);
					data.end_time = JsonUtil.optString(objdata, "end_time",
							null);
					data.title = JsonUtil.optString(objdata, "title", null);
					data.location = JsonUtil.optString(objdata, "location",
							null);
					data.description = JsonUtil.optString(objdata,
							"description", null);
					JSONArray attendeesArray = objdata
							.optJSONArray("attendees");
					if (attendeesArray != null) {
						for (int j = 0; j < attendeesArray.length(); j++) {
							data.attendees.add(attendeesArray.optString(j, ""));
						}
					}
					data.all_day = JsonUtil.optString(objdata, "all_day", null);
					data.has_alarm = JsonUtil.optString(objdata, "has_alarm",
							null);
					data.advance_time = JsonUtil.optString(objdata,
							"advance_time", null);
					data.repeat_rule = JsonUtil.optString(objdata,
							"repeat_rule", null);
					data.selectable = JsonUtil.optInt(objdata, "selectable", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mCalendarData.add(data);
			}
			return true;
		}
	}

	public class SMSJsonData extends JsonData {
		public List<SmsData> mLstSms = null;

		@Override
		public boolean parse(Context context, JSONArray obj) {
			boolean ret = false;
			if (obj.length() > 0) {
				// long[] ids = new long[obj.length()];
				mLstSms = new ArrayList<CallUtil.SmsData>();
				for (int i = 0; i < obj.length(); i++) {
					JSONObject objData = obj.optJSONObject(i);
					if (objData != null) {
						SmsData smsData = new SmsData();
						smsData.setmId(JsonUtil.optLong(objData, "id", 0));
						smsData.setmContactName(JsonUtil.optString(objData,
								"contact_id", ""));
						smsData.setmContactPhone(JsonUtil.optString(objData,
								"contact_number", ""));
						smsData.setmContent(JsonUtil.optString(objData,
								"content", ""));
						smsData.setmTime(JsonUtil.optLong(objData, "time", 0));
						smsData.setmType(JsonUtil
								.optInt(objData, "sms_type", 1));
						mLstSms.add(smsData);
					}
				}
				ret = true;
			}
			return ret;
		}

		/*
		 * protected void prepareTts() { if(mLstSms != null) { mTtsStr =
		 * String.format("�?d个短信�?, mLstSms.size()); mDisplayStr =
		 * String.format("�?d个短信�?, mLstSms.size());
		 * 
		 * if(mLstSms.size() > 0) { int index = 1; for(SmsData sms : mLstSms) {
		 * String name = sms.getmContactName(); if(name == null || name.length()
		 * == 0) name = sms.getmContactPhone();
		 * 
		 * Date date = new Date(sms.getmTime()); String sDateTts =
		 * String.format("%d�?d�?%d�?d�?, date.getMonth() + 1, date.getDate(),
		 * date.getHours(), date.getMinutes());
		 * 
		 * mTtsStr += "短信" + index + ":|*" + index + "*|";
		 * 
		 * if(sms.getmType() == PhoneData.TYPE_SENT) { mTtsStr += sDateTts +
		 * "发给" + name + ":" + sms.getmContent() + "�?; } else if(sms.getmType()
		 * == SmsData.TYPE_READ) { mTtsStr += sDateTts + "来自" + name + ":" +
		 * sms.getmContent() + "�?; } else { mTtsStr += sDateTts + "来自" + name +
		 * "(未读):" + sms.getmContent() + "�?; }
		 * 
		 * index++; } } } }
		 */
	}

	public class CallJsonData extends JsonData {
		public List<CallData> mCallData = new ArrayList<CallData>();

		public class CallData {
			public String id;
			public String contact_name;
			public String contact_number;
			public String time;
			public String call_type;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				CallData call = new CallData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					call.id = JsonUtil.optString(objdata, "id", null);
					call.contact_name = JsonUtil.optString(objdata,
							"contact_name", null);
					call.contact_number = JsonUtil.optString(objdata,
							"contact_number", null);
					call.time = JsonUtil.optString(objdata, "time", null);
					call.call_type = JsonUtil.optString(objdata, "call_type",
							null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mCallData.add(call);
			}
			return true;
		}
	}

	public class MemoJsonData extends JsonData {
		// public List<String> content = new ArrayList<String>();
		public List<MemoData> mMemoData = new ArrayList<PreFormatData.MemoJsonData.MemoData>();

		public class MemoData {
			public String date;
			public String content;
			public int selectable;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			MemoData memoData;
			for (int i = 0; i < obj.length(); i++) {
				memoData = new MemoData();
				JSONObject objData;
				try {
					objData = obj.getJSONObject(i);
					memoData.date = JsonUtil.optString(objData, "date", null);
					memoData.content = JsonUtil.optString(objData, "content",
							null);
					memoData.selectable = JsonUtil.optInt(objData, "selectable", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mMemoData.add(memoData);
			}
			return true;
		}
	}

	public class BluetoothJsonData extends JsonData {
		public List<BluetoothData> mBluetoothData = new ArrayList<BluetoothData>();

		public class BluetoothData {
			public String name;
			public String paired;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				BluetoothData bluetooth = new BluetoothData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					bluetooth.name = JsonUtil.optString(objdata, "name", null);
					bluetooth.paired = JsonUtil.optString(objdata, "paired",
							null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mBluetoothData.add(bluetooth);
			}
			return true;
		}
	}

	/**
	 * 人物百科
	 */
	public class PersonJsonData extends JsonData {
//		public List<PersonData> mPersonDatas = new ArrayList<PersonData>();
		public PersonData mPersonData=new PersonData();
		public class PersonData {
			public String name;
			public String photo_url;
			public String nation;
			public String ethnic;
			public String birthday;
			public String blood_type;
			public String school;
			public String career;
			public String height;
			public String weight;
			public String sex;
			public String spouse;
			public String children;
			public String book;
			public String description;
			public int highlight_index;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PersonData data=new PersonData();
				JSONObject objData;
				try {
					objData=obj.getJSONObject(i);
					data.name=JsonUtil.optString(objData, "name", null);
					data.photo_url=JsonUtil.optString(objData, "photo_url", null);
					data.nation=JsonUtil.optString(objData, "nation", null);
					data.ethnic=JsonUtil.optString(objData, "ethnic", null);
					data.birthday=JsonUtil.optString(objData, "birthday", null);
					data.blood_type=JsonUtil.optString(objData, "blood_type", null);
					data.school=JsonUtil.optString(objData, "school", null);
					data.career=JsonUtil.optString(objData, "career", null);
					data.height=JsonUtil.optString(objData, "height", null);
					data.weight=JsonUtil.optString(objData, "weight", null);
					data.sex=JsonUtil.optString(objData, "sex", null);
					data.spouse=JsonUtil.optString(objData, "spouse", null);
					data.children=JsonUtil.optString(objData, "children", null);
					data.book=JsonUtil.optString(objData, "book", null);
					data.description=JsonUtil.optString(objData, "description", null);
					data.highlight_index=JsonUtil.optInt(objData, "highlight_index", 0);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				mPersonData=data;
			}
			return true;
		}
	}
	
	public class OtherBaikeJsonData extends JsonData{
		public OtherBaikeData mOtherBaikeData=new OtherBaikeData();
		public class OtherBaikeData{
			public String field_value[];
			public String field_name[];
			public String description;
			public String photo_url;
			public int hightlight_item;
		}
		
		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				OtherBaikeData data=new OtherBaikeData();
				JSONObject objData;
				try {
					objData=obj.getJSONObject(i);
					data.field_name=JsonUtil.optStringArray(objData, "field_name");
					data.field_value=JsonUtil.optStringArray(objData, "field_value");
					data.description=JsonUtil.optString(objData, "description", null);
					data.photo_url=JsonUtil.optString(objData, "photo_url", null);
					data.hightlight_item=JsonUtil.optInt(objData, "highlight_index", 0);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				mOtherBaikeData=data;
			}
			return true;
		}
	}

	public class WeatherJsonData extends JsonData {
		public List<WeatherData> mWeatherData = new ArrayList<WeatherData>();

		public class WeatherData {
			public int date;
			public long real_date;
			public int weather1;
			public int weather2;
			public String wind;
			public String wind_level;
			public String temperature_high;
			public String temperature_low;
			public String descript;
			public String city;
			public int is_querying;
			public int pm25;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				WeatherData data = new WeatherData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.date = JsonUtil.optInt(objdata, "date", 0);
					data.real_date=JsonUtil.optLong(objdata, "real_date", 0);
					data.weather1 = JsonUtil.optInt(objdata, "weather1", 0);
					data.weather2 = JsonUtil.optInt(objdata, "weather2", 0);
					data.wind = JsonUtil.optString(objdata, "wind", "");
					data.wind_level = JsonUtil.optString(objdata, "wind_level",
							"");
					data.temperature_high = JsonUtil.optString(objdata,
							"temperature_high", null);
					data.temperature_low = JsonUtil.optString(objdata,
							"temperature_low", null);
					data.descript = JsonUtil.optString(objdata, "description",
							null);
					data.city = JsonUtil.optString(objdata, "city", null);
					data.is_querying = JsonUtil.optInt(objdata, "is_querying",
							0);
					data.pm25 = JsonUtil.optInt(objdata, "pm25", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mWeatherData.add(data);
			}
			return true;
		}
	}

	public class PoemJsonData extends JsonData {
		public List<PoemData> mPoemData = new ArrayList<PoemData>();

		public class PoemData {
			public String author;
			public String content;
			public String title;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PoemData data = new PoemData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.author = JsonUtil.optString(objdata, "author", null);
					data.content = JsonUtil.optString(objdata, "content", null);
					data.title = JsonUtil.optString(objdata, "title", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mPoemData.add(data);
			}
			return true;
		}
	}

	public class WeiboJsonData extends JsonData {
		public class WeiboData {
			public String source;
			public String sub_type;
			public String text_content;
			public String image_thumb;
			public String image_middle;
			public String image_larger;
			public String from;
			public String from_portrait;
			public int from_care_num;
			public int from_fan_num;
			public int from_weibo_num;
			public String from_sex;
			public int from_comment_num;
			public int from_forward_num;
			public int from_like_num;
			public String original_from;
			public String original_from_portrait;
			public String original_text_content;
			public String original_image_thumb;
			public String original_image_middle;
			public String original_image_larger;
			public int original_from_care_num;
			public int original_from_fan_num;
			public int original_from_weibo_num;
			public int original_comment_num;
			public int original_forward_num;
			public int original_like_num;
			public String create_time;
			public String time;

		}

		public List<WeiboData> lstContent = new ArrayList<WeiboData>();

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				try {
					JSONObject objSub = obj.getJSONObject(i);
					WeiboData data = new WeiboData();
					data.source = JsonUtil.optString(objSub, "source", null);
					data.sub_type = JsonUtil
							.optString(objSub, "sub_type", null);
					data.text_content = JsonUtil.optString(objSub,
							"text_content", null);
					data.image_thumb = JsonUtil.optString(objSub,
							"image_thumb", null);
					data.image_middle = JsonUtil.optString(objSub,
							"image_middle", null);
					data.image_larger = JsonUtil.optString(objSub,
							"image_larger", null);
					data.from = JsonUtil.optString(objSub, "from", null);
					data.from_portrait = JsonUtil.optString(objSub,
							"from_portrait", null);
					data.original_from = JsonUtil.optString(objSub,
							"original_from", null);
					data.original_from_portrait = JsonUtil.optString(objSub,
							"original_from_portrait", null);
					data.original_text_content = JsonUtil.optString(objSub,
							"original_text_content", null);
					data.original_image_thumb = JsonUtil.optString(objSub,
							"original_image_thumb", null);
					data.original_image_middle = JsonUtil.optString(objSub,
							"original_image_middle", null);
					data.original_image_larger = JsonUtil.optString(objSub,
							"original_image_larger", null);
					data.from_care_num = JsonUtil.optInt(objSub,
							"from_care_num", 0);
					data.from_fan_num = JsonUtil.optInt(objSub, "from_fan_num",
							0);
					data.from_weibo_num = JsonUtil.optInt(objSub,
							"from_weibo_num", 0);
					data.from_sex = JsonUtil
							.optString(objSub, "from_sex", null);
					data.from_weibo_num = JsonUtil.optInt(objSub,
							"from_weibo_num", 0);
					data.from_comment_num = JsonUtil.optInt(objSub,
							"from_comment_num", 0);
					data.from_forward_num = JsonUtil.optInt(objSub,
							"from_forward_num", 0);
					data.from_like_num = JsonUtil.optInt(objSub,
							"from_like_num", 0);
					data.original_from_care_num = JsonUtil.optInt(objSub,
							"original_from_care_num", 0);
					data.original_from_fan_num = JsonUtil.optInt(objSub,
							"original_from_fan_num", 0);
					data.original_from_weibo_num = JsonUtil.optInt(objSub,
							"original_from_weibo_num", 0);
					data.original_comment_num = JsonUtil.optInt(objSub,
							"original_from_weibo_num", 0);
					data.original_forward_num = JsonUtil.optInt(objSub,
							"original_forward_num", 0);
					data.original_like_num = JsonUtil.optInt(objSub,
							"original_like_num", 0);
					data.create_time = JsonUtil.optString(objSub,
							"create_time", null);
					data.time = JsonUtil.optString(objSub,
							"time", null);
					lstContent.add(data);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
	}

	public class NewsJsonData extends JsonData {
		public List<NewsData> mNewsData = new ArrayList<NewsData>();

		public class NewsData {
			public String time;
			public String title;
			public String url;
			public String detail;
			public String source;
			public String image_url;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				NewsData data = new NewsData();
				JSONObject objData;
				try {
					objData = obj.getJSONObject(i);
					data.time = JsonUtil.optString(objData, "time", null);
					data.title = JsonUtil.optString(objData, "title", null);
					data.url = JsonUtil.optString(objData, "url", null);
					data.detail = JsonUtil.optString(objData, "detail", null);
					data.source = JsonUtil.optString(objData, "source", null);
					data.image_url = JsonUtil.optString(objData, "image_url",
							null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mNewsData.add(data);
			}
			return true;
		}
	}

	public class StockJsonData extends JsonData {
		public List<StockData> mStockData = new ArrayList<StockData>();

		public class StockData {
			public String id;
			public String name;
			public String cur_price;
			public String price_start; // 开�?			
			public String price_end; // 收盘
			public String price_high; // 最高价
			public String price_low; // 最低价
			public String turnover; // 换手�?			
			public String change_rate; // 涨跌�?			
			public String change_amount;// 涨跌�?			
			public String amount;// 成交�?			
			public String volume;// 成交�?			
			public String time;
			public int is_history;
			public int favorite;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				StockData data = new StockData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.id = JsonUtil.optString(objdata, "id", null);
					data.name = JsonUtil.optString(objdata, "name", null);
					data.cur_price = JsonUtil.optString(objdata, "cur_price",
							null);
					data.price_start = JsonUtil.optString(objdata,
							"price_start", null);
					data.price_end = JsonUtil.optString(objdata, "price_end",
							null);
					data.price_high = JsonUtil.optString(objdata, "price_high",
							null);
					data.price_low = JsonUtil.optString(objdata, "price_low",
							null);
					data.turnover = JsonUtil.optString(objdata, "turnover",
							null);
					data.change_rate = JsonUtil.optString(objdata,
							"change_rate", null);
					data.change_amount = JsonUtil.optString(objdata,
							"change_amount", null);
					data.amount = JsonUtil.optString(objdata, "amount", null);
					data.volume = JsonUtil.optString(objdata, "volume", null);
					data.time = JsonUtil.optString(objdata, "time", null);
					data.is_history = JsonUtil.optInt(objdata, "is_history", 0);
					data.favorite = JsonUtil.optInt(objdata, "favorite", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mStockData.add(data);
			}
			return true;
		}
	}

	public class ContactJsonData extends JsonData {
		public List<ContactData> mContactData = new ArrayList<ContactData>();

		public class ContactData {
			public int id;
			public String display_name;
			public String first_name[];
			public String middle_name[];
			public String last_name[];
			public String nick_name[];
			public String organization_title[];
			public String organization_company[];
			public String home_address[];
			public String work_address[];
			public String other_address[];
			public String home_phone[];
			public String work_phone[];
			public String mobile_phone[];
			public String other_phone[];
			public String private_email[];
			public String work_email[];
			public String msn[];
			public String qq[];
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				ContactData data = new ContactData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.id = JsonUtil.optInt(objdata, "id", 0);
					data.display_name = JsonUtil.optString(objdata,
							"display name", null);
					data.first_name = JsonUtil.optStringArray(objdata,
							"first_name");
					data.middle_name = JsonUtil.optStringArray(objdata,
							"middle_name");
					data.last_name = JsonUtil.optStringArray(objdata,
							"last_name");
					data.nick_name = JsonUtil.optStringArray(objdata,
							"nick_name");
					data.organization_title = JsonUtil.optStringArray(objdata,
							"organization_title");
					data.organization_company = JsonUtil.optStringArray(
							objdata, "organization_company");
					data.home_address = JsonUtil.optStringArray(objdata,
							"home_address");
					data.work_address = JsonUtil.optStringArray(objdata,
							"work_address");
					data.other_address = JsonUtil.optStringArray(objdata,
							"other_address");
					data.home_phone = JsonUtil.optStringArray(objdata,
							"home phone");
					data.work_phone = JsonUtil.optStringArray(objdata,
							"work phone");
					data.mobile_phone = JsonUtil.optStringArray(objdata,
							"mobile phone");
					data.other_phone = JsonUtil.optStringArray(objdata,
							"other phone");
					data.private_email = JsonUtil.optStringArray(objdata,
							"private_email");
					data.work_email = JsonUtil.optStringArray(objdata,
							"work_email");
					data.msn = JsonUtil.optStringArray(objdata, "msn");
					data.qq = JsonUtil.optStringArray(objdata, "qq");
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mContactData.add(data);
			}
			return true;
		}
	}

	public class TrainJsonData extends JsonData {
		public List<TrainData> mTrainData = new ArrayList<TrainData>();

		public class TrainData {
			public String city_from;
			public String station_from;
			public int is_start;
			public String city_to;
			public String station_to;
			public int is_end;
			public String departure_time;
			public String arrive_time;
			public String train_no;
			public String train_type;
			public String seat_class;
			public String price;
			public String quantity;
			public String url;
			public int provider_id;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				TrainData data = new TrainData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.city_from = JsonUtil.optString(objdata, "city_from",
							null);
					data.station_from = JsonUtil.optString(objdata,
							"station_from", null);
					data.is_start=JsonUtil.optInt(objdata, "is_start", 0);
					data.city_to = JsonUtil.optString(objdata, "city_to", null);
					data.station_to = JsonUtil.optString(objdata, "station_to",
							null);
					data.is_end=JsonUtil.optInt(objdata, "is_end", 0);
					data.departure_time = JsonUtil.optString(objdata,
							"departure_time", null);
					data.arrive_time = JsonUtil.optString(objdata,
							"arrive_time", null);
					data.train_no = JsonUtil.optString(objdata, "train_no",
							null);
					data.seat_class = JsonUtil.optString(objdata, "seat_class",
							null);
					data.quantity = JsonUtil.optString(objdata, "quantity",
							null);
					data.price = JsonUtil.optString(objdata, "price", null);
					data.url = JsonUtil.optString(objdata, "url", null);
					data.train_type = JsonUtil.optString(objdata, "train_type",
							null);
					data.provider_id = JsonUtil.optInt(objdata, "provider_id",
							0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mTrainData.add(data);
			}
			return true;
		}
	}

	public class PlaneJsonData extends JsonData {
		public List<PlaneData> mPlaneData = new ArrayList<PlaneData>();
		public PlanDescriptionData mPlaneDescData = null;

		public class PlaneData {
			public String city_from;
			public String airport_from;
			public String city_to;
			public String airport_to;
			public String departure_time;
			public String arrive_time;
			public String air_company;
			public String craft_type;
			public String flight_no;
			public String flight_class;
			public String price;
			public String discount;
			public String quantity;
			public String url;
			public int provider_id;

		}
		
		public class PlanDescriptionData extends DescriptionData{
			public String all_url;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PlaneData data = new PlaneData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.city_from = JsonUtil.optString(objdata, "city_from",
							null);
					data.airport_from = JsonUtil.optString(objdata,
							"airport_from", null);
					data.city_to = JsonUtil.optString(objdata, "city_to", null);
					data.airport_to = JsonUtil.optString(objdata, "airport_to",
							null);
					data.departure_time = JsonUtil.optString(objdata,
							"departure_time", null);
					data.arrive_time = JsonUtil.optString(objdata,
							"arrive_time", null);
					data.air_company = JsonUtil.optString(objdata,
							"air_company", null);
					data.craft_type = JsonUtil.optString(objdata, "craft_type",
							null);
					data.flight_no = JsonUtil.optString(objdata, "flight_no",
							null);
					data.flight_class = JsonUtil.optString(objdata,
							"flight_class", null);
					data.price = JsonUtil.optString(objdata, "price", null);
					data.discount = JsonUtil.optString(objdata, "discount",
							null);
					data.quantity = JsonUtil.optString(objdata, "quantity",
							null);
					data.url = JsonUtil.optString(objdata, "url", null);
					data.provider_id = JsonUtil.optInt(objdata, "provider_id",
							0);

				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mPlaneData.add(data);
			}
			return true;
		}
		
		@Override
		public boolean parseDescription(Context context, JSONObject obj) {
			mDescriptionData = new PlanDescriptionData();
			try {
				((PlanDescriptionData)mDescriptionData).all_url = JsonUtil.optString(obj, "all_url", null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			super.parseDescription(context, obj);
			mPlaneDescData = (PlanDescriptionData)mDescriptionData;
			return true;
		}
	}

	public class WikipediaJsonData extends JsonData {
		public List<WikipediaData> mWikipediaData = new ArrayList<WikipediaData>();

		public class WikipediaData {
			public String content;
			public String url;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				WikipediaData data = new WikipediaData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.content = JsonUtil.optString(objdata, "content", null);
					data.url = JsonUtil.optString(objdata, "url", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mWikipediaData.add(data);
			}
			return true;
		}
	}

	public class PoiJsonData extends JsonData {
		public List<PoiItemData> mLstPoi = new ArrayList<PoiItemData>();

		public class PoiItemData {
			public String address; // 地图中心点详细地址
			public double longitude;
			public double latitude;

			public String poi_id; // poi的id
			public String poi_title; // poi的名�?			
			public String poi_snippet; // poi地址
			public double poi_longitude; // poi经度
			public double poi_latitude; // poi维度
			public String poi_type; // poi的类�?			
			public String poi_telephone; // poi电话
			public String poi_distance;
			public int poi_avg_rating; // 0-10
			public String poi_avg_price;
			public int source;
			public String poi_url;
			public String coupon_url;

		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PoiItemData data = new PoiItemData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.address = JsonUtil.optString(objdata, "address", null);
					data.longitude = longToLatLngDouble(JsonUtil.optLong(objdata, "longitude", 0));
					data.latitude = longToLatLngDouble(JsonUtil.optLong(objdata, "latitude", 0));
					data.poi_id = JsonUtil.optString(objdata, "poi_id", null);
					data.poi_title = JsonUtil.optString(objdata, "poi_title",
							null);
					data.poi_snippet = JsonUtil.optString(objdata,
							"poi_snippet", null);
					data.poi_longitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"poi_longitude", 0));
					data.poi_latitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"poi_latitude", 0));
					data.poi_type = JsonUtil.optString(objdata, "poi_type",
							null);
					data.poi_telephone = JsonUtil.optString(objdata,
							"poi_telephone", null);
					data.poi_distance = JsonUtil.optString(objdata,
							"poi_distance", null);
					data.poi_avg_rating = JsonUtil.optInt(objdata,
							"poi_avg_rating", 0);
					data.poi_avg_price = JsonUtil.optString(objdata,
							"poi_avg_price", null);
					data.source = JsonUtil.optInt(objdata, "source", 0);
					data.poi_url = JsonUtil.optString(objdata, "poi_url", null);
					data.coupon_url = JsonUtil.optString(objdata, "coupon_url",
							null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mLstPoi.add(data);
			}
			return true;
		}
		
		private double longToLatLngDouble(long value) {
			return (double)value / 1000000;
		}
	}

	public class PoemtitleJsonData extends JsonData {
		public List<PoemTitleData> mPoemTitleData = new ArrayList<PoemTitleData>();

		public class PoemTitleData {
			public String author;
			public String title;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PoemTitleData data = new PoemTitleData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.author = JsonUtil.optString(objdata, "author", null);
					data.title = JsonUtil.optString(objdata, "title", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mPoemTitleData.add(data);
			}
			return true;
		}
	}

	public class RouteJsonData extends JsonData {
		public List<RouteData> mLstRouteData = new ArrayList<RouteData>();

		public class RouteData {
			public String from_name;
			public String to_name;
			public double route_from_longitude;
			public double route_from_latitude;
			public double route_to_longitude;
			public double route_to_latitude;
			public long route_mode;
			public String address;
			public String distance;
			public String way;
		}
		
		private double longToLatLngDouble(long value) {
			return (double)value / 1000000;
		}
		

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				RouteData data = new RouteData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.from_name = JsonUtil.optString(objdata, "from_name",
							null);
					data.to_name = JsonUtil.optString(objdata, "to_name", null);
					data.route_from_longitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"route_from_longitude", 0));
					data.route_from_latitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"route_from_latitude", 0));
					data.route_to_longitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"route_to_longitude", 0));
					data.route_to_latitude = longToLatLngDouble(JsonUtil.optLong(objdata,
							"route_to_latitude", 0));
					data.route_mode = JsonUtil
							.optLong(objdata, "route_mode", 0);
					data.address = JsonUtil.optString(objdata, "to_address",null);
					data.distance = JsonUtil.optString(objdata, "distance",null);
					data.way = JsonUtil.optString(objdata, "way", null);

				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mLstRouteData.add(data);
			}
			return true;
		}
	}

	public class ArtistAlbumJsonData extends JsonData {
		public List<ArtistAlbumData> mLstArtistData = new ArrayList<ArtistAlbumData>();

		public class ArtistAlbumData {
			public String artist;
			public String album;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				ArtistAlbumData data = new ArtistAlbumData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.artist = JsonUtil.optString(objdata, "artist", null);
					data.album = JsonUtil.optString(objdata, "album", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mLstArtistData.add(data);
			}
			return true;
		}
	}

	public class MusicAlbumJsonData extends JsonData {
		public List<MusicAlbumData> mLstMusicData = new ArrayList<MusicAlbumData>();

		public class MusicAlbumData {
			public String title;
			public String artist;
			public String album;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				MusicAlbumData data = new MusicAlbumData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.title = JsonUtil.optString(objdata, "title", null);
					data.artist = JsonUtil.optString(objdata, "artist", null);
					data.album = JsonUtil.optString(objdata, "album", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mLstMusicData.add(data);
			}
			return true;
		}

	}

	public class ShoppingItemJsonData extends JsonData {
		public List<ShoppingItemData> mLstItem = new ArrayList<ShoppingItemData>();

		public class ShoppingItemData {
			public String name;
			public String price;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				ShoppingItemData data = new ShoppingItemData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.name = JsonUtil.optString(objdata, "name", null);
					data.price = JsonUtil.optString(objdata, "price", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mLstItem.add(data);
			}
			return true;
		}
	}

	public class HotelJsonData extends JsonData {
		public List<HotelData> hotels = new ArrayList<PreFormatData.HotelJsonData.HotelData>();

		public class HotelData {
			public String name;
			public String image;
			public String address;
			public String ctrip_rating;
			public String user_rating;
			public String floor_price;
			public String max_price;
			public String description;
			public String description_url;
		}
		
		public class HotelDescrptionData {
			public int filter;
			public String[] filters;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				HotelData data = new HotelData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.name = JsonUtil.optString(objdata, "hotel_name", null);
					data.image = JsonUtil.optString(objdata, "hotel_image",
							null);
					data.address = JsonUtil.optString(objdata, "hotel_address",
							null);
					data.ctrip_rating = JsonUtil.optString(objdata,
							"ctrip_rating", null);
					data.user_rating = JsonUtil.optString(objdata,
							"user_rating", null);
					data.floor_price = JsonUtil.optString(objdata,
							"floor_price", null);
					data.max_price = JsonUtil.optString(objdata,
							"max_price", null);
					data.description = JsonUtil.optString(objdata,
							"description", null);
					data.description_url = JsonUtil.optString(objdata,
							"description_url", null);

				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				hotels.add(data);
			}
			return true;
		}
	}

	public class TrafficJsonData extends JsonData {
		public List<TrafficData> trafficDatas = new ArrayList<PreFormatData.TrafficJsonData.TrafficData>();
		public String from_latitude;
		public String from_longitude;
		public String from_name;
		public String to_latitude;
		public String to_longitude;
		public String to_name;
		public String search_mode;

		public class TrafficData {

			public int total_distance;
			public int total_time;
			public String[] segment_title;
			public String[] segment_type;
			public String[] segment_distance;
			public String[] segment_time;
			public String[] segment_description;

		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			TrafficData trafficData;
			for (int i = 0; i < obj.length(); i++) {
				try {
					JSONObject jsonObject = obj.getJSONObject(i);
					trafficData = new TrafficData();
					trafficData.total_distance = JsonUtil.optInt(jsonObject,
							"total_distance", 0);
					trafficData.total_time = JsonUtil.optInt(jsonObject,
							"total_time", 0);
					trafficData.segment_title = JsonUtil.optStringArray(
							jsonObject, "segment_title");
					trafficData.segment_type = JsonUtil.optStringArray(
							jsonObject, "segment_type");
					trafficData.segment_distance = JsonUtil.optStringArray(
							jsonObject, "segment_distance");
					trafficData.segment_time = JsonUtil.optStringArray(
							jsonObject, "segment_time");
					trafficData.segment_description = JsonUtil.optStringArray(
							jsonObject, "segment_description");
					trafficDatas.add(trafficData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public boolean parseDescription(Context context, JSONObject obj) {
			from_latitude = JsonUtil.optString(obj, "from_latitude", null);
			from_longitude = JsonUtil.optString(obj, "from_longitude", null);
			from_name = JsonUtil.optString(obj, "from_name", null);
			to_name = JsonUtil.optString(obj, "to_name", null);
			search_mode = JsonUtil.optString(obj, "search_mode", null);
			to_latitude = JsonUtil.optString(obj, "to_latitude", null);
			to_longitude = JsonUtil.optString(obj, "to_longitude", null);
			return super.parseDescription(context, obj);
		}
	}

	public class BusJsonData extends JsonData {
		public List<BusData> busDatas = new ArrayList<PreFormatData.BusJsonData.BusData>();

		public class BusData {
			public String stop_name;
			public String stop_longitude;
			public String stop_latitude;
			public String[] bus_name;
			public String[] bus_start;
			public String[] bus_stop;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				BusData data = new BusData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.stop_name = JsonUtil.optString(objdata, "stop_name",
							null);
					data.stop_longitude = JsonUtil.optString(objdata,
							"stop_longitude", null);
					data.stop_latitude = JsonUtil.optString(objdata,
							"stop_latitude", null);
					data.bus_name = JsonUtil
							.optStringArray(objdata, "bus_name");
					data.bus_start = JsonUtil.optStringArray(objdata,
							"bus_start");
					data.bus_stop = JsonUtil
							.optStringArray(objdata, "bus_stop");
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				busDatas.add(data);
			}
			return true;
		}
	}
	
	public class GameJsonData extends JsonData{
		public GameData mGameData = new GameData();
		public class GameData{
			public int img;
			public String sn;
			public String result;
			public String description;
			public String action;
			public int failed_count;
			public String spend_time;
			public ArrayList<String> topnames = new ArrayList<String>();
			public ArrayList<String> toptimes = new ArrayList<String>();
		}
		
		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				GameData data=new GameData();
				JSONObject objData;
				try {
					objData=obj.getJSONObject(i);
					data.img=JsonUtil.optInt(objData, "img", 0);
					data.sn=JsonUtil.optString(objData, "sn", null);
					data.description = JsonUtil.optString (objData, "description", null);
					data.result = JsonUtil.optString (objData, "result", null);
					for( int index = 1; index <= 10; index ++) {
						String temp = JsonUtil.optString(objData, "top_" + index, null);
						if (temp == null) {
							data.topnames.add("---");
							data.toptimes.add("---");
							continue;
						}
						String subString[] = temp.split(" ");
						if (subString.length < 2) {
							data.topnames.add("---");
							data.toptimes.add("---");
						}
						data.topnames.add(subString[0]);
						data.toptimes.add(subString[1]);
					}
					data.action = JsonUtil.optString (objData, "action", null);
					data.failed_count = JsonUtil.optInt (objData, "failcount", 0);
					data.spend_time = JsonUtil.optString(objData, "spendtime", null);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				mGameData=data;
			}
			return true;
		}
	}
	
	public class TVJsonData extends JsonData{
		public TVProgramData mTVProgramData = new TVProgramData();
		public TVDescriptionData mTVDescriptionData =null;
		
		public class TVDescriptionData extends DescriptionData{
			public String logo;
			public String station_name;
			public String url;
		}
		
		public class TVProgramData{
			public String time[];
			public String name[];
			public long is_highlight;
		}
		
		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				TVProgramData programData = new TVProgramData();
				JSONObject objData;
				try {
					objData=obj.getJSONObject(i);
					programData.time=JsonUtil.optStringArray(objData, "time");
					if (programData.time == null) {
						programData.time = new String[]{""};
					}
					programData.name=JsonUtil.optStringArray(objData, "name");
					if (programData.name == null) {
						programData.name = new String[]{""};
					}
					programData.is_highlight = JsonUtil.optLong(objData, "is_highlight", 0);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				mTVProgramData=programData;
			}
			return true;
		}
		
		@Override
		public boolean parseDescription(Context context, JSONObject obj) {
			mDescriptionData = new TVDescriptionData();
			try {
				((TVDescriptionData)mDescriptionData).station_name = JsonUtil.optString(obj, "station_name",
						null);
				((TVDescriptionData)mDescriptionData).logo = JsonUtil.optString(obj, "logo", null);
				((TVDescriptionData)mDescriptionData).url = JsonUtil.optString(obj, "url", null);
				mDescriptionData.filter = JsonUtil.optInt(obj, "filter", 0);
				mDescriptionData.filters = changeToDate(JsonUtil.optStringArray(obj, "filters"));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			mTVDescriptionData = (TVDescriptionData)mDescriptionData;
			return true;
		}
		
		private String[] changeToDate(String[] filters){
			String[] values = new String[filters.length];
			for (int i = 0; i < filters.length; i++) {
				values[i] = getDate(filters[i]);
			}			
			return values;
		}
		
		private String getWeekday(String string) {
			Date date = new Date();
			long time = 0;
			try {
				time = Long.parseLong(string);
			}catch (NumberFormatException e) {
				return "";
			}
			date.setTime(time);
			String day = "一";
			switch (date.getDay()) {
			case 0:
				day = "日";
				break;
			case 1:
				day = "一";
				break;
			case 2:
				day = "二";
				break;
			case 3:
				day = "三";
				break;
			case 4:
				day = "四";
				break;
			case 5:
				day = "五";
				break;
			case 6:
				day = "六";
				break;
			}			
			return " 星期" + day;
		}

		private String getDate(String dateStr) {
			Date date = new Date();
			long time = 0;
			try {
				time = Long.parseLong(dateStr);
			}catch (NumberFormatException e) {
				return "";
			}
			date.setTime(time);
			
			return (date.getMonth() + 1) + "-" + date.getDate() + " " + getWeekday(dateStr);
		}
	}
	
	public class BusInfoJsonData extends JsonData {
		public BusInfoData mData = null;
		public BusDescriptionData mBusDescriptionData = null;

		public class BusInfoData {			
			public String[] bus_start;
			public String[] bus_end;
			public String[] stop_names;
			public String[] last_bus;
			public String[] early_bus;
			public String[] mileage;
		}
		
		public class BusDescriptionData extends DescriptionData{
			public String bus_name;
			public String city;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				BusInfoData data = new BusInfoData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.bus_start = JsonUtil.optStringArray(objdata, "bus_start");
					data.bus_end = JsonUtil.optStringArray(objdata, "bus_end");
					data.early_bus = JsonUtil.optStringArray(objdata, "early_bus");
					data.last_bus = JsonUtil.optStringArray(objdata, "last_bus");
					data.stop_names = JsonUtil.optStringArray(objdata, "stop_names");
					data.mileage= JsonUtil.optStringArray(objdata, "mileage");
					
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mData = data;
			}
			return true;
		}
		
		@Override
		public boolean parseDescription(Context context, JSONObject obj) {
			mDescriptionData = new BusDescriptionData();
			try {
				((BusDescriptionData)mDescriptionData).bus_name = JsonUtil.optString(obj, "bus_number", null);
				((BusDescriptionData)mDescriptionData).city = JsonUtil.optString(obj, "city",	null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			super.parseDescription(context, obj);
			mBusDescriptionData = (BusDescriptionData)mDescriptionData;
			return true;
		}
	}
   public class CallDelayJsonData extends JsonData {
		public CallDelayData mCallDelayData = new CallDelayData();

		public class CallDelayData {
			public String contact_name;
			public String contact_number;
			public int contact_id;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				CallDelayData callDelay = new CallDelayData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);					
					callDelay.contact_name = JsonUtil.optString(objdata,
							"contact_name", null);
					callDelay.contact_number = JsonUtil.optString(objdata,
							"contact_number", null);
					callDelay.contact_id = JsonUtil.optInt(objdata,
							"contact_id", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mCallDelayData = callDelay;
			}
			return true;
		}
	}	
   
   public class ExchangeRateJsonData extends JsonData {
		public String mSource = null;
		public String[] mtargets = null;


		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);					
					mtargets = JsonUtil.optStringArray(objdata,	"target_currency");
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		
		@Override
		public boolean parseDescription(Context context, JSONObject obj) {
			try {
				mSource = JsonUtil.optString(obj, "source_currency", null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return super.parseDescription(context, obj);
		}
	}
   
	public class LyricsJsonData extends JsonData {
		public String lyrics = null;

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);					
					lyrics = JsonUtil.optString(objdata, "lyrics", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}		
	}
	
	public class PoiReferenceJsonData extends JsonData {
		public List<PoiReferenceData> mPoiReferenceDatas = new ArrayList<PreFormatData.PoiReferenceJsonData.PoiReferenceData>();
		
		public class PoiReferenceData {
			public String ref_name;
			public String ref_address;
			public String ref_city;
			public long ref_longitude;
			public long ref_latitude;
			public long ref_distance;
		}

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				PoiReferenceData data = new PoiReferenceData();
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);
					data.ref_name = JsonUtil.optString(objdata, "ref_name",	null);
					data.ref_address = JsonUtil.optString(objdata, "ref_address", null);
					data.ref_city = JsonUtil.optString(objdata, "ref_city", null);
					data.ref_longitude = JsonUtil.optLong(objdata, "ref_longitude", 0);
					data.ref_latitude = JsonUtil.optLong(objdata, "ref_latitude", 0);
					data.ref_distance = JsonUtil.optLong(objdata, "ref_distance", 0);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				mPoiReferenceDatas.add(data);
			}
			return true;
		}		
	}
	
	public class JokeJsonData extends JsonData {
		public String joke = null;

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);					
					joke = JsonUtil.optString(objdata, "content", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}		
	}
	
	public class CookingJsonData extends JsonData {
		public String cooking = null;

		@Override
		public boolean parse(Context context, JSONArray obj) {
			for (int i = 0; i < obj.length(); i++) {
				
				JSONObject objdata;
				try {
					objdata = obj.getJSONObject(i);					
					cooking = JsonUtil.optString(objdata, "content", null);
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}		
	}
}
