package com.cetcme.xkterminal.Fragment.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.widget.Satellite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

public class Satellite2Fragment extends Fragment {


    private final List<Satellite> datas = new ArrayList<>();
    private final List<Satellite> gpsDatas = new ArrayList<>();
    private final List<Satellite> bd1Datas = new ArrayList<>();
    private final List<Satellite> bd2Datas = new ArrayList<>();

    private int[] gps;

    private final int[] gpsXin = new int[]{20, 25, 26, 27,31, 32, 33, 35, 38, 40};

    private final int[] bdXin = new int[]{1, 2, 1, 3, 4, 11, 35};

    private final int[] bd = new int[]{1, 7, 8, 10, 13};

    private SatelliteView satellites;
    private ColumnChartView chart1, chart2, chart3;


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
        initData();
        onBindView(view);
        return view;
    }

    private void initData() {
        gps = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    }

    private void onBindView(View view) {
        satellites = view.findViewById(R.id.satellites);
        Random random = new Random();


        for (int i = 0; i < 10; i++) {
            int noIndex = random.nextInt(gps.length);
            Satellite s = new Satellite();
            s.setNo(gps[noIndex]);
            s.setNum(gpsXin[random.nextInt(gpsXin.length)]);
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("GPS");
            gpsDatas.add(s);
            datas.add(s);
            gps = reBuild(noIndex);
        }

        for (int i = 0; i < 5; i++) {
//            int noIndex = random.nextInt(bd.length);
            Satellite s = new Satellite();
            s.setNo(bd[i]);
            s.setNum(bdXin[random.nextInt(bdXin.length)]);
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("BD2");
            bd2Datas.add(s);
            datas.add(s);
        }

        for (int i = 1; i <= 6; i++) {
            Satellite s = new Satellite();
            s.setNo(i);
            s.setNum(bdXin[random.nextInt(bdXin.length)]);
            s.setElevationAngle(random.nextInt(90));
            s.setAzimuth(random.nextInt(360));
            s.setSatelliteType("BD1");
            bd1Datas.add(s);
            datas.add(s);
        }

        satellites.setDatas(datas);


        chart1 = view.findViewById(R.id.chart1);
        chart2 = view.findViewById(R.id.chart2);
        chart3 = view.findViewById(R.id.chart3);


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


    private void createColumnChart(List<Satellite> datas, ColumnChartView gpsBar) {
        if (datas == null || datas.isEmpty()) return;
        //定义有多少个柱子
        //int numColumns = 5;
        //定义表格实现类
        ColumnChartData columnChartData;
        //Column 是下图中柱子的实现类
        List<Column> columns = new ArrayList<>();
        //SubcolumnValue 是下图中柱子中的小柱子的实现类，下面会解释我说的是什么
        List<SubcolumnValue> values = null;
        List<AxisValue> axisValues = new ArrayList<>();

        Axis axis = new Axis();
        int len = datas.size();
        //循环初始化每根柱子，
        for (int i = 0; i < len; i++) {
            axisValues.add(new AxisValue(i).setLabel(ltTen(datas.get(i).getNo())));
            values = new ArrayList<>();
            //每一根柱子中只有一根小柱子
            float num = datas.get(i).getNum() * 1.0f;
            values.add(new SubcolumnValue(num, getColor(num)));
            //初始化Column
            Column column = new Column(values);
            // column.setHasLabels(true);
            columns.add(column);
        }
        axis.setValues(axisValues);
        axis.setTextColor(Color.BLACK);
        //给表格添加写好数据的柱子
        columnChartData = new ColumnChartData(columns);
        columnChartData.setAxisXBottom(axis);
        columnChartData.setFillRatio(0.5f);
        //给画表格的View添加要画的表格
        gpsBar.setColumnChartData(columnChartData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        datas.clear();
        gpsDatas.clear();
        bd1Datas.clear();
        bd2Datas.clear();
    }

    private int[] reBuild(int index) {
        int[] newInt = new int[gps.length - 1];
        int cur = 0;
        for (int i=0;i<index;i++){
            newInt[cur++] = gps[i];
        }
        for (int i=index+1;i<gps.length;i++){
            newInt[cur++] = gps[i];
        }
        return newInt;
    }

    private int getColor(float num) {
        if (num < 10f) {
            return Color.RED;
        } else if (num < 30f) {
            return Color.YELLOW;
        }
        return Color.GREEN;
    }

    private String ltTen(int i) {
        if (i < 10) return "0" + i;
        return "" + i;
    }
}
