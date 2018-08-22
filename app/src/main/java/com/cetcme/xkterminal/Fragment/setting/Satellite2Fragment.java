package com.cetcme.xkterminal.Fragment.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.widget.Satellite;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Satellite2Fragment extends Fragment {


    private final List<Satellite> datas = new ArrayList<>();
    private final List<Satellite> gpsDatas = new ArrayList<>();
    private final List<Satellite> bd1Datas = new ArrayList<>();
    private final List<Satellite> bd2Datas = new ArrayList<>();

    private int[] gps;

    private final int[] gpsXin = new int[]{20, 25, 26, 27, 31, 32, 33, 35, 38, 40};

    private final int[] bdXin = new int[]{10, 15, 20, 25, 30, 35};
    private final int[] bd2Xin = new int[]{23,24,25, 26,27, 28,29,30, 31,32,33,34,35,36,37,38,39};

    private int[] bd;

    private SatelliteView satellites;
    private BarChart chart1, chart2, chart3;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            initData();
            loadData();
            handler.postDelayed(this, 30 * 1000);
        }
    };

    private Timer timer = null;

    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            initData();
            loadData();
        }
    };


    public static Satellite2Fragment newInstance() {
        Bundle args = new Bundle();
        Satellite2Fragment fragment = new Satellite2Fragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_satellite2, container, false);
        onBindView(view);
        handler.postDelayed(runnable, 10);
        return view;
    }

    private void initData() {
        gps = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        bd = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        datas.clear();
        gpsDatas.clear();
        bd1Datas.clear();
        bd2Datas.clear();
    }

    private void onBindView(View view) {
        satellites = view.findViewById(R.id.satellites);

        chart1 = view.findViewById(R.id.chart1);
        chart2 = view.findViewById(R.id.chart2);
        chart3 = view.findViewById(R.id.chart3);

    }

    private void loadData() {
        Random random = new Random();
        int len = random.nextInt(6) + 6;
        for (int i = 0; i < len; i++) {
            int noIndex = random.nextInt(gps.length);
            Satellite s = new Satellite();
            s.setNo(gps[noIndex]);
            s.setNum(gpsXin[random.nextInt(gpsXin.length)]);
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("GPS");
            gpsDatas.add(s);
            datas.add(s);
            gps = reBuild(noIndex, gps);
        }
        len = random.nextInt(6) + 6;
        for (int i = 0; i < len; i++) {
            int noIndex = random.nextInt(bd.length);
            Satellite s = new Satellite();
            s.setNo(bd[noIndex]);
            s.setNum(bd2Xin[random.nextInt(bd2Xin.length)]);
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("BD2");
            bd2Datas.add(s);
            datas.add(s);
            bd = reBuild(noIndex, bd);
        }

        for (int i = 1; i <= 6; i++) {
            Satellite s = new Satellite();
            s.setNo(i);
            if (i == 3 || i == 4 || i == 6) {
                s.setNum(1);
            } else {
                s.setNum(bdXin[random.nextInt(bdXin.length)]);
            }
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("BD1");
            bd1Datas.add(s);
            datas.add(s);
        }

        satellites.setDatas(datas);


        Collections.sort(bd2Datas, new Comparator<Satellite>() {
            @Override
            public int compare(Satellite o1, Satellite o2) {
                return o1.getNo() - o2.getNo();
            }
        });

        Collections.sort(gpsDatas, new Comparator<Satellite>() {
            @Override
            public int compare(Satellite o1, Satellite o2) {
                return o1.getNo() - o2.getNo();
            }
        });
        Collections.sort(bd1Datas, new Comparator<Satellite>() {
            @Override
            public int compare(Satellite o1, Satellite o2) {
                return o1.getNo() - o2.getNo();
            }
        });

        createColumnChart(gpsDatas, chart1);// GPS
        createColumnChart(bd2Datas, chart2);// 北斗2
        createColumnChart(bd1Datas, chart3);// 北斗1
    }


    private void createColumnChart(final List<Satellite> list, BarChart gpsBar) {
        if (list == null || list.isEmpty()) return;
        int len = list.size();
        gpsBar.getLegend().setEnabled(false);
        gpsBar.getAxisRight().setEnabled(false);//隐藏右边坐标轴
        gpsBar.getDescription().setEnabled(false);
        gpsBar.setDrawBarShadow(false);                          //绘制当前展示的内容顶部阴影
        gpsBar.setMaxVisibleValueCount(40);
        gpsBar.setDrawGridBackground(false);
        gpsBar.setScaleEnabled(false);

        XAxis xAxis = gpsBar.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
//        xAxis.setLabelCount(len, false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(16);
        xAxis.setLabelCount(16);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                try {
                    return ltTen(list.get((int) value).getNo());
                }catch (Exception e){
                    return "";
                }
            }
        });


        YAxis yAxis = gpsBar.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setAxisMaximum(40);
        yAxis.setAxisMinimum(0);

        List<BarEntry> barEntries = new ArrayList<>();
        int[] colors = new int[len];
        //循环初始化每根柱子，
        for (int i = 0; i < len; i++) {
            BarEntry barEntry = new BarEntry(i, list.get(i).getNum());
            barEntries.add(barEntry);
            colors[i] = getColor(list.get(i).getNum());
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setDrawValues(false);
        barDataSet.setColors(colors);

        BarData barData = new BarData(barDataSet);
//        barData.setBarWidth(0.2f);

        gpsBar.setData(barData);
        gpsBar.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private int[] reBuild(int index, int[] gps) {
        int[] newInt = new int[gps.length - 1];
        int cur = 0;
        for (int i = 0; i < index; i++) {
            newInt[cur++] = gps[i];
        }
        for (int i = index + 1; i < gps.length; i++) {
            newInt[cur++] = gps[i];
        }
        return newInt;
    }

    private int getColor(float num) {
        if (num < 10f) {
            return Color.RED;
        } else if (num < 27f) {
            return Color.YELLOW;
        }
        return Color.GREEN;
    }

    private String ltTen(int i) {
        if (i < 10) return "0" + i;
        return "" + i;
    }
}
