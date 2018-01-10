package com.cetcme.xkterminal;

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
import com.cetcme.xkterminal.ActionBar.MessageBar;
import com.cetcme.xkterminal.ActionBar.PageBar;
import com.cetcme.xkterminal.Fragment.AboutFragment;
import com.cetcme.xkterminal.Fragment.LogFragment;
import com.cetcme.xkterminal.Fragment.MainFragment;
import com.cetcme.xkterminal.Fragment.MessageFragment;
import com.cetcme.xkterminal.Fragment.SettingFragment;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;
    private MessageBar messageBar;
    private PageBar pageBar;
    private BackBar backBar;

    private MainFragment mainFragment;
    private MessageFragment messageFragment;
    private LogFragment logFragment;
    private SettingFragment settingFragment;
    private AboutFragment aboutFragment;

    private String fragmentName = "main";

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
        bottomBar = findViewById(R.id.bottomBar);
        messageBar = findViewById(R.id.messageBar);
        pageBar = findViewById(R.id.pageBar);
        backBar = findViewById(R.id.backBar);

        bottomBar.mainActivity = this;
        messageBar.mainActivity = this;
        pageBar.mainActivity = this;
        backBar.mainActivity = this;

    }

    private void showMainBar() {
        bottomBar.setVisibility(View.VISIBLE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
    }

    private void showMessageBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.VISIBLE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.GONE);
    }

    private void showPageBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.VISIBLE);
        backBar.setVisibility(View.GONE);
    }

    private void showBackBar() {
        bottomBar.setVisibility(View.GONE);
        messageBar.setVisibility(View.GONE);
        pageBar.setVisibility(View.GONE);
        backBar.setVisibility(View.VISIBLE);
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
    }

    public void initMessageFragment(String tg){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (messageFragment == null){
            messageFragment = new MessageFragment(tg);
//        }
        transaction.replace(R.id.main_frame_layout, messageFragment);
        transaction.commit();

        showMessageBar();
        fragmentName = "message";
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

}
