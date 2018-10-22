package com.mola.molamolaclock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mola.Alarm.CreateAlarmActivity;
import com.mola.database.DAOManager;
import com.mola.database.DBHelper;
import com.mola.setting.AboutActivity;
import com.mola.setting.Setting;
import com.mola.utils.DataCleanManager;

import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends FragmentActivity {
    private RelativeLayout timeSetting;
    private RelativeLayout timeMode;
    private RelativeLayout about;
    private RelativeLayout personalTimeSetting;
    private LinearLayout commit,delete;
    private AlertDialog textDialog;
    private AlertDialog timeSettingDialog;
    private AlertDialog timeModeDialog;
    private TextView timeSettingTextView;//时区
    private TextView timeModeTextView;//省电模式
    private Switch autoClearAlarm;
    private Button clear;
    private Button clearSave;

    private Setting setting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setting=getSettingInformation();
        initViewOnClickListeners();
        initSwitchs();
    }
    private void initViewOnClickListeners(){
        //设定时区
        timeModeTextView=(TextView)findViewById(R.id.time_mode);
        switch (setting.getTimeMode()){
            case 0:{
                timeModeTextView.setText("超能省电");
                break;
            }
            case 1:{
                timeModeTextView.setText("正常耗电");
                break;
            }
            case 2:{
                timeModeTextView.setText("精准定时");
                break;
            }
        }
        timeSettingTextView=(TextView)findViewById(R.id.time_type);
        switch (setting.getTimeType()){
            case 0:{
                timeSettingTextView.setText("北京时间");
                break;
            }
            case 1:{
                timeSettingTextView.setText("伦敦时间");
                break;
            }
            case 2:{
                timeSettingTextView.setText("纽约时间");
                break;
            }
            case 3:{
                timeSettingTextView.setText("东京时间");
                break;
            }
        }
        timeSetting=(RelativeLayout)findViewById(R.id.time_setting);
        timeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("timeset", "onClick: ");
                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(SettingActivity.this);
                final String[] strings={"北京时间","伦敦时间","纽约时间","东京时间"};
                alertBuilder.setSingleChoiceItems(strings, setting.getTimeType(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:{
                                timeSettingTextView.setText("北京时间");
                                setting.setTimeType(0);
                                break;
                            }
                            case 1:{
                                timeSettingTextView.setText("伦敦时间");
                                setting.setTimeType(1);
                                break;
                            }
                            case 2:{
                                timeSettingTextView.setText("纽约时间");
                                setting.setTimeType(2);
                                break;
                            }
                            case 3:{
                                timeSettingTextView.setText("东京时间");
                                setting.setTimeType(3);
                                break;
                            }
                        }
                        timeSettingDialog.dismiss();
                    }
                });
                timeSettingDialog=alertBuilder.create();
                timeSettingDialog.setTitle("设置时区");
                timeSettingDialog.show();
            }
        });
        //关于
        timeMode=(RelativeLayout)findViewById(R.id.time_mode_layout);
        timeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("timeset", "onClick: ");
                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(SettingActivity.this);
                final String[] strings={"超能省电","正常耗电","精准定时"};
                alertBuilder.setSingleChoiceItems(strings, setting.getTimeMode(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:{
                                timeModeTextView.setText("超能省电");
                                setting.setTimeMode(0);
                                break;
                            }
                            case 1:{
                                timeModeTextView.setText("正常耗电");
                                setting.setTimeMode(1);
                                break;
                            }
                            case 2:{
                                timeModeTextView.setText("精准定时");
                                setting.setTimeMode(2);
                                break;
                            }

                        }
                        timeModeDialog.dismiss();
                    }
                });
                timeModeDialog=alertBuilder.create();
                timeModeDialog.setTitle("设置后台唤醒");
                timeModeDialog.show();
            }
        });
        about=(RelativeLayout)findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        //设置个人信息
        personalTimeSetting=(RelativeLayout)findViewById(R.id.personal_setting);
        personalTimeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("per", "onClick: ");
            }
        });
        //clear
        clear=(Button)findViewById(R.id.clear_alarm);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DAOManager daoManager=new DAOManager(SettingActivity.this);
                daoManager.getReadableDatabase();
                daoManager.deleteAllCloseAlarm();
                Intent intent=new Intent(SettingActivity.this,Clock.class);
                startActivity(intent);
                Toast.makeText(SettingActivity.this,"清除成功",Toast.LENGTH_SHORT).show();
            }
        });
        //clear-save
        clearSave=(Button)findViewById(R.id.clear_save);
        clearSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataCleanManager.cleanInternalCache(SettingActivity.this);
                new DAOManager(SettingActivity.this).DeleteTestData();
                Toast.makeText(SettingActivity.this,"缓存清除成功",Toast.LENGTH_SHORT).show();
            }
        });
        //commit
        commit=(LinearLayout)findViewById(R.id.commit2);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存设置
                saveSettingInformation();
                Toast.makeText(SettingActivity.this,"已保存设置",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //delete
        delete=(LinearLayout)findViewById(R.id.delete2);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(SettingActivity.this);
                final Animation animation= AnimationUtils.loadAnimation(SettingActivity.this,R.anim.click_rotate);
                textDialog = alertBuilder.create();
                textDialog.setTitle("mola警报");
                textDialog.setMessage("没保存就想退出吗？");
                textDialog.setButton(DialogInterface.BUTTON_POSITIVE, "保存设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除
                        delete.startAnimation(animation);
                        Toast.makeText(SettingActivity.this,"已保存设置",Toast.LENGTH_SHORT).show();
                        Timer timer=new Timer();
                        TimerTask timerTask=new TimerTask() {
                            @Override
                            public void run() {
                             saveSettingInformation();
                             finish();
                            }
                        };
                        timer.schedule(timerTask,350);
                    }
                });
                textDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                textDialog.show();
            }
        });
    }
    private void initSwitchs(){
        autoClearAlarm=(Switch)findViewById(R.id.auto_clear_alarm);
        autoClearAlarm.setChecked(setting.getAutoClearAlarm());
        autoClearAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setting.setAutoClearAlarm(isChecked);
            }
        });
    }
    public Setting getSettingInformation(){
        Setting setting;
        SharedPreferences sharedPreferences=getSharedPreferences("my_setting",MODE_PRIVATE);
        setting=new Setting(
        sharedPreferences.getInt("timeType",0),
        sharedPreferences.getBoolean("isAutoClearAlarm",false)
        ,sharedPreferences.getInt("timeMode",1));
        return setting;
    }
    public void saveSettingInformation(){
        SharedPreferences sharedPreferences=getSharedPreferences("my_setting",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("timeType",setting.getTimeType());
        editor.putBoolean("isAutoClearAlarm",setting.getAutoClearAlarm());
        editor.putInt("timeMode",setting.getTimeMode());
        editor.commit();
        Intent intent=new Intent("time_Type_change");
        sendBroadcast(intent);
    }
}
