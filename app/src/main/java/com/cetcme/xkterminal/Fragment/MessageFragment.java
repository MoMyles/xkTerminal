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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
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
import io.realm.RealmResults;

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
    private String status;

    public MessageFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "MessageFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = ((MyApplication) getActivity().getApplication()).realm;

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message,container,false);

        // 通过WindowManager获取
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenHeight = DensityUtil.px2dip(getContext(), (float) dm.heightPixels);
        int messageListHeight = screenHeight - 60 - 50 - 60 - 50; // gps 60 bottom 60 title 50
        messagePerPage = messageListHeight / 50;

        titleBar = view.findViewById(R.id.titleBar);
        TextView titleTextView = view.findViewById(R.id.sender_title_textView);
        if (tg.equals("send")) {
            titleBar.setTitle("发件箱");
            titleTextView.setText("收件人");
            status = "sender";
        }
        if (tg.equals("receive")) {
            titleBar.setTitle("收件箱");
            titleTextView.setText("发件人");
            status = "receiver";
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

        RealmResults<Message> messages = realm.where(Message.class).equalTo(status.equals("sender") ? "receiver" : "sender", "123456").findAll();

        dataList.clear();
//        for (int i = 0; i < messagePerPage; i++) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("selected", "");
//            map.put("time", "2018/01/10 09:58:3" + i);
//            map.put("sender", "123456");
//            map.put("content", "message content" + pageIndex);
//            int id = pageIndex * messagePerPage + i;
//            map.put("id", id);
//            map.put("read", id < 3 ? "未读" : "");
//            dataList.add(map);
//        }
//        totalPage = 4;

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("selected", "");
            map.put("time", DateUtil.Date2String(message.getSend_time()));
            map.put("sender", message.getSender());
            map.put("receiver", message.getReceiver());
            map.put("content", message.getContent());
            int id = pageIndex * messagePerPage + i;
            map.put("id", id);
            map.put("read", message.isRead() ? "未读" : "");
            dataList.add(map);
        }

        totalPage = 1;
        modifyPageButton(pageIndex, totalPage);
        return dataList;
    }

    public void setMessageRead(int index) {
        dataList.get(index).put("read", "");
        simpleAdapter.notifyDataSetChanged();
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
}
