package com.mola.Alarm;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mola.molamolaclock.R;
import com.mola.utils.MyUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AlarmFormActivity extends FragmentActivity {
    private LinearLayout clockAlarmLayout;
    //电源管理者
    private PowerManager pm;
    //唤醒锁
    private PowerManager.WakeLock mWakeLock;

    private TextView yearAndMonthText;
    private TextView weekDayText;
    private ImageView clockpanel;
    private ImageView secondImage;
    private ImageView minImage;
    private ImageView hourImage;
    private TextView tixingText;
    private TextView eClockScreen;
    private TextView defEclockScreen;
    private MyUtil myUtil;
    private int timeType;
    private int musicType;
    private boolean isShake;
    private MediaPlayer mediaPlayer;

    //震动器
    private Vibrator vib;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                {
                    setClock();
                    setTime();
                }
                break;
                default:
            }
        }
    };
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            Message message=handler.obtainMessage();
            message.what=1;
            handler.sendMessage(message);
            handler.postDelayed(this,500);
        }
    };
    Thread eClock=new Thread(runnable);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //显示在上方
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_alarm_form);
        clockAlarmLayout=(LinearLayout)findViewById(R.id.activity_alarm_form);
        myUtil=new MyUtil();
        timeType=getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0);
        setScreenTouchEvent();
        pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_DIM_WAKE_LOCK,"SimpleTimer");
        mWakeLock.acquire();
        //触摸屏幕事件
        setScreenTouchEvent();
        //初始化控件
        tixingText=(TextView)findViewById(R.id.tixing);
        secondImage=(ImageView) findViewById(R.id.secondAlarm);
        minImage=(ImageView)findViewById(R.id.minAlarm);
        hourImage=(ImageView)findViewById(R.id.hourAlarm);
        clockpanel=(ImageView)findViewById(R.id.imageViewAlarm);
        //start a new thread
        eClock.start();
        weekDayText=(TextView)findViewById(R.id.dayAlarm);
        yearAndMonthText=(TextView)findViewById(R.id.yearAndMonthAlarm);
        eClockScreen=(TextView)findViewById(R.id.textViewAlarm);
        defEclockScreen=(TextView)findViewById(R.id.deftextViewAlarm);
        setLedTextView();
        //获得时间信息
        setYearAndWeekday();
        getInformation();
        //震动
        vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        if(isShake)
            shake();
        if (musicType!=2)
            playMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        Log.d("ds", "onDestroy: ");
        eClock.interrupt();
        eClock=null;
        sendBroadcast(new Intent("checkcheck"));
        if (isShake)
            stopShake();
        if (musicType!=2)
            stopMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }
    public void setScreenTouchEvent(){
        clockAlarmLayout.setOnTouchListener(new View.OnTouchListener() {
            float x,y;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        x=event.getX();
                        y=event.getY();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:break;
                    case MotionEvent.ACTION_UP:{
                        if (Math.abs(x-event.getX())>30||Math.abs(y-event.getY())>30)
                            finish();
                        break;
                    }
                }
                return true;
            }
        });
    }
    public void setClock(){
        Calendar calendar=Calendar.getInstance();
        int second=calendar.get(Calendar.SECOND);
        int min=calendar.get(Calendar.MINUTE);
        int hour=myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR));
        float realS = second;
        float realM = min + realS / 60.0f;
        float realH = hour + realM / 60.0f;
        //--------角度计算
        float rotateS = 360f / 60f * realS+1.2f;
        float rotateM = 360f / 60f * realM;
        float rotateH = 360f / 12f * realH;
        //-------旋转-----
        secondImage.setRotation(rotateS);
        minImage.setRotation(rotateM);
        hourImage.setRotation(rotateH);
    }
    public void setYearAndWeekday(){
        Calendar calendar=Calendar.getInstance();
        int date=calendar.get(Calendar.DAY_OF_MONTH);
        int weekDay=calendar.get(Calendar.DAY_OF_WEEK);
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        Log.d("1", weekDay+"+"+year+"+"+month+"+"+date);
        yearAndMonthText.setText(year+"年"+month+"月"+date+"日");
        switch (weekDay){
            case 2:weekDayText.setText("星期一");break;
            case 3:weekDayText.setText("星期二");break;
            case 4:weekDayText.setText("星期三");break;
            case 5:weekDayText.setText("星期四");break;
            case 6:weekDayText.setText("星期五");break;
            case 7:weekDayText.setText("星期六");break;
            case 1:weekDayText.setText("星期日");break;
        }
    }
    public void getInformation(){
        Intent intent=getIntent();
        isShake=intent.getBooleanExtra("isShake",false);
        musicType=intent.getIntExtra("musicType",0);
        tixingText.setText(intent.getStringExtra("tixing"));
    }
    public void setLedTextView(){
        Typeface typeface=Typeface.createFromAsset(this.getAssets(),"fonts/digital-7.ttf");
        eClockScreen.setTypeface(typeface);
        defEclockScreen.setTypeface(typeface);
    }
    public void setTime(){
        Calendar calendar=Calendar.getInstance();
        String second=myUtil.getPlusZeroTime(calendar.get(Calendar.SECOND));
        String min=myUtil.getPlusZeroTime(calendar.get(Calendar.MINUTE));
        String hour=myUtil.getPlusZeroTime(
                myUtil.changeTimePartHour(timeType,calendar.get(Calendar.HOUR)));
        eClockScreen.setText(hour+":"+min+":"+second);
    }
    //震动
    public void shake(){
        vib.vibrate(new long[]{500,600,500,600},0);
        shakeAnim();
    }
    //停止震动
    public void stopShake(){
        vib.cancel();
    }
    public void shakeAnim(){
        TranslateAnimation animation = new TranslateAnimation(0, 0, 20, -20);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setDuration(200);
        animation.setRepeatCount(300);
        animation.setRepeatMode(Animation.REVERSE);
        clockpanel.setAnimation(animation);
    }
    public void playMusic(){
        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, AudioManager.ADJUST_SAME);
//        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.ADJUST_LOWER);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            AssetFileDescriptor fileDescriptor = this.getAssets().openFd("ring/xx.mp3");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),
                    fileDescriptor.getStartOffset());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
//        mediaPlayer=MediaPlayer.create()
//        mediaPlayer.setLooping(true);
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
//        mediaPlayer.start();
//        final AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
//            MediaPlayer.OnPreparedListener onPreparedListener=new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mediaPlayer.start();
//                }
//            };
//            try {
//                if(mediaPlayer !=null){
//                    mediaPlayer.stop();
//                }
//                mediaPlayer.setOnPreparedListener(onPreparedListener);
//                mediaPlayer.prepareAsync();
//            }catch (IllegalStateException e){
//                e.printStackTrace();
//            }
    }
    public void stopMusic(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
