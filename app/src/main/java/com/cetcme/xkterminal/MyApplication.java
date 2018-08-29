package com.cetcme.xkterminal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.IDFormat;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Navigation.GpsInfo;
import com.cetcme.xkterminal.Navigation.GpsParse;
import com.cetcme.xkterminal.Socket.SocketManager;
import com.cetcme.xkterminal.Socket.SocketServer;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.cetcme.xkterminal.netty.heartbeats.HeartBeatsClient;
import com.cetcme.xkterminal.netty.utils.Constants;
import com.cetcme.xkterminal.netty.utils.SendMsg;
import com.cetcme.xkterminal.port.AisReadThread;
import com.cetcme.xkterminal.port.USBEvent;
import com.cetcme.xkterminal.port.USBInfo;
import com.cetcme.xkterminal.port.Utils;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import yimamapapi.skia.M_POINT;
import yimamapapi.skia.OtherVesselCurrentInfo;
import yimamapapi.skia.YimaLib;

import static com.cetcme.xkterminal.MainActivity.myNumber;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class MyApplication extends MultiDexApplication {

    public MainActivity mainActivity;
    public MessageDialogActivity messageDialogActivity;
    public IDCardActivity idCardActivity;

    private static MyApplication mContext;
    public static LocationBean currentLocation;
    public DbManager db;

    private DataHandler mHandler;

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    //    private SerialPort gpsSerialPort = null;
//    private OutputStream gpsOutputStream;
//    private InputStream gpsInputStream;
    private SerialPort aisSerialPort = null;
    private OutputStream aisOutputStream;
    private InputStream aisInputStream;

    public boolean messageSendFailed = true;

    // for file pick
    private Handler handler;

    private Toast tipToast;

    private int failedMessageId = 0;

    public long oldAisReceiveTime = System.currentTimeMillis();

    public boolean isAisConnected = false;
    public static boolean isSendThreadStart = false;

    // 是否已定位, fake，收短信不处理， sendByte不处理，导航不处理，申报不处理
    public static boolean isLocated = false;
    // 电压
    public static String voltage = "-";

    public static String powerFrom = "-";

    public static final List<OtherShipBean> osbDataList = new ArrayList<>();

    /**
     * 加载库文件（只需调用一次）
     */
    static {
        YimaLib.LoadLib();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

//        if (!PreferencesUtils.getBoolean(this, "copiedYimaFile")) {
//            copyYimaFile();
//            PreferencesUtils.putBoolean(this, "copiedYimaFile", true);
//        }

        x.Ext.init(this);
//        x.Ext.setDebug(BuildConfig.DEBUG); // 开启debug会影响性能

        EventBus.getDefault().register(this);

        initDb();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        String str = "[" + format.format(Constant.SYSTEM_DATE) + "]" + msg.obj.toString();
                        tipToast.setText(str);
                        tipToast.show();
                        System.out.println(str);
                        break;
                    case 1:
                        System.out.println("本机IP：" + " 监听端口:" + msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        if (mainActivity != null && mainActivity.fragmentName != null && mainActivity.messageFragment != null) {
                            if (mainActivity.fragmentName.equals("message") && mainActivity.messageFragment.tg.equals("send")) {
                                mainActivity.messageFragment.reloadDate();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(getApplicationContext(), "短信发送成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        String str1 = DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS);
                        PreferencesUtils.putString(getApplicationContext(), "lastSendTime", str1);
                        break;
                    case 6:
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Constant.SYSTEM_DATE);
                        calendar.add(Calendar.SECOND, -61);
                        String str2 = DateUtil.parseDateToString(calendar.getTime(), DateUtil.DatePattern.YYYYMMDDHHMMSS);
                        PreferencesUtils.putString(getApplicationContext(), "lastSendTime", str2);
                        break;
                }
            }
        };


        if (!Constant.PHONE_TEST) {
            try {
                mSerialPort = getSerialPort();
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();
                ReadThread mReadThread = new ReadThread();
                mReadThread.start();

//            aisSerialPort = getAisSerialPort();
//            aisOutputStream = aisSerialPort.getOutputStream();
//            aisInputStream = aisSerialPort.getInputStream();
//            AisReadThread aisReadThread = new AisReadThread();
//            aisReadThread.start();
                db.delete(com.cetcme.xkterminal.Sqlite.Bean.Message.class);

            } catch (SecurityException e) {
                DisplayError(R.string.error_security);
            } catch (IOException e) {
                DisplayError(R.string.error_unknown);
            } catch (InvalidParameterException e) {
                DisplayError(R.string.error_configuration);
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new HeartBeatsClient().connect(3349, "61.164.208.174");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //显示所有path
//        String[] paths =  mSerialPortFinder.getAllDevicesPath();
//        for (String path : paths) {
//            Log.i("qh_port", "onCreate: " + path);
//        }

        tipToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        new SocketManager(handler, getApplicationContext());

        mHandler = new DataHandler(this);


        // 语音
        Iconify.with(new FontAwesomeModule());

        StringBuffer param = new StringBuffer();
        param.append("appid=5b3985d5"); // mao: 5afb90f6, qh: 5b2c61e9, lw: 5b2c638f, xb: 5b3985d5
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());
        // 每隔1分钟 移除5分钟未收到AIS信息的渔船
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (osbDataList != null && !osbDataList.isEmpty()) {
                    synchronized (osbDataList) {
                        Iterator<OtherShipBean> it = osbDataList.iterator();
                        while (it.hasNext()) {
                            OtherShipBean o = it.next();
                            if (Constant.SYSTEM_DATE.getTime() - o.getAcq_time().getTime() > (4 * 60 + 59) * 1000) {
                                it.remove();
                            }
                        }
                    }
                }
                new Handler().postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);
        // 距离报警
        warnTimer = new Timer();
        warnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isDangerOfDistance();
            }
        }, 10000, 5 * 60 * 1000);

        //TODO: fake 初始化定位点
        currentLocation = new LocationBean();
        currentLocation.setLongitude(0);
        currentLocation.setLatitude(0);
        currentLocation.setHeading(0.0f);
        currentLocation.setAcqtime(new Date(Constant.SYSTEM_DATE.getTime() - 10 * 60 * 1000));
        currentLocation.setSpeed(0.0f);

        initUSB();
    }

    public void startSendThread() {
        new SendingThread().start();
    }

    public D2xxManager ftdid2xx;
    public final Map<String, USBInfo> openMap = new HashMap<>();
    public Handler usbHandler;

    private void initUSB() {
        try {
            ftdid2xx = D2xxManager.getInstance(this);

            if (ftdid2xx != null) {
                ftdid2xx.createDeviceInfoList(this);
            }
            usbHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    USBEvent usbEvent = new USBEvent();
                    switch (msg.what) {
                        case 0x1:
                            usbEvent.setWhat(0x1);
                            usbEvent.setMessage((byte[]) msg.obj);
                            break;
                        case 0x2:
                            usbEvent.setWhat(0x2);
                            usbEvent.setMessage((byte[]) msg.obj);
                            break;
                    }
                    EventBus.getDefault().post(usbEvent);
                }
            };
            String str = PreferencesUtils.getString(getApplicationContext(), "usb_path", "");
            if (!TextUtils.isEmpty(str)) {
                String[] paths = str.split(",");
                for (int i = 0; i < paths.length; i++) {
                    String[] arr = paths[i].split(":");
                    connectFunction(arr[0], arr[1]);
                }
            }
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }
    }

    public void connectFunction(final String path, final String baudRate) {
        if (openMap.containsKey(path)) return;
        final USBInfo usbInfo = new USBInfo();
        openMap.put(path, usbInfo);
        usbInfo.setPath(path);
        usbInfo.setBaudrate(baudRate);
        int openIndex = Integer.parseInt(path);
        final FT_Device ftDevice = ftdid2xx.openByIndex(this, openIndex - 1);
        if (ftDevice == null) {
            return;
        }
        if (true == ftDevice.isOpen()) {
            usbInfo.setFtDevice(ftDevice);
            usbHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
                    ftDevice.setBaudRate(Integer.parseInt(baudRate));
                    ftDevice.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
                    ftDevice.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b, (byte) 0x0d);
                    AisReadThread aisReadThread = new AisReadThread(path, ftDevice, usbHandler);
                    usbInfo.setReadThread(aisReadThread);
                    aisReadThread.start();
                }
            }, 1000);
            String str = PreferencesUtils.getString(getApplicationContext(), "usb_path", "");
            PreferencesUtils.putString(getApplicationContext(), "usb_path", str + path + ":" + baudRate + ",");
        } else {
            Toast.makeText(this, "打开串口" + path + "失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectFunction(String currentPath) {
        if (!openMap.containsKey(currentPath)) return;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        USBInfo usb = openMap.get(currentPath);
        FT_Device ftDevice = usb.getFtDevice();
        if (ftDevice != null) {
            synchronized (ftDevice) {
                if (true == ftDevice.isOpen()) {
                    ftDevice.close();
                }
            }
        }
        AisReadThread aisReadThread = usb.getReadThread();
        if (aisReadThread != null) {
            aisReadThread.interrupt();
        }
        openMap.remove(currentPath);
        String str = PreferencesUtils.getString(getApplicationContext(), "usb_path", "");
        if (!TextUtils.isEmpty(str)) {
            String[] paths = str.split(",");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < paths.length; i++) {
                String[] arr = paths[i].split(":");
                if (arr[0].equals(currentPath)) continue;
                sb.append(paths[i] + ",");
            }
            PreferencesUtils.putString(getApplicationContext(), "usb_path", sb.toString());
        }
    }

    /**
     * 是否需要距离报警
     *
     * @return
     */
    private void isDangerOfDistance() {
        YimaLib mYimaLib = null;
        if (mainActivity != null && mainActivity.mainFragment != null && mainActivity.mainFragment.skiaDrawView != null) {
            mYimaLib = mainActivity.mainFragment.skiaDrawView.mYimaLib;
        }
        if (mYimaLib == null) return;
        if (PreferencesUtils.getBoolean(this, "warn_switch", false)) {
            LocationBean myself = MyApplication.getInstance().getCurrentLocation();
            if (myself == null) return;
            int distance = PreferencesUtils.getInt(this, "warn_distance", 200);
            double haili = distance * 1.0 / 1852;// 海里
            M_POINT start = mYimaLib.getDesPointOfCrsAndDist(myself.getLongitude()
                    , myself.getLatitude(), haili, myself.getHeading());// 本船报警距离目标点
            try {
                List<OtherShipBean> list = db.selector(OtherShipBean.class).findAll();
                if (list != null && !list.isEmpty()) {
                    for (OtherShipBean osb : list) {
                        int vessel_id = mYimaLib.GetOtherVesselPosOfID(osb.getShip_id());
                        OtherVesselCurrentInfo ovci = mYimaLib.getOtherVesselCurrentInfo(vessel_id);
                        M_POINT end = mYimaLib.getDesPointOfCrsAndDist(ovci.currentPoint.x
                                , ovci.currentPoint.y, haili, ovci.fCourseOverGround);
                        int x_ = myself.getLongitude() - ovci.currentPoint.x;
                        int y_ = myself.getLatitude() - ovci.currentPoint.y;

                        int x_2 = start.x - end.x;
                        int y_2 = start.y - end.y;

                        double curHaili = mYimaLib.GetDistBetwTwoPoint(start.x, start.y, end.x, end.y);
                        if (x_2 < x_ && y_2 < y_ && curHaili <= haili) {
                            mainActivity.showMessageDialog("自身设备", "您即将撞船", 1);
                            SoundPlay.startAlertSound(mainActivity);
                        }
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    private Timer warnTimer;

    public DataHandler getHandler() {
        return mHandler;
    }

    public static MyApplication getInstance() {
        return mContext;
    }

    public DbManager getDb() {
        return db;
    }

    public LocationBean getCurrentLocation() {
        return currentLocation;
    }

    /**
     * 文件复制: 把assets目录下的workDir目录拷贝到data/data/包名/files目录下。（只需调用一次，用户也可以自己实现）
     */
    private void copyYimaFile() {
        String strFile = getApplicationContext().getFilesDir().getAbsolutePath();
        long startTime = System.currentTimeMillis();
        YimaLib.CopyWorkDir(getApplicationContext(), strFile);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Yima WorkDir 文件拷贝: " + String.valueOf(endTime - startTime) + "ms");
//        Toast.makeText(MainActivity.this, "文件拷贝" + String.valueOf(endTime - startTime), Toast.LENGTH_SHORT).show();
    }

    public void initDb() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("xkTerminal.db")
                // 不设置dbDir时, 默认存储在app的私有目录.
                //.setDbDir(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"mswcs/db")) //设置数据库.db文件存放的目录,默认为包名下databases目录下
                .setDbVersion(1)//设置数据库版本,每次启动应用时将会检查该版本号,
                //发现数据库版本低于这里设置的值将进行数据库升级并触发DbUpgradeListener
                .setAllowTransaction(true)//设置是否开启事务,默认为false关闭事务
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setTableCreateListener(new DbManager.TableCreateListener() {
                    @Override
                    public void onTableCreated(DbManager db, TableEntity<?> table) {

                    }
                })//设置数据库创建时的Listener
                //设置数据库升级时的Listener,这里可以执行相关数据库表的相关修改,比如alter语句增加字段等
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // db.addColumn(...);
                        // db.dropTable(...);
                        // ...
                        // or
                        // db.dropDb();
                        if (newVersion > oldVersion) {

                        }
                    }
                });
        db = x.getDb(daoConfig);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(SmsEvent smsEvent) {
        JSONObject receiveJson = smsEvent.getReceiveJson();
        try {
            String apiType = receiveJson.getString("apiType");
            JSONObject jsonObject = new JSONObject();
            switch (apiType) {
                case "check_login":
                    mainActivity.showPhoneLoginDialog();
                    break;
                case "login":
                    if (mainActivity != null) {
                        Toast.makeText(mainActivity, "手机客户端登陆成功", Toast.LENGTH_SHORT).show();
                        MainActivity.play("手机客户端登陆成功");
                        // 服务器app版本检测
                        Log.i(TAG, "Event: 发送更新检测byte");
                        String unique = ConvertUtil.rc4ToHex();
                        sendBytes(MessageFormat.format(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "server_address", Constants.MUSHROOM_ADDRESS), "1", MessageFormat.MESSAGE_TYPE_APP_VERSION, 0, unique));
                    }
                    break;
                case "device_info_set":
                    JSONObject data = receiveJson.getJSONObject("data");
                    String newID = data.getString("deviceID");
                    sendBytes(IDFormat.format(newID));
                    break;
//                case "device_id":
//                    sendBytes(IDFormat.getID());
//                    break;
                case "sms_list":
                    JSONArray smsList = getSmsList();

                    jsonObject.put("apiType", "sms_list");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", smsList);
                    jsonObject.put("userAddress", MainActivity.myNumber);
                    SocketServer.send(jsonObject);

                    break;
                case "sms_detail":
                    String userAddress = receiveJson.getString("userAddress");
                    int countPerPage = receiveJson.getInt("countPerPage");
                    String timeBefore = receiveJson.getString("timeBefore");
                    JSONArray smsDetailStr = getSmsDetail(userAddress, countPerPage, timeBefore);

                    jsonObject.put("apiType", "sms_detail");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", smsDetailStr);
                    SocketServer.send(jsonObject);
                    break;

                case "sms_send":
                    final MessageBean message = new MessageBean();
                    message.fromJson(receiveJson.getJSONObject("data"));

                    SharedPreferences sharedPreferences = getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
                    String lastSendTime = sharedPreferences.getString("lastSendTime", "");
                    if (!lastSendTime.isEmpty()) {
                        Long sendDate = DateUtil.parseStringToDate(lastSendTime, DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
                        Long now = Constant.SYSTEM_DATE.getTime();
                        if (now - sendDate <= Constant.MESSAGE_SEND_LIMIT_TIME) {
                            long remainSecond = (Constant.MESSAGE_SEND_LIMIT_TIME - (now - sendDate)) / 1000;
                            // 返回不成功socket
                            Toast.makeText(this, "发送时间间隔不到1分钟，请等待" + remainSecond + "秒", Toast.LENGTH_SHORT).show();

                            JSONObject sendJson = new JSONObject();
                            try {
                                sendJson.put("apiType", "sms_send");
                                sendJson.put("code", 1);
                                sendJson.put("msg", "发送时间间隔不到1分钟，请等待" + remainSecond + "秒");

                                SocketServer.send(sendJson);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }

                    int length = 0;
                    try {
                        length = message.getContent().getBytes("GBK").length;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (Constant.MESSAGE_CONTENT_MAX_LENGTH != 0 && length > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
                        // 返回不成功socket

                        JSONObject sendJson = new JSONObject();
                        try {
                            sendJson.put("apiType", "sms_send");
                            sendJson.put("code", 2);
                            sendJson.put("msg", "内容长度:" + length + ",超出最大值" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "!");

                            SocketServer.send(sendJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

//                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
//                    editor.putString("lastSendTimeSave", DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS));
//                    editor.apply(); //提交修改

                    MessageProxy.insert(db, message);
                    failedMessageId = message.getId();

                    byte[] messageBytes = MessageFormat.format(message.getReceiver(), message.getContent(), message.getReceiver().length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL, 0);
                    sendBytes(messageBytes);
                    System.out.println("发送短信： " + ConvertUtil.bytesToHexString(messageBytes));

                    messageSendFailed = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messageSendFailed) {
                                // 返回失败socket
                                JSONObject sendJson = new JSONObject();
                                try {
                                    sendJson.put("apiType", "sms_send");
                                    sendJson.put("code", 1);
                                    sendJson.put("msg", "发送失败");
                                    sendJson.put("id", failedMessageId);

                                    SocketServer.send(sendJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (failedMessageId != 0) {
                                    MessageProxy.setMessageFailed(db, failedMessageId);

                                    if (mainActivity.fragmentName.equals("message") && mainActivity.messageFragment.tg.equals("send")) {
                                        mainActivity.messageFragment.reloadDate();
                                    }
                                }
                            }
                        }
                    }, Constant.MESSAGE_FAIL_TIME);

                    break;
                case "sms_read":
                    final String userAddress1 = receiveJson.getString("userAddress");
                    MessageProxy.setMessageReadBySender(db, userAddress1);
                    mainActivity.modifyGpsBarMessageCount();
                    break;
                case "sms_delete":
                    final String userAddress2 = receiveJson.getString("userAddress");
                    // 删除 所有消息
                    MessageProxy.deleteAllByAddress(db, userAddress2, myNumber);
                    mainActivity.modifyGpsBarMessageCount();
                    break;
                case "set_time":
                    Date date = new Date(receiveJson.getString("time"));
                    if (Math.abs(date.getTime() - Constant.SYSTEM_DATE.getTime()) > 3600 * 1000) {
                        Toast.makeText(getApplicationContext(), "设置时间", Toast.LENGTH_SHORT).show();
                        Constant.SYSTEM_DATE = date;
                    }
                    break;
                case "debug":
                    int orderCode = receiveJson.getInt("code");
                    int orderContent = receiveJson.getInt("content");
                    switch (orderCode) {
                        case 0:
                            mainActivity.gpsBar.setDebugButtonLayoutShow(orderContent == 0);
                            break;
                    }
                    break;
                case "route_list":
                    jsonObject.put("apiType", "route_list");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("data", getRouteList());
                    SocketServer.send(jsonObject);
                    break;
                case "route_detail":
                    String navtime = receiveJson.getString("navtime");
                    jsonObject.put("apiType", "route_detail");
                    jsonObject.put("code", "0");
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("navtime", receiveJson.getString("navtime"));
                    jsonObject.put("data", getRouteDetail(navtime));
                    SocketServer.send(jsonObject);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取航迹列表
     *
     * @return
     */
    public JSONArray getRouteList() {
        JSONArray routeList = new JSONArray();
        try {
            Cursor cursor = db.execQuery("select navtime from t_location group by navtime");
            //判断游标是否为空
            if (cursor.moveToFirst()) {
                //遍历游标
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String navtime = cursor.getString(0);
                    if (navtime != null) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("navtime", navtime);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        routeList.put(jsonObject);
                    }
                }
            }
            cursor.close();
        } catch (DbException e) {
            e.printStackTrace();
        }

        return routeList;
    }

    /**
     * 获取航迹内容 返回String
     *
     * @param navtime
     * @return
     */
    public String getRouteDetail(String navtime) {
        String detail;
        try {
            List<LocationBean> list = db.selector(LocationBean.class)
                    .where("navtime", "=", navtime)
                    .orderBy("acqtime")
                    .findAll();
            if (list == null || list.isEmpty()) {
                detail = "未查询到相关轨迹信息";
            } else {
                detail = "";
                for (LocationBean lb : list) {
                    detail += lb.toString();
                }
            }
            db.delete(list);
            Date date = new Date(Long.parseLong(navtime));
            Toast.makeText(mainActivity, "已删除航迹：" + com.cetcme.xkterminal.MyClass.DateUtil.Date2String(date), Toast.LENGTH_SHORT).show();

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("apiType", "refreshRouteList");
            EventBus.getDefault().post(new SmsEvent(jsonObject1));
        } catch (Exception e) {
            e.printStackTrace();
            detail = "未查询到相关轨迹信息";
        }

        return detail;
    }

    public JSONArray getSmsList() {
        List<String> userAddresses = MessageProxy.getAddress(db);

        JSONArray smsList = new JSONArray();
        if (userAddresses != null) {
            for (String userAddress : userAddresses) {

                MessageBean message = MessageProxy.getLast(db, userAddress, myNumber);
                if (message != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("lastSmsContent", message.getContent());
                        jsonObject.put("userAddress", userAddress);
                        jsonObject.put("lastSmsTime", message.getSend_time());
                        jsonObject.put("hasUnread", MessageProxy.getUnReadCountByAddress(db, userAddress) != 0);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    smsList.put(jsonObject);
                }

            }
        }

        return smsList;
    }

    public JSONArray getSmsDetail(String userAddress, int countPerPage, String timeBefore) {
        List<MessageBean> messages = MessageProxy.getByAddressAndTime(db, userAddress, myNumber, countPerPage, timeBefore);
        JSONArray smsList = new JSONArray();
        if (messages != null) {
            for (int i = 0; i < messages.size(); i++) {
                MessageBean message = messages.get(i);
                JSONObject jsonObject = message.toJson();
                smsList.put(jsonObject);
            }
        }
        return smsList;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (warnTimer != null) {
            warnTimer.cancel();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }


    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
//            SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
//            String path = sp.getString("DEVICE", "");
//            path = "/dev/ttyS3";
//            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            String path = Constant.SERIAL_DATA_PORT_PATH;
            int baudrate = Constant.SERIAL_DATA_PORT_BAUD_RATE;

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    //    public SerialPort getGpsSerialPort() throws SecurityException, IOException, InvalidParameterException {
//        if (gpsSerialPort == null) {
//			/* Read serial port parameters */
//            String path = Constant.SERIAL_GPS_PORT_PATH;
//            int baudrate = Constant.SERIAL_GPS_PORT_BAUD_RATE;
//
//			/* Check parameters */
//            if ( (path.length() == 0) || (baudrate == -1)) {
//                throw new InvalidParameterException();
//            }
//
//			/* Open the serial port */
//            gpsSerialPort = new SerialPort(new File(path), baudrate, 0);
//        }
//        return gpsSerialPort;
//    }
    public SerialPort getAisSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (aisSerialPort == null) {
            /* Read serial port parameters */
            String path = Constant.SERIAL_AIS_PORT_PATH;
            int baudrate = Constant.SERIAL_AIS_PORT_BAUD_RATE;

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            aisSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return aisSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void DisplayError(int resourceId) {
        Log.e("MyApplication", "DisplayError: " + getString(resourceId), null);
        /*
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        b.show();
        */
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    Thread.sleep(10);
                    byte[] buffer = new byte[1];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    private class GpsReadThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            while(!isInterrupted()) {
//                int size;
//                try {
//                    Thread.sleep(1000);
//                    byte[] buffer = new byte[200];
//                    if (gpsInputStream == null) return;
//                    size = gpsInputStream.read(buffer);
//                    if (size > 0) {
//                        onGpsDataReceived(buffer, size);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


    byte[] serialBuffer = new byte[100];
    int serialCount = 0;
    boolean hasHead = false;

    protected void onDataReceived(byte[] buffer, int size) {
        serialBuffer[serialCount] = buffer[0];
        serialCount++;
        if (serialCount == 3) {
            String head = Util.bytesGetHead(serialBuffer, 3);
            if (head == null) return;
            if (head.equals("$04") ||
                    head.equals("$R4") ||
                    head.equals("$R1") ||
                    head.equals("$R2") ||
                    head.equals("$R5") ||
                    head.equals("$R0") ||
                    head.equals("$R6") ||
                    head.equals("$R7") ||
                    head.equals("$R8") ||
                    head.equals("$RA")) {
                hasHead = true;
            } else {
                Util.bytesRemoveFirst(serialBuffer, serialCount);
                serialCount--;
            }
        }

        if (hasHead) {
            if (serialCount >= 81) {
                serialBuffer = new byte[100];
                serialCount = 0;
                hasHead = false;
                return;
            }
            if (serialBuffer[serialCount - 2] == (byte) 0x0D && serialBuffer[serialCount - 1] == (byte) 0x0A) {
                serialBuffer = ByteUtil.subBytes(serialBuffer, 0, serialCount);
                System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("bytes", serialBuffer);
                switch (Util.bytesGetHead(serialBuffer, 3)) {
                    case "$04":
                        // 接收短信
                        message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_MESSAGE;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    default:
                        hasHead = false;
                        Util.bytesRemoveFirst(serialBuffer, serialCount);
                        serialCount--;
                        break;
                }
                hasHead = false;
                serialBuffer = new byte[100];
                serialCount = 0;
            } else if (serialBuffer[serialCount - 1] == (byte) 0x3B) {
                serialBuffer = ByteUtil.subBytes(serialBuffer, 0, serialCount);
                System.out.println("收到包：" + ConvertUtil.bytesToHexString(serialBuffer));
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("bytes", serialBuffer);
                switch (Util.bytesGetHead(serialBuffer, 3)) {
                    case "$04":
                        // 接收短信 如果短信内容有分号0x3B 将会进入此处 返回继续接收数据
                        return;
                    case "$R4":
                        // 短信发送成功
                        message.what = DataHandler.SERIAL_PORT_MESSAGE_SEND_SUCCESS;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R1":
                        // 接收时间
                        if (serialCount == 25) {
                            message.what = DataHandler.SERIAL_PORT_TIME_NUMBER_AND_COMMUNICATION_FROM;
                        } else if (serialCount == 20) {
                            message.what = DataHandler.SERIAL_PORT_TIME;
                        }
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R2":
                        // 接收时间
                        message.what = DataHandler.SERIAL_PORT_ID_EDIT_OK;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R5":
                        if (serialCount == 14) {
                            // 紧急报警成功
                            message.what = DataHandler.SERIAL_PORT_ALERT_SEND_SUCCESS;
                        } else if (serialCount == 15) {
                            // 显示报警activity
                            message.what = DataHandler.SERIAL_PORT_SHOW_ALERT_ACTIVITY;
                        } else if (serialCount == 16) {
                            // 增加报警记录，显示收到报警
                            message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_ALERT;
                        }
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R0":
                        // 接收身份证信息
                        message.what = DataHandler.SERIAL_PORT_RECEIVE_NEW_SIGN;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R6":
                        // 调节背光
                        message.what = DataHandler.SERIAL_PORT_MODIFY_SCREEN_BRIGHTNESS;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R7":
                        // 关机
                        message.what = DataHandler.SERIAL_PORT_SHUT_DOWN;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        break;
                    case "$R8":
                        if (serialBuffer[3] == 0x01) {
                            System.out.println("报警中");
                            // 报警中
                            message.what = DataHandler.SERIAL_PORT_ALERT_START;
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        } else if (serialBuffer[3] == 0x02) {
                            System.out.println("报警失败");
                            // 报警失败
                            message.what = DataHandler.SERIAL_PORT_ALERT_FAIL;
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                        break;
                    case "$RA":
                        // 自检
                        message.what = DataHandler.SERIAL_PORT_CHECK;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    default:
                        hasHead = false;
                        Util.bytesRemoveFirst(serialBuffer, serialCount);
                        serialCount--;
                        break;
                }
                hasHead = false;
                serialBuffer = new byte[100];
                serialCount = 0;
            }

        }

        if (serialCount >= 81) {
            serialBuffer = new byte[100];
            serialCount = 0;
            hasHead = false;
            return;
        }

    }

    String TAG = "GPS Serial Port";

    protected void onGpsDataReceived(byte[] buffer, int size) {

        Log.i(TAG, "onGpsDataReceived: ");
        Log.i(TAG, "size: " + size);

        String gpsDataStr = new String(ByteUtil.subBytes(buffer, 0, size));

        // 过滤掉GPGAA
        int loc = gpsDataStr.indexOf("$GNRMC");
        if (loc == -1) return; // 不包含的话
        String string = gpsDataStr.substring(loc);

        Log.i(TAG, "onGpsDataReceived: " + string);
        if (string.startsWith("$GNRMC") && string.endsWith("\r\n")) {
            Log.i(TAG, "onGpsDataReceived: 完整");
            string = string.replace("$GNRMC", "$GPRMC");
            GpsInfo gpsInfo = GpsParse.parse(string);
            if (gpsInfo != null) {
                LocationBean locationBean = new LocationBean();
                locationBean.setLatitude(gpsInfo.latititude);
                locationBean.setLongitude(gpsInfo.longtitude);
                locationBean.setSpeed(gpsInfo.speed);
                locationBean.setHeading(gpsInfo.course);
                locationBean.setAcqtime(gpsInfo.cal1.getTime());
                currentLocation = locationBean;

                EventBus.getDefault().post(locationBean);
            }
        }
    }

    private static final Queue<com.cetcme.xkterminal.Sqlite.Bean.Message> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
    private static final Queue<com.cetcme.xkterminal.Sqlite.Bean.Message> MESSAGE_BACK_QUEUE = new LinkedBlockingQueue<>();

    private static final ArrayList<com.cetcme.xkterminal.Sqlite.Bean.Message> WAIT_MESSAGE = new ArrayList();

    public void sendMessageBytes(int msgId, final byte[] buffer, boolean longPackage) {
        if (!Constant.PHONE_TEST) {
            if (!isSendThreadStart) {
                if (msgId != 0) {
                    MessageProxy.setMessageFailed(db, msgId);
                    if (mainActivity != null && mainActivity.fragmentName != null && mainActivity.messageFragment != null) {
                        if (mainActivity.fragmentName.equals("message") && mainActivity.messageFragment.tg.equals("send")) {
                            mainActivity.messageFragment.reloadDate();
                        }
                    }
                    if (Looper.myLooper() != Looper.getMainLooper()) {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
            try {
                com.cetcme.xkterminal.Sqlite.Bean.Message msg = new com.cetcme.xkterminal.Sqlite.Bean.Message();
                msg.setId(0);
                msg.setMessageId(msgId);
                msg.setMessage(buffer);
                msg.setSend(false);
                if (longPackage) {
                    WAIT_MESSAGE.add(msg);
                } else {
                    MESSAGE_QUEUE.offer(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String nettySendResult = SendMsg.getSendMsg().sendMsg(buffer);
                    Log.e(TAG, "sendBytes: " + nettySendResult);
                }
            }).start();
        }
    }


    public void sendBytes(final byte[] buffer) {
        if (!Constant.PHONE_TEST) {
            String str = new String(buffer);
            if (str != null && str.startsWith("$04")) {
//                synchronized (MESSAGE_QUEUE) {
                try {
                    com.cetcme.xkterminal.Sqlite.Bean.Message msg = new com.cetcme.xkterminal.Sqlite.Bean.Message();
                    msg.setMessage(buffer);
                    msg.setSend(false);
//                    if (db.saveBindingId(msg)) {
                    MESSAGE_BACK_QUEUE.offer(msg);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                }
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mOutputStream != null) {
                                mOutputStream.write(buffer);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String nettySendResult = SendMsg.getSendMsg().sendMsg(buffer);
                    Log.e(TAG, "sendBytes: " + nettySendResult);
                }
            }).start();
        }
//        System.out.println("发送包：" + ConvertUtil.bytesToHexString(buffer));
    }

    private class SendingThread extends Thread {

        SendingThread() {
            try {
                List<com.cetcme.xkterminal.Sqlite.Bean.Message> msgs = db.selector(com.cetcme.xkterminal.Sqlite.Bean.Message.class)
                        .findAll();
                if (msgs != null && !msgs.isEmpty()) {
                    for (com.cetcme.xkterminal.Sqlite.Bean.Message msg : msgs) {
                        MESSAGE_BACK_QUEUE.offer(msg);
                    }
                    Log.e("TAG_DIANTAI", "以保存的size"+MESSAGE_BACK_QUEUE.size());
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            com.cetcme.xkterminal.Sqlite.Bean.Message msg = null;
            while (true) {
                try {
                    if (canSend) {
                        boolean flag = true;
                        if (MESSAGE_QUEUE.isEmpty()) {
                            if (MESSAGE_BACK_QUEUE.isEmpty()) continue;
                            msg = MESSAGE_BACK_QUEUE.poll();
                        } else {
                            msg = MESSAGE_QUEUE.poll();
                            flag = false;
                        }
                        if (msg != null && mOutputStream != null) {
                            canSend = false;
                            messageSendFailed = true;
                            byte[] content = msg.getMessage();
                            Log.e("TAG_DIANTAI_3", Utils.byte2HexStr(content));
                            mOutputStream.write(content);
                            handler.sendEmptyMessage(5);
                            if (flag) {
                                db.delete(msg);
                            } else {
                                //超时时间 过了
//                            Thread.sleep(Constant.MESSAGE_FAIL_TIME + 2000);
                                timer = new Timer();
                                timer.schedule(new SendTimeOutTask(msg), 1000, 1000);
                            }
                            currentTimeoutRunnable = new TimeoutRunnable();
                            handler.postDelayed(currentTimeoutRunnable, 60 * 1000);
                        }
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Timer timer;
    private int timeCount = 0;
    private boolean canSend = true;
    private TimeoutRunnable currentTimeoutRunnable;

    private class TimeoutRunnable implements Runnable {
        @Override
        public void run() {
            if (!canSend) {
                canSend = true;
            }
        }
    }

    private class SendTimeOutTask extends TimerTask {
        private com.cetcme.xkterminal.Sqlite.Bean.Message msg;

        private SendTimeOutTask(com.cetcme.xkterminal.Sqlite.Bean.Message msg) {
            timeCount = 0;
            this.msg = msg;
        }

        @Override
        public void run() {
            timeCount++;
            if (messageSendFailed) {
                //发送超时
                if (timeCount > Constant.MESSAGE_FAIL_TIME / 1000) {
                    if (timer != null) {
                        timer.cancel();
                    }
                    // 发送超时
                    handler.sendEmptyMessage(6);
                    //失败
                    if (msg.getMessageId() != 0) {
                        MessageProxy.setMessageFailed(db, msg.getMessageId());
                        handler.sendEmptyMessage(3);
                    }
                    if (!WAIT_MESSAGE.isEmpty()) {
                        Iterator<com.cetcme.xkterminal.Sqlite.Bean.Message> iterator = WAIT_MESSAGE.iterator();
                        while (iterator.hasNext()) {
                            com.cetcme.xkterminal.Sqlite.Bean.Message m = iterator.next();
                            if (m.getMessageId() == msg.getMessageId()) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                    if (handler != null && currentTimeoutRunnable != null) {
                        handler.removeCallbacks(currentTimeoutRunnable);
                    }
                    canSend = true;
                }
            } else {
                // 发送成功
                if (timer != null) {
                    timer.cancel();
                }
                handler.sendEmptyMessage(4);
                if (!WAIT_MESSAGE.isEmpty()) {
                    Iterator<com.cetcme.xkterminal.Sqlite.Bean.Message> iterator = WAIT_MESSAGE.iterator();
                    while (iterator.hasNext()) {
                        com.cetcme.xkterminal.Sqlite.Bean.Message m = iterator.next();
                        if (m.getMessageId() == msg.getMessageId()) {
                            MESSAGE_QUEUE.offer(m);
                            iterator.remove();
                            break;
                        }
                    }
                }
                canSend = false;
            }
        }
    }

    public void sendLightOn(boolean on) {
        System.out.println("控制灯：" + on);
        byte[] bytes;
        if (on) {
            bytes = "$08".getBytes();
        } else {
            bytes = "$09".getBytes();
        }
        bytes = ByteUtil.byteMerger(bytes, new byte[]{0x01});
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());
        sendBytes(bytes);
    }
}
