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
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class Satellite2Fragment extends Fragment {


    private final List<Satellite> datas = new ArrayList<>();
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

    }

    private void onBindView(View view) {
        satellites = view.findViewById(R.id.satellites);

        Satellite s1 = new Satellite();
        s1.setNo(5);
        s1.setNum(20);
        s1.setElevationAngle(80);
        s1.setAzimuth(200);
        s1.setSatelliteType("GPS");
        datas.add(s1);

        Satellite s2 = new Satellite();
        s2.setNo(10);
        s2.setNum(50);
        s2.setElevationAngle(30);
        s2.setAzimuth(100);
        s2.setSatelliteType("BD");
        datas.add(s2);

        satellites.setDatas(datas);


        chart1 = view.findViewById(R.id.chart1);
        chart2 = view.findViewById(R.id.chart2);
        chart3 = view.findViewById(R.id.chart3);

        createColumnChart(chart1);
        createColumnChart(chart2);
        createColumnChart(chart3);
    }


    private void createColumnChart(ColumnChartView gpsBar) {
        //定义有多少个柱子
        int numColumns = 5;
        //定义表格实现类
        ColumnChartData columnChartData;
        //Column 是下图中柱子的实现类
        List<Column> columns = new ArrayList<>();
        //SubcolumnValue 是下图中柱子中的小柱子的实现类，下面会解释我说的是什么
        List<SubcolumnValue> values = null;
        List<AxisValue> axisValues = new ArrayList<>();

        Axis axis = new Axis();
        int len = Math.min(numColumns, datas.size());
        //循环初始化每根柱子，
        for (int i = 0; i < len; i++) {
            axisValues.add(new AxisValue(i).setLabel(datas.get(i).getNo() + ""));
            values = new ArrayList<>();
            //每一根柱子中只有一根小柱子
            values.add(new SubcolumnValue(datas.get(i).getNum() * 1.0f, ChartUtils.pickColor()));
            //初始化Column
            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }
        axis.setValues(axisValues);
        axis.setTextColor(Color.BLACK);
        //给表格添加写好数据的柱子
        columnChartData = new ColumnChartData(columns);
        columnChartData.setAxisXBottom(axis);
        //给画表格的View添加要画的表格
        gpsBar.setColumnChartData(columnChartData);
    }
}
