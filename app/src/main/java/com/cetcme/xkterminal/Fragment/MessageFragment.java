package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.MyClass.DensityUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;

import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by qiuhong on 10/01/2018.
 */

@SuppressLint("ValidFragment")
public class MessageFragment extends Fragment{

    public MainActivity mainActivity;

    public String tg;
    private TitleBar titleBar;

    private ListView listView;
    private int messagePerPage;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    private int pageIndex = 0;
    private int totalPage = 1;

    public String status;

    private DbManager db;

    public MessageFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "MessageFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = ((MyApplication) getActivity().getApplication()).db;

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message,container,false);

        messagePerPage = CommonUtil.getCountPerPage(getContext(), getActivity());

        titleBar = view.findViewById(R.id.titleBar);
        TextView titleTextView = view.findViewById(R.id.sender_title_textView);
        if (tg.equals("send")) {
            titleBar.setTitle("发件箱");
            titleTextView.setText("收件人");
            status = "receiver";
        }
        if (tg.equals("receive")) {
            titleBar.setTitle("收件箱");
            titleTextView.setText("发件人");
            status = "sender";
        }

        //设置listView
        listView = view.findViewById(R.id.list_view);
        simpleAdapter = new SimpleAdapter(getActivity(), getMessageData(), R.layout.cell_message_list,
                new String[]{"number", "time", status, "content", "status"},
                new int[]{R.id.selected_in_message_cell, R.id.time_in_message_cell, R.id.sender_in_message_cell, R.id.content_in_message_cell, R.id.status_in_message_cell});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                /* 详情 和 转发按钮
                System.out.println("message id: " + dataList.get(i));
                mainActivity.messageBar.setDetailAndRelayButtonEnable(true);
                if (i == selectedIndex) return;
                dataList.get(i).put("selected", "●");
                if (selectedIndex != -1) dataList.get(selectedIndex).put("selected", "");
                selectedIndex = i;
                simpleAdapter.notifyDataSetChanged();

                mainActivity.messageIndex = i;
                mainActivity.messageId = dataList.get(i).get("id").toString();
                mainActivity.messageReceiver = dataList.get(i).get("sender").toString();
                mainActivity.messageContent = dataList.get(i).get("content").toString();
                mainActivity.messageTime = dataList.get(i).get("time").toString();
                */


                // 单击进入
                mainActivity.messageIndex = i;
                mainActivity.messageId = dataList.get(i).get("id").toString();
                if (tg.equals("receive")) {
                    mainActivity.messageReceiver = dataList.get(i).get("sender").toString();
                } else {
                    mainActivity.messageReceiver = dataList.get(i).get("receiver").toString();
                }
                mainActivity.messageContent = dataList.get(i).get("content").toString();
                mainActivity.messageTime = dataList.get(i).get("time").toString();
                mainActivity.initNewFragment("detail");

                dataList.get(i).put("status", "");
                simpleAdapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("message id: " + dataList.get(i));
                mainActivity.messageId = dataList.get(i).get("id").toString();
                mainActivity.messageReceiver = dataList.get(i).get("sender").toString();
                mainActivity.messageContent = dataList.get(i).get("content").toString();
                mainActivity.messageTime = dataList.get(i).get("time").toString();
                mainActivity.initNewFragment("relay");
                return true;
            }
        });

        mainActivity.messageBar.setDetailAndRelayButtonEnable(false);

        return view;
    }

    private int selectedIndex = -1;

    public void nextPage() {
        if (pageIndex + 1 >= totalPage) {
            Toast.makeText(getActivity(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        }

        pageIndex++;
        getMessageData();
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
        getMessageData();
        simpleAdapter.notifyDataSetChanged();

        modifyPageButton(pageIndex, totalPage);

        Log.e("Main", "MessageFragment: prev");
    }

    private List<Map<String, Object>> getMessageData() {
        dataList.clear();

        long count = MessageProxy.getCount(db, tg.equals("send"));

        totalPage = CommonUtil.getTotalPage(count, messagePerPage);
        if (count == 0) {
            totalPage = 1;
        }

        List<MessageBean> list = MessageProxy.getByPage(db, tg.equals("send"), messagePerPage, pageIndex);
        if (list == null) {
            return dataList;
        }
        for (int i = 0; i < list.size(); i++) {
            MessageBean message = list.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("number", count - pageIndex * messagePerPage - i);
            map.put("selected", "");
            map.put("time", DateUtil.Date2String(message.getSend_time()));
            map.put("sender", message.getSender());
            map.put("receiver", message.getReceiver());
            map.put("content", message.getContent().replace("\n", " "));
            map.put("id", message.getId());
            if (tg.equals("send")) {
                map.put("status", message.isSendOK() ? "" : "失败");
            } else {
                map.put("status", message.isRead() ? "" : "未读");
            }
            dataList.add(map);
        }

        modifyPageButton(pageIndex, totalPage);

        return dataList;
    }

    public void setMessageRead(int index) {

        dataList.get(index).put("read", "");
        simpleAdapter.notifyDataSetChanged();

        int id = Integer.parseInt(dataList.get(index).get("id").toString());
        MessageProxy.setMessageRead(db, id);
        mainActivity.modifyGpsBarMessageCount();
    }

    private void modifyPageButton(int currentPage, int totalPage) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (currentPage == totalPage - 1) {
            mainActivity.messageBar.setNextButtonEnable(false);
        } else {
            mainActivity.messageBar.setNextButtonEnable(true);
        }

        if (currentPage == 0) {
            mainActivity.messageBar.setPrevButtonEnable(false);
        } else {
            mainActivity.messageBar.setPrevButtonEnable(true);
        }
    }

    public void reloadDate() {
        getMessageData();
        simpleAdapter.notifyDataSetChanged();
    }
}
