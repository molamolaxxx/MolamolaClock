package com.mola.Alarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mola.database.DAOManager;
import com.mola.molamolaclock.Clock;
import com.mola.molamolaclock.R;
import com.mola.utils.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class CreateAlarmActivity extends FragmentActivity {
    private static final String TAG = "CreateAlarmActivity";
    AlarmSetting alarmSetting=new AlarmSetting();
    MyUtil myUtil;
    private boolean isEdit=false;//是否是编辑模式
    /*
    click区域
     */
    Handler handler=new Handler();

    Runnable runnable1=new Runnable() {
        @Override
        public void run() {
            minusPlus();
            handler.postDelayed(this,200);
        }
    };
    Runnable runnable2=new Runnable() {
        @Override
        public void run() {
            minusMinus();
            handler.postDelayed(this,200);
        }
    };
    Runnable runnable3=new Runnable() {
        @Override
        public void run() {
            hoursPlus();
            handler.postDelayed(this,400);
        }
    };
    Runnable runnable4=new Runnable() {
        @Override
        public void run() {
            hoursMinus();
            handler.postDelayed(this,400);
        }
    };
    private float mPosX=0,mPosY=0;

    private int moveFlag=-1;         //上滑还是下滑
    private TextView alarmClock;
    private RelativeLayout fieldRepeat;
    private RelativeLayout fieldSound;
    private RelativeLayout fieldShake;
    private RelativeLayout fieldText;

    private LinearLayout headLayout;

    private FrameLayout alarmEdit;
    //-------textfield
    private TextView clockTextView;
    private TextView Repeat;
    private TextView Sound;
    private TextView defAlarmClock;
    private Switch Shake;
    private TextView Text;
    //------imageview
    private LinearLayout commit;//勾
    private LinearLayout delete;//叉叉
    private ImageView clockBack;
    //----选择框
    private AlertDialog repeatDialog;
    private AlertDialog soundDialog;
    private AlertDialog textDialog;
    private AlertDialog weekdayDialog;
    //-----圆形选择框
    //------数据库连接管理
    private DAOManager daoManager;
    Animation animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);
        myUtil=new MyUtil();
        //
        delete=(LinearLayout)findViewById(R.id.delete);
        commit=(LinearLayout)findViewById(R.id.commit);
        clockBack=(ImageView)findViewById(R.id.clockback);
        /////
        fieldRepeat=(RelativeLayout)findViewById(R.id.Repeat_field);
        fieldSound=(RelativeLayout)findViewById(R.id.Sound_field);
        fieldShake=(RelativeLayout)findViewById(R.id.Shake_field);
        fieldText=(RelativeLayout)findViewById(R.id.Text_field);
        headLayout=(LinearLayout)findViewById(R.id.headLayout);
        /////
        clockTextView=(TextView)findViewById(R.id.ClockTextView);
        Repeat=(TextView)findViewById(R.id.Repeat);
        Sound=(TextView)findViewById(R.id.Sound);
        Shake=(Switch) findViewById(R.id.Shake);
        Text=(TextView)findViewById(R.id.Text);
        //圆形选框
        alarmEdit=(FrameLayout)findViewById(R.id.AlarmEdit);
        alarmClock=(TextView) findViewById(R.id.Alarm);
        defAlarmClock=(TextView) findViewById(R.id.DEfAlarm);
        setLedTextView();
        //初始化按钮
        initDelete();
        initCommit();
        initSwitch();
        //初始化点击
        initFieldRepeat();
        initFieldText();
        initFieldSound();
        //初始化滑动
        initAlarmEdit();
        //----数据库
        daoManager=new DAOManager(this);//连接打开数据库
        if(getIntent().getBooleanExtra("isEdit",false))
        {
            Log.d(TAG, "现在是编辑模式"+getIntent().getIntExtra("id",0));
            clockTextView.setText("Delete");
            alarmSetting=daoManager.findDataById(getIntent().getIntExtra("id",0));
            showEditData();//显示该时钟的信息
            isEdit=true;
            //删除闹钟
            animation= AnimationUtils.loadAnimation(this,R.anim.click_rotate);
            headLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(CreateAlarmActivity.this);
                    textDialog = alertBuilder.create();
                    textDialog.setTitle("Delete");
                    textDialog.setMessage("你想删除这个闹钟吗？");
                    textDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除
                            delete.startAnimation(animation);
                            Toast.makeText(CreateAlarmActivity.this,"Delete Success",Toast.LENGTH_SHORT).show();
                            Timer timer=new Timer();
                            TimerTask timerTask=new TimerTask() {
                                @Override
                                public void run() {
                                    //删除操作
                                    daoManager.deleteData(alarmSetting.getId());
                                    daoManager.closeDatabase();
                                    Intent intent=new Intent(CreateAlarmActivity.this,Clock.class);
                                    startActivity(intent);
                                }
                            };
                            timer.schedule(timerTask,350);
                        }
                    });
                    textDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    textDialog.show();
                }
            });
        }
    }
    //-----关掉当前页面
    public void initDelete(){
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void initCommit(){
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //commit,提交到数据库
                //智能判断闹钟的精确时间
                if (alarmSetting.getRepeat()==0){
                    alarmSetting.setMonth(myUtil.getMonth());
                    alarmSetting.setDay(myUtil.getDay(alarmSetting.getHours(),alarmSetting.getMinus(),
                            getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0)));
                    alarmSetting.setWeekday(myUtil.getWeekDay(alarmSetting.getHours(),alarmSetting.getMinus(),
                            getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0)));
                }
                else if (alarmSetting.getRepeat()==1){
                    alarmSetting.setMonth(111);
                    alarmSetting.setDay(111);
                    alarmSetting.setWeekday(111);
                }
                else if (alarmSetting.getRepeat()==2){
                    alarmSetting.setMonth(222);
                    alarmSetting.setDay(222);
                    alarmSetting.setWeekday(222);
                }
                Log.d(TAG, "showdetail"+alarmSetting.getDay()+alarmSetting.getWeekday()+alarmSetting.getMonth());
                if(isEdit)
                {
                    Intent intent=new Intent(CreateAlarmActivity.this,Clock.class);
                    daoManager.updateData(alarmSetting,CreateAlarmActivity.this);//update
                    daoManager.closeDatabase();
                    startActivity(intent);
                    return;
                }
                daoManager.insertData(alarmSetting,CreateAlarmActivity.this);
                daoManager.closeDatabase();
                //----------跳转
                Intent intent=new Intent(CreateAlarmActivity.this,Clock.class);
                intent.putExtra("isCommit",true);
                startActivity(intent);
            }
        });
        commit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //跳转到数据库管理模式测试
                Intent intent=new Intent("android.intent.DAOTestActivity");
                startActivity(intent);
                return true;
            }
        });
    }
    //初始化点击区域事件
    public void initFieldRepeat(){
        fieldRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入日程选择界面
                showRepeatDialog(v);
            }
        });
    }
    public void initFieldSound(){
        fieldSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入铃声选择界面
                showSoundDialog(v);
            }
        });
    }
    public void initFieldText(){
        fieldText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextDialog();
                //输入提醒文字
            }
        });
    }
    //初始化开关
    public void initSwitch(){
        Shake.setChecked(alarmSetting.isShake());
        Shake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //open
                    alarmSetting.setShake(true);
                }
                else {
                    //close
                    alarmSetting.setShake(false);
                }
            }
        });
    }
    //初始化选择框
    public void showRepeatDialog(View view) {
        final String[] items = {"once", "everyday", "weekday","custom"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Repeat");
        alertBuilder.setSingleChoiceItems(items, alarmSetting.getRepeat()>9?3:alarmSetting.getRepeat(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:{
                        Repeat.setText("once");   //更新ui
                        alarmSetting.setRepeat(0);//设置setting
                        break;
                    }
                    case 1:{
                        Repeat.setText("everyday");
                        alarmSetting.setRepeat(1);
                        break;
                    }
                    case 2:{
                        Repeat.setText("weekday");
                        alarmSetting.setRepeat(2);
                        break;
                    }
                    //Thursday 周四
                    //Friday  周五
                    //Saturday 周六
                    //Sunday  周日
                    case 3:{
                        final String[] weekdayItems = {"Monday", "Tuesday", "Wednesday","Thursday",
                                "Friday","Saturday","Sunday"};
                        final AlertDialog.Builder weekdayAlertBuilder = new AlertDialog.Builder(CreateAlarmActivity.this);
                        weekdayAlertBuilder.setTitle("Custom");
                        weekdayAlertBuilder.setSingleChoiceItems(weekdayItems, alarmSetting.getRepeat()%10, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                    {
                                        Repeat.setText("Monday");   //更新ui
                                        alarmSetting.setRepeat(30);//设置setting
                                        break;
                                    }
                                    case 1:
                                    {
                                        Repeat.setText("Tuesday");
                                        alarmSetting.setRepeat(31);
                                        break;
                                    }
                                    case 2:
                                    {
                                        Repeat.setText("Wednesday");
                                        alarmSetting.setRepeat(32);
                                        break;
                                    }
                                    case 3:
                                    {
                                        Repeat.setText("Thursday");   //更新ui
                                        alarmSetting.setRepeat(33);//设置setting
                                        break;
                                    }
                                    case 4:
                                    {
                                        Repeat.setText("Friday");
                                        alarmSetting.setRepeat(34);
                                        break;
                                    }
                                    case 5:
                                    {
                                        Repeat.setText("Saturday");
                                        alarmSetting.setRepeat(35);
                                        break;
                                    }
                                    case 6:
                                    {
                                        Repeat.setText("Sunday");   //更新ui
                                        alarmSetting.setRepeat(36);//设置setting
                                        break;
                                    }
                                }
                            weekdayDialog.dismiss();
                            }
                        });//set
                        weekdayDialog=weekdayAlertBuilder.create();
                        weekdayDialog.show();
                        break;
                    }//case4
                }
                repeatDialog.dismiss();
            }
        });
        repeatDialog=alertBuilder.create();
        repeatDialog.show();
    }
    public void showSoundDialog(View view) {
        final String[] items = {"翻车鱼创造的铃声", "custom", "none"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Sound");
        alertBuilder.setSingleChoiceItems(items, alarmSetting.getSound(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:{
                        Sound.setText("翻车鱼创造的铃声");   //更新ui
                        alarmSetting.setSound(0);//设置setting
                        break;
                    }
                    case 1:{
                        Sound.setText("custom");
                        alarmSetting.setSound(1);
                        break;
                    }
                    case 2:{
                        Sound.setText("none");
                        alarmSetting.setSound(2);
                        break;
                    }
                }
                soundDialog.dismiss();
            }
        });
        soundDialog=alertBuilder.create();
        soundDialog.show();
    }
    public void showTextDialog(){
        AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this);
        textDialog = alertBuilder.create();
        ///
        final EditText editText=new EditText(this);
        //
        editText.setHeight(230);
        editText.setWidth(400);
        editText.setGravity(Gravity.CENTER);
        editText.setTextSize(30);
        editText.setText(alarmSetting.getAlarmText());
        //
        textDialog.setTitle("MakeText");
        textDialog.setView(editText);
        textDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String string=editText.getText().toString();
                alarmSetting.setAlarmText(string);
                Text.setText(string);
            }
        });
        textDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        textDialog.show();
    }
    public void initAlarmEdit(){
        initAlarmClock();//初始化闹钟初始值为当前时间
        alarmEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //标志上滑还是下滑
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        clockBack.setBackgroundResource(R.drawable.scrubber_control_normal_holo);
                        mPosX=event.getX();
                        mPosY=event.getY();
                        if (mPosX>=alarmEdit.getWidth()/2)
                            if (mPosY>alarmEdit.getHeight()/2)
                                minusMinus();
                            else
                                minusPlus();
                        else
                            if (mPosY>alarmEdit.getHeight()/2)
                                hoursMinus();
                            else
                                hoursPlus();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:{
                        clockBack.setBackgroundResource(R.drawable.scrubber_control_normal_holo);
                        if (mPosX>=alarmEdit.getWidth()/2)
                            if (event.getY()-mPosY<0&&Math.abs(event.getY()-mPosY)>25)  //向上滑
                            {
                                if (moveFlag==0) stopNumber(0);
                                changeNumber(1);
                                moveFlag=1;
                            }
                            else if (event.getY()-mPosY>0&&Math.abs(event.getY()-mPosY)>25)
                            {
                                //向下
                                if (moveFlag==1) stopNumber(1);
                                moveFlag=0;
                                changeNumber(0);
                            }
                        if (mPosX<alarmEdit.getWidth()/2)
                                if (event.getY() - mPosY < 0 && Math.abs(event.getY() - mPosY) > 25)  //向上滑
                                {
                                    if (moveFlag==2) stopNumber(2);
                                    changeNumber(3);
                                    moveFlag = 3;
                                } else if (event.getY() - mPosY > 0 && Math.abs(event.getY() - mPosY) > 25) {
                                    //向下
                                    if (moveFlag==3) stopNumber(3);
                                    moveFlag = 2;
                                    changeNumber(2);
                                }

                        break;
                    }
                    case MotionEvent.ACTION_UP:{
                        clockBack.setBackgroundResource(R.drawable.scrubber_control_pressed_holo);
                        stopNumber(moveFlag);
                        break;
                    }
                }
                return true;
            }
        });
    }
    public void changeNumber(int flag){
        if(flag==1)
            handler.post(runnable1);
        else if (flag==0)
            handler.post(runnable2);
        else if (flag==3)
            handler.post(runnable3);
        else if (flag==2)
            handler.post(runnable4);

    }
    public void stopNumber(int flag){
        if (flag==1)
            handler.removeCallbacks(runnable1);
        else if(flag==0)
            handler.removeCallbacks(runnable2);
        else if (flag==3)
            handler.removeCallbacks(runnable3);
        else if (flag==2)
            handler.removeCallbacks(runnable4);
    }
    public void minusMinus(){
        alarmSetting.setMinus(alarmSetting.getMinus()-1);
        alarmClock.setText(getStringHour()+":"+getStringMin());
    }
    public void minusPlus(){
        alarmSetting.setMinus(alarmSetting.getMinus()+1);
        alarmClock.setText(getStringHour()+":"+getStringMin());
    }
    public void hoursMinus(){
        alarmSetting.setHours(alarmSetting.getHours()-1);
        alarmClock.setText(getStringHour()+":"+getStringMin());
    }
    public void hoursPlus(){
        alarmSetting.setHours(alarmSetting.getHours()+1);
        alarmClock.setText(getStringHour()+":"+getStringMin());
    }
    public String getStringHour(){
        if(alarmSetting.getHours()>=10)
            return Integer.toString(alarmSetting.getHours());
        else
            return "0"+Integer.toString(alarmSetting.getHours());
    }
    public String getStringMin(){
        if(alarmSetting.getMinus()>=10)
            return Integer.toString(alarmSetting.getMinus());
        else
            return "0"+Integer.toString(alarmSetting.getMinus());
    }
    //
    public void setLedTextView(){
        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/digital-7.ttf");
        alarmClock.setTypeface(typeface);
        defAlarmClock.setTypeface(typeface);
    }
    private void initAlarmClock(){       //初始化闹钟初始值为当前时间
        Calendar calendar=Calendar.getInstance();
        String min=myUtil.getPlusZeroTime(calendar.get(Calendar.MINUTE));
        String hour=myUtil.getPlusZeroTime(myUtil.changeTimePartHour(
                getSharedPreferences("my_setting",MODE_PRIVATE).getInt("timeType",0),calendar.get(Calendar.HOUR_OF_DAY)));
        alarmSetting.setHours(Integer.parseInt(hour));
        alarmSetting.setMinus(Integer.parseInt(min));
        alarmClock.setText(hour+":"+min);
    }
    ////
    public void showEditData(){
        switch (alarmSetting.getRepeat()){
            case 0:{
                Repeat.setText("once");   //更新ui
                break;
            }
            case 1:{
                Repeat.setText("everyday");
                break;
            }
            case 2:{
                Repeat.setText("weekday");
                break;
            }
            case 30:
            {
                Repeat.setText("Monday");   //更新ui
                break;
            }
            case 31:
            {
                Repeat.setText("Tuesday");
                break;
            }
            case 32:
            {
                Repeat.setText("Wednesday");
                break;
            }
            case 33:
            {
                Repeat.setText("Thursday");   //更新ui
                break;
            }
            case 34:
            {
                Repeat.setText("Friday");
                break;
            }
            case 35:
            {
                Repeat.setText("Saturday");
                break;
            }
            case 36:
            {
                Repeat.setText("Sunday");   //更新ui
                break;
            }
        }
        switch (alarmSetting.getSound()){
            case 0:{
                Sound.setText("翻车鱼创造的铃声");   //更新ui
                break;
            }
            case 1:{
                Sound.setText("custom");
                break;
            }
            case 2:{
                Sound.setText("none");
                break;
            }
        }
        if (alarmSetting.isShake())
            Shake.setChecked(true);
        else
            Shake.setChecked(false);
        Text.setText(alarmSetting.getAlarmText());
        alarmClock.setText(getStringHour()+":"+getStringMin());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        daoManager.closeDatabase();
    }

}
