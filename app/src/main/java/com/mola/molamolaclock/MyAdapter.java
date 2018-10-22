package com.mola.molamolaclock;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mola.Alarm.AlarmSetting;
import com.mola.Alarm.CreateAlarmActivity;
import com.mola.database.DAOManager;
import com.mola.utils.MyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/2.
 */

public class MyAdapter extends SimpleAdapter {
    static MyUtil myUtil;
    private DAOManager daoManager;
    private Switch mSwitch;
    private Context nowContext;
    private ArrayList<AlarmSetting> listItem;
    public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        myUtil=new MyUtil();
        nowContext=context;
    }
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View v= super.getView(position, convertView, parent);
        mSwitch=(Switch)v.findViewById(R.id.clockSwitch);
        Log.d("getview", "getView: ");
        daoManager=new DAOManager(nowContext);
        listItem=myUtil.sort(daoManager.findAllData());
        if (listItem.get(position).isClockOpen()) {
            mSwitch.setChecked(true);
            Log.d("getview", "true");
        }
        else {
            mSwitch.setChecked(false);
            Log.d("getview", "false");
        }
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daoManager.getReadableDatabase();
                listItem=myUtil.sort(daoManager.findAllData());
                if(!listItem.get(position).isClockOpen()){
                    //mSwitch.setChecked(false);
                    daoManager.changeData(listItem.get(position).getId(), 7, "1",nowContext);
                    Toast.makeText(nowContext,"闹钟开启,"+
                           myUtil.during(listItem.get(position).getHours(),listItem.get(position).getMinus(),
                                   nowContext.getSharedPreferences("my_setting",Context.MODE_PRIVATE).getInt("timeType",0)),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    //mSwitch.setChecked(true);
                    daoManager.changeData(listItem.get(position).getId(), 7, "0",nowContext);
                    Toast.makeText(nowContext,"闹钟关闭",Toast.LENGTH_SHORT).show();
                }
                daoManager.closeDatabase();
            }
        });
        return v;
    }
    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

}
