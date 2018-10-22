package com.mola.molamolaclock;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class QucikAlarmActivity extends FragmentActivity {
    private LinearLayout r1;
    private LinearLayout r2;
    private LinearLayout r3;
    private LinearLayout r4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qucik_alarm);
        initView();
    }
    private void initView(){
        r1=(LinearLayout)findViewById(R.id.r1);
        r2=(LinearLayout)findViewById(R.id.r2);
        r3=(LinearLayout)findViewById(R.id.r3);
        r4=(LinearLayout)findViewById(R.id.r4);
        r1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        r3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        r4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
