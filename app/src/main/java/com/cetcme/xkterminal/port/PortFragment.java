package com.cetcme.xkterminal.port;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.codice.common.ais.Decoder;
import org.codice.common.ais.message.Message18;
import org.codice.common.ais.message.Message19;
import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import aisparser.Message1;
import aisparser.Message11;
import aisparser.Message14;
import aisparser.Message2;
import aisparser.Message24;
import aisparser.Message3;
import aisparser.Message4;
import aisparser.Message5;
import aisparser.Sixbit;
import aisparser.Vdm;
import android_serialport_api.SerialPort;
import yimamapapi.skia.AisInfo;

import static com.cetcme.xkterminal.MainActivity.play;

public class PortFragment extends Fragment implements View.OnClickListener {

    public static final String MO_GU_TOU = "382570";

    private TextView tv1, tv2, tv3;
    private Button btn1, btn2;

    private String com1 = "";
    private int buadRate1 = 38400;
    private boolean _16Scale1 = false;

    private boolean open1 = false;
    private SerialPort port1 = null;
    private InputStream is1 = null;
    private OutputStream os1 = null;
    private AisReadThread aisReadThread;

    private QMUIBottomSheet comSheet, baudRateSheet;

    private DbManager db;


    public static PortFragment newInstance() {

        Bundle args = new Bundle();

        PortFragment fragment = new PortFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ports, container, false);
        db = MyApplication.getInstance().getDb();
        onBindView(view, savedInstanceState);
        return view;
    }

    private void onBindView(View view, Bundle savedInstanceState) {
        tv1 = view.findViewById(R.id.tv1);
        tv2 = view.findViewById(R.id.tv2);
        tv3 = view.findViewById(R.id.tv3);
        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);

        comSheet = new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem("COM1", "/dev/ttyS1")
                .addItem("COM2", "/dev/ttyS2")
                .addItem("COM3", "/dev/ttyS3")
                .addItem("COM4", "/dev/ttyS4")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        com1 = tag;
                        tv1.setText(com1);
                    }
                })
                .build();

        baudRateSheet = new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem("38400", "38400")
                .addItem("9600", "9600")
                .addItem("4800", "4800")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        buadRate1 = Integer.valueOf(tag);
                        tv2.setText(buadRate1);
                    }
                })
                .build();
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv1:
                if (comSheet != null && !comSheet.isShowing()) {
                    comSheet.show();
                }
                break;
            case R.id.tv2:
                if (baudRateSheet != null && !baudRateSheet.isShowing()) {
                    baudRateSheet.show();
                }
                break;
            case R.id.btn1:
                // 打开串口
                if (open1) {
                    close1();
                    btn1.setText("打开串口");
                } else {
                    try {
                        if (port1 == null) {
                            port1 = open(com1, buadRate1);
                        }
                        is1 = port1.getInputStream();
                        os1 = port1.getOutputStream();
                        aisReadThread = new AisReadThread();
                        aisReadThread.start();
                        btn1.setText("关闭串口");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "打开串口失败", Toast.LENGTH_SHORT).show();
                        close1();
                    }
                }
                break;
            case R.id.btn2:
                if (_16Scale1) {
                    //16进制
                    btn2.setText("CHAR");
                } else {
                    // 显示结果
                    btn2.setText("HEX");
                }
                _16Scale1 = !_16Scale1;
                break;
        }
    }

    private SerialPort open(String com, int buadRate) throws IOException {
        if (TextUtils.isEmpty(com) || (com != null && com.length() == 0) || (buadRate == -1)) {
            throw new InvalidParameterException();
        }
        return new SerialPort(new File(com), buadRate, 0);
    }

    public void close1() {
        try {
            if (aisReadThread != null) {
                aisReadThread.interrupt();
            }
            if (is1 != null) {
                is1.close();
            }
            if (os1 != null) {
                os1.close();
            }
            if (port1 != null) {
                port1.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        is1 = null;
        os1 = null;
        port1 = null;
    }


    class AisReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1];
                    if (is1 == null) return;
                    size = is1.read(buffer);
                    if (size > 0) {
                        onAisDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    private final List<Byte> aisByts = new LinkedList<>();
    private String preRestStr = "";
    private final List<Map<String, Object>> headIndex = new ArrayList<>();
    private static final Vdm vdm = new Vdm();

    protected void onAisDataReceived(byte[] buffer, int size) {
//        Log.i(TAG, "16进制：" + ConvertUtil.bytesToHexString(ByteUtil.subBytes(buffer, 0, size)));
//        AisInfo a = YimaAisParse.mParseAISSentence("!AIVDM,1,1,,A,15MgK45P3@G?fl0E`JbR0OwT0@MS,0*4E");
        if (buffer[0] == 33 || buffer[0] == 36) {
            MyApplication.getInstance().oldAisReceiveTime = System.currentTimeMillis();
            if (!MyApplication.getInstance().isAisConnected) {
                MyApplication.getInstance().isAisConnected = true;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.getInstance().mainActivity.gpsBar.setAisStatus(true);
                        play("AIS已连接");
                    }
                });
            }
            // ! 号头
//            int len = aisByts.size();
//            if (len > 0) {
//                //int len = aisByts.size();
//                // \r\n 结尾
//                Byte[] byts = aisByts.toArray(new Byte[len]);
//                byte[] tmpByts = new byte[len];
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < len; i++) {
//                    tmpByts[i] = byts[i];
//                }
//                sb.append(ConvertUtil.bytesToHexString(tmpByts));
//                String gpsDataStr = new String(tmpByts);
//
//            }
            aisByts.clear();
            aisByts.add(buffer[0]);
        } else {
            aisByts.add(buffer[0]);
            int len = aisByts.size();
            if (len > 5 && aisByts.get(len - 2) == 13 && aisByts.get(len - 1) == 10) {
                // \r\n 结尾
                Byte[] byts = aisByts.toArray(new Byte[len]);
                byte[] tmpByts = new byte[len];
                for (int i = 0; i < len; i++) {
                    tmpByts[i] = byts[i];
                }
                String gpsDataStr = new String(tmpByts);
                tv3.setText(tv3.getText() + gpsDataStr);
                if (gpsDataStr.startsWith("$04")) {
                    String[] messageStrings = MessageFormat.unFormat(tmpByts);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    String type = messageStrings[2];
                    int group = Integer.parseInt(messageStrings[3]);
                    int frameCount = Integer.parseInt(messageStrings[4]);
                    final String unique = ConvertUtil.rc4ToHex();
                    if (MessageFormat.MESSAGE_TYPE_TRADE.equals(type)) {
                        MyApplication.getInstance().sendBytes(MessageFormat.format(MO_GU_TOU// 蘑菇头编号
                                , content, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
                    }
                } else {
                    headIndex.clear();
                    gpsDataStr = preRestStr + gpsDataStr;
                    len = gpsDataStr.length();
                    if (len <= 6) {
                        preRestStr = gpsDataStr;
                        return;
                    }
                    for (int i = 0; i < len - 6; i++) {
                        String headStr = gpsDataStr.substring(i, i + 6);
                        if ("!AIVDM".equals(headStr)
                                || "!AIVDO".equals(headStr)
                                || "$GPGSV".equals(headStr)) {
                            int end = gpsDataStr.indexOf("\n", i + 1);
                            if (end != -1) {
                                String str = gpsDataStr.substring(i + 7, end + 1);
                                if (str.contains("$") || str.contains("!")) {
                                    continue;
                                }
                                //我要的头
                                Map<String, Object> map = new HashMap<>();
                                map.put("type", headStr);
                                map.put("index", i);
                                headIndex.add(map);
                            } else {
                                preRestStr = gpsDataStr.substring(i);
                            }
                        } else {
                            if (len - i < 6) {
                                preRestStr = gpsDataStr.substring(i + 1);
                            }
                        }
                    }
                    for (int i = 0; i < headIndex.size(); i++) {
                        Map<String, Object> map = headIndex.get(i);
                        int end = gpsDataStr.indexOf("\n", (Integer) map.get("index") + 1);
                        if (end != -1) {
                            String newStr = gpsDataStr.substring((Integer) map.get("index"), end + 1);
                            preRestStr = "";
                            String type = (String) map.get("type");
                            if ("!AIVDM".equals(type)
                                    || "!AIVDO".equals(type)) {
                                Log.e("TAG", "ais: " + newStr);
                                try {

                                    boolean isOwn = "!AIVDO".equals(type);
                                    int result = vdm.add(newStr);
                                    boolean isMsg5 = false;
                                    if (0 == result) {
                                        AisInfo aisInfo = new AisInfo("null");
                                        Sixbit sixbit = vdm.sixbit();
                                        Log.e("TAG", "msg: " + vdm.msgid());
                                        switch (vdm.msgid()) {
                                            case 1:
                                                Message1 message1 = new Message1();
                                                message1.parse(sixbit);
                                                aisInfo.mmsi = (int) message1.userid();
                                                aisInfo.COG = message1.cog() / 10.0f;
                                                aisInfo.SOG = message1.sog() / 10.0f;
                                                aisInfo.MsgType = 1;
                                                aisInfo.longtitude = (int) (message1.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message1.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 2:
                                                Message2 message2 = new Message2();
                                                message2.parse(sixbit);
                                                aisInfo.mmsi = (int) message2.userid();
                                                aisInfo.COG = message2.cog() / 10.0f;
                                                aisInfo.SOG = message2.sog() / 10.0f;
                                                aisInfo.MsgType = 2;
                                                aisInfo.longtitude = (int) (message2.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message2.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 3:
                                                Message3 message3 = new Message3();
                                                message3.parse(sixbit);
                                                aisInfo.mmsi = (int) message3.userid();
                                                aisInfo.COG = message3.cog() / 10.0f;
                                                aisInfo.SOG = message3.sog() / 10.0f;
                                                aisInfo.MsgType = 3;
                                                aisInfo.longtitude = (int) (message3.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message3.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 4:
                                                Message4 message4 = new Message4();
                                                message4.parse(sixbit);
                                                aisInfo.mmsi = (int) message4.userid();
                                                aisInfo.MsgType = 4;
                                                aisInfo.longtitude = (int) (message4.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message4.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 11:
                                                Message11 message11 = new Message11();
                                                message11.parse(sixbit);
                                                aisInfo.mmsi = (int) message11.userid();
                                                aisInfo.MsgType = 11;
                                                aisInfo.longtitude = (int) (message11.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message11.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 5:
                                                isMsg5 = true;
                                                Message5 message5 = new Message5();
                                                message5.parse(sixbit);
                                                if (MyApplication.osbDataList != null && !MyApplication.osbDataList.isEmpty()) {
                                                    for (OtherShipBean osb : MyApplication.osbDataList) {
                                                        if (osb.getMmsi() == message5.userid()) {
                                                            osb.setShip_name(message5.name().replace("@", ""));
                                                            osb.setCallsign(message5.callsign());
                                                            osb.setWidth(message5.dim_port() + message5.dim_starboard());
                                                            osb.setLenght(message5.dim_bow() + message5.dim_stern());
                                                        }
                                                    }
                                                    EventBus.getDefault().post("openShip2");
                                                }
                                                break;
                                            case 24:
                                                isMsg5 = true;
                                                Message24 Message24 = new Message24();
                                                Message24.parse(sixbit);
                                                if (MyApplication.osbDataList != null && !MyApplication.osbDataList.isEmpty()) {
                                                    for (OtherShipBean osb : MyApplication.osbDataList) {
                                                        if (osb.getMmsi() == Message24.userid()) {
                                                            osb.setShip_name(Message24.name());
                                                            osb.setCallsign(Message24.callsign());
                                                            osb.setWidth(Message24.dim_port() + Message24.dim_starboard());
                                                            osb.setLenght(Message24.dim_bow() + Message24.dim_stern());
                                                        }
                                                    }
                                                    EventBus.getDefault().post("openShip2");
                                                }
                                                break;
                                            case 14:
                                                Message14 message14 = new Message14();
                                                message14.parse(sixbit);
                                                String message = message14.message();
                                                if (TextUtils.isEmpty(message)) {
                                                    message = "AIS报警";
                                                }
                                                // 暂时停用14发信息功能
//                                            MyApplication.getInstance().sendBytes(WarnFormat.format("" + message14.userid(), message));
                                                break;
                                            case 18:
                                                aisparser.Message18 message18 = new aisparser.Message18();
                                                message18.parse(sixbit);
                                                aisInfo.mmsi = (int) message18.userid();
                                                aisInfo.COG = message18.cog() / 10.0f;
                                                aisInfo.SOG = message18.sog() / 10.0f;
                                                aisInfo.MsgType = 18;
                                                aisInfo.longtitude = (int) (message18.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message18.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 19:
                                                aisparser.Message19 message19 = new aisparser.Message19();
                                                message19.parse(sixbit);
                                                aisInfo.shipName = message19.name().replace("@", "");
                                                aisInfo.mmsi = (int) message19.userid();
                                                aisInfo.COG = message19.cog() / 10.0f;
                                                aisInfo.SOG = message19.sog() / 10.0f;
                                                aisInfo.MsgType = 19;
                                                aisInfo.longtitude = (int) (message19.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message19.latitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.Width = message19.dim_port() + message19.dim_starboard();
                                                aisInfo.Length = message19.dim_bow() + message19.dim_stern();
                                                aisInfo.ShipType = message19.ship_type();
                                                break;
                                        }
                                        if (isMsg5) continue;
                                        int mmsi = Integer.valueOf(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "shipNo", "0")).intValue();
                                        if (aisInfo.mmsi == -1) {
                                            continue;
                                        }
                                        if (isOwn) {
                                            // 本船
                                            judge18(newStr, aisInfo);
                                        } else {
                                            // 其他船
                                            if (mmsi == aisInfo.mmsi) {
                                                judge18(newStr, aisInfo);
                                            } else {
                                                EventBus.getDefault().post(aisInfo);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if ("$GPGSV".equals(type)) {
                                Log.e("TAG", "gps: " + newStr);
                                try {
                                    newStr = newStr.substring(newStr.indexOf(",") + 1, newStr.lastIndexOf("*"));
                                    boolean isDou = newStr.endsWith(",");
                                    if (isDou) {
                                        newStr += "0,";
                                    }
                                    String[] arr = newStr.split(",");
                                    for (int j = 3; j < arr.length; j += 4) {
                                        int no = Integer.valueOf(arr[j]);
                                        int yangjiao = Integer.valueOf(arr[j + 1]);
                                        int fangwei = Integer.valueOf(arr[j + 2]);
                                        int xinhao = 0;
                                        xinhao = Integer.valueOf("".equals(arr[j + 3]) ? "0" : arr[j + 3]);

                                        GPSBean bean = db.selector(GPSBean.class).where("no", "=", no).findFirst();
                                        if (bean == null) {
                                            // 不存在
                                            bean = new GPSBean();
                                            bean.setNo(no);
                                            bean.setYangjiao(yangjiao);
                                            bean.setFangwei(fangwei);
                                            bean.setXinhao(xinhao);
                                            db.saveBindingId(bean);
                                        } else {
                                            // 存在
                                            bean.setYangjiao(yangjiao);
                                            bean.setFangwei(fangwei);
                                            bean.setXinhao(xinhao);
                                            db.update(bean, "yangjiao", "fangwei", "xinhao");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            preRestStr = gpsDataStr.substring((Integer) map.get("index"));
                            // Log.e("TAG", "pre: " + preRestStr);
                        }
                    }
                }
                aisByts.clear();
            }
        }
        if (aisByts.size() > 1024) {
            aisByts.clear();
        }
    }

    private void judge18(String newStr, AisInfo aisInfo) {
        if (18 == aisInfo.MsgType
                || 19 == aisInfo.MsgType) {
            try {
                List<org.codice.common.ais.message.Message> list = new Decoder().parseString(newStr);
                if (list != null && !list.isEmpty()) {
                    for (org.codice.common.ais.message.Message m : list) {
                        if (18 == m.getMessageType()) {
                            Message18 m18 = (Message18) m;
                            LocationBean locationBean = new LocationBean();
                            locationBean.setLatitude((int) (m18.getLat() * 1e7));
                            locationBean.setLongitude((int) (m18.getLon() * 1e7));
                            locationBean.setSpeed((float) m18.getSog());
                            locationBean.setHeading((float) m18.getTrueHeading());
                            locationBean.setAcqtime(Constant.SYSTEM_DATE);
                            MyApplication.getInstance().currentLocation = locationBean;
                            EventBus.getDefault().post(locationBean);
                        } else if (19 == m.getMessageType()) {
                            Message19 m19 = (Message19) m;
                            LocationBean locationBean = new LocationBean();
                            locationBean.setLatitude((int) (m19.getLat() * 1e7));
                            locationBean.setLongitude((int) (m19.getLon() * 1e7));
                            locationBean.setSpeed((float) m19.getSog());
                            locationBean.setHeading((float) m19.getTrueHeading());
                            locationBean.setAcqtime(Constant.SYSTEM_DATE);
                            MyApplication.getInstance().currentLocation = locationBean;
                            EventBus.getDefault().post(locationBean);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocationBean locationBean = new LocationBean();
            locationBean.setLatitude(aisInfo.latititude);
            locationBean.setLongitude(aisInfo.longtitude);
            locationBean.setSpeed(aisInfo.SOG);
            locationBean.setHeading(aisInfo.COG);
            locationBean.setAcqtime(Constant.SYSTEM_DATE);
            MyApplication.getInstance().currentLocation = locationBean;
            EventBus.getDefault().post(locationBean);
        }
    }
}
