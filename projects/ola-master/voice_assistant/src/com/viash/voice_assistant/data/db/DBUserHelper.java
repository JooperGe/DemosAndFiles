package com.viash.voice_assistant.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.viash.voice_assistant.entity.UserEntity;
public class DBUserHelper extends BaseHelper {
	public DBUserHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase sqlitedatabase) {
		createTable(sqlitedatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqlitedatabase, int i, int j) {
		
	}
	
	public void addUser(String username,String password,String phone){
		SQLiteDatabase  database = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("username", username);
		values.put("password",password);
		values.put("phone",phone);
		database.insert("al_user", "",values);
		database.close();
	}
	
	public void updateUserInfo(int id,String username,String password,String phone){
		SQLiteDatabase  database = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("username", username);
		values.put("password",password);
		values.put("phone",phone);
		database.update("al_user", values, "id = ?",new String[]{String.valueOf(id)});
		database.close();
	}
	
	public void cleanUser(){
		SQLiteDatabase  database = getWritableDatabase();
		database.execSQL("delete from al_user");
	}
	
	public UserEntity getUserInfo(){
		UserEntity entity =null;
		SQLiteDatabase  database = getReadableDatabase();
		Cursor cursor = database.query("al_user", new String[]{"id","username","password","phone"}, null, null, null, null, null);
		while(cursor.moveToNext()){
			entity = new UserEntity();
			entity.setId(cursor.getInt(cursor.getColumnIndex("id")));
			entity.setUsername(cursor.getString(cursor.getColumnIndex("username")));
			entity.setPassword(cursor.getString(cursor.getColumnIndex("password")));
			entity.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
		}
		cursor.close();
		database.close();
		
		return entity;
	}
	

}
