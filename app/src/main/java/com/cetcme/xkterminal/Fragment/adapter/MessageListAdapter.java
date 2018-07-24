package com.cetcme.xkterminal.Fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {

    Context context;
    List<MessageBean> dataList;
    String tg;

    boolean canCheck;

    int pageIndex, messagePerPage;
    long count;

    public MessageListAdapter(Context context, List<MessageBean> dataList, String tg, int pageIndex, int messagePerPage, boolean canCheck) {
        this.context = context;
        this.dataList = dataList;
        this.tg = tg;
        this.pageIndex = pageIndex;
        this.messagePerPage = messagePerPage;
        this.canCheck = canCheck;
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    public void setCanCheck(boolean canCheck) {
        this.canCheck = canCheck;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.cell_message_list_checkbox,null);
            holder = new ViewHolder();
            holder.checkBox = view.findViewById(R.id.checkBox);
            holder.tv_index = view.findViewById(R.id.tv_index);
            holder.tv_time = view.findViewById(R.id.tv_time);
            holder.tv_address = view.findViewById(R.id.tv_address);
            holder.tv_content = view.findViewById(R.id.tv_content);
            holder.tv_status = view.findViewById(R.id.tv_status);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        count = MessageProxy.getCount(MyApplication.getInstance().getDb(), tg.equals("send"));

        MessageBean messageBean = dataList.get(i);

        long index = count - pageIndex * messagePerPage - i;
        holder.tv_index.setText(index + "");
        holder.tv_time.setText(DateUtil.Date2String(messageBean.getSend_time()));

        holder.tv_address.setText(tg.equals("send") ? messageBean.getReceiver() : messageBean.getSender());
        holder.tv_content.setText(messageBean.getContent().replace("\n", " "));
        if (tg.equals("send")) {
            holder.tv_status.setText(messageBean.isSendOK() ? "" : "失败");
        } else {
            holder.tv_status.setText(messageBean.isRead() ? "" : "未读");
        }

        if (canCheck) {
            holder.checkBox.setVisibility(View.VISIBLE);

        } else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    class ViewHolder {
        TextView tv_index, tv_time, tv_address, tv_content, tv_status;
        CheckBox checkBox;
    }
}
