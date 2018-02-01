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
 * Created by qiuhong on 10/01/2018.
 */

public class MessageBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_new;
    private Button button_detail;
    private Button button_relay;
    private Button button_prev;
    private Button button_next;
    public Button button_back;

    private ArrayList<Button> buttons = new ArrayList<>();

    public MessageBar(Context context) {
        super(context);
    }

    public MessageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_message_view, this, true);

        bindView(view);

    }

    private void bindView(View view) {
        button_new      = view.findViewById(R.id.button_new);
        button_detail   = view.findViewById(R.id.button_detail);
        button_relay    = view.findViewById(R.id.button_relay);
        button_prev     = view.findViewById(R.id.button_prev);
        button_next     = view.findViewById(R.id.button_next);
        button_back     = view.findViewById(R.id.button_back);

        buttons.add(button_new);
        buttons.add(button_detail);
        buttons.add(button_relay);
        buttons.add(button_prev);
        buttons.add(button_next);
        buttons.add(button_back);

        button_detail.setVisibility(GONE);
        button_relay.setVisibility(GONE);

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
            case R.id.button_new:
                mainActivity.initNewFragment("new");
                break;
            case R.id.button_detail:
                mainActivity.initNewFragment("detail");
                break;
            case R.id.button_relay:
                mainActivity.initNewFragment("relay");
                break;
            case R.id.button_prev:
                mainActivity.prevPage();
                break;
            case R.id.button_next:
                mainActivity.nextPage();
                break;
            case R.id.button_back:
                mainActivity.initMainFragment();
                break;
            default:
                break;
        }
    }

    public void setNextButtonEnable(boolean enable) {
        button_next.setEnabled(enable);
        if (enable) {
            button_next.setTextColor(0xFF000000);
        } else {
            button_next.setTextColor(0xFFA8A8A8);
        }
    }

    public void setPrevButtonEnable(boolean enable) {
        button_prev.setEnabled(enable);
        if (enable) {
            button_prev.setTextColor(0xFF000000);
        } else {
            button_prev.setTextColor(0xFFA8A8A8);
        }
    }

    public void setDetailAndRelayButtonEnable(boolean enable) {
        button_detail.setEnabled(enable);
        button_relay.setEnabled(enable);
        if (enable) {
            button_detail.setTextColor(0xFF000000);
            button_relay.setTextColor(0xFF000000);
        } else {
            button_detail.setTextColor(0xFFA8A8A8);
            button_relay.setTextColor(0xFFA8A8A8);
        }
    }

}
