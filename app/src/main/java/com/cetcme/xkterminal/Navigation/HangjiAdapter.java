package com.cetcme.xkterminal.Navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;

import java.text.SimpleDateFormat;
import java.util.List;

public class HangjiAdapter extends BaseAdapter {

    private Context mContext;
    private List<LocationBean> datas;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public HangjiAdapter(Context context, List<LocationBean> datas) {
        mContext = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public LocationBean getItem(int i) {
        return datas == null ? null : datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh = null;
        if (view == null) {
            vh = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_hangji, viewGroup, false);
            vh.tv1 = view.findViewById(R.id.tv_lng);
            vh.tv2 = view.findViewById(R.id.tv_lat);
            vh.tv3 = view.findViewById(R.id.tv_speed);
            vh.tv4 = view.findViewById(R.id.tv_time);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        LocationBean item = getItem(i);
        if (item != null) {
            vh.tv1.setText(item.getLongitude()/1e7 + "");
            vh.tv2.setText(item.getLatitude()/1e7 + "");
            vh.tv3.setText(item.getSpeed() + "");
            vh.tv4.setText(sdf.format(item.getAcqtime()) + "");
        } else {
            vh.tv1.setText("");
            vh.tv2.setText("");
            vh.tv3.setText("");
            vh.tv4.setText("");
        }
        return view;
    }

    class ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;
    }
}
