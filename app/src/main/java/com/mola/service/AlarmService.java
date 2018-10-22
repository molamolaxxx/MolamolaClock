package com.mola.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mola.Alarm.AlarmFormActivity;
import com.mola.Alarm.AlarmSetting;
import com.mola.Alarm.RecordTime;
import com.mola.database.DAOManager;
import com.mola.molamolaclock.Clock;
import com.mola.molamolaclock.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.mola.IMyAidlInterface;
import com.mola.utils.MyUtil;

public class AlarmService extends Service {
    private ArrayList<AlarmSetting> alarmSettingsList;
    private DAOManager daoManager;
    private Context context;
    private Thread alarmListener;
    private Thread listenerOfAlarmListener;
    private boolean everydayRepeat=true;
    private boolean weekdayRepeat=true;
    private boolean isFragmentBroadcastReg=false;

    private AlarmManager alarmManager;
    private AlarmManager alarmManager2;
    private MyUtil myUtil;
    private Intent intent;
    private PendingIntent pendingIntent;
    private static long TIME_INTERVAL=60*1000*5;
    MyBinder binder;
    MyConn conn;
    Handler handler=new Handler();
    Timer timer;

    PowerManager pm;
    PowerManager.WakeLock mWakeLock;
    private MediaPlayer mediaPlayer;//针对智能识别的下策
    //是否是时间启动检测进程
    public AlarmService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //使cpu不休眠
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
//        mWakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
//        mWakeLock.acquire();

        startMediaPlayer();

        binder=new MyBinder();
        myUtil=new MyUtil();
        conn=new MyConn();
        context=this;

        frontService();
        daoManager=new DAOManager(this);
        alarmSettingsList=daoManager.findAllData();

        initBroadcast();
        //方案二，线程监听
        //打开检测线程的检测线程
        listenerOfAlarmListener=new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar calendar=Calendar.getInstance();
                final int t=calendar.get(Calendar.SECOND);
                if(t==0) {
                    /*
                    使用timer
                     */
                    timer=new Timer();
                    TimerTask timerTask=new TimerTask() {
                        @Override
                        public void run() {
                            checkAlarm();
                            Log.d("thread", "check");
                        }
                    };
                    timer.schedule(timerTask,0,12000);
                      Log.d("thread", "ting");
                      listenerOfAlarmListener.interrupt();
                      listenerOfAlarmListener=null;
                    /*
                    使用alarmmanager检测是否需要打开播放器欺骗cpu
                     */
                    alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager2=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    intent=new Intent("checkcheck");
                    pendingIntent=PendingIntent.getBroadcast(context,0,intent,0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+5000,TIME_INTERVAL,pendingIntent);

                }
                else {
                    handler.postDelayed(this,1000);
                    Log.d("thread", "pao");
                }
            }
        });
        listenerOfAlarmListener.start();
    }
    public void startMediaPlayer(){
        if(mediaPlayer != null){    //如果存在mediaplayer就释放
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
            Log.d("先施放个", "startMediaPlayer: ");
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.empty);
        mediaPlayer.setLooping(true);
        if (!mediaPlayer.isPlaying())
            mediaPlayer.start();
        Log.d("mediaplayer", "顺利创建");
    }
    public void stopMediaPlayer(){
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            Log.d("停止放歌", "stopMediaPlayer: ");
        }
        Log.d("不存在播放器", "stopMediaPlayer: ");
    }
    public void initBroadcast(){
        //如果没注册过，注册
        //check广播
        if (!isFragmentBroadcastReg)
        {
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction("checkcheck");
            BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //TODO
                        Log.d("check", "checkcheckcheck");
                        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AlarmService.this.getClass().getCanonicalName());
                        mWakeLock.acquire();
                        int hourLater = 0;
                        int minLater = 30;
                        switch (getSharedPreferences("my_setting", MODE_PRIVATE).getInt("timeMode", 1)) {
                            case 0: {
                                hourLater = 0;
                                minLater = 12;
                                break;
                            }
                            case 1: {
                                hourLater = 0;
                                minLater = 30;
                                break;
                            }
                            case 2: {
                                hourLater = 1;
                                minLater = 20;
                                break;
                            }
                        }
                        //检测x分钟内是否有闹钟存在

                        daoManager.getReadableDatabase();
                        Log.d("检测", hourLater + "+" + minLater);
                        if (checkIsFutureAlarmExist(daoManager.findAllData(), hourLater, minLater)
                                ||checkLastRecordTime(daoManager.findLastTimeImformation())) {
                            startMediaPlayer();
//                            if (checkLastRecordTime(daoManager.findLastTimeImformation())) {
//                                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 5, TIME_INTERVAL, pendingIntent);
//                                Log.d("再次开启", "setRepeating");
//                            }
                        } else
                            stopMediaPlayer();
                        if(Build.VERSION.SDK_INT>=23)
                            alarmManager2.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime()+9*60*1000,pendingIntent);
                        //将小时和秒写入数据库
                        Calendar calendar = Calendar.getInstance();
                        daoManager.insertDataTest(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                        mWakeLock.release();
                }
            };
            isFragmentBroadcastReg=true;
            this.registerReceiver(broadcastReceiver,intentFilter);
        }
    }
    //检测该次计时与上次计时的差距，若超过，则打开播放器，若不超过，关闭播放器
    public Boolean checkLastRecordTime(RecordTime recordTime){
        int hour=recordTime.getHour();
        int min=recordTime.getMin();
        Log.d(hour+":"+min, "lasttime");
        int timeType=getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0);
        Calendar calendar=Calendar.getInstance();
        if(myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))-hour!=0) {
            Log.d("不在范围内，打开播放器报警", "checkLastRecordTime");
            return true;
        }
        if(calendar.get(Calendar.MINUTE)-min<=12){
            Log.d("在范围内，关闭","checkLastRecordTime");
            return false;
        }
        Log.d("不在范围内，打开播放器报警", "checkLastRecordTime");
        return true;
    }
    //未来某段时间内是否存在时钟
    //参数后两个是我希望检测的时间范围，如0小时30分钟
    public Boolean checkIsFutureAlarmExist(ArrayList<AlarmSetting> alarmSettingsList,int setHour,int setMin){
        for (int i=0;i<alarmSettingsList.size();i++){
            if(!alarmSettingsList.get(i).isClockOpen())
                continue;
            int hour=alarmSettingsList.get(i).getHours();
            int min=alarmSettingsList.get(i).getMinus();
            int duringMin,duringHour;
            int timeType=getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0);
            Calendar calendar=Calendar.getInstance();
            if(myUtil.todayOrTomorrow(hour,min,timeType))
            {
                //今天响
                if(min>calendar.get(Calendar.MINUTE))
                {
                    duringMin=min-calendar.get(Calendar.MINUTE);
                    duringHour=hour-myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY));
                }
                else {
                    duringMin=min+60-calendar.get(Calendar.MINUTE);
                    duringHour=hour-myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))-1;
                }
            }
            else {
                if (min>calendar.get(Calendar.MINUTE)){
                    duringMin=min-calendar.get(Calendar.MINUTE);
                    duringHour=hour-myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))+24;
                }
                else {
                    duringMin=min-calendar.get(Calendar.MINUTE)+60;
                    duringHour=hour-myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR_OF_DAY))+23;
                }
                //明天响
            }
            if (duringMin==60){
                duringHour++;
                duringMin=0;
                if(duringHour==24)  duringHour=0;
            }
            Log.d("during", duringHour+":"+duringMin);
            if(duringHour<setHour) {
                Log.d("可以开始欺骗cpu了", "checkIsFutureAlarmExist: ");
                return true;
            }
            else if(duringHour==setHour)
                    if (duringMin<=setMin)
                    {
                        Log.d("可以开始欺骗cpu了", "checkIsFutureAlarmExist: ");
                        return true;
                    }
        }
        Log.d("还不能欺骗cpu，会耗电", "checkIsFutureAlarmExist: ");
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
        mWakeLock.release();
        timer.cancel();
        //开启远程服务
        AlarmService.this.startService(new Intent(AlarmService.this, MyGuardService.class));
        //绑定远程服务
        AlarmService.this.bindService(new Intent(AlarmService.this, MyGuardService.class), conn, Context.BIND_IMPORTANT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.bindService(new Intent(AlarmService.this, MyGuardService.class), conn, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    public void checkAlarm(){
        Calendar cl=Calendar.getInstance();
        int hour,min,weekday;
        daoManager.getReadableDatabase();
        alarmSettingsList=daoManager.findAllData();
        for (int i=0;i<alarmSettingsList.size();i++){
            final AlarmSetting as=alarmSettingsList.get(i);
            hour=myUtil.changeTimePartHour(
                    getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0),cl.get(Calendar.HOUR_OF_DAY));
            min=cl.get(Calendar.MINUTE);
            weekday=cl.get(Calendar.DAY_OF_WEEK);
            if(as.isClockOpen())
                switch (as.getRepeat()){
                    case 0:{
                        if (hour==as.getHours()&&min==as.getMinus())
                            {
                                ring(as);
                                ///五秒后删除闹钟
                                Timer timer=new Timer();
                                TimerTask tm=new TimerTask() {
                                    @Override
                                    public void run() {
                                        //删除闹钟
                                        Log.d("Ring", "闹钟已经响完");
                                        daoManager.getReadableDatabase();
                                        daoManager.changeData(as.getId(), 7, "0",context);
                                        daoManager.closeDatabase();
                                        sendBroadcast();//发送广播让clock刷新list
                                    }
                                };
                                timer.schedule(tm,1000);
                            }
                        break;
                    }
                    //everyday
                    case 1:{
                        if (hour==as.getHours()&&min==as.getMinus())
                        {
                            if (everydayRepeat)
                            {
                                ring(as);
                                everydayRepeat=false;
                                daoManager.closeDatabase();
                            }
                        }
                        else {
                            if (!everydayRepeat)
                                everydayRepeat=true;
                        }
                        break;
                    }
                    //weekday
                    case 2:{
                        if (hour==as.getHours()&&min==as.getMinus())
                        {
                            if (weekdayRepeat)
                            {
                                if(weekday!=7&&weekday!=1) {
                                    ring(as);
                                    weekdayRepeat =false;
                                    daoManager.closeDatabase();
                                }
                            }
                        }
                        else {
                            if (!weekdayRepeat)
                                weekdayRepeat=true;
                        }
                        break;
                    }

                }

        }
    }
    public void ring(final AlarmSetting as){
        Log.d("Ring", "ring:"+as.getRepeat());
        //点亮屏幕
        Intent intent=new Intent(AlarmService.this, AlarmFormActivity.class);
        intent.putExtra("isShake",as.isShake());
        intent.putExtra("musicType",as.getSound());
        intent.putExtra("tixing",as.getAlarmText());
        startActivity(intent);
        //响铃

   }
    public void sendBroadcast(){
        Intent intent=new Intent("deletalarmfromlistview");
        intent.putExtra("flag",1);
        sendBroadcast(intent);
    }
    private void frontService() {
        //构建通知栏，
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setContentTitle("molamola")
                .setContentText("是个闹钟")
                .setSmallIcon(R.drawable.label);
        Intent notificationIntent = new Intent(this, Clock.class);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
        startForeground(1, notification);
    }

    class MyBinder extends IMyAidlInterface.Stub{
        @Override
        public String getServiceName() throws RemoteException {
            return AlarmService.class.getSimpleName();
        }
    }
    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("远程服务kill,重新拉起", "onServiceDisconnected: ");

            //开启远程服务
            AlarmService.this.startService(new Intent(AlarmService.this, MyGuardService.class));
            //绑定远程服务
            AlarmService.this.bindService(new Intent(AlarmService.this, MyGuardService.class), conn, Context.BIND_IMPORTANT);
        }

    }

}
