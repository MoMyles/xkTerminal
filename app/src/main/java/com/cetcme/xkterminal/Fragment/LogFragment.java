package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.AlertBean;
import com.cetcme.xkterminal.Sqlite.Bean.SignBean;
import com.cetcme.xkterminal.Sqlite.Proxy.AlertProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.SignProxy;

import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qiuhong on 10/01/2018.
 */

@SuppressLint("ValidFragment")
public class LogFragment extends Fragment{

    private String tg;
    private TitleBar titleBar;

    private ListView listView;
    private int logPerPage;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    private int pageIndex = 0;
    private int totalPage = 1;

    private DbManager db;

    public LogFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "LogFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = ((MyApplication) getActivity().getApplication()).db;

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_log,container,false);

        logPerPage = CommonUtil.getCountPerPage(getContext(), getActivity());

        titleBar = view.findViewById(R.id.titleBar);
        if (tg.equals("sign")) {
            titleBar.setTitle("打卡记录");
            view.findViewById(R.id.title_alert_layout).setVisibility(View.GONE);
            simpleAdapter = new SimpleAdapter(getActivity(), getSignData(), R.layout.cell_sign_list,
                    new String[]{"number", "idCard", "name", "time"},
                    new int[]{R.id.number_in_sign_cell, R.id.idCard_in_sign_cell, R.id.name_in_sign_cell, R.id.time_in_sign_cell});
        }
        if (tg.equals("alert")) {
            titleBar.setTitle("报警记录");
            view.findViewById(R.id.title_sign_layout).setVisibility(View.GONE);
            simpleAdapter = new SimpleAdapter(getActivity(), getAlertData(), R.layout.cell_alert_list,
                    new String[]{"number", "type", "time"},
                    new int[]{R.id.number_in_alert_cell, R.id.type_in_alert_cell, R.id.time_in_alert_cell});
        }

        //设置listView
        listView = view.findViewById(R.id.list_view);
        listView.setAdapter(simpleAdapter);

        return view;
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
        simpleAdapter.notifyDataSetChanged();

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
        simpleAdapter.notifyDataSetChanged();

        modifyPageButton(pageIndex, totalPage);

        Log.e("Main", "MessageFragment: prev");
    }

    private List<Map<String, Object>> getSignData() {
        dataList.clear();
        long count = SignProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }

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

        modifyPageButton(pageIndex, totalPage);

        return dataList;
    }

    private List<Map<String, Object>> getAlertData() {
        dataList.clear();
        long count = AlertProxy.getCount(db);
        totalPage = CommonUtil.getTotalPage(count, logPerPage);
        if (count == 0) {
            totalPage = 1;
        }

        List<AlertBean> list = AlertProxy.getByPage(db, logPerPage, pageIndex);
        if (list == null) {
            return dataList;
        }

        for (int i = 0; i < list.size(); i++) {
            AlertBean alert = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * logPerPage - i);
            map.put("time", DateUtil.Date2String(alert.getTime()));
            map.put("type", alert.getType());
            dataList.add(map);
        }

        modifyPageButton(pageIndex, totalPage);

        return dataList;
    }

    private void modifyPageButton(int currentPage, int totalPage) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (currentPage == totalPage - 1) {
            mainActivity.pageBar.setNextButtonEnable(false);
        } else {
            mainActivity.pageBar.setNextButtonEnable(true);
        }

        if (currentPage == 0) {
            mainActivity.pageBar.setPrevButtonEnable(false);
        } else {
            mainActivity.pageBar.setPrevButtonEnable(true);
        }
    }
}
