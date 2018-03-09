package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MainFragment extends Fragment{

    private LinearLayout main_layout;
    private LinearLayout alert_layout;

    private QMUIRoundButton alert_confirm_btn;
    private TextView alert_tv;

    private boolean alert_need_flash = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main,container,false);
        EventBus.getDefault().register(this);

        main_layout = view.findViewById(R.id.main_layout);
        alert_layout = view.findViewById(R.id.alert_layout);

        if (PreferencesUtils.getBoolean(getActivity(), "homePageAlertView")) {
            showAlertLayout();
        } else {
            showMainLayout();
        }

        alert_confirm_btn = view.findViewById(R.id.alert_confirm_btn);
        alert_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", false);
                showMainLayout();
            }
        });

        alert_tv = view.findViewById(R.id.alert_tv);

        view.findViewById(R.id.app_name_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("开始播放");
//                if (player != null && player.isPlaying()) {
//                    player.stop();
//                    player.release();
//                    player = null;
//                    return;
//                }
//                player = MediaPlayer.create(getActivity(), R.raw.talkroom_begin);
                player.start();

            }
        });

        player = MediaPlayer.create(getActivity(), R.raw.alert);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (needAlertSound) {
                    player.start();
                }
                System.out.println("播放完毕00000000000000000");
            }
        });

        return view;
    }

    MediaPlayer player;
    boolean needAlertSound = false;

    private void showMainLayout() {
        alert_need_flash = false;
        needAlertSound = false;
        main_layout.setVisibility(View.VISIBLE);
        alert_layout.setVisibility(View.GONE);
    }

    private void showAlertLayout() {
        main_layout.setVisibility(View.GONE);
        alert_layout.setVisibility(View.VISIBLE);
        alert_need_flash = true;
        if (Constant.ALERT_FLASH_TIME != 0) {
            alert_tv.setVisibility(View.INVISIBLE);
            new TimeHandler().start();
        }
    }

    private void alertSound() {
        needAlertSound = true;
        player.start();
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
                case "alertSound":
                    alertSound();
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


    class TimeHandler extends Thread{
        @Override
        public void run() {
            super.run();
            int i = 0;
            do {

                try {
                    Thread.sleep(100);
                }
                catch (Exception e) {

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
    private Handler handler = new Handler(){
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



}
