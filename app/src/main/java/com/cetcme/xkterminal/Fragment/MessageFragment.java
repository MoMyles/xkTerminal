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
import com.cetcme.xkterminal.RealmModels.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by qiuhong on 10/01/2018.
 */

@SuppressLint("ValidFragment")
public class MessageFragment extends Fragment{

    public MainActivity mainActivity;

    private String tg;
    private TitleBar titleBar;

    private ListView listView;
    private int messagePerPage;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    private int pageIndex = 0;
    private int totalPage = 1;

    private Realm realm;
    public String status;

    public MessageFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "MessageFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = ((MyApplication) getActivity().getApplication()).realm;

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
                new String[]{"selected", "time", status, "content", "read"},
                new int[]{R.id.selected_in_message_cell, R.id.time_in_message_cell, R.id.sender_in_message_cell, R.id.content_in_message_cell, R.id.read_in_message_cell});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
//        mainActivity.kProgressHUD.show();
        dataList.clear();

        RealmResults<Message> messages = realm.where(Message.class)
                .equalTo(tg.equals("send") ? "sender" : "receiver", MainActivity.myNumber)
                .findAll();
        messages = messages.sort("send_time", Sort.DESCENDING);

        totalPage = CommonUtil.getTotalPage(messages.size(), messagePerPage);

        int lastMessageIndex;
        if (messages.size() == 0) {
            lastMessageIndex = 0;
            totalPage = 1;
        } else {
            if (pageIndex == totalPage - 1) {
                lastMessageIndex = messages.size();
            } else {
                lastMessageIndex = (pageIndex + 1) * messagePerPage;
            }
        }

        for (int i = pageIndex * messagePerPage; i < lastMessageIndex; i++) {
            Message message = messages.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("selected", "");
            map.put("time", DateUtil.Date2String(message.getSend_time()));
            map.put("sender", message.getSender());
            map.put("receiver", message.getReceiver());
            map.put("content", message.getContent().replace("\n", " "));
            map.put("id", message.getId());
            map.put("read", message.isRead() ? "" : tg.equals("send") ? "" : "未读");
            dataList.add(map);
        }

        modifyPageButton(pageIndex, totalPage);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mainActivity.kProgressHUD.dismiss();
//                mainActivity.okHUD.show();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mainActivity.okHUD.dismiss();
//                    }
//                },700);
//            }
//        },500);

        return dataList;
    }

    public void setMessageRead(int index) {

        dataList.get(index).put("read", "");
        simpleAdapter.notifyDataSetChanged();

        final String id = dataList.get(index).get("id").toString();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //先查找后得到User对象
                Message message = realm.where(Message.class).equalTo("id", id).findFirst();
                message.setRead(true);
            }
        });
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
