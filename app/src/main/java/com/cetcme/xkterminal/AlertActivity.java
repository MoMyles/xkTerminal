package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.RealmModels.Alert;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;

public class AlertActivity extends Activity implements View.OnClickListener{

    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;
    private CheckBox checkBox6;
    private CheckBox checkBox7;
    private CheckBox checkBox8;


    private Button confirm_button;
    private Realm realm;

    boolean needDismissActivity = true;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);

        realm = ((MyApplication) getApplication()).realm;

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

                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isChecked()) {
                        String danger = getResources().getStringArray(R.array.dangerType)[i];
                        System.out.println(danger);
                    }
                }
                Toast.makeText(AlertActivity.this, "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                addAlertLog();
                onBackPressed();
            }
        });
    }

    protected void onDestroy() {
        needDismissActivity = false;
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        needDismissActivity = false;
    }

    private void addAlertLog() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Alert alert = realm.createObject(Alert.class);
                alert.setDeleted(false);
                alert.setType("火灾、碰撞");
                alert.setTime(new Date());
            }
        });
    }
}
