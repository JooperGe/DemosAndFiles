package com.viash.voice_assistant.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.iflytek.tts.TtsService.Tts;
import com.viash.voice_assistant.R;
import com.viash.voice_assistant.common.Config;

public class SavedData {
	// Settings
	private static final String SETTINGS_BACKGROUND = "settings_background";
	private static final String SETTINGS_VOICESETTING = "settings_voicesetting";
	private static final String SETTINGS_VOICETYPE = "settings_voicetype";
	private static final String SETTINGS_USERCALL = "settings_usercall";
	private static final String SETTINGS_PROMPT = "settings_prompt";
	private static final String SETTINGS_MAP_DISPLAY = "settings_map_display";
	private static final String SETTINGS_MAP_TRAVEL = "settings_map_travel";
	private static final String SETTINGS_MAP_TRAFFIC = "settings_map_traffic";
	private static final String SETTINGS_SEARCHONLINEMUSIC = "settings_searchonlinemusic";
	private static final String SETTINGS_LISTENANDSAVE = "settings_listenandsave";
	private static final String SETTINGS_MUSICDOWNLOADPATH = "settings_musicdownloadpath";
	private static final String DATA_USER_LEVEL = "data_user_level";
	private static final String DATA_USER_SCORE = "data_user_score";
	private static final String DATA_USER_NEXT_LEVEL_SCORE = "data_user_next_level_score";
	private static final String DATA_USER_SPECIAL_TIME="data_user_special_time";
	private static final String SETTINGS_WAKEUP_BY_AUDIO="settings_wakeup_by_audio";
	private static final String SETTINGS_WAKEUP_LOCK="settings_wakeuplock";   //����
	private static final String IS_91_REGISTERED = "is_91_registered";
	private static final String LOCK_MESSAGE = "lock_message";
	private static final String DATA_SYSTEM_RESTART = "data_system_restart";
	private static final String DATA_TOP_VOICE_BUTTON_SWITCH = "top_voice_button_switch";
	//private static final String SETTINGS_HELP_TIPS = "settings_help_tips";
    private static final String SETTINGS_NETWORK_TIPS = "settings_network_tips";
    private static final String SETTINGS_FLOATVIEW_ONDESK = "settings_floatview_ondesk";
    private static final String SETTINGS_AUTO_REMIND_INCOMING_CALL = "settings_auto_remind_incoming_call";
    private static final String SETTINGS_AUTO_REMIND_INCOMING_CALL_NAME = "settings_auto_remind_incoming_call_name";
    private static final String SETTINGS_AUTO_REMIND_SMS = "settings_auto_remind_sms";
    private static final String SETTINGS_AUTO_REMIND_SMS_NAME_OR_NUMBER = "settings_auto_remind_sms_name_or_number";
    private static final String SETTINGS_AUTO_REMIND_SMS_CONTENT = "settings_auto_remind_sms_content";
    private static final String SETTINGS_VOICE_WAKEUP = "settings_voice_wakeup";
    
	private static int settings_background = 1;
	private static boolean settings_voicesetting = true;
	private static int settings_voicetype = 3;
	private static String settings_usercall = null;
	private static boolean settings_prompt = false;
	private static int settings_map_display = 1;
	private static int settings_map_travel = 1;
	private static boolean settings_map_traffic = false;
	private static boolean settings_searchonlinemusic = false;
	private static boolean settings_listenandsave = false;
	private static String settings_musicdownloadpath = null;
	private static String lock_message = null;
	private static boolean system_restart = false;

	private static boolean server_mode = Config.USING_HTTP_WITH_SERVER;
		
	private static final String SHARED_DATA = "shared_data";
	private static final String IP_ADDRESS = "ip_address";
	private static final String AUTO_START_RECORD = "auto_start_record";
	private static final String KEY_INTRANET_IP = "intranet_ip";
	private static final String KEY_CUSTOM_IP = "custom_ip";
	private static final String KEY_SERVER_PORT = "server_port";
	private static final String KEY_LAST_LOCATION = "last_location";
	private static final String KEY_DISPLAY_FLOATVIEW = "display_floatview";
	private static final String KEY_SERVER_MODE = "key_server_mode";
	
	private static final String IS_RECORD = "is_record";
	private static final String IS_DISMISS_ALARM_BY_HAND = "is_Dismiss_Alarm_By_Hand";
	
	private static String mIP = null;
	private static String mIntranetIp = null;
	private static String mCustomIp = null;
	private static boolean mAutoStartRecord = false;
	private static String mLastLocation = null;
	private static int mPort = 0;
	private static boolean mDisplayFloatView = true;
	
	private static int mUserLevel = 0;
	private static int mUserScore = 0;
	private static int mUserNextLevel = 0;
	private static int mUserSpecialTime = 0;
	
	private static boolean mAllowWakeupByAudio = false;
	private static boolean mAllowLock = false;   //����
	private static boolean mIs91Registered = false;
	//private static boolean mHelpTips = true;
	private static boolean mNetworkTips = true;
	private static boolean mFloatViewOnDesk = true;
	private static boolean mAutoRemindIncomingCall = true;
	private static boolean mAutoRemindIncomingCallName = false;
	private static boolean mAutoRemindSMS = true;
	private static boolean mAutoRemindSMSNameOrNumber = false;
	private static boolean mAutoRemindSMSContent = false;
	private static boolean mVoiceWakeUp = false;
	//public static final String INTERNET_SERVER_IP = "58.246.139.29";
	public static final String INTERNET_SERVER_IP = "dls.olavoice.com";
	public static final String INTRANET_SERVER_IP = "10.27.254.240";
	

	public static final String INTERNET_SERVER_IP_HTTP = "api.olavoice.com";
	public static final String INTRANET_SERVER_IP_HTTP = "10.27.129.41";
	
	private static Context mContext = null;
	private static boolean mShowTopVoiceButton = false;
	
	private static boolean  isRecord = false;
    private static boolean isDismissAlarmByHand = false;
    
	public static void init(Context context)
	{
		mContext = context;		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		mIP = settings.getString(IP_ADDRESS, INTERNET_SERVER_IP_HTTP);
		mIntranetIp = settings.getString(KEY_INTRANET_IP, INTRANET_SERVER_IP_HTTP);
		mCustomIp = settings.getString(KEY_CUSTOM_IP, "10.3.172.");
		mAutoStartRecord = settings.getBoolean(AUTO_START_RECORD, true);
		mAllowWakeupByAudio = settings.getBoolean(SETTINGS_WAKEUP_BY_AUDIO, true);
		mAllowLock = settings.getBoolean(SETTINGS_WAKEUP_LOCK, false);     //����
		mPort = settings.getInt(KEY_SERVER_PORT, 0);
		mLastLocation = settings.getString(KEY_LAST_LOCATION, null);
		mDisplayFloatView = settings.getBoolean(KEY_DISPLAY_FLOATVIEW, false);
		
		// settings
		settings_background = settings.getInt(SETTINGS_BACKGROUND, 1);
		settings_voicesetting = settings.getBoolean(SETTINGS_VOICESETTING, true);
		settings_voicetype = settings.getInt(SETTINGS_VOICETYPE, 3);
		settings_usercall = settings.getString(SETTINGS_USERCALL, mContext.getResources().getString(R.string.setting_default_usercall));
		settings_prompt = settings.getBoolean(SETTINGS_PROMPT, false);
		settings_map_display = settings.getInt(SETTINGS_MAP_DISPLAY, 0);
		settings_map_travel = settings.getInt(SETTINGS_MAP_TRAVEL, 0);
		settings_map_traffic = settings.getBoolean(SETTINGS_MAP_TRAFFIC, false);
		settings_searchonlinemusic = settings.getBoolean(SETTINGS_SEARCHONLINEMUSIC, false);
		settings_listenandsave = settings.getBoolean(SETTINGS_LISTENANDSAVE, false);
		settings_musicdownloadpath = settings.getString(SETTINGS_MUSICDOWNLOADPATH, "");
		
		mUserLevel = settings.getInt(DATA_USER_LEVEL, 0);
		mUserScore = settings.getInt(DATA_USER_SCORE, 0);
		mUserNextLevel = settings.getInt(DATA_USER_NEXT_LEVEL_SCORE, 0);
		mUserSpecialTime = settings.getInt(DATA_USER_SPECIAL_TIME, 0);
		
		mAllowWakeupByAudio = settings.getBoolean(SETTINGS_WAKEUP_BY_AUDIO, false);
		mAllowLock = settings.getBoolean(SETTINGS_WAKEUP_LOCK, false); //����
		mIs91Registered = settings.getBoolean(IS_91_REGISTERED, false);
		lock_message = settings.getString(LOCK_MESSAGE, null);
		system_restart = settings.getBoolean(DATA_SYSTEM_RESTART,false);
		//mHelpTips = settings.getBoolean(SETTINGS_HELP_TIPS, true);
		mNetworkTips = settings.getBoolean(SETTINGS_NETWORK_TIPS, true);
		mFloatViewOnDesk = settings.getBoolean(SETTINGS_FLOATVIEW_ONDESK, true);
		mVoiceWakeUp = settings.getBoolean(SETTINGS_VOICE_WAKEUP, false);
		mAutoRemindIncomingCall = settings.getBoolean(SETTINGS_AUTO_REMIND_INCOMING_CALL, false);
		mAutoRemindIncomingCallName = settings.getBoolean(SETTINGS_AUTO_REMIND_INCOMING_CALL_NAME, false);
		mAutoRemindSMS = settings.getBoolean(SETTINGS_AUTO_REMIND_SMS, false);
		mAutoRemindSMSNameOrNumber = settings.getBoolean(SETTINGS_AUTO_REMIND_SMS_NAME_OR_NUMBER, false);
		mAutoRemindSMSContent = settings.getBoolean(SETTINGS_AUTO_REMIND_SMS_CONTENT, false);
		mShowTopVoiceButton = settings.getBoolean(DATA_TOP_VOICE_BUTTON_SWITCH, false);
		server_mode = settings.getBoolean(KEY_SERVER_MODE, true);
		
		isRecord = settings.getBoolean(IS_RECORD,false);
		isDismissAlarmByHand = settings.getBoolean(IS_DISMISS_ALARM_BY_HAND, false);
		
		initServerMode();
	}
	
	public static boolean isHttpMode() {
		return server_mode;
	}

	public static void setIsHttpMode(boolean servermode) {
		SavedData.server_mode = servermode;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(KEY_SERVER_MODE, server_mode);
		editor.commit();
		if (server_mode) {
			setmIP(INTERNET_SERVER_IP_HTTP);
			setmIntranetIp(INTRANET_SERVER_IP_HTTP);
			setmPort(0);
		}else {
			setmIP(INTERNET_SERVER_IP);
			setmIntranetIp(INTRANET_SERVER_IP);
			setmPort(80);
		}
	}

	public static String getmIP() {
		return mIP;
	}

	public static void setmIP(String mIP) {
		SavedData.mIP = mIP;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(IP_ADDRESS, mIP);
		editor.commit();
	}
	
	

	public static String getmIntranetIp() {
		return mIntranetIp;
	}

	public static void setmIntranetIp(String mIntranetIp) {
		SavedData.mIntranetIp = mIntranetIp;

		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(KEY_INTRANET_IP, mIntranetIp);
		editor.commit();
	}

	public static String getmCustomIp() {
		return mCustomIp;
	}

	public static void setmCustomIp(String mCustomIp) {
		SavedData.mCustomIp = mCustomIp;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(KEY_CUSTOM_IP, mCustomIp);
		editor.commit();
	}

	public static boolean getmAutoStartRecord() {
		return mAutoStartRecord;
	}

	public static void setmAutoStartRecord(boolean mAutoStartRecord) {
		SavedData.mAutoStartRecord = mAutoStartRecord;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(AUTO_START_RECORD, mAutoStartRecord);
		editor.commit();		
	}

	public static int getmPort() {
		return mPort;
	}

	public static void setmPort(int mPort) {
		SavedData.mPort = mPort;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(KEY_SERVER_PORT, mPort);
		editor.commit();
	}

	public static String getmLastLocation() {
		return mLastLocation;
	}

	public static void setmLastLocation(String mLastLocation) {
		SavedData.mLastLocation = mLastLocation;
		if(mLastLocation != null && mLastLocation.length() > 0)
		{
			SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
			Editor editor = settings.edit();
			editor.putString(KEY_LAST_LOCATION, mLastLocation);
			editor.commit();	
		}
	}
	
	public static boolean getmDisplayFloatView() {
		return mDisplayFloatView;
	}

	public static void setmDisplayFloatView(boolean mDisplayFloatView) {
		SavedData.mDisplayFloatView = mDisplayFloatView;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(KEY_DISPLAY_FLOATVIEW, mDisplayFloatView);
		editor.commit();		
	}
	
	// Settings
	public static void setBackground(int id){
		settings_background = id;

		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(SETTINGS_BACKGROUND, id);
		editor.commit();
	}
	public static void setVoiceSetting(boolean voicesetting){
		settings_voicesetting = voicesetting;

		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_VOICESETTING, voicesetting);
		editor.commit();
	}
	public static void setVoiceType(int voicetype){
		settings_voicetype = voicetype;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(SETTINGS_VOICETYPE, voicetype);
		editor.commit();
		
		Tts.setSpeaker(settings_voicetype);
	}
	public static void setUserCall(String usercall){
		settings_usercall = usercall;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(SETTINGS_USERCALL, usercall);
		editor.commit();
	}
	public static void setPrompt(boolean prompt){
		settings_prompt = prompt;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_PROMPT, prompt);
		editor.commit();
	}
	
	public static void setMapDisplay(int displaytype){
		settings_map_display = displaytype;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(SETTINGS_MAP_DISPLAY, displaytype);
		editor.commit();
	}
	public static void setMapTravel(int traveltype){
		settings_map_travel = traveltype;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(SETTINGS_MAP_TRAVEL, traveltype);
		editor.commit();
	}
	public static void setMapTraffic(boolean traffic){
		settings_map_traffic = traffic;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_MAP_TRAFFIC, traffic);
		editor.commit();
	}
	
	public static void setSearchOnlineMusic(boolean searchonlinemusic){
		settings_searchonlinemusic = searchonlinemusic;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_SEARCHONLINEMUSIC, searchonlinemusic);
		editor.commit();
	}
	public static void setListenAndSave(boolean listenandsave){
		settings_listenandsave = listenandsave;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_LISTENANDSAVE, listenandsave);
		editor.commit();
	}
	public static void setMusicDownloadPath(String downloadpath){
		settings_musicdownloadpath = downloadpath;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(SETTINGS_MUSICDOWNLOADPATH, downloadpath);
		editor.commit();
	}
	
	public static int getBackground(){
		return settings_background;
	}
	public static boolean getVoiceSetting(){
		return settings_voicesetting;
	}
	public static int getVoiceType(){
		return settings_voicetype;
	}
	public static String getUserCall(){
		return settings_usercall;
	}
	public static boolean getPrompt(){
		return settings_prompt;
	}
	public static int getMapDisplay(){
		return settings_map_display;
	}
	public static int getMapTravel(){
		return settings_map_travel;
	}
	public static boolean getMapTraffic(){
		return settings_map_traffic;
	}
	public static boolean getSearchOnlineMusic(){
		return settings_searchonlinemusic;
	}
	public static boolean getListenAndSave(){
		return settings_listenandsave;
	}
	public static String getMusicDownloadPath(){
		return settings_musicdownloadpath;
	}

	public static int getmUserLevel() {
		return mUserLevel;
	}

	public static int getmUserScore() {
		return mUserScore;
	}

	public static int getmUserNextLevel() {
		return mUserNextLevel;
	}
	
	public static int getmUserSpecialTime() {
		return mUserSpecialTime;
	}

	public static void setmUserInfo(int userLevel, int userScore, int userNextLevel, int userSpecialTime) {
		mUserLevel = userLevel;
		mUserScore = userScore;
		mUserNextLevel = userNextLevel;
		mUserSpecialTime = userSpecialTime;

		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putInt(DATA_USER_LEVEL, mUserLevel);
		editor.putInt(DATA_USER_SCORE, mUserScore);
		editor.putInt(DATA_USER_NEXT_LEVEL_SCORE, mUserNextLevel);
		editor.putInt(DATA_USER_SPECIAL_TIME, mUserSpecialTime);
		editor.commit();
	}
	
	public static void setAllowWakeupByAudio(boolean wakeup){
		mAllowWakeupByAudio = wakeup;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_WAKEUP_BY_AUDIO, wakeup);
		editor.commit();
	}
	
	public static boolean isAllowWakeupByAudio()
	{
		return mAllowWakeupByAudio;
	}
	//����
	public static void setAllowLock(boolean wakeuplock){
		mAllowLock = wakeuplock;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_WAKEUP_LOCK, wakeuplock);
		editor.commit();
	}
	
	public static boolean isAllowLock()
	{
		return mAllowLock;
		
	}
	
	
	public static void set91Registered(boolean register)
	{
		mIs91Registered = register;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(IS_91_REGISTERED, register);
		editor.commit();
		
	}
	public static boolean is91Registered()
	{
		return mIs91Registered;
	}
	
	public static void setLockMessage(String register)
	{
		lock_message = register;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putString(LOCK_MESSAGE, register);
		editor.commit();
		
	}
	public static String getLockMessage()
	{
		return lock_message;
	}
	
	public static boolean isSystem_restart() {
		return system_restart;
	}

	public static void setSystemRestart(boolean system_restart) {
		SavedData.system_restart = system_restart;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(DATA_SYSTEM_RESTART, system_restart);
		editor.commit();
	}
	
	private static void initServerMode()
	{
		if (server_mode){
			Config.WHICH_SERVER = Config.HTTP_SERVER;
			if (mIP == INTERNET_SERVER_IP) {
				setmIP(INTERNET_SERVER_IP_HTTP);
				setmIntranetIp(INTRANET_SERVER_IP_HTTP);
			}
		}
		else {
			Config.WHICH_SERVER = SavedData.getmIP();
			if (mIP == INTERNET_SERVER_IP_HTTP) {
				setmIP(INTERNET_SERVER_IP);
				setmIntranetIp(INTRANET_SERVER_IP);
			}
		}
	}

	public static boolean ismShowTopVoiceButton() {
		return mShowTopVoiceButton;
	}

	public static void setmShowTopVoiceButton(boolean mShowTopVoiceButton) {
		SavedData.mShowTopVoiceButton = mShowTopVoiceButton;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(DATA_TOP_VOICE_BUTTON_SWITCH, mShowTopVoiceButton);
		editor.commit();
	}
	
	/*public static void setHelpTips(boolean helpTips){
		mHelpTips = helpTips;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_HELP_TIPS, helpTips);
		editor.commit();
	}
	
	public static boolean isHelpTips()
	{
		return mHelpTips;
	}*/
	
	public static void setNetworkTips(boolean networkTips){
		mNetworkTips = networkTips;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_NETWORK_TIPS, networkTips);
		editor.commit();
	}
	
	public static boolean isNetworkTips()
	{
		return mNetworkTips;
	}
	
	public static void setFloatViewOnDesk(boolean value){
		mFloatViewOnDesk = value;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_FLOATVIEW_ONDESK, value);
		editor.commit();
	}
	
	public static boolean isFloatViewOnDesk()
	{
		return mFloatViewOnDesk;
	}
	
	public static void setVoiceWakeUp(boolean value){
		mVoiceWakeUp = value;
		
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_VOICE_WAKEUP, value);
		editor.commit();
	}
	
	public static boolean isVoiceWakeUpOpen()
	{
		return mVoiceWakeUp;		 
	}
	
	public static boolean isAutoRemindIncomingCall()
	{
		return mAutoRemindIncomingCall;
	}
	
	public static void setAutoRemindIncomingCall(boolean autoRemindIncomingCall)
	{
		mAutoRemindIncomingCall = autoRemindIncomingCall;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_AUTO_REMIND_INCOMING_CALL, autoRemindIncomingCall);
		editor.commit();
	}
	
	public static boolean isAutoRemindIncomingCallName()
	{
		return mAutoRemindIncomingCallName;
	}
	
	public static void setAutoRemindIncomingCallName(boolean autoRemindIncomingCallName)
	{
		mAutoRemindIncomingCallName = autoRemindIncomingCallName;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_AUTO_REMIND_INCOMING_CALL_NAME, autoRemindIncomingCallName);
		editor.commit();
	}
	
	public static boolean isAutoRemindSMS()
	{
		return mAutoRemindSMS;
	}
	
	public static void setAutoRemindSMS(boolean autoRemindSMS)
	{
		mAutoRemindSMS = autoRemindSMS;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_AUTO_REMIND_SMS, autoRemindSMS);
		editor.commit();
	}
	
	public static boolean isAutoRemindSMSNameOrNumber()
	{
		return mAutoRemindSMSNameOrNumber;
	}
	
	public static void setAutoRemindSMSNameOrName(boolean autoRemindSMSNameOrNumber)
	{
		mAutoRemindSMSNameOrNumber = autoRemindSMSNameOrNumber;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_AUTO_REMIND_SMS_NAME_OR_NUMBER, autoRemindSMSNameOrNumber);
		editor.commit();
	}
	
	public static boolean isAutoRemindSMSContent()
	{
		return mAutoRemindSMSContent;
	}
	
	public static void setAutoRemindSMSContent(boolean autoRemindSMSContent)
	{
		mAutoRemindSMSContent = autoRemindSMSContent;
		SharedPreferences settings = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = settings.edit();
		editor.putBoolean(SETTINGS_AUTO_REMIND_SMS_CONTENT, autoRemindSMSContent);
		editor.commit();
	}
	
	
	//语音选择 状态保存
	public static void setVoiceTypes(String string ){
		SharedPreferences settings = mContext.getSharedPreferences("voice_type", 0);
		Editor editor = settings.edit();
		editor.putString("chosed_voice_type", string);
		editor.commit();
	}
	
	public static String getVoiceTypes(){
		SharedPreferences preferences = mContext.getSharedPreferences("voice_type", 0);
		String string = preferences.getString("chosed_voice_type", "科大讯飞");
		return string;
	}
	 
	public static void setVoiceTypeNum(int is){
		SharedPreferences settings = mContext.getSharedPreferences("voice_type_num", 0);
		Editor editor = settings.edit();
		editor.putInt("chosed_voice_type_num", is);
		editor.commit();
	}
	
	public static int getVoiceTypeNum(){
		SharedPreferences preferences = mContext.getSharedPreferences("voice_type_num", 0);
		int is  = preferences.getInt("chosed_voice_type_num", 0);
		return is;
	}
	
	public static boolean getIsDismissAlarmByHand(){
		return isDismissAlarmByHand;
	}
	
	public static void setIsDismissAlarmByHand(boolean b){
		SavedData.isDismissAlarmByHand = b ;
		SharedPreferences preferences = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = preferences.edit();
		editor.putBoolean(IS_DISMISS_ALARM_BY_HAND,b);
		editor.commit();
	}
	
	public static boolean getISRecord(){
		return isRecord;
	}
	
	public static void setIsRecord(boolean b){
		SavedData.isRecord = b;
		SharedPreferences preferences = mContext.getSharedPreferences(SHARED_DATA, 0);
		Editor editor = preferences.edit();
		editor.putBoolean(IS_RECORD,b);
		editor.commit();
	}
}
