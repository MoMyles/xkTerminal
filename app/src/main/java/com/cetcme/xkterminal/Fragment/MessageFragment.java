package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by qiuhong on 10/01/2018.
 */

@SuppressLint("ValidFragment")
public class MessageFragment extends Fragment {

    public MainActivity mainActivity;

    public String tg;
    private TitleBar titleBar;

    private TextView titleTextView;
    private ListView listView;
    private ListView listView2;
    private int messagePerPage;
    private SimpleAdapter simpleAdapter;
    private SimpleAdapter simpleAdapter2;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    private int pageIndex = 0;
    private int totalPage = 1;

    public String status;

    private DbManager db;

    public MessageFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "MessageFragment: " + tg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = ((MyApplication) getActivity().getApplication()).db;

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message, container, false);

        messagePerPage = CommonUtil.getCountPerPage(getContext(), getActivity());

        titleBar = view.findViewById(R.id.titleBar);
        titleTextView = view.findViewById(R.id.sender_title_textView);

        //设置listView
        listView = view.findViewById(R.id.list_view);
        listView2 = view.findViewById(R.id.list_view2);
        create(listView, dataList);
        create2(listView2, dataList);
        changeTitle();
        mainActivity.messageBar.setDetailAndRelayButtonEnable(false);

        return view;
    }


    private void create(ListView listView, final List<Map<String, Object>> dataList) {
        simpleAdapter = new SimpleAdapter(getActivity(), getMessageData(), R.layout.cell_message_list,
                new String[]{"number", "time", "receiver", "content", "status"},
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
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                QMUIBottomSheet.BottomListSheetBuilder bottomListSheetBuilder = new QMUIBottomSheet.BottomListSheetBuilder(getActivity());
                bottomListSheetBuilder.addItem("转发");
                if (tg.equals("send")) {
                    bottomListSheetBuilder.addItem("重新发送");
                } else if (tg.equals("receive")) {
                    bottomListSheetBuilder.addItem("回复");
                }
                bottomListSheetBuilder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
//                        Toast.makeText(getActivity(), "Item " + (position + 1), Toast.LENGTH_SHORT).show();

                        mainActivity.messageId = dataList.get(i).get("id").toString();
                        mainActivity.messageContent = dataList.get(i).get("content").toString();
                        mainActivity.messageTime = dataList.get(i).get("time").toString();
                        if (position == 0) {
                            mainActivity.initNewFragment("relay");
                        } else if (position == 1) {
                            if (tg.equals("send")) {
                                // 重新发送
                                mainActivity.messageReceiver = dataList.get(i).get("receiver").toString();
                                mainActivity.initNewFragment("resend");
                            } else {
                                // 回复
                                mainActivity.messageReceiver = dataList.get(i).get("sender").toString();
                                mainActivity.initNewFragment("reply");
                            }

                        }
                    }
                });
                QMUIBottomSheet qmuiBottomSheet = bottomListSheetBuilder.build();
                qmuiBottomSheet.show();

                return true;
            }
        });
    }

    private void create2(ListView listView, final List<Map<String, Object>> dataList) {
        simpleAdapter2 = new SimpleAdapter(getActivity(), getMessageData(), R.layout.cell_message_list,
                new String[]{"number", "time", "sender", "content", "status"},
                new int[]{R.id.selected_in_message_cell, R.id.time_in_message_cell, R.id.sender_in_message_cell, R.id.content_in_message_cell, R.id.status_in_message_cell});
        listView.setAdapter(simpleAdapter2);
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
                simpleAdapter2.notifyDataSetChanged();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                QMUIBottomSheet.BottomListSheetBuilder bottomListSheetBuilder = new QMUIBottomSheet.BottomListSheetBuilder(getActivity());
                bottomListSheetBuilder.addItem("转发");
                if (tg.equals("send")) {
                    bottomListSheetBuilder.addItem("重新发送");
                } else if (tg.equals("receive")) {
                    bottomListSheetBuilder.addItem("回复");
                }
                bottomListSheetBuilder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
//                        Toast.makeText(getActivity(), "Item " + (position + 1), Toast.LENGTH_SHORT).show();

                        mainActivity.messageId = dataList.get(i).get("id").toString();
                        mainActivity.messageContent = dataList.get(i).get("content").toString();
                        mainActivity.messageTime = dataList.get(i).get("time").toString();
                        if (position == 0) {
                            mainActivity.initNewFragment("relay");
                        } else if (position == 1) {
                            if (tg.equals("send")) {
                                // 重新发送
                                mainActivity.messageReceiver = dataList.get(i).get("receiver").toString();
                                mainActivity.initNewFragment("resend");
                            } else {
                                // 回复
                                mainActivity.messageReceiver = dataList.get(i).get("sender").toString();
                                mainActivity.initNewFragment("reply");
                            }

                        }
                    }
                });
                QMUIBottomSheet qmuiBottomSheet = bottomListSheetBuilder.build();
                qmuiBottomSheet.show();

                return true;
            }
        });
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    private void changeTitle() {
        if (tg.equals("send")) {
            titleBar.setTitle("发件箱");
            titleTextView.setText("收件人");
            status = "receiver";
            listView.setVisibility(View.VISIBLE);
            listView2.setVisibility(View.GONE);
        } else if (tg.equals("receive")) {
            titleBar.setTitle("收件箱");
            titleTextView.setText("发件人");
            status = "sender";
            listView.setVisibility(View.GONE);
            listView2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        pageIndex = 0;
        totalPage = 1;
        if (!hidden) {
            changeTitle();
            getMessageData();
            if ("send".equals(tg)) {
                simpleAdapter.notifyDataSetChanged();
            }else{
                simpleAdapter2.notifyDataSetChanged();
            }
        }
    }

    private int selectedIndex = -1;

    public void nextPage() {
        if (pageIndex + 1 >= totalPage) {
            Toast.makeText(getActivity(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        }

        pageIndex++;
        getMessageData();
        if ("send".equals(tg)) {
            simpleAdapter.notifyDataSetChanged();
        }else{
            simpleAdapter2.notifyDataSetChanged();
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
        getMessageData();
        if ("send".equals(tg)) {
            simpleAdapter.notifyDataSetChanged();
        }else{
            simpleAdapter2.notifyDataSetChanged();
        }

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
        modifyPageButton(pageIndex, totalPage);

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
            map.put("sendOK", message.isSendOK());
            if (tg.equals("send")) {
                map.put("status", message.isSendOK() ? "" : "失败");
            } else {
                map.put("status", message.isRead() ? "" : "未读");
            }
            dataList.add(map);
        }
        return dataList;
    }

    public void setMessageRead(int index) {

        dataList.get(index).put("read", "");
        if ("send".equals(tg)) {
            simpleAdapter.notifyDataSetChanged();
        }else{
            simpleAdapter2.notifyDataSetChanged();
        }

        int id = Integer.parseInt(dataList.get(index).get("id").toString());
        MessageProxy.setMessageReadById(db, id);
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
        if ("send".equals(tg)) {
            simpleAdapter.notifyDataSetChanged();
        }else{
            simpleAdapter2.notifyDataSetChanged();
        }
    }
}
