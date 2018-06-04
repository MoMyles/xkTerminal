package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.SoundPlay;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageDialogActivity extends Activity {

    public static final int TYPE_RESCUE = 0;
    public static final int TYPE_ALERT = 1;
    public static final int TYPE_CALL_ROLL = 2;
    public static final int TYPE_ALARM = 3;

    @BindView(R.id.rescue_content_tv) TextView rescue_content_tv;
    @BindView(R.id.confirm_button) Button confirm_bt;
    @BindView(R.id.tv_title) TextView tv_title;

    int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_dialog);
        ButterKnife.bind(this);

        // type 0: 救护, 1: 报警提醒, 2: 夜间点名, 3: 告警信息
        type = getIntent().getIntExtra("type", -1);
        switch (type) {
            case TYPE_RESCUE:
                tv_title.setText("救护短信");
                SoundPlay.startAlertSound(MessageDialogActivity.this);
                break;
            case TYPE_ALERT:
                tv_title.setText("报警提醒");
                break;
            case TYPE_CALL_ROLL:
                tv_title.setText("夜间点名");
                break;
            case TYPE_ALARM:
                tv_title.setText("告警信息");
                break;
        }

        rescue_content_tv.setText(getIntent().getStringExtra("content"));
        confirm_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRescue();
                finish();
            }
        });
    }

    private void confirmRescue() {
        switch (type) {
            case TYPE_RESCUE:
                MyApplication myApplication = (MyApplication) getApplication();
                myApplication.sendBytes(AlertFormat.format("00100000", "00000000"));
                myApplication.sendLightOn(false);
                break;
            case TYPE_ALERT:
                //
                break;
            case TYPE_CALL_ROLL:
                byte[] bytes = MessageFormat.format(Constant.SERVER_BD_NUMBER, "1", MessageFormat.MESSAGE_TYPE_CALL_THE_ROLL, 0);
                MyApplication.getInstance().sendBytes(bytes);
                break;
        }
    }

    public void onBackPressed() {

    }

    protected void onDestroy() {
        SoundPlay.stopAlertSound();
        super.onDestroy();
    }
}
