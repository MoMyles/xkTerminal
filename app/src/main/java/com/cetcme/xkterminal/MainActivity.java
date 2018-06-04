package com.cetcme.xkterminal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
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

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

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
        super.onCreate(savedInstanceState);
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
        }, 2000);
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
                    if (System.currentTimeMillis() - MyApplication.getInstance().oldAisReceiveTime > 60 * 1000) {
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

        if (gpsBar != null) {
            gpsBar.setAisStatus(true);
            gpsBar.setGPSStatus(true);
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

    public void addSignLog(String id, String name) {
        SignProxy.insert(db, id, name, Constant.SYSTEM_DATE, false);
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

    public void showIDCardDialog(String id, String name, String nation, String address) {
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

        addSignLog(id, name);

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

    private void showMessageBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.VISIBLE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
        sendBar.setVisibility(View.GONE);
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
        showMessageBar();
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
            showMessageBar();
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
        if (!tg.equals("new") && (messageContent.isEmpty() || messageReceiver.isEmpty())) {
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

        showMessageBar();
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

    // 发送短信
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

        byte[] messageBytes = MessageFormat.format(receiver, content, receiver.length() == 11 ? MessageFormat.MESSAGE_TYPE_CELLPHONE : MessageFormat.MESSAGE_TYPE_NORMAL);
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


    //TODO: fake
    public double voltage = 4.20;
    public Timer volTimer = new Timer();


}
