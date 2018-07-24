package com.cetcme.xkterminal.Fragment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;

import java.util.List;

public class RvShipAdapter extends RecyclerView.Adapter<RvShipAdapter.VH> {

    private Context mContext;
    private List<OtherShipBean> mDatas;
    private SkiaDrawView skiaDrawView;


    public RvShipAdapter(Context context, List<OtherShipBean> datas, SkiaDrawView skiaDrawView) {
        this.mContext = context;
        this.mDatas = datas;
        this.skiaDrawView = skiaDrawView;
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_ship_info, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final OtherShipBean osb = mDatas.get(position);
        if (osb != null) {
            if (osb.isShow()) {
                holder.mLlShipInfo.setVisibility(View.VISIBLE);
            } else {
                holder.mLlShipInfo.setVisibility(View.GONE);
            }
//            int vessel_id = skiaDrawView.mYimaLib.GetOtherVesselPosOfID(osb.getShip_id());
//            final OtherShipBaicInfo osbi = skiaDrawView.mYimaLib.getOtherVesselBasicInfo(vessel_id);
            //final OtherVesselCurrentInfo ovci = skiaDrawView.mYimaLib.getOtherVesselCurrentInfo(vessel_id);
            String shipName = osb.getShip_name() == null ? "" :  osb.getShip_name();
            holder.mmsi.setText("MMSI: " + osb.getMmsi() + " 船名: " + shipName);
            holder.tv1.setText(osb.getCallsign());
            holder.length.setText(osb.getLenght() + " m");
            holder.width.setText(osb.getWidth() + " m");
            if (osb != null) {
                holder.hangxiang.setText(osb.getCog() + " °");
                holder.speed.setText(osb.getSog() + " kn");
                holder.lon.setText("" + (osb.getLongitude() * 1.0 / 1e7));
                holder.lat.setText("" + (osb.getLatitude() * 1.0 / 1e7));
            } else {
                holder.hangxiang.setText("0 °");
                holder.speed.setText("0 kn");
                holder.lon.setText("");
                holder.lat.setText("");
            }
            holder.acq.setText(DateUtil.Date2String(osb.getAcq_time()));
            holder.mLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (osb != null) {
                        //M_POINT point = ovci.currentPoint;
                        skiaDrawView.mYimaLib.CenterMap(osb.getLongitude(), osb.getLatitude());
                        skiaDrawView.postInvalidate();
                    }
                    boolean isShow = osb.isShow();
                    for (OtherShipBean o : mDatas) {
                        o.setShow(false);
                    }
                    osb.setShow(!isShow);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.mLlShipInfo.setVisibility(View.GONE);
            holder.tv1.setText("");
            holder.mmsi.setText("");
            holder.mLl.setOnClickListener(null);
            holder.length.setText("0 m");
            holder.width.setText("0 m");
            holder.hangxiang.setText("0 °");
            holder.speed.setText("0 kn");
            holder.lon.setText("0.0 °");
            holder.lat.setText("0.0 °");
            holder.acq.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    class VH extends RecyclerView.ViewHolder {

        LinearLayout mLl, mLlShipInfo;
        TextView mmsi, tv1, length, width, hangxiang, speed, lon, lat, acq;

        public VH(View itemView) {
            super(itemView);
            mLl = itemView.findViewById(R.id.ll_ship);
            mLlShipInfo = itemView.findViewById(R.id.ll_ship_info);
            mmsi = itemView.findViewById(R.id.tv_mmsi);
            tv1 = itemView.findViewById(R.id.tv1);
            length = itemView.findViewById(R.id.tv_ship_length);
            width = itemView.findViewById(R.id.tv_ship_width);
            hangxiang = itemView.findViewById(R.id.tv_ship_hangxiang);
            speed = itemView.findViewById(R.id.tv_ship_speed);
            lon = itemView.findViewById(R.id.tv_ship_lon);
            lat = itemView.findViewById(R.id.tv_ship_lat);
            acq = itemView.findViewById(R.id.tv_ship_acq);
        }
    }
}
