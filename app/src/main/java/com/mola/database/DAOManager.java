package com.mola.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.mola.Alarm.AlarmSetting;
import com.mola.Alarm.RecordTime;
import com.mola.utils.MyUtil;

import java.util.ArrayList;


/**
 * Created by Administrator on 2018/3/11.
 */

public class DAOManager {
    private DBHelper dbHelper;
    static MyUtil myUtil=new MyUtil();
    private SQLiteDatabase database;
    public DAOManager(Context context){
        dbHelper=new DBHelper(context);
        database=dbHelper.getReadableDatabase();
    }
    public void getReadableDatabase(){
        database=dbHelper.getReadableDatabase();
    }
    public void insertData(AlarmSetting alarmSetting,Context context){
        if (isTimeRepeated(alarmSetting)!=0)
        {
            Toast.makeText(context,"已存在相同时钟",Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values=new ContentValues();
        values.put("hour",alarmSetting.getHours());
        values.put("min",alarmSetting.getMinus());
        values.put("repeat",alarmSetting.getRepeat());
        values.put("sound",alarmSetting.getSound());
        values.put("shake",alarmSetting.isShake());
        values.put("text",alarmSetting.getAlarmText());
        values.put("open",alarmSetting.isClockOpen());
        putDifferentValue(alarmSetting,values,context);
        database.insert(DBHelper.TABLE_NAME,null,values);
        Log.d("database", "insertData:success");
    }
    public void insertDataTest(int hour,int min){
        ContentValues values=new ContentValues();
        values.put("hour",hour);
        values.put("min",min);
        database.insert(DBHelper.TABLE_NAME_test,null,values);
    }
    public ArrayList<String> findAllTestData(){
        ArrayList<String> arrayList=new ArrayList<>();
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME_test,null);
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String str=new String();
            str=cursor.getInt(1)+":"+cursor.getInt(2);
            arrayList.add(str);
        }
        cursor.close();
        return arrayList;
    }
    public void updateData(AlarmSetting alarmSetting,Context context){
        if (isTimeRepeated(alarmSetting)!=0&&isTimeRepeated(alarmSetting)!=alarmSetting.getId())
        {
            Toast.makeText(context,"已存在相同时钟",Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values=new ContentValues();
        values.put("hour",alarmSetting.getHours());
        values.put("min",alarmSetting.getMinus());
        values.put("repeat",alarmSetting.getRepeat());
        values.put("sound",alarmSetting.getSound());
        values.put("shake",alarmSetting.isShake());
        values.put("text",alarmSetting.getAlarmText());
        values.put("open",alarmSetting.isClockOpen());
        putDifferentValue(alarmSetting,values,context);
        database.update(DBHelper.TABLE_NAME,values,"_id=?",new String[]{Integer.toString(alarmSetting.getId())});
    }
    //返回相同时钟的id
    private int isTimeRepeated(AlarmSetting alarmSetting){
        int hour,min;
        hour=alarmSetting.getHours();
        min=alarmSetting.getMinus();
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME,null);
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            if(cursor.getInt(1)==hour&&cursor.getInt(2)==min)
                return cursor.getInt(0);
        }
        return 0;
    }
    public ArrayList<AlarmSetting> findAllData(){
        ArrayList<AlarmSetting> arrayList=new ArrayList<>();
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME,null);
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            AlarmSetting alarmSetting=new AlarmSetting();
            alarmSetting.setId(cursor.getInt(0));
            alarmSetting.setHours(cursor.getInt(1));
            alarmSetting.setMinus(cursor.getInt(2));
            alarmSetting.setRepeat(cursor.getInt(3));
            alarmSetting.setSound(cursor.getInt(4));
            alarmSetting.setShake(cursor.getInt(5)==1);
            alarmSetting.setAlarmText(cursor.getString(6));
            alarmSetting.setClockOpen(cursor.getInt(7)==1);
            alarmSetting.setMonth(cursor.getInt(8));
            alarmSetting.setWeekday(cursor.getInt(9));
            alarmSetting.setDay(cursor.getInt(10));
            arrayList.add(alarmSetting);
        }
        cursor.close();
        return arrayList;
    }
    public AlarmSetting findDataById(int id){
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME+" where _id="+id,null);
        cursor.moveToNext();
        AlarmSetting alarmSetting=new AlarmSetting();
        alarmSetting.setId(cursor.getInt(0));
        alarmSetting.setHours(cursor.getInt(1));
        alarmSetting.setMinus(cursor.getInt(2));
        alarmSetting.setRepeat(cursor.getInt(3));
        alarmSetting.setSound(cursor.getInt(4));
        alarmSetting.setShake(cursor.getInt(5)==1);
        alarmSetting.setAlarmText(cursor.getString(6));
        alarmSetting.setClockOpen(cursor.getInt(7)==1);
        return alarmSetting;
    }
    public void deleteData(int id){
        String sql="DELETE FROM "+DBHelper.TABLE_NAME+" WHERE _id="+id;
        database.execSQL(sql);
    }
    public void deleteTestData(int id){
        String sql="DELETE FROM "+DBHelper.TABLE_NAME_test+" WHERE _id="+id;
        database.execSQL(sql);
    }
    public void deleteAllCloseAlarm(){
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME,null);
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            //找寻并删除
            if(!(cursor.getInt(7)==1))
                deleteData(cursor.getInt(0));
        }
    }
    public RecordTime findLastTimeImformation(){
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME_test,null);
        RecordTime recordTime=new RecordTime();
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            //找寻并删除
            if(i==cursor.getCount()-1)
            {
                recordTime.setHour(cursor.getInt(1));
                recordTime.setMin(cursor.getInt(2));
            }
        }
        return recordTime;
    }
    public void DeleteTestData(){
        Cursor cursor=database.rawQuery("select * from "+DBHelper.TABLE_NAME_test,null);
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            //找寻并删除
            if(i==cursor.getCount()-1)
                return;
            deleteTestData(cursor.getInt(0));
        }
    }
    public void changeData(int id,int position,String what,Context context){
        String field=new String();
        AlarmSetting alarmSetting=findDataById(id);
        switch (position){
            case 1:field="hour";break;
            case 2:field="min";break;
            case 3:field="repeat";break;
            case 4:field="sound";break;
            case 5:field="shake";break;
            case 6:field="text";break;
            case 7:field="open";break;
        }
        String sql="UPDATE "+DBHelper.TABLE_NAME+" SET "+field+" = "+what+" WHERE _id="+id;
        database.execSQL(sql);
        ContentValues values=new ContentValues();
        if(alarmSetting.getRepeat()==0)//闹钟为once类型
        {
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
        }
        else if (alarmSetting.getRepeat()==1){
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
        }
        else if (alarmSetting.getRepeat()==2){
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
        }
        database.update(DBHelper.TABLE_NAME,values,"_id=?",new String[]{Integer.toString(alarmSetting.getId())});
    }
    public void putDifferentValue(AlarmSetting alarmSetting,ContentValues values,Context context){
        if(alarmSetting.getRepeat()==0)//闹钟为once类型
        {
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
            Toast.makeText(context,myUtil.during(alarmSetting.getHours(),alarmSetting.getMinus(),
                    context.getSharedPreferences("my_setting",Context.MODE_PRIVATE).getInt("timeType",0)),Toast.LENGTH_SHORT).show();
        }
        else if (alarmSetting.getRepeat()==1){
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
            if(alarmSetting.isClockOpen())
                Toast.makeText(context,"设置每日重复闹钟成功！",Toast.LENGTH_SHORT).show();
        }
        else if (alarmSetting.getRepeat()==2){
            values.put("month",alarmSetting.getMonth());
            values.put("weekday",alarmSetting.getWeekday());
            values.put("day",alarmSetting.getDay());
            if(alarmSetting.isClockOpen())
                Toast.makeText(context,"设置工作日重复闹钟成功！",Toast.LENGTH_SHORT).show();
        }
    }
    public void closeDatabase(){
        database.close();
    }
}
