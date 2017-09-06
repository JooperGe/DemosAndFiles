package com.iflytek.aitalk4;

public class AiTalkShareData {
	public static final String[] recognize_words = {"哦啦你好", "哦了你好"};
	public static int identificationFlag = 0;//0 do nothing. 1 recognize right. 2 recognize error. 3.recognize it is noise
	public static boolean isAsrDestoryed = false;//false: undestory. true: already destory
	public static boolean isStartPanel = true;//false open directly. true open when touching
	public static int recognizeFlag = 1;//1 stop recognize but not exit. 2 recognizing and write file. 3 exit recognize
	public static boolean isLeaveMainInterface = false;
	public static void setIdentificationFlag(int flag)
	{
		identificationFlag = flag;
	}
	public static int getIdentificationFlag()
	{
		return identificationFlag;
	}
	public static void setAsrDestoryState(boolean flag)
	{
		isAsrDestoryed = flag;
	}
	public static boolean getAsrDestoryState()
	{
		return isAsrDestoryed;
	}
	public static void setSpeechStartState(boolean flag)
	{
		isStartPanel = flag;
	}
	public static boolean getSpeechStartState()
	{
		return isStartPanel;
	}
	public static void setRecognizeFlag(int flag)
	{
		recognizeFlag = flag;
	}
	public static int getRecognizeFlag()
	{
		return recognizeFlag;
	}
	public static void setLeaveMainInterfaceFlag(boolean flag)
	{
		isLeaveMainInterface = flag;
	}
	public static boolean getLeaveMainInterfaceFlag()
	{
		return isLeaveMainInterface;
	}
	
}
