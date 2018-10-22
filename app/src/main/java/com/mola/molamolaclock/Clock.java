package com.mola.molamolaclock;


import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mola.database.DAOManager;
import com.mola.service.AlarmService;
import com.mola.service.MyGuardService;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Manifest;


public class Clock extends FragmentActivity {
     //----fragment
    private SlidingMenu mSlidingMenu;
    private TextView ClockTextView;
    private TextView AlarmTextView;
     private ViewPager mViewPager;
    //动画图片
    private ImageView cursor;
    LinearLayout clockHeadLayout;
    //波纹动画
    private ObjectAnimator rippleAnimator;
    //动画图片偏移量

    private int position_one;
    private int position_two;

    //当前页卡编号
    private int currIndex;
    //存放Fragment
    private ArrayList<Fragment> fragmentArrayList;
    //管理Fragment
    private FragmentManager fragmentManager;
    private boolean isFragmentBroadcastReg=false;
    private long firstTime=0;
    public Context context;
    ClockFragment clockFragment;
    AlarmFragment alarmFragment;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1: {
                    textColorChange(currIndex);
                    clockFragment.physicalAnime(currIndex);
                }
                break;
                default:
            }
        }
    };
    //----菜单里的控件
    private LinearLayout userNameTextField;
    private LinearLayout beiZhuTextField;

    private TextView userNameText;
    private TextView beiZhuText;

    private AlertDialog mAlertDialog;

    private LinearLayout settingField;
    private LinearLayout attackField;
    private LinearLayout chatField;
    private LinearLayout quickAlarmField;

    private ImageView settingImage;
    private ImageView attackImage;
    private ImageView chatImage;
    private ImageView quickAlarmImage;
    private SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //---------------------设置全屏-----------
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_clock);
        context = this;
        clockHeadLayout=(LinearLayout)findViewById(R.id.clockHead);
        //-------------fragment相关
        //权限申请

        initTextView();
        initImageAnime();
        initFragment();
        initViewPaper(0);
        initSlidingMenu();
        //启动服务
        initBroadcast();
        //startService(new Intent(this, LockService.class));
        startService(new Intent(this, AlarmService.class));
        startService(new Intent(this, MyGuardService.class));
        if(getSharedPreferences("my_setting",MODE_PRIVATE).getBoolean("isAutoClearAlarm",false))
        {
            DAOManager daoManager=new DAOManager(Clock.this);
            daoManager.getReadableDatabase();
            daoManager.deleteAllCloseAlarm();
            //清除时间数据库
            //daoManager.DeleteTestData();
            Toast.makeText(Clock.this,"自动清除成功",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    //----------------fragment----
    public void initTextView(){         //初始化标题栏
        ClockTextView=(TextView)findViewById(R.id.ClockTextView);
        AlarmTextView=(TextView)findViewById(R.id.AlarmTextView);
        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/FlemishScriptBT.ttf");
        ClockTextView.setTypeface(typeface);
        AlarmTextView.setTypeface(typeface);
        ClockTextView.setOnClickListener(new MyOnClickListener(0));
        AlarmTextView.setOnClickListener(new MyOnClickListener(1));
        textColorChange(0);
    }
    public class MyOnClickListener implements View.OnClickListener{
        private int index = 0 ;
        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(index);
        }
    }
    //---------------初始化动画
    public void initImageAnime(){
        cursor=(ImageView)findViewById(R.id.cursor);//找到滑块
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //获取屏幕分辨率宽度
        int screen=dm.widthPixels;
        position_one = (int) (screen / 4.0);
        position_two=(int)(screen/4.0*3);
        Animation animation;
        animation=new TranslateAnimation(0,position_one,0,0);
        animation.setFillAfter(true);
        animation.setDuration(800);
        cursor.setAnimation(animation);
    }
    public void initFragment(){
        clockFragment=new ClockFragment();
        alarmFragment=new AlarmFragment();
        fragmentArrayList=new ArrayList<>();
        fragmentArrayList.add(clockFragment);
        fragmentArrayList.add(alarmFragment);
        fragmentManager=getSupportFragmentManager();
    }
    public void initViewPaper(int i){
        mViewPager=(ViewPager) findViewById(R.id.vPager);
        mViewPager.setAdapter(new MFragmentPagerAdapter(fragmentManager ,fragmentArrayList));
        mViewPager.setOffscreenPageLimit(1);//缓存一个页面
        mViewPager.setCurrentItem(i);        //设置当前页面

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                Animation animation=null;
                /**
                 * position为滑完之后的页面
                 */
                currIndex=position;
                switch (position){
                    //页卡2到1
                    case 0:
                    {
                        animation = new TranslateAnimation(position_two, position_one, 0, 0);
                        mSlidingMenu.setMode(SlidingMenu.LEFT);
                        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                        break;
                    }
                    case 1:
                    {
                        animation = new TranslateAnimation(position_one, position_two, 0, 0);
                        //夺取slidingmenu焦点,设置成不可打开
                        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
                        alarmFragment.addButtonAnim();
                        break;
                    }
                }
                currIndex = position;
                animation.setFillAfter(true);// true:图片停在动画结束位置
                animation.setDuration(300);
                cursor.setAnimation(animation);
                Timer timer=new Timer();
                TimerTask timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        Message message=handler.obtainMessage();
                        message.what=1;
                        handler.sendMessage(message);
                    }
                };
                timer.schedule(timerTask,300);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("回调", "onResume: ");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //----singleTask模式下intent驱动调用此函数
        //----刷新listView
        alarmFragment.openDatabase();
        alarmFragment.refreshListView();
        alarmFragment.onOperateListView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //解除绑定
    public void textColorChange(int which){
        if (which==1) {
            ClockTextView.setTextColor(getResources().getColor(R.color.white));
            AlarmTextView.setTextColor(getResources().getColor(R.color.gray));

        }
        else {
            AlarmTextView.setTextColor(getResources().getColor(R.color.white));
            ClockTextView.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("clock", "onKeyDown");
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
            {
                if (System.currentTimeMillis() - firstTime > 2000) {
                    firstTime = System.currentTimeMillis();
                    Toast.makeText(this, "再点一次退出", Toast.LENGTH_SHORT).show();
                    //test,需要删除
                    return false;
                }
                break;
            }
            case KeyEvent.KEYCODE_MENU:
            {
                Log.d("menu", "onKeyDown: ");
                break;
            }

        }
        return super.onKeyDown(keyCode,event);

    }

    //接受service的广播
    public void initBroadcast(){
        //如果没注册过，注册
        if (!isFragmentBroadcastReg)
        {
            IntentFilter intentFilter=new IntentFilter();
            IntentFilter intentFilter2=new IntentFilter();
            intentFilter.addAction("deletalarmfromlistview");
            intentFilter2.addAction("time_Type_change");
            BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle data= intent.getExtras();
                    //TODO
                    Log.d("clock", "onReceive:"+data.getInt("flag"));
                    alarmFragment.openDatabase();
                    alarmFragment.refreshListView();
                }
            };
            BroadcastReceiver broadcastReceiver2=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //TODO
                    clockFragment.setTimeType();
                }
            };
                isFragmentBroadcastReg=true;
                Log.d("value", "initBroadcast: "+isFragmentBroadcastReg);
                Clock.this.registerReceiver(broadcastReceiver,intentFilter);
                Clock.this.registerReceiver(broadcastReceiver2,intentFilter2);
        }
    }
    public void setI(int i){
        beiZhuText.setText(String.valueOf(i));
    }
    //初始化滑动菜单
    public void initSlidingMenu(){
        mSlidingMenu = new SlidingMenu(this);
        //设置从下弹出/滑出SlidingMenu//设置占满屏幕
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setBehindOffset(getWindowManager().getDefaultDisplay().getWidth() / 5*3);
        mSlidingMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);    //绑定到哪一个Activity对象
        mSlidingMenu.setMenu(R.layout.slidingmenu);//设置弹出的SlidingMenu的布局文件
        mSharedPreferences=getSharedPreferences("user",MODE_PRIVATE);

        initUserNameText();
        initBeiZhuText();
        initSetting();
        initAttack();
        initChat();
        initQuickAlarm();
    }
    public void initUserNameText(){
        userNameTextField=(LinearLayout)findViewById(R.id.userName);
        userNameText=(TextView)findViewById(R.id.userNameText);
        userNameText.setText(mSharedPreferences.getString("userName","此处为用户名"));
        userNameTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(userNameText,1);
            }
        });
    }
    public void initBeiZhuText(){
        beiZhuTextField=(LinearLayout)findViewById(R.id.beiZhu);
        beiZhuText=(TextView)findViewById(R.id.beiZhuText);
        if(mSharedPreferences.getString("beiZhu","点击输入备注").length()>=14)
            beiZhuText.setText(mSharedPreferences.getString("beiZhu","点击输入备注").substring(0,11)+"...");
        else
            beiZhuText.setText(mSharedPreferences.getString("beiZhu","点击输入备注"));
        beiZhuTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(beiZhuText,2);
            }
        });
    }
    //初始化设置
    public void initSetting(){
        settingField=(LinearLayout) findViewById(R.id.settingField);
        settingImage=(ImageView)findViewById(R.id.setting);
        settingField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //动画
                Animation animation= AnimationUtils.loadAnimation(Clock.this,R.anim.click_rotate);
                settingImage.startAnimation(animation);
                final Timer timer=new Timer();
                TimerTask timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(Clock.this,SettingActivity.class);
                        startActivity(intent);
                        timer.cancel();
                    }
                };
                timer.schedule(timerTask,400);
                //跳转到设置页面
            }
        });
    }
    public void initAttack(){
        attackField=(LinearLayout) findViewById(R.id.attackField);
        attackField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Clock.this,"敬请期待",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void initChat(){
        chatField=(LinearLayout) findViewById(R.id.chatField);
        chatField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Clock.this,"敬请期待",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void initQuickAlarm(){
        quickAlarmField=(LinearLayout)findViewById(R.id.quickAlarm);
        quickAlarmImage=(ImageView)findViewById(R.id.quickAlarmImage);
        quickAlarmField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shakeAnim();
                final Timer timer=new Timer();
                TimerTask timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(Clock.this,QucikAlarmActivity.class);
                        startActivity(intent);
                        timer.cancel();
                    }
                };
                timer.schedule(timerTask,400);
            }
        });
    }
    public void shakeAnim(){
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -20);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setDuration(380);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        quickAlarmImage.setAnimation(animation);
    }
    public void showDialog(final TextView textView, final int type){
        final SharedPreferences.Editor editor=mSharedPreferences.edit();
        AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this);
        mAlertDialog = alertBuilder.create();
        ///
        final EditText editText=new EditText(this);
        //
        editText.setHeight(230);
        editText.setWidth(400);
        editText.setGravity(Gravity.CENTER);
        editText.setTextSize(30);
        editText.setText(textView.getText());
        //
        if(type==1) {
            mAlertDialog.setTitle("昵称");
            editText.setText(textView.getText());
        }
        else {
            mAlertDialog.setTitle("输入备注提醒");
            editText.setText(mSharedPreferences.getString("beiZhu","点击输入备注"));
        }
        mAlertDialog.setView(editText);
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String string=editText.getText().toString();
                if(string.length()>=14) {
                    if (type==1){
                        Toast.makeText(Clock.this, "昵称超出长度限制", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        editor.putString("beiZhu",string);
                        editor.commit();
                        string = string.substring(0, 11) + "...";
                        textView.setText(string);
                    }
                }
                else {
                    if (type == 1) {
                        if(string.equals(""))
                        {
                            Toast.makeText(Clock.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        editor.putString("userName", string);
                        editor.commit();
                        textView.setText(string);
                    } else {
                        editor.putString("beiZhu", string);
                        editor.commit();
                        textView.setText(string);
                    }
                }
                Toast.makeText(Clock.this, "输入成功", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mAlertDialog.show();
    }
}
