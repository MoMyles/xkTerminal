package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.AlertBean;
import com.cetcme.xkterminal.Sqlite.Bean.GroupBean;
import com.cetcme.xkterminal.Sqlite.Bean.InoutBean;
import com.cetcme.xkterminal.Sqlite.Bean.SignBean;
import com.cetcme.xkterminal.Sqlite.Proxy.AlertProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.InoutProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.SignProxy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qiuhong on 10/01/2018.
 */

@SuppressLint("ValidFragment")
public class LogFragment extends Fragment {

    private View view;
    private String tg;
    private TitleBar titleBar;

    private ListView listView;
    private ListView listView2;
    private ListView listView3;
    private ListView listView4;
    private int logPerPage;
    private SimpleAdapter simpleAdapter;
    private SimpleAdapter simpleAdapter2;
    private SimpleAdapter simpleAdapter3;
    private SimpleAdapter simpleAdapter4;
    private List<Map<String, Object>> dataList = new ArrayList<>();
    private List<Map<String, Object>> dataList2 = new ArrayList<>();
    private List<Map<String, Object>> dataList3 = new ArrayList<>();
    private List<Map<String, Object>> dataList4 = new ArrayList<>();

    private int pageIndex = 0;
    private int totalPage = 1;

    private DbManager db;

    public LogFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "LogFragment: " + tg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = ((MyApplication) getActivity().getApplication()).db;
        EventBus.getDefault().register(this);

        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_log, container, false);

        logPerPage = CommonUtil.getCountPerPage(getContext(), getActivity());

        titleBar = view.findViewById(R.id.titleBar);
        //设置listView
        listView = view.findViewById(R.id.list_view);
        //设置listView
        listView2 = view.findViewById(R.id.list_view2);
        //设置listView
        listView3 = view.findViewById(R.id.list_view3);
        listView4 = view.findViewById(R.id.list_view4);
        if (tg.equals("sign")) {
            titleBar.setTitle("打卡记录");
            view.findViewById(R.id.title_sign_layout).setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            createDaka();
        } else if (tg.equals("alert")) {
            titleBar.setTitle("报警记录");
            view.findViewById(R.id.title_alert_layout).setVisibility(View.VISIBLE);
            listView2.setVisibility(View.VISIBLE);
            createBaojing();
        } else if (tg.equals("inout")) {
            titleBar.setTitle("进出港申报");
            view.findViewById(R.id.title_inout_layout).setVisibility(View.VISIBLE);
            listView3.setVisibility(View.VISIBLE);
            createInout();
        } else if (tg.equals("group")) {
            titleBar.setTitle("组播列表");
            view.findViewById(R.id.title_group_layout).setVisibility(View.VISIBLE);
            listView4.setVisibility(View.VISIBLE);
            createGroup();
        }
        return view;
    }

    private void createGroup() {
        if (simpleAdapter4 != null) {
            getGroupData();
            return;
        }
        simpleAdapter4 = new SimpleAdapter(getActivity(), getGroupData(), R.layout.cell_group_list,
                new String[]{"number", "group", "name"},
                new int[]{R.id.number_in_inout_cell, R.id.type_in_inout_cell, R.id.count_in_inout_cell});
        listView4.setAdapter(simpleAdapter4);
    }

    private void createInout() {
        if (simpleAdapter3 != null) {
            getInoutData();
            return;
        }
        simpleAdapter3 = new SimpleAdapter(getActivity(), getInoutData(), R.layout.cell_inout_list,
                new String[]{"number", "type", "count", "lon", "lat", "time"},
                new int[]{R.id.number_in_inout_cell, R.id.type_in_inout_cell, R.id.count_in_inout_cell, R.id.lon_in_inout_cell, R.id.lat_in_inout_cell, R.id.time_in_inout_cell});
        listView3.setAdapter(simpleAdapter3);
    }

    private void createBaojing() {
        if (simpleAdapter2 != null) {
            getAlertData();
            return;
        }
        simpleAdapter2 = new SimpleAdapter(getActivity(), getAlertData(), R.layout.cell_alert_list,
                new String[]{"number", "type", "time"},
                new int[]{R.id.number_in_alert_cell, R.id.type_in_alert_cell, R.id.time_in_alert_cell});
        listView2.setAdapter(simpleAdapter2);
    }

    private void createDaka() {
        if (simpleAdapter != null) {
            getSignData();
            return;
        }
        simpleAdapter = new SimpleAdapter(getActivity(), getSignData(), R.layout.cell_sign_list,
                new String[]{"number", "idCard", "name", "time"},
                new int[]{R.id.number_in_sign_cell, R.id.idCard_in_sign_cell, R.id.name_in_sign_cell, R.id.time_in_sign_cell});
        listView.setAdapter(simpleAdapter);
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            pageIndex = 0;
            totalPage = 1;
            if (tg.equals("sign")) {
                titleBar.setTitle("打卡记录");
                view.findViewById(R.id.title_sign_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.title_alert_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_inout_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_group_layout).setVisibility(View.GONE);
                createDaka();
                simpleAdapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
                listView2.setVisibility(View.GONE);
                listView3.setVisibility(View.GONE);
                listView4.setVisibility(View.GONE);
            }
            if (tg.equals("alert")) {
                titleBar.setTitle("报警记录");
                createBaojing();
                view.findViewById(R.id.title_alert_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.title_sign_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_inout_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_group_layout).setVisibility(View.GONE);
                simpleAdapter2.notifyDataSetChanged();
                listView.setVisibility(View.GONE);
                listView2.setVisibility(View.VISIBLE);
                listView3.setVisibility(View.GONE);
                listView4.setVisibility(View.GONE);
            }
            if (tg.equals("inout")) {
                titleBar.setTitle("进出港申报");
                createInout();
                view.findViewById(R.id.title_inout_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.title_sign_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_alert_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_group_layout).setVisibility(View.GONE);
                simpleAdapter3.notifyDataSetChanged();
                listView.setVisibility(View.GONE);
                listView2.setVisibility(View.GONE);
                listView3.setVisibility(View.VISIBLE);
                listView4.setVisibility(View.GONE);
            }
            if (tg.equals("group")) {
                titleBar.setTitle("组播列表");
                createGroup();
                view.findViewById(R.id.title_inout_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_sign_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_alert_layout).setVisibility(View.GONE);
                view.findViewById(R.id.title_group_layout).setVisibility(View.VISIBLE);
                simpleAdapter4.notifyDataSetChanged();
                listView.setVisibility(View.GONE);
                listView2.setVisibility(View.GONE);
                listView3.setVisibility(View.GONE);
                listView4.setVisibility(View.VISIBLE);
            }
        }
    }

    public void nextPage() {
        if (pageIndex + 1 >= totalPage) {
            Toast.makeText(getActivity(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        }

        pageIndex++;
        if (tg.equals("sign")) {
            getSignData();
        }
        if (tg.equals("alert")) {
            getAlertData();
        }
        if (tg.equals("inout")) {
            getInoutData();
        }
        if (tg.equals("group")){
            getGroupData();
        }

        if (tg.equals("sign")) {
            simpleAdapter.notifyDataSetChanged();
        } else if (tg.equals("alert")) {
            simpleAdapter2.notifyDataSetChanged();
        } else if (tg.equals("inout")) {
            simpleAdapter3.notifyDataSetChanged();
        } else if (tg.equals("group")) {
            simpleAdapter4.notifyDataSetChanged();
        }

        modifyPageButton(pageIndex, totalPage);

        Log.e("Main", "MessageFragment: next");
    }

    public void prevPage() {
        if (pageIndex - 1 < 0) {
            Toast.makeText(getActivity(), "已经是第一页了", Toast.LENGTH_SHORT).show();
            return;
        }

        pageIndex--;
        if (tg.equals("sign")) getSignData();
        if (tg.equals("alert")) getAlertData();
        if (tg.equals("inout")) getInoutData();
        if (tg.equals("group")) getGroupData();

        if (tg.equals("sign")) {
            simpleAdapter.notifyDataSetChanged();
        } else if (tg.equals("alert")) {
            simpleAdapter2.notifyDataSetChanged();
        } else if (tg.equals("inout")) {
            simpleAdapter3.notifyDataSetChanged();
        } else if (tg.equals("group")) {
            simpleAdapter4.notifyDataSetChanged();
        }
        modifyPageButton(pageIndex, totalPage);

        Log.e("Main", "MessageFragment: prev");
    }

    private List<Map<String, Object>> getGroupData() {
        dataList4.clear();
        long count = GroupProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }
        modifyPageButton(pageIndex, totalPage);

        List<GroupBean> list = GroupProxy.getByPage(db, logPerPage, pageIndex);
        if (list == null) {
            return dataList4;
        }

        for (int i = 0; i < list.size(); i++) {
            GroupBean group = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * logPerPage - i);
            map.put("group", group.getNumber());
            map.put("name", group.getName());
            dataList4.add(map);
        }

        return dataList4;
    }

    private List<Map<String, Object>> getSignData() {
        dataList.clear();
        long count = SignProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }
        modifyPageButton(pageIndex, totalPage);

        List<SignBean> list = SignProxy.getByPage(db, logPerPage, pageIndex);
        if (list == null) {
            return dataList;
        }

        for (int i = 0; i < list.size(); i++) {
            SignBean sign = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * logPerPage - i);
            map.put("time", DateUtil.Date2String(sign.getTime()));
            map.put("idCard", sign.getIdCard());
            map.put("name", sign.getName());
            dataList.add(map);
        }

        return dataList;
    }

    private List<Map<String, Object>> getAlertData() {
        dataList2.clear();
        long count = AlertProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }
        modifyPageButton(pageIndex, totalPage);
        List<AlertBean> list = AlertProxy.getByPage(db, logPerPage, pageIndex);
        if (list == null) {
            return dataList2;
        }

        for (int i = 0; i < list.size(); i++) {
            AlertBean alert = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * logPerPage - i);
            map.put("time", DateUtil.Date2String(alert.getTime()));
            map.put("type", alert.getType());
            dataList2.add(map);
        }
        return dataList2;
    }

    private List<Map<String, Object>> getInoutData() {

        dataList3.clear();
        long count = InoutProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }
        modifyPageButton(pageIndex, totalPage);
        List<InoutBean> list = InoutProxy.getByPage(db, logPerPage, pageIndex);
        if (list == null) {
            return dataList3;
        }

        // "number", "type", "count", "lon", "lat", "time"
        for (int i = 0; i < list.size(); i++) {
            InoutBean inoutBean = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * logPerPage - i);
            map.put("time", DateUtil.Date2String(inoutBean.getTime()));
            map.put("type", inoutBean.getType() == 1 ? "出港" : "进港");
            map.put("count", inoutBean.getCount());
            map.put("lon", String.format("%.3f", inoutBean.getLon() / 10000000f));
            map.put("lat", String.format("%.3f", inoutBean.getLat() / 10000000f));
            dataList3.add(map);
        }

        return dataList3;
    }

    private void modifyPageButton(int currentPage, int totalPage) {
        MainActivity mainActivity = (MainActivity) getActivity();

        boolean isNext = currentPage == totalPage - 1 ? false : true;
        boolean isPrev = currentPage == 0 ? false : true;
        if (tg.equals("inout")) {
            mainActivity.messageBar.setNextButtonEnable(isNext);
            mainActivity.messageBar.setPrevButtonEnable(isPrev);
        } else {
            mainActivity.pageBar.setNextButtonEnable(isNext);
            mainActivity.pageBar.setPrevButtonEnable(isPrev);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(SmsEvent smsEvent) {

        try {
            JSONObject receiveJson = smsEvent.getReceiveJson();
            String apiType = receiveJson.getString("apiType");
            switch (apiType) {
                case "refreshInout":
                    if (tg.equals("inout")) {
                        getInoutData();
                        simpleAdapter3.notifyDataSetChanged();
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
