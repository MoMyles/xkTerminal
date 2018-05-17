package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MapMainActivity;
import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class BottomBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_receive;
    private Button button_send;
    private Button button_sign;
    private Button button_alert;
    private Button button_setting;
    private Button button_navigate;
    private Button button_about;

    private ArrayList<Button> buttons = new ArrayList<>();

    public BottomBar(Context context) {
        super(context);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_bottom_view, this, true);

        bindView(view);

    }

    private void bindView(View view) {
        button_receive  = view.findViewById(R.id.button_receive);
        button_send     = view.findViewById(R.id.button_send);
        button_sign     = view.findViewById(R.id.button_sign);
        button_alert    = view.findViewById(R.id.button_alert);
        button_setting  = view.findViewById(R.id.button_setting);
        button_navigate = view.findViewById(R.id.button_navigate);
        button_about    = view.findViewById(R.id.button_about);

        buttons.add(button_receive);
        buttons.add(button_send);
        buttons.add(button_sign);
        buttons.add(button_alert);
        buttons.add(button_setting);
        buttons.add(button_navigate);
        buttons.add(button_about);

        for (Button button: buttons) {
            button.setTextColor(0xFF000000);
            button.setBackgroundResource(R.drawable.button_bg_selector);
            button.setOnClickListener(this);
//            button.setTextSize(10); //16
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_receive:
                mainActivity.initMessageFragment("send");
                break;
            case R.id.button_send:
                mainActivity.initMessageFragment("receive");
                break;
            case R.id.button_sign:
                mainActivity.initLogFragment("sign");
                break;
            case R.id.button_alert:
                mainActivity.initLogFragment("alert");
                break;
            case R.id.button_setting:
                mainActivity.initSettingFragment();
                break;
            case R.id.button_navigate:
                mainActivity.startActivity(new Intent(mainActivity, MapMainActivity.class));
                break;
            case R.id.button_about:
                mainActivity.initAboutFragment();
                break;
            default:
                break;
        }
    }
}
