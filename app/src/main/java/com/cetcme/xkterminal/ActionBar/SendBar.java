package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by qiuhong on 11/01/2018.
 */

public class SendBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_send;
    public Button button_back;

    private ArrayList<Button> buttons = new ArrayList<>();

    public SendBar(Context context) {
        super(context);
    }

    public SendBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_send_view, this, true);

        bindView(view);

    }

    private void bindView(View view) {
        button_send  = view.findViewById(R.id.button_send);
        button_back = view.findViewById(R.id.button_back);

        buttons.add(button_send);
        buttons.add(button_back);

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
            case R.id.button_send:
                mainActivity.sendMessage();
                break;
            case R.id.button_back:
                mainActivity.backToMessageFragment();
                break;
            default:
                break;
        }
    }
}
