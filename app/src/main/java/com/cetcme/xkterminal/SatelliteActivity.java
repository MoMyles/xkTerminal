package com.cetcme.xkterminal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.cetcme.xkterminal.widget.DrawEarth;
import com.cetcme.xkterminal.widget.DrawSatellite;
import com.cetcme.xkterminal.widget.Satellite;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 卫星天体位置
 * Created by dell on 2018/5/24.
 */

public class SatelliteActivity extends AppCompatActivity {

    private final ArrayList<Satellite> mSatelliteList = new ArrayList<>();

    private FrameLayout mView;

    private DrawSatellite satelliteView;

    private int w = 32;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_satellite);

        mView = findViewById(R.id.LinearLayout);

        refreshSatellite();

        //初始化
        int mWidth = getResources().getDisplayMetrics().widthPixels / 2 - 35;
        DrawEarth earthView = new DrawEarth(this, mWidth / 2, mWidth / 2);
        earthView.invalidate();
        mView.addView(earthView);

        //添加卫星数据
        if (satelliteView != null) {
            mView.removeView(satelliteView);
        }
        satelliteView = new DrawSatellite(this, w / 2, w / 2, mSatelliteList);
        satelliteView.invalidate();
        mView.addView(satelliteView);

        // 开启定时器 5分钟更新一次
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshSatellite();
            }
        }, 5 * 60 * 1000);
    }

    private void refreshSatellite() {
        mSatelliteList.clear();
        Random random = new Random();
        for (int i=0;i<10;i++) {
            Satellite satellite1 = new Satellite();
            int j = random.nextInt(90);
            satellite1.setNum(j);
            int k = random.nextInt(359);
            satellite1.setAzimuth(k);
            int l = random.nextInt(90);
            satellite1.setElevationAngle(l);
            mSatelliteList.add(satellite1);
        }
        satelliteView.postInvalidate();
    }
}
