package com.viash.voicelib.data;

import org.json.JSONObject;

import android.content.Context;

public class ConfirmData extends BaseData {
	public static String number;
	public static String name;
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_SMS = 1;
	public static final int TYPE_WEIBO = 2;
	private static final int TYPE_CONTACT = 3;
	
	public int mType = TYPE_NORMAL;
	private boolean isContainData = false;

	public boolean isContainData() {
		return isContainData;
	}

	public void setContainData(boolean isContainData) {
		this.isContainData = isContainData;
	}

	protected SendSmsData mSmsData = null;
	protected ContactData mContactData = null;

	public ConfirmData(Context context, JSONObject obj, BaseData data) {
		super(data);
		mParseResult = parseFromJson(context, obj);
	}

	public ConfirmData(BaseData data) {
		super(data);
	}

	public ConfirmData() {
		super();
	}

	@Override
	public boolean parseFromJson(Context context, JSONObject obj) {
		// boolean ret = false;
		int present = obj.optInt("Present");
		String str = obj.optString("Content", null);
		String tag = obj.optString("tag");

		JSONObject appendix = obj.optJSONObject("appendix");
		if (tag != null) {
			updateTagString(tag);
		}

		if (str != null) {
			if ((present & 1) == 1) {
				updateTTSString(str);
			}
			if ((present & 2) == 2) {
				updateDisplayString(str);
			}

			mParseResult = true;
		}

		if (appendix != null) {

			String type = appendix.optString("type", null);
			if (type != null) {
				if (type.equalsIgnoreCase("sms")) {
					mType = TYPE_SMS;
					mSmsData = new SendSmsData(appendix);
				}else if(type.equalsIgnoreCase("contact")){
					mType = TYPE_CONTACT;
					mContactData = new ContactData(appendix);
				}
				
			}
			mParseResult = true;
		}
		return mParseResult;
	}

	public int getType() {
		return mType;
	}

	public SendSmsData getSmsData() {
		return mSmsData;
	}
	public ContactData getContactData(){
		return mContactData;
	}
	public boolean isDataNeedAnswer() {
		return true;
	}
	public class ContactData{
		public String mName = null;
		public String mNumber = null;
		public ContactData(JSONObject obj){
			 mName = obj.optString("name",null);
			 mNumber = obj.optString("phonenumber",null);
		}
		public String getmName() {
			return mName;
		}
		public void setmName(String mName) {
			this.mName = mName;
		}
		public String getmNumber() {
			return mNumber;
		}
		public void setmNumber(String mNumber) {
			this.mNumber = mNumber;
		}
		
	}
	public class SendSmsData {
		protected String mTo = null;
		protected String mToPhone = null;
		protected String mContent = null;

		public SendSmsData(JSONObject obj) {
			mTo = obj.optString("to_name", null);
			mToPhone = obj.optString("to_phone", null);
			mContent = obj.optString("sms_content", null);
		}

		public String getTo() {
			return mTo;
		}

		public void setTo(String mTo) {
			this.mTo = mTo;
			setModified(true);
		}

		public String getToPhone() {
			return mToPhone;
		}

		public void setToPhone(String mToPhone) {
			this.mToPhone = mToPhone;
			setModified(true);
		}

		public String getContent() {
			return mContent;
		}

		public void setContent(String mContent) {
			this.mContent = mContent;
			setModified(true);
		}
	}
}
