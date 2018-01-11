package com.cetcme.xkterminal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cetcme.xkterminal.ActionBar.BackBar;
import com.cetcme.xkterminal.ActionBar.BottomBar;
import com.cetcme.xkterminal.ActionBar.GPSBar;
import com.cetcme.xkterminal.ActionBar.MessageBar;
import com.cetcme.xkterminal.ActionBar.PageBar;
import com.cetcme.xkterminal.ActionBar.SendBar;
import com.cetcme.xkterminal.Fragment.AboutFragment;
import com.cetcme.xkterminal.Fragment.LogFragment;
import com.cetcme.xkterminal.Fragment.MainFragment;
import com.cetcme.xkterminal.Fragment.MessageFragment;
import com.cetcme.xkterminal.Fragment.MessageNewFragment;
import com.cetcme.xkterminal.Fragment.SettingFragment;
import com.cetcme.xkterminal.MyClass.Constant;
import com.qiuhong.qhlibrary.Dialog.QHDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private GPSBar gpsBar;

    private BottomBar bottomBar;
    public  MessageBar messageBar;
    public  PageBar pageBar;
    private BackBar backBar;
    private SendBar sendBar;

    private MainFragment mainFragment;
    private MessageFragment messageFragment;
    private LogFragment logFragment;
    private SettingFragment settingFragment;
    private AboutFragment aboutFragment;
    private MessageNewFragment messageNewFragment;

    private String fragmentName = "main";

    public String backButtonStatus = "backToMain";
    public String messageListStatus = "";

    //按2次返回退出
    private boolean hasPressedBackOnce = false;
    //back toast
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置当前窗体为全屏显示
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);
        //隐藏动作条
        getSupportActionBar().hide();


        bindView();
        initMainFragment();


        // 模拟弹窗
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(MainActivity.this, IDCardActivity.class);
//                intent.putExtra("name", "李四");
//                intent.putExtra("sex", "男");
//                intent.putExtra("birthday", "1994年12月12日");
//                intent.putExtra("address", "浙江省嘉兴市南湖区xx小区xx幢xx室");
//                intent.putExtra("idCard", "330199412120111");
//                startActivity(intent);
//            }
//        },1000);

        // 模拟弹窗
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DangerActivity.class);
                startActivity(intent);
            }
        },1000);

    }

    public void onBackPressed() {

        if (!hasPressedBackOnce) {
            backToast = Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT);
            backToast.show();
            hasPressedBackOnce = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasPressedBackOnce = false;
                }
            },2500);
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

    public void initMainFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mainFragment == null){
            mainFragment = new MainFragment();
        }
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

    public void initMessageFragment(String tg){
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

    public void initLogFragment(String tg){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (logFragment == null){
            logFragment = new LogFragment(tg);
//        }
        transaction.replace(R.id.main_frame_layout, logFragment);
        transaction.commit();

        showPageBar();
        fragmentName = "log";
    }

    public void initSettingFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (settingFragment == null){
            settingFragment = new SettingFragment();
        }
        transaction.replace(R.id.main_frame_layout, settingFragment);
        transaction.commit();

        showBackBar();
        fragmentName = "setting";
    }

    public void initAboutFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (aboutFragment == null){
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

    public void initNewFragment(String tg){
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

    public void sendMessage() {

        String receiver = messageNewFragment.getReceiver();
        String content = messageNewFragment.getContent();

        if (receiver.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this,"提示", "收件人为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        if (content.isEmpty()) {
            QHDialog qhDialog = new QHDialog(this,"提示", "短信内容为空！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        if (Constant.MESSAGE_CONTENT_MAX_LENGTH != 0 && content.length() > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
            QHDialog qhDialog = new QHDialog(this,"提示", "短信内容长度超出最大值（" + Constant.MESSAGE_CONTENT_MAX_LENGTH + "）！");
            qhDialog.setOnlyOneButtonText("好的");
            qhDialog.show();
            return;
        }

        QHDialog qhDialog = new QHDialog(this,"提示", "短信发送成功");
        qhDialog.setPositiveButton("ok", 0, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                backToMessageFragment();
                dialog.dismiss();
            }
        });
        qhDialog.show();
    }

}
