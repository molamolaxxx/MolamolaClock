package com.mola.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.mola.molamolaclock.R;
import com.mola.molamolaclock.SettingActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by molamola on 2018/7/25.
 * 可以快速搭建警告栏
 */

public class MolaQuickBuild {
    public MolaQuickBuild(){

    }
    public static void buildAlert(AlertDialog mDialog, Context mContext){
        AlertDialog.Builder alertBuilder=new AlertDialog.Builder(mContext);
        mDialog = alertBuilder.create();
        mDialog.setTitle("mola警报");
        mDialog.setMessage("没保存就想退出吗？");
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "保存设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "不保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mDialog.show();
    }
}

