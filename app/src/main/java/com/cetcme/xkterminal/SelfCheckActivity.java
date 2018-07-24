package com.cetcme.xkterminal;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ByteUtil;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelfCheckActivity extends Activity {

    @BindView(R.id.tv_circle_1)
    TextView tv_circle_1;
    @BindView(R.id.tv_circle_2)
    TextView tv_circle_2;

    @BindView(R.id.tv_text_1)
    TextView tv_text_1;
    @BindView(R.id.tv_text_2)
    TextView tv_text_2;

    @BindView(R.id.tv_result)
    TextView tv_result;

    @BindView(R.id.loading)
    ProgressBar loading;

    int successColor;
    int failColor;

    int[] checkResultArr = new int[] {0, 0}; // 默认0， 成功1， 不成功2
    boolean checking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_check);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        successColor = getResources().getColor(R.color.check_success);
        failColor = getResources().getColor(R.color.check_fail);

        tv_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 3分组内没有收到 则显示失败
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post("self_check_timeout");
            }
        }, 3 * 60 * 1000);

        send0A();
//        send04_12();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO: test

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("apiType", "self_check_1");
//                    EventBus.getDefault().post(new SmsEvent(jsonObject));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 3000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("apiType", "self_check_2");
//                    EventBus.getDefault().post(new SmsEvent(jsonObject));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 5000);

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setCircleStatus(TextView tv, boolean success) {
        tv.setTextColor(success ? successColor : failColor);
        tv.setBackgroundResource(success ? R.mipmap.icon_check_success : R.mipmap.icon_check_fail);
    }

    private void setTextStatus(TextView tv, boolean success) {
        tv.setTextColor(success ? successColor : failColor);
    }

    private void setStepStatus(int step, boolean success) {
        switch (step) {
            case 1:
                setCircleStatus(tv_circle_1, success);
                setTextStatus(tv_text_1, success);
                break;
            case 2:
                setCircleStatus(tv_circle_2, success);
                setTextStatus(tv_text_2, success);
                break;
        }
    }

    private void stopCheck(boolean success) {
        loading.setVisibility(View.GONE);
        tv_result.setVisibility(View.VISIBLE);
        tv_result.setTextColor(success ? successColor : failColor);
        tv_result.setText(success ? "自检成功，点击返回" : "自检失败，点击返回");
        if (success) EventBus.getDefault().post("check_ok");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String str) {
        switch (str) {
            case "self_check_timeout":
                for (int i = 0; i < checkResultArr.length; i++) {
                    if (checkResultArr[i] == 0) {
                        setStepStatus(i + 1, false);
                        stopCheck(false);
                    }
                }
                checking = false;
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SmsEvent event) {
        try {
            JSONObject receiveJson = event.getReceiveJson();
            String type = receiveJson.getString("apiType");
            switch (type) {
                case "self_check_1":
                    if (!checking) return;
                    setStepStatus(1, true);
                    checkResultArr[0] = 1;
                    checkResult();
                    send04_12();
                    break;
                case "self_check_2":
                    if (!checking) return;
                    setStepStatus(2, true);
                    checkResultArr[1] = 1;
                    checkResult();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkResult() {
        int resultInt = 0;
        for (int i = 0; i < 2; i++) {
            if (checkResultArr[i] == 0) {
                return;
            } else if (checkResultArr[i] == 1) {
                resultInt = resultInt == 2 ? 2 : 1;
            } else if (checkResultArr[i] == 2) {
                resultInt = 2;
            }
        }

        stopCheck(resultInt == 1);
    }

    private void send0A() {
        byte[] bytes = "$0A".getBytes();
        bytes = ByteUtil.byteMerger(bytes, new byte[] {0x01});
        bytes = ByteUtil.byteMerger(bytes, "*hh".getBytes());
        bytes = ByteUtil.byteMerger(bytes, "\r\n".getBytes());
        MyApplication.getInstance().sendBytes(bytes);
    }

    private void send04_12() {

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

        MainActivity.sendCheckAndMapMessage();
    }
}
