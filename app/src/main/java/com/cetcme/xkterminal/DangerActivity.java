package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.qiuhong.qhlibrary.Dialog.QHDialog;

import java.util.ArrayList;

public class DangerActivity extends Activity {

    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;
    private CheckBox checkBox6;
    private CheckBox checkBox7;
    private CheckBox checkBox8;

    private Button confirm_button;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);

        bindView();
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
                Toast.makeText(DangerActivity.this, "遇险报警发送成功", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }


}
