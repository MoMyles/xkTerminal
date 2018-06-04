package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.ScreenUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.widget.DrawEarth;
import com.cetcme.xkterminal.widget.DrawSatellite;
import com.cetcme.xkterminal.widget.Satellite;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class SatelliteFragment extends Fragment {


    private final ArrayList<Satellite> mSatelliteList = new ArrayList<>();

    private FrameLayout mView;

    private DrawSatellite satelliteView;

    private TextView mLongitude;
    private TextView mLatitude;
    private TextView mElevation;
    private TextView mSatellite;
    private TextView mStatus;
    private TextView mDOP;
    private TextView mTime;
    private TextView mDate;
    private TextView mDouble;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    private Handler handler ;


    public SatelliteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_satellite, container, false);
        init(view);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        LocationBean lb = (LocationBean) msg.obj;
                        mLongitude.setText(lb.getLongitude() * 1.0 / 1e7 + "");
                        mLatitude.setText(lb.getLatitude() * 1.0 / 1e7 + "");
                        mTime.setText(timeFormat.format(Constant.SYSTEM_DATE));
                        mDate.setText(dateFormat.format(Constant.SYSTEM_DATE));
                        mSatellite.setText(mSatelliteList.size() + "");
                        break;
                }
            }
        };
        return view;
    }

    private void init(View view) {
        mView = view.findViewById(R.id.LinearLayout);
        mLongitude = view.findViewById(R.id.longitude_tv);
        mLatitude = view.findViewById(R.id.latitude_tv);
        mElevation = view.findViewById(R.id.height_tv);
        mSatellite = view.findViewById(R.id.satelliteCount_tv);
        mStatus = view.findViewById(R.id.satelliteStatus_tv);
        mDOP = view.findViewById(R.id.satellitePDOP_tv);
        mTime = view.findViewById(R.id.time_tv);
        mDate = view.findViewById(R.id.date_tv);
        mDouble = view.findViewById(R.id.bestPosa_accuracy_tv);

        //初始化
        int realWidth = QMUIDisplayHelper.dp2px(getActivity()
                ,(ScreenUtil.getScreenHigh(getActivity()) - 34 - 40 - 40 - 30 - 50 - 50) / 2);
                // gps 34 bottom 40 title 30 head 30;
        int mWidth = getResources().getDisplayMetrics().widthPixels / 4;
        DrawEarth earthView = new DrawEarth(getActivity(), mWidth, realWidth);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        earthView.setLayoutParams(lp);
        earthView.invalidate();
        mView.addView(earthView);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        llp.addRule(RelativeLayout.CENTER_IN_PARENT);

        //添加卫星数据
        if (satelliteView != null) {
            mView.removeView(satelliteView);
        }
        satelliteView = new DrawSatellite(getActivity(), mWidth, realWidth, mSatelliteList);
        satelliteView.invalidate();
        mView.addView(satelliteView);
        // 开启定时器 5分钟更新一次
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshSatellite();
            }
        }, 0, 5 * 60 * 1000);
    }

    private void refreshSatellite() {
        mSatelliteList.clear();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            Satellite satellite1 = new Satellite();
            int j = random.nextInt(50);
            satellite1.setNum(j);
            int k = random.nextInt(359);
            satellite1.setAzimuth(k);
            int l = random.nextInt(90);
            satellite1.setElevationAngle(l);
            mSatelliteList.add(satellite1);
        }
        DbManager db = MyApplication.getInstance().getDb();
        try {
            List<GPSBean> list = db.selector(GPSBean.class).findAll();
            for (GPSBean g : list) {
                Satellite satellite = new Satellite();
                satellite.setNum(g.getNo());
                satellite.setAzimuth(g.getFangwei());
                satellite.setElevationAngle(g.getYangjiao());
                mSatelliteList.add(satellite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        satelliteView.postInvalidate();

        final LocationBean lb = MyApplication.getInstance().getCurrentLocation();
        if (lb != null) {
            Message msg = Message.obtain();
            msg.obj = lb;
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

}
