package com.cetcme.xkterminal;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.MyClass.SoundPlay;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RescueMessageActivity extends Activity {

    @BindView(R.id.rescue_content_tv) TextView rescue_content_tv;
    @BindView(R.id.confirm_button) Button confirm_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_message);
        ButterKnife.bind(this);
        SoundPlay.startAlertSound(RescueMessageActivity.this);

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
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.sendBytes(AlertFormat.format("00100000", "00000000"));
        myApplication.sendLightOn(false);
    }

    public void onBackPressed() {

    }

    protected void onDestroy() {
        SoundPlay.stopAlertSound();
        super.onDestroy();
    }
}
