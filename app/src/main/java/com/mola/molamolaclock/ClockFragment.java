package com.mola.molamolaclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mola.openingstartanimation.LineDrawStrategy;
import com.mola.openingstartanimation.OpeningStartAnimation;
import com.mola.utils.MyDrawableUtils;
import com.mola.utils.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class ClockFragment extends Fragment {
    FrameLayout frameLayout;
    private TextView timeType;
    private MyUtil myUtil;
    private int timeTypeNum;
    private TextView eClockScreen;
    private TextView defEclockScreen;
    private ImageView clockPanel;
    private ImageView secondImage;
    private ImageView minImage;
    private ImageView hourImage;
    Animation animationText;
    Animation animationScale;
    private int rotateFlag;
    RotateAnimation rotateAnimation;
    public ClockFragment() {
        // Required empty public constructor
    }
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
    Runnable runnable=new Runnable() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myUtil=new MyUtil();
        eClock.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.clock_fragment, container, false);
        frameLayout=(FrameLayout)view.findViewById(R.id.clockLayout);

        eClockScreen=(TextView) view.findViewById(R.id.textView);
        defEclockScreen=(TextView) view.findViewById(R.id.DeftextView);
        timeType=(TextView)view.findViewById(R.id.time_type2);
        setTimeType();
        setLedTextView();
        ////
        clockPanel=(ImageView)view.findViewById(R.id.imageView1) ;
        secondImage=(ImageView) view.findViewById(R.id.second);
        minImage=(ImageView)view.findViewById(R.id.min);
        hourImage=(ImageView)view.findViewById(R.id.hour);
        ///////
        animationText= AnimationUtils.loadAnimation(getContext(),R.anim.clicktext);
        ////
        animationScale=AnimationUtils.loadAnimation(getContext(),R.anim.clickmola);
        startPointAnim();
        showClickText();
        //触摸显示mola的部分
        showClickClock();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    public void startPointAnim(){
        secondImage.startAnimation(animationScale);
        minImage.startAnimation(animationScale);
        hourImage.startAnimation(animationScale);
    }
    public void setTime(){
        Calendar calendar=Calendar.getInstance();
        String second=myUtil.getPlusZeroTime(calendar.get(Calendar.SECOND));
        String min=myUtil.getPlusZeroTime(calendar.get(Calendar.MINUTE));
        String hour=myUtil.getPlusZeroTime(myUtil.changeTimePartHour(timeTypeNum,calendar.get(Calendar.HOUR_OF_DAY)));
        eClockScreen.setText(hour+":"+min+":"+second);
    }

    public void setClock(){
        Calendar calendar=Calendar.getInstance();
        int second=calendar.get(Calendar.SECOND);
        int min=calendar.get(Calendar.MINUTE);
        int hour=myUtil.changeTimePartHour(timeTypeNum,calendar.get(Calendar.HOUR));
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
    public void setLedTextView(){
        Typeface typeface=Typeface.createFromAsset(getContext().getAssets(),"fonts/digital-7.ttf");
        eClockScreen.setTypeface(typeface);
        defEclockScreen.setTypeface(typeface);
    }
    //触摸text会向上升起
    private void showClickText(){
        eClockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               textAnim();
            }
        });
    }
    private void showClickClock(){
        rotateFlag=0;
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateAnimation=new RotateAnimation(rotateFlag*90,rotateFlag*90+90,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
                rotateAnimation.setDuration(400);
                rotateAnimation.setFillAfter(true);
                clockPanel.startAnimation(rotateAnimation);
                if(rotateFlag==0)
                    rotateFlag=1;
                else if(rotateFlag==1)
                    rotateFlag=2;
                else if(rotateFlag==2)
                    rotateFlag=3;
                else if(rotateFlag==3) {
                    rotateFlag = 0;
                    startPointAnim();
                    textAnim();
                }
                Intent intent=new Intent("android.intent.fragment");
                intent.putExtra("flag",rotateFlag);
                getActivity().sendBroadcast(intent);
            }
        });
    }
    //时钟文本的动画
    public void textAnim(){
        eClockScreen.startAnimation(animationText);
    }
    public void physicalAnime(int flag){
        if (flag==1) {
            startPointAnim();
        }
    }
    public void setTimeType(){
        timeTypeNum=getActivity().getSharedPreferences("my_setting", Context.MODE_PRIVATE).getInt("timeType",0);
        switch (timeTypeNum) {
            case 0:{
                timeType.setText("北京时间");
                break;
            }
            case 1:{
                timeType.setText("伦敦时间");
                break;
            }
            case 2:{
                timeType.setText("纽约时间");
                break;
            }
            case 3:{
                timeType.setText("东京时间");
                break;
            }
        }
    }
}




