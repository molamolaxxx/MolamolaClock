package com.mola.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mola.molamolaclock.Clock;

/**
 * Created by Administrator on 2018/3/9.
 * 连接数据库，创建表格所用
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="alarm.db";
    private static final int DB_VERSION=1;
    public static final String TABLE_NAME="alarmsetting";
    public static final String TABLE_NAME_test="testtime";
    public DBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE "+TABLE_NAME+"(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hour INTEGER," +
                "min INTEGER," +
                "repeat INTEGER," +
                "sound INTEGER," +
                "shake," +
                "text," +
                "open,"+
                "month INTEGER,"+
                "weekday INTEGER,"+
                "day INTEGER)";
        db.execSQL(sql);
        String sqlTest="CREATE TABLE "+TABLE_NAME_test+"(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hour INTEGER," +
                "min INTEGER)" ;
        db.execSQL(sqlTest);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
