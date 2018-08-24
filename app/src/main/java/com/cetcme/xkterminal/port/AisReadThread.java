package com.cetcme.xkterminal.port;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.ftdi.j2xx.FT_Device;

import org.codice.common.ais.Decoder;
import org.codice.common.ais.message.Message18;
import org.codice.common.ais.message.Message19;
import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;

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
import yimamapapi.skia.AisInfo;

import static com.cetcme.xkterminal.MainActivity.play;

public class AisReadThread extends Thread {

    private static final int readLength = 512;
    private final byte[] readData = new byte[readLength];
    private final List<Byte> aisByts = new LinkedList<>();
    private String preRestStr = "";
    private final List<Map<String, Object>> headIndex = new ArrayList<>();
    private final Vdm vdm = new Vdm();

    private String path;
    private DbManager db;
    private FT_Device ftDevice;
    private Handler handler;

    public AisReadThread(String path, FT_Device ftDevice, Handler handler) {
        this.path = path;
        this.ftDevice = ftDevice;
        this.handler = handler;
        db = MyApplication.getInstance().getDb();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (ftDevice == null) continue;
            int iavailable = ftDevice.getQueueStatus();
            if (iavailable > 0) {

                if (iavailable > readLength) {
                    iavailable = readLength;
                }

                ftDevice.read(readData, iavailable);

                byte[] byts = new byte[iavailable];

                for (int i = 0; i < iavailable; i++) {
                    byts[i] = readData[i];
                    formatAis(readData[i]);
                }
                if (USBFragment2.currentPath.equals(path)) {
                    Message message = Message.obtain();
                    message.what = 0x1;
                    message.obj = byts;
                    handler.sendMessage(message);
                }
            }
        }
    }

    private void formatAis(byte b) {
        aisByts.add(b);
        int len = aisByts.size();
        if (len > 5 && len < 1024) {
            Byte[] byts = aisByts.toArray(new Byte[len]);
            byte[] tmpByts = new byte[len];
            for (int i = 0; i < len; i++) {
                tmpByts[i] = byts[i];
            }
            String tmp = new String(tmpByts);
            // 有正确的头
            if (tmp.startsWith("$04")) {
                if (aisByts.get(len - 2) == 13 && aisByts.get(len - 1) == 10) {
                    String[] messageStrings = MessageFormat.unFormat(tmpByts);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    String type = messageStrings[2];
                    int group = Integer.parseInt(messageStrings[3]);
                    int frameCount = Integer.parseInt(messageStrings[4]);
                    final String unique = ConvertUtil.rc4ToHex();
                    if (MessageFormat.MESSAGE_TYPE_TRADE.equals(type)) {
                        MyApplication.getInstance().sendBytes(MessageFormat.format(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "server_address", Constant.SERVER_BD_NUMBER)// 蘑菇头编号
                                , content, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
                    } else if (MessageFormat.MESSAGE_TYPE_BROADCASTING.equals(type)) {
                        MyApplication.getInstance().sendBytes(MessageFormat.format(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "server_address", Constant.SERVER_BD_NUMBER)// 蘑菇头编号
                                , content, MessageFormat.MESSAGE_TYPE_BROADCASTING, 0, unique));
                    }
                }
            } else if (tmp.startsWith("!AIVDO")
                    || tmp.startsWith("!AIVDM")
                    || tmp.startsWith("$GPGSV")) {
                if (aisByts.get(len - 2) == 13 && aisByts.get(len - 1) == 10) {
                    headIndex.clear();
                    tmp = preRestStr + tmp;
                    len = tmp.length();
                    if (len <= 6) {
                        preRestStr = tmp;
                        return;
                    }
                    for (int i = 0; i < len - 6; i++) {
                        String headStr = tmp.substring(i, i + 6);
                        if ("!AIVDM".equals(headStr)
                                || "!AIVDO".equals(headStr)
                                || "$GPGSV".equals(headStr)) {
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
                            int end = tmp.indexOf("\n", i + 1);
                            if (end != -1) {
                                String str = tmp.substring(i + 7, end + 1);
                                if (str.contains("$") || str.contains("!")) {
                                    continue;
                                }
                                //我要的头
                                Map<String, Object> map = new HashMap<>();
                                map.put("type", headStr);
                                map.put("index", i);
                                headIndex.add(map);
                            } else {
                                preRestStr = tmp.substring(i);
                            }
                        } else {
                            if (len - i < 6) {
                                preRestStr = tmp.substring(i + 1);
                            }
                        }
                    }
                    for (int i = 0; i < headIndex.size(); i++) {
                        Map<String, Object> map = headIndex.get(i);
                        int end = tmp.indexOf("\n", (Integer) map.get("index") + 1);
                        if (end != -1) {
                            String newStr = tmp.substring((Integer) map.get("index"), end + 1);
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
                            preRestStr = tmp.substring((Integer) map.get("index"));
                        }
                    }
                }
            } else {
                aisByts.remove(0);
            }
        } else if (len >= 1024) {
            aisByts.clear();
        }
//        if (b == 33 || b == 36) {
//            aisByts.clear();
//            aisByts.add(b);
//        } else {
//            aisByts.add(b);
//            int len = aisByts.size();
//            System.out.print(ByteUtil.byte2Str(b));
//            if (len > 5 && aisByts.get(len - 2) == 13 && aisByts.get(len - 1) == 10) {
//                // \r\n 结尾
//                Byte[] byts = aisByts.toArray(new Byte[len]);
//                byte[] tmpByts = new byte[len];
//                for (int i = 0; i < len; i++) {
//                    tmpByts[i] = byts[i];
//                }
//                String gpsDataStr = new String(tmpByts);
//                Log.e("TAG_AisRead", gpsDataStr);
//                if (gpsDataStr.startsWith("$04")) {
//                    StringBuffer sb = new StringBuffer();
//                    for (byte bb : tmpByts){
//                        sb.append(ByteUtil.byte2Str(bb)+",");
//                    }
//                    String[] messageStrings = MessageFormat.unFormat(tmpByts);
//                    String address = messageStrings[0];
//                    String content = messageStrings[1];
//                    String type = messageStrings[2];
//                    int group = Integer.parseInt(messageStrings[3]);
//                    int frameCount = Integer.parseInt(messageStrings[4]);
//                    final String unique = ConvertUtil.rc4ToHex();
//                    if (MessageFormat.MESSAGE_TYPE_TRADE.equals(type)) {
//                        MyApplication.getInstance().sendBytes(MessageFormat.format(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "server_address", Constant.SERVER_BD_NUMBER)// 蘑菇头编号
//                                , content, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
//                    } else if (MessageFormat.MESSAGE_TYPE_BROADCASTING.equals(type)) {
//                        Log.e("TAG_DIANTAI", "-------------");
//                        MyApplication.getInstance().sendBytes(MessageFormat.format("382570"//PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "server_address", Constant.SERVER_BD_NUMBER)// 蘑菇头编号
//                                , content, MessageFormat.MESSAGE_TYPE_BROADCASTING, 0, unique));
//                    }
//                } else {
//
//                }
//                aisByts.clear();
//            }
//        }
//        if (aisByts.size() > 512) {
//            aisByts.clear();
//        }
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
