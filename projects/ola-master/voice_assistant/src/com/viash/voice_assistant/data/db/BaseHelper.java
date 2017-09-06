package com.viash.voice_assistant.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseHelper extends SQLiteOpenHelper {
	protected final static String DATABASE_NAME = "aola";
	protected final static int DATABASE_VERSION = 1;
	public BaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		
	}
	@Override
	public void onCreate(SQLiteDatabase sqlitedatabase) {
		createTable(sqlitedatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqlitedatabase, int i, int j) {
		// TODO Auto-generated method stub

	}
	
	public void createTable(SQLiteDatabase sqlitedatabase){
		String sql =null;
		sql ="CREATE TABLE al_user(id INTEGER PRIMARY KEY  AUTOINCREMENT,username char(100), password CHAR(100),phone CHAR(20))";
		sqlitedatabase.execSQL(sql);
		
	}

}
