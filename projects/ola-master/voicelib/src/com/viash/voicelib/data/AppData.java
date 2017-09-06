package com.viash.voicelib.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

import com.viash.voicelib.hardware.HBluetooth;
import com.viash.voicelib.hardware.HGPS;
import com.viash.voicelib.hardware.HModelSetting;
import com.viash.voicelib.hardware.HNet;
import com.viash.voicelib.hardware.HScreenBrightness;
import com.viash.voicelib.hardware.HVolume;
import com.viash.voicelib.msg.MsgConst;
import com.viash.voicelib.utils.AppUtil;
import com.viash.voicelib.utils.CalendarUtil;
import com.viash.voicelib.utils.CalendarUtil.EventData;
import com.viash.voicelib.utils.CalendarUtil.InstanceData;
import com.viash.voicelib.utils.CallUtil;
import com.viash.voicelib.utils.CallUtil.PhoneData;
import com.viash.voicelib.utils.CallUtil.SmsData;
import com.viash.voicelib.utils.ContactUtil;
import com.viash.voicelib.utils.ContactUtil.ContactInfo;
import com.viash.voicelib.utils.CustomToast;
import com.viash.voicelib.utils.JsonUtil;
import com.viash.voicelib.utils.MemoUtil;
import com.viash.voicelib.utils.alarm.AlarmUtil;

public class AppData extends BaseData {
	private static final String TAG = "AppData";
	public static final String SENT_SMS_ACTION = "VIASH_SENT_SMS_ACTION";
	public static final String RECEIVE_SMS_ACTION = "VIASH_RECEIVED_SMS_ACTION";
	public static final String SENT_SMS_NUMBER = "number";
	public static final String SENT_SMS_CONTENT = "content";
	public static final String APP_NAME_PHONE = "PhoneCall";
	public static final String APP_NAME_SMS = "SMS";
	public static final String APP_NAME_MUSIC = "PlayMusic";
	public static final String APP_NAME_OPEN_APP = "OpenExe";
	public static final String APP_NAME_OPEN_WEB = "OpenWeb";
	public static final String APP_NAME_SEND_WEIBO = "send_weibo";
	public static final String APP_NAME_SEND_WEIXIN = "send_weixin";
	public static final String APP_NAME_SEND_RENREN = "send_to_renren";
	public static final String APP_NAME_CALENDAR = "SetCalendarEvent";
	public static final String APP_NAME_DELETE_CALENDAR_BY_ID = "delete_calendar_event_by_id";
	public static final String APP_NAME_MODIFY_CALENDAR_BY_ID = "modify_calendar_event_by_id";
	public static final String APP_NAME_SWITCH_MOBILE_FUNCTION = "switch_mobile_function";
	public static final String APP_NAME_SWITCH_PAIR_BLUETOOTH = "pair_bluetooth";
	public static final String APP_NAME_READ_SMS = "ReadSMS";
	public static final String APP_NAME_MAP = "map";
	public static final String APP_NAME_ROUTE_MAP = "route_map";
	public static final String APP_NAME_NAVIGATION = "navigation";
	public static final String APP_NAME_SEND_EMAIL = "send_email_to_address";
	public static final String APP_NAME_RECEIVE_EMAIL = "receive_email";
	public static final String APP_NAME_SHARE_FILE = "share_file";

	public static final String APP_NAME_DELETE_ALARM_BY_ID = "delete_alarm_by_id";
	public static final String APP_NAME_CREATE_ALARM = "create_alarm";
	public static final String APP_NAME_CREATE_MEMO = "create_memo";
	public static final String APP_NAME_DELETE_MEMO = "delete_memo";
	public static final String APP_NAME_ADD_CONTACT = "add_contact";
	public static final String APP_NAME_DEL_CONTACT = "del_contact";
	public static final String APP_NAME_OPEN_CALENDAR = "month_calendar";
	public static final String APP_NAME_STOP_MUSIC = "StopMusic";
	public static final String APP_NANE_CONTROL_MUSIC = "MusicControl";
	public static final String APP_NANE_DOWNLOAD = "Download";
	public static final String APP_NANE_ADD_POSITION_ALARM = "create_pos_alarm";
	public static final String APP_NANE_DEL_POSITION_ALARM = "delete_pos_alarm_by_id";
	public static final String APP_NANE_PLAY_VIDEO = "play_video";
	public static final String APP_NANE_SAVE_SMS = "save_sms";
	public static final String APP_NANE_SEND_WEIXIN = "send_weixin";
	public static final String APP_NANE_SEND_RENREN = "send_to_renren";
	public static final String APP_NAME_HANDLE_INCOMING_CALL = "handle_incoming_call";// Leo
	public static final String APP_NAME_TAKE_PHOTO = "take_photo";
	public static final String APP_NAME_ALARM_SNOOZE ="alarm";
	public static final String APP_NAME_REMIND_SNOOZE = "remind";

	public static final String APP_NAME_HA_CONTROL = "ha_control";

	public static final String COM_VIASH_VOICE_ASSISTANT_REBOOT = "com.viash.voice_assistant.REBOOT";
	public static final String COM_VIASH_VOICE_ASSISTANT_START_RECORD = "com.viash.voice_assistant.START_RECORD";
	public static final String COM_VIASH_VOICE_ASSISTANT_START_ANSWER_ACTION = "com.viash.voice_assistant.ANSWER_ACTION";
	public static final String COM_VIASH_VOICE_ASSISTANT_START_RESTORE_RING = "com.viash.voice_assistant.RESTORE_RING";
	public static final String COM_VIASH_VOICE_ASSISTANT_END_CALL = "com.viash.voice_assistant.END_CALL";
	public static final String COM_VIASH_VOICE_ASSISTANT_INCOMING_CALL = "com.viash.voice_assistant.INCOMING_CALL";
	protected String mAppName = "";
	protected ServerCommand mServerCommand = null;
	@SuppressWarnings("rawtypes")
	public static HashMap<String, Class> mAppMap = new HashMap<String, Class>();

	public AppData(Context context, JSONObject obj) {
		super();
		mParseResult = parseFromJson(context, obj);
	}
	
	public String getAppName() {
		return mAppName;
	}

	public ServerCommand getServerCommand()
	{
		return mServerCommand;
	}
	public static void initAppMap() {
		mAppMap.put(APP_NAME_PHONE, PhoneCommand.class);
		mAppMap.put(APP_NAME_SMS, SendSMSCommand.class);
		mAppMap.put(APP_NAME_MUSIC, PlayMusicCommand.class);
		mAppMap.put(APP_NAME_OPEN_APP, OpenAppCommand.class);
		mAppMap.put(APP_NAME_OPEN_WEB, OpenWebCommand.class);
		mAppMap.put(APP_NAME_SEND_WEIBO, OpenWeiboCommand.class);
		mAppMap.put(APP_NAME_SEND_WEIXIN, OPenWeixinCommand.class);
		mAppMap.put(APP_NAME_SEND_RENREN, OPenRenrenCommand.class);
		mAppMap.put(APP_NAME_CALENDAR, AddCalendarCommand.class);
		mAppMap.put(APP_NAME_DELETE_CALENDAR_BY_ID, DeleteCalendarCommand.class);
		mAppMap.put(APP_NAME_MODIFY_CALENDAR_BY_ID, AddCalendarCommand.class);
		mAppMap.put(APP_NAME_SWITCH_MOBILE_FUNCTION,
				SwitchMobileFunctionCommand.class);
		mAppMap.put(APP_NAME_SWITCH_PAIR_BLUETOOTH, PaireBluetoothCommand.class);
		mAppMap.put(APP_NAME_READ_SMS, ReadSmsCommand.class);
		mAppMap.put(APP_NAME_MAP, OpenMapCommand.class);
		mAppMap.put(APP_NAME_ROUTE_MAP, OpenRouteMapCommand.class);
		mAppMap.put(APP_NAME_NAVIGATION, NavigationCommand.class);
		mAppMap.put(APP_NAME_SEND_EMAIL, SendEmailCommand.class);
		mAppMap.put(APP_NAME_RECEIVE_EMAIL, ReceiveEmailCommand.class);
		mAppMap.put(APP_NAME_SHARE_FILE, ShareFileCommand.class);
		mAppMap.put(APP_NAME_ADD_CONTACT, AddContactCommand.class);
		mAppMap.put(APP_NAME_DEL_CONTACT, DeleteContactCommand.class);
		mAppMap.put(APP_NAME_CREATE_ALARM, CreateAlarmCommand.class);
		mAppMap.put(APP_NAME_DELETE_ALARM_BY_ID, DeleteAlarmCommand.class);
		mAppMap.put(APP_NAME_CREATE_MEMO, CreateMemoCommand.class);
		mAppMap.put(APP_NAME_DELETE_MEMO, DeleteMemoCommand.class);
		mAppMap.put(APP_NAME_OPEN_CALENDAR, OpenCalendarCommand.class);
		mAppMap.put(APP_NAME_STOP_MUSIC, StopMusicCommand.class);
		mAppMap.put(APP_NANE_CONTROL_MUSIC, ControlMusicCommand.class);
		mAppMap.put(APP_NANE_DOWNLOAD, DownloadApp.class);
		mAppMap.put(APP_NANE_ADD_POSITION_ALARM, AddPositionAlarmCommand.class);
		mAppMap.put(APP_NANE_DEL_POSITION_ALARM, DelPositionAlarmCommand.class);
		mAppMap.put(APP_NANE_PLAY_VIDEO, PlayVideoCommand.class);
		mAppMap.put(APP_NANE_SAVE_SMS, SaveSmsCommand.class);
		mAppMap.put(APP_NANE_SEND_WEIXIN, SendWeixinCommand.class);
		mAppMap.put(APP_NANE_SEND_RENREN, SendRenrenCommand.class);
		mAppMap.put(APP_NAME_HANDLE_INCOMING_CALL,
				HandleIncomingCallCommand.class);// Leo
		mAppMap.put(APP_NAME_TAKE_PHOTO, TakePhotoCommand.class);
		mAppMap.put(APP_NAME_ALARM_SNOOZE, Alarm.class);
		mAppMap.put(APP_NAME_REMIND_SNOOZE,Remind.class);
		mAppMap.put(APP_NAME_HA_CONTROL, HAControlCommand.class);
	}

	static {
		initAppMap();
	}

	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		//boolean ret = false;

		String appName = JsonUtil.optString(obj, "App Name", null);
		if (appName != null) {
			Class<?> clsCommand = mAppMap.get(appName);
			if (clsCommand != null) {
				try {
					Constructor<?>[] constructor = clsCommand
							.getDeclaredConstructors();
					mServerCommand = (ServerCommand) constructor[0]
							.newInstance(this);
					mParseResult = mServerCommand.parse(context, obj);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} else {
				Log.e(TAG, appName + "是不支持的命令!");
			}
		}
		return mParseResult;
	}

	public boolean doAction(Context context, Handler handler) {
		boolean ret = false;
		if (mServerCommand != null) {
			ret = mServerCommand.action(context, handler);
		}

		return ret;
	}

	public String getActionResult(Context context) {
		String ret = null;
		if (mServerCommand != null) {
			ret = mServerCommand.getActionResult(context);
		}
		return ret;
	}

	public class ServerCommand {
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		public boolean action(Context context, Handler handler) {
			return false;
		}

		public String getActionResult(Context context) {
			return null;
		}
	}

	public class PhoneCommand extends ServerCommand {
		String mPhoneNumber = null;
		int	mHangup = -1;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mPhoneNumber = JsonUtil.optString(obj, "PhoneNumber", null);
			mHangup = JsonUtil.optInt(obj, "hang_up", -1);
			if (mPhoneNumber != null) {
				mPhoneNumber = mPhoneNumber.trim();
				// String format = ResourceUtil.getString(R.string.dial_hint);
				// mDisplayStr = String.format(format, mPhoneNumber);
				mDisplayStr = null;
				mTtsStr = null;
				ret = true;
			}
			if (mHangup == 0) {				
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mPhoneNumber != null) {
				Message msg = handler.obtainMessage(MsgConst.MSG_CALL_START);
				msg.obj = mPhoneNumber;
				handler.sendMessage(msg);
				ret = true;
			}
			if (mHangup == 0) {
				Message msg = handler.obtainMessage(MsgConst.MSG_CALL_END);
				handler.sendMessage(msg);
				Message handlerMsg = handler.obtainMessage(MsgConst.CLIENT_ACTION_VIEW_HANDLER_MSG);
				handlerMsg.obj = PhoneCommand.this;
				handler.sendMessage(handlerMsg);
				ret = true;
			}

			return ret;
		}
		
		public String getPhoneNumber()
		{
			return mPhoneNumber;
		}

	}

	public class SendSMSCommand extends ServerCommand {
		String[] mPhoneNumber = null;
		String mSmsContent = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			// mPhoneNumber = obj.optString("PhoneNumber", null);
			JSONArray array = obj.optJSONArray("PhoneNumber");
			if (array != null) {
				mPhoneNumber = new String[array.length()];
				mSmsContent = JsonUtil.optString(obj, "Content", null);

				if (mPhoneNumber != null && mSmsContent != null) {
					for (int i = 0; i < array.length(); i++) {
						mPhoneNumber[i] = array.optString(i, null);
					}

					mDisplayStr = null;
					mTtsStr = null;
					ret = true;
				}
			}

			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mPhoneNumber != null && mSmsContent != null
					&& mSmsContent.length() > 0) {
				SmsManager smsManager = SmsManager.getDefault();
				ArrayList<String> lstContent = smsManager
						.divideMessage(mSmsContent);
				ArrayList<PendingIntent> pendingIntens = new ArrayList<PendingIntent>();

				for (int j = 0; j < mPhoneNumber.length; j++) {
					if (mPhoneNumber[j].length() > 0) {
						CustomToast.makeToast(context,
								"正在给" + mPhoneNumber[j] + "发送短信");//,
								//Toast.LENGTH_LONG).show();
						for (int i = 0; i < lstContent.size(); i++) {
							PendingIntent pendingIntent = null;
							if (i == lstContent.size() - 1) {
								Intent intent = new Intent();
								intent.setAction(SENT_SMS_ACTION);
								intent.putExtra(SENT_SMS_NUMBER,
										mPhoneNumber[j]);
								intent.putExtra(SENT_SMS_CONTENT, mSmsContent);
								pendingIntent = PendingIntent.getBroadcast(
										context, 0, intent,
										PendingIntent.FLAG_ONE_SHOT);
							}
							pendingIntens.add(pendingIntent);
						}
						smsManager.sendMultipartTextMessage(mPhoneNumber[j],
								null, lstContent, pendingIntens, null);
					}
				}

				ret = true;
			}
			return ret;
		}
	}

	public class PlayMusicCommand extends ServerCommand {
		String[] mid = null;
		String[] mTitles = null;
		String[] mUrls = null;
		String[] mArtists = null;
		String[] mPhoto = null;
		String[] mTime = null;
		String[] mAlbum = null;
		int not_auto_start;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			JSONArray arrayId = obj.optJSONArray("id");
			JSONArray arrayTitles = obj.optJSONArray("titles");
			JSONArray arrayUrls = obj.optJSONArray("urls");
			JSONArray arrayArtists = obj.optJSONArray("artists");
			JSONArray arrayPhoto = obj.optJSONArray("photo");
			JSONArray arrayTime = obj.optJSONArray("time");
			JSONArray arrayAlbum = obj.optJSONArray("album");
			mAppName = AppData.APP_NAME_MUSIC;
			not_auto_start = obj.optInt("not_auto_start");

			if (arrayTitles != null && arrayUrls != null
					&& arrayTitles.length() > 0
					&& arrayTitles.length() == arrayUrls.length()) {
				if (arrayId != null)
					mid = new String[arrayId.length()];
				mTitles = new String[arrayTitles.length()];
				mUrls = new String[arrayUrls.length()];
				mArtists = new String[arrayTitles.length()];
				if (arrayPhoto != null)
					mPhoto = new String[arrayPhoto.length()];
				if (arrayTime != null)
					mTime = new String[arrayTime.length()];
				if (arrayAlbum != null)
					mAlbum = new String[arrayAlbum.length()];

				for (int i = 0; i < arrayTitles.length(); i++) {
					if (mid != null)
						mid[i] = String.valueOf(arrayId.optInt(i, 0));
					mTitles[i] = arrayTitles.optString(i, "");
					mUrls[i] = arrayUrls.optString(i, "");
					if (arrayArtists != null)
						mArtists[i] = arrayArtists.optString(i, "");
					if (arrayPhoto != null && mPhoto != null
							&& i < arrayPhoto.length())
						mPhoto[i] = arrayPhoto.optString(i, "");
					if (arrayTime != null && mTime != null && mTime.length > i)
						mTime[i] = arrayTime.optString(i, "0.0");
					if (arrayAlbum != null && mAlbum != null
							&& mAlbum.length > i)
						mAlbum[i] = arrayAlbum.optString(i, "");
				}
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mUrls != null && mTitles != null && mUrls.length > 0) {
				List<String[]> lstData = new ArrayList<String[]>();
				lstData.add(mid);
				lstData.add(mTitles);
				lstData.add(mArtists);
				lstData.add(mUrls);
				lstData.add(mPhoto);
				lstData.add(mTime);
				lstData.add(mAlbum);
				String[] temp = { not_auto_start + "" };
				lstData.add(temp);
				Message msg = handler.obtainMessage(MsgConst.MSG_MUSIC_PLAY);
				msg.obj = lstData;
				handler.sendMessage(msg);
				ret = true;
			}
			return ret;
		}
	}

	public class OpenAppCommand extends ServerCommand {
		String mOpenAppName = null;
		String mOpenFixedAppName = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mOpenAppName = JsonUtil.optString(obj, "ExeName", null);
			if (mOpenAppName != null) {
				ret = true;
			} else {
				mOpenFixedAppName = JsonUtil.optString(obj, "FixedExeName",
						null);
				if (mOpenFixedAppName != null) {
					ret = true;
				}
			}

			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mOpenAppName != null) {
				ret = AppUtil.launchApp(context, mOpenAppName, true);
				if (!ret)

					CustomToast.makeToast(context,
							"无法开启\"" + mOpenAppName + "\"");
					//Toast.makeText(context, "无法开启\"" + mOpenAppName + "\"",
						//	Toast.LENGTH_SHORT).show();
			} else if (mOpenFixedAppName != null) {
				ret = AppUtil.launchFixedApp(context, mOpenFixedAppName);
				if (!ret)
					CustomToast.makeToast(context,
							"无法开启\"" + mOpenFixedAppName + "\"");//,
							//Toast.LENGTH_SHORT).show();
			}

			if(ret)
				handler.sendEmptyMessage(MsgConst.MSG_JUMP_TO_NEW_APP);
			return ret;
		}

	}

	public class OpenWebCommand extends ServerCommand {
		String mUrl = null;
		boolean mEmbeded = false;
		boolean mDisableJavaScript = false;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mUrl = JsonUtil.optString(obj, "URL", null);
			if (JsonUtil.optInt(obj, "Embed", 0) == 1)
				mEmbeded = true;
			if (JsonUtil.optInt(obj, "DisableJavaScript", 0) == 1)
				mDisableJavaScript = true;
			if (mUrl != null) {
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mUrl != null) {
				if (!mEmbeded) {
					Message msg = handler.obtainMessage(
							MsgConst.MSG_SHOW_WEB,
							(mDisableJavaScript ? 1 : 0), 0);
					msg.obj = mUrl;
					handler.sendMessage(msg);
				} else {
					Message msg = handler.obtainMessage(
							MsgConst.MSG_SHOW_INTERNAL_WEB,
							(mDisableJavaScript ? 1 : 0), 0);
					msg.obj = mUrl;
					handler.sendMessage(msg);
				}
			}

			return ret;
		}

	}

	public class OpenWeiboCommand extends ServerCommand {
		String mContent = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mContent = JsonUtil.optString(obj, "Content", null);
			if (mContent != null) {
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			ret = AppUtil.launchApp(context, "微博", false);
			if (!ret)
				CustomToast.makeToast(context, "无法开启\"" + "微博" + "\"");//,
						//Toast.LENGTH_SHORT).show();
			return ret;
		}

	}

	public class AddCalendarCommand extends ServerCommand {
		/*protected EventData mCalendarData = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;

			mCalendarData = new EventData();
			mCalendarData.setmEventId(JsonUtil.optLong(obj, "id", 0));
			mCalendarData.setmTitle(JsonUtil.optString(obj, "Title", null));
			mCalendarData.setmLocation(JsonUtil
					.optString(obj, "Location", null));
			mCalendarData.setmDescription(JsonUtil.optString(obj,
					"Description", null));
			mCalendarData
					.setmHasAlarm(JsonUtil.optInt(obj, "HasAlarm", 1) == 1);
			mCalendarData.setmIsAllDay(JsonUtil.optInt(obj, "AllDay", 0) == 1);
			mCalendarData.setmAdvanceTime(JsonUtil
					.optInt(obj, "AdvanceTime", 0));
			mCalendarData.setmStart(JsonUtil.optLong(obj, "StartTime", 0));
			mCalendarData.setmAvailability(JsonUtil.optInt(obj, "Availability",
					0));
			long duration = JsonUtil.optLong(obj, "Duration", 0);
			if (duration == 0) {
				duration = 60000;
				mCalendarData.setmAvailability(1);
			}
			mCalendarData.setmDuration(duration);

			JSONArray array = obj.optJSONArray("attendees");
			if (array != null) {
				List<String> lstAttendee = new ArrayList<String>();
				for (int i = 0; i < array.length(); i++) {
					lstAttendee.add(array.optString(i));
				}
				mCalendarData.setmLstAttendee(lstAttendee);
			}

			JSONObject objRepeat = JsonUtil.optJsonObj(obj, "RepeatRule");
			if (objRepeat != null) {
				mCalendarData.setmIsRepeated(true);

				String rule = "";
				String value = objRepeat.optString("FREQ", null);
				if (value != null)
					rule += "FREQ=" + value;

				value = objRepeat.optString("BYDAY", null);
				if (value != null) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYDAY=" + value;
				}

				int nValue = objRepeat.optInt("BYMONTHDAY", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYMONTHDAY=" + nValue;
				}

				nValue = objRepeat.optInt("BYMONTH", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYMONTH=" + nValue;
				}

				nValue = objRepeat.optInt("INTERVAL", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "INTERVAL=" + nValue;
				}

				Date dateUntil = null;

				String sUntil = objRepeat.optString("UNTIL", null);
				if (sUntil != null) {
					try {
						dateUntil = new Date(Long.parseLong(sUntil));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}

				if (dateUntil == null) {
					dateUntil = new Date(mCalendarData.getmStart());
					dateUntil.setYear(dateUntil.getYear() + 5);
				}

				if (dateUntil != null) {
					mCalendarData.setmUntil(dateUntil.getTime());

					if (rule.length() > 0)
						rule += ";";
					rule += "UNTIL="
							+ String.format("%04d%02d%02dT%02d%02d%02dZ",
									dateUntil.getYear() + 1900,
									dateUntil.getMonth() + 1,
									dateUntil.getDate(), dateUntil.getHours(),
									dateUntil.getMinutes(),
									dateUntil.getSeconds());
				}

				mCalendarData.setmRule(rule);
			}

			ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;

			if (mCalendarData != null) {
				if (mCalendarData.getmEventId() == 0)
					ret = CalendarUtil.addCalendarEvent(context, mCalendarData);
				else
					ret = CalendarUtil.modifyCalendarEvent(context,
							mCalendarData);
			}

			return ret;
		}*/
		
		protected EventData mCalendarData = null;
		
		protected long mMinutes = 0;
		protected String rTime = "";
		protected int mRepeat = 0;
		protected String mTitle = null;
		protected boolean mVibrate = false;
		protected boolean mEnabled = true;
		protected int tab = 0;
		protected int mId = 0;
		

		@Override
		public boolean parse(Context context, JSONObject obj) {
			//system calendar reminder start
			boolean ret = false;

			mCalendarData = new EventData();
			mCalendarData.setmEventId(JsonUtil.optLong(obj, "id", 0));
			mCalendarData.setmTitle(JsonUtil.optString(obj, "Title", null));
			mCalendarData.setmLocation(JsonUtil
					.optString(obj, "Location", null));
			mCalendarData.setmDescription(JsonUtil.optString(obj,
					"Description", null));
			mCalendarData
					.setmHasAlarm(JsonUtil.optInt(obj, "HasAlarm", 1) == 1);
			mCalendarData.setmIsAllDay(JsonUtil.optInt(obj, "AllDay", 0) == 1);
			mCalendarData.setmAdvanceTime(JsonUtil
					.optInt(obj, "AdvanceTime", 0));
			mCalendarData.setmStart(JsonUtil.optLong(obj, "StartTime", 0));
			mCalendarData.setmAvailability(JsonUtil.optInt(obj, "Availability",
					0));
			long duration = JsonUtil.optLong(obj, "Duration", 0);
			if (duration == 0) {
				duration = 60000;
				mCalendarData.setmAvailability(1);
			}
			mCalendarData.setmDuration(duration);

			JSONArray array = obj.optJSONArray("attendees");
			if (array != null) {
				List<String> lstAttendee = new ArrayList<String>();
				for (int i = 0; i < array.length(); i++) {
					lstAttendee.add(array.optString(i));
				}
				mCalendarData.setmLstAttendee(lstAttendee);
			}

			JSONObject objRepeat = JsonUtil.optJsonObj(obj, "RepeatRule");
			if (objRepeat != null) {
				mCalendarData.setmIsRepeated(true);

				String rule = "";
				String value = objRepeat.optString("FREQ", null);
				if (value != null)
					rule += "FREQ=" + value;

				value = objRepeat.optString("BYDAY", null);
				if (value != null) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYDAY=" + value;
				}

				int nValue = objRepeat.optInt("BYMONTHDAY", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYMONTHDAY=" + nValue;
				}

				nValue = objRepeat.optInt("BYMONTH", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "BYMONTH=" + nValue;
				}

				nValue = objRepeat.optInt("INTERVAL", 0);
				if (nValue != 0) {
					if (rule.length() > 0)
						rule += ";";
					rule += "INTERVAL=" + nValue;
				}

				Date dateUntil = null;

				String sUntil = objRepeat.optString("UNTIL", null);
				if (sUntil != null) {
					try {
						dateUntil = new Date(Long.parseLong(sUntil));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}

				if (dateUntil == null) {
					dateUntil = new Date(mCalendarData.getmStart());
					dateUntil.setYear(dateUntil.getYear() + 5);
				}

				if (dateUntil != null) {
					mCalendarData.setmUntil(dateUntil.getTime());

					if (rule.length() > 0)
						rule += ";";
					rule += "UNTIL="
							+ String.format("%04d%02d%02dT%02d%02d%02dZ",
									dateUntil.getYear() + 1900,
									dateUntil.getMonth() + 1,
									dateUntil.getDate(), dateUntil.getHours(),
									dateUntil.getMinutes(),
									dateUntil.getSeconds());
				}

				mCalendarData.setmRule(rule);
			}
			ret = true;
			//system calendar reminder end
			
			//local calendar reminder start
			mId = JsonUtil.optInt(obj, "id", 0);
			mMinutes = JsonUtil.optLong(obj, "StartTime", 0);
			rTime = mMinutes+"";
			mRepeat = 0;
			mTitle = JsonUtil.optString(obj, "Title", "");
			mVibrate = true;
			mEnabled = true;
			tab = 2;//calenadar:2 clock:1
			
		Calendar  calendar = Calendar.getInstance();
		Date date = new Date(mMinutes);
		//Log.i(TAG, date.toString() + "" + calendar.get(Calendar.HOUR_OF_DAY));
		calendar.setTime(date);
		//date.getHours();
		mMinutes = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
			Log.i(TAG, mMinutes+"  创建提醒的基本信息："+obj.toString());
			return true;
		//local calendar reminder end	
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			
			if (mCalendarData != null) {
				if (mCalendarData.getmEventId() == 0)
				{
					ret = CalendarUtil.addCalendarEvent(context, mCalendarData);
					if(ret == false)
					{
						return AlarmUtil.addAlarm(context, (int)mMinutes, mRepeat, mTitle,
								mVibrate, mEnabled,rTime,tab);
					}
						
				}
				else
				{	
					ret = CalendarUtil.modifyCalendarEvent(context,
							mCalendarData);
					if(ret == false)
					{
						return AlarmUtil.modifyAlarm(context, mId, (int)mMinutes, mRepeat,
								mTitle, mVibrate, mEnabled,rTime,tab);
					}
				}
			}
			else
			{
				if (mId == 0){
					return AlarmUtil.addAlarm(context, (int)mMinutes, mRepeat, mTitle,
							mVibrate, mEnabled,rTime,tab);
				}else{
					return AlarmUtil.modifyAlarm(context, mId, (int)mMinutes, mRepeat,
							mTitle, mVibrate, mEnabled,rTime,tab);
				}
			}
			return true;
		}
	}

	public class OPenWeixinCommand extends ServerCommand {

		@Override
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}

	}

	public class OPenRenrenCommand extends ServerCommand {
		@Override
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}
	}

	public class DeleteCalendarCommand extends ServerCommand {
		private long[] mIds = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			JSONArray array = obj.optJSONArray("ids");
			if (array != null && array.length() > 0) {
				mIds = new long[array.length()];
				for (int i = 0; i < array.length(); i++) {
					mIds[i] = array.optLong(i);
				}
				ret = true;
			}

			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mIds.length > 0) {
				List<InstanceData> lstCalendar = CalendarUtil.queryCalendar(context, 0, 0);
				if(lstCalendar == null)
				   AlarmUtil.deleteAlarm(context, mIds);
				else if(lstCalendar != null)
				{
					if(lstCalendar.size() > 0)
					   CalendarUtil.deleteEvents(context, mIds);
				}
			}
			return ret;
		}

	}

	public class SwitchMobileFunctionCommand extends ServerCommand {
		private final String[] mFunctionTypes = { "bluetooth",
				"bluetooth_visible", "gps", "mute", "vibrate", "wifi",
				"brightness", "volume" ,"data_connection_all","2g3g"};

		protected int mFunctionIndex = -1;
		protected boolean mEnabled = false;
		protected int brightnessPercent = -1;
		private int typeValue = -1;
		private int current_percent=-1;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			String funtionType = JsonUtil.optString(obj, "function_type", null);
			if (funtionType != null) {
				for (int i = 0; i < mFunctionTypes.length; i++) {
					if (mFunctionTypes[i].equalsIgnoreCase(funtionType)) {
						mFunctionIndex = i + 1;
						mEnabled = (JsonUtil.optInt(obj, "enabled", 0) == 1);
						brightnessPercent = JsonUtil.optInt(obj, "percentage",
								-1);
						typeValue = JsonUtil.optInt(obj, "typevalue",	-1);
						current_percent = JsonUtil.optInt(obj, "current_percent",	-1);
						ret = true;
						break;
					}
				}

			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;

			if (mFunctionIndex > 0) {
				switch (mFunctionIndex - 1) {
				case 0:
					if (mEnabled)
						HBluetooth.openBluetooth();
					else
						HBluetooth.closeBluetooth();
					break;
				case 1:
					HBluetooth.setVisible(context, mEnabled);
					break;
				case 2:
					HGPS.openGPSSetting(context);
					break;
				case 3:
					if (mEnabled)
						HModelSetting.closeRing(context);
					else
						HModelSetting.openRing(context);
					break;
				case 4:
					if (mEnabled)
						HModelSetting.openVibrate(context);
					else
						HModelSetting.closeVibrate(context);
					break;
				case 5:
					HNet net = new HNet(context);
					if (mEnabled)
						net.openWifi();
					else
						net.closeWifi();
					break;
				case 6:

					if (context instanceof Activity) {
						Activity activity = (Activity) context;
						if (brightnessPercent == -1) {
							if (mEnabled)
								HScreenBrightness.addBrightness(activity);
							else
								HScreenBrightness.minusBrightness(activity);
						} else {
							HScreenBrightness.setParcentBrightness(activity,
									brightnessPercent);
						}
					} else {
						if (brightnessPercent == -1) {
							if (mEnabled)
								HScreenBrightness.addBrightness();
							else
								HScreenBrightness.minusBrightness();
						} else {
							HScreenBrightness
									.setParcentBrightness(brightnessPercent);
						}
					}
					break;
				case 7:
					HVolume.setVolumeType(typeValue);
					if (current_percent == -1 && brightnessPercent != -1) {
						HVolume.setPercentVolume(context, brightnessPercent);
					} else if (current_percent == -1 && brightnessPercent == -1) {
						if (mEnabled) {
							HVolume.addVolume(context);
						} else {
							HVolume.minusVolume(context);
						}
					} else {
						HVolume.setPercentCurrentVolume(context,
								current_percent);
					}
					break;
				case 8:
					HNet hNet = new HNet(context);
					if(mEnabled)
						hNet.openNetWork();
					else
						hNet.closeNetwork();
					break;
				case 9:
					HNet hNet2 = new HNet(context);
					if(mEnabled)
						hNet2.openMobileDataNetwork();
					else
//						hNet2.closeMobileDataNetwork();
					break;
				default:
					break;
				}
			}
			return ret;
		}

	}

	public class PaireBluetoothCommand extends ServerCommand {
		protected String mRemoteName = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mRemoteName = JsonUtil.optString(obj, "remote_name", null);
			if (mRemoteName != null) {
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mRemoteName != null) {
				ret = HBluetooth.requestPair(mRemoteName);
			}

			return ret;
		}

	}

	public class ReadSmsCommand extends ServerCommand {
		protected long mIds[] = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;

			JSONArray array = obj.optJSONArray("ids");
			if (array != null) {
				mIds = new long[array.length()];
				for (int i = 0; i < array.length(); i++) {
					mIds[i] = array.optLong(i, 0);
				}

				mDisplayStr = null;
				mTtsStr = String.format("有%d个短信。", array.length());
				mDisplayStr = String.format("有%d个短信。<p>", array.length());
				List<SmsData> lstSms = CallUtil.querySms(context, mIds);
				if (lstSms != null) {
					int index = 1;
					for (SmsData sms : lstSms) {
						String name = sms.getmContactName();
						if (name == null || name.length() == 0)
							name = sms.getmContactPhone();

						Date date = new Date(sms.getmTime());
						String sDate = String.format("%d-%d %d:%d",
								date.getMonth() + 1, date.getDate(),
								date.getHours(), date.getMinutes());
						String sDateTts = String.format("%d月%d日 %d点%d分",
								date.getMonth() + 1, date.getDate(),
								date.getHours(), date.getMinutes());

						mTtsStr += "短信" + index + ":|*" + index + "*|";
						mDisplayStr += "短信" + index + "(" + sDate + ")<p>";

						if (sms.getmType() == PhoneData.TYPE_SENT) {
							mTtsStr += sDateTts + "发给" + name + ":"
									+ sms.getmContent() + "。";
							mDisplayStr += "收件人:" + name + "<p>";
							mDisplayStr += "内容:" + sms.getmContent() + "<p>";
						} else if (sms.getmType() == SmsData.TYPE_READ) {
							mTtsStr += sDateTts + "来自" + name + ":"
									+ sms.getmContent() + "。";
							mDisplayStr += "发件人:" + name + "<p>";
							mDisplayStr += "内容(已读):" + sms.getmContent()
									+ "<p>";
						} else {
							mTtsStr += sDateTts + "来自" + name + "(未读):"
									+ sms.getmContent() + "。";
							mDisplayStr += "发件人:" + name + "<p>";
							mDisplayStr += "内容(未读):" + sms.getmContent()
									+ "<p>";
						}

						index++;
					}
				}
				ret = true;
			}

			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return true;
		}
	}

	public static class MapInfo {
		public double mLatitude = 0;
		public double mLongitude = 0;
		public String mAddress = null;
		// protected long mRouteIds[] = null;
		public String[] mPoiId = null;
		public String[] mPoiTitle = null;
		public String[] mPoiSnippet = null;
		public double[] mPoiLongitude = null;
		public double[] mPoiLatitude = null;
		public double[] mRouteFromLongitude = null;
		public double[] mRouteFromLatitude = null;
		public double[] mRouteToLongitude = null;
		public double[] mRouteToLatitude = null;
		public long[] mRouteMode = null;

		public MapInfo() {

		}
	}

	public static class RouteMapInfo {
		public double[] mRouteFromLongitude = null;
		public double[] mRouteFromLatitude = null;
		public double[] mRouteToLongitude = null;
		public double[] mRouteToLatitude = null;
		public long[] mRouteMode = null;
	}

	public class OpenMapCommand extends ServerCommand {
		protected MapInfo mMapInfo = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mMapInfo = new MapInfo();
			mMapInfo.mLongitude = longToLatLngDouble(JsonUtil.optLong(obj, "longitude", 0));
			mMapInfo.mLatitude = longToLatLngDouble(JsonUtil.optLong(obj, "latitude", 0));
			mMapInfo.mAddress = JsonUtil.optString(obj, "address", null);
			mMapInfo.mPoiId = JsonUtil.optStringArray(obj, "poi_id");
			mMapInfo.mPoiTitle = JsonUtil.optStringArray(obj, "poi_title");
			mMapInfo.mPoiSnippet = JsonUtil.optStringArray(obj, "poi_snippet");
			mMapInfo.mPoiLongitude = longToLatLngDouble(JsonUtil
					.optLongArray(obj, "poi_longitude"));
			mMapInfo.mPoiLatitude = longToLatLngDouble(JsonUtil.optLongArray(obj, "poi_latitude"));

			mMapInfo.mRouteFromLongitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_from_longitude"));
			mMapInfo.mRouteFromLatitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_from_latitude"));
			mMapInfo.mRouteToLongitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_to_longitude"));
			mMapInfo.mRouteToLatitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_to_latitude"));
			mMapInfo.mRouteMode = JsonUtil.optLongArray(obj, "route_mode");

			ret = true;
			return ret;
		}
		
		private double longToLatLngDouble(long value) {
			return (double)value / 1000000;
		}
		
		
		private double[] longToLatLngDouble(long value[]) {
			if (value == null) {
				return null;
			}
			double[] doubleValues = new double[value.length];
			for (int i = 0; i < value.length; i ++) {
				doubleValues[i] = (double)value[i] / 1000000;
			}
			return doubleValues;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mMapInfo != null) {
				Message msg = handler.obtainMessage(MsgConst.MSG_SHOW_MAP);
				msg.obj = mMapInfo;
				handler.sendMessage(msg);
			}

			return ret;
		}
	}

	public class NavigationCommand extends ServerCommand {
		protected String mStart = null;
		protected String mEnd = null;
		protected String mMethod = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mStart = JsonUtil.optString(obj, "start", null);
			mEnd = JsonUtil.optString(obj, "end", null);
			mMethod = JsonUtil.optString(obj, "option", null);

			return (mEnd != null);
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}

		public String getActionResult(Context context) {
			return "导航功能还不支持！";
		}
	}

	public class SendEmailCommand extends ServerCommand {

		@Override
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}

	}

	public class ReceiveEmailCommand extends ServerCommand {

		@Override
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}

	}

	public class ShareFileCommand extends ServerCommand {

		@Override
		public boolean parse(Context context, JSONObject obj) {
			return false;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return false;
		}

	}

	public class CreateAlarmCommand extends ServerCommand {
		protected int mMinutes = 0;
		protected int mRepeat = 0;
		protected String mTitle = null;
		protected boolean mVibrate = false;
		protected boolean mEnabled = true;
		protected int mId = 0;
		protected int tab = 0;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mId = JsonUtil.optInt(obj, "id", 0);
			mMinutes = JsonUtil.optInt(obj, "time", 0);
			mRepeat = JsonUtil.optInt(obj, "repeat", 0);
			mTitle = JsonUtil.optString(obj, "title", "");
			mVibrate = (JsonUtil.optInt(obj, "vibrate", 0) != 0);
			mEnabled = (JsonUtil.optInt(obj, "enabled", 0) != 0);
			tab = 1;
			Log.i(TAG, "  创建闹钟的基本信息："+obj.toString());
			return true;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			if (mId == 0)
				return AlarmUtil.addAlarm(context, mMinutes, mRepeat, mTitle,
						mVibrate, mEnabled,"",tab);
			else
				return AlarmUtil.modifyAlarm(context, mId, mMinutes, mRepeat,
						mTitle, mVibrate, mEnabled,"",tab);
		}
	}

	public class DeleteAlarmCommand extends ServerCommand {
		protected long mIds[] = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			JSONArray array = obj.optJSONArray("ids");
			if (array != null && array.length() > 0) {
				mIds = new long[array.length()];
				for (int i = 0; i < array.length(); i++) {
					mIds[i] = array.optLong(i);
				}
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			return AlarmUtil.deleteAlarm(context, mIds);
		}

	}

	public class CreateMemoCommand extends ServerCommand {
		String mContent = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mContent = JsonUtil.optString(obj, "content", null);
			if (mContent != null) {
				ret = true;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mContent != null) {
				MemoUtil.addMemo(context, mContent);
			}
			return ret;
		}

	}

	public class DeleteMemoCommand extends ServerCommand {
		protected String mContent = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mContent = JsonUtil.optString(obj, "content", null);
			return true;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mContent != null) {
				MemoUtil.deleteMemo(context, mContent);
			}
			return ret;
		}
	}

	public class AddContactCommand extends ServerCommand {
		ContactInfo mContactInfo = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mContactInfo = new ContactInfo();
			mContactInfo.setmId(JsonUtil.optInt(obj, "id", 0));
			JSONArray arrayName = obj.optJSONArray("display name");
			JSONArray arrayFirstName = obj.optJSONArray("first name");
			JSONArray arrayLastName = obj.optJSONArray("last name");
			JSONArray arrayMiddleName = obj.optJSONArray("middle name");
			if (arrayName != null) {
				for (int i = 0; i < arrayName.length(); i++) {
					String name = arrayName.optString(i, null);
					String firstName = null;
					if (arrayFirstName != null && arrayFirstName.length() > 0)
						firstName = arrayFirstName.optString(i, null);
					String lastName = null;
					if (arrayLastName != null && arrayLastName.length() > 0)
						lastName = arrayLastName.optString(i, null);
					String middleName = null;
					if (arrayMiddleName != null && arrayMiddleName.length() > 0)
						middleName = arrayMiddleName.optString(i, null);
					mContactInfo.addName(name, firstName, lastName, middleName);
				}
			}

			JSONArray arrayNickName = obj.optJSONArray("nick name");
			if (arrayNickName != null) {
				for (int i = 0; i < arrayNickName.length(); i++) {
					String nickName = arrayName.optString(i, null);
					if (nickName != null)
						mContactInfo.addNickName(nickName);
				}
			}

			JSONArray arrayCompany = obj.optJSONArray("organization_company");
			JSONArray arrayTitle = obj.optJSONArray("organization_title");
			if (arrayCompany != null) {
				for (int i = 0; i < arrayCompany.length(); i++) {
					String company = arrayCompany.optString(i, null);
					String title = arrayTitle.optString(i, null);
					mContactInfo.addCompany(company, title);
				}
			}

			addAddress(obj, "home address",
					ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);
			addAddress(obj, "work address",
					ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
			addAddress(
					obj,
					"other address",
					ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER);

			addPhone(obj, "home phone",
					ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
			addPhone(obj, "work phone",
					ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
			addPhone(obj, "mobile phone",
					ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
			addPhone(obj, "other phone",
					ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);

			addMail(obj, "private email",
					ContactsContract.CommonDataKinds.Email.TYPE_HOME);
			addMail(obj, "work email",
					ContactsContract.CommonDataKinds.Email.TYPE_WORK);

			addIM(obj, "msn", ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN);
			addIM(obj, "qq", ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ);

			return true;
		}

		protected void addAddress(JSONObject obj, String key, int type) {
			JSONArray arrayAddress = obj.optJSONArray(key);
			if (arrayAddress != null) {
				for (int i = 0; i < arrayAddress.length(); i++) {
					String address = arrayAddress.optString(i, null);
					if (address != null)
						mContactInfo.addAddress(type, null, address);
				}
			}
		}

		protected void addIM(JSONObject obj, String key, int type) {
			JSONArray arrayIM = obj.optJSONArray(key);
			if (arrayIM != null) {
				for (int i = 0; i < arrayIM.length(); i++) {
					String im = arrayIM.optString(i, null);
					if (im != null) {
						if (type == ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN)
							mContactInfo.setmMsn(im);
						else if (type == ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ)
							mContactInfo.setmQQ(im);
					}
				}
			}
		}

		protected void addMail(JSONObject obj, String key, int type) {
			JSONArray arrayMail = obj.optJSONArray(key);
			if (arrayMail != null) {
				for (int i = 0; i < arrayMail.length(); i++) {
					String mail = arrayMail.optString(i, null);
					if (mail != null)
						mContactInfo.addEmail(type, null, mail);
				}
			}
		}

		protected void addPhone(JSONObject obj, String key, int type) {
			JSONArray arrayPhone = obj.optJSONArray(key);
			if (arrayPhone != null) {
				for (int i = 0; i < arrayPhone.length(); i++) {
					String address = arrayPhone.optString(i, null);
					if (address != null)
						mContactInfo.addPhone(type, null, address);
				}
			}
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mContactInfo != null) {
				if (mContactInfo.getmId() > 0) {
					long id = mContactInfo.getmId();
					long newId = ContactUtil.modifyContact(context,
							mContactInfo);
					if (newId != 0 && newId != id) {
						Message msg = handler
								.obtainMessage(MsgConst.MSG_CONTACT_MODIFIED);
						msg.arg1 = (int) id;
						msg.arg2 = (int) newId;
						handler.sendMessage(msg);
					}
				} else {
					long id = ContactUtil.addContact(context, mContactInfo);
					if (id > 0) {
						Message msg = handler
								.obtainMessage(MsgConst.MSG_CONTACT_ADDED);
						msg.arg1 = (int) id;
						handler.sendMessage(msg);
					}
				}

			}
			return ret;
		}

	}

	public class DeleteContactCommand extends ServerCommand {
		protected int mId = 0;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mId = JsonUtil.optInt(obj, "id", 0);
			return mId > 0;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mId > 0) {
				ret = ContactUtil.delContact(context, mId);
				if (ret) {
					Message msg = handler
							.obtainMessage(MsgConst.MSG_CONTACT_DELETED);
					msg.arg1 = (int) mId;
					handler.sendMessage(msg);
				}
			}
			return ret;
		}

	}

	public class OpenCalendarCommand extends ServerCommand {
		protected int mYear = 0;
		protected int mMonth = 0;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			mMonth = JsonUtil.optInt(obj, "month", 0);
			mYear = JsonUtil.optInt(obj, "year", 0);
			return (mMonth != 0 && mYear != 0);
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mMonth != 0 && mYear != 0)
				CalendarUtil.openCalendarApp(context, mYear, mMonth);
			return ret;
		}

	}

	public class StopMusicCommand extends ServerCommand {
		@Override
		public boolean parse(Context context, JSONObject obj) {
			return true;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			if (handler != null) {
				Message msg = handler.obtainMessage(MsgConst.MSG_MUSIC_STOP);
				handler.sendMessage(msg);
			}
			return true;
		}
	}

	public class OpenRouteMapCommand extends ServerCommand {
		protected RouteMapInfo mRouteInfo = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mRouteInfo = new RouteMapInfo();

			mRouteInfo.mRouteFromLongitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_from_longitude"));
			mRouteInfo.mRouteFromLatitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_from_latitude"));
			mRouteInfo.mRouteToLongitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_to_longitude"));
			mRouteInfo.mRouteToLatitude = longToLatLngDouble(JsonUtil.optLongArray(obj,
					"route_to_latitude"));
			mRouteInfo.mRouteMode = JsonUtil.optLongArray(obj, "route_mode");

			ret = true;
			return ret;
		}
		
		private double[] longToLatLngDouble(long value[]) {
			if (value == null) {
				return null;
			}
			double[] doubleValues = new double[value.length];
			for (int i = 0; i < value.length; i ++) {
				doubleValues[i] = (double)value[i] / 1000000;
			}
			return doubleValues;
		}


		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mRouteInfo != null) {
				Message msg = handler
						.obtainMessage(MsgConst.MSG_SHOW_ROUTE_MAP);
				msg.obj = mRouteInfo;
				handler.sendMessage(msg);
				ret = true;
			}

			return ret;
		}
	}

	public class ControlMusicCommand extends ServerCommand {
		protected String mCommand = null;
		protected int mIndex = 0;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mCommand = JsonUtil.optString(obj, "command", null);
			mIndex = JsonUtil.optInt(obj, "index", 0);
			if (mCommand != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			if (handler != null) {
				Message msg = handler.obtainMessage(MsgConst.MSG_MUSIC_CONTROL);
				msg.obj = mCommand;
				msg.arg1 = mIndex;
				handler.sendMessage(msg);
			}
			return true;
		}
	}

	public class DownloadApp extends ServerCommand {
		protected String url = null;
		protected String title = null;
		//private String size = null;
		//private String version = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			url = JsonUtil.optString(obj, "URL", null);
			title = JsonUtil.optString(obj, "Title", null);
			/*size = JsonUtil.optString(obj, "size", null);
			version = JsonUtil.optString(obj, "version", null);*/
			mAppName = AppData.APP_NANE_DOWNLOAD;
			if (url != null && title != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			Message msg = handler.obtainMessage(MsgConst.MSG_DOWNLOAD_APP);
			msg.obj = url;
			Bundle bundle = new Bundle();
			bundle.putString("title", title);
			/*bundle.putString("size", size);
			bundle.putString("version", version);*/
			msg.setData(bundle);
			handler.sendMessage(msg);
			return true;
		}
	}

	public class AddPositionAlarmCommand extends ServerCommand {
		protected int longitude;
		protected int latitude;
		protected int longitude_range;
		protected int latitude_range;
		protected String alarm_title;
		protected String position_name;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			longitude = JsonUtil.optInt(obj, "longitude", -1);
			latitude = JsonUtil.optInt(obj, "latitude", -1);
			longitude_range = JsonUtil.optInt(obj, "longitude_range", -1);
			latitude_range = JsonUtil.optInt(obj, "latitude_range", -1);
			alarm_title = JsonUtil.optString(obj, "alarm_title", null);
			position_name = JsonUtil.optString(obj, "position_name", null);
			if (longitude != -1 && latitude == -1 && alarm_title != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			Message msg = handler
					.obtainMessage(MsgConst.MSG_POSITION_ALARM_ADDED);
			Bundle bundle = new Bundle();
			bundle.putInt("longitude", longitude);
			bundle.putInt("latitude", latitude);
			bundle.putInt("longitude_range", longitude_range);
			bundle.putInt("latitude_range", latitude_range);
			bundle.putString("alarm_title", alarm_title);
			bundle.putString("position_name", position_name);
			msg.setData(bundle);
			handler.sendMessage(msg);
			return true;
		}
	}

	public class DelPositionAlarmCommand extends ServerCommand {
		protected long[] ids;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			ids = JsonUtil.optLongArray(obj, "ids");
			if (ids.length > 0)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (ids.length > 0) {
				Message msg = handler
						.obtainMessage(MsgConst.MSG_POSITION_ALARM_DELETED);
				msg.obj = ids;
				handler.sendMessage(msg);
				ret = true;
			}
			return ret;
		}
	}

	public class PlayVideoCommand extends ServerCommand {
		protected String mUrl = null;
		protected int mStartTime = 0;
		protected int mVideoPlayerType = 0;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mStartTime = JsonUtil.optInt(obj, "start_time", 0);
			mVideoPlayerType = JsonUtil.optInt(obj, "video_player", 0);
			mUrl = JsonUtil.optString(obj, "url", null);
			if (mUrl != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mUrl != null) {
				Message msg = handler.obtainMessage(MsgConst.MSG_PLAY_VIDEO,
						mVideoPlayerType, mStartTime);
				msg.obj = mUrl;
				handler.sendMessage(msg);
				ret = true;
			}
			return ret;
		}
	}

	public class SaveSmsCommand extends ServerCommand {
		protected String mNumber = null;
		protected String mContent = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mNumber = JsonUtil.optString(obj, "number", null);
			mContent = JsonUtil.optString(obj, "content", null);
			if (mNumber != null && mContent != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if (mNumber != null && mContent != null) {
				CallUtil.addSmsToInBox(context, mNumber, mContent);
			}
			return ret;
		}
	}

	public class SendWeixinCommand extends ServerCommand {
		protected int mTo;
		protected String mTitle = null;
		protected String mDescription = null;
		protected String mUrl = null;
		protected int mType;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mTo = JsonUtil.optInt(obj, "to", 0);
			mTitle = JsonUtil.optString(obj, "title", null);
			mDescription = JsonUtil.optString(obj, "description", null);
			mUrl = JsonUtil.optString(obj, "url", null);
			mType = JsonUtil.optInt(obj, "type", 0);
			if (mDescription != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;

			List<String> txtDatas = new ArrayList<String>();
			txtDatas.add(mTitle);
			txtDatas.add(mDescription);
			txtDatas.add(mUrl);

			Message msg = handler.obtainMessage(MsgConst.MSG_SEND_TO_WEIXIN,
					mTo, mType);
			msg.obj = txtDatas;
			handler.sendMessage(msg);

			return ret;
		}
	}

	public class SendRenrenCommand extends ServerCommand {
		protected int pTo;
		protected String pTitle = null;
		protected String pDescription = null;
		protected String pUrl = null;
		protected int pType;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			pTo = JsonUtil.optInt(obj, "to", 0);
			pTitle = JsonUtil.optString(obj, "title", null);
			pDescription = JsonUtil.optString(obj, "description", null);
			pUrl = JsonUtil.optString(obj, "url", null);
			pType = JsonUtil.optInt(obj, "type", 0);
			if (pDescription != null)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			List<String> txtDatas = new ArrayList<String>();
			txtDatas.add(pTitle);
			txtDatas.add(pDescription);
			txtDatas.add(pUrl);
			Message msg = handler.obtainMessage(MsgConst.MSG_SEND_TO_RENREN,
					pTo, pType);
			msg.obj = txtDatas;
			handler.sendMessage(msg);
			return ret;
		}
	}

	// Leo Begin
	public class HandleIncomingCallCommand extends ServerCommand {
		protected String action = null;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			Log.e(TAG, "HandleIncomingCallCommand()11 ");
			boolean ret = false;
			try {
				action = obj.getString("action");
				Log.e(TAG, "HandleIncomingCallCommand() action = " + action);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			Message msg = handler
					.obtainMessage(MsgConst.MSG_HANDLE_INCOMING_CALL);
			Bundle bundle = new Bundle();
			bundle.putString("action", action);
			Log.e(TAG, "action()11 action = " + action);
			msg.setData(bundle);
			handler.sendMessage(msg);
			ret = true;
			return ret;
		}
	}

	// Leo End

	public class TakePhotoCommand extends ServerCommand {
		protected int mPreview;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			mPreview = JsonUtil.optInt(obj, "preview", 1);
			if (mPreview >= 0)
				ret = true;
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			Message msg = handler.obtainMessage(MsgConst.MSG_TAKE_PHOTO,
					mPreview, 0);
			handler.sendMessage(msg);
			return ret;
		}
	}
	
	public class Alarm extends ServerCommand{
		private int id;
		private int time;
		private String command;
		private boolean enable = false;
		private boolean ret = true;
		private Context mContext;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			// TODO Auto-generated method stub
			mContext = context;
			id = JsonUtil.optInt(obj, "id",0);
			time = Integer.parseInt(JsonUtil.optString(obj, "time","-1"));
			command = JsonUtil.optString(obj, "command",null);
			if(id<=0 || time <= 0){
				Log.i(TAG, "-------->>>闹钟推迟id或time数据出错");
				ret = false;
			}else{
				enable = AlarmUtil.getAlarm(context.getContentResolver(), id).daysOfWeek.isRepeatSet();
			}

			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			// TODO Auto-generated method stub
			if(command != null && command.equals("snooze")){
				//dismiss();
				AlarmUtil.snooze(context,id, time,1);
			}else if(command != null && command.equals("modify")){
				/*dismiss();
				AlarmUtil.stop(id, false);*/
				boolean isFA = AlarmUtil.isFirstAlarm(id, context);
				if(isFA){
					AlarmUtil.sendMsgToServer();
				}else{
					Log.i(TAG,"---------->>>当前非当天第一个闹钟");
				}
				/*if(!enable)
					AlarmUtil.deleteAlarm(context, id);*/
			}else{
				//dismiss();
				/*if(!enable)
					AlarmUtil.deleteAlarm(context, id);*/
				ret = false;
				Log.e(TAG, "no command to do !");
			}
			return ret;
		}
		
		public void dismiss(){
			Intent intent = new Intent(AlarmUtil.ALARM_DISMISS_ALARM_WARN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.sendBroadcast(intent);
		}
	}
	
	public class Remind extends ServerCommand{
		private int id;
		private int time;
		private String command;
		private boolean ret = true;

		@Override
		public boolean parse(Context context, JSONObject obj) {
			// TODO Auto-generated method stub
			id = JsonUtil.optInt(obj, "id",0);
			time = Integer.parseInt(JsonUtil.optString(obj, "time",null));
			command = JsonUtil.optString(obj, "command",null);
			if(id<=0 || time <=0){
				Log.i(TAG, "---------->>>提醒id或time数据出错");
				ret = false;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			// TODO Auto-generated method stub

			if(command != null && command.equals("snooze")){
				AlarmUtil.snooze(context,id, time,2);
			}else{
				ret = false;
				Log.e(TAG, "no command to do !");
			}

			return ret;
		}

	}


	public class HAControlCommand extends ServerCommand{
		JSONObject mJsonObj;


		@Override
		public boolean parse(Context context, JSONObject obj) {
			boolean ret = false;
			if(JsonUtil.optString(obj, "dev_id", null) != null)
			{
				ret = true;
				mJsonObj = obj;
			}
			return ret;
		}

		@Override
		public boolean action(Context context, Handler handler) {
			boolean ret = false;
			if(mJsonObj != null)
			{
				Message msg = handler.obtainMessage(MsgConst.MSG_SEND_HA_CMD, mJsonObj);
				handler.sendMessage(msg);
				ret = true;
			}
			return ret;
		}

	}
}
