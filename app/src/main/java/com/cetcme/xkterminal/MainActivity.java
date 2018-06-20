package com.cetcme.xkterminal;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.cetcme.xkterminal.ActionBar.PageBar;
import com.cetcme.xkterminal.ActionBar.SendBar;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.DataFormat.Util.Util;
import com.cetcme.xkterminal.DataFormat.WarnFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.Fragment.AboutFragment;
import com.cetcme.xkterminal.Fragment.LogFragment;
import com.cetcme.xkterminal.Fragment.MainFragment;
import com.cetcme.xkterminal.Fragment.MessageFragment;
import com.cetcme.xkterminal.Fragment.MessageNewFragment;
import com.cetcme.xkterminal.Fragment.SettingTabFragment;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.DensityUtil;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Socket.SocketServer;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.AlertProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.SignProxy;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import aisparser.Message1;
import aisparser.Message14;
import aisparser.Message2;
import aisparser.Message3;
import aisparser.Sixbit;
import aisparser.Vdm;
import yimamapapi.skia.AisInfo;

public class MainActivity extends AppCompatActivity {

    public static String myNumber = "";

    private DbManager db;

    public GPSBar gpsBar;

    public BottomBar bottomBar;
    public MessageBar messageBar;
    public PageBar pageBar;
    public BackBar backBar;
    public SendBar sendBar;

    public MainFragment mainFragment;
    public MessageFragment messageFragment;
    private LogFragment logFragment;
    private SettingTabFragment settingFragment;
    private AboutFragment aboutFragment;
    private MessageNewFragment messageNewFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            ftdid2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);

        readData = new byte[readLength];
        readDataToText = new char[readLength];
        try {
            typeMap = new JSONObject("{\"0\":\"000000\",\"1\":\"000001\",\"2\":\"000010\",\"3\":\"000011\",\"4\":\"000100\",\"5\":\"000101\",\"6\":\"000110\",\"7\":\"000111\",\"8\":\"001000\",\"9\":\"001001\",\":\":\"001010\",\";\":\"001011\",\"<\":\"001100\",\"=\":\"001101\",\">\":\"001110\",\"?\":\"001111\",\"@\":\"010000\",\"A\":\"010001\",\"B\":\"010010\",\"C\":\"010011\",\"D\":\"010100\",\"E\":\"010101\",\"F\":\"010110\",\"G\":\"010111\",\"H\":\"011000\",\"I\":\"011001\",\"J\":\"011010\",\"K\":\"011011\",\"L\":\"011100\",\"M\":\"011101\",\"N\":\"011110\",\"O\":\"011111\",\"P\":\"100000\",\"Q\":\"100001\",\"R\":\"100010\",\"S\":\"100011\",\"T\":\"100100\",\"U\":\"100101\",\"V\":\"100110\",\"W\":\"100111\",\"`\":\"101000\",\"a\":\"101001\",\"b\":\"101010\",\"c\":\"101011\",\"d\":\"101100\",\"e\":\"101101\",\"f\":\"101110\",\"g\":\"101111\",\"h\":\"110000\",\"i\":\"110001\",\"j\":\"110010\",\"k\":\"110011\",\"l\":\"110100\",\"m\":\"110101\",\"n\":\"110110\",\"o\":\"110111\",\"p\":\"111000\",\"q\":\"111001\",\"r\":\"111010\",\"s\":\"111011\",\"t\":\"111100\",\"u\":\"111101\",\"v\":\"111110\",\"w\":\"111111\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //设置当前窗体为全屏显示
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);
        //隐藏动作条
        getSupportActionBar().hide();

        db = ((MyApplication) getApplication()).db;

        ((MyApplication) getApplication()).mainActivity = this;

        bindView();

        initSpeech();

        initMainFragment();
        initHud();

        myNumber = PreferencesUtils.getString(this, "myNumber");
        if (myNumber == null || myNumber.isEmpty()) {
            myNumber = "654321";
            PreferencesUtils.putString(this, "myNumber", myNumber);
        }

        modifyGpsBarMessageCount();

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        createWifiHotspot();

        // 发送启动$01，要求对方发时间
        sendBootData();

        checkoutShutDown();

        // TODO: test
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showMessageDialog("测试短信", MessageDialogActivity.TYPE_CALL_ROLL);
            }
        }, 2000)
        */

        // TODO: fake
        volTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (voltage < 3.2) return;
                voltage -= 0.02;
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "voltage");
                    jsonObject.put("voltage", voltage);
                    SmsEvent smsEvent = new SmsEvent(jsonObject);
                    EventBus.getDefault().post(smsEvent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, 0, 60 * 1000);

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (System.currentTimeMillis() - MyApplication.getInstance().oldAisReceiveTime >= 5 * 60 * 1000) {
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

//        if (gpsBar != null) {
//            gpsBar.setAisStatus(true);
//            gpsBar.setGPSStatus(true);
//        }

        // test
//        addMessage("123456", "ceshiduanxin", false);
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
                // play("我打开了海图导航");
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
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

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
        this.unregisterReceiver(mUsbReceiver);
        super.onDestroy();
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
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
    public void addMessage(final String address, final String content, final boolean read) {
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

    public void showMessageDialog(String content, int type) {
        // type 0: 救护, 1: 报警提醒, 2: 夜间点名
        Intent intent = new Intent(MainActivity.this, MessageDialogActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("type", type);
        startActivity(intent);
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

        gpsBar.mainActivity = this;
        bottomBar.mainActivity = this;
        messageBar.mainActivity = this;
        pageBar.mainActivity = this;
        backBar.mainActivity = this;
        sendBar.mainActivity = this;

    }

    private void showMainBar() {
        bottomBar.setVisibility(View.VISIBLE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
    }

    private void showMessageBar(String tg) {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.VISIBLE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);

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
    }

    private void showBackBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.VISIBLE);
        sendBar.setVisibility(View.GONE);
    }

    private void showSendBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.VISIBLE);
    }

    public void initMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }
//        if (!mainFragment.isAdded()) {
//            transaction.add(R.id.main_frame_layout, mainFragment);
//        } else {
//            if (mainFragment.isHidden()) {
//                transaction.show(mainFragment);
//            }
//        }
        transaction.replace(R.id.main_frame_layout, mainFragment);
        transaction.commit();
        showMainBar();
        fragmentName = "main";

        messageReceiver = "";
        messageContent = "";
        messageTime = "";
        messageId = "";
        messageIndex = -1;
    }

    public void initMessageFragment(String tg) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (messageFragment == null){
        messageFragment = new MessageFragment(tg);
//        }
        transaction.replace(R.id.main_frame_layout, messageFragment);
        transaction.commit();

        messageFragment.mainActivity = this;
        showMessageBar(tg);
        fragmentName = "message";
        messageListStatus = tg;
    }

    public void initLogFragment(String tg) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (logFragment == null){
        logFragment = new LogFragment(tg);
//        }
        transaction.replace(R.id.main_frame_layout, logFragment);
        transaction.commit();

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
//        if (settingFragment == null) {
        settingFragment = new SettingTabFragment();
//        }
        transaction.replace(R.id.main_frame_layout, settingFragment);
        transaction.commit();

        showBackBar();
        fragmentName = "setting";
    }

    public void initAboutFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (aboutFragment == null) {
            aboutFragment = new AboutFragment();
        }
        transaction.replace(R.id.main_frame_layout, aboutFragment);
        transaction.commit();

        showBackBar();
        fragmentName = "about";
    }

    public String messageReceiver = "";
    public String messageContent = "";
    public String messageTime = "";
    public String messageId = "";
    public int messageIndex = -1;

    public void initNewFragment(String tg) {
        if (!tg.equals("new") && (messageContent.isEmpty())) {
            Toast.makeText(this, "请选择一条短信", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (logFragment == null){
        messageNewFragment = new MessageNewFragment(tg, messageReceiver, messageContent, messageTime);
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
            showBackBar();
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

    private void sendBootData() {
        byte[] bytes = "$01".getBytes();
        bytes = ByteUtil.byteMerger(bytes, new byte[]{0x01, 0x00});
        bytes = ByteUtil.byteMerger(bytes, new byte[]{0x2A, 0x01});
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());
        ((MyApplication) getApplication()).sendBytes(bytes);
    }

    public void showShutDownHud() {
        kProgressHUD.setLabel("关机中");
        kProgressHUD.show();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                kProgressHUD.dismiss();
//            }
//        }, 1500);
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
                }
                break;
        }
    }


    //TODO: fake
    public double voltage = 4.20;
    public Timer volTimer = new Timer();

    @Override
    protected void onResume() {
        super.onResume();
        DevCount = 0;
        createDeviceList();
        if (DevCount > 0) {
            connectFunction();
            SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }
    }

    // USB处理
    D2xxManager ftdid2xx;
    FT_Device ftDev = null;
    int DevCount = -1;
    int currentIndex = -1;
    int openIndex = 2;
    public boolean bReadThreadGoing = false;
    public USBReadThread read_thread;
    boolean uart_configured = false;
    public static final int readLength = 512;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    /*local variables*/
    int baudRate = 38400; /*baud rate*/
    byte stopBit = 1; /*1:1stop bits, 2:2 stop bits*/
    byte dataBit = 8; /*8:8bit, 7: 7bit*/
    byte parity = 0;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    byte flowControl = 0; /*0:none, 1: flow control(CTS,RTS)*/
    int portNumber = 1; /*port number*/


    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (ftDev == null) return;
        if (ftDev.isOpen() == false) {
            Log.e("j2xx", "SetConfig: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

        uart_configured = true;
        // Toast.makeText(getApplicationContext(), "Config done", Toast.LENGTH_SHORT).show();
    }


    public void createDeviceList() {
        if (ftdid2xx == null) return;
        int tempDevCount = ftdid2xx.createDeviceInfoList(getApplicationContext());

        if (tempDevCount > 0) {
            if (DevCount != tempDevCount) {
                DevCount = tempDevCount;
            }
        } else {
            DevCount = -1;
            currentIndex = -1;
        }
    }


    public void connectFunction() {
        if (ftdid2xx == null) return;
        int tmpProtNumber = openIndex + 1;

        if (currentIndex != openIndex) {
            if (null == ftDev) {
                ftDev = ftdid2xx.openByIndex(getApplicationContext(), openIndex);
            } else {
                synchronized (ftDev) {
                    ftDev = ftdid2xx.openByIndex(getApplicationContext(), openIndex);
                }
            }
            uart_configured = false;
        } else {
            // Toast.makeText(getApplicationContext(), "Device port " + tmpProtNumber + " is already opened", Toast.LENGTH_LONG).show();
            return;
        }

        if (ftDev == null) {
            // Toast.makeText(getApplicationContext(), "open device port(" + tmpProtNumber + ") NG, ftDev == null", Toast.LENGTH_LONG).show();
            return;
        }

        if (true == ftDev.isOpen()) {
            currentIndex = openIndex;
            // Toast.makeText(getApplicationContext(), "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();

            if (false == bReadThreadGoing) {
                read_thread = new USBReadThread(usbHandler);
                read_thread.start();
                bReadThreadGoing = true;
            }
        } else {
            // Toast.makeText(getApplicationContext(), "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
            //Toast.makeText(DeviceUARTContext, "Need to get permission!", Toast.LENGTH_SHORT).show();
        }
    }

    private String preRestStr = "";
    private final List<Map<String, Object>> headIndex = new ArrayList<>();
    private JSONObject typeMap;
    private final Vdm vdm = new Vdm();

    @SuppressLint("HandlerLeak")
    final Handler usbHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (iavailable > 0) {
                headIndex.clear();
                String gpsDataStr = preRestStr + String.copyValueOf(readDataToText, 0, iavailable);
                int len = gpsDataStr.length();
                if (len <= 6) {
                    preRestStr = gpsDataStr;
                    return;
                }
                for (int i = 0; i < len - 6; i++) {
                    String headStr = gpsDataStr.substring(i, i + 6);
                    if ("!AIVDM".equals(headStr)
                            || "!AIVDO".equals(headStr)
                            || "$GPGSV".equals(headStr)) {
                        int end = gpsDataStr.indexOf("\n", i+1);
                        if (end != -1) {
                            String str = gpsDataStr.substring(i+7, end + 1);
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
                            Log.e("TAG", "ais: "+ newStr);
                            try {
                                MyApplication.getInstance().oldAisReceiveTime = System.currentTimeMillis();
                                MyApplication.getInstance().isAisConnected = true;
                                if (MyApplication.getInstance().mainActivity != null) {
                                    MyApplication.getInstance().mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MyApplication.getInstance().mainActivity.gpsBar.setAisStatus(true);
                                        }
                                    });
                                }
                                boolean isOwn = "!AIVDO".equals(type);
                                int result = vdm.add(newStr);
                                if (0 == result) {
                                    AisInfo aisInfo = new AisInfo("null");
                                    Sixbit sixbit = vdm.sixbit();
                                    switch (vdm.msgid()) {
                                        case 1:
                                            Message1 message1 = new Message1();
                                            message1.parse(sixbit);
                                            aisInfo.mmsi = (int) message1.userid();
                                            aisInfo.COG = message1.cog() / 10.0f;
                                            aisInfo.SOG = message1.sog() / 10.0f;
                                            aisInfo.MsgType = 1;
                                            aisInfo.longtitude = (int)(message1.longitude() * 1.0 / 600000 * 1e7);
                                            aisInfo.latititude = (int)(message1.latitude() * 1.0 / 600000 * 1e7);
                                            break;
                                        case 2:
                                            Message2 message2 = new Message2();
                                            message2.parse(sixbit);
                                            aisInfo.mmsi = (int) message2.userid();
                                            aisInfo.COG = message2.cog() / 10.0f;
                                            aisInfo.SOG = message2.sog() / 10.0f;
                                            aisInfo.MsgType = 2;
                                            aisInfo.longtitude = (int)(message2.longitude() * 1.0 / 600000 * 1e7);
                                            aisInfo.latititude = (int)(message2.latitude() * 1.0 / 600000 * 1e7);
                                            break;
                                        case 3:
                                            Message3 message3 = new Message3();
                                            message3.parse(sixbit);
                                            aisInfo.mmsi = (int) message3.userid();
                                            aisInfo.COG = message3.cog() / 10.0f;
                                            aisInfo.SOG = message3.sog() / 10.0f;
                                            aisInfo.MsgType = 3;
                                            aisInfo.longtitude = (int)(message3.longitude() * 1.0 / 600000 * 1e7);
                                            aisInfo.latititude = (int)(message3.latitude() * 1.0 / 600000 * 1e7);
                                            break;
                                        case 14:
                                            Message14 message14 = new Message14();
                                            message14.parse(sixbit);
                                            String message = message14.message();
                                            if (TextUtils.isEmpty(message)) {
                                                message = "AIS报警";
                                            }
                                            MyApplication.getInstance().sendBytes(WarnFormat.format("" + message14.userid(), message));
                                            break;
                                        case 18:
                                            aisparser.Message18 message18 = new aisparser.Message18();
                                            message18.parse(sixbit);
                                            aisInfo.mmsi = (int) message18.userid();
                                            aisInfo.COG = message18.cog() / 10.0f;
                                            aisInfo.SOG = message18.sog() / 10.0f;
                                            aisInfo.MsgType = 18;
                                            aisInfo.longtitude = (int)(message18.longitude() * 1.0 / 600000 * 1e7);
                                            aisInfo.latititude = (int)(message18.latitude() * 1.0 / 600000 * 1e7);
                                            break;
                                        case 19:
                                            aisparser.Message19 message19 = new aisparser.Message19();
                                            message19.parse(sixbit);
                                            aisInfo.mmsi = (int) message19.userid();
                                            aisInfo.COG = message19.cog() / 10.0f;
                                            aisInfo.SOG = message19.sog() / 10.0f;
                                            aisInfo.MsgType = 19;
                                            aisInfo.longtitude = (int)(message19.longitude() * 1.0 / 600000 * 1e7);
                                            aisInfo.latititude = (int)(message19.latitude() * 1.0 / 600000 * 1e7);
                                            break;
                                    }
                                    int mmsi = Integer.valueOf(PreferencesUtils.getString(MainActivity.this, "shipNo", "0")).intValue();
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
                            Log.e("TAG", "gps: "+ newStr);
                            try {
                                newStr = newStr.substring(newStr.indexOf(",") + 1, newStr.lastIndexOf("*")) + ",";
                                boolean isDou = newStr.endsWith(",");
                                String[] arr = newStr.split(",");
                                for (int j = 3; j < arr.length; j += 4) {
                                    int no = Integer.valueOf(arr[j]);
                                    int yangjiao = Integer.valueOf(arr[j + 1]);
                                    int fangwei = Integer.valueOf(arr[j + 2]);
                                    int xinhao = 0;
                                    // xinhao arr[j + 3] 可能为空
                                    if (!isDou) {
                                        xinhao = Integer.valueOf("".equals(arr[j + 3]) ? "0" : arr[j + 3]);
                                    }
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
    };


    private class USBReadThread extends Thread {
        Handler mHandler;

        USBReadThread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            int i;

            while (true == bReadThreadGoing) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }

                synchronized (ftDev) {
                    iavailable = ftDev.getQueueStatus();
                    if (iavailable > 0) {

                        if (iavailable > readLength) {
                            iavailable = readLength;
                        }

                        ftDev.read(readData, iavailable);
                        for (i = 0; i < iavailable; i++) {
                            readDataToText[i] = (char) readData[i];
                        }
                        Message msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }

    }

    int index = 0;
    /***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = "FragL";
            String action = intent.getAction();
            //Log.e("TAG", action);
            index++;
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) && index == 1) {
                Log.i(TAG, "DETACHED...");
                MyApplication.getInstance().isAisConnected = false;
                gpsBar.setAisStatus(false);
                DevCount = -1;
                currentIndex = -1;
                bReadThreadGoing = false;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (ftDev != null) {
                    synchronized (ftDev) {
                        if (true == ftDev.isOpen()) {
                            ftDev.close();
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) && index == 1) {
                MyApplication.getInstance().isAisConnected = true;
                gpsBar.setAisStatus(true);
            }
            if (index > 1) {
                index = 0;
            }
        }
    };
}
