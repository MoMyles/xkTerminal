package com.cetcme.xkterminal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.BackBar;
import com.cetcme.xkterminal.ActionBar.BottomBar;
import com.cetcme.xkterminal.ActionBar.GPSBar;
import com.cetcme.xkterminal.ActionBar.MessageBar;
import com.cetcme.xkterminal.ActionBar.MessageDetailBar;
import com.cetcme.xkterminal.ActionBar.PageBar;
import com.cetcme.xkterminal.ActionBar.SendBar;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.Fragment.AboutFragment;
import com.cetcme.xkterminal.Fragment.LogFragment;
import com.cetcme.xkterminal.Fragment.MainFragment;
import com.cetcme.xkterminal.Fragment.MessageFragment;
import com.cetcme.xkterminal.Fragment.MessageNewFragment;
import com.cetcme.xkterminal.Fragment.SettingTabFragment;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.DensityUtil;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;
import com.cetcme.xkterminal.Socket.SocketServer;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.cetcme.xkterminal.Sqlite.Proxy.AlertProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.SignProxy;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qiuhong.qhlibrary.Dialog.QHDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.codice.common.ais.Decoder;
import org.codice.common.ais.message.Message18;
import org.codice.common.ais.message.Message19;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import yimamapapi.skia.AisInfo;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    public static String myNumber = "";

    private DbManager db;

    public GPSBar gpsBar;

    public BottomBar bottomBar;
    public MessageBar messageBar;
    public PageBar pageBar;
    public BackBar backBar;
    public SendBar sendBar;
    public MessageDetailBar messageDetailBar;

    private Fragment currentFragment;
    public MainFragment mainFragment;
    public MessageFragment messageFragment;
    private LogFragment logFragment;
    private SettingTabFragment settingFragment;
    private AboutFragment aboutFragment;
    public MessageNewFragment messageNewFragment;

    public String fragmentName = "main";

    public String backButtonStatus = "backToMain";
    public String messageListStatus = "";

    //按2次返回退出
    private boolean hasPressedBackOnce = false;
    //back toast
    private Toast backToast;

    public KProgressHUD kProgressHUD;
    public KProgressHUD okHUD;

    public WifiManager mWifiManager;

    public boolean messageSendFailed = true;
    private int failedMessageId = 0;

    private SerialPort aisSerialPort = null;
    private InputStream aisInputStream;
    private OutputStream aisOutputStream;

    private Handler mHandler = null;

    private boolean is$04 = false;

    private Timer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置当前窗体为全屏显示
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);
        //隐藏动作条
        getSupportActionBar().hide();

        mHandler = new Handler();

        db = ((MyApplication) getApplication()).db;

        ((MyApplication) getApplication()).mainActivity = this;

        bindView();

        initSpeech();

        initMainFragment();
        initHud();

        myNumber = PreferencesUtils.getString(this, "myNumber");
        if (myNumber == null || myNumber.isEmpty()) {
            myNumber = "000000";
            PreferencesUtils.putString(this, "myNumber", myNumber);
        }

        modifyGpsBarMessageCount();

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        createWifiHotspot();

        checkoutShutDown();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (System.currentTimeMillis() - MyApplication.getInstance().oldAisReceiveTime > 10 * 1000) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (gpsBar != null) {
                                    gpsBar.setAisStatus(false);
                                    MyApplication.getInstance().isAisConnected = false;
                                }
                            }
                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // 设备自检中
        showSelfCheckHud();


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dismissSelfCheckHud();
                if (!MyApplication.getInstance().isLocated) {
                    Looper.prepare();
                    Toast.makeText(MyApplication.getInstance().getApplicationContext(), "自检失败", Toast.LENGTH_SHORT).show();
                    MainActivity.play("卫星中断故障");
                    Looper.loop();
                }
            }
        }, Constant.SELF_CHECK_TIME_OUT);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (isSelfCheckLoading) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    aisSerialPort = MyApplication.getInstance().getAisSerialPort();
//                    aisOutputStream = aisSerialPort.getOutputStream();
//                    aisInputStream = aisSerialPort.getInputStream();
//                    AisReadThread aisReadThread = new AisReadThread();
//                    aisReadThread.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();

        // 发送启动$01，要求对方发时间
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBootData();
            }
        }, 2000);

        // 开启手机客户端socket服务
        new Thread() {
            @Override
            public void run() {
                new SocketServer().startService(MainActivity.this);
            }
        }.start();

        toast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
        // gps定位
        methodRequiresTwoPermission();

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    final static int RC_CAMERA_AND_LOCATION = 0x01;

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            new GPSLocation(MainActivity.this);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "camera_and_location_rationale",
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    class AisReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1];
                    if (aisInputStream == null) return;
                    size = aisInputStream.read(buffer);
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
                runOnUiThread(new Runnable() {
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
                                        int mmsi = Integer.valueOf(PreferencesUtils.getString(getApplicationContext(), "shipNo", "0")).intValue();
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

    /**
     * 初始化语音
     */
    private void initSpeech() {
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
    }

    // 语音合成对象
    private static SpeechSynthesizer mTts;

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("TAG", "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(MyApplication.getInstance(), "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
//                play("我打开了海图导航");
            }
        }
    };

    /**
     * 参数设置
     *
     * @return
     */
    private static void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    //获取发音人资源路径
    private static String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(MyApplication.getInstance(), ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(MyApplication.getInstance(), ResourceUtil.RESOURCE_TYPE.assets, "tts/xiaoyan.jet"));
        return tempBuffer.toString();
    }

    public static void play(String text) {
        if (mTts == null) return;
        setParam();
        int code = mTts.startSpeaking(text, new SynthesizerListener() {

            @Override
            public void onSpeakBegin() {
                //showTip("开始播放");
            }

            @Override
            public void onSpeakPaused() {
                //showTip("暂停播放");
            }

            @Override
            public void onSpeakResumed() {
                //showTip("继续播放");
            }

            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos,
                                         String info) {
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {

            }

            @Override
            public void onCompleted(SpeechError error) {
                if (error == null) {
                    //showTip("播放完成");
                } else if (error != null) {
                    //showTip(error.getPlainDescription(true));
                }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                // 若使用本地能力，会话id为null
                //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                //		Log.d(TAG, "session id =" + sid);
                //	}
            }
        });

        if (code != ErrorCode.SUCCESS) {
            //showTip("语音合成失败,错误码: " + code);
        }
    }

    public static void cancel() {
        if (mTts == null) return;
        mTts.stopSpeaking();
    }

    public static void stop() {
        if (mTts == null) return;
        mTts.pauseSpeaking();
    }

    public static void resume() {
        if (mTts == null) return;
        mTts.resumeSpeaking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }

        if (aisInputStream != null) {
            try {
                aisInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (aisSerialPort != null) {
            aisSerialPort.close();
        }
    }

    private void initHud() {

        int hudWidth = DensityUtil.getScreenHeight(getApplicationContext(), this) / 4;
        if (hudWidth < 110) hudWidth = 110;

        //hudView
        kProgressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("加载中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(hudWidth, hudWidth)
                .setCancellable(false);
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.checkmark);
        okHUD = KProgressHUD.create(this)
                .setCustomView(imageView)
                .setLabel("加载成功")
                .setCancellable(false)
                .setSize(hudWidth, hudWidth)
                .setDimAmount(0.3f);
    }

    // 收到短信
    public int addMessage(final String address, final String content, final boolean read) {
        MessageBean message = new MessageBean();
        message.setSender(address);
        message.setReceiver(myNumber);
        message.setContent(content);
        message.setSend_time(Constant.SYSTEM_DATE);
        message.setRead(read);
        message.setDeleted(false);
        message.setSend(false);
        message.setSendOK(true);
        MessageProxy.insert(db, message);

        // 短信推送
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("apiType", "sms_push");
            sendJson.put("userAddress", "sms_push");
            sendJson.put("data", message.toJson());

            SocketServer.send(sendJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (fragmentName.equals("message") && messageFragment.tg.equals("receive")) {
            messageFragment.reloadDate();
        }
        return message.getId();
    }

    public void addSignLog(String id, String name, Date date) {
        SignProxy.insert(db, id, name, date, false);
    }

    public void addAlertLog(String type) {
        AlertProxy.insert(db, type, Constant.SYSTEM_DATE, false);
    }

    public void addFriend(String name, String number) {
        FriendProxy.insert(db, name, number);
    }

    public void addGroup(String name, String number) {
        GroupProxy.insert(db, name, Integer.parseInt(number));
    }

    public void deleteGroup(String number) {
        GroupProxy.deleteByNumber(db, number);
    }

    public void showIDCardDialog(String id, String name, String nation, String address, Date date) {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("sex", Util.idCardGetSex(id));
        bundle.putString("nation", nation);
        bundle.putString("birthday", Util.idCardGetBirthday(id));
        bundle.putString("address", address);
        bundle.putString("idCard", id);

        if (!idCardDialogOpen) {
            Intent intent = new Intent(MainActivity.this, IDCardActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            idCardDialogOpen = true;
        } else {
            ((MyApplication) getApplication()).idCardActivity.setData(bundle);
        }

        addSignLog(id, name, date);
    }

    public static boolean idCardDialogOpen = false;

    public void showDangerDialog() {
        SoundPlay.startAlertSound(MainActivity.this);

        Intent intent = new Intent(MainActivity.this, AlertActivity.class);
        startActivity(intent);
    }

    public void showMessageDialog(String address, String content, int type) {
        int id = addMessage(address, content, false);

        if (MyApplication.getInstance().messageDialogActivity == null) {
            // type 0: 救护, 1: 报警提醒, 2: 夜间点名
            Intent intent = new Intent(MainActivity.this, MessageDialogActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("type", type);
            intent.putExtra("id", id);
            startActivity(intent);
        } else {
            MyApplication.getInstance().messageDialogActivity.updateMessage(id, content, type);
        }
    }

    public void onBackPressed() {

        if (backBar.getVisibility() == View.VISIBLE) {
            backBar.button_back.performClick();
            return;
        }

        if (messageBar.getVisibility() == View.VISIBLE) {
            messageBar.button_back.performClick();
            return;
        }

        if (pageBar.getVisibility() == View.VISIBLE) {
            pageBar.button_back.performClick();
            return;
        }

        if (sendBar.getVisibility() == View.VISIBLE) {
            sendBar.button_back.performClick();
            return;
        }

        if (messageDetailBar.getVisibility() == View.VISIBLE) {
            messageDetailBar.button_back.performClick();
            return;
        }

        if (!hasPressedBackOnce) {
            backToast = Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT);
            backToast.show();
            hasPressedBackOnce = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasPressedBackOnce = false;
                }
            }, 2500);
        } else {
            backToast.cancel();
            super.onBackPressed();
        }
    }

    private void bindView() {
        gpsBar = findViewById(R.id.gpsBar);

        bottomBar = findViewById(R.id.bottomBar);
        messageBar = findViewById(R.id.messageBar);
        pageBar = findViewById(R.id.pageBar);
        backBar = findViewById(R.id.backBar);
        sendBar = findViewById(R.id.sendBar);
        messageDetailBar = findViewById(R.id.messageDetailBar);

        gpsBar.mainActivity = this;
        bottomBar.mainActivity = this;
        messageBar.mainActivity = this;
        pageBar.mainActivity = this;
        backBar.mainActivity = this;
        sendBar.mainActivity = this;
        messageDetailBar.mainActivity = this;

    }

    private void showMainBar() {
        bottomBar.setVisibility(View.VISIBLE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
        messageDetailBar.setVisibility(View.GONE);
    }

    private void showMessageBar(String tg) {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.VISIBLE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
        messageDetailBar.setVisibility(View.GONE);
        if (tg != null) {
            if (tg.equals("send")) {
                messageBar.setNewBtnVisible(true);
            } else {
                messageBar.setNewBtnVisible(false);
            }
        }
    }

    private void showPageBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.VISIBLE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
        messageDetailBar.setVisibility(View.GONE);
    }

    private void showBackBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.VISIBLE);
        sendBar.setVisibility(View.GONE);
        messageDetailBar.setVisibility(View.GONE);
    }

    public void showSendBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.VISIBLE);
        messageDetailBar.setVisibility(View.GONE);
    }

    private void showMessageDetailBar(String messageListStatus) {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
        messageDetailBar.setVisibility(View.VISIBLE);
        messageDetailBar.setStatus(messageListStatus);
    }

    public void initMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }
        if (currentFragment == mainFragment) {
            return;
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!mainFragment.isAdded()) {
            transaction.add(R.id.main_frame_layout, mainFragment).commit();
        } else {
            transaction.show(mainFragment).commit();
        }
        currentFragment = mainFragment;
        showMainBar();
        fragmentName = "main";
        messageReceiver = "";
        messageContent = "";
        messageTime = "";
        messageId = -1;
        messageIndex = -1;
    }

    public void initMessageFragment(String tg) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (messageFragment == null) {
            messageFragment = new MessageFragment(tg);
        } else {
            messageFragment.setTg(tg);
        }
        showMessageBar(tg);

        if (currentFragment == messageFragment) {
            return;
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!messageFragment.isAdded()) {
            transaction.add(R.id.main_frame_layout, messageFragment).commit();
        } else {
            transaction.show(messageFragment).commit();
        }
        currentFragment = messageFragment;
        messageFragment.mainActivity = this;
        fragmentName = "message";
        messageListStatus = tg;
    }

    public void initLogFragment(String tg) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (logFragment == null) {
            logFragment = new LogFragment(tg);
        }
        logFragment.setTg(tg);
        if (currentFragment == logFragment) {
            return;
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!logFragment.isAdded()) {
            transaction.add(R.id.main_frame_layout, logFragment).commit();
        } else {
            transaction.show(logFragment).commit();
        }
        currentFragment = logFragment;

        if (tg.equals("inout")) {
            showMessageBar("send");
            messageBar.isInout = true;
        } else {
            showPageBar();
        }
        fragmentName = "log";
    }

    public void initSettingFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (settingFragment == null) {
            settingFragment = new SettingTabFragment();
        }
        if (currentFragment == settingFragment) {
            return;
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!settingFragment.isAdded()) {
            transaction.add(R.id.main_frame_layout, settingFragment).commit();
        } else {
            transaction.show(settingFragment).commit();
        }
        currentFragment = settingFragment;
        showBackBar();
        fragmentName = "setting";
    }

    public void initAboutFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (aboutFragment == null) {
            aboutFragment = new AboutFragment();
        }
        if (currentFragment == aboutFragment) {
            return;
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!aboutFragment.isAdded()) {
            transaction.add(R.id.main_frame_layout, aboutFragment).commit();
        } else {
            transaction.show(aboutFragment).commit();
        }
        currentFragment = aboutFragment;
        showBackBar();
        fragmentName = "about";
    }

    public String messageReceiver = "";
    public String messageContent = "";
    public String messageTime = "";
    public int messageId = -1;
    public int messageIndex = -1;

    public void initNewFragment(String tg) {
        if (!tg.equals("new") && (messageContent.isEmpty())) {
            Toast.makeText(this, "请选择一条短信", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (logFragment == null){
        messageNewFragment = new MessageNewFragment(tg, messageReceiver, messageContent, messageTime, messageId);
        messageNewFragment.mainActivity = this;
//        }
//        transaction.replace(R.id.main_frame_layout, messageNewFragment);
//        transaction.commit();
        transaction.add(R.id.main_frame_layout, messageNewFragment);
        transaction.show(messageNewFragment);
        transaction.hide(messageFragment);
        transaction.commit();

        if (tg.equals("detail")) {
            messageFragment.setMessageRead(messageIndex);
            showMessageDetailBar(messageListStatus);
            backButtonStatus = "backToMessageList";
        } else {
            showSendBar();
        }

        fragmentName = "new";
    }

    public void backToMessageFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(messageNewFragment);
        messageNewFragment = null;
        transaction.show(messageFragment);
        transaction.commit();

        showMessageBar(null);
        fragmentName = "message";
        backButtonStatus = "backToMain";

        if (messageFragment.tg.equals("send")) {
            messageFragment.reloadDate();
        }

        if (messageFragment != null) {
            messageFragment.setTg(messageFragment.tg);
        }
    }

    public void nextPage() {
        if (fragmentName.equals("message")) {
            messageFragment.nextPage();
        } else if (fragmentName.equals("log")) {
            logFragment.nextPage();
        }
    }

    public void prevPage() {
        if (fragmentName.equals("message")) {
            messageFragment.prevPage();
        } else if (fragmentName.equals("log")) {
            logFragment.prevPage();
        }
    }

    // 分包发送短信
    public void sendMessage() {

        SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
        String lastSendTime = sharedPreferences.getString("lastSendTime", "");
        if (!lastSendTime.isEmpty()) {
            Long sendDate = DateUtil.parseStringToDate(lastSendTime, DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
            Long now = Constant.SYSTEM_DATE.getTime();
            if (now - sendDate <= Constant.MESSAGE_SEND_LIMIT_TIME && now - sendDate > 0) {
                long remainSecond = (Constant.MESSAGE_SEND_LIMIT_TIME - (now - sendDate)) / 1000;
                Toast.makeText(this, "发送时间间隔不到1分钟，请等待" + remainSecond + "秒", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final String receiver = messageNewFragment.getReceiver();
        final String content = messageNewFragment.getContent();

        if (receiver.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this, "提示", "收件人为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        if (!CommonUtil.isNumber(receiver)) {
            QHDialog qhDialog = new QHDialog(this, "提示", "请正确填写收件人地址");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        if (content.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this, "提示", "短信内容为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        int length = 0;
        try {
            length = content.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (Constant.MESSAGE_CONTENT_MAX_LENGTH != 0 && length > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
            QHDialog qhDialog = new QHDialog(this, "提示", "短信内容长度超出最大值（" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "）！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("lastSendTimeSave", DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS));
        editor.apply(); //提交修改

        final MessageBean newMessage = new MessageBean();
        newMessage.setSender(myNumber);
        newMessage.setReceiver(receiver);
        newMessage.setContent(content);
        newMessage.setDeleted(false);
        newMessage.setSend_time(Constant.SYSTEM_DATE);
        newMessage.setRead(true);
        newMessage.setSend(true);
        newMessage.setSendOK(true);

        MessageProxy.insert(db, newMessage);
        failedMessageId = newMessage.getId();

        if (length > 54) {
            final QMUITipDialog tipDialog = new QMUITipDialog.Builder(MainActivity.this)
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("发送中")
                    .create();
            tipDialog.show();
            final String unique = ConvertUtil.rc4ToHex();
            String firstContent = MessageFormat.shortcutMessage(content);
            final String secondContent = content.replace(firstContent, "");
            byte[] messageBytes = MessageFormat.format(receiver, firstContent, receiver.length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL, 1, unique);
            ((MyApplication) getApplication()).sendBytes(messageBytes);
            System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    byte[] messageBytes = MessageFormat.format(receiver, secondContent, receiver.length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL, 0, unique);
                    ((MyApplication) getApplication()).sendBytes(messageBytes);
                    System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));
                    tipDialog.dismiss();
                    backToMessageFragment();

                }
            }, 10000);

            // 显示短信发送失败
            messageSendFailed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (messageSendFailed) {
                        Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                        if (failedMessageId != 0) {
                            MessageProxy.setMessageFailed(db, failedMessageId);
                            if (fragmentName.equals("message") && messageFragment.tg.equals("send")) {
                                messageFragment.reloadDate();
                            }
                            failedMessageId = 0;
                        }
                    }
                }
            }, Constant.MESSAGE_FAIL_TIME + 10000);

        } else {
            byte[] messageBytes = MessageFormat.format(receiver, content, receiver.length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL, 0);
            ((MyApplication) getApplication()).sendBytes(messageBytes);
            System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));
            backToMessageFragment();

            // 显示短信发送失败
            messageSendFailed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (messageSendFailed) {
                        Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                        if (failedMessageId != 0) {
                            MessageProxy.setMessageFailed(db, failedMessageId);
                            if (fragmentName.equals("message") && messageFragment.tg.equals("send")) {
                                messageFragment.reloadDate();
                            }
                            failedMessageId = 0;
                        }
                    }
                }
            }, Constant.MESSAGE_FAIL_TIME);
        }

        // 短信推送
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("apiType", "sms_push");
            sendJson.put("userAddress", "sms_push");
            sendJson.put("data", newMessage.toJson());

            SocketServer.send(sendJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 发送短信
    public void sendMessageOld() {

        SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
        String lastSendTime = sharedPreferences.getString("lastSendTime", "");
        if (!lastSendTime.isEmpty()) {
            Long sendDate = DateUtil.parseStringToDate(lastSendTime, DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
            Long now = Constant.SYSTEM_DATE.getTime();
            if (now - sendDate <= Constant.MESSAGE_SEND_LIMIT_TIME && now - sendDate > 0) {
                long remainSecond = (Constant.MESSAGE_SEND_LIMIT_TIME - (now - sendDate)) / 1000;
                Toast.makeText(this, "发送时间间隔不到1分钟，请等待" + remainSecond + "秒", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final String receiver = messageNewFragment.getReceiver();
        final String content = messageNewFragment.getContent();

        if (receiver.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this, "提示", "收件人为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        if (content.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this, "提示", "短信内容为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        int length = 0;
        try {
            length = content.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (Constant.MESSAGE_CONTENT_MAX_LENGTH != 0 && length > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
            QHDialog qhDialog = new QHDialog(this, "提示", "短信内容长度超出最大值（" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "）！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("lastSendTimeSave", DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS));
        editor.apply(); //提交修改

        final MessageBean newMessage = new MessageBean();
        newMessage.setSender(myNumber);
        newMessage.setReceiver(receiver);
        newMessage.setContent(content);
        newMessage.setDeleted(false);
        newMessage.setSend_time(Constant.SYSTEM_DATE);
        newMessage.setRead(true);
        newMessage.setSend(true);
        newMessage.setSendOK(true);

        MessageProxy.insert(db, newMessage);
        failedMessageId = newMessage.getId();

        byte[] messageBytes = MessageFormat.format(receiver, content, receiver.length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL, 0);
        ((MyApplication) getApplication()).sendBytes(messageBytes);
        System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));

        backToMessageFragment();


        // 显示短信发送失败
        messageSendFailed = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messageSendFailed) {
                    Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    if (failedMessageId != 0) {
                        MessageProxy.setMessageFailed(db, failedMessageId);
                        if (fragmentName.equals("message") && messageFragment.tg.equals("send")) {
                            messageFragment.reloadDate();
                        }
                        failedMessageId = 0;
                    }
                }
            }
        }, Constant.MESSAGE_FAIL_TIME);

        // 短信推送
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("apiType", "sms_push");
            sendJson.put("userAddress", "sms_push");
            sendJson.put("data", newMessage.toJson());
            SocketServer.send(sendJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void modifyGpsBarMessageCount() {
        long count = MessageProxy.getUnreadMessageCount(db, myNumber);
        gpsBar.modifyMessageCount(count);
    }

    /**
     * 创建Wifi热点
     */
    public void createWifiHotspot() {
        if (mWifiManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            mWifiManager.setWifiEnabled(false);
        }
        closeWifiHotspot();
        WifiConfiguration config = new WifiConfiguration();

        String ssid = PreferencesUtils.getString(MainActivity.this, "wifiSSID");
        if (ssid == null) {
            ssid = getString(R.string.wifi_ssid);
        }

        config.SSID = ssid;
//        config.preSharedKey = "123456789";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(mWifiManager, config, true);
            if (enable) {
                System.out.println("热点已开启 SSID:" + ssid + " password: 无");
            } else {
                System.out.println("创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("创建热点失败");
        }
    }

    /**
     * 关闭WiFi热点
     */
    public void closeWifiHotspot() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
            Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(mWifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void sendBootData() {
        byte[] bytes = "$01".getBytes();
        bytes = ByteUtil.byteMerger(bytes, new byte[]{0x01, 0x00});
        bytes = ByteUtil.byteMerger(bytes, new byte[]{0x2A, 0x01});
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());
        ((MyApplication) getApplication()).sendBytes(bytes);
    }

    public void showShutDownHud() {
        kProgressHUD.setLabel("关机中");
        kProgressHUD.show();
    }

    public boolean isSelfCheckLoading = false;

    public void showSelfCheckHud() {
        kProgressHUD.setLabel("自检中");
        kProgressHUD.show();
        isSelfCheckLoading = true;
    }

    public void dismissSelfCheckHud() {
        if (kProgressHUD != null && kProgressHUD.isShowing()) {
            kProgressHUD.dismiss();
        }
        isSelfCheckLoading = false;
    }

    public void showAlertFailDialog() {
        new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                .setTitle("提示")
                .setMessage("报警失败!")
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void openOtherShips() {
        EventBus.getDefault().post("openShip");
    }

    private void checkoutShutDown() {
        boolean shutdown = PreferencesUtils.getBoolean(MainActivity.this, "shutdown");
        if (shutdown) {
            new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("遥毙功能已启动, app不可再使用!")
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            System.exit(0);
                        }
                    })
                    .show();
        }
    }

    public void openPinList() {
        startActivityForResult(new Intent(this, PinActivity.class), REQUEST_CODE_PIN_ACTIVITY);
    }

    private static final int REQUEST_CODE_PIN_ACTIVITY = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_PIN_ACTIVITY:
                switch (resultCode) {
                    case PinActivity.RESULT_CODE_MAP:
                        // MainFragment里接收
                        EventBus.getDefault().post("pin_map");
                        break;
                    case PinActivity.RESULT_CODE_CO:
                        // MainFragment里接收
                        EventBus.getDefault().post("pin_co");
                        break;
                    case PinActivity.RESULT_OUT_OF_LIMIT:
                        showInoutOutOfLimitDialog();
                        break;
                }
                break;
        }
    }

    private void showInoutOutOfLimitDialog() {
        new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                .setTitle("提示")
                .setMessage("已达到标位最大数量(" + Constant.LIMIT_PIN + ")，请删除后再添加。")
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void showPhoneLoginDialog() {
        new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                .setTitle("提示")
                .setMessage("是否允许手机客户端登陆？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        SocketServer.denyLogin();
                        dialog.dismiss();
                    }
                })
                .addAction(0, "允许", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        SocketServer.allowLogin();
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("apiType", "login");
                            EventBus.getDefault().post(new SmsEvent(jsonObject));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void sendCheckAndMapMessage() {
        String deviceID = SkiaDrawView.mYimaLib.GetDeviceIDForLicSvr();
        final String unique = ConvertUtil.rc4ToHex();
        MyApplication.getInstance().sendBytes(MessageFormat.format(MO_GU_TOU, deviceID, MessageFormat.MESSAGE_TYPE_CHECK_AND_MAP, 0, unique));
    }

    public static final String MO_GU_TOU = "382570";

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
        new GPSLocation(MainActivity.this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }

    Toast toast;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(Location location) {
        toast.setText(location.getLatitude() + ", " + location.getLongitude());
        toast.show();
    }

}
