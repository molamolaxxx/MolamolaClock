package com.mola.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.mola.IMyAidlInterface;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyGuardService extends Service {
    MyConn conn;
    MyBinder binder;
    Context context;
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        conn = new MyConn();
        binder = new MyBinder();
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                Log.d("thread2", "check");
                if (isAppRunning(MyGuardService.this))
                    Log.d("app还在运行", "run: ");
                else
                {
                    Log.d("app被干了，需要重启", "run: ");
                    //重启
                    PackageManager pm=getPackageManager();
                    String packageName=context.getPackageName();
                    Intent launchIntentForPackage = pm.getLaunchIntentForPackage(packageName);
                    startActivity(launchIntentForPackage);
                }
            }
        };
        timer.schedule(timerTask,0,1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("远程服务启动", "onStartCommand: ");
        this.bindService(new Intent(this, AlarmService.class), conn, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return MyGuardService.class.getSimpleName();
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("本地服务kill,重新拉起", "onServiceDisconnected: ");

            //开启本地服务
            MyGuardService.this.startService(new Intent(MyGuardService.this, AlarmService.class));
            //绑定本地服务
            MyGuardService.this.bindService(new Intent(MyGuardService.this, AlarmService.class), conn, Context.BIND_IMPORTANT);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //开启本地服务
            MyGuardService.this.startService(new Intent(MyGuardService.this, AlarmService.class));
            //绑定本地服务
            MyGuardService.this.bindService(new Intent(MyGuardService.this, AlarmService.class), conn, Context.BIND_IMPORTANT);
    }
    //检测app是否运行
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
}

