package com.mola.database;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mola.Alarm.AlarmSetting;
import com.mola.molamolaclock.R;

import java.util.ArrayList;

public class DAOTestActivity extends AppCompatActivity {
    private Button delete;
    private Button show;
    private Button timeRepeat;
    private EditText idText;
    private ImageView mola;
    private int id;
    Animation animation;
    private DAOManager daoManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daotest);
        delete=(Button)findViewById(R.id.DeleteById);
        show=(Button)findViewById(R.id.showDatabase);
        timeRepeat=(Button)findViewById(R.id.time_reapeat);
        timeRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTestData();
            }
        });
        idText=(EditText)findViewById(R.id.idText);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id=Integer.parseInt(idText.getText().toString());
                deleteData(id);
            }
        });
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData();
            }
        });
        daoManager=new DAOManager(DAOTestActivity.this);
        mola=(ImageView)findViewById(R.id.molaClick);
        animation= AnimationUtils.loadAnimation(DAOTestActivity.this,R.anim.clickmola);
        mola.setAnimation(animation);
        mola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mola.startAnimation(animation);
            }
        });
    }
    public void deleteData(int id){
        daoManager.deleteData(id);
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
    }
    public void showData(){
        //找到并打印
        ArrayList<AlarmSetting> arrayList;
        arrayList=daoManager.findAllData();
        String str=new String();
        for(int i=0;i<arrayList.size();i++){
            str="id="+arrayList.get(i).getId()+" 数据为 "+arrayList.get(i).getHours()+":"+arrayList.get(i).getMinus()
                    +","+arrayList.get(i).getRepeat()
                    +","+arrayList.get(i).getSound()
                    +"," +arrayList.get(i).isShake()
                    +"," +arrayList.get(i).getAlarmText()
                    +"," +arrayList.get(i).isClockOpen()
                    +"月"+arrayList.get(i).getMonth()
                    +"星期"+arrayList.get(i).getWeekday()
                    +"日"+arrayList.get(i).getDay();
            Log.d("result", str);
        }
    }
    public void showTestData(){
        ArrayList<String> arrayList;
        arrayList=daoManager.findAllTestData();
        for(int i=0;i<arrayList.size();i++){
            String string=arrayList.get(i);
            Log.d("result", string);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        daoManager.closeDatabase();
    }
}
