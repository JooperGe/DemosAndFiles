package com.viash.voicelib.utils;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class ContactUtil {
	private static final boolean USE_SIM_DATA = false;
	protected static final int SIM_CONTACT_ID_START_1 = 0x10000000;
	protected static final int SIM_CONTACT_ID_START_2 = 0x20000000;
	private static final String TAG = "ContactUitl";
	protected static List<ContactInfo> mLatestContactsInfo = new ArrayList<ContactInfo>();

	public static class ContactInfo {		
		protected int mId;
		protected List<String> mLstName = new ArrayList<String>();
		protected List<String> mLstLastName = new ArrayList<String>();
		protected List<String> mLstMiddleName = new ArrayList<String>();
		protected List<String> mLstFirstName = new ArrayList<String>();
		protected List<String> mLstNickName = new ArrayList<String>();

		protected List<String> mLstHomeEmail = new ArrayList<String>();
		protected List<String> mLstWorkEmail = new ArrayList<String>();
		protected List<String> mLstOtherEmail = new ArrayList<String>();

		protected List<String> mLstHomeAddress = new ArrayList<String>();
		protected List<String> mLstWorkAddress = new ArrayList<String>();
		protected List<String> mLstOtherAddress = new ArrayList<String>();
		
		protected List<String> mLstCompany = new ArrayList<String>();
		protected List<String> mLstTitle = new ArrayList<String>();
		
		protected List<String> mLstHomePhone = new ArrayList<String>();
		protected List<String> mLstWorkPhone = new ArrayList<String>();
		protected List<String> mLstMobile = new ArrayList<String>();	
		protected List<String> mLstOtherPhone = new ArrayList<String>();	
		
		protected String mQQ;
		protected String mMsn;
		
		protected byte[] md5;
		
		public static final int INFO_PHONE = 1;
		public static final int INFO_ADDRESS = 2;
		public static final int INFO_EMAIL = 4;
		public static final int INFO_NAME = 8;
		public static final int INFO_NICK_NAME = 16;
		public static final int INFO_COMPANY = 32;
		public static final int INFO_IM = 64;

		
		protected boolean containsPhone(String number, List<String> lstPhone)
		{
			boolean ret = false;
			if(lstPhone != null)
			{
				
				for(int i = 0; i < lstPhone.size(); i++)
				{
					String phone = lstPhone.get(i);
					if(phone != null)
					{
						phone = phone.replace("-", "").replace("(", "").replace(")", "").replace("+", "").replace(" ", "");
						if(number.length() > 4 && phone.length() > 4)
						{
							if(number.contains(phone) || phone.contains(number))
							{
								ret = true;
								break;
							}
						}
						else
						{
							if(number.equalsIgnoreCase(phone))
							{
								ret = true;
								break;
							}
						}
					}
				}
			}
			
			return ret;
		}
		
		public boolean containsPhone(String number)
		{
			boolean ret = false;
			number = number.replace("-", "").replace("(", "").replace(")", "").replace("+", "").replace(" ", "");
			ret = containsPhone(number, mLstHomePhone);
			if(!ret)
				ret = containsPhone(number, mLstWorkPhone);
			if(!ret)
				ret = containsPhone(number, mLstMobile);
			if(!ret)
				ret = containsPhone(number, mLstOtherPhone);
			
			return ret;
		}
		
		public String getDefaultName()
		{
			String name = null;
			if(mLstName != null && mLstName.size() > 0)
			{
				name = mLstName.get(0);
			}
			
			return name;
		}
		
		public List<String> getmLstName() {
			return mLstName;
		}

		public List<String> getmLstLastName() {
			return mLstLastName;
		}

		public List<String> getmLstFirstName() {
			return mLstFirstName;
		}

		public List<String> getmLstNickName() {
			return mLstNickName;
		}

		public List<String> getmLstHomeEmail() {
			return mLstHomeEmail;
		}

		public List<String> getmLstWorkEmail() {
			return mLstWorkEmail;
		}

		public List<String> getmLstOtherEmail() {
			return mLstOtherEmail;
		}

		public List<String> getmLstHomeAddress() {
			return mLstHomeAddress;
		}

		public List<String> getmLstWorkAddress() {
			return mLstWorkAddress;
		}

		public List<String> getmLstOtherAddress() {
			return mLstOtherAddress;
		}

		public List<String> getmLstCompany() {
			return mLstCompany;
		}

		public List<String> getmLstTitle() {
			return mLstTitle;
		}

		public List<String> getmLstHomePhone() {
			return mLstHomePhone;
		}

		public List<String> getmLstWorkPhone() {
			return mLstWorkPhone;
		}

		public List<String> getmLstMobile() {
			return mLstMobile;
		}

		public List<String> getmLstOtherPhone() {
			return mLstOtherPhone;
		}

		public String getmQQ() {
			return mQQ;
		}

		public String getmMsn() {
			return mMsn;
		}
		
		public byte[] getMD5() {
			return md5;
		}
		
		public List<String> getmLstMiddleName() {
			return mLstMiddleName;
		}

		public void setmLstMiddleName(List<String> mLstMiddleName) {
			this.mLstMiddleName = mLstMiddleName;
		}

		public void setmLstName(List<String> mLstName) {
			this.mLstName = mLstName;
		}

		public void setmLstLastName(List<String> mLstLastName) {
			this.mLstLastName = mLstLastName;
		}

		public void setmLstFirstName(List<String> mLstFirstName) {
			this.mLstFirstName = mLstFirstName;
		}

		public void setmLstNickName(List<String> mLstNickName) {
			this.mLstNickName = mLstNickName;
		}

		public void setmLstHomeEmail(List<String> mLstHomeEmail) {
			this.mLstHomeEmail = mLstHomeEmail;
		}

		public void setmLstWorkEmail(List<String> mLstWorkEmail) {
			this.mLstWorkEmail = mLstWorkEmail;
		}

		public void setmLstOtherEmail(List<String> mLstOtherEmail) {
			this.mLstOtherEmail = mLstOtherEmail;
		}

		public void setmLstHomeAddress(List<String> mLstHomeAddress) {
			this.mLstHomeAddress = mLstHomeAddress;
		}

		public void setmLstWorkAddress(List<String> mLstWorkAddress) {
			this.mLstWorkAddress = mLstWorkAddress;
		}

		public void setmLstOtherAddress(List<String> mLstOtherAddress) {
			this.mLstOtherAddress = mLstOtherAddress;
		}

		public void setmLstCompany(List<String> mLstCompany) {
			this.mLstCompany = mLstCompany;
		}

		public void setmLstTitle(List<String> mLstTitle) {
			this.mLstTitle = mLstTitle;
		}

		public void setmLstHomePhone(List<String> mLstHomePhone) {
			this.mLstHomePhone = mLstHomePhone;
		}

		public void setmLstWorkPhone(List<String> mLstWorkPhone) {
			this.mLstWorkPhone = mLstWorkPhone;
		}

		public void setmLstMobile(List<String> mLstMobile) {
			this.mLstMobile = mLstMobile;
		}

		public void setmLstOtherPhone(List<String> mLstOtherPhone) {
			this.mLstOtherPhone = mLstOtherPhone;
		}

		public ContactInfo() {
			super();
		}		

		public int getmId() {
			return mId;
		}



		public void setmId(int mId) {
			this.mId = mId;
		}



		public void setmQQ(String mQQ) {
			this.mQQ = mQQ;
		}



		public void setmMsn(String mMsn) {
			this.mMsn = mMsn;
		}
		
		

		protected boolean isHome(String label)
		{
			label = label.toLowerCase();
			return (label.contains("home") || label.contains("家"));
		}
		
		protected boolean isWork(String label)
		{
			label = label.toLowerCase();
			return (label.contains("work") || label.contains("工作"));
		}
		
		protected boolean isMobile(String label)
		{
			label = label.toLowerCase();
			return (label.contains("mobile") || label.contains("cell") || label.contains("手机"));
		}
		
		protected void addStringToList(List<String> lstData, String data)
		{
			if(lstData != null && data != null)
			{
				if(!lstData.contains(data))
					lstData.add(data);
			}
		}

		public void addPhone(int type, String label, String number) {
			if(type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
			{
				addStringToList(mLstHomePhone, number);				
			}
			else if(type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK ||
					type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)
			{
				addStringToList(mLstWorkPhone, number);	
			}
			else if(type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
			{
				addStringToList(mLstMobile, number);	
			}
			else if(type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
			{
				if(label != null && label.length() > 0)
				{
					if(isHome(label))
					{
						addStringToList(mLstHomePhone, number);	
					}
					else if(isWork(label))
					{
						addStringToList(mLstWorkPhone, number);	
					}
					else if(isMobile(label))
					{
						addStringToList(mLstMobile, number);	
					}
					else
					{
						addStringToList(mLstOtherPhone, number);	
					}						
				}
				else
				{
					addStringToList(mLstOtherPhone, number);
				}
			}
			else
			{
				addStringToList(mLstOtherPhone, number);
			}
		}
		
		public void addAddress(int type, String label, String address) {
			if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
			{
				mLstHomeAddress.add(address);
			}
			else if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
			{
				mLstWorkAddress.add(address);
			}
			else if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM)			
			{
				if(label != null && label.length() > 0)
				{
					if(isHome(label))
					{
						mLstHomeAddress.add(address);
					}
					else if(isWork(label))
					{
						mLstWorkAddress.add(address);
					}
					else
					{
						mLstOtherAddress.add(address);
					}						
				}
				else
				{
					mLstOtherAddress.add(address);
				}
			}
			else
			{
				mLstOtherAddress.add(address);
			}
		}
		
		public void addEmail(int type, String label, String email) {
			if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
			{
				mLstHomeEmail.add(email);
			}
			else if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
			{
				mLstWorkEmail.add(email);
			}
			else if(type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM)			
			{
				if(label != null && label.length() > 0)
				{
					if(isHome(label))
					{
						mLstHomeEmail.add(email);
					}
					else if(isWork(label))
					{
						mLstWorkEmail.add(email);
					}
					else
					{
						mLstOtherEmail.add(email);
					}						
				}
				else
				{
					mLstOtherEmail.add(email);
				}
			}
			else
			{
				mLstOtherEmail.add(email);
			}
		}

		public void addCompany(String name, String title)
		{
			mLstCompany.add(name);
			mLstTitle.add(title);
		}
		
		public void addName(String name, String firstName, String lastName, String middleName)
		{
			mLstName.add(name);
			mLstFirstName.add(firstName);
			mLstLastName.add(lastName);
			mLstMiddleName.add(middleName);
		}
		
		public void addNickName(String nickName)
		{
			if(nickName != null)
				mLstNickName.add(nickName);
		}
		
		protected void addJsonData(JSONObject objParent, String key, String subKey, List<String> lstData)
		{
			if(lstData.size() > 0)
			{
				try {
					JSONArray jsonArray = new JSONArray();
					for(int i = 0; i < lstData.size(); i++)
					{
						if(lstData.get(i) != null)
						{
							JSONObject jsonObj = new JSONObject();
							jsonObj.put(subKey, lstData.get(i));
							jsonArray.put(jsonObj);
						}
					}
					
					objParent.put(key, jsonArray);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		public JSONObject toJsonObject() {
		
			JSONArray jsonArray;
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", mId);
				
				if(mLstName.size() > 0)
				{
					jsonArray = new JSONArray();
					for(int i = 0; i < mLstName.size(); i++)
					{
						JSONObject objName = new JSONObject();
						if( mLstName.get(i) != null)
							objName.put("name", mLstName.get(i));
						
						if(mLstLastName.get(i) != null)
							objName.put("last name", mLstLastName.get(i));
						
						if(mLstFirstName.get(i) != null)
							objName.put("first name", mLstFirstName.get(i));
						
						if(mLstMiddleName.get(i) != null)
							objName.put("middle name", mLstMiddleName.get(i));
						
						jsonArray.put(objName);
					}
					obj.put("name", jsonArray);
				}
				
				if(mLstCompany.size() > 0)
				{
					jsonArray = new JSONArray();
					for(int i = 0; i < mLstCompany.size(); i++)
					{
						JSONObject objCompany = new JSONObject();
						if(mLstTitle.get(i) != null)
							objCompany.put("title", mLstTitle.get(i));
						
						if(mLstCompany.get(i) != null)
							objCompany.put("company", mLstCompany.get(i));
						jsonArray.put(objCompany);
					}
					obj.put("organization", jsonArray);
				}
				
				addJsonData(obj, "nick name", "name", mLstNickName);
				addJsonData(obj, "home address", "address", mLstHomeAddress);
				addJsonData(obj, "work address", "address", mLstWorkAddress);
				addJsonData(obj, "other address", "address", mLstOtherAddress);
				
				addJsonData(obj, "home phone", "phone", mLstHomePhone);
				addJsonData(obj, "work phone", "phone", mLstWorkPhone);
				addJsonData(obj, "mobile phone", "phone", mLstMobile);
				addJsonData(obj, "other phone", "phone", mLstOtherPhone);
				
				addJsonData(obj, "private email", "email", mLstHomeEmail);
				addJsonData(obj, "work email", "email", mLstWorkEmail);
				addJsonData(obj, "other email", "email", mLstOtherEmail);
	
				if(mMsn != null)
					obj.put("msn", mMsn);
				
				if(mQQ != null)
					obj.put("qq", mQQ);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj;
		}

		public boolean parseFromJson(JSONObject obj) {
			boolean ret = false;
			int i = 0;
			int id = obj.optInt("id");
			if(id != 0)
			{
				setmId(id);
				JSONArray array = obj.optJSONArray("name");
				if(array != null)
				{
					for(i = 0; i < array.length(); i++)
					{
						JSONObject objName = array.optJSONObject(i);
						if(objName != null)
						{
							String name = objName.optString("name", null);
							String last_name = objName.optString("last name");
							String first_name = objName.optString("first name");
							String middle_name = objName.optString("middle name");
							addName(name, first_name, last_name, middle_name);
						}
					}
				}
				
				array = obj.optJSONArray("organization");
				if(array != null)
				{
					for(i = 0; i < array.length(); i++)
					{
						JSONObject objName = array.optJSONObject(i);
						if(objName != null)
						{
							String title = objName.optString("title", null);
							String company = objName.optString("company");
							addCompany(company, title);
						}
					}
				}
				
				mLstNickName = parseJsonList(obj, "nick name", "name");
				
				mLstHomeAddress = parseJsonList(obj, "home address", "address");
				mLstWorkAddress = parseJsonList(obj, "work address", "address");
				mLstOtherAddress = parseJsonList(obj, "other address", "address");
				
				mLstHomePhone = parseJsonList(obj, "home phone", "phone");
				mLstWorkPhone = parseJsonList(obj, "work phone", "phone");
				mLstMobile = parseJsonList(obj, "mobile phone", "phone");
				mLstOtherPhone = parseJsonList(obj, "other phone", "phone");
				
				mLstHomeEmail = parseJsonList(obj, "private email", "email");
				mLstWorkEmail = parseJsonList(obj, "work email", "email");
				mLstOtherEmail = parseJsonList(obj, "other email", "email");
				
				mMsn = obj.optString("msn", null);
				mQQ = obj.optString("qq", null);
				
				countMD5();
				
				ret = true;
			}
			
			return ret;
		}
		
		protected List<String> parseJsonList(JSONObject obj, String key, String subKey)
		{
			List<String> lstData = new ArrayList<String>();
			JSONArray array = obj.optJSONArray(key);
			if(array != null)
			{
				for(int i = 0; i < array.length(); i++)
				{
					JSONObject objSub = array.optJSONObject(i);
					if(objSub != null)
					{
						String value = objSub.optString(subKey);
						if(value != null)
							lstData.add(value);
					}
				}
			}
			
			return lstData;
		}
		
		protected void countMD5(){
			String sumData = "";
			sumData = String.valueOf(mId);
			sumData += connectString(mLstLastName);
			sumData += connectString(mLstName);
			sumData += connectString(mLstFirstName);
			sumData += connectString(mLstNickName);
			sumData += connectString(mLstHomeAddress);
			sumData += connectString(mLstWorkAddress);
			sumData += connectString(mLstOtherAddress);
			sumData += connectString(mLstHomePhone);
			sumData += connectString(mLstWorkPhone);
			sumData += connectString(mLstMobile);
			sumData += connectString(mLstOtherPhone);
			sumData += connectString(mLstHomeEmail);
			sumData += connectString(mLstWorkEmail);
			sumData += connectString(mLstOtherEmail);
			sumData += connectString(mLstCompany);
			sumData += connectString(mLstTitle);
			if(mMsn != null) sumData += (mMsn);
			if(mQQ != null) sumData += (mQQ);
			md5 = getMD5(sumData);
		}
		
		protected byte[] getMD5(String data){
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(data.getBytes());
				return md.digest();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		protected String connectString(List<String> data){
			String cs = "";
			for(int i = 0 ; i < data.size() ; i++){
				if(data.get(i) != null) cs += data.get(i);
			}
			return cs;
		}
	}
	
	protected static Cursor findCursor(ContentResolver resolver, String itemType)
	{
		String where = ContactsContract.Data.MIMETYPE + " = ?"; 
	 	String[] whereParams = new String[]{itemType}; 
		Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, null, 
				where, whereParams, null); 
		
		return cursor;
	}
	
	protected static Cursor findCursor(ContentResolver resolver)
	{
		String where = ContactsContract.Data.MIMETYPE + " = ? or " + 
				ContactsContract.Data.MIMETYPE + " = ? or " +
				ContactsContract.Data.MIMETYPE + " = ? or " +
				ContactsContract.Data.MIMETYPE + " = ? or " +
				ContactsContract.Data.MIMETYPE + " = ? or " +
				ContactsContract.Data.MIMETYPE + " = ? or " +
				ContactsContract.Data.MIMETYPE + " = ? or "; 
	 	String[] whereParams = new String[]
	 			{
	 				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
	 				ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
	 			
	 			}; 
		Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, null, 
				where, whereParams, null); 
		
		return cursor;
	}
	
	protected static Cursor findCursor(ContentResolver resolver, String itemType, String contcatId)
	{
		String where = ContactsContract.Data.MIMETYPE + " = ? and " + ContactsContract.Data.CONTACT_ID + " = ?" ; 
	 	String[] whereParams = new String[]{itemType, "" + contcatId}; 
		Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, null, 
				where, whereParams, null); 
		
		return cursor;
	}
	
	public static List<ContactInfo> findAllContacts(Context context, boolean saveToLatest) {
		List<ContactInfo> lstContactInfo = new ArrayList<ContactInfo>();
		ContentResolver resolver = context.getContentResolver();
		Uri uri = ContactsContract.Contacts.CONTENT_URI;		
		String sortOrder = ContactsContract.Contacts._ID + " ASC";
		long time = System.currentTimeMillis();

		Map<Integer, ContactUtil.ContactInfo> mapContacts = new HashMap<Integer, ContactUtil.ContactInfo>();
		Map<String, ContactUtil.ContactInfo> mapContactsName = new HashMap<String, ContactUtil.ContactInfo>();
		
		try
		{
			Cursor curContacts = resolver.query(uri, null, null, null, sortOrder);
			if (curContacts != null) {			
				int idColIndex = curContacts
						.getColumnIndex(ContactsContract.Contacts._ID);
				
				while (curContacts.moveToNext()) {				
					int contactId = curContacts.getInt(idColIndex);			
					
					ContactInfo info = new ContactInfo();
					info.setmId(contactId);
					lstContactInfo.add(info);
					mapContacts.put(contactId, info);
				}
				curContacts.close();
			}
		}
		catch(Exception e)
		{
			// In some machine, curContacts.close() will throw IllegalState exception
			e.printStackTrace();
		}
		
		Log.i(TAG, "Query Time1:" + (System.currentTimeMillis() - time) + "ms");
		Cursor curNames = findCursor(resolver, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		if(curNames != null)
		{
			int colId = curNames.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID);
			int colName = curNames.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
			int colFirstName = curNames.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
			int colLastName = curNames.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
			int colMiddleName = curNames.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
			while(curNames.moveToNext())
			{
				int contactId = curNames.getInt(colId);				
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					String name = curNames.getString(colName);
					info.addName(								
							name,
							curNames.getString(colFirstName),
							curNames.getString(colLastName),
							curNames.getString(colMiddleName));
					if(name != null && name.length() > 0)
					{
						mapContactsName.put(name ,  info);
					}
				}
				
				
			}
			curNames.close(); 
		}
		
		Log.i(TAG, "Query Time2:" + (System.currentTimeMillis() - time) + "ms");
		
	 	Cursor curNickNames = findCursor(resolver, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
	 	if (curNickNames != null) { 
	 		int colId = curNickNames.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.CONTACT_ID);
	 		int colName = curNickNames.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME);
	 		while(curNickNames.moveToNext())
	 		{
	 			int contactId = curNickNames.getInt(colId);		
	 			String nickName = curNickNames.getString(colName);
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					info.addNickName(nickName);
				}
	 		}
	 		curNickNames.close();
	 	} 
	 	
	 	Log.i(TAG, "Query Time3:" + (System.currentTimeMillis() - time) + "ms");
	 	
	 	Cursor curOrg = findCursor(resolver, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
	 	if (curOrg != null) { 
	 		int colId = curOrg.getColumnIndex(ContactsContract.CommonDataKinds.Organization.CONTACT_ID);
	 		int colName = curOrg.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA);
	 		int colTitle = curOrg.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE);
	 		while(curOrg.moveToNext())
	 		{
	 			int contactId = curOrg.getInt(colId);		
	 			String orgName = curOrg.getString(colName);
		 		String title = curOrg.getString(colTitle);
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					info.addCompany(orgName, title);
				}
	 		}
	 		curOrg.close();
	 	} 
	 	
	 	Log.i(TAG, "Query Time4:" + (System.currentTimeMillis() - time) + "ms");
	 	
	 	Cursor curPhones = findCursor(resolver, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		if (curPhones != null) {
			int colId = curPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
			int colType = curPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
			int colLabel = curPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
			int colNumber = curPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			while (curPhones.moveToNext()) {
				int contactId = curPhones.getInt(colId);		
	 			int type = curPhones.getInt(colType);
		 		String label = curPhones.getString(colLabel);
		 		String number = curPhones.getString(colNumber);
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					info.addPhone(type, label, number);
				}
			}
			curPhones.close();
		}
		
		Log.i(TAG, "Query Time5:" + (System.currentTimeMillis() - time) + "ms");
		
		Cursor curAddress = findCursor(resolver, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
		if (curAddress != null) {
			int colId = curAddress.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID);
			int colType = curAddress.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
			int colLabel = curAddress.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.LABEL);
			int colAddress = curAddress.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);

			while (curAddress.moveToNext())
			{
				int contactId = curAddress.getInt(colId);		
	 			int type = curAddress.getInt(colType);
		 		String label = curAddress.getString(colLabel);
		 		String address = curAddress.getString(colAddress);
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					info.addAddress(type, label, address); 
				}
			}
			curAddress.close();
		}
		
		Cursor curEmail = findCursor(resolver, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
		if (curEmail != null) {
			int colId = curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
			int colType = curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
			int colLabel = curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
			int colEmail = curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1);

			while (curEmail.moveToNext())
			{
				int contactId = curEmail.getInt(colId);		
	 			int type = curEmail.getInt(colType);
		 		String label = curEmail.getString(colLabel);
		 		String email = curEmail.getString(colEmail);
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					info.addEmail(type, label, email); 
				}
			}
			curEmail.close();
		}
		
		Cursor curIms = findCursor(resolver, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
		if (curIms != null) {
			int colId = curIms.getColumnIndex(ContactsContract.CommonDataKinds.Im.CONTACT_ID);
			int colProtocol = curIms.getColumnIndex(Im.PROTOCOL);
			int colData = curIms.getColumnIndex(Im.DATA);

			while (curIms.moveToNext())
			{
				int contactId = curIms.getInt(colId);	
				ContactInfo info = mapContacts.get(contactId);
				if(info != null)
				{
					int protocol = curIms.getInt(colProtocol);
					String data = curIms.getString(colData);
					if(protocol == ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ)
						info.setmQQ(data);
					else if(protocol == ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN)
						info.setmMsn(data);
				}
			}
			curIms.close();
		}
		
		if(USE_SIM_DATA)
		{
			List<ContactInfo> lstContactInSim = findAllSimContact(context);
			if(lstContactInSim != null)
			{
				for(int i = 0; i < lstContactInSim.size(); i++)
				{
					ContactInfo infoContactInSim = lstContactInSim.get(i);
					String name = infoContactInSim.getmLstName().get(0);
					if(name != null)
					{
						ContactInfo infoContact = mapContactsName.get(name);
						if(infoContact != null)
						{
							//Do not add to contact if it is already exist
						//	infoContact.addPhone(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, null, 
						//			infoContactInSim.getmLstHomePhone().get(0));
						}
						else
						{
							lstContactInfo.add(infoContactInSim);
						}
					}
				}
			}
		}
		Log.i(TAG, "Query Time:" + (System.currentTimeMillis() - time) + "ms");
		
		// count MD5
		for(int i = 0 ; i < lstContactInfo.size() ; i++){
			lstContactInfo.get(i).countMD5();
		}
		
		if(saveToLatest)
			mLatestContactsInfo = lstContactInfo;
		return lstContactInfo;
	}
	
	public static List<String> findContactNameByPhone(Context context, String phoneNumber)
	{  	/*	
		List<String> lstNames = null;
        Uri uri;            
        String[] projection;             
        uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,                   
        		Uri.encode(phoneNumber));            
        projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };        
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);            
        if (cursor != null) 
        {                
        	if (cursor.moveToFirst()) 
        	{
        		lstNames = new ArrayList<String>();
        		do
        		{
        			String name = cursor.getString(0);         
        			if(name != null && name.length() > 0)
        				lstNames.add(name);
        		}while(cursor.moveToNext());
        	}        		       
        	cursor.close();            
        } 
        return lstNames;     */
		
		List<String> lstPhone = new ArrayList<String>();
		if(mLatestContactsInfo != null)
		{
			for(int i = 0; i < mLatestContactsInfo.size(); i++)
			{
				ContactInfo infoContact = mLatestContactsInfo.get(i);
				if(infoContact.containsPhone(phoneNumber))
				{
					String name = infoContact.getDefaultName();
					if(name != null)
						lstPhone.add(name);
						
				}
			}
		}
		return lstPhone;
    }  

	public static List<String> findPhonesById(Context context, String id)
	{
		List<String> lstPhone = new ArrayList<String>();
		if(mLatestContactsInfo != null)
		{
			for(int i = 0; i < mLatestContactsInfo.size(); i++)
			{
				if(id.equals("" + mLatestContactsInfo.get(i).getmId()))
				{
					if(mLatestContactsInfo.get(i).getmLstMobile() != null)
						lstPhone.addAll(mLatestContactsInfo.get(i).getmLstMobile());
					if(mLatestContactsInfo.get(i).getmLstHomePhone() != null)
						lstPhone.addAll(mLatestContactsInfo.get(i).getmLstHomePhone());
					if(mLatestContactsInfo.get(i).getmLstOtherPhone() != null)
						lstPhone.addAll(mLatestContactsInfo.get(i).getmLstOtherPhone());
					if(mLatestContactsInfo.get(i).getmLstWorkPhone() != null)
						lstPhone.addAll(mLatestContactsInfo.get(i).getmLstWorkPhone());
					break;
				}
			}
		}
		
		return lstPhone;
	}
	
	public static JSONObject getJsonObjectOfContacts(List<ContactInfo> infoContacts) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonContacts = new JSONArray();

		long time = System.currentTimeMillis();
		if(infoContacts != null)
		{
			for (ContactInfo info : infoContacts) {
				jsonContacts.put(info.toJsonObject());
			}
			try {
				jsonObject.put("contact", jsonContacts);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Log.i(TAG, "Convert Time:" + (System.currentTimeMillis() - time) + "ms");

		return jsonObject;
	}
	public static List<ContactInfo> parseFromJson(Context context, JSONObject obj)
	{
		List<ContactInfo> lstContact = new ArrayList<ContactUtil.ContactInfo>();
		JSONArray array = obj.optJSONArray("contact");
		if(array != null)
		{
			for(int i = 0; i < array.length(); i++)
			{
				try {
					JSONObject objContact = array.getJSONObject(i);
					if(objContact != null)
					{
						ContactInfo info = new ContactInfo();
						if(info.parseFromJson(objContact))
						{
							lstContact.add(info);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return lstContact;
	}

	public static List<ContactInfo> getmLatestContactsInfo() {
		return mLatestContactsInfo;
	}

	public static void setmLatestContactsInfo(List<ContactInfo> mLatestContactsInfo) {
		ContactUtil.mLatestContactsInfo = mLatestContactsInfo;
	}
	
	protected static List<ContactInfo> findAllSimContact(Context context) {
		List<ContactInfo> lstContacts = null;
		lstContacts = findAllSimContact(context, "content://icc/adn", SIM_CONTACT_ID_START_1);
		List<ContactInfo> lstSim = findAllSimContact(context, "content://sim/adn", SIM_CONTACT_ID_START_2);
		if(lstSim != null && lstSim.size() > 0)
		{
			if(lstContacts == null)
				lstContacts = lstSim;
			else
				lstContacts.addAll(lstSim);
		}
		return lstContacts;
	}
	
	protected static List<ContactInfo> findAllSimContact(Context context, String url, int startId) {
		// 读取SIM卡手机号,有两种可能:content://icc/adn与content://sim/adn
		List<ContactInfo> lstContacts = new ArrayList<ContactUtil.ContactInfo>();
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse(url));
			Uri uri = intent.getData();
			String[] projection = {"_id", "name", "number"};
			Cursor cursor = context.getContentResolver().query(uri, projection, null,
					null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int colName = cursor.getColumnIndex("name");
					int colId = cursor.getColumnIndex("_id");
					int colPhone = cursor.getColumnIndex("number");

					if(colId != -1 && colName != -1 && colPhone != -1)
					do
					{
						ContactInfo sci = new ContactInfo();
						sci.setmId(cursor.getInt(colId) + startId);
						sci.addName(cursor.getString(colName), null, null, null);
						sci.addPhone(ContactsContract.CommonDataKinds.Phone.TYPE_HOME,
								"", cursor.getString(colPhone));
						lstContacts.add(sci);
					}while(cursor.moveToNext());
				}

				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstContacts;
	}
	
	public static long addContact(Context context, ContactInfo contact)
	{
		ContentValues values=new ContentValues();
		if(contact.getmLstName() != null && contact.getmLstName().size() > 0)
			values.put(ContactsContract.Contacts.DISPLAY_NAME, contact.getmLstName().get(0));
		
		Uri rawContactUri=context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		contact.setmId((int)rawContactId);
		getmLatestContactsInfo().add(contact);
		// inser name
		//DISPLAY_NAME
		List<String> array=contact.getmLstName();
		List<String> givenNameArray=contact.getmLstFirstName();
		List<String> familyNameArray=contact.getmLstLastName();
		List<String> middleNameArray=contact.getmLstMiddleName();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
				values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, array.get(i));
				if(givenNameArray!=null)
					values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenNameArray.get(i));
				if(familyNameArray!=null)
					values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyNameArray.get(i));
				if(middleNameArray!=null)
					values.put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleNameArray.get(i));
				context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
	
		//NAME
		array=contact.getmLstNickName();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
				values.put(CommonDataKinds.Nickname.NAME, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		//insert email data
		array=contact.getmLstHomeEmail();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				values.put(Email.TYPE, Email.TYPE_HOME);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstHomeEmail();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				values.put(Email.TYPE, Email.TYPE_HOME);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstWorkEmail();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				values.put(Email.TYPE, Email.TYPE_WORK);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstOtherEmail();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				values.put(Email.TYPE, Email.TYPE_OTHER);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		//insert phone data
		array=contact.getmLstHomePhone();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.TYPE, Phone.TYPE_HOME);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstWorkPhone();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.TYPE, Phone.TYPE_WORK);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstOtherPhone();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.TYPE, Phone.TYPE_OTHER);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstMobile();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				values.put(Email.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		//insert company data
		array=contact.getmLstCompany();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Organization.TYPE, Organization.TYPE_WORK);
				values.put(Organization.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstTitle();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Organization.TYPE, Organization.TITLE);
				values.put(Organization.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		//insert address data
		array=contact.getmLstHomeAddress();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE,StructuredPostal.CONTENT_ITEM_TYPE);
				values.put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME);
				values.put(Organization.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstWorkAddress();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE,StructuredPostal.CONTENT_ITEM_TYPE);
				values.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
				values.put(Organization.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		array=contact.getmLstOtherAddress();
		if(array!=null&&array.size()>0){
			for (int i = 0; i < array.size(); i++) {
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE,StructuredPostal.CONTENT_ITEM_TYPE);
				values.put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER);
				values.put(Organization.DATA, array.get(i));
				context.getContentResolver().insert(
		                android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
		}
		
		
		return rawContactId;
	}
	
	public static long modifyContact(Context context, ContactInfo contact)
	{ 
		long ret = 0;
		//int id = contact.getmId();
		ret = addContact(context, contact);
		//if(ret != id)
			//delContact(context,id);//do not delete,or the phone will be deleted.
		return ret;
	}
	
	
	public static boolean delContact(Context context, int id)
	{
		 ContentResolver contentResolver = context.getContentResolver();  
		 contentResolver.delete(RawContacts.CONTENT_URI, CommonDataKinds.StructuredName.CONTACT_ID + "=?", new String[]{String.valueOf(id)}); 
		 ContactInfo info = findContactById(id);
		 if(info != null)
			 getmLatestContactsInfo().remove(info);
		 return true;  
	}
	
	public static ContactInfo findContactById(int id)
	{
		ContactInfo info = null;
		if(mLatestContactsInfo != null)
		{
			for(int i = 0; i < mLatestContactsInfo.size(); i++)
			{
				if(mLatestContactsInfo.get(i).getmId() == id)
				{
					info = mLatestContactsInfo.get(i);
					break;
				}
			}
		}
		
		return info;
	}
	


	/**
     * 获取联系人头像
     *
     * @param people_id
     * @return
     */
    public static byte[] getPhoto(Context context,String people_id) {
            String photo_id = null;
            String selection1 = ContactsContract.Contacts._ID + " = " + people_id;
            Cursor cur1 = context.getContentResolver().query(
                            ContactsContract.Contacts.CONTENT_URI, null, selection1, null,
                            null);
            if (cur1.getCount() > 0) {
                    cur1.moveToFirst();
                    photo_id = cur1.getString(cur1
                                    .getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    Log.i(TAG, "photo_id:" + photo_id);   // 如果没有头像，这里为空值
            }
            cur1.close();
           
            String selection = null;
            if(photo_id == null){                       
                    return null;
            }else{
                    selection = ContactsContract.Data._ID + " = " + photo_id;
            }
           
            String[] projection = new String[] { ContactsContract.Data.DATA15 };
            Cursor cur = context.getContentResolver().query(
                            ContactsContract.Data.CONTENT_URI, projection, selection, null, null);
            cur.moveToFirst();
            byte[] contactIcon = null;
            if(cur.getCount() > 0){
            	 contactIcon = cur.getBlob(0);
            	 LogOutput.i(TAG, "conTactIcon:" + contactIcon);
            }
            cur.close();
            
            return contactIcon;
    }
    
    public static String getContactName(Context context,String number)
    {
  	  String name = null;
  	  if((number == null)||(number.equals("")))
  		  return name;
  	  String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
  				ContactsContract.CommonDataKinds.Phone.NUMBER };

  		try {
  			Cursor cursor = context.getContentResolver().query(
  					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
  					projection,
  					ContactsContract.CommonDataKinds.Phone.NUMBER + "= '"
  							+ number + "'", null, null);

  			if (cursor != null) {
  				for (int i = 0; i < cursor.getCount(); ++i) {
  					cursor.moveToPosition(i);
  					int nameFieldColumnIndex = cursor
  							.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
  					name = cursor.getString(nameFieldColumnIndex);
  				}
  			}

  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		
  	  return name;
    }
    
    public static Bitmap getBitMapFromNumberAndName(Context context,String number,String name)
    {
    	Bitmap bitmap = null;
    	if (number.startsWith("+86")) {
    		number = number.substring(3);
		} else if (number.startsWith("+")) {
			number = number.substring(1);
		}else if (number.startsWith("12520")) {
			number = number.substring(5);
		}else if (number.startsWith("17951")) {
			number = number.substring(5);
		}
    	Uri uriNumber=Uri.parse("content://com.android.contacts/"  
                + "data/phones/filter/" +number);
    	try{
			Cursor cursor=context.getContentResolver().query(uriNumber,null , null,null,null);
			if(cursor.getCount()>0)
			{				
				cursor.moveToFirst();
				if(name != null)
				{
					do{
						if(cursor.getString(cursor.getColumnIndex("display_name")).equals(name))
							break;
					}while(cursor.moveToNext());
				}
				
				Long contacteID=cursor.getLong(cursor.getColumnIndex("contact_id"));
				Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contacteID);
				
				InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri); 
	            if(input!=null)
	            {
	        		bitmap = BitmapFactory.decodeStream(input); 	
				}             
			}
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return bitmap;
    }
}
