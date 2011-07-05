package org.yavdr.yadroid.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class YaDroidDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "yavdrDB";

	private static final int DATABASE_VERSION = 1;
    
    public YaDroidDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE vdr (name VARCHAR( 120 ) NOT NULL, "
        		+ "ip VARCHAR( 16 ) NOT NULL, port INT NOT NULL, "
				+ "PRIMARY KEY (ip, port) )");
        
        db.execSQL("CREATE TABLE settings (setting INT NOT NULL, "
				+ "value VARCHAR( 120 ) NOT NULL, PRIMARY KEY (setting)) ");
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE vdr");
		db.execSQL("DROP TABLE settings");
		onCreate(db);
	}
}