package com.mola.lockscreen;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mola.molamolaclock.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class LockActivity extends FragmentActivity {
    private TextView yearAndMonthText;
    private TextView weekDayText;
    private TextView eClockScreen;
    private TextView defEclockScreen;
    private ImageView secondImage;
    private ImageView minImage;
    private ImageView hourImage;

    private LinearLayout clockLockLayout;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                {
                    setTime();
                    setClock();
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
        //将界面设置在系统锁屏的前面
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_lock);
        clockLockLayout=(LinearLayout)findViewById(R.id.ClockLock);
        setScreenTouchEvent();

        eClockScreen=(TextView) findViewById(R.id.textViewLock);
        defEclockScreen=(TextView) findViewById(R.id.deftextViewLock);
        setLedTextView();
        secondImage=(ImageView) findViewById(R.id.secondLock);
        minImage=(ImageView)findViewById(R.id.minLock);
        hourImage=(ImageView)findViewById(R.id.hourLock);
        //start a new thread
        eClock.start();
        weekDayText=(TextView)findViewById(R.id.day);
        yearAndMonthText=(TextView)findViewById(R.id.yearAndMonth);
        setYearAndWeekday();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public void setTime(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        eClockScreen.setText(simpleDateFormat.format(date));
    }
    public void setClock(){
        Calendar calendar=Calendar.getInstance();
        int second=calendar.get(Calendar.SECOND);
        int min=calendar.get(Calendar.MINUTE);
        int hour=calendar.get(Calendar.HOUR);
        float realS = second;
        float realM = min + realS / 60.0f;
        float realH = hour + realM / 60.0f;
        //--------角度计算
        float rotateS = 360f / 60f * realS;
        float rotateM = 360f / 60f * realM;
        float rotateH = 360f / 12f * realH;
        //-------旋转-----
        secondImage.setRotation(rotateS);
        minImage.setRotation(rotateM);
        hourImage.setRotation(rotateH);
    }
    public void setLedTextView(){
        Typeface typeface=Typeface.createFromAsset(this.getAssets(),"fonts/digital-7.ttf");
        eClockScreen.setTypeface(typeface);
        defEclockScreen.setTypeface(typeface);
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
    public void setScreenTouchEvent(){
        clockLockLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:break;
                    case MotionEvent.ACTION_MOVE:break;
                    case MotionEvent.ACTION_UP:finish();break;
                }
                return true;
            }
        });
    }
}
