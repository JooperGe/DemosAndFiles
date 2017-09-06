package com.viash.voice_assistant.common;

public class IncomingCallShareState {
	
public static boolean recordFlag = false;
public static int hanleCallState = 2;//0:undo，1:answer call，2:end  call
public static String name = null;
public static String number = null;
public static final String UNKNOWN_NAME = "unknown";
public static final int START_PLAY_TTS_DELAY_SECONDS = 0;
public static final int START_PLAY_TTS_WITHOUT_DELAY = 1;
public static final int ANSWER_RING = 0;
public static final int END_CALL = 1;
public static void setRecordingFlag(boolean state)
{
	recordFlag = state;
}

public static void setHandleCallState(int state)
{
	hanleCallState = state;
}
public static void setName(String s)
{
   name = s;
}
public static void setNumber(String s)
{
   number = s;
}
public static boolean getRecordingFlag()
{
   return  recordFlag;	
}
public static int getHandleCallState()
{
   return hanleCallState;	
}
public static String getName()
{
   return name;	
}
public static String getNumber()
{
   return number;	
}
public static boolean isIncomgingCall()
{
	if ((getHandleCallState() == 0)&&(getName() != null))	
	    return true;
	else
		return false;
}
}
