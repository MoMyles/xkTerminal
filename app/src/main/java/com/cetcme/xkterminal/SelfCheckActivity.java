package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cetcme.xkterminal.Event.SmsEvent;

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
    @BindView(R.id.tv_circle_3)
    TextView tv_circle_3;
    @BindView(R.id.tv_circle_4)
    TextView tv_circle_4;

    @BindView(R.id.tv_text_1)
    TextView tv_text_1;
    @BindView(R.id.tv_text_2)
    TextView tv_text_2;
    @BindView(R.id.tv_text_3)
    TextView tv_text_3;
    @BindView(R.id.tv_text_4)
    TextView tv_text_4;

    @BindView(R.id.tv_result)
    TextView tv_result;

    @BindView(R.id.loading)
    ProgressBar loading;

    int successColor;
    int failColor;

    int[] checkResultArr = new int[] {0, 0, 0, 0}; // 默认0， 成功1， 不成功2
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

        // 30内没有收到 则显示失败
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post("self_check_timeout");
            }
        }, 10 * 1000);
    }

    @Override
    protected void onStart() {
        //TODO: test
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "self_check_1");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "self_check_2");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "self_check_3");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 12000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("apiType", "self_check_4");
                    EventBus.getDefault().post(new SmsEvent(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 33000);

        super.onStart();
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
            case 3:
                setCircleStatus(tv_circle_3, success);
                setTextStatus(tv_text_3, success);
                break;
            case 4:
                setCircleStatus(tv_circle_4, success);
                setTextStatus(tv_text_4, success);
                break;
        }
    }

    private void stopCheck(boolean success) {
        loading.setVisibility(View.GONE);
        tv_result.setVisibility(View.VISIBLE);
        tv_result.setTextColor(success ? successColor : failColor);
        tv_result.setText(success ? "自检成功，点击返回" : "自检失败，点击返回");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String str) {
        switch (str) {
            case "self_check_timeout":
                for (int i = 0; i < 4; i++) {
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
                    break;
                case "self_check_2":
                    if (!checking) return;
                    setStepStatus(2, true);
                    checkResultArr[1] = 1;
                    checkResult();
                    break;
                case "self_check_3":
                    if (!checking) return;
                    setStepStatus(3, true);
                    checkResultArr[2] = 1;
                    checkResult();
                    break;
                case "self_check_4":
                    if (!checking) return;
                    setStepStatus(4, true);
                    checkResultArr[3] = 1;
                    checkResult();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkResult() {
        int resultInt = 0;
        for (int i = 0; i < 4; i++) {
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
}
