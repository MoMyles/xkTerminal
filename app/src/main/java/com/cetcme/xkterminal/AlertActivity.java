package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.Sqlite.Proxy.AlertProxy;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.util.ArrayList;

public class AlertActivity extends Activity implements View.OnClickListener{

    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;
    private CheckBox checkBox6;
    private CheckBox checkBox7;
    private CheckBox checkBox8;

    private Button cancel_button;
    private Button confirm_button;

    private DbManager db;

    boolean needDismissActivity = true;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        db = ((MyApplication) getApplication()).db;

        bindView();

        if (Constant.ALERT_REMAIN_TIME != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (needDismissActivity) {
                        onBackPressed();
                    }
                }
            }, Constant.ALERT_REMAIN_TIME);
        }
    }

    private void bindView() {

        findViewById(R.id.close_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox6 = findViewById(R.id.checkBox6);
        checkBox7 = findViewById(R.id.checkBox7);
        checkBox8 = findViewById(R.id.checkBox8);

        checkBoxes.add(checkBox1);
        checkBoxes.add(checkBox2);
        checkBoxes.add(checkBox3);
        checkBoxes.add(checkBox4);
        checkBoxes.add(checkBox5);
        checkBoxes.add(checkBox6);
        checkBoxes.add(checkBox7);
        checkBoxes.add(checkBox8);

        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setOnClickListener(this);
        }

        confirm_button = findViewById(R.id.confirm_button);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = "";
                for (int i = 0; i < checkBoxes.size(); i++) {
                    String singleType = checkBoxes.get(i).isChecked() ? "1" : "0";
                    type = singleType + type;
                }
                ((MyApplication) getApplication()).sendBytes(AlertFormat.format("00000001", type));

                addAlertLog(type);
                onBackPressed();
            }
        });

        cancel_button = findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    protected void onDestroy() {
        needDismissActivity = false;
        PreferencesUtils.putBoolean(this, "homePageAlertView", true);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("apiType", "showAlertInHomePage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new SmsEvent(jsonObject));
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        needDismissActivity = false;
    }

    private void addAlertLog(final String type) {
        AlertProxy.insert(db, AlertFormat.getStringType(type), Constant.SYSTEM_DATE, false);
    }
}
