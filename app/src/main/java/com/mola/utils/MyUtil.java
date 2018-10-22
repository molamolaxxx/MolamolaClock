package com.mola.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.mola.Alarm.AlarmSetting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/4/20.
 */

public class MyUtil {
    public MyUtil() {
    }
    //智能判断once闹钟的精确时间
    public int getMonth(){
        Calendar calendar=Calendar.getInstance();
        return calendar.get(Calendar.MONTH);
    }
    public int getDay(int hour,int min,int timeType){
        Calendar calendar=Calendar.getInstance();
        if(todayOrTomorrow(hour,min,timeType))
            return calendar.get(Calendar.DAY_OF_MONTH);
        else
            return calendar.get(Calendar.DAY_OF_MONTH)+1;

    }
    public int getWeekDay(int hour,int min,int timeType){
        Calendar calendar=Calendar.getInstance();
        if(todayOrTomorrow(hour,min,timeType))
            return calendar.get(Calendar.DAY_OF_WEEK);
        else
            return calendar.get(Calendar.DAY_OF_WEEK)+1;
    }
    //判断闹钟是今天的还是明天的
    public boolean todayOrTomorrow(int hour,int min,int timeType){
        Calendar calendar=Calendar.getInstance();
        if(changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))<hour){
            //闹钟当天执行
            return true;
        }
        else if (changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))>hour){
            //闹钟次日执行
            return false;
        }
        else if(changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))==hour)
            if (calendar.get(Calendar.MINUTE)>=min)
            {
                //次日执行
                return false;
            }
            else
            {
                //当天执行
                return true;
            }
        return true;
    }
    // 判断闹钟距离现在还有多久
    public String during(int hour,int min,int timeType){
        int duringMin,duringHour;
        Calendar calendar=Calendar.getInstance();
        if(todayOrTomorrow(hour,min,timeType))
        {
            //今天响
            if(min>calendar.get(Calendar.MINUTE))
            {
                duringMin=min-calendar.get(Calendar.MINUTE);
                duringHour=hour-changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY));
            }
            else {
                duringMin=min+60-calendar.get(Calendar.MINUTE);
                duringHour=hour-changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))-1;
            }
        }
        else {
            if (min>calendar.get(Calendar.MINUTE)){
                duringMin=min-calendar.get(Calendar.MINUTE);
                duringHour=hour-changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))+24;
            }
            else {
                duringMin=min-calendar.get(Calendar.MINUTE)+60;
                duringHour=hour-changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))+23;
            }
            //明天响
        }
        if (duringMin==60){
            duringHour++;
            duringMin=0;
            if(duringHour==24)  duringHour=0;
        }
        return "距离响铃还有"+duringHour+"小时"+duringMin+"分钟";
    }
    //检测应用在前台还是后台
    public boolean isOnBackGround(Context context){
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }
    public boolean isAppRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName()))
                return true;
        }
        return false;
    }
    //检测某一服务是否进行
    public static boolean isServiceWorked(Context context, String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
    public ArrayList<AlarmSetting> sort(ArrayList<AlarmSetting> list){
        Comparator<AlarmSetting> mcp=new Comparator<AlarmSetting>() {
            @Override
            public int compare(AlarmSetting o1, AlarmSetting o2) {
                if (o1.getHours()!=o2.getHours())
                    return o1.getHours()-o2.getHours();
                else
                    return o1.getMinus()-o2.getMinus();
            }
        };
        Collections.sort(list,mcp);
        return list;
    }
    public int changeTimePartHour(int timePart,int hour){
        switch (timePart){
            case 0://北京时间
            {
                return hour;
            }
            case 1://伦敦时间
            {
                return (hour+17)%24;
            }
            case 2://纽约时间
            {
                return (hour+12)%24;
            }
            case 3://东京时间
            {
                return (hour+1)%24;
            }
        }
        return -99;
    }
    public String getPlusZeroTime(int time){
        if(time<10)
            return "0"+time;
        return Integer.toString(time);
    }
}
