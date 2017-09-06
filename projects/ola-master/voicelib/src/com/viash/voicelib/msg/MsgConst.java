package com.viash.voicelib.msg;

public class MsgConst {
	public static final int TS_S_HANDSHAKE = 0;
	public static final int TS_S_REQLOGIN = 1;
	public static final int TS_S_RSPLOGIN = 2;
	public static final int TS_S_PROMPT = 3;
	public static final int TS_S_DUMP = 4;
	public static final int TS_S_QUERY = 5;
	public static final int TS_S_CHECK_STATUS = 6;
	
	public static final int TS_C_HANDSHAKE = 0;
	public static final int TS_C_ACCOUNT_INFO = 1;
	public static final int TS_C_PROMPT = 2;
	public static final int TS_C_LOGOFF = 3;
	public static final int TS_C_HEARTBEAT = 4;
	public static final int TS_C_DUMP = 5;
	public static final int TS_C_ANSWER = 6;
	public static final int TS_C_CHECK_STATUS = 10;
	
	public static final int STATE_SERVER_NOT_CONNECTED = 0;
	public static final int STATE_SERVER_CONNECTING = 1;
	public static final int STATE_SERVER_CONNECTED = 2;	
	
	public static final int WAKEUP_AUDIO_DISABLE = 0;
	public static final int WAKEUP_AUDIO_ENABLE_WAIT = 1;		// Need a startup sentence
	public static final int WAKEUP_AUDIO_ENABLE_NOWAIT = 2;		// Do not need a startup sentence
	
	public static final int UI_STATE_UNINIT = 0;
	public static final int UI_STATE_INITED = 1;
	public static final int UI_STATE_SPEAKING = 2;
	public static final int UI_STATE_RECOGNIZING = 3;
	
	public static final int MSG_DATA_FROM_SERVER = 1000;
	public static final int MSG_DATA_FROM_VOICE = 1001;	
	public static final int MSG_DATA_FROM_OPTION = 1002;
	public static final int MSG_DATA_FROM_TEXT = 1003;
	public static final int MSG_DATA_FROM_TEXT_SHARE = 1004;
	
	public static final int MSG_SERVER_CONNECTED = 1200;
	public static final int MSG_SERVER_DISCONNECTED = 1201;
	public static final int MSG_SERVER_CONNECTING = 1202;
	
	public static final int MSG_SERVER_SESSION_BROKEN = 1203;
	public static final int MSG_SERVER_NET_BROKEN = 1204;
	public static final int MSG_SERVER_DATA_ERROR = 1205;
	
	public static final int MSG_COPY_TEXT_FROM_ITEM = 1301;
	
	public static final int MSG_PROCESS_SERVER_QUERY_SUCCESSED = 1401;
	
	public static final int MSG_SERVER_NO_RESPONSE = 1501;
	
	public static final int MSG_BLUETOOTH_ACTION = 6000;
	public static final int MSG_MUSIC_PLAY = 6001;
	public static final int MSG_SHOW_MAP = 6002;
	public static final int MSG_ROUTE_SEARCH_RESULT = 6003;
	public static final int MSG_BLUETOOTH_FOUND_START = 6004;
	public static final int MSG_BLUETOOTH_FOUND = 6005;
	public static final int MSG_MUSIC_STOP = 6006;
	public static final int MSG_SHOW_ROUTE_MAP = 6007;
	public static final int MSG_MUSIC_CONTROL = 6008;
	public static final int MSG_SEARCH_POS = 6009;
	public static final int MSG_DOWNLOAD_APP = 6010;
	public static final int MSG_SHOW_ERROR = 6011;
	public static final int MSG_SHOW_INTERNAL_WEB = 6012;
	public static final int MSG_PLAY_VIDEO = 6013;
	public static final int MSG_FORCE_STOP_TTS = 6014;
	public static final int MSG_NAVIGATE_TO = 6015;
	public static final int MSG_SEND_TO_WEIXIN = 6016;	
	public static final int MSG_CHECK_TTS = 6017;
	public static final int MSG_SHOW_TIPS = 6018;
	public static final int MSG_TIPS_TIMEOUT = 6019;
	public static final int MSG_HANDLE_INCOMING_CALL = 6020;
	public static final int MSG_TAKE_PHOTO = 6021;
	public static final int MSG_CAMERA_OPERATION = 6022;
	public static final int MSG_CAMERA_RESTORE_TO_BEFORE = 6023;
	public static final int MSG_SHOW_WEB = 6024;
	public static final int MSG_CALL_START = 6025;
	public static final int MSG_CALL_END = 6026;
	public static final int MSG_ON_VIEW_TOUCH = 6027;
	public static final int MSG_SHOW_WHOLE_SCREEN = 6028;
	public static final int MSG_SHOW_WHOLE_SCREEN_CANCEL = 6029;
	public static final int MSG_SHOW_MAP_BUSINFO = 6030;
	public static final int MSG_SEND_TO_RENREN = 6031;
	public static final int MSG_JUMP_TO_NEW_APP = 6032;
	public static final int MSG_SEND_HA_CMD = 6033;

	public static final int MSG_CONTACT_ADDED = 6100;
	public static final int MSG_CONTACT_MODIFIED = 6101;
	public static final int MSG_CONTACT_DELETED = 6102;
	
	public static final int MSG_POSITION_ALARM_ADDED = 6200;
	public static final int MSG_POSITION_ALARM_DELETED = 6201;
	
	public static final int MSG_SIM_VOICE_BUTTON = 6301;
	public static final int MSG_APPLIST_CHANGED = 6302;
	//public static final int MSG_NO_RECORDING_SEND_WITH_TEXT = 6303; //same as MSG_DATA_FROM_TEXT remove
	public static final int MSG_START_CAPTURE = 6304;
	public static final int MSG_STOP_CAPTURE = 6305;
	public static final int MSG_START_CAPTURE_OFFLINE = 6306;
	
	public static final int MSG_UPDATE_VERSION_NEWVERSION = 7000;
	public static final int MSG_UPDATE_VERSION_NONEWVERSION = 7001;
	public static final int MSG_UPDATE_VERSION_NOSDCARD = 7002;
	public static final int MSG_UPDATE_VERSION_SERVERERROR = 7003;
	public static final int MSG_UPDATE_VERSION_DOWNLOADSTOP = 7004;
	public static final int MSG_UPDATE_VERSION_DOWNLOADUPDATE = 7005;
	public static final int MSG_UPDATE_VERSION_DOWNLOADSUCCESS = 7006;
	public static final int MSG_UPDATE_VERSION_HASUPDATE = 7007;
	public static final int MSG_UPDATE_VERSION_UPDATEFAIL = 7008;
	public static final int MSG_UPDATE_VERSION_DIRECTLY = 7009;
	
	public static final int MSG_DRAWER_UPDATE_SCROLLVIEW_LAYOUT = 8000;
	public static final int MSG_DRAWER_UPDATE_PAGE_LAYOUT = 8001;
	public static final int MSG_SENDCMD_PHONE_BINDED = 8010;
	public static final int MSG_SENDCMD_PHONE_NOT_BIND = 8011;
	public static final int MSG_SENDCMD_PHONE_BE_USED = 8012;
	public static final int MSG_SENDCMD_PHONE_NOT_BE_USED = 8013;
	public static final int MSG_SENDCMD_GET_CODE_SUCCESS = 8020;
	public static final int MSG_SENDCMD_GET_CODE_FAILURE = 8021;
	public static final int MSG_SENDCMD_GET_CODE_INTERVAL_LESS_THAN_60S = 8022;
	public static final int MSG_SENDCMD_SUBMIT_CODE_SUCCESS = 8030;
	public static final int MSG_SENDCMD_SUBMIT_CODE_VCODE_ERROR = 8031;
	public static final int MSG_SENDCMD_SUBMIT_CODE_ID_PWD_NOT_MATCHED = 8032;
	public static final int MSG_SENDCMD_SUBMIT_CODE_OTHER_ERROR = 8033;
	public static final int MSG_SENDCMD_REPEAT_CODE_SUCCESS = 8040;
	public static final int MSG_SENDCMD_REPEAT_CODE_FAILURE = 8041;
	public static final int MSG_SENDCMD_MODIFY_PWD_SUCCESS = 8050;
	public static final int MSG_SENDCMD_MODIFY_PWD_FAILURE = 8051;	
	

	/**
	 * Action ID
	 */
	public static final int SERVICE_ACTION_CLOSE_SPLASH = 10000;
	public static final int SERVICE_ACTION_SET_PROCESSING_STATE = 10001;
	public static final int SERVICE_ACTION_UPDATE_ADAPTER_DATA = 10002;
	public static final int SERVICE_ACTION_UPDATE_VOICE_VOLUME = 10003;
	public static final int SERVICE_ACTION_QUERY_WEIBO = 10004;
	public static final int SERVICE_ACTION_QUERY_MUSIC = 10005;
	public static final int SERVICE_ACTION_QUERY_POSITION_ALARM = 10006;
	public static final int SERVICE_ACTION_SERVER_RESPONSE = 10007;
	public static final int SERVICE_ACTION_CAPTURE_VIEW = 10008;
	public static final int SERVICE_ACTION_SDKCOMMAND_RESPONSE = 10009;
	public static final int SERVICE_ACTION_SERVER_DISCONNECTED = 10010;
	public static final int SERVICE_ACTION_SERVER_CONNECTED = 10011;
	public static final int SERVICE_ACTION_SERVER_CONNECTING = 10012;
	//public static final int SERVICE_ACTION_SERVER_NO_RECORDING_SEND_WITH_TEXT = 10013; //same as MSG_DATA_FROM_TEXT remove
	public static final int SERVICE_ACTION_SDK_QUERY_STATE = 10014;
	public static final int SERVICE_ACTION_TTS_PLAY_START = 10015;
	public static final int SERVICE_ACTION_TTS_PLAY_END = 10016;
	public static final int SERVICE_ACTION_SERVER_BROKEN = 10017;
	public static final int SERVICE_ACTION_CLOSE_HELP_GUIDE_VIEW = 10018;
	public static final int SERVICE_ACTION_SHOW_HELP_GUIDE = 10019;
	public static final int SERVICE_ACTION_SEND_FEEDBACK_SUCCESS = 10020;
	public static final int SERVICE_ACTION_SEND_FEEDBACK_FAILED = 10021;
	
	public static final int CLIENT_ACTION_REGISTER_CLIENT_MESSENGER = 11000;
	public static final int CLIENT_ACTION_UNREGISTER_CLIENT_MESSENGER = 11001;
	public static final int CLIENT_ACTION_INIT_COMMUNICATION = 11002;
	public static final int CLIENT_ACTION_SEND_DATA_TO_SERVER = 11003;
	public static final int CLIENT_ACTION_START_CAPTURE = 11004;
	public static final int CLIENT_ACTION_START_RECOGNITION = 11005;
	public static final int CLIENT_ACTION_PROCESS_SERVER_QUERY_SUCCESSED = 11006;
	public static final int CLIENT_ACTION_SELECTION_ANSWER = 11007;
	public static final int CLIENT_ACTION_STOP_TTS = 11008;
	public static final int CLIENT_ACTION_CAPTURE_VIEW_OK = 11009;
	public static final int CLIENT_ACTION_CREATE_FLOATING_WINDOW = 11010;
	public static final int CLIENT_ACTION_USER_LOGINED = 11011;
	public static final int CLIENT_ACTION_SET_WAKEUP_BY_AUDIO = 11012;
	public static final int CLIENT_ACTION_REENTRY_WAKEUP_BY_AUDIO = 11013;
	public static final int CLIENT_ACTION_REPORT_UI_INFO = 11014;
	public static final int CLIENT_ACTION_REMOVE_DATA = 11015;
	public static final int CLIENT_ACTION_ADD_DATA = 11016;
	public static final int CLIENT_ACTION_SHOW_MUSIC_LIST= 11017;
	public static final int CLIENT_ACTION_CLEAR_TALK_HISTORY = 11018;
	public static final int CLIENT_ACTION_CANCEL_RECORD = 11019;
	public static final int CLIENT_ACTION_ABORT_VR_BY_PHONE_OR_SMS = 11020;
	public static final int CLIENT_ACTION_START_TTS = 11021;
	public static final int CLIENT_ACTION_VIEW_HANDLER_MSG = 11022;	
	public static final int CLIENT_ACTION_SHOW_NEW_ICON_ON_PROMO_BUTTON = 11023;
	public static final int CLIENT_ACTION_WELCOME = 11024;
	public static final int CLIENT_ACTION_UPDATA_USER_LOG_STATUS = 11025;
	public static final int CLIENT_ACTION_SHOW_HELP_GUIDE = 11026;
    public static final int CLIENT_ACTION_SHOW_HELP_GUIDE_DETAIL = 11027;
    public static final int CLIENT_ACTION_HIDE_HELP_GUIDE = 11028;
    public static final int CLIENT_ACTION_GOTO_HELP_VIEW = 11029;
    public static final int CLIENT_ACTION_CLOSE_HELP_GUIDE_DETAIL = 11030;
    public static final int CLIENT_ACTION_SEND_FEEDBACK = 11031;
    public static final int CLIENT_ACTION_ADD_COMMONDATA = 11032;
    public static final int CLIENT_ACTION_LISTVIEW_GOTO_LAST_POSITION = 11033;
    public static final int CLIENT_ACTION_DISPLAY_UPDATE_DIALOG = 11044;
    public static final int CLIENT_ACTION_DISPLAY_VERSION_UPDATE = 11045;
    public static final int CLIENT_ACTION_START_WITH_INDICATION_STRING = 11046;
    public static final int CLIENT_ACTION_SEND_HA_CMD = 11047;
	public static final String SERVER_MSG_ID = "message_id";
	public static final String SERVER_MSG_TITLE = "message_title";
	public static final String SERVER_MSG_DESCRIPTION = "message_description";
	public static final String SERVER_MSG_IMAGE_URL = "image_url";
	public static final String SERVER_MSG_URL = "pushhtml_url";
	
}
		
