package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.GPSFormatUtils;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import yimamapapi.skia.AisInfo;
import yimamapapi.skia.M_POINT;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MainFragment extends Fragment {

    private SkiaDrawView skiaDrawView;
    //    private LinearLayout main_layout;
    private LinearLayout alert_layout;


    private TextView tv_lon;
    private TextView tv_lat;
    private TextView tv_head;
    private TextView tv_speed;

    private QMUIRoundButton alert_confirm_btn;
    private QMUIRoundButton alert_cancel_btn;
    private TextView alert_tv;

    private boolean alert_need_flash = false;

    private DbManager db;

    private ArrayAdapter<String> aisInfoAdapter;
    private ListView mLvAisInfo;
    private final List<String> datas = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main, container, false);
        EventBus.getDefault().register(this);

//        main_layout = view.findViewById(R.id.main_layout);
        skiaDrawView = view.findViewById(R.id.skiaView);
        alert_layout = view.findViewById(R.id.alert_layout);

        tv_lon = view.findViewById(R.id.tv_lon);
        tv_lat = view.findViewById(R.id.tv_lat);
        tv_head = view.findViewById(R.id.tv_head);
        tv_speed = view.findViewById(R.id.tv_speed);

        mLvAisInfo = view.findViewById(R.id.ais_info_lv);
        aisInfoAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, datas);
        mLvAisInfo.setAdapter(aisInfoAdapter);



        alert_confirm_btn = view.findViewById(R.id.alert_confirm_btn);
        alert_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmAlert();
            }
        });

        alert_cancel_btn = view.findViewById(R.id.alert_cancel_btn);
        alert_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlert();
            }
        });

        alert_tv = view.findViewById(R.id.alert_tv);

        view.findViewById(R.id.app_name_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ((MainActivity)getActivity()).showShutDownHud();
//                ((MyApplication) getActivity().getApplication()).sendLightOn(true);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((MyApplication) getActivity().getApplication()).sendLightOn(false);
//                    }
//                }, 2000);
            }
        });

        db = MyApplication.getInstance().getDb();

        if (PreferencesUtils.getBoolean(getActivity(), "homePageAlertView")) {
            showAlertLayout();
        } else {
            showMainLayout();
        }
        return view;
    }

    public void showMainLayout() {
        alert_need_flash = false;
        SoundPlay.stopAlertSound();

//        main_layout.setVisibility(View.VISIBLE);
        skiaDrawView.setVisibility(View.VISIBLE);
        alert_layout.setVisibility(View.GONE);
    }

    private void showAlertLayout() {
//        main_layout.setVisibility(View.GONE);
        skiaDrawView.setVisibility(View.GONE);
        alert_layout.setVisibility(View.VISIBLE);
        alert_need_flash = true;
        if (Constant.ALERT_FLASH_TIME != 0) {
            alert_tv.setVisibility(View.INVISIBLE);
            new TimeHandler().start();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SmsEvent event) {
        JSONObject receiveJson = event.getReceiveJson();
        try {
            String apiType = receiveJson.getString("apiType");
            switch (apiType) {
                case "showAlertInHomePage":
                    if (alert_layout.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", true);
                    showAlertLayout();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onDestroy() {
        super.onDestroy();
        alert_need_flash = false;
        EventBus.getDefault().unregister(this);
    }


    class TimeHandler extends Thread {
        @Override
        public void run() {
            super.run();
            int i = 0;
            do {

                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }

                if (Constant.ALERT_FLASH_TIME != 0) {
                    int flashTime = Constant.ALERT_FLASH_TIME / 100;
                    if (i % flashTime == 0) {
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                    if (i == flashTime) i = 0;
                }

                i++;
            } while (alert_need_flash);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    alert_tv.setVisibility(alert_tv.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 解除报警，用户手动解除或者北斗中心解除
     */
    public void cancelAlert() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("解除报警")
                .setMessage("确定要解除吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(0, "解除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                        // 解除报警操作
                        ((MyApplication) getActivity().getApplication()).sendBytes(AlertFormat.format("00010000", "00000000"));
                        PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", false);
                        showMainLayout();

                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 确认报警
     */
    private void confirmAlert() {
        PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", false);
        showMainLayout();
        PreferencesUtils.putBoolean(getActivity(), "flashAlert", true);
    }


    private M_POINT myLocation;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(Object locationBean) {
        if (locationBean instanceof LocationBean) {
            LocationBean lb = (LocationBean) locationBean;
            Log.i("TAG", "onLocationEvent: 收到自身位置");
            Log.i("TAG", "onLocationEvent: 纬度lat:" + lb.getLatitude());
            Log.i("TAG", "onLocationEvent: 经度lon:" + lb.getLongitude());
            if (myLocation == null) {
                myLocation = new M_POINT();
                myLocation.x = lb.getLongitude();
                myLocation.y = lb.getLatitude();
                skiaDrawView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
            } else {
                myLocation.x = lb.getLongitude();
                myLocation.y = lb.getLatitude();
            }

            // 根据每次gps信息更新位置
            setOwnShip(myLocation, lb.getHeading(), false);

            // 更新框
            updateShipInfo(lb);
        } else if (locationBean instanceof AisInfo) {
            Toast.makeText(getActivity(), "收到AIS信息", Toast.LENGTH_SHORT).show();
            AisInfo aisInfo = (AisInfo) locationBean;
            int mmsi = aisInfo.mmsi;
            try {
                OtherShipBean osb = db.selector(OtherShipBean.class).where("mmsi", "=", mmsi).findFirst();
                if (osb == null) {
                    // 不存在， 新增
                    osb = new OtherShipBean();
                    osb.setMmsi(mmsi);
                    int ship_id = skiaDrawView.mYimaLib.AddOtherVessel(true
                            , aisInfo.longtitude, aisInfo.latititude,aisInfo.COG,aisInfo.COG
                            ,0,aisInfo.SOG, 0);
                    osb.setId(ship_id);
                } else {
                    // 存在， 更新信息
                    int versselId = skiaDrawView.mYimaLib.GetOtherVesselPosOfID(osb.getShip_id());
                    skiaDrawView.mYimaLib.SetOtherVesselCurrentInfo(versselId
                            , aisInfo.longtitude, aisInfo.latititude,aisInfo.COG,aisInfo.COG
                            ,0,aisInfo.SOG, 0);
                }
                // 显示所有船
                skiaDrawView.mYimaLib.SetAllOtherVesselDrawOrNot(true);

                String str = String.format("mmsi:{0},msgType:{1},shipName:{2},cog:{3},sog:{4}", aisInfo.mmsi, aisInfo.MsgType,aisInfo.shipName, aisInfo.COG
                , aisInfo.SOG);
                datas.add(str);
                aisInfoAdapter.notifyDataSetChanged();
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置本船
     */
    public void setOwnShip(M_POINT m_point, float heading, boolean rotateScreen) {
        skiaDrawView.mYimaLib.SetOwnShipBasicInfo("本船", "123456789", 100, 50);
        skiaDrawView.mYimaLib.SetOwnShipCurrentInfo(m_point.x, m_point.y, heading, 50, 50, 0, 0);
        skiaDrawView.mYimaLib.SetOwnShipShowSymbol(false, 4, true, 16, 5000000);
        skiaDrawView.mYimaLib.RotateMapByScrnCenter(rotateScreen ? 0 - heading : 0);
        skiaDrawView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        skiaDrawView.postInvalidate();
    }

    /**
     * 更新GPS状态框
     *
     * @param locationBean
     */
    private void updateShipInfo(LocationBean locationBean) {
        tv_lon.setText(GPSFormatUtils.DDtoDMS(locationBean.getLongitude() / 10000000d, true));
        tv_lat.setText(GPSFormatUtils.DDtoDMS(locationBean.getLatitude() / 10000000d, false));
        tv_head.setText(locationBean.getHeading() + "°");
        tv_speed.setText(String.format("%.1f", locationBean.getSpeed()) + "kn");
    }

}
