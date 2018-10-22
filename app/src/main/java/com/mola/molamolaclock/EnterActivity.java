package com.mola.molamolaclock;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mola.openingstartanimation.LineDrawStrategy;
import com.mola.openingstartanimation.OpeningStartAnimation;
import com.mola.utils.MyDrawableUtils;

import java.util.Timer;
import java.util.TimerTask;

public class EnterActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        startOpeningAnimation();
        final Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(EnterActivity.this,Clock.class));
                timer.cancel();
                finish();
            }
        };
        timer.schedule(timerTask,1920);

    }
        public void startOpeningAnimation(){
        Resources resources=EnterActivity.this.getResources();
        Drawable drawable=resources.getDrawable(R.drawable.label);
        drawable= MyDrawableUtils.zoomDrawable(drawable,100,100);
        OpeningStartAnimation openingStartAnimation=new OpeningStartAnimation.Builder(this)
                .setAppIcon(drawable)
                .setAppName("MolaMolaClock")
                .setColorOfAppName(R.color.blue)
                .setColorOfAppStatement(R.color.textcolor)
                .setAppStatement("快睡啦~明天早上不要迟到")
                .setAnimationInterval(2000)
                .setAnimationFinishTime(1)
                .setDrawStategy(new LineDrawStrategy())
                .create();
        openingStartAnimation.show(this);
    }
}
