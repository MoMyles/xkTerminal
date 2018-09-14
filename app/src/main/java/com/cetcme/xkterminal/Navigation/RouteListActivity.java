package com.cetcme.xkterminal.Navigation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.R;
import com.qiuhong.qhlibrary.QHTitleView.QHTitleView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cetcme.xkterminal.Navigation.FileUtil.stampToDate;


public class RouteListActivity extends FragmentActivity {

    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.qhTitleView)
    QHTitleView qhTitleView;
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    @BindView(R.id.et_start)
    EditText etStart;
    @BindView(R.id.et_end)
    EditText etEnd;
    @BindView(R.id.btn_search)
    Button btnSearch;

    //    private SimpleAdapter simpleAdapter;
    private TestAdapter testAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    public static final int ACTIVITY_RESULT_ROUTE_SHOW = 0x02;
    public static final int ACTIVITY_RESULT_ROUTE_ADD = 0x01;
    public static final int ACTIVITY_RESULT_ROUTE_NOTHING = 0x00;
    private Context context;

    private DbManager db = MyApplication.getInstance().getDb();

    private String tag = "航线";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        ButterKnife.bind(this);
        context = this;

        EventBus.getDefault().register(this);
        // PermissionUtil.verifyStoragePermissions(this);


        String intentTag = getIntent().getStringExtra("tag");
        if (intentTag != null && intentTag.equals("航迹")) {
            tag = "航迹";
        }

        initTitleView();
        initListView();
        if (tag.equals("航线")) {
            getFilesData();
        } else {
            getRouteData();
        }

        etStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildTime(etStart);
            }
        });
        etEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildTime(etEnd);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date start = sdf.parse(etStart.getText().toString().trim());
                    Date end = sdf.parse(etEnd.getText().toString().trim());
                    getRouteData(start.getTime(), end.getTime());
                    Toast.makeText(getApplicationContext(), "查询完成", Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void buildTime(final EditText et) {
        final View view = getLayoutInflater().inflate(R.layout.dialog_date_time_picker, null);
        final TextView tv1 = view.findViewById(R.id.tv1);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(com.cetcme.xkterminal.MyClass.Constant.SYSTEM_DATE);
                final int year = calendar.get(Calendar.YEAR);
                final View content = getLayoutInflater().inflate(R.layout.popup_years, null);
//                content.getLayoutParams().width = QMUIDisplayHelper.getScreenWidth(getApplicationContext());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(QMUIDisplayHelper.getScreenWidth(getApplicationContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
                content.setLayoutParams(lp);
                final EasyPopup pop = EasyPopup.create(RouteListActivity.this)
                        .setContentView(content)
                        .setOutsideTouchable(false);
                final TextView _tv1 = content.findViewById(R.id.tv1);
                final TextView _tv2 = content.findViewById(R.id.tv2);
                final TextView _tv3 = content.findViewById(R.id.tv3);
                final TextView _tv4 = content.findViewById(R.id.tv4);
                final TextView _tv5 = content.findViewById(R.id.tv5);
                _tv1.setText("" + (year - 5 + 1));
                _tv2.setText("" + (year - 5 + 2));
                _tv3.setText("" + (year - 5 + 3));
                _tv4.setText("" + (year - 5 + 4));
                _tv5.setText("" + (year - 5 + 5));

                bindKeyClick(pop, _tv1, tv1);
                bindKeyClick(pop, _tv2, tv1);
                bindKeyClick(pop, _tv3, tv1);
                bindKeyClick(pop, _tv4, tv1);
                bindKeyClick(pop, _tv5, tv1);

                pop.showAtAnchorView(getWindow().getDecorView().findViewById(android.R.id.content), XGravity.CENTER, YGravity.CENTER, 0, 60);
            }
        });
        final TextView tv2 = view.findViewById(R.id.tv2);
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View content = getLayoutInflater().inflate(R.layout.popup_months, null);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(QMUIDisplayHelper.getScreenWidth(getApplicationContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
                content.setLayoutParams(lp);
                final EasyPopup pop = EasyPopup.create(RouteListActivity.this)
                        .setContentView(content)
                        .setOutsideTouchable(false);
                final TextView _tv1 = content.findViewById(R.id.tv1);
                final TextView _tv2 = content.findViewById(R.id.tv2);
                final TextView _tv3 = content.findViewById(R.id.tv3);
                final TextView _tv4 = content.findViewById(R.id.tv4);
                final TextView _tv5 = content.findViewById(R.id.tv5);
                final TextView _tv6 = content.findViewById(R.id.tv6);
                final TextView _tv7 = content.findViewById(R.id.tv7);
                final TextView _tv8 = content.findViewById(R.id.tv8);
                final TextView _tv9 = content.findViewById(R.id.tv9);
                final TextView _tv10 = content.findViewById(R.id.tv10);
                final TextView _tv11 = content.findViewById(R.id.tv11);
                final TextView _tv12 = content.findViewById(R.id.tv12);
                _tv1.setText("01");
                _tv2.setText("02");
                _tv3.setText("03");
                _tv4.setText("04");
                _tv5.setText("05");
                _tv6.setText("06");
                _tv7.setText("07");
                _tv8.setText("08");
                _tv9.setText("09");
                _tv10.setText("10");
                _tv11.setText("11");
                _tv12.setText("12");

                bindKeyClick(pop, _tv1, tv2);
                bindKeyClick(pop, _tv2, tv2);
                bindKeyClick(pop, _tv3, tv2);
                bindKeyClick(pop, _tv4, tv2);
                bindKeyClick(pop, _tv5, tv2);
                bindKeyClick(pop, _tv6, tv2);
                bindKeyClick(pop, _tv7, tv2);
                bindKeyClick(pop, _tv8, tv2);
                bindKeyClick(pop, _tv9, tv2);
                bindKeyClick(pop, _tv10, tv2);
                bindKeyClick(pop, _tv11, tv2);
                bindKeyClick(pop, _tv12, tv2);

                pop.showAtAnchorView(getWindow().getDecorView().findViewById(android.R.id.content), XGravity.CENTER, YGravity.CENTER, 0, 120);
            }
        });
        final TextView tv3 = view.findViewById(R.id.tv3);
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View content = getLayoutInflater().inflate(R.layout.popup_days, null);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(QMUIDisplayHelper.getScreenWidth(getApplicationContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
                content.setLayoutParams(lp);
                final EasyPopup pop = EasyPopup.create(RouteListActivity.this)
                        .setContentView(content)
                        .setOutsideTouchable(false);
                final TextView _tv1 = content.findViewById(R.id.tv1);
                final TextView _tv2 = content.findViewById(R.id.tv2);
                final TextView _tv3 = content.findViewById(R.id.tv3);
                final TextView _tv4 = content.findViewById(R.id.tv4);
                final TextView _tv5 = content.findViewById(R.id.tv5);
                final TextView _tv6 = content.findViewById(R.id.tv6);
                final TextView _tv7 = content.findViewById(R.id.tv7);
                final TextView _tv8 = content.findViewById(R.id.tv8);
                final TextView _tv9 = content.findViewById(R.id.tv9);
                final TextView _tv10 = content.findViewById(R.id.tv10);
                final TextView _tv11 = content.findViewById(R.id.tv11);
                final TextView _tv12 = content.findViewById(R.id.tv12);
                final TextView _tv13 = content.findViewById(R.id.tv13);
                final TextView _tv14 = content.findViewById(R.id.tv14);
                final TextView _tv15 = content.findViewById(R.id.tv15);
                final TextView _tv16 = content.findViewById(R.id.tv16);
                final TextView _tv17 = content.findViewById(R.id.tv17);
                final TextView _tv18 = content.findViewById(R.id.tv18);
                final TextView _tv19 = content.findViewById(R.id.tv19);
                final TextView _tv20 = content.findViewById(R.id.tv20);
                final TextView _tv21 = content.findViewById(R.id.tv21);
                final TextView _tv22 = content.findViewById(R.id.tv22);
                final TextView _tv23 = content.findViewById(R.id.tv23);
                final TextView _tv24 = content.findViewById(R.id.tv24);
                final TextView _tv25 = content.findViewById(R.id.tv25);
                final TextView _tv26 = content.findViewById(R.id.tv26);
                final TextView _tv27 = content.findViewById(R.id.tv27);
                final TextView _tv28 = content.findViewById(R.id.tv28);
                final TextView _tv29 = content.findViewById(R.id.tv29);
                final TextView _tv30 = content.findViewById(R.id.tv30);
                final TextView _tv31 = content.findViewById(R.id.tv31);

                _tv1.setText("01");
                _tv2.setText("02");
                _tv3.setText("03");
                _tv4.setText("04");
                _tv5.setText("05");
                _tv6.setText("06");
                _tv7.setText("07");
                _tv8.setText("08");
                _tv9.setText("09");
                _tv10.setText("10");
                _tv11.setText("11");
                _tv12.setText("12");
                _tv13.setText("13");
                _tv14.setText("14");
                _tv15.setText("15");
                _tv16.setText("16");
                _tv17.setText("17");
                _tv18.setText("18");
                _tv19.setText("19");
                _tv20.setText("20");
                _tv21.setText("21");
                _tv22.setText("22");
                _tv23.setText("23");
                _tv24.setText("24");
                _tv25.setText("25");
                _tv26.setText("26");
                _tv27.setText("27");
                _tv28.setText("28");
                _tv29.setText("29");
                _tv30.setText("30");
                _tv31.setText("31");

                bindKeyClick(pop, _tv1, tv3);
                bindKeyClick(pop, _tv2, tv3);
                bindKeyClick(pop, _tv3, tv3);
                bindKeyClick(pop, _tv4, tv3);
                bindKeyClick(pop, _tv5, tv3);
                bindKeyClick(pop, _tv6, tv3);
                bindKeyClick(pop, _tv7, tv3);
                bindKeyClick(pop, _tv8, tv3);
                bindKeyClick(pop, _tv9, tv3);
                bindKeyClick(pop, _tv10, tv3);
                bindKeyClick(pop, _tv11, tv3);
                bindKeyClick(pop, _tv12, tv3);
                bindKeyClick(pop, _tv13, tv3);
                bindKeyClick(pop, _tv14, tv3);
                bindKeyClick(pop, _tv15, tv3);
                bindKeyClick(pop, _tv16, tv3);
                bindKeyClick(pop, _tv17, tv3);
                bindKeyClick(pop, _tv18, tv3);
                bindKeyClick(pop, _tv19, tv3);
                bindKeyClick(pop, _tv20, tv3);
                bindKeyClick(pop, _tv21, tv3);
                bindKeyClick(pop, _tv22, tv3);
                bindKeyClick(pop, _tv23, tv3);
                bindKeyClick(pop, _tv24, tv3);
                bindKeyClick(pop, _tv25, tv3);
                bindKeyClick(pop, _tv26, tv3);
                bindKeyClick(pop, _tv27, tv3);
                bindKeyClick(pop, _tv28, tv3);
                bindKeyClick(pop, _tv29, tv3);
                bindKeyClick(pop, _tv30, tv3);
                bindKeyClick(pop, _tv31, tv3);

                int year = Integer.parseInt(tv1.getText().toString());
                int m = Integer.parseInt(tv2.getText().toString());

                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    //闰年
                    if (m == 2) {
                        _tv30.setVisibility(View.INVISIBLE);
                        _tv31.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (m == 2) {
                        _tv29.setVisibility(View.INVISIBLE);
                        _tv30.setVisibility(View.INVISIBLE);
                        _tv31.setVisibility(View.INVISIBLE);
                    }
                }

                if (m == 4 || m == 6 || m == 9 || m == 11) {
                    _tv31.setVisibility(View.INVISIBLE);
                }

                pop.showAtAnchorView(getWindow().getDecorView().findViewById(android.R.id.content), XGravity.CENTER, YGravity.CENTER, 0, 120);
            }
        });
        final TextView tv4 = view.findViewById(R.id.tv4);
        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View content = getLayoutInflater().inflate(R.layout.popup_hours, null);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(QMUIDisplayHelper.getScreenWidth(getApplicationContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
                content.setLayoutParams(lp);
                final EasyPopup pop = EasyPopup.create(RouteListActivity.this)
                        .setContentView(content)
                        .setOutsideTouchable(false);
                final TextView _tv1 = content.findViewById(R.id.tv1);
                final TextView _tv2 = content.findViewById(R.id.tv2);
                final TextView _tv3 = content.findViewById(R.id.tv3);
                final TextView _tv4 = content.findViewById(R.id.tv4);
                final TextView _tv5 = content.findViewById(R.id.tv5);
                final TextView _tv6 = content.findViewById(R.id.tv6);
                final TextView _tv7 = content.findViewById(R.id.tv7);
                final TextView _tv8 = content.findViewById(R.id.tv8);
                final TextView _tv9 = content.findViewById(R.id.tv9);
                final TextView _tv10 = content.findViewById(R.id.tv10);
                final TextView _tv11 = content.findViewById(R.id.tv11);
                final TextView _tv12 = content.findViewById(R.id.tv12);
                final TextView _tv13 = content.findViewById(R.id.tv13);
                final TextView _tv14 = content.findViewById(R.id.tv14);
                final TextView _tv15 = content.findViewById(R.id.tv15);
                final TextView _tv16 = content.findViewById(R.id.tv16);
                final TextView _tv17 = content.findViewById(R.id.tv17);
                final TextView _tv18 = content.findViewById(R.id.tv18);
                final TextView _tv19 = content.findViewById(R.id.tv19);
                final TextView _tv20 = content.findViewById(R.id.tv20);
                final TextView _tv21 = content.findViewById(R.id.tv21);
                final TextView _tv22 = content.findViewById(R.id.tv22);
                final TextView _tv23 = content.findViewById(R.id.tv23);
                final TextView _tv24 = content.findViewById(R.id.tv24);

                _tv1.setText("01");
                _tv2.setText("02");
                _tv3.setText("03");
                _tv4.setText("04");
                _tv5.setText("05");
                _tv6.setText("06");
                _tv7.setText("07");
                _tv8.setText("08");
                _tv9.setText("09");
                _tv10.setText("10");
                _tv11.setText("11");
                _tv12.setText("12");
                _tv13.setText("13");
                _tv14.setText("14");
                _tv15.setText("15");
                _tv16.setText("16");
                _tv17.setText("17");
                _tv18.setText("18");
                _tv19.setText("19");
                _tv20.setText("20");
                _tv21.setText("21");
                _tv22.setText("22");
                _tv23.setText("23");
                _tv24.setText("00");

                bindKeyClick(pop, _tv1, tv4);
                bindKeyClick(pop, _tv2, tv4);
                bindKeyClick(pop, _tv3, tv4);
                bindKeyClick(pop, _tv4, tv4);
                bindKeyClick(pop, _tv5, tv4);
                bindKeyClick(pop, _tv6, tv4);
                bindKeyClick(pop, _tv7, tv4);
                bindKeyClick(pop, _tv8, tv4);
                bindKeyClick(pop, _tv9, tv4);
                bindKeyClick(pop, _tv10, tv4);
                bindKeyClick(pop, _tv11, tv4);
                bindKeyClick(pop, _tv12, tv4);

                bindKeyClick(pop, _tv13, tv4);
                bindKeyClick(pop, _tv14, tv4);
                bindKeyClick(pop, _tv15, tv4);
                bindKeyClick(pop, _tv16, tv4);
                bindKeyClick(pop, _tv17, tv4);
                bindKeyClick(pop, _tv18, tv4);
                bindKeyClick(pop, _tv19, tv4);
                bindKeyClick(pop, _tv20, tv4);
                bindKeyClick(pop, _tv21, tv4);
                bindKeyClick(pop, _tv22, tv4);
                bindKeyClick(pop, _tv23, tv4);
                bindKeyClick(pop, _tv24, tv4);

                pop.showAtAnchorView(getWindow().getDecorView().findViewById(android.R.id.content), XGravity.CENTER, YGravity.CENTER, 0, 120);
            }
        });
        final Button btn = view.findViewById(R.id.btn);
        final Button btn2 = view.findViewById(R.id.btn2);
        String time = et.getText().toString();
        if (!TextUtils.isEmpty(time)) {
            try {
                Calendar calendar = Calendar.getInstance();
                Date date = sdf.parse(time);
                calendar.setTime(date);
                tv1.setText("" + calendar.get(Calendar.YEAR));
                tv2.setText(re10("" + (calendar.get(Calendar.MONTH) + 1)));
                tv3.setText(re10("" + calendar.get(Calendar.DAY_OF_MONTH)));
                tv4.setText(re10("" + calendar.get(Calendar.HOUR_OF_DAY)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        final EasyPopup popup = EasyPopup.create(RouteListActivity.this)
                .setContentView(view)
                .setOutsideTouchable(false);
        popup.showAtAnchorView(findViewById(android.R.id.content).getRootView(), XGravity.CENTER, YGravity.CENTER, 0, 0);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String y = tv1.getText().toString().trim();
                String m = tv2.getText().toString().trim();
                String d = tv3.getText().toString().trim();
                String h = tv4.getText().toString().trim();
                et.setText(y + "-" + re10(m) + "-" + re10(d) + " " + re10(h) + ":00:00");
                popup.dismiss();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    private void bindKeyClick(final EasyPopup pop, final TextView _tv1, final TextView tv1) {
        _tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv1.setText(_tv1.getText().toString().trim());
                pop.dismiss();
            }
        });
    }

    private String re10(String s) {
        if (TextUtils.isEmpty(s)) {
            s = "0";
        }
        if (s.length() < 2) {
            return "0" + s;
        }
        return s;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initTitleView() {
        qhTitleView.setTitle(tag + "列表");
        qhTitleView.setBackView(R.mipmap.title_icon_back_2x);
        qhTitleView.setRightView(tag.equals("航迹") ? 0 : R.mipmap.title_icon_add_2x);
        llSearch.setVisibility(tag.equals("航迹") ? View.VISIBLE : View.GONE);
        qhTitleView.setBackgroundResource(R.drawable.top_select);
        qhTitleView.setClickCallback(new QHTitleView.ClickCallback() {
            @Override
            public void onBackClick() {
                // setResult(ACTIVITY_RESULT_ROUTE_NOTHING);
                finish();
            }

            @Override
            public void onRightClick() {
                if (tag.equals("航线")) {
                    // setResult(ACTIVITY_RESULT_ROUTE_ADD);
                    Intent intent = new Intent();
                    intent.setClass(RouteListActivity.this, NavigationMainActivity.class);
                    intent.putExtra("requestCode", 0);
                    intent.putExtra("resultCode", ACTIVITY_RESULT_ROUTE_ADD);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void initListView() {
//        simpleAdapter = new SimpleAdapter(this, getFilesData(), R.layout.cell_route_list,
//                new String[] {"fileName", "lastModifyTime"},
//                new int[] {R.id.tv_name, R.id.tv_time});
        testAdapter = new TestAdapter();
        listView.setAdapter(testAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                if (tag.equals("航线")) {
                    intent.setClass(RouteListActivity.this, NavigationMainActivity.class);
                    intent.putExtra("requestCode", 0);
                    intent.putExtra("resultCode", ACTIVITY_RESULT_ROUTE_SHOW);
                    intent.putExtra("fileName", dataList.get(i).get("fileName").toString());
                } else {
                    intent.setClass(RouteListActivity.this, NavigationMainActivity.class);
                    intent.putExtra("requestCode", 1);
                    intent.putExtra("resultCode", ACTIVITY_RESULT_ROUTE_SHOW);
                    intent.putExtra("navtime", dataList.get(i).get("navtime").toString());
                }
                startActivity(intent);
//                setResult(ACTIVITY_RESULT_ROUTE_SHOW, intent);
                finish();
            }
        });

        if (tag.equals("航线")) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> item = (Map<String, Object>) adapterView.getAdapter().getItem(i);
                    final String fileName = item.get("fileName").toString();
                    File filePath = new File(Constant.ROUTE_FILE_PATH + "/" + fileName);
                    if (filePath.exists()) {
                        filePath.delete();
                    }
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                    dataList.remove(item);
                    testAdapter.notifyDataSetChanged();
                    return true;
                }
            });
        }

    }

    public List<Map<String, Object>> getFilesData() {
        dataList.clear();

        File f = new File(Constant.ROUTE_FILE_PATH);
        File[] files = f.listFiles();

        if (files == null) {
            return dataList;
        }

        for (File file : files) {
            Map<String, Object> map = new Hashtable<>();
            map.put("fileName", file.getName());
            map.put("lastModifyTime", stampToDate(file.lastModified()));
            map.put("lastModifyStamp", file.lastModified());
            map.put("fileLength", (new BigDecimal(file.length() / 1024f)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "KB");
            dataList.add(map);
        }
        System.out.println(dataList.size());

        // 排序 最近创建的在前面
        Collections.sort(dataList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                // rhs 和lhs 换位置 --> 正序或倒序
                return Long.valueOf(rhs.get("lastModifyStamp").toString()).compareTo(Long.valueOf(lhs.get("lastModifyStamp").toString()));
            }
        });

        testAdapter.notifyDataSetChanged();
        return dataList;
    }

    public List<Map<String, Object>> getRouteData() {
        dataList.clear();

        try {
            List<String> navs = new ArrayList<>();
            long pre7Time = 0l;
            if (com.cetcme.xkterminal.MyClass.Constant.SYSTEM_DATE != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(com.cetcme.xkterminal.MyClass.Constant.SYSTEM_DATE);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                etEnd.setText(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                pre7Time = calendar.getTime().getTime();
                etStart.setText(sdf.format(calendar.getTime()));
            }
            Cursor cursor = db.execQuery("select navtime from t_location where navtime >= " + pre7Time + " group by navtime");
            //判断游标是否为空
            if (cursor.moveToFirst()) {
                Calendar calendar = Calendar.getInstance();
                //遍历游标
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    //获得ID
                    String navtime = cursor.getString(0);
                    //输出用户信息
                    if (navtime != null) {
                        navs.add(navtime);
                        Date date = new Date(Long.parseLong(navtime));
                        calendar.setTime(date);
                        String fileName = DateUtil.Date2String(date);
                        if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0
                                && calendar.get(Calendar.MILLISECOND) == 0) {
                            fileName = DateUtil.Date2String(date, "yyyy-MM-dd");
                        }
                        Map<String, Object> map = new Hashtable<>();
                        map.put("fileName", fileName);
                        map.put("lastModifyTime", "");
                        map.put("lastModifyStamp", "");
                        map.put("fileLength", "");
                        map.put("navtime", navtime);
                        dataList.add(map);
                    }
                }
            }
            cursor.close();
        } catch (DbException e) {
            e.printStackTrace();
        }

        testAdapter.notifyDataSetChanged();
        return dataList;
    }


    public List<Map<String, Object>> getRouteData(long start, long end) {
        if (end - start < 0) {
            Toast.makeText(getApplicationContext(), "结束时间不能小于开始时间", Toast.LENGTH_SHORT).show();
            return null;
        } else if (end - start > 7 * 24 * 60 * 60 * 1000) {
            Toast.makeText(getApplicationContext(), "查询范围为7天以内", Toast.LENGTH_SHORT).show();
            return null;
        }
        dataList.clear();

        try {
            List<String> navs = new ArrayList<>();
            Cursor cursor = db.execQuery("select navtime from t_location where navtime between " + start + " and " + end + " group by navtime");
            //判断游标是否为空
            if (cursor.moveToFirst()) {
                Calendar calendar = Calendar.getInstance();
                //遍历游标
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    //获得ID
                    String navtime = cursor.getString(0);
                    //输出用户信息
                    if (navtime != null) {
                        navs.add(navtime);
                        Date date = new Date(Long.parseLong(navtime));
                        calendar.setTime(date);
                        String fileName = DateUtil.Date2String(date);
                        if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0
                                && calendar.get(Calendar.MILLISECOND) == 0) {
                            fileName = DateUtil.Date2String(date, "yyyy-MM-dd");
                        }
                        Map<String, Object> map = new Hashtable<>();
                        map.put("fileName", fileName);
                        map.put("lastModifyTime", "");
                        map.put("lastModifyStamp", "");
                        map.put("fileLength", "");
                        map.put("navtime", navtime);
                        dataList.add(map);
                    }
                }
            }
            cursor.close();
        } catch (DbException e) {
            e.printStackTrace();
        }

        testAdapter.notifyDataSetChanged();
        return dataList;
    }

    @Override
    public void onBackPressed() {
        setResult(ACTIVITY_RESULT_ROUTE_NOTHING);
        super.onBackPressed();
    }

    class TestAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Map<String, Object> getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh = null;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.cell_route_list, viewGroup, false);
                vh = new ViewHolder();
                vh.mTv1 = view.findViewById(R.id.tv_name);
                vh.mTv2 = view.findViewById(R.id.tv_time);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }
            Map<String, Object> item = getItem(i);
            if (item != null) {
                vh.mTv1.setText(item.get("fileName") + "");
                vh.mTv2.setText(item.get("lastModifyTime") + "");
            } else {
                vh.mTv1.setText("");
                vh.mTv2.setText("");
            }
            return view;
        }

        class ViewHolder {
            TextView mTv1;
            TextView mTv2;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(SmsEvent smsEvent) {
        JSONObject receiveJson = smsEvent.getReceiveJson();
        try {
            String apiType = receiveJson.getString("apiType");
            switch (apiType) {
                case "refreshRouteList":
                    getRouteData();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
